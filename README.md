# Retry

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
    <groupId>com.eshabakhov</groupId>
    <artifactId>retry</artifactId>
    <version>0.0.1</version>
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

Adds randomness to avoid “thundering herd” problem.

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

##️ Execution Model

Retry loop behavior:

1. Execute operation
2. If success → return result
3. If failure:
    - check policy (`allows(nextAttempt)`)
    - sleep using delay strategy
    - retry
4. If policy rejects → throw `RetryException`

---

## Failure handling

All exceptions are wrapped into:

```java
RetryException
```

Interrupted retries preserve interruption state:

```java
Thread.currentThread().interrupt();
```

---

## Building from source

```bash
mvn clean install
```

Run tests:

```bash
mvn test
```

Mutation testing:

```bash
mvn test -Pmutation
```

---

## License

MIT License

---

## Why this library exists

Most retry libraries are either:
- too heavy (framework-level)
- too implicit (annotations / magic)
- too coupled to HTTP clients

This library focuses on:
> explicit, composable retry logic without framework dependency

---
