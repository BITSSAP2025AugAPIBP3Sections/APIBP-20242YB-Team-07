# Cooknect - Recipe & Nutrition Platform

**Cooknect** is a full-featured recipe and nutrition platform that enables users to create, browse, listen to, and track recipes while managing their dietary preferences and nutrition goals.

## Features

### Recipe Creator
- Create, update, and delete recipes
- Tag recipes with dietary categories (vegan, keto, high-protein, etc.)
- Add ingredient and step-by-step recipe instructions
- Listen to recipe steps via integrated Text-to-Speech (TTS) for hands-free cooking

### Everyday User
- Discover recipes by ingredients, tags, or nutritional needs
- Save favorites and build personal recipe collections
- Enjoy hands-free cooking with **TTS-powered** audio instructions
- Track calories and nutrition goals using logged meals

### Community Challenges
- Participate in **recipe challenges** by submitting your creations
- Browse all challenge entries and **vote** on your favorites
- View challenge leaderboards and community ratings
- Encourage user engagement through gamified participation

### System-Wide
- Secure authentication and **role-based access control** (Admin, Creator, User)
- REST + GraphQL APIs for seamless integration with frontends and third-party systems
- Centralized PostgreSQL database for recipes, users, and nutrition data

---

## Architecture Overview

Cooknect follows a modular, service-oriented backend architecture:

| Service | Responsibility |
|----------|----------------|
| **Recipe Service** | Manages recipe CRUD, dietary tagging, and provides audio-based recipe instruction playback via TTS integration. |
| **User Service** | Handles registration, authentication (JWT), authorization (RBAC), and user health goals/preferences. |
| **Nutrition Service** | Performs nutritional analysis, meal logging, and generates analytical dashboards. |
| **Challenge Service** | Manages gamification: challenge creation, recipe submissions, community voting, leaderboard scoring.|
| **Notification Service** | Uses Apache Kafka for asynchronous alerts, reminders, and challenge/meal updates. |
| **Gateway Service** | Public entry point; routes requests to microservices and handles cross-cutting concerns. |

---

## Tech Stack

- **Backend Framework:** Spring Boot  
- **Database:** PostgreSQL  
- **API Layers:** REST + GraphQL  
- **Authentication:** JWT with Role-Based Access Control  
- **Documentation:** Swagger UI  
- **Containerization:** Docker (optional)

---

## API Documentation

Cooknect provides a detailed, interactive API documentation using **Swagger UI**.

Once the backend is running locally, you can explore all available REST endpoints here:

> **Swagger UI:** [http://localhost:8089/swagger-ui/index.html](http://localhost:8089/swagger-ui/index.html)

The Swagger interface allows you to:
- View and test endpoints interactively
- Inspect request/response payloads
- Understand authentication and parameter requirements

---

## Project Setup

### Prerequisites
Ensure you have the following installed:
- Java 17+
- Maven 3.9+
- PostgreSQL
- Docker (optional)

### 1. Clone the Repository
```bash
git clone https://github.com/BITSSAP2025AugAPIBP3Sections/APIBP-20242YB-Team-07.git
cd cooknect
```

### 2. Configure Environment
Edit your **application.yml** or **.env** file:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cooknect
    username: <db_user>
    password: <db_password>
```

### 3. Build And Run
```bash
mvn clean install
mvn spring-boot:run
```

---

## API Endpoints Overview
| Type | Endpoint | Description |
|----------|--------------------|--------------------|
| **REST** | **/api/v1/...** | **Standard CRUD and integrations** |
| **GraphQL** | **/graphql** | **Query and mutate resources** |
| **Swagger** | **/swagger-ui/index.html** | **Interactive API documentation** |

---

## Running Tests
Run all tests locally:
```bash
mvn test
```

---

## Contributing
We welcome community contributions!
See [COTRIBUTING.md](https://github.com/BITSSAP2025AugAPIBP3Sections/APIBP-20242YB-Team-07/blob/main/CONTRIBUTING.md) for setup, coding, and pull request guidelines.

---

## License
Licensed under the MIT License
See [LICENSE](https://github.com/BITSSAP2025AugAPIBP3Sections/APIBP-20242YB-Team-07/blob/main/LICENSE) for details

---

## Contact
Cooknect Team
For issues or suggestion, open a GitHUB issue.
```markdown
Thank you for your interest in contributing to Cooknect!
This project thrives on community-driven innovation and open collaboration.
```
How to contribute
1. Fork this repository.
2. Clone your fork:
```bash
git clone https://github.com/BITSSAP2025AugAPIBP3Sections/APIBP-20242YB-Team-07.git
cd cooknect
```
3. Create a feature branch
```bash
git checkout -b feature/your-feature-name
```
4. Implement your changes.
5. Test thoroughly before committing.
6. Commit with a clear message:
```bash
git commit -m "feat: add challenge voting endpoint"
```
7. Push to your fork:
```bash
git push origin feature/your-feature-name
```
8. Open a **Pull Request (PR)**:
- Explain the motivation and changes.
- Link related issues (e.g., “Fixes #12”).
- Attach screenshots or API examples if relevant.

---

## Code Standards
- Use clean, modular code with clear naming conventions.
- Follow Spring Boot best practices for controllers, services, and repositories.
- Always include or update Swagger annotations for new endpoints.
- Write unit tests and integration tests for new features.
- Document new services or API routes in the [README.md](https://github.com/BITSSAP2025AugAPIBP3Sections/APIBP-20242YB-Team-07/blob/main/README.md) or [Swagger](http://localhost:8089/swagger-ui/index.html).

---

## Testing
Befor Submitting:
```bash
mvn test
```
Ensure all tests pass and the application runs locally with:
```bash
mvn spring-boot:run
```

---

## Commit Message Convention
Follow Conventional Commits:
| Type | Endpoint |
|----------|--------------------|
| **feat** | **New feature** |
| **fix** | **Bug fix** |
| **refactor** | **Code restructuring without behaviour change** |
| **docs** | **Documentation update** |
| **test** | **Adding or updating tests** |
| **chore** | **Maintenance or dependency updates** |

Examples:
- **feat: add voting endpoint for challenges**
- **fix: resolve null pointer in nutrition service**
- **docs: update API usage instructions**
- **refactor: optimize recipe search logic**

---

## Communication & Conduct
- Be respectful, constructive, and inclusive.
- Discuss large changes in an issue before starting.
- Keep discussions professional and solution-focused.
