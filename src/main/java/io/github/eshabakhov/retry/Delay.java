/*
 * © 2026 Eset Shabakhov. Retry
 */
package io.github.eshabakhov.retry;

import java.time.Duration;

/**
 * Strategy interface for determining how long to wait between retry attempts.
 * @since 0.0.1
 */
@FunctionalInterface
public interface Delay {

    /**
     * Returns the duration to wait before the given retry attempt.
     * @param attempt The retry attempt number (starting from 1)
     * @return The duration to wait before the next attempt
     */
    Duration delayFor(int attempt);
}
