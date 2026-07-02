# Notification Service

A lightweight microservice that listens to Kafka events from `wallet-api` and processes transaction notifications.

---

## Overview

The `notification-service` is part of the **Wallet Platform** — a multi-service backend for managing digital wallets.

```
wallet-platform/
├── wallet-api/            # Core REST API (port 8080) — produces events
└── notification-service/  # Kafka consumer (port 8081) — this service
```

### Tech Stack

| Layer     | Technology                    |
|-----------|-------------------------------|
| Framework | Spring Boot 4.1               |
| Messaging | Apache Kafka 7.5 (Confluent)  |
| Java      | 21                            |
| Build     | Maven                         |

---

## Getting Started

### 1. Start Infrastructure

Run the shared Docker Compose from the root of `wallet-api`:

```bash
cd wallet-api
docker compose up -d
```

Starts: **Zookeeper** (2181), **Kafka** (9092).

### 2. Run Notification Service

```bash
cd notification-service
./mvnw spring-boot:run
```

The service starts on port `8081` and immediately begins listening to the `transactions` Kafka topic.

---

## Kafka Integration

### Topic

| Topic          | Role     | Group ID               |
|----------------|----------|------------------------|
| `transactions` | Consumer | `notification-service` |

### Event Schema — `TransactionEvent`

Published by `wallet-api` after every deposit, withdrawal, or transfer:

```json
{
  "transactionId": 1,
  "fromAccountId": 10,
  "toAccountId": 20,
  "amount": 500.00
}
```

### Consumer

`NotificationConsumer` logs every incoming event using SLF4J:

```java
@KafkaListener(topics = "transactions", groupId = "notification-service")
public void consume(TransactionEvent event) {
    log.info("Received transaction event: {}", event);
}
```

Deserialization is handled via `ErrorHandlingDeserializer` wrapping `JsonDeserializer`, with the target type set explicitly via configuration (no type headers required).

---

## Project Structure

```
src/main/java/com/aidana/notification_service/
├── NotificationServiceApplication.java
└── kafka/
    ├── consumer/
    │   └── NotificationConsumer.java   # @KafkaListener
    └── event/
        └── TransactionEvent.java       # Deserialized payload
```

---

## Configuration

`application.yml`:

```yaml
server:
  port: 8081

spring:
  application:
    name: notification-service

  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      properties:
        spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
        spring.json.use.type.headers: false
        spring.json.value.default.type: com.aidana.notification_service.kafka.event.TransactionEvent
        spring.json.trusted.packages: "*"
```

---

## Running Tests

```bash
./mvnw test
```

| Test Class                          | Description                    |
|-------------------------------------|--------------------------------|
| `NotificationServiceApplicationTests` | Verifies Spring context loads |

