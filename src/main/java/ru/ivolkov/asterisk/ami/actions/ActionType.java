package ru.ivolkov.asterisk.ami.actions;

import lombok.Getter;

@Getter
public enum ActionType {
	CHALLENGE("Challenge"),
	LOGIN("Login"),
	LOGOFF("Logoff"),
	ORIGINATE("Originate"),
	PING("Ping"),
	EXTENSION_STATE("ExtensionState");

	ActionType(String action) {
		this.action = action;
	}

	private final String action;
}
