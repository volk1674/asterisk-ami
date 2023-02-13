package ru.ivolkov.asterisk.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ivolkov.asterisk.ami.EventProcessor;
import ru.ivolkov.asterisk.ami.events.Event;

/**
 * Отлавливает событие входящего звонка и отправляет информацию о входящем звонке по REST внешнему приложению.
 * Адрес внешнего приложения должен быть задан в конфигурации.
 */
@Slf4j
@Component
public class IncomingCallNotifier implements EventProcessor {

	private final IncomingCallNotifierProperties properties;
	private final WebClient webClient;

	public IncomingCallNotifier(IncomingCallNotifierProperties properties) {
		this.properties = properties;
		this.webClient = WebClient.builder()
				.baseUrl(properties.getUrl())
				.build();
	}

	@Override
	public void processEvent(Event event) {
		for (EventFilter eventFilter : properties.getFilters()) {
			if (!eventFilter.test(event)) {
				return;
			}
		}

		log.info("processEvent: {}", event);
		try {
			webClient.post()
					.bodyValue(event)
					.retrieve()
					.bodyToMono(Void.class)
					.block();
		} catch (Exception ex) {
			log.error("Failed to send event to url {}", properties.getUrl(), ex);
		}
	}
}
