package ru.ivolkov.asterisk.clients;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.ivolkov.asterisk.ami.events.Event;

@Data()
public class EventFilter {
	private String name;
	private String value;
	private String regex;

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
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
