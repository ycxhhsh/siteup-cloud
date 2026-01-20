# SiteUp Cloud Error Codes

This document defines all error codes used across the SiteUp Cloud microservices.

## Error Response Format

All error responses follow this unified format:

```json
{
  "code": "ERROR_CODE",
  "message": "Human readable error message",
  "timestamp": "2024-01-14T10:30:00Z",
  "detail": "Optional additional details"
}
```

## Common Error Codes

### Resource Errors (4xx)

| Code | HTTP Status | Description | Services |
|------|-------------|-------------|----------|
| `RESOURCE_NOT_FOUND` | 404 | The requested resource was not found | All services |
| `INVALID_REQUEST` | 400 | Request parameters are invalid or malformed | All services |

### Authentication & Authorization Errors (4xx)

| Code | HTTP Status | Description | Services |
|------|-------------|-------------|----------|
| `USER_NOT_FOUND` | 404 | User account does not exist | siteup-auth |
| `INVALID_CREDENTIALS` | 401 | Username or password is incorrect | siteup-auth |

### Service Errors (5xx)

| Code | HTTP Status | Description | Services |
|------|-------------|-------------|----------|
| `SERVICE_UNAVAILABLE` | 503 | External service is currently unavailable | siteup-biz |
| `INTERNAL_ERROR` | 500 | Unexpected internal server error | All services |

## Service-Specific Error Codes

### Business Service (siteup-biz)

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `TEMPLATE_NOT_FOUND` | 404 | Template with specified ID does not exist |
| `PROJECT_NOT_FOUND` | 404 | Project with specified ID does not exist |
| `INVALID_PROJECT_CONFIG` | 400 | Project configuration JSON is invalid |

### Authentication Service (siteup-auth)

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `USER_ALREADY_EXISTS` | 409 | User account already exists during registration |
| `TOKEN_EXPIRED` | 401 | Authentication token has expired |
| `INVALID_TOKEN` | 401 | Authentication token is invalid or malformed |

### Engine Service (siteup-engine)

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `RENDERING_FAILED` | 500 | Failed to render HTML from configuration |
| `INVALID_COMPONENT_TYPE` | 400 | Unknown component type in configuration |

## Error Handling Best Practices

### Client Error Handling

1. **Check HTTP Status Code First**
   - 4xx: Client errors (validation, authentication)
   - 5xx: Server errors (infrastructure, bugs)

2. **Parse Error Code**
   - Use error code for programmatic handling
   - Display user-friendly message from error message field

3. **Retry Logic**
   - 5xx errors may be retried with exponential backoff
   - 4xx errors should not be retried without fixing the request

### Example Error Handling (JavaScript)

```javascript
try {
  const response = await fetch('/api/projects/123/publish');
  if (!response.ok) {
    const error = await response.json();
    switch (error.code) {
      case 'PROJECT_NOT_FOUND':
        showError('Project not found. Please check the project ID.');
        break;
      case 'SERVICE_UNAVAILABLE':
        showError('Service temporarily unavailable. Please try again later.');
        break;
      default:
        showError(error.message);
    }
  }
} catch (networkError) {
  showError('Network error. Please check your connection.');
}
```

### Example Error Handling (Java)

```java
try {
  Project project = projectService.publishProject(projectId);
  return ResponseEntity.ok(project);
} catch (ResourceNotFoundException e) {
  // Handle 404
  return ResponseEntity.notFound().build();
} catch (ServiceUnavailableException e) {
  // Handle 503
  return ResponseEntity.status(503).build();
} catch (Exception e) {
  // Handle 500
  return ResponseEntity.status(500).build();
}
```

## Monitoring & Alerting

Error codes should be monitored for:
- High frequency of specific error codes
- Sudden increases in error rates
- New error codes appearing in logs

Key metrics to monitor:
- Error rate by service
- Error rate by error code
- Response time degradation during errors
