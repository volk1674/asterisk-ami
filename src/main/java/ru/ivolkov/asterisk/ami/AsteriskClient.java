package ru.ivolkov.asterisk.ami;

import java.io.IOException;
import reactor.core.publisher.Mono;
import ru.ivolkov.asterisk.api.v1.dto.GetExtensionStateResponse;
import ru.ivolkov.asterisk.api.v1.dto.OriginateRequest;
import ru.ivolkov.asterisk.api.v1.dto.OriginateResponse;

public interface AsteriskClient {
	void login() throws IOException, InterruptedException;

	void logoff();

	String getVersion();

	Mono<OriginateResponse> originate(OriginateRequest request);

	Mono<GetExtensionStateResponse> getExtensionState(String exten, String context);

	ManagerConnectionState getState();
}
