/*
 * © 2026 Eset Shabakhov. Retry
 */
package io.github.eshabakhov.retry;

import java.time.Duration;
import java.util.Random;

/**
 * Adds random jitter to a base delay strategy.
 * Useful to avoid thundering herd problem when multiple retries happen concurrently.
 * @since 0.0.1
 */
public final class DlJitter implements Delay {

    /**
     * Original delay strategy to wrap.
     */
    private final Delay origin;

    /**
     * Maximum relative jitter ratio.
     */
    private final double ratio;

    /** Random. */
    private final Random random;

    /**
     * Ctor.
     * @param origin Base delay
     * @param ratio Maximum relative deviation from the base delay
     */
    public DlJitter(final Delay origin, final double ratio) {
        this.origin = origin;
        this.ratio = ratio;
        this.random = new Random();
    }

    @Override
    public Duration delayFor(final int attempt) {
        final long nanos = this.origin.delayFor(attempt).toNanos();
        final long range = (long) (nanos * this.ratio);
        return Duration.ofNanos(nanos - range / 2 + (long) (this.random.nextDouble() * range));
    }
}
