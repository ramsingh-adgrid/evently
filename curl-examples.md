# Evently API Curl Examples - Phase 2

All edge requests are routed through `evt-bff` on port `8080`.

---

## 1. Create a New Event (POST)

Creates an event in the `DRAFT` state.

```bash
curl -i -X POST -H "Content-Type: application/json" \
  -d '{"eventName":"Live Jazz Night","organizerName":"Blue Note Jazz","organizerMobile":"1234567890","city":"San Francisco","category":"MUSIC"}' \
  http://localhost:8080/api/v1/events
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": "3fea1182-84cd-451a-b9a3-a20b999ec987",
    "eventName": "Live Jazz Night",
    "organizerName": "Blue Note Jazz",
    "organizerMobile": "1234567890",
    "city": "San Francisco",
    "category": "MUSIC",
    "status": "DRAFT",
    "createdOn": "2026-06-08T14:37:22.962222",
    "modifiedOn": "2026-06-08T14:37:22.962222"
  },
  "message": null
}
```

---

## 2. Get Event Details (GET)

Fetches a single event by its UUID.

```bash
curl -i -X GET http://localhost:8080/api/v1/events/3fea1182-84cd-451a-b9a3-a20b999ec987
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "3fea1182-84cd-451a-b9a3-a20b999ec987",
    "eventName": "Live Jazz Night",
    "organizerName": "Blue Note Jazz",
    "organizerMobile": "1234567890",
    "city": "San Francisco",
    "category": "MUSIC",
    "status": "DRAFT",
    "createdOn": "2026-06-08T14:37:22.962222",
    "modifiedOn": "2026-06-08T14:37:22.962222"
  },
  "message": null
}
```

---

## 3. Update Event Status (PATCH)

Updates status from `DRAFT` to `PUBLISHED` (or subsequent allowed transitions).

```bash
curl -i -X PATCH -H "Content-Type: application/json" \
  -d '{"status":"PUBLISHED"}' \
  http://localhost:8080/api/v1/events/3fea1182-84cd-451a-b9a3-a20b999ec987/status
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "3fea1182-84cd-451a-b9a3-a20b999ec987",
    "eventName": "Live Jazz Night",
    "organizerName": "Blue Note Jazz",
    "organizerMobile": "1234567890",
    "city": "San Francisco",
    "category": "MUSIC",
    "status": "PUBLISHED",
    "createdOn": "2026-06-08T14:37:22.962222",
    "modifiedOn": "2026-06-08T14:39:14.046384"
  },
  "message": null
}
```

---

## 4. Get Statistics (GET)

Retrieves event metrics aggregated by status and category.

```bash
curl -i -X GET http://localhost:8080/api/v1/events/stats
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "totalEvents": 1,
    "byStatus": {
      "CANCELLED": 0,
      "PUBLISHED": 1,
      "DRAFT": 0,
      "SOLD_OUT": 0
    },
    "byCategory": {
      "WORKSHOP": 0,
      "OTHER": 0,
      "MUSIC": 1,
      "SPORTS": 0,
      "COMEDY": 0
    }
  },
  "message": null
}
```

---

## 5. List Events with Filters & Pagination (GET)

Lists events, pushing pagination and active filtering down to the database query level.

```bash
curl -i -X GET "http://localhost:8080/api/v1/events?city=San%20Francisco"
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "events": [
      {
        "id": "3fea1182-84cd-451a-b9a3-a20b999ec987",
        "eventName": "Live Jazz Night",
        "organizerName": "Blue Note Jazz",
        "organizerMobile": "1234567890",
        "city": "San Francisco",
        "category": "MUSIC",
        "status": "PUBLISHED",
        "createdOn": "2026-06-08T14:37:22.962222",
        "modifiedOn": "2026-06-08T14:39:14.046384"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "currentPage": 0
  },
  "message": null
}
```

---

## 6. Error & Validation Verification

### A. Edge Validation Failure (Bad Mobile Format)
Checks constraints on input payload length and regex at the BFF layer.
```bash
curl -i -X POST -H "Content-Type: application/json" \
  -d '{"eventName":"Comedy Show","organizerName":"Laugh Factory","organizerMobile":"123","city":"San Francisco","category":"COMEDY"}' \
  http://localhost:8080/api/v1/events
```
**Response (400 Bad Request):**
```json
{"success":false,"data":null,"message":"mobile must be of 10 numbers"}
```

### B. Duplicate Organizer Mobile
DB unique mobile constraint mapping.
```bash
curl -i -X POST -H "Content-Type: application/json" \
  -d '{"eventName":"Comedy Show","organizerName":"Laugh Factory","organizerMobile":"1234567890","city":"San Francisco","category":"COMEDY"}' \
  http://localhost:8080/api/v1/events
```
**Response (409 Conflict):**
```json
{"success":false,"data":null,"message":"Organizer mobile already registered: 1234567890"}
```

### C. Illegal Status Transition
Business logic state transitions error propagation.
```bash
curl -i -X PATCH -H "Content-Type: application/json" \
  -d '{"status":"DRAFT"}' \
  http://localhost:8080/api/v1/events/3fea1182-84cd-451a-b9a3-a20b999ec987/status
```
**Response (400 Bad Request):**
```json
{"success":false,"data":null,"message":"Invalid status transition: PUBLISHED -> DRAFT"}
```

### D. Not Found
Requesting a non-existent UUID.
```bash
curl -i -X GET http://localhost:8080/api/v1/events/00000000-0000-0000-0000-000000000000
```
**Response (404 Not Found):**
```json
{"success":false,"data":null,"message":"Event not found: 00000000-0000-0000-0000-000000000000"}
```
