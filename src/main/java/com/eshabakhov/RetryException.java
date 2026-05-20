/*
 * © 2026 Eset Shabakhov. Retry
 */
package com.eshabakhov;

/**
 * Exception thrown when a retryable operation fails after exceeding allowed attempts.
 * @since 0.0.1
 */
public class RetryException extends Exception {

    /**
     * Ctor.
     * @param message Message exception
     * @param cause Cause of exception
     */
    public RetryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Ctor.
     * @param cause Cause of exception
     * @checkstyle ConstructorsOrderCheck (2 lines)
     */
    public RetryException(final Throwable cause) {
        super(cause);
    }
}
