# Evently Backend System

Evently is a multi-service event ticketing platform backend designed using Spring Boot, gRPC, and REST gateways.

## Architecture Layout

```text
  cURL / Postman
         ↓  (REST / JSON)  [Port 8080]
     [evt-bff]
         ↓  (REST / OpenFeign + OkHttp)  [Port 8082]
  [evt-open-service]
         ↓  (gRPC / Protobuf)  [Port 9090]
  [evt-core-service]
         ↓  (Spring Data JPA)  [Port 5432]
     PostgreSQL
```

---

## Life of a Request (POST Event)

Below is the end-to-end journey of a `POST /api/v1/events` request through the class layers of our three services:

1. **Client Call**: A client issues a `POST` request to `http://localhost:8080/api/v1/events`.
2. **`evt-bff` Entry**:
   - The request hits `EventBffController.createEvent(CreateEventRequest)`.
   - Before method execution, Spring validation (`@Valid` on the request class) inspects constraints (e.g. `organizerMobile` format). If invalid, `GlobalExceptionHandler` intercepts and returns a `400 Bad Request` envelope.
   - The controller calls the OpenFeign interface `EventOpenServiceClient.createEvent(request)`.
3. **HTTP Hop (BFF -> Open Service)**:
   - Feign (backed by `feign-okhttp`) serializes the payload to JSON and sends a `POST` request to `http://evt-open-service:8082/open/v1/events`.
4. **`evt-open-service` Entry**:
   - The request is received by `EventOpenController.createEvent(CreateEventRequest)`.
   - The controller delegates to `EventGrpcClientService.createEvent(CreateEventRequest)`.
   - `EventGrpcClientService` maps the REST DTO into the protobuf representation: `com.evently.grpc.CreateEventRequest`.
   - It fires the request using `EventServiceGrpc.EventServiceBlockingStub.createEvent(...)`.
5. **gRPC Hop (Open Service -> Core Service)**:
   - The gRPC framework transfers the binary payload over HTTP/2 to port `9090` of the core service container.
6. **`evt-core-service` Entry**:
   - The gRPC request is intercepted by `EventGrpcService.createEvent(protoRequest, responseObserver)`.
   - `EventGrpcService` maps the proto request to a domain request, calling the business service `EventService.createEvent(CreateEventRequest)`.
   - `EventService` performs database checks (e.g. duplicate organizer mobile) via `EventRepository`.
   - `EventService` persists a new `Event` entity. If successful, it returns an `EventResponse` DTO.
   - `EventGrpcService` maps the response DTO to protobuf `EventResponse` and sends it back to the client via `responseObserver.onNext(protoResponse)` and `onCompleted()`.
7. **Return Journey**:
   - `EventGrpcClientService` receives the protobuf response and deserializes it back to a REST DTO, returning it up the controllers to the client.
   - *Error Handling*: If the database check throws a `DuplicateResourceException` in the core service, the gRPC handler converts it to `Status.ALREADY_EXISTS`. In the open-service, this is caught by `EventGrpcClientService` and re-thrown as a local `DuplicateResourceException` which `GlobalExceptionHandler` converts to a `409 Conflict` HTTP envelope.

---

## Phase 2 Self-Check Answers

### 1. Why use gRPC between internal services but REST at the edge?
- **REST at the Edge**: External clients (web browsers, mobile apps, third-party developers) natively support HTTP/JSON. REST is standardized, readable, easy to cache at CDN level, and easy to secure and load-balance.
- **gRPC Internally**: Internal service-to-service communication requires high throughput and low latency. gRPC operates over HTTP/2 (multiplexed TCP streams, smaller header size) and compiles to a binary format (Protocol Buffers) which is significantly faster to serialize/deserialize and smaller to transmit than JSON text. Additionally, it provides strictly typed interfaces natively.

### 2. What breaks if the proto file adds a field — which services need rebuilding, and why is the shared-contracts module the answer?
- When a proto field is added, both the producer (gRPC server) and consumer (gRPC client) must recognize the new class structure to send or parse that field.
- Without a shared module, you would manually copy-paste the `.proto` file to multiple folders, which leads to out-of-sync contracts and runtime failures.
- The **shared-contracts module (`evt-grpc-contracts`)** is the single source of truth. When it is compiled, it generates all the necessary stubs. Both services simply reference this module, guaranteeing that both compile and run with the identical wire format definitions.

### 3. If open-service is down, what does the BFF return today? What *should* it return?
- **Today**: The BFF's Feign client will fail to make connection and throw a `RetryableException` (or `ConnectException`). Since we do not catch this specifically, it gets caught by the fallback `Exception.class` handler in the `GlobalExceptionHandler` and returns a generic `500 Internal Server Error` envelope.
- **Should Return**: It should return a `503 Service Unavailable` error envelope indicating that the downstream dependency is unreachable, or run a fallback method (e.g. using Circuit Breaker) that returns a default/cached response instead of failing.
