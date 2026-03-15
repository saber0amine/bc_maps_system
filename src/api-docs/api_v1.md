# API V1

## Overview


### Authentication model

The API uses bearer token authentication.

Protected routes require the following header:

```http
Authorization: Bearer <token>
```

### Protected / Public routes

#### Public

* `POST /api/auth/register`
* `POST /api/auth/login`

#### Protected

* `POST /api/tokens`
* `GET /api/tokens`
* `DELETE /api/tokens/{id}`
* `POST /api/external-sources`
* `GET /api/external-sources/aggregate`
* `GET /api/collections`
* `GET /api/collections/places?tagId={uuid}`

---

## Common response formats

### Standard error object

```json
{
  "timeStamp": "2026-03-15T12:00:00",
  "message": "Error message",
  "httpStatus": 400
}
```

> Note: some security errors returned by the interceptor may only return an HTTP status without a JSON body.

---

# 1. Authentication

## 1.1 Register

Create a new user account and return a master token.

### Request

```http
POST /api/auth/register
Content-Type: application/json
```

### Body

```json
{
  "username": "amine",
  "email": "amine@gmail.com",
  "password": "123456"
}
```

### Response - `201 Created`

```json
{
  "userId": "2f8d4c68-4f6e-4e12-b0d8-25cb6f0d7a31",
  "username": "amine",
  "email": "amine@gmail.com",
  "token": "MASTER_TOKEN_VALUE"
}
```

### Possible errors

* `400 Bad Request` if username already exists
* `400 Bad Request` if email already exists

---

## 1.2 Login

Authenticate an existing user and return the master token.

### Request

```http
POST /api/auth/login
Content-Type: application/json
```

### Body

```json
{
  "email": "amine@test.com",
  "password": "123456"
}
```

### Response - `200 OK`

```json
{
  "userId": "2f8d4c68-4f6e-4e12-b0d8-25cb6f0d7a31",
  "username": "amine",
  "email": "amine@test.com",
  "token": "MASTER_TOKEN_VALUE"
}
```

### Possible errors

* `400 Bad Request` if credentials are invalid

---

# 2. Tokens ACL

## 2.1 Create a token

Generate a new token for the authenticated user, optionally with ACL permissions.

### Request

```http
POST /api/tokens
Authorization: Bearer <master_or_valid_token>
Content-Type: application/json
```

### Body

```json
{
  "description": "Read access to one tag",
  "expiresAt": "2026-12-31T23:59:59",
  "permissions": [
    {
      "resourceType": "TAG",
      "resourceId": "9c0a6f61-8e3d-4c4a-8e42-36f73b45c9c1",
      "accessType": "READ"
    }
  ]
}
```

### Fields

#### `resourceType`

Possible values:

* `PLACE`
* `COLLECTION`
* `TAG`

#### `accessType`

Project enum-based permission type. Typical values expected by the current ACL logic:

* `READ`
* `WRITE`
* `ADMIN`

### Response - `200 OK`

```json
{
  "id": "f5e23d2d-60f3-4e57-a18f-5f0fb4a3c732",
  "value": "GENERATED_TOKEN_VALUE",
  "description": "Read access to one tag",
  "createdAt": "2026-03-15T12:10:20",
  "expiresAt": "2026-12-31T23:59:59",
  "revoked": false,
  "serverUrl": "http://localhost:8080"
}
```

### Notes

* The token is created for the currently authenticated user.
* If `permissions` is omitted or empty, the token is still created.

### Possible errors

* `401 Unauthorized` if Authorization header is missing or invalid
* `403 Forbidden` if token is expired or revoked

---

## 2.2 List my active tokens

Return all active non-revoked tokens of the authenticated user.

### Request

```http
GET /api/tokens
Authorization: Bearer <valid_token>
```

### Response - `200 OK`

```json
[
  {
    "id": "f5e23d2d-60f3-4e57-a18f-5f0fb4a3c732",
    "value": "GENERATED_TOKEN_VALUE",
    "description": "Read access to one tag",
    "createdAt": "2026-03-15T12:10:20",
    "expiresAt": "2026-12-31T23:59:59",
    "revoked": false,
    "serverUrl": "http://localhost:8080"
  }
]
```

---

## 2.3 Revoke a token

Revoke a token by its identifier.

### Request

```http
DELETE /api/tokens/{id}
Authorization: Bearer <valid_token>
```

### Path parameter

