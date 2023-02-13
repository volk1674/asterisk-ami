package ru.ivolkov.asterisk.ami;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ivolkov.asterisk.ami.impl.AsteriskClientImpl;

@Configuration(proxyBeanMethods = false)
public class AsteriskClientConfiguration {

	@ConfigurationProperties("asterisk.client")
	@Bean
	AsteriskClientProperties asteriskClientProperties() {
		return new AsteriskClientProperties();
	}

	@Bean
	AsteriskClient asteriskClient(AsteriskClientProperties properties, @Autowired(required = false) List<EventProcessor> processors) {
		return new AsteriskClientImpl(properties, processors);
	}

}
