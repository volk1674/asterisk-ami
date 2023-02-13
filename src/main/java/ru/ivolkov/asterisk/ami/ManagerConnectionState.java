package ru.ivolkov.asterisk.ami;

public enum ManagerConnectionState {
	INITIAL,
	CONNECTING,
	CONNECTED,
	RECONNECTING,
	DISCONNECTING,
	DISCONNECTED
}
