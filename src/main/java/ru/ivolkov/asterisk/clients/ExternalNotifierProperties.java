package ru.ivolkov.asterisk.clients;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("external-notifier")
@Data
public class ExternalNotifierProperties {
	/**
	 * URL на которые надо отправлять нотификации
	 */
	private String url;

	private List<ExternalNotifierEventProperties> events;


	@Data
	public static class ExternalNotifierEventProperties {
		/**
		 * Название события
		 */
		private String name;

		/**
		 * Фильтры по которым отбираются события AMI
		 */
		private List<EventFilter> filters = new ArrayList<>();

		/**
		 * Маппинг названий полей события. Поля, не добавленные в маппинг, отправляться не будут.
		 */
		private Map<String, String> fieldsMapping = new HashMap<>();
	}

}
