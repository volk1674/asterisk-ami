package ru.ivolkov.asterisk.clients;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("call-notification")
@Data
public class IncomingCallNotifierProperties {
	/**
	 * URL на которые надо отправлять нотификации
	 */
	private String url;

	/**
	 * Фильтры по которым отбираются события AMI
	 */
	private List<EventFilter> filters = new ArrayList<>();


}
