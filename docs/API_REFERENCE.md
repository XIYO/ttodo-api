# TTODO API Reference

## Base URL

- Development: `http://localhost:8080`
- Production: `https://api.ttodo.dev`

## Authentication

All endpoints except those under `/auth/**` require JWT authentication via:

- **Header**: `Authorization: Bearer <token>`
- **Cookie**: `access-token=<token>`

## API Endpoints

### 🔐 Authentication (`/auth`)

| Method | Endpoint          | Description               | Auth |
|--------|-------------------|---------------------------|------|
| POST   | `/auth/sign-up`   | Register new user         | ❌    |
| POST   | `/auth/sign-in`   | Login                     | ❌    |
| POST   | `/auth/sign-out`  | Logout                    | ❌    |
| POST   | `/auth/refresh`   | Refresh token             | 🔄   |
| GET    | `/auth/dev-token` | Get test token (dev only) | ❌    |

### 👤 Members (`/members`)

| Method | Endpoint               | Description           | Auth |
|--------|------------------------|-----------------------|------|
| GET    | `/members/me`          | Get current user info | ✅    |
| PUT    | `/members/me`          | Update user info      | ✅    |
| DELETE | `/members/me`          | Delete account        | ✅    |
| POST   | `/members/me/password` | Change password       | ✅    |

### 📝 Todos (`/todos`)

| Method | Endpoint                 | Description            | Auth |
|--------|--------------------------|------------------------|------|
| GET    | `/todos`                 | List todos (paginated) | ✅    |
| POST   | `/todos`                 | Create todo            | ✅    |
| GET    | `/todos/{id}`            | Get todo details       | ✅    |
| PUT    | `/todos/{id}`            | Update todo            | ✅    |
| DELETE | `/todos/{id}`            | Delete todo            | ✅    |
| POST   | `/todos/{id}/complete`   | Mark as complete       | ✅    |
| POST   | `/todos/{id}/uncomplete` | Mark as incomplete     | ✅    |
| GET    | `/todos/statistics`      | Get todo statistics    | ✅    |
| GET    | `/todos/calendar`        | Get calendar view      | ✅    |
| POST   | `/todos/order`           | Change todo order      | ✅    |

### 📁 Categories (`/categories`)

| Method | Endpoint                                  | Description         | Auth |
|--------|-------------------------------------------|---------------------|------|
| GET    | `/categories`                             | List categories     | ✅    |
| POST   | `/categories`                             | Create category     | ✅    |
| GET    | `/categories/{id}`                        | Get category        | ✅    |
| PUT    | `/categories/{id}`                        | Update category     | ✅    |
| DELETE | `/categories/{id}`                        | Delete category     | ✅    |
| POST   | `/categories/{id}/collaborators`          | Add collaborator    | ✅    |
| DELETE | `/categories/{id}/collaborators/{userId}` | Remove collaborator | ✅    |

### 🏆 Challenges (`/challenges`)

| Method | Endpoint                        | Description       | Auth |
|--------|---------------------------------|-------------------|------|
| GET    | `/challenges`                   | List challenges   | ❌    |
| POST   | `/challenges`                   | Create challenge  | ✅    |
| GET    | `/challenges/{id}`              | Get challenge     | ✅    |
| PUT    | `/challenges/{id}`              | Update challenge  | ✅    |
| DELETE | `/challenges/{id}`              | Delete challenge  | ✅    |
| POST   | `/challenges/{id}/join`         | Join challenge    | ✅    |
| POST   | `/challenges/{id}/leave`        | Leave challenge   | ✅    |
| GET    | `/challenges/{id}/participants` | List participants | ✅    |
| GET    | `/challenges/{id}/leaderboard`  | Get leaderboard   | ✅    |

### 👤 Profile (`/profiles`)

| Method | Endpoint             | Description      | Auth |
|--------|----------------------|------------------|------|
| GET    | `/profiles/me`       | Get my profile   | ✅    |
| PUT    | `/profiles/me`       | Update profile   | ✅    |
| GET    | `/profiles/{userId}` | Get user profile | ✅    |

### 📊 Statistics (`/statistics`)

| Method | Endpoint                 | Description        | Auth |
|--------|--------------------------|--------------------|------|
| GET    | `/statistics/me`         | Get my statistics  | ✅    |
| GET    | `/statistics/me/daily`   | Daily statistics   | ✅    |
| GET    | `/statistics/me/weekly`  | Weekly statistics  | ✅    |
| GET    | `/statistics/me/monthly` | Monthly statistics | ✅    |

