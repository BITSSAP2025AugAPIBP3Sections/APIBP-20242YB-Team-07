# Notification Service

A Spring Boot microservice for managing notifications, email communications, and real-time messaging in the Cooknect application ecosystem.

## Overview

The Notification Service handles all communication-related functionalities including email notifications, in-app notifications, and event-driven messaging. It integrates with Kafka to process events from other microservices and sends appropriate notifications to users. Built with Spring Boot 3.5.6 and Java 21.

## Features

- **Email Notifications**

  - SMTP email sending capabilities
  - HTML and plain text email support
  - Template-based email composition
  - Bulk email operations

- **In-App Notifications**

  - User notification management
  - Notification status tracking (read/unread)
  - Notification history and persistence
  - User-specific notification retrieval

- **Event-Driven Messaging**

  - Kafka integration for real-time events
  - User event processing (registration, updates)
  - Recipe event notifications (new recipes, likes)
  - Challenge event alerts (new challenges, participation)

- **Notification Management**
  - Create, read, update, delete notifications
  - Mark notifications as read
  - Filter notifications by user
  - Notification lifecycle management

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Java Version**: 21
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Email**: Spring Boot Mail Starter
- **Message Broker**: Apache Kafka
- **Documentation**: Swagger/OpenAPI 3
- **Containerization**: Docker
- **Orchestration**: Kubernetes

## Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Mail Starter
- Spring Kafka
- PostgreSQL Driver
- Common module (shared events and DTOs)
- Swagger for API documentation

## API Endpoints

### Email Operations

- `POST /api/v1/notifications/sendMail` - Send email notification
  - Parameters: `to`, `subject`, `body`

### Notification Management

- `GET /api/v1/notifications/user/{userEmail}` - Get notifications for user
- `PATCH /api/v1/notifications/{id}/read` - Mark notification as read
- `DELETE /api/v1/notifications/{id}` - Delete notification

## Event Processing

The service listens to Kafka events from various microservices:

### User Events

- **User Registration**: Welcome email and notification
- **Profile Updates**: Confirmation notifications
- **Account Changes**: Security and update alerts

### Recipe Events

- **New Recipe Created**: Notification to followers
- **Recipe Liked**: Notification to recipe author
- **Recipe Comments**: Comment notifications

### Challenge Events

- **New Challenge**: Challenge announcement notifications
- **Challenge Participation**: Participation confirmations
- **Challenge Results**: Winner announcements and results

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/cooknect/notification_service/
│           ├── NotificationServiceApplication.java
│           ├── Config/            # Configuration classes
│           ├── Controller/        # REST controllers
│           ├── Repository/        # Data repositories
│           ├── model/            # Entity classes
│           └── service/          # Business logic
└── test/                        # Test classes
```

## Data Models

### Notification Entity

- **Notification ID**: Unique identifier
- **User Email**: Target user email
- **Message**: Notification content
- **Type**: Notification category/type
- **Status**: Read/unread status
- **Timestamp**: Creation and update timestamps
- **Metadata**: Additional notification data

## Kafka Integration

### Event Listeners

The service includes Kafka listeners for:

```java
@KafkaListener(topics = "user-events")
public void handleUserEvent(UserEvent event)

@KafkaListener(topics = "recipe-events")
public void handleRecipeEvent(RecipeEvent event)

@KafkaListener(topics = "challenge-events")
public void handleChallengeEvent(ChallengeEvent event)
```

### Event Processing Flow

1. **Event Reception**: Kafka listener receives event
2. **Event Validation**: Validate event structure and content
3. **Notification Creation**: Create appropriate notification
4. **Email Sending**: Send email if required
5. **Persistence**: Save notification to database
6. **Error Handling**: Handle failures and retries

## Email Configuration

### SMTP Settings

- **Mail Server**: Configurable SMTP server
- **Authentication**: Username/password authentication
- **Security**: TLS/SSL support
- **Templates**: HTML and text email templates

### Email Features

- **Rich HTML Content**: Support for styled emails
- **Attachments**: File attachment capabilities
- **Bulk Sending**: Efficient bulk email operations
- **Delivery Tracking**: Email delivery status monitoring

## Getting Started

### Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL database
- Apache Kafka broker
- SMTP email server access

### Installation

1. Clone the repository:

```bash
git clone <repository-url>
cd backend/notification-service
```

2. Configure the following in `application.properties`:

   - Database connection settings
   - Kafka broker configuration
   - SMTP server settings
   - Email templates location

3. Build the project:

```bash
./mvnw clean install
```

4. Run the application:

```bash
./mvnw spring-boot:run
```

The service will start on the configured port (default: 8084).

## Configuration

### Key Configuration Properties

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/notification_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=notification-service

# Email Configuration
spring.mail.host=${SMTP_HOST}
spring.mail.port=${SMTP_PORT}
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Environment Variables

- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `SMTP_HOST`: SMTP server hostname
- `SMTP_PORT`: SMTP server port
- `EMAIL_USERNAME`: Email account username
- `EMAIL_PASSWORD`: Email account password

## Notification Types

### System Notifications

- Welcome messages
- System updates
- Security alerts

### User Activity Notifications

- Profile updates
- Friend requests
- Achievement notifications

### Content Notifications

- New recipe alerts
- Recipe interactions
- Challenge updates

### Marketing Notifications

- Promotional content
- Newsletter updates
- Feature announcements

## Performance Features

- **Async Processing**: Non-blocking email sending
- **Batch Operations**: Efficient bulk notifications
- **Caching**: Notification template caching
- **Rate Limiting**: Email sending rate limits
- **Queue Management**: Kafka message queue optimization

## Monitoring and Logging

- **Health Checks**: Service health monitoring
- **Email Delivery Metrics**: Track email success rates
- **Event Processing Metrics**: Kafka consumption metrics
- **Error Logging**: Comprehensive error tracking
- **Performance Monitoring**: Response time and throughput metrics

## Security

- **Input Validation**: Sanitize email content
- **Rate Limiting**: Prevent spam and abuse
- **Authentication**: Secure API endpoints
- **Data Encryption**: Encrypt sensitive notification data
- **GDPR Compliance**: Data privacy and retention policies

## API Documentation

API documentation is available through Swagger UI:

- **Swagger UI**: `http://localhost:8084/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8084/v3/api-docs`

## Testing

### Unit Tests

```bash
./mvnw test
```

### Integration Tests

```bash
./mvnw test -Dtest=*IntegrationTest
```

### Email Testing

- Use test SMTP servers for development
- Mock email services for unit testing
- Validate email content and delivery

## Error Handling

- **Retry Mechanisms**: Automatic retry for failed operations
- **Dead Letter Queues**: Handle permanently failed messages
- **Circuit Breakers**: Prevent cascade failures
- **Graceful Degradation**: Fallback notification methods

## Scalability

- **Horizontal Scaling**: Multiple service instances
- **Kafka Partitioning**: Distributed event processing
- **Database Sharding**: Scale notification storage
- **Caching**: Reduce database load

## Troubleshooting

### Common Issues

1. **Email Delivery Failures**: Check SMTP configuration
2. **Kafka Connection Issues**: Verify broker connectivity
3. **Database Connection Problems**: Validate database settings
4. **Event Processing Delays**: Monitor Kafka lag

### Monitoring Tools

- Application logs for debugging
- Kafka monitoring for event processing
- Email delivery reports
- Database performance metrics

## Contributing

1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Update documentation
5. Submit a pull request

## License

Licensed under the MIT License. See [LICENSE](LICENSE) for details.
