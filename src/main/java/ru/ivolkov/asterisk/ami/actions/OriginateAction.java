package ru.ivolkov.asterisk.ami.actions;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class OriginateAction implements Action {
	private final String action = ActionType.ORIGINATE.getAction();
	private final String actionId;
	private final String channel;
	private final String context;
	private final Integer priority;
	private final String exten;
	private final String application;
	private final String data;
	private final Long timeout;
	private final String callerID;
	private final Map<String, String> variables;
	private final String account;
	private Boolean earlyMedia;
	private Boolean async;
	private String codecs;
	private String channelId;
	private String otherChannelId;
}
