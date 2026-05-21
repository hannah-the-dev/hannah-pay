# Customer / User Table Schema Proposal

## Scope

This proposal keeps the physical table name `users` to stay consistent with the current design doc, but treats it as the canonical customer identity and authentication table only.

It does not cover wallet, payment, ledger, or notification data.

## Table Purpose

Store the minimum stable identity data required for:

- customer sign-up and login
- account lifecycle management
- email uniqueness enforcement
- audit-friendly retention with soft delete

## Proposed Table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    status VARCHAR(1) NOT NULL DEFAULT 'A',
    email_verified_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT ck_users_status
        CHECK (status IN ('A', 'S', 'W')),
    CONSTRAINT ck_users_deleted_at
        CHECK (deleted_at IS NULL OR deleted_at >= created_at)
);

CREATE UNIQUE INDEX ux_users_email_lower
    ON users (LOWER(email));

CREATE INDEX ix_users_status
    ON users (status);

CREATE INDEX ix_users_deleted_at
    ON users (deleted_at);
```

## Columns

| Column | Type | Null | Purpose |
|---|---|---:|---|
| `id` | `BIGSERIAL` | No | Surrogate primary key |
| `email` | `VARCHAR(254)` | No | Login and customer contact email |
| `password_hash` | `VARCHAR(255)` | No | BCrypt or future-compatible password hash |
| `full_name` | `VARCHAR(100)` | No | Customer display/legal name |
| `status` | `VARCHAR(1)` | No | Account lifecycle state |
| `email_verified_at` | `TIMESTAMPTZ` | Yes | Verification timestamp |
| `created_at` | `TIMESTAMPTZ` | No | Row creation time |
| `updated_at` | `TIMESTAMPTZ` | No | Last update time |
| `deleted_at` | `TIMESTAMPTZ` | Yes | Soft-delete marker |

## Constraints

- Primary key on `id`
- Case-insensitive unique email via `LOWER(email)`
- Status limited to `A`, `S`, `W` meaning `ACTIVE`, `SUSPENDED`, `WITHDRAWN`
- Soft-delete timestamp cannot be earlier than `created_at`

## Indexes

- `ux_users_email_lower`
  - Enforces email uniqueness regardless of case
  - Supports fast login lookup
- `ix_users_status`
  - Supports admin and lifecycle queries
- `ix_users_deleted_at`
  - Supports retention and soft-delete filtering

## Key Choices

- Use `TIMESTAMPTZ` instead of plain `TIMESTAMP` to avoid timezone ambiguity in a financial system.
- Store `password_hash`, not `password`, to align with BCrypt-based authentication rules.
- Keep `status` separate from `deleted_at` so account lifecycle and retention are not conflated.
- Use soft delete instead of hard delete because customer identity records should remain auditable.
- Keep email uniqueness at the database level to prevent duplicate identities.

## Notes

- Store `email` normalized to lowercase in the application before insert or update.
- `updated_at` should be maintained by the application layer or a database trigger.
- If the product later needs a richer compliance or audit trail, add a separate audit table rather than widening this core identity table.
