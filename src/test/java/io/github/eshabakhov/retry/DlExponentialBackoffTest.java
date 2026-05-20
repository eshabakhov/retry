/*
 * © 2026 Eset Shabakhov. Retry
 */
package io.github.eshabakhov.retry;

import java.time.Duration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DlExponentialBackoff}.
 * @since 0.0.1
 */
final class DlExponentialBackoffTest {

    @Test
    void baseDurationForFirstAttempt() {
        MatcherAssert.assertThat(
            "Must return base duration for the first retry attempt",
            new DlExponentialBackoff(Duration.ofSeconds(1L), 2.0).delayFor(1),
            Matchers.is(Duration.ofSeconds(1L))
        );
    }

    @Test
    void growsDelayExponentiallyForSubsequentAttempts() {
        MatcherAssert.assertThat(
            "Must grow delay exponentially according to multiplier",
            new DlExponentialBackoff(Duration.ofSeconds(1L), 2.0).delayFor(4),
            Matchers.equalTo(Duration.ofSeconds(8L))
        );
    }

    @Test
    void calculatesDelayUsingCustomMultiplier() {
        MatcherAssert.assertThat(
            "Must calculate delay using provided multiplier",
            new DlExponentialBackoff(Duration.ofSeconds(3L), 3.0).delayFor(3),
            Matchers.equalTo(Duration.ofSeconds(27L))
        );
    }

    @Test
    void keepsDelayConstantWhenMultiplierEqualsOne() {
        MatcherAssert.assertThat(
            "Must keep delay unchanged when multiplier equals one",
            new DlExponentialBackoff(Duration.ofSeconds(5L), 1.0).delayFor(10),
            Matchers.equalTo(Duration.ofSeconds(5L))
        );
    }

    @Test
    void truncatesFractionalExponentialResultToLongValue() {
        MatcherAssert.assertThat(
            "Must truncate fractional exponential result during long conversion",
            new DlExponentialBackoff(Duration.ofSeconds(1L), 1.5).delayFor(3),
            Matchers.equalTo(Duration.ofSeconds(2L))
        );
    }

    @Test
    void supportsNegativeAttemptNumber() {
        MatcherAssert.assertThat(
            "Must support negative attempt numbers",
            new DlExponentialBackoff(Duration.ofSeconds(8L), 2.0).delayFor(-2),
            Matchers.equalTo(Duration.ofSeconds(0L))
        );
    }

    @Test
    void supportsNegativeMultiplier() {
        MatcherAssert.assertThat(
            "Must support negative multiplier values",
            new DlExponentialBackoff(Duration.ofSeconds(1L), -2.0).delayFor(2),
            Matchers.equalTo(Duration.ofSeconds(-2L))
        );
    }

    @Test
    void supportsNegativeBaseDuration() {
        MatcherAssert.assertThat(
            "Must support negative base duration",
            new DlExponentialBackoff(Duration.ofSeconds(-2L), 2.0).delayFor(3),
            Matchers.equalTo(Duration.ofSeconds(-8L))
        );
    }

    @Test
    void failsWhenBaseDurationIsNull() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new DlExponentialBackoff(null, 2.0).delayFor(1)
        );
    }
}
