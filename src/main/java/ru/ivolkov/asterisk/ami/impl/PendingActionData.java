package ru.ivolkov.asterisk.ami.impl;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import ru.ivolkov.asterisk.ami.actions.ActionResponse;

@Data
public class PendingActionData {
	private CompletableFuture<ActionResponse> future;
	private Instant sendTime;
}
