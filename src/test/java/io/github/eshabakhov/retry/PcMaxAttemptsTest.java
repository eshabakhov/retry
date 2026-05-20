/*
 * © 2026 Eset Shabakhov. Retry
 */
package io.github.eshabakhov.retry;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PcMaxAttempts}.
 * @since 0.0.1
 */
final class PcMaxAttemptsTest {

    @Test
    void correctAttempt() {
        MatcherAssert.assertThat(
            "Attemt allowed",
            new PcMaxAttempts(3).allows(1),
            Matchers.is(true)
        );
    }

    @Test
    void correctLastAttempt() {
        MatcherAssert.assertThat(
            "Last attemt allowed",
            new PcMaxAttempts(3).allows(3),
            Matchers.is(true)
        );
    }

    @Test
    void attemptFails() {
        MatcherAssert.assertThat(
            "Attemt failed",
            new PcMaxAttempts(3).allows(4),
            Matchers.is(false)
        );
    }
}