* `id`: UUID of the token to revoke

### Response

* `204 No Content` on success
* `404 Not Found` if token is not found
* `403 Forbidden` if token does not belong to the authenticated user

### Important note

Current implementation contains a known issue in the revoke flow and may not behave correctly in all cases.

---

# 3. Collections

## 3.1 List accessible collections

Return the list of tags accessible by the current token according to ACL rules.

### Request

```http
GET /api/collections
Authorization: Bearer <valid_token>
```

### Response - `200 OK`

```json
[
  {
    "id": "9c0a6f61-8e3d-4c4a-8e42-36f73b45c9c1",
    "name": "restaurants",
    "createdAt": "2026-03-15T12:00:00"
  }
]
```

### Notes

* Collections are currently represented by `Tag` entities.
* The endpoint filters tags with ACL `canRead(...)`.

### Possible errors

* `401 Unauthorized`
* `403 Forbidden`

---

## 3.2 Get places of a collection (by tag)

Return all places attached to a tag, if the token has read access on that tag.

### Request

```http
GET /api/collections/places?tagId=9c0a6f61-8e3d-4c4a-8e42-36f73b45c9c1
Authorization: Bearer <valid_token>
```

### Query parameters

* `tagId`: UUID of the tag / collection

### Response - `200 OK`

```json
[
  {
    "id": "0f31d6f8-7449-4cdb-8db6-3d72b53c3219",
    "title": "Cafe Central",
    "description": "Test place",
    "latitude": 33.5731,
    "longitude": -7.5898,
    "imageUrl": "https://example.com/image.jpg",
    "isCurrentPosition": false,
    "createdAt": "2026-03-15T12:20:00",
    "updatedAt": "2026-03-15T12:20:00"
  }
]
```

### Possible errors

* `403 Forbidden` if token cannot read the given tag
* `404 Not Found` if the tag does not exist
* `401 Unauthorized` if token is missing or invalid

---

# 4. External Sources

## 4.1 Add an external source

Register an external server to aggregate places from another backend instance.

### Request

```http
POST /api/external-sources
Authorization: Bearer <valid_token>
Content-Type: application/json
```

### Body

```json
{
  "name": "Partner server",
  "serverUrl": "http://localhost:8081",
  "token": "REMOTE_SHARED_TOKEN"
}
```

### Response - `201 Created`

```json
{
  "id": "cb9ccf39-bfc4-46eb-9891-6cb8e6b9d16d",
  "name": "Partner server",
  "serverUrl": "http://localhost:8081",
  "token": "REMOTE_SHARED_TOKEN",
  "active": true,
  "lastSync": null
}
```

### Possible errors

* `401 Unauthorized`
* `403 Forbidden`
* `500 Internal Server Error` if current user cannot be resolved

---

## 4.2 Aggregate places from external sources

Call all active external sources registered by the current user and merge their `/api/places` responses.

### Request

```http
GET /api/external-sources/aggregate
Authorization: Bearer <valid_token>
```

### Response - `200 OK`

```json
[
  {
    "id": "remote-place-id-1",
    "title": "Remote place 1"
  },
  {
    "id": "remote-place-id-2",
    "title": "Remote place 2"
  }
]
```

### Aggregation behavior

* sends `Authorization: Bearer <source.token>` to the remote server
* calls `GET {serverUrl}/api/places`
* merges all successful JSON responses
* updates `lastSync` on successful synchronization
* disables a source automatically on remote `401 Unauthorized`
* keeps the source active on remote `403 Forbidden`
* ignores temporary failures and continues with the next source

### Important note

This endpoint assumes the remote backend exposes `GET /api/places`.

---

# 5. Security behavior

## Authorization header required

All protected endpoints require:

```http
Authorization: Bearer <token>
```

### Interceptor rules

* `401 Unauthorized` if header is missing
* `401 Unauthorized` if token value is unknown
* `403 Forbidden` if token exists but is revoked or expired

---

# 6. Enums used by the API

## ResourceType

```text
PLACE
COLLECTION
TAG
```

## AccessType

The project uses an enum-based access model for permissions.
Use the values defined in the backend enum currently integrated in the ACL flow.

Typical expected values:

```text
READ
WRITE
ADMIN
```

---

# 7. Not yet exposed as API

The following backend elements are not yet documented here because no public controller endpoint is currently available for them:

* Places CRUD endpoints
* Tags endpoints
* Import endpoints
* Export endpoints

---
