# Retry

[![Maven](https://github.com/eshabakhov/retry/actions/workflows/mvn.yml/badge.svg)](https://github.com/eshabakhov/retry/actions/workflows/mvn.yml)
[![Hits-of-Code](https://hitsofcode.com/github/eshabakhov/retry)](https://hitsofcode.com/github/eshabakhov/retry/view)
[![Test Coverage](https://codecov.io/gh/eshabakhov/retry/graph/badge.svg)](https://codecov.io/gh/eshabakhov/retry)

A lightweight, composable retry library for Java.

It provides flexible **retry policies**, **delay strategies**, and **functional execution model**
for handling transient failures in a clean and predictable way.

---

## Features

- Functional retry execution model
- Pluggable delay strategies (exponential backoff, jitter, etc.)
- Configurable retry policies (e.g. max attempts)
- Composable design (Policy + Delay)
- Fully testable and deterministic core

---

## Installation

### Maven

```xml
<dependency>
   <groupId>io.github.eshabakhov</groupId>
   <artifactId>retry</artifactId>
   <version>0.0.2</version>
</dependency>
```

---

## Core Concepts

### Retry

Main entry point:

```java
Retry retry = new RtFunctional(policy, delay);

String result = retry.execute(
     "operation-name",
     () -> {
        return "success";
     }
);
```

---

### Policy

Controls when retries stop.

```java
Policy policy = new PcMaxAttempts(3);
```

Meaning: retry up to 3 attempts.

---

### Delay

Defines wait time between retries.

#### Exponential backoff

```java
Delay delay = new DlExponentialBackoff(
    Duration.ofSeconds(1),
    2.0
);
```

---

#### Jitter (randomized delay)

```java
Delay delay = new DlJitter(
    attempt -> Duration.ofSeconds(10),
    0.5
);
```

Adds randomness to avoid "thundering herd" problem.

---

## Composition Example

```java
Retry retry = new RtFunctional(
    new PcMaxAttempts(5),
    new DlJitter(
        new DlExponentialBackoff(Duration.ofMillis(500), 2.0),
        0.3
    )
);

String response = retry.execute(
    "call-service",
    () -> {
        return httpClient.call();
    }
);
```

---

## Execution Model

Retry loop behavior:

1. Execute operation
2. If success → return result
3. If failure:
   - check policy (`allows(nextAttempt)`)
   - sleep using delay strategy
   - retry
4. If policy rejects → throw `RetryException`

---

## Failure Handling

All exceptions are wrapped into:

```java
RetryException
```

Interrupted retries preserve interruption state:

```java
Thread.currentThread().interrupt();
```

---

## Extensibility

All core components are interfaces. You can provide your own implementations
by implementing the corresponding interface and passing it to `RtFunctional`.

### `Retry`

```java
public interface Retry {
    <T> T execute(String name, Callable<T> operation) throws RetryException;
}
```

Entry point for executing operations with retry logic. Implement to provide
a custom execution strategy — e.g. async retry, circuit breaker integration,
or metric collection.

```java
public final class CustomRetry implements Retry {

    @Override
    public <T> T execute(final String name, final Callable<T> operation) throws RetryException {
        // your custom retry logic
    }
}
```

---

### `Policy`

```java
@FunctionalInterface
public interface Policy {
    boolean allows(int attempt);
}
```

Controls whether the next retry attempt is allowed. `attempt` starts from 1.
Implement to express any custom condition — time-based limits, error type
filtering, external circuit state, etc.

```java
// Allow retries only within a time window
Instant deadline = Instant.now().plusSeconds(30);
Policy policy = attempt -> Instant.now().isBefore(deadline);
```

---

### `Delay`

```java
@FunctionalInterface
public interface Delay {
    Duration delayFor(int attempt);
}
```

Defines how long to wait before the next attempt. `attempt` starts from 1.
Implement to provide custom wait strategies — fixed delay, linear growth,
external configuration, etc.

```java
// Fixed 2-second delay regardless of attempt number
Delay delay = attempt -> Duration.ofSeconds(2);
```

All three interfaces are `@FunctionalInterface`, so lambda expressions
are supported throughout.

---

## Building from Source

```bash
mvn clean install
```

Run tests:

```bash
mvn clean test
```

Mutation testing:

```bash
mvn clean verify -Pmutation
```

---

## Why This Library Exists

Most retry libraries are either:
- too heavy (framework-level)
- too implicit (annotations / magic)
- too coupled to HTTP clients

This library focuses on:
> explicit, composable retry logic without framework dependency
