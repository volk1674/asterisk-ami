package ru.ivolkov.asterisk.ami.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Mono;
import ru.ivolkov.asterisk.ami.AsteriskActionException;
import ru.ivolkov.asterisk.ami.AsteriskClient;
import ru.ivolkov.asterisk.ami.AsteriskClientProperties;
import ru.ivolkov.asterisk.ami.AsteriskException;
import ru.ivolkov.asterisk.ami.EventProcessor;
import ru.ivolkov.asterisk.ami.ManagerConnectionState;
import ru.ivolkov.asterisk.ami.actions.ActionResponse;
import ru.ivolkov.asterisk.ami.actions.ChallengeAction;
import ru.ivolkov.asterisk.ami.actions.ExtensionStateAction;
import ru.ivolkov.asterisk.ami.actions.LoginAction;
import ru.ivolkov.asterisk.ami.actions.LogoffAction;
import ru.ivolkov.asterisk.ami.actions.OriginateAction;
import ru.ivolkov.asterisk.ami.actions.PingAction;
import ru.ivolkov.asterisk.ami.events.Event;
import ru.ivolkov.asterisk.api.v1.dto.GetExtensionStateResponse;
import ru.ivolkov.asterisk.api.v1.dto.OriginateRequest;
import ru.ivolkov.asterisk.api.v1.dto.OriginateResponse;


@Slf4j
public class AsteriskClientImpl implements AsteriskClient {
	private final AsteriskClientProperties properties;

	private final AtomicLong actionIdGenerator = new AtomicLong(System.currentTimeMillis());
	private final ScheduledExecutorService pingAsteriskThread = Executors.newSingleThreadScheduledExecutor();

	private Socket socket;
	private Scanner scanner;
	private Thread readerThread;

	private AsteriskActionSender sender;
	private ManagerConnectionState state = ManagerConnectionState.INITIAL;
	private ScheduledFuture<?> pingTask = null;

	private final List<EventProcessor> processors;

	// Сохранил на всякий случай. Возможно потребуется.
	private volatile String version;

	public AsteriskClientImpl(AsteriskClientProperties properties, List<EventProcessor> processors) {
		this.properties = properties;
		if (log.isDebugEnabled()) {
			log.debug("asterisk ami client created with config: {}", properties);
		}

		this.processors = processors == null ? List.of() : processors;
	}

	private String generateActionId() {
		return String.valueOf(actionIdGenerator.incrementAndGet());
	}

	@Override
	public synchronized void login() throws IOException {
		try {
			log.debug("Login");

			connect();

			ChallengeAction challengeAction = ChallengeAction.builder()
					.actionId(generateActionId())
					.authType("MD5")
					.build();

			ActionResponse challengeResponse = sender.sendAction(challengeAction).get();

			MessageDigest md = MessageDigest.getInstance("MD5");
			String challenge = ChallengeAction.getChallenge(challengeResponse);

			if (challenge != null) {
				md.update(challenge.getBytes(properties.getEncoding()));
			}
			if (properties.getPassword() != null) {
				md.update(properties.getPassword().getBytes(properties.getEncoding()));
			}

			String key = HexFormat.of().formatHex(md.digest());

			LoginAction loginAction = LoginAction.builder()
					.actionId(generateActionId())
					.key(key)
					.authType("MD5")
					.userName(properties.getUser())
					.events("on")
					.build();

			sender.sendAction(loginAction).get();

			log.info("Login success");

			if (this.pingTask == null) {
				this.pingTask = pingAsteriskThread.scheduleWithFixedDelay(this::ping, 10, 10, TimeUnit.SECONDS);
			}

			state = ManagerConnectionState.CONNECTED;
		} catch (IOException e) {
			disconnect();
			throw e;
		} catch (NoSuchAlgorithmException e) {
			log.warn("Login error", e);
			throw new AsteriskException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AsteriskException(e);
		} catch (ExecutionException e) {
			log.warn("Login error", e);
			disconnect();
			if (e.getCause() instanceof AsteriskActionException ex) {
				throw ex;
			} else {
				throw new AsteriskException(e.getCause());
			}
		}
	}

	private synchronized void ping() {
		try {
			log.debug("Ping server");

			if (state == ManagerConnectionState.DISCONNECTED) {
				login();
			}

			PingAction action = PingAction.builder()
					.actionId(generateActionId())
					.build();

			sender.sendAction(action).get();
			log.debug("Ping server success");

		} catch (InterruptedException ex) {
			log.error(ex.getMessage(), ex);
			Thread.currentThread().interrupt();
		} catch (ExecutionException | IOException ex) {
			log.error(ex.getMessage(), ex);
			disconnect();
		}
	}

