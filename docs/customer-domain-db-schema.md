# Customer Domain DB Schema

This document defines the PostgreSQL table design for the HannahPay customer domain.
It follows the project rules in `AGENTS.md` and aligns with the existing `users`-first
direction in `docs/hannahpay-desgine.md`, while making the schema safer for production use.

## Design Goals

- Keep customer identity separate from wallet, payment, and ledger data
- Enforce unique login identity
- Support account lifecycle control
- Preserve auditability with timestamps and soft delete
- Keep the schema simple enough for Spring Data JPA

## Table: `users`

### Purpose

Stores the core customer account record used for authentication, profile lookup, and account status management.

### Columns

| Column | Type | Null | Default | Description |
|---|---|---:|---|---|
| `id` | `BIGSERIAL` | No | generated | Surrogate primary key |
| `email` | `VARCHAR(100)` | No | - | Login identifier, must be unique |
| `password_hash` | `VARCHAR(255)` | No | - | BCrypt password hash |
| `full_name` | `VARCHAR(100)` | No | - | Customer display/legal name |
| `phone_number` | `VARCHAR(20)` | Yes | - | Optional phone number for verification and support |
| `status` | `VARCHAR(1)` | No | `'A'` | Account lifecycle state |
| `last_login_at` | `TIMESTAMP` | Yes | - | Last successful login time |
| `created_at` | `TIMESTAMP` | No | `CURRENT_TIMESTAMP` | Creation time |
| `updated_at` | `TIMESTAMP` | No | `CURRENT_TIMESTAMP` | Last modification time |
| `deleted_at` | `TIMESTAMP` | Yes | - | Soft delete marker |

### Recommended Constraints

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    status VARCHAR(1) NOT NULL DEFAULT 'A',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
```

Recommended additional constraints:

```sql
ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT ck_users_status
    CHECK (status IN ('A', 'S', 'W'));
```

### Indexes

```sql
CREATE UNIQUE INDEX idx_users_email
    ON users (email);

CREATE INDEX idx_users_status
    ON users (status);

CREATE INDEX idx_users_created_at
    ON users (created_at DESC);
```

### Notes on Indexing

- `email` is the primary lookup key for login and should be unique.
- `status` supports admin and operational filtering.
- `created_at` helps with recent signup queries and audit support.

## Why `users` instead of `customers`

The original design uses `users`, and keeping that physical table name is simpler because:

- it matches the existing design doc and reduces drift
- it keeps repository/entity naming aligned with the first schema sketch
- it avoids introducing a second identity concept too early

## Domain Rules

- `email` must be normalized before insert, typically lowercase and trimmed.
- `password_hash` must store only a BCrypt hash.
- `deleted_at` must be used for soft delete instead of hard delete when audit retention matters.
- `status = 'WITHDRAWN'` should be used for logical account withdrawal instead of removing the row.

## Suggested JPA Mapping

If this table is mapped to an entity, the entity should:

- avoid public setters
- expose behavior such as `suspend()`, `withdraw()`, and `markLastLogin()`
- manage `updated_at` through auditing or entity lifecycle callbacks

## Optional Follow-up Tables

These are not required for the initial customer domain table, but may be added later:

- `customer_profiles` for optional address or marketing preferences
- `customer_auth_logs` for login audit history
- `customer_terms_acceptance` for consent tracking

## Recommended Final Direction

Use `users` as the primary customer identity table, and keep wallet/payment/ledger data in separate domain tables.
That keeps the customer domain independent and reduces coupling across financial operations.
