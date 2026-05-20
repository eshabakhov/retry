/*
 * © 2026 Eset Shabakhov. Retry
 */
package com.eshabakhov.retry;

/**
 * Retry policy that limits retries to a maximum number of attempts.
 * @since 0.0.1
 */
public final class PcMaxAttempts implements Policy {

    /**
     * Maximum allowed retry attempts.
     */
    private final int attempts;

    /**
     * Ctor.
     * @param attempts Maximum attempts
     */
    public PcMaxAttempts(final int attempts) {
        this.attempts = attempts;
    }

    @Override
    public boolean allows(final int attempt) {
        return attempt <= this.attempts;
    }
}
