package ru.ivolkov.asterisk.ami;

import ru.ivolkov.asterisk.ami.events.Event;

public interface EventProcessor {

	void processEvent(Event event);
	
}
