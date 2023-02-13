package ru.ivolkov.asterisk.ami;

public class AsteriskException extends RuntimeException {

	public AsteriskException() {
	}

	public AsteriskException(String message) {
		super(message);
	}

	public AsteriskException(String message, Throwable cause) {
		super(message, cause);
	}

	public AsteriskException(Throwable cause) {
		super(cause);
	}

	public AsteriskException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
