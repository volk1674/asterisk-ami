package ru.ivolkov.asterisk.configs;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

	private static final String API_FILE_PATH = "api/v1/*.yaml";

	@Primary
	@Bean
	public SwaggerResourcesProvider swaggerResourcesProvider(ResourceLoader resourceLoader) {
		return () -> Arrays.stream(getApiSchemas(resourceLoader))
				.map(resource -> {
					final SwaggerResource swaggerResource = new SwaggerResource();
					final String filename = resource.getFilename();
					final String apiName = extractVersion(filename);
					swaggerResource.setName(apiName);
					swaggerResource.setSwaggerVersion(apiName);
					swaggerResource.setLocation("/api/v1/" + filename);
					return swaggerResource;
				})
				.sorted(Comparator.comparing(SwaggerResource::getSwaggerVersion).reversed())
				.toList();
	}

	private Resource[] getApiSchemas(ResourceLoader resourceLoader) {
		try {
			return ResourcePatternUtils
					.getResourcePatternResolver(resourceLoader)
					.getResources("classpath:" + API_FILE_PATH);
		} catch (IOException e) {
			throw new IllegalStateException("Swagger openapi not found.", e);
		}
	}

	private String extractVersion(String fileName) {
		return StringUtils.substringBefore(fileName, ".yaml");
	}

	@Bean
	public RouterFunction<ServerResponse> staticResourceRouterApi() {
		return RouterFunctions.resources("/api/v1/**", new ClassPathResource("api/v1/"));
	}

	@Bean
	public RouterFunction<ServerResponse> redirectToSwaggerPageRoute() {
		return RouterFunctions.route(RequestPredicates.GET("/swagger"), req ->
						ServerResponse.temporaryRedirect(URI.create("/swagger-ui/"))
								.build())
				.andRoute(RequestPredicates.GET("/v3/api-docs.yaml"), req ->
						ServerResponse.temporaryRedirect(URI.create("/" + API_FILE_PATH))
								.build());
	}


	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http.csrf(ServerHttpSecurity.CsrfSpec::disable);

		return http.build();
	}

}
