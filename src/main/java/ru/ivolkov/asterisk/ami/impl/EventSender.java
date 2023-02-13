package ru.ivolkov.asterisk.ami.impl;

import lombok.extern.slf4j.Slf4j;
import ru.ivolkov.asterisk.ami.AsteriskClientProperties;
import ru.ivolkov.asterisk.ami.events.Event;

@Slf4j
public class EventSender {
	private final AsteriskClientProperties properties;

	public EventSender(AsteriskClientProperties properties) {
		this.properties = properties;
	}

	public void processEvent(Event event) {

	}
}
