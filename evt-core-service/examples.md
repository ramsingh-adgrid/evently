# Curl Examples — evt-core-service

## Create Event (success)
curl -s -X POST http://localhost:8081/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventName": "Coldplay Concert",
    "organizerName": "Live Nation",
    "organizerMobile": "9876543210",
    "city": "Mumbai",
    "category": "MUSIC"
  }' | jq .

## Create Event (duplicate mobile → 409)
curl -s -X POST http://localhost:8081/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventName": "Another Concert",
    "organizerName": "Someone Else",
    "organizerMobile": "9876543210",
    "city": "Delhi",
    "category": "SPORTS"
  }' | jq .

## Get Event by ID (success)
curl -s http://localhost:8081/v1/events/{uuid} | jq .

## Get Event by ID (not found → 404)
curl -s http://localhost:8081/v1/events/00000000-0000-0000-0000-000000000000 | jq .

## List Events (no filters)
curl -s "http://localhost:8081/v1/events?page=0&size=10" | jq .

## List Events (filtered by city)
curl -s "http://localhost:8081/v1/events?city=Mumbai&page=0&size=10" | jq .

## List Events (filtered by category)
curl -s "http://localhost:8081/v1/events?category=MUSIC&page=0&size=10" | jq .

## List Events (filtered by status)
curl -s "http://localhost:8081/v1/events?status=DRAFT&page=0&size=10" | jq .

## Update Status DRAFT → PUBLISHED (success)
curl -s -X PATCH http://localhost:8081/v1/events/{uuid}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "PUBLISHED"}' | jq .

## Update Status (invalid transition → 400)
curl -s -X PATCH http://localhost:8081/v1/events/{uuid}/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DRAFT"}' | jq .

## Get Stats
curl -s http://localhost:8081/v1/events/stats | jq .