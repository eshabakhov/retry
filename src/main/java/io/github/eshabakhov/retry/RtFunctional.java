/*
 * © 2026 Eset Shabakhov. Retry
 */
package io.github.eshabakhov.retry;

import io.github.eshabakhov.Retry;
import io.github.eshabakhov.RetryException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Retry executor that uses a policy and a delay strategy.
 * Attempts the operation and retries according to policy and delays.
 * @since 0.0.1
 */
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public final class RtFunctional implements Retry {

    /**
     * Policy controlling the maximum number of retries.
     */
    private final Policy policy;

    /**
     * Strategy to determine the delay between retries.
     */
    private final Delay delay;

    /**
     * Ctor.
     * @param policy Retry policy
     * @param delay Retry delay
     */
    public RtFunctional(final Policy policy, final Delay delay) {
        this.policy = policy;
        this.delay = delay;
    }

    @Override
    public <T> T execute(final String name, final Callable<T> operation) throws RetryException {
        return this.attempt(name, operation);
    }

    private <T> T attempt(final String name, final Callable<T> operation) throws RetryException {
        int attempt = 1;
        while (true) {
            try {
                return operation.call();
                //@checkstyle IllegalCatch (1 line)
            } catch (final Exception ex) {
                if (this.policy.allows(attempt + 1)) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(this.delay.delayFor(attempt).toMillis());
                    } catch (final InterruptedException iex) {
                        Thread.currentThread().interrupt();
                        iex.addSuppressed(ex);
                        throw new RetryException(
                            String.format("Retry for '%s' was interrupted", name),
                            iex
                        );
                    }
                    attempt += 1;
                } else {
                    throw new RetryException(ex);
                }
            }
        }
    }
}
