# Challenge Service

A Spring Boot microservice for managing cooking challenges, participant registration, and leaderboards in the Cooknect application.

## Overview

The Challenge Service enables users to create and participate in cooking challenges, submit recipes, track progress, and compete on leaderboards. It's built with Spring Boot 3.5.6 and Java 21, providing a comprehensive challenge management system.

## Features

- **Challenge Management**

  - Create new cooking challenges
  - View all available challenges with pagination
  - Get detailed challenge information
  - Delete challenges

- **Participant Management**

  - Join challenges
  - Leave challenges
  - Payment confirmation for paid challenges
  - View challenge participants

- **Recipe Submission**

  - Submit recipes for challenges
  - Recipe validation and scoring
  - Track submission status

- **Leaderboard System**

  - Real-time leaderboard updates
  - Score calculation and ranking
  - Performance tracking

- **Event-Driven Architecture**
  - Kafka integration for challenge events
  - Real-time notifications for challenge updates

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
- PostgreSQL Driver
- Common module (shared DTOs and events)
- Kafka integration
- Swagger for API documentation

## API Endpoints

### Challenge Operations

- `POST /api/v1/challenges` - Create a new challenge
- `GET /api/v1/challenges` - Get all challenges with pagination
- `GET /api/v1/challenges/{id}` - Get challenge by ID
- `DELETE /api/v1/challenges/{id}` - Delete challenge

### Participation Management

- `POST /api/v1/challenges/{challengeId}/join` - Join a challenge
- `POST /api/v1/challenges/{challengeId}/confirm-payment` - Confirm payment for paid challenges
- `POST /api/v1/challenges/{challengeId}/leave` - Leave a challenge
- `GET /api/v1/challenges/{challengeId}/participants` - Get challenge participants

### Recipe Submission

- `POST /api/v1/challenges/{challengeId}/submit-recipe` - Submit recipe for challenge

### Leaderboard

- `GET /api/v1/challenges/{challengeId}/leaderboard` - Get challenge leaderboard

### Health Check

- `GET /actuator/health/challenge-service` - Service health check

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/cooknect/challenge_service/
│           ├── ChallengeServiceApplication.java
│           ├── config/            # Configuration classes
│           ├── controller/        # REST controllers
│           ├── dto/              # Data Transfer Objects
│           ├── event/            # Kafka event handling
│           ├── model/            # Entity classes
│           ├── repository/       # Data repositories
│           ├── service/          # Business logic
│           └── utils/            # Utility classes
└── test/                        # Test classes
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
cd backend/challenge-service
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

The service will start on the configured port (default: 8080).

## Configuration

Key configuration properties:

- Database connection settings
- Kafka broker configuration
- Payment processing settings
- Logging configuration

## Challenge Workflow

1. **Challenge Creation**: Admin creates a new cooking challenge
2. **Registration**: Users join challenges (with payment if required)
3. **Payment Confirmation**: For paid challenges, payment must be confirmed
4. **Recipe Submission**: Participants submit their recipes
5. **Scoring**: Recipes are evaluated and scored
6. **Leaderboard**: Real-time ranking updates
7. **Completion**: Challenge ends and winners are determined

## Event System

The service publishes challenge events to Kafka topics:

- Challenge creation events
- Participant join/leave events
- Recipe submission events
- Leaderboard update events

## Health Monitoring

The service includes Spring Boot Actuator for monitoring:

- Health checks available at `/actuator/health`
- Custom health endpoint at `/actuator/health/challenge-service`

## API Documentation

API documentation is available through Swagger UI when the service is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Testing

Run tests with:

```bash
./mvnw test
```

## Performance Features

- **Horizontal Pod Autoscaling**: Configured HPA for automatic scaling
- **Pagination**: Efficient data loading for large datasets
- **Caching**: Optimized database queries
- **Async Processing**: Non-blocking operations for better performance

## Security

- JWT-based authentication
- Input validation and sanitization
- Secure payment processing
- Role-based access control

## Monitoring and Logging

- Structured logging with appropriate log levels
- Health check endpoints for monitoring
- Performance metrics collection
- Error tracking and reporting

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

MIT License. See `LICENSE` file for details.
