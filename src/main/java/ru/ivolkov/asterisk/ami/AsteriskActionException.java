package ru.ivolkov.asterisk.ami;

import lombok.Getter;
import ru.ivolkov.asterisk.ami.actions.ActionResponse;

@Getter
public class AsteriskActionException extends RuntimeException {
	private final ActionResponse response;

	public AsteriskActionException(ActionResponse response) {
		this.response = response;
	}
}
