# AGENTS.md

# HannahPay AI Engineering Rules

This document defines the mandatory engineering rules, architecture principles, coding standards, and implementation constraints for all AI agents working on the HannahPay project.

All generated code MUST follow this document.

---

# 1. Project Overview

HannahPay is a fintech payment and transfer platform.
Any real-life financial transaction shall NOT be performed.

The system must prioritize:

- financial transaction consistency
- concurrency safety
- event-driven architecture
- maintainability
- production-ready implementation
- readability
- operational stability

This is NOT an experimental architecture playground.

Avoid unnecessary complexity.

---

# 2. Core Technology Stack

| Category | Stack |
|---|---|
| Language | Java 23 |
| Framework | Spring Boot |
| Build Tool | Gradle |
| Database | PostgreSQL |
| ORM | Spring Data JPA |
| Cache | Redis |
| Messaging | Kafka |
| Infra | Docker |
| Cloud | AWS |
| Authentication | JWT |

---

# 3. Mandatory Engineering Principles

## 3.1 Financial Consistency First

Financial consistency is the highest priority.

The system MUST NEVER:
- create negative wallet balances
- lose transaction history
- duplicate payment processing
- lose Kafka events after DB commit
- process the same request multiple times

---

## 3.2 Simplicity Over Cleverness

Prefer:
- explicit code
- readable code
- maintainable code

Avoid:
- magic abstractions
- excessive generic programming
- reflection-heavy implementations
- unnecessary design patterns

---

## 3.3 Production-Oriented Code

All generated code should be:
- testable
- production-oriented
- observable
- failure-aware

Include:
- logging
- exception handling
- transaction boundaries

---

# 4. Package Structure Rules

Use domain-oriented package structure.

DO NOT use large shared util structures.

---

## 4.1 Recommended Structure

```text
payment-service/
 ├── payment/
 │    ├── controller/
 │    ├── service/
 │    ├── domain/
 │    ├── repository/
 │    ├── dto/
 │    ├── kafka/
 │    └── event/
 │
 ├── common/
 │    ├── exception/
 │    ├── config/
 │    └── util/
 │
 └── infrastructure/
```

---

## 4.2 Forbidden Structures

DO NOT create:
- gigantic util classes
- shared god objects
- static mutable helper classes
- common business logic dumping grounds

Forbidden examples:

```text
CommonUtil.java
HelperManager.java
PaymentUtilFactory.java
GlobalService.java
```

---

# 5. Coding Standards

## 5.1 Dependency Injection

Use constructor injection ONLY.

Use:

```java
@RequiredArgsConstructor
```

Do NOT use:
- field injection
- setter injection

Forbidden:

```java
@Autowired
private PaymentService paymentService;
```

---

## 5.2 Entity Rules

Entities must:
- encapsulate behavior
- avoid public setters
- protect consistency

Good:

```java
public void deduct(Long amount) {
    validateBalance(amount);
    this.balance -= amount;
}
```

Bad:

```java
wallet.setBalance(balance - amount);
```

---

## 5.3 Controller Rules

Controllers must:
- contain no business logic
- validate request format only
- delegate to service layer

---

## 5.4 Service Rules

Services are responsible for:
- transaction management
- business logic
- orchestration
- consistency guarantees

---

## 5.5 Repository Rules

Repositories must:
- contain persistence logic only
- not contain business logic

---

# 6. Transaction Rules

## 6.1 Mandatory Transaction Protection

Every payment-related operation MUST:
- use DB transaction
- validate balance
- prevent duplicate processing
- persist ledger history

---

## 6.2 Idempotency

All payment APIs MUST support idempotency.

Required header:

```http
Idempotency-Key: UUID
```

Duplicate requests must return existing results.

---

## 6.3 Concurrency Control

Use:
- Redis distributed lock
- optimistic locking
- transaction boundaries

Wallet balance must NEVER become negative.

---

## 6.4 Ledger Persistence

Every balance-changing action MUST create ledger history.

Forbidden:

```java
wallet.balance -= amount;
```

without ledger persistence.

---

# 7. Kafka Rules

## 7.1 Kafka Usage

Kafka is used for:
- asynchronous processing
- notification events
- audit propagation
- decoupled architecture

---

## 7.2 Kafka Topic Naming

Use lowercase dot notation.

Good:

```text
payment.completed
payment.failed
transfer.completed
```

Bad:

