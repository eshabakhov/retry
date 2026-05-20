/*
 * © 2026 Eset Shabakhov. Retry
 */
package io.github.eshabakhov;

import java.util.concurrent.Callable;

/**
 * Interface for executing an operation with retry logic.
 * @since 0.0.1
 */
public interface Retry {

    /**
     * Executes the given operation with retry according to the configured policies.
     * @param name The name of operation to execute
     * @param operation The operation to execute
     * @param <T>       The type of the result
     * @return The result of the operation
     * @throws RetryException if the operation fails after exhausting retries
     */
    <T> T execute(String name, Callable<T> operation) throws RetryException;
}
