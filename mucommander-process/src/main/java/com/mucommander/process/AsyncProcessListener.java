package com.mucommander.process;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Listener used to provide the output messages to the end-user when an error occurs while executing a task
 * with the {@link ProcessRunner}.
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 */
class AsyncProcessListener implements ProcessListener {

    /**
     * The buffer that will contain the output messages provided by the {@link ProcessOutputMonitor}.
     */
    private final StringBuilder buffer = new StringBuilder();
    /**
     * The {@link CompletableFuture} that will be used as callback to provide the output messages in case
     * the return value is not {@code 0}.
     */
    private final CompletableFuture<Optional<String>> completableFuture = new CompletableFuture<>();

    /**
     * Gives the {@link CompletionStage} allowing to get the output messages.
     * @return the corresponding callback to provide the output messages.
     */
    CompletionStage<Optional<String>> toCompletionStage() {
        return completableFuture;
    }

    @Override
    public void processDied(int returnValue) {
        completableFuture.complete(returnValue == 0 ? Optional.empty() : Optional.of(buffer.toString().trim()));
    }

    @Override
    public void processOutput(String output) {
        buffer.append(output);
    }

    @Override
    public void processOutput(byte[] buffer, int offset, int length) {
        // Nothing to do
    }
}
