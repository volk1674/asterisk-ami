package ru.ivolkov.asterisk.ami.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import ru.ivolkov.asterisk.ami.AsteriskClientProperties;
import ru.ivolkov.asterisk.ami.actions.Action;
import ru.ivolkov.asterisk.ami.actions.ActionHelper;
import ru.ivolkov.asterisk.ami.actions.ActionResponse;

@Slf4j
public class AsteriskActionSender {
	private final AsteriskClientProperties properties;

	/**
	 * Все команды отправляются через один поток, что бы не связываться с блокировками
	 */
	private final ExecutorService sendActionService;

	/**
	 * Writer для socket.getOutputStream() с установленной кодировкой
	 */
	private final Writer writer;

	/**
	 * Команды отправленные на сервер по которым еще не получен ответ.
	 */
	private final ConcurrentHashMap<String, PendingActionData> pendingActions = new ConcurrentHashMap<>();


	AsteriskActionSender(AsteriskClientProperties properties, Socket socket) throws IOException {
		this.properties = properties;
		this.writer = new OutputStreamWriter(socket.getOutputStream(), properties.getEncoding());

		this.sendActionService = Executors.newSingleThreadExecutor(r -> {
			Thread thread = new Thread(r, "asterisk-write-thread");
			thread.setDaemon(true);
			return thread;
		});
	}

	public CompletableFuture<ActionResponse> sendAction(Action action) {

		CompletableFuture<ActionResponse> future = new CompletableFuture<>();

		registerPendingAction(action, future);

		String actionData = ActionHelper.serializeAction(action, properties.getLineSeparator());
		log.debug("{}", actionData);

		this.sendActionService.submit(() -> {
			try {
				writer.write(actionData);
				writer.flush();
			} catch (IOException e) {
				log.error("Failed to send action {}", action);
				pendingActions.remove(action.getActionId());
				future.completeExceptionally(e);
			}
		});

		return future;
	}

	private void registerPendingAction(Action action, CompletableFuture<ActionResponse> future) {
		PendingActionData pendingActionData = new PendingActionData();
		pendingActionData.setSendTime(Instant.now());
		pendingActionData.setFuture(future);
		pendingActions.put(action.getActionId(), pendingActionData);
	}

	public void shutdown() {
		sendActionService.shutdown();
	}

	public PendingActionData getPendingAction(String actionId) {
		return pendingActions.get(actionId);
	}
}
