/*
 * © 2026 Eset Shabakhov. Retry
 */
package io.github.eshabakhov.retry;

import io.github.eshabakhov.RetryException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RtFunctional}.
 * @since 0.0.1
 */
@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "PMD.TooManyMethods"})
final class RtFunctionalTest {

    @Test
    void resultOnFirstSuccessfulAttempt() throws Exception {
        MatcherAssert.assertThat(
            "Must return operation result when first attempt succeeds",
            new RtFunctional(
                attempt -> true,
                attempt -> Duration.ofMillis(0)
            ).execute("success", () -> "OK"),
            Matchers.equalTo("OK")
        );
    }

    @Test
    void failsWhenPolicyDoesNotAllowRetry() {
        Assertions.assertThrows(
            RetryException.class,
            () -> new RtFunctional(
                attempt -> false,
                attempt -> Duration.ofMillis(0)
            ).execute(
                "no-retry",
                () -> {
                    throw new RuntimeException("boom");
                }
            )
        );
    }

    @Test
    void throwsRetryExceptionWhenMaxAttemptsExceeded() {
        Assertions.assertThrows(
            RetryException.class,
            () -> new RtFunctional(
                new PcMaxAttempts(3),
                new DlExponentialBackoff(Duration.ofMillis(1), 1.0)
            ).execute(
                "one",
                () -> {
                    throw new RuntimeException("first failure");
                }
            )
        );
    }

    @Test
    void throwsRetryExceptionOnFirstAttemptWithZeroRetries() {
        Assertions.assertThrows(
            RetryException.class,
            () -> new RtFunctional(
                new PcMaxAttempts(1),
                new DlExponentialBackoff(Duration.ofMillis(1), 1.0)
            ).execute(
                "two",
                () -> {
                    throw new IllegalStateException("second failure");
                }
            )
        );
    }

    @Test
    void throwsRetryExceptionWithInterruptedDelay() {
        Thread.currentThread().interrupt();
        Assertions.assertThrows(
            RetryException.class,
            () -> new RtFunctional(
                new PcMaxAttempts(3),
                new DlExponentialBackoff(Duration.ofMillis(100), 2.0)
            ).execute(
                "three",
                () -> {
                    throw new RuntimeException("third failure");
                }
            )
        );
    }

    @Test
    void jitterDelayStillThrowsRetryExceptionOnExhaustion() {
        Assertions.assertThrows(
            RetryException.class,
            () -> new RtFunctional(
                new PcMaxAttempts(2),
                new DlJitter(
                    new DlExponentialBackoff(Duration.ofMillis(1), 1.0),
                    0.5
                )
            ).execute(
                "four",
                () -> {
                    throw new RuntimeException("fourth failure");
                }
            )
        );
    }

    @Test
    void executesExactNumberOfAttempts() {
        final int max = 4;
        final AtomicInteger counter = new AtomicInteger();
        Assertions.assertThrows(
            RetryException.class,
            () -> new RtFunctional(
                new PcMaxAttempts(max),
                new DlExponentialBackoff(Duration.ofMillis(1), 1.0)
            ).execute(
                "five", () -> {
                    counter.incrementAndGet();
                    throw new RuntimeException("fifth failure");
                }
            )
        );
        MatcherAssert.assertThat(
            counter.get(),
            Matchers.is(max)
        );
    }

    @Test
    void executesExactlyOnceWhenSingleAttemptAllowed() {
        final AtomicInteger counter = new AtomicInteger();
        Assertions.assertThrows(
            RetryException.class,
            () -> new RtFunctional(
                new PcMaxAttempts(1),
                new DlExponentialBackoff(Duration.ofMillis(1), 1.0)
            ).execute(
                "six",
                () -> {
                    counter.incrementAndGet();
                    throw new RuntimeException("sixth failure");
                }
            )
        );
        MatcherAssert.assertThat(
            counter.get(),
            Matchers.is(1)
        );
    }

    @Test
    void stopsRetryingAfterSuccess() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        new RtFunctional(
            new PcMaxAttempts(5),
            new DlExponentialBackoff(Duration.ofMillis(1), 1.0)
        ).execute(
            "seven",
            () -> {
                if (counter.incrementAndGet() < 3) {
                    throw new RuntimeException("seventh failure");
                }
                return null;
            }
        );
        MatcherAssert.assertThat(
            counter.get(),
            Matchers.is(3)
        );
    }

    @Test
    void originalCauseIsPreservedInRetryException() {
        final RuntimeException cause = new RuntimeException("original");
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                RetryException.class,
                () -> new RtFunctional(
                    new PcMaxAttempts(1),
                    new DlExponentialBackoff(Duration.ofMillis(1), 1.0)
                ).execute(
                    "eight",
                    () -> {
                        throw cause;
                    }
                )
            ).getCause(),
            Matchers.allOf(
                Matchers.sameInstance(cause),
                Matchers.hasProperty(
                    "message",
                    Matchers.is("original")
                )
            )
        );
    }

    @Test
    void originalCauseMessageIsPreservedAfterRetries() {
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                RetryException.class,
                () -> new RtFunctional(
                    new PcMaxAttempts(3),
                    new DlExponentialBackoff(Duration.ofMillis(1), 1.0)
                ).execute(
                    "nine",
                    () -> {
                        throw new RuntimeException("original message");
                    }
                )
            ).getCause(),
            Matchers.hasProperty(
                "message",
                Matchers.is("original message")
            )
        );
    }

    @Test
    void interruptedExceptionSuppressesOriginalCause() {
        final RuntimeException cause = new RuntimeException("original");
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                RetryException.class,
                () -> new RtFunctional(
                    new PcMaxAttempts(3),
                    new DlExponentialBackoff(Duration.ofMillis(100), 1.0)
                ).execute(
                    "ten",
                    () -> {
                        Thread.currentThread().interrupt();
                        throw cause;
                    }
                )
            ),
            Matchers.allOf(
                Matchers.hasProperty("cause", Matchers.instanceOf(InterruptedException.class)),
                Matchers.hasProperty(
                    "cause",
                    Matchers.hasProperty(
                        "suppressed",
                        Matchers.arrayContaining(Matchers.sameInstance(cause))
                    )
                )
            )
        );
    }

    @Test
    void originalExceptionTypeIsPreservedAsCause() {
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                RetryException.class,
                () -> new RtFunctional(
                    new PcMaxAttempts(2),
                    new DlExponentialBackoff(Duration.ofMillis(1), 1.0)
                ).execute(
                    "eleven",
                    () -> {
                        throw new IllegalArgumentException("bad arg");
                    }
                )
            ).getCause(),
            Matchers.allOf(
                Matchers.instanceOf(IllegalArgumentException.class),
                Matchers.hasProperty("message", Matchers.is("bad arg"))
            )
        );
    }

    @Test
    void interruptFlagIsRestoredAfterInterruptedException() {
        Assertions.assertThrows(
            RetryException.class,
            () -> new RtFunctional(
                new PcMaxAttempts(3),
                new DlExponentialBackoff(Duration.ofMillis(100), 1.0)
            ).execute(
                "test",
                () -> {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("original");
                }
            )
        );
        MatcherAssert.assertThat(
            "Interrupt flag must be restored after InterruptedException",
            Thread.interrupted(),
            Matchers.is(true)
        );
    }
}
