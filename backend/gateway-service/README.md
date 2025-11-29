# Gateway Service

A Spring Boot API Gateway microservice that provides centralized routing, authentication, and load balancing for the Cooknect application's microservices architecture.

## Overview

The Gateway Service acts as the single entry point for all client requests in the Cooknect ecosystem. It handles request routing, JWT authentication, authorization, and provides a unified API interface for frontend applications. Built with Spring Cloud Gateway MVC and Spring Boot 3.5.6.

## Features

- **API Gateway & Routing**

  - Centralized request routing to microservices
  - Path-based routing configuration
  - Load balancing and service discovery
  - Health check aggregation

- **Authentication & Authorization**

  - JWT token validation and processing
  - User role extraction and forwarding
  - Security context management
  - Request header enrichment with user information

- **Service Integration**

  - Routes to User Service (port 8081)
  - Routes to Recipe Service (port 8082)
  - Routes to Challenge Service (port 8083)
  - Routes to Notification Service (port 8084)
  - Routes to Nutrition Service (port 8085)

- **API Documentation Aggregation**

  - Centralized Swagger/OpenAPI documentation
  - Service-specific API docs aggregation
  - Unified documentation endpoint

- **GraphQL Support**
  - GraphQL endpoint routing for multiple services
  - Query aggregation and forwarding

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Java Version**: 21
- **Gateway**: Spring Cloud Gateway MVC
- **Security**: Spring Security with JWT
- **Documentation**: Swagger/OpenAPI 3
- **Containerization**: Docker
- **Orchestration**: Kubernetes

## Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Security
- Spring Cloud Gateway Server WebMVC
- JWT (JSON Web Token) processing
- Common module for shared DTOs

## Routing Configuration

### Service Routes

| Service              | Path Pattern               | Target Port | Description                         |
| -------------------- | -------------------------- | ----------- | ----------------------------------- |
| User Service         | `/api/v1/users/**`         | 8081        | User management and authentication  |
| Recipe Service       | `/api/v1/recipes/**`       | 8082        | Recipe management and operations    |
| Challenge Service    | `/api/v1/challenges/**`    | 8083        | Cooking challenges and competitions |
| Notification Service | `/api/v1/notifications/**` | 8084        | Email and notification management   |
| Nutrition Service    | `/api/v1/nutrition/**`     | 8085        | Nutritional analysis and data       |

### Health Check Routes

- `http://localhost:8081/actuator/health` → User Service health
- `http://localhost:8082/actuator/health` → Recipe Service health
- `http://localhost:8083/actuator/health` → Challenge Service health
- `http://localhost:8084/actuator/health` → Notification Service health
- `http://localhost:8085/actuator/health` → Nutrition Service health

### GraphQL Routes

- `/api/users/graphql` → User Service GraphQL
- `/api/recipes/graphql` → Recipe Service GraphQL
- `/api/challenges/graphql` → Challenge Service GraphQL
- `/api/nutrition/graphql` → Nutrition Service GraphQL

### API Documentation Routes

- `/aggregate/user-service/v3/api-docs` → User Service API docs
- `/aggregate/recipe-service/v3/api-docs` → Recipe Service API docs
- `/aggregate/challenge-service/v3/api-docs` → Challenge Service API docs
- `/aggregate/notification-service/v3/api-docs` → Notification Service API docs
- `/aggregate/nutrition-service/v3/api-docs` → Nutrition Service API docs

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/cooknect/gateway_service/
│           ├── GatewayServiceApplication.java
│           ├── configuration/     # Spring configuration classes
│           ├── exception/         # Exception handling
│           ├── routes/           # Gateway routing configuration
│           └── service/          # Business logic (JWT, etc.)
└── test/                        # Test classes
```

## Authentication Flow

1. **Request Reception**: Client sends request with JWT token in Authorization header
2. **Token Extraction**: Gateway extracts JWT from Authorization header
3. **Token Validation**: JWT service validates token signature and expiration
4. **User Information Extraction**: Extract user ID, email, and role from token
5. **Header Enrichment**: Add user information to request headers:
   - `X-User-Id`: User identifier
   - `X-User-Email`: User email address
   - `X-User-Role`: User role/permissions
6. **Request Forwarding**: Route request to appropriate microservice with enriched headers

## Security Features

- **JWT Token Validation**: Validates token signature and expiration
- **Role-Based Access Control**: Extracts and forwards user roles
- **Secure Headers**: Adds security-related headers to requests
- **Authentication Context**: Maintains security context throughout request lifecycle

## Getting Started

### Prerequisites

- Java 21
- Maven 3.6+
- All downstream microservices running on configured ports

### Installation

1. Clone the repository:

```bash
git clone <repository-url>
cd backend/gateway-service
```

2. Configure JWT secret and service endpoints in `application.properties`

3. Build the project:

```bash
./mvnw clean install
```

4. Run the application:

```bash
./mvnw spring-boot:run
```

The gateway will start on the configured port (default: 8089).

## Configuration

Key configuration properties:

- **JWT Secret**: Secret key for token validation
- **Service URLs**: Target URLs for each microservice
- **CORS Settings**: Cross-origin resource sharing configuration
- **Security Settings**: Authentication and authorization rules
- **Routing Rules**: Path matching and forwarding rules

## Load Balancing

- **Horizontal Pod Autoscaling**: Automatic scaling based on traffic
- **Service Discovery**: Dynamic service endpoint resolution
- **Health Checks**: Continuous monitoring of downstream services
- **Circuit Breaker**: Fault tolerance for service failures

## Monitoring and Observability

- **Request Tracing**: Distributed tracing across services
- **Metrics Collection**: Gateway performance metrics
- **Health Endpoints**: Service health aggregation
- **Error Tracking**: Centralized error logging and monitoring

## API Documentation

The gateway aggregates API documentation from all services:

- **Unified Swagger UI**: Single interface for all service APIs
- **Service-Specific Docs**: Individual service documentation
- **GraphQL Schema**: GraphQL endpoint documentation

## Development

### Adding New Routes

1. Define route in `Routes.java`
2. Configure path patterns and target services
3. Add authentication/authorization if needed
4. Update documentation

### JWT Configuration

The JWT service handles:

- Token validation
- User information extraction
- Role-based authorization
- Security context management

## Security Considerations

- **Token Expiration**: JWT tokens have configurable expiration
- **Secret Management**: JWT secret should be externalized
- **HTTPS**: Use HTTPS in production environments
- **Rate Limiting**: Implement rate limiting for API protection

## Performance Features

- **Connection Pooling**: Efficient connection management
- **Request Caching**: Cacheable response handling
- **Async Processing**: Non-blocking request processing
- **Resource Optimization**: Memory and CPU optimization

## Troubleshooting

### Common Issues

1. **Service Unreachable**: Check if downstream services are running
2. **JWT Validation Errors**: Verify JWT secret configuration
3. **Routing Issues**: Check path patterns and service URLs
4. **Authentication Failures**: Validate JWT token format and content

### Health Checks

Monitor service health through:

- Individual service health endpoints
- Gateway aggregated health status
- Kubernetes readiness/liveness probes

## Contributing

1. Fork the repository
2. Create a feature branch
3. Implement changes with appropriate tests
4. Update routing configuration if needed
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
