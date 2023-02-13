package ru.ivolkov.asterisk.ami.actions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class PingAction implements Action {
	private final String action = ActionType.PING.getAction();
	private final String actionId;
}
