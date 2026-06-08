# Evently - BFF (Backend-For-Frontend) Edge Service

`evt-bff` is the edge gateway service for the Evently ticketing platform. It handles incoming REST requests, enforces input validation at the edge, validates headers/query constraints, and communicates downstream with `evt-open-service` via OpenFeign.

## Features

1. **Edge-Level Validation**: Uses `@Valid` alongside `@NotBlank`, `@Pattern`, and `@NotNull` annotations to ensure data integrity before letting requests traverse inner services.
2. **Feign with OkHttp**: Integrated with OkHttp backing client to support standard HTTP `PATCH` requests (which the default Java `HttpURLConnection` client fails on).
3. **Downstream Error Propagation**: Uses a custom `FeignErrorDecoder` to parse downstream JSON error envelopes and map HTTP codes (e.g. 400, 404, 409) to local typed exceptions (`BadRequestException`, `ResourceNotFoundException`, `DuplicateResourceException`).
4. **Standard Envelope Enforcement**: Enforces the API response envelope in all success and error responses:
   - Success: `{ "success": true, "data": { ... } }`
   - Failure: `{ "success": false, "message": "error description" }`

---

## Configuration Details

- **Port**: `8080` (edge port exposed on localhost)
- **Downstream URL**: Connects to `evt-open-service` at port `8082` (configured via env var `EVT_OPEN_SERVICE_URL` in container).
- **Lombok Integration**: Configured in `pom.xml` via `maven-compiler-plugin` for automatic generation of getters, setters, loggers, and builders.
- **Spring Cloud Version Compatibility**: Since parent Pom uses Spring Boot `3.5.14`, compatibility verifications are bypassed (`spring.cloud.compatibility-verifier.enabled=false`) to support the Spring Cloud release train safely.

---

## Exposed API Endpoints

All endpoints are prefix-configured under `/api/v1/events`:

| Method | Endpoint | Description | Request Validation |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/events` | Create a new event (Status: `DRAFT`) | Name, organizer name/mobile, city, category |
| **GET** | `/api/v1/events/{id}` | Get detailed information of a specific event | UUID path parameter |
| **GET** | `/api/v1/events` | Paginated and filtered list of events | Page (default 0), size (default 10), optional filters (`city`, `category`, `status`) |
| **PATCH** | `/api/v1/events/{id}/status` | Transition event status | Status must be valid enum |
| **GET** | `/api/v1/events/stats` | Retrieve aggregated event stats | None |

---

## Running Locally

To build and run tests:
```bash
mvn clean package
```

To run the application standalone (expects `evt-open-service` running on `localhost:8082`):
```bash
mvn spring-boot:run
```
