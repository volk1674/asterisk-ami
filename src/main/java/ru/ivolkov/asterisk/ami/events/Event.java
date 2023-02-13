package ru.ivolkov.asterisk.ami.events;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import ru.ivolkov.asterisk.ami.EventProcessor;

@Getter
@Builder
@ToString
public class Event {

	private final String name;
	private final Map<String, String> data;


	public void process(EventProcessor processor) {
		processor.processEvent(this);
	}
}
