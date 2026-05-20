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
 * Tests for {@link DlJitter}.
 * @since 0.0.1
 */
final class DlJitterTest {

    @Test
    void originalDelayWhenRatioEqualsZero() {
        MatcherAssert.assertThat(
            "Must return original delay when jitter ratio equals zero",
            new DlJitter(attempt -> Duration.ofSeconds(5L), 0.0).delayFor(1),
            Matchers.equalTo(Duration.ofSeconds(5L))
        );
    }

    @Test
    void delayWithinJitterRangeForHalfRatio() {
        MatcherAssert.assertThat(
            "Must return delay greater than or equal to lower jitter bound and Must return delay less than upper jitter bound",
            new DlJitter(
                attempt -> Duration.ofSeconds(10L),
                0.5
            ).delayFor(1),
            Matchers.allOf(
                Matchers.greaterThanOrEqualTo(Duration.ofMillis(7_500L)),
                Matchers.lessThan(Duration.ofMillis(12_500L))
            )
        );
    }

    @Test
    void delayWithinJitterRangeForFullRatio() {
        MatcherAssert.assertThat(
            "Must return delay greater than or equal to lower bound and Must return delay less than upper bound",
            new DlJitter(
                attempt -> Duration.ofSeconds(10L),
                1.0
            ).delayFor(1),
            Matchers.allOf(
                Matchers.greaterThanOrEqualTo(Duration.ofSeconds(5L)),
                Matchers.lessThan(Duration.ofSeconds(15L))
            )
        );
    }

    @Test
    void supportsNegativeAttemptNumbers() {
        MatcherAssert.assertThat(
            "Must support negative attempt numbers",
            new DlJitter(
                Duration::ofSeconds,
                0.0
            ).delayFor(-3),
            Matchers.is(Duration.ofSeconds(-3L))
        );
    }

    @Test
    void supportsNegativeOriginDurations() {
        MatcherAssert.assertThat(
            "Must support negative origin duration values",
            new DlJitter(
                attempt -> Duration.ofSeconds(-5L),
                0.0
            ).delayFor(1),
            Matchers.is(Duration.ofSeconds(-5L))
        );
    }

    @Test
    void supportsNegativeJitterRatio() {
        MatcherAssert.assertThat(
            "Must return duration instance for negative ratio",
            new DlJitter(
                attempt -> Duration.ofSeconds(10L),
                -0.5
            ).delayFor(1),
            Matchers.allOf(
                Matchers.greaterThanOrEqualTo(Duration.ofMillis(7_500L)),
                Matchers.lessThan(Duration.ofMillis(12_500L))
            )
        );
    }

    @Test
    void supportsRatioGreaterThanOne() {
        MatcherAssert.assertThat(
            "Must return delay greater than or equal to zero and Must return delay less than upper extended jitter bound",
            new DlJitter(
                attempt -> Duration.ofSeconds(10L),
                2.0
            ).delayFor(1),
            Matchers.allOf(
                Matchers.greaterThanOrEqualTo(Duration.ZERO),
                Matchers.lessThan(Duration.ofSeconds(20L))
            )
        );
    }

    @Test
    void failsWhenOriginDelayIsNull() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new DlJitter(
                null,
                0.5
            ).delayFor(1)
        );
    }

    @Test
    void failsWhenOriginReturnsNullDuration() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new DlJitter(
                attempt -> null,
                0.5
            ).delayFor(1)
        );
    }
}
