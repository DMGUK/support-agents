# API Reference

## Base URL
```
https://api.support-platform.example.com/v2
```

All requests must include the following headers:
```
Authorization: Bearer YOUR_API_KEY
Content-Type: application/json
Accept: application/json
```

## Authentication

API keys are generated under **Settings → API Keys**. Keys have three permission scopes:
- `read` — GET endpoints only
- `write` — GET and POST/PUT/DELETE endpoints
- `admin` — all endpoints including account management

Keys can be rotated at any time; old keys are invalidated immediately upon rotation.

## Rate Limits

| Plan  | Requests / minute | Requests / day |
|-------|-------------------|----------------|
| Free  | 10                | 500            |
| Pro   | 120               | 50 000         |
| Enterprise | Unlimited    | Unlimited      |

When a rate limit is exceeded the API returns HTTP 429 with header `Retry-After` (seconds).

## Core Endpoints

### POST /events

Submit a new event.

Request body:
```json
{
  "type": "user.action",
  "payload": { "key": "value" },
  "timestamp": "2025-06-01T12:00:00Z"
}
```

Response:
```json
{
  "event_id": "evt_abc123",
  "status": "accepted"
}
```

### GET /events/{event_id}

Retrieve a previously submitted event by ID.

### GET /status

Returns the current API status and version.

## Error Codes

| Code | Meaning |
|------|---------|
| 400  | Bad Request — malformed JSON or missing required field |
| 401  | Unauthorized — missing or invalid API key |
| 403  | Forbidden — API key lacks required scope |
| 404  | Not Found — resource does not exist |
| 429  | Too Many Requests — rate limit exceeded |
| 500  | Internal Server Error — contact support |

## Pagination

List endpoints accept `page` (default 1) and `per_page` (default 20, max 100) query parameters.