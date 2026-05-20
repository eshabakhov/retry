/*
 * © 2026 Eset Shabakhov. Retry
 */
package com.eshabakhov.retry;

/**
 * Policy interface for determining if a retry attempt is allowed.
 * @since 0.0.1
 */
@FunctionalInterface
public interface Policy {

    /**
     * Checks whether the retry attempt is allowed based on the policy.
     * @param attempt The current retry attempt number (starting from 1)
     * @return If the attempt is allowed {@code true}, {@code false} otherwise
     */
    boolean allows(int attempt);
}
