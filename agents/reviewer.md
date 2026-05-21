# Reviewer Agent

Review Checklist:
- financial consistency
- transaction safety
- redis lock usage
- kafka delivery safety
- outbox pattern validation
- duplicate request prevention

Reject:
- unnecessary abstraction
- giant util classes
- business logic in controllers
- missing transaction boundaries