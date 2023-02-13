package ru.ivolkov.asterisk.clients;

import java.util.regex.Pattern;
import lombok.Data;
import ru.ivolkov.asterisk.ami.events.Event;

@Data
public class EventFilter {
	private String name;
	private String value;
	private String regex;

	private Pattern pattern;

	boolean testRegex(String data) {
		if (pattern == null) {
			pattern = Pattern.compile(regex);
		}

		return pattern.matcher(data).find();
	}

	public boolean test(Event event) {
		String data = event.getData().get(name.toLowerCase());
		return (value != null && value.equalsIgnoreCase(data)) || (regex != null && testRegex(data));
	}
}
