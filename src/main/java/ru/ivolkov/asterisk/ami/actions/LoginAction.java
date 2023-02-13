package ru.ivolkov.asterisk.ami.actions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class LoginAction implements Action {
	private final String action = ActionType.LOGIN.getAction();
	private final String actionId;
	private final String userName;
	private final String secret;
	private final String key;
	private final String authType;
	private final String events;

	@Getter
	@Setter
	public static class LoginActionResponse extends ActionResponse {

	}
}
