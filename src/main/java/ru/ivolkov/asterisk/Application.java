package ru.ivolkov.asterisk;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import ru.ivolkov.asterisk.clients.IncomingCallNotifierProperties;

@EnableConfigurationProperties(IncomingCallNotifierProperties.class)
@EnableWebFluxSecurity
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
