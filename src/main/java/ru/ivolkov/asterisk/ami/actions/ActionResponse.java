package ru.ivolkov.asterisk.ami.actions;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ActionResponse implements Serializable {
	@Serial
	private static final long serialVersionUID = -7983285820391533636L;

	private final Map<String, String> attributes = new HashMap<>();

	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}

	public String getAttribute(String name) {
		return attributes.get(name);
	}

}
