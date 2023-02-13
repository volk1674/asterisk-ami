package ru.ivolkov.asterisk.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.ivolkov.asterisk.ami.AsteriskClient;
import ru.ivolkov.asterisk.api.v1.dto.GetExtensionStateResponse;
import ru.ivolkov.asterisk.api.v1.dto.GetStatusResponse;
import ru.ivolkov.asterisk.api.v1.dto.OriginateRequest;
import ru.ivolkov.asterisk.api.v1.dto.OriginateResponse;

@RequiredArgsConstructor
@Slf4j
@RestController()
@RequestMapping(path = "/api/v1", produces = {MediaType.APPLICATION_JSON_VALUE})
public class AsteriskController {

	private final AsteriskClient asteriskClient;

	@GetMapping("/state")
	public Mono<GetStatusResponse> getState() {
		return Mono.just(GetStatusResponse.builder()
				.state(GetStatusResponse.StateEnum.fromValue(asteriskClient.getState().name()))
				.version(asteriskClient.getVersion())
				.build());
	}

	@GetMapping(value = "/extension/state", produces = "application/json")
	public Mono<GetExtensionStateResponse> getExtensionState(@RequestParam("exten") String exten, @RequestParam(value = "context", required = false) String context) {
		return asteriskClient.getExtensionState(exten, context);
	}

	@PostMapping("/originate")
	public Mono<OriginateResponse> originate(@RequestBody Mono<OriginateRequest> requestMono) {
		return requestMono.flatMap(asteriskClient::originate);
	}
}
