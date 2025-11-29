# User Service

A Spring Boot microservice for managing user accounts, authentication, and user preferences in the Cooknect application.

## Overview

The User Service handles user registration, authentication, profile management, and user preferences including dietary preferences, health goals, and cuisine preferences. It's built with Spring Boot 3.5.6 and Java 21.

## Features

- **User Management**

  - User registration and authentication
  - Profile management (create, read, update, delete)
  - User query and search functionality

- **Preferences Management**

  - Health goals management
  - Dietary preferences configuration
  - Cuisine preferences setup

- **Authentication & Security**

  - User login functionality
  - JWT-based authentication
  - Secure user data handling

- **Event-Driven Architecture**
  - Kafka integration for user events
  - Event publishing for user state changes

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Java Version**: 21
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Message Broker**: Apache Kafka
- **Documentation**: Swagger/OpenAPI 3
- **Containerization**: Docker
- **Orchestration**: Kubernetes

## Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Actuator
- PostgreSQL Driver
- Common module (shared DTOs and events)
- Kafka integration
- Swagger for API documentation

## API Endpoints

### User Operations

- `GET /api/v1/users/` - Get all users with pagination
- `GET /api/v1/users/{id}` - Get user by ID
- `POST /api/v1/users/usernames` - Get users by usernames
- `GET /api/v1/users/user-details` - Get user details
- `PUT /api/v1/users/{id}` - Update user profile
- `DELETE /api/v1/users/{id}` - Delete user

### Authentication

- `POST /api/v1/users/login` - User login
- `POST /api/v1/users/register` - User registration

### Preferences Management

- `GET /api/v1/users/{id}/health-goal` - Get user health goals
- `GET /api/v1/users/{id}/dietary-preference` - Get dietary preferences
- `GET /api/v1/users/{id}/cuisine-preference` - Get cuisine preferences
- `PUT /api/v1/users/{id}/preferences` - Update general preferences
- `PUT /api/v1/users/{id}/health-preference` - Update health preferences
- `PUT /api/v1/users/{id}/dietary-preference` - Update dietary preferences
- `PUT /api/v1/users/{id}/cuisine-preference` - Update cuisine preferences

### Query Operations

- `POST /api/v1/users/query` - Advanced user queries

### Health Check

- `GET /actuator/health/user-service` - Service health check

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/cooknect/user_service/
│           ├── UserServiceApplication.java
│           ├── configuration/       # Configuration classes
│           ├── controller/         # REST controllers
│           ├── dto/               # Data Transfer Objects
│           ├── event/             # Kafka event handling
│           ├── interceptor/       # HTTP interceptors
│           ├── logging/           # Logging configuration
│           ├── model/             # Entity classes
│           ├── repository/        # Data repositories
│           ├── seeder/           # Data seeders
│           ├── service/          # Business logic
│           └── utils/            # Utility classes
└── test/                         # Test classes
```

## Getting Started

### Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL database
- Apache Kafka (for event processing)

### Installation

1. Clone the repository:

```bash
git clone <repository-url>
cd backend/user-service
```

2. Configure the database connection in `application.properties`

3. Build the project:

```bash
./mvnw clean install
```

4. Run the application:

```bash
./mvnw spring-boot:run
```

The service will start on the configured port (default: 8081).


## Configuration

Key configuration properties:

- Database connection settings
- Kafka broker configuration
- JWT settings
- Logging configuration
- Actuator endpoints

## Health Monitoring

The service includes Spring Boot Actuator for monitoring:

- Health checks available at `/actuator/health`
- Custom health endpoint at `/actuator/health/user-service`

## API Documentation

API documentation is available through Swagger UI when the service is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Event System

The service publishes user events to Kafka topics:

- User registration events
- User update events
- User deletion events

## Testing

Run tests with:

```bash
./mvnw test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