	@Override
	public synchronized void logoff() {

		if (this.pingTask != null) {
			this.pingTask.cancel(true);
			this.pingTask = null;
		}

		if (sender != null) {
			sender.sendAction(LogoffAction.builder()
							.actionId(generateActionId())
							.build())
					.thenAccept(logoffActionResponse -> {
						log.info("Logoff success");
						disconnect();
					});
		}
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public ManagerConnectionState getState() {
		return state;
	}

	@Override
	public Mono<OriginateResponse> originate(OriginateRequest request) {

		Map<String, String> variables = new HashMap<>();

		OriginateAction action = OriginateAction.builder()
				.actionId(generateActionId())
				.channel(request.getChannel())
				.exten(request.getExten())
				.context(request.getContext())
				.priority(request.getPriority())
				.variables(variables)
				.async(true)
				.build();

		return Mono.fromFuture(sender.sendAction(action))
				.map(response -> OriginateResponse.builder()
						.success(true).build())
				.onErrorResume(throwable -> Mono.just(OriginateResponse.builder()
						.success(false).build()));
	}

	@Override
	public Mono<GetExtensionStateResponse> getExtensionState(String exten, String context) {

		ExtensionStateAction action = ExtensionStateAction.builder()
				.actionId(generateActionId())
				.exten(exten)
				.context(context)
				.build();

		return Mono.fromFuture(sender.sendAction(action))
				.map(response -> GetExtensionStateResponse.builder()
						.exten(exten)
						.context(ExtensionStateAction.getContext(response))
						.status(ExtensionStateAction.getStatus(response))
						.statusText(ExtensionStateAction.getStatusText(response))
						.hint(ExtensionStateAction.getHint(response))
						.build());
	}

	private synchronized void connect() throws IOException {
		log.debug("connect");

		if (socket == null) {
			state = ManagerConnectionState.CONNECTING;

			if (properties.isSsl()) {
				socket = SSLSocketFactory.getDefault().createSocket();
			} else {
				socket = SocketFactory.getDefault().createSocket();
			}

			socket.setSoTimeout(properties.getTimeout());
			socket.connect(new InetSocketAddress(properties.getHost(), properties.getPort()), properties.getTimeout());
			log.debug("connected to {}:{}", properties.getHost(), properties.getPort());

			scanner = new Scanner(new BufferedReader(new InputStreamReader(socket.getInputStream(), properties.getEncoding())));

			readerThread = new Thread(this::readerThreadLoop, "asterisk-read-thread");
			readerThread.setDaemon(true);
			readerThread.start();

			sender = new AsteriskActionSender(properties, socket);
		}

		log.debug("connect success");
	}

	private synchronized void disconnect() {
		version = null;

		if (readerThread != null) {
			readerThread.interrupt();
			readerThread = null;
		}

		if (sender != null) {
			sender.shutdown();
			sender = null;
		}

		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				log.debug("Socket close threw an exception.", e);
			}

			socket = null;
		}

		state = ManagerConnectionState.DISCONNECTED;
	}

	private void saveVersion(String version) {
		this.version = version;
		log.info(version);
	}

	private void readerThreadLoop() {
		log.debug("readerThreadLoop started");

		// Первая строка версия Asterisk Call Manager, но возможно это справедливо не для всех версий AMI

		String line = scanner.nextLine();
		while (line.isBlank()) {
			line = scanner.nextLine();
			log.debug(line);
		}

		saveVersion(line);

		// Бесконечный цикл чтения входящих сообщений
		Map<String, String> buf = new HashMap<>();
		while (!Thread.interrupted()) {

			try {
				line = scanner.nextLine();
				log.debug(line);

				String[] kv = line.split(": *", 2);
				if (kv.length == 2) {
					buf.put(kv[0].toLowerCase(), kv[1]);
				} else if (line.isEmpty()) {
					if (buf.containsKey("Response".toLowerCase())) {
						String response = buf.get("Response".toLowerCase());
						String actionId = buf.get("ActionID".toLowerCase());

						PendingActionData data = (sender != null) ? sender.getPendingAction(actionId) : null;

						if (data != null) {
							ActionResponse obj = new ActionResponse();
							buf.forEach(obj::setAttribute);

							if ("Success".equals(response)) {
								data.getFuture()
										.complete(obj);
							} else {
								data.getFuture()
										.completeExceptionally(new AsteriskActionException(obj));
							}
						}
					} else if (buf.containsKey("Event".toLowerCase())) {

						String name = buf.get("Event".toLowerCase());

						Event event = Event.builder()
								.name(name)
								.data(Map.copyOf(buf))
								.build();

						processors.forEach(event::process);
					}

					buf.clear();
				} else {
					log.warn("Something is probably wrong: {}", line);
				}

			} catch (NoSuchElementException ex) {
				disconnect();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				disconnect();
			}
		}

		log.debug("readerThreadLoop finished");
	}

	@EventListener
	public void onContextRefreshedEvent(ContextRefreshedEvent ignoredEvent) {
		try {
			login();
		} catch (IOException e) {
			throw new AsteriskException(e);
		}
	}

	@EventListener
	public void onContextClosedEvent(ContextClosedEvent ignoredEvent) {
		logoff();
	}


}
