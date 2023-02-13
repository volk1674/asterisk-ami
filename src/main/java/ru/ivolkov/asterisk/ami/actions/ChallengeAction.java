package ru.ivolkov.asterisk.ami.actions;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChallengeAction implements Action {
	private final String action = ActionType.CHALLENGE.getAction();
	private final String actionId;
	private final String authType;


	public static String getChallenge(ActionResponse response) {
		return response.getAttribute("challenge");
	}

}
