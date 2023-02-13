package ru.ivolkov.asterisk.ami;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.Data;
import lombok.ToString;

@ToString(exclude = {"password"})
@Data
public class AsteriskClientProperties {
	private String host;
	private int port;
	private String user;
	private String password;
	private boolean ssl;
	private int timeout;
	private int readTimeout;
	private Charset encoding = StandardCharsets.UTF_8;


	private String lineSeparator = "\n";
	private Duration pingInterval = Duration.ofSeconds(20);

	public void setLineSeparator(String lineSeparator) {
		if (lineSeparator != null) {
			this.lineSeparator = lineSeparator
					.replace("\\n", "\n")
					.replace("\\r", "\r");
		}
	}
}
