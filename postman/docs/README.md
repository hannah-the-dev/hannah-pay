# HannahPay User API Postman Collection

Files:
- `postman/collections/HannahPay - User API/`
- `postman/environments/HannahPay Local.environment.yaml`

## Endpoints
- `POST /users/signup`
- `POST /users/login`
- `GET /users/me`
- `DELETE /users/me`

## How to use
1. Import both JSON files into Postman.
2. Select the `HannahPay Local` environment.
3. Run `Signup` or `Login` first to populate `accessToken`.
4. Run `Me` to verify authenticated profile access.
5. Run `Withdraw` to verify authenticated account withdrawal.

The signup flow generates a unique email per run, so the same asset set can be reused repeatedly in CI and manual testing.

## CI / Newman
Example:

```bash
newman run postman/collections/hannahpay-user.postman_collection.json \
  -e postman/environments/hannahpay-user.postman_environment.json
```

For CI, override values with environment variables or an exported environment file after import.
