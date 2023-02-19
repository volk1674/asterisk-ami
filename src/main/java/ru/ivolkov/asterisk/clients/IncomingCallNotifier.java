package ru.ivolkov.asterisk.clients;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ivolkov.asterisk.ami.EventProcessor;
import ru.ivolkov.asterisk.ami.events.Event;
import ru.ivolkov.asterisk.clients.ExternalNotifierProperties.ExternalNotifierEventProperties;

/**
 * Отлавливает событие входящего звонка и отправляет информацию о входящем звонке по REST внешнему приложению.
 * Адрес внешнего приложения должен быть задан в конфигурации.
 */
@Slf4j
@Component
public class IncomingCallNotifier implements EventProcessor {

	private final ExternalNotifierProperties properties;
	private final WebClient webClient;

	public IncomingCallNotifier(ExternalNotifierProperties properties) {
		this.properties = properties;
		this.webClient = WebClient.builder()
				.baseUrl(properties.getUrl())
				.build();
	}

	@Override
	public void processEvent(Event event) {
		log.debug("processEvent: {}", event);

		for (ExternalNotifierEventProperties eventProperties : properties.getEvents()) {
			boolean suitable = true;
			for (EventFilter eventFilter : eventProperties.getFilters()) {
				if (!eventFilter.test(event)) {
					suitable = false;
					break;
				}
			}

			if (suitable) {
				try {
					Event mappedEvent = event;
					var fieldsMapping = eventProperties.getFieldsMapping();
					if (!fieldsMapping.isEmpty()) {
						Map<String, String> mappedFields = new HashMap<>();
						event.getData().forEach((k, v) -> {
							String nk = fieldsMapping.get(k);
							if (nk != null) {
								mappedFields.put(nk, v);
							}
						});
						mappedEvent = Event.builder().name(eventProperties.getName())
								.data(mappedFields)
								.build();
					}

					webClient.post()
							.bodyValue(mappedEvent)
							.retrieve()
							.bodyToMono(Void.class)
							.block();
				} catch (Exception ex) {
					log.error("Failed to send event to url {}", properties.getUrl(), ex);
				}
			}
		}
	}
}
