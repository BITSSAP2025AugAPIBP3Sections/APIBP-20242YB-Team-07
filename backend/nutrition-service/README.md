# Cooknect - Nutrition Service
The Nutrition Service is a core component of the Cooknect ecosystem, responsible for nutritional analysis, food item management, meal logging, and integration with external nutrition APIs. It provides both REST and GraphQL interfaces.

---

## Features
### Nutrition Management
- Analyze nutrition data from food items or meals
- Calculate calories, macros, and micronutrients
- Fetch nutritional info via external API integrations
- Support for ingredient-level and recipe-level nutrition breakdown

### Meal Logging
- Log meals by type (breakfast, lunch, dinner, etc.)
- Track daily calorie and nutrition goals
- View historical logs and aggregated insights

### Integration & Interoperability
- GraphQL API for rich querying and mutations
- Standard REST endpoints for CRUD operations
- Swagger UI for interactive API exploration

### System-Level
- Centralised logging via Logback & MDC interceptors
- Configurable environment setup
- Spring Boot auto-configuration
- Supports Docker deployment

---

## Architecture Overview
| Component | Responsibility |
|----------|--------------------|
|**Nutrition Controller**|**REST endpoints for nutrition lookup, meal logging, and metadata.**|
|**Nutrition GraphQL Controller**|**GraphQL-based queries for food items, meals, and nutrition insights.**|
|**ExternalNutritionApiService**|**Integrates with third-party nutrition APIs for analysis.**|
|**Model Layer**|**Entities like FoodItem, NutritionLog, and MealType.**|
|**Config Layer**|**WebClient, MDC Interceptor, Swagger, and Web MVC configurations.**|

---

## Tech Stack
- Framework: Spring Boot
- Languages: Java 17+
- APIs: REST + GraphQL
- Documentation: Swagger UI
- Build Tool: Maven
- Logging: Logback, MDC, custom HTTP appenders
- Containerization: Docker (optional)

---

## API Documentation
Once the service is running locally, access API docs here:
> Swagger UI
> http://localhost:8089/swagger-ui/index.html?urls.primaryName=Nutrition%20Service

The Swagger interface allows you to:
- Explore REST endpoints
- Inspect schemas for nutrition logs, food items, etc.
- Test API calls directly in the browser

---

## Project Setup
### Prerequisites
Ensure the following are installed:
- Java 17+
- Maven 3.9+
- Docker (Optional)

#### 1. Clone the Repositiory
```bash
git clone https://github.com/BITSSAP2025AugAPIBP3Sections/APIBP-20242YB-Team-07.git
cd backend/nutrition-service
```

#### 2. Configure Application Properties
Update application.properties with env-specific values:
```properties
external.nutrition.api.key=<YOUR_API_KEY>
```

#### 3. Build & Run the Service
```bash
mvn clean install
mvn spring-boot:run
```
Or run as a standalone JAR:
```bash
java -jar target/nutrition-service.jar
```

---

## API Overview
| Type | Endpoint | Description |
|----------|---------------------|--------------------|
|**REST**|**/api/v1/nutrition**|**CRUD operations & nutrition computations**|
|**GraphQL**|**/graphql**|**Rich data queries & mutations**|
|**Swagger**|**/swagger-ui/index.html**|**REST documentation**|

---

## Contributing
We welcome contributions from developers and students!
To contribute:
- Fork the repository
- Clone your fork
- Create a feature branch
- Implement changes and test
- Commit using conventional commit messages
- Push to your fork
- Open a Pull Request
### Example Commands
```bash
git checkout -b feat/add-nutrition-logs
git commit -m "feat(nutrition-service): add nutrition logs for meal history"
git push origin feat/add-nutrition-logs
```

---

## Code Standards
- Follow clean, modular service-layer design
- Use DTOs for all API payloads
- Avoid business logic inside controllers
- Add or update Swagger annotations for new endpoints
- Write unit tests before submitting PRs
- Document new queries or mutations in GraphQL schema

---

## Commit Message Guidelines
| Type | Purpose |
|----------|--------------------|
|**feat**|**New feature**|
|**fix**|**Bug Fix**|
|**docs**|**Documentation Updates**|
|**refactor**|**Non-breaking improvements**|
|**test**|**Add/updating tests**|
|**chore**|**Maintenance**|
### Examples
- feat(nutrition-service): Integrate external nutrition API for macro analysis.
- fix(nutrition-service): correct calculation formula
- docs(nutrition-service): update GraphQL Schema documentation

---

## License
This project is licensed under the MIT License.

---

## Contact
### Cooknect Team
For Issues, Suggestions, or bug reports, please open a GitHub Issue.
