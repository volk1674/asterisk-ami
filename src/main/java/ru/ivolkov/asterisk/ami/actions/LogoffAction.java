package ru.ivolkov.asterisk.ami.actions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class LogoffAction implements Action {
	private final String action = ActionType.LOGOFF.getAction();
	private final String actionId;

}