### 🏷️ Tags (`/tags`)

| Method | Endpoint        | Description      | Auth |
|--------|-----------------|------------------|------|
| GET    | `/tags`         | List all tags    | ✅    |
| GET    | `/tags/popular` | Get popular tags | ✅    |
| POST   | `/tags`         | Create tag       | ✅    |
| DELETE | `/tags/{id}`    | Delete tag       | ✅    |

### ⭐ Experience (`/experience`)

| Method | Endpoint                  | Description         | Auth |
|--------|---------------------------|---------------------|------|
| GET    | `/experience/me`          | Get my XP and level | ✅    |
| GET    | `/experience/levels`      | List all levels     | ✅    |
| GET    | `/experience/leaderboard` | Global leaderboard  | ✅    |

## Request/Response Formats

### Common Request Headers

```http
Authorization: Bearer eyJ...
Content-Type: application/json
Accept: application/json
```

### Common Response Structure

#### Success Response

```json
{
  "data": {},
  "message": "Success",
  "timestamp": "2025-01-01T00:00:00Z"
}
```

#### Error Response

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/todos",
  "timestamp": "2025-01-01T00:00:00Z",
  "errors": [
    {
      "field": "title",
      "message": "Must not be blank"
    }
  ]
}
```

### Pagination

Paginated endpoints support these query parameters:

| Parameter | Type   | Default | Description              |
|-----------|--------|---------|--------------------------|
| page      | int    | 0       | Page number (0-indexed)  |
| size      | int    | 20      | Items per page           |
| sort      | string | id,desc | Sort field and direction |

Example:

```http
GET /todos?page=0&size=10&sort=createdAt,desc
```

Response:

```json
{
  "content": [...],
  "pageable": {
    "sort": {
      "sorted": true,
      "descending": true
    },
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false
}
```

## Data Models

### User/Member

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "nickname": "username",
  "createdAt": "2025-01-01T00:00:00Z",
  "updatedAt": "2025-01-01T00:00:00Z"
}
```

### Todo

```json
{
  "id": "uuid",
  "title": "Task title",
  "description": "Task description",
  "completed": false,
  "priority": "MEDIUM",
  "dueDate": "2025-01-01",
  "categoryId": "uuid",
  "tags": ["tag1", "tag2"],
  "createdAt": "2025-01-01T00:00:00Z",
  "updatedAt": "2025-01-01T00:00:00Z"
}
```

### Category

```json
{
  "id": "uuid",
  "name": "Work",
  "color": "#FF5733",
  "icon": "briefcase",
  "shared": false,
  "ownerId": "uuid",
  "collaborators": []
}
```

### Challenge

```json
{
  "id": "uuid",
  "title": "30-Day Challenge",
  "description": "Complete tasks for 30 days",
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "visibility": "PUBLIC",
  "participantCount": 10,
  "creatorId": "uuid"
}
```

## Status Codes

| Code | Description           | Usage                 |
|------|-----------------------|-----------------------|
| 200  | OK                    | Successful GET, PUT   |
| 201  | Created               | Successful POST       |
| 204  | No Content            | Successful DELETE     |
| 400  | Bad Request           | Validation error      |
| 401  | Unauthorized          | Missing/invalid token |
| 403  | Forbidden             | No permission         |
| 404  | Not Found             | Resource not found    |
| 409  | Conflict              | Duplicate resource    |
| 500  | Internal Server Error | Server error          |

## Rate Limiting

- **Anonymous**: 100 requests/hour
- **Authenticated**: 1000 requests/hour
- **Premium**: Unlimited

Rate limit headers:

```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1640995200
```

## Webhooks

Configure webhooks for these events:

- `todo.created`
- `todo.completed`
- `challenge.joined`
- `challenge.completed`

## SDK Examples

### JavaScript/TypeScript

```javascript
const response = await fetch('http://localhost:8080/todos', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
const todos = await response.json();
```

### Java

```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/todos"))
    .header("Authorization", "Bearer " + token)
    .GET()
    .build();
```

### Python

```python
import requests

response = requests.get(
    'http://localhost:8080/todos',
    headers={'Authorization': f'Bearer {token}'}
)
todos = response.json()
```

## Swagger Documentation

Interactive API documentation available at:

- Development: http://localhost:8080/swagger-ui/index.html
- API Spec: http://localhost:8080/v3/api-docs