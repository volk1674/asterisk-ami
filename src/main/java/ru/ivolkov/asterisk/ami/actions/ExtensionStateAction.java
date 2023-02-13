package ru.ivolkov.asterisk.ami.actions;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExtensionStateAction implements Action {
	private final String action = ActionType.EXTENSION_STATE.getAction();
	private final String actionId;
	private final String exten;
	private final String context;

	public static String getContext(ActionResponse response) {
		return response.getAttribute("Context".toLowerCase());
	}

	public static Integer getStatus(ActionResponse response) {
		return Integer.valueOf(response.getAttribute("Status".toLowerCase()));
	}

	public static String getStatusText(ActionResponse response) {
		return response.getAttribute("StatusText".toLowerCase());
	}

	public static String getHint(ActionResponse response) {
		return response.getAttribute("Hint".toLowerCase());
	}
}
