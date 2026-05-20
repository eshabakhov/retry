/*
 * © 2026 Eset Shabakhov. Retry
 */
package com.eshabakhov.retry;

import java.time.Duration;

/**
 * Exponential backoff delay strategy for retries.
 * Computes the wait duration as base * multiplier^(attempt-1).
 * @since 0.0.1
 */
public final class DlExponentialBackoff implements Delay {

    /**
     * Base duration for the first attempt.
     */
    private final Duration base;

    /**
     * Multiplier for exponential growth of delay.
     */
    private final double multiplier;

    /**
     * Ctor.
     * @param base Base duration
     * @param multiplier Multiplier for base duration
     */
    public DlExponentialBackoff(final Duration base, final double multiplier) {
        this.base = base;
        this.multiplier = multiplier;
    }

    @Override
    public Duration delayFor(final int attempt) {
        return this.base.multipliedBy((long) Math.pow(this.multiplier, attempt - 1.0));
    }
}