```text
PaymentCompleted
PAYMENT_TOPIC
```

---

## 7.3 Kafka Delivery Safety

Kafka publish failure MUST NOT lose transaction data.

Use:
- Outbox Pattern
- retry mechanism
- DLQ

---

# 8. Redis Rules

## 8.1 Redis Usage

Redis is used for:
- distributed locking
- caching
- rate limiting
- duplicate request prevention

---

## 8.2 Redis Key Naming

Use colon-based naming.

Good:

```text
wallet:user:1001
payment:lock:1001
payment:request:uuid
```

Bad:

```text
wallet1001
lock_user
```

---

# 9. Error Handling Rules

Use custom exception hierarchy.

Example:

```text
BusinessException
 ├── InsufficientBalanceException
 ├── DuplicatePaymentException
 └── InvalidTransactionException
```

---

# 10. Logging Rules

All important financial actions MUST be logged.

Required:
- transaction id
- user id
- amount
- result status

Never log:
- passwords
- tokens
- sensitive personal information

---

# 11. Security Rules

Mandatory:
- JWT authentication
- BCrypt password hashing
- HTTPS
- request validation

Forbidden:
- plain text password storage
- insecure token handling

---

# 12. Testing Rules

Every business logic implementation MUST include:
- unit tests
- integration tests for transaction flow

Important scenarios:
- duplicate payment
- concurrent payment
- insufficient balance
- Kafka failure
- Redis lock failure

---

# 13. Docker Rules

Every service must:
- run independently
- support docker execution
- use environment variables

---

# 14. AWS Rules

Preferred AWS services:

| Purpose | AWS |
|---|---|
| Compute | ECS / EC2 |
| Database | RDS |
| Cache | ElastiCache |
| Container Registry | ECR |
| Monitoring | CloudWatch |

---

# 15. Forbidden Engineering Behaviors

DO NOT:
- introduce unnecessary microservices
- create overengineered abstractions
- use excessive inheritance
- mix infrastructure logic with domain logic
- bypass transaction handling
- bypass Redis lock for payment flow

---

# 16. AI Agent Behavior Rules

When implementing code:
- prioritize correctness over cleverness
- prioritize consistency over optimization
- prefer explicit implementations
- generate production-oriented code

When uncertain:
- choose safer transaction behavior
- choose more readable code
- avoid speculative optimization

---

# 17. Recommended Development Order

1. User/Auth
2. Wallet
3. Payment
4. Ledger
5. Redis Lock
6. Kafka Integration
7. Notification
8. Outbox Pattern
9. Docker
10. AWS Deployment

---

# 18. Final Principle

This project is a fintech transaction platform.

The most important principle is:

```text
Never break financial consistency.
```

# 19. Git Management Rules

## 19.1 Branch Strategy

Use the following branch structure:

```text
main
feature/*
hotfix/*
```

---

## 19.2 Branch Naming

Use lowercase kebab-case naming.

Good:

```text
feature/payment-redis-lock
feature/wallet-charge-api
hotfix/duplicate-payment
```

Bad:

```text
feature/test
mybranch
fix123
```

---

## 19.3 Commit Message Rules

Format:

```text
[type] message
```

Examples:

```text
[feature] add wallet charge api
[fix] prevent duplicate payment
[refactor] separate payment validator
[test] add concurrent payment test
```

---

## 19.4 Pull Request Rules

- One logical change per PR
- Keep PR size small
- Include related tests
- Avoid unrelated file modifications
- Keep commit history clean

---

## 19.5 AI Agent Git Rules

AI agents MUST:
- never modify unrelated files
- never rename files unnecessarily
- never reformat entire files without reason
- keep changes minimal and focused

# 20. Agent Routing Rules

## architect
Use when:
- creating new domain structure
- designing kafka flow
- designing redis strategy
- designing db schema
- changing system architecture

Do NOT use for:
- simple bug fixes
- controller implementation

---

## backend-developer
Use when:
- implementing APIs
- implementing services
- writing repositories
- adding kafka consumers/producers

Must follow:
- AGENTS.md

---

## reviewer
Use when:
- implementation is complete
- PR review is requested
- transaction safety must be verified
- financial consistency must be verified

Review Focus:
- transaction boundary
- redis locking
- idempotency
- ledger persistence

---

## tester
Use when:
- implementation is complete
- edge case validation is required
- concurrency validation is needed

Generate:
- integration tests
- race condition tests
- duplicate request tests
