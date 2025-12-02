# Cooknect - Recipe Service
The Recipe Service is a core component of the Cooknect ecosystem, responsible for managing recipes, ingredients, preparation steps, comments, likes, saved recipes, and audio generation for step-by-step cooking instructions. It exposes both REST and GraphQL APIs and integrates with external services for speech synthesis and event handling.

---

## Features
### Recipe Management
- Create, update, and delete recipes
- Manage ingredients, preparation steps, tags, and cuisine types
- Retrieve recipe details with full ingredient and step breakdown
- GraphQL-based rich query support
- Pagination, filtering, and searching of recipes

### User Interactions
- Comment on recipes
- Like recipes
- Save/bookmark recipes
- Retrieve user-specific lists (saved recipes, liked recipes, etc.)

### Audio Generation
- Convert preparation steps into audio instructions
- Supports WAV/PCM output
- Uses TTS integration via SpeechSynthService

### Integartion & Interoperability
- GraphQL API for advanced querying
- Standard REST endpoints for CRUD operations
- gRPC with Protobuf definitions (recipe.proto)
- Event publishing via RecipeEventProducer (e.g., Kafka)

### System-Level
- Centralized exception handling
- Configurable environment variables
- Spring Boot auto-configuration
- Supports Docker deployment

---

## Architecture Overview
| Component | Responsibility |
|-----------|----------------------|
|**Recipe Controller**|**REST endpoints for recipe CRUD operations, comments, likes, and saved recipes**|
|**Recipe GraphQL Controller**|**GraphQL queries and mutations for complex recipe queries**|
|**SpeechSynthService**|**Converts recipe steps into synthesized audio**|
|**RecipeEventProducer**|**Sends recipe-related events to external services**|
|**Model Layer**|**Entities including Recipe, Ingredient, Cuisine, Comment, Like, SavedRecipe, PreparationStep**|
|**DTO Layer**|**Payload validation and transfer objects**|
|**Exception Layer**|**Custom exceptions such as NotFoundException and ForbiddenException**|

---

## Tech Stack
- Framework: Spring Boot
- Languages: Java 17+
- APIs: REST + GraphQL + gRPC
- Build Tool: Maven
- Containerization: Docker
- Serialization: Protobuf (gRPC)

---

## gRPC Definition
The service includes a Protobuf-based contract:
```css
src/main/proto/recipe.proto
```
This defines recipe requests, responses, and service-level RPC methods.

---

## API Documentation
Once running locally:
> REST Endpoints (Recipe Service)
> http://localhost:8082/api/v1/recipes

> GraphQL Pkayground
> http://localhost:8082/graphql

> Swagger
> http://localhost:8082/swagger-ui/index.html

---

## Project Setup
### Prerequisites
Ensure the following are installed:
- Java 17+
- Maven 3.9+
- Docker (Optional)

#### 1. Clone the repository
```bash
git clone https://github.com/BITSSAP2025AugAPIBP3Sections/APIBP-20242YB-Team-07.git
cd backend/recipe-service
```

#### 2. Configure Application Properties
```properties
GL_API_KEY=<YOUR-GL_API_KEY>
```

#### 3. Build & Run the Service
Run Locally:
```bash
mvn clean install
mvn spring-boot:run
```
Or run the built JAR:
```bash
java -jar target/recipe-service-0.0.1-SNAPSHOT.jar
```

---

## API Overview
| Type | Endpoint | Description |
|-----------|--------------------|-------------------|
|**REST**|**/api/v1/recipes**|**CRUD operations, comments, likes, saves**|
|**GraphQL**|**/graphql**|**Rich querying of recipes, comments, etc.**|
|**gRPC**|**N/A (Service interface defined in recipe.proto)**|**Remote recipe operations**|

---

## Contributing
Contributions are welcome!

### Steps
- Fork the repository
- Clone your fork
- Create a feature branch
- Implement changes + add tests
- Commit using conventional commit messages
- Push and open a Pull Request
#### Example Commands
```bash
git checkout -b feat/add-audio-generation
git commit -m "feat(recipe-service): implement audio synthesis for preparation steps"
git push origin feat/add-audio-generation
```

### Code Standards
- Maintain clear separation between controller, service, and repository layers
- Use DTOs for request/response models
- Avoid business logic in controllers
- Add tests for new features
- Document new GraphQL queries or mutations
- Ensure Protobuf definitions remain backward-compatible

---

## Commit Message Guidelines
| Type | Purpose |
|----------|--------------------|
|**feat**|**New feature**|
|**fix**|**Bug fix**|
|**docs**|**Documentation changes**|
|**refactor**|**Code restructuring without behavior change**|
|**test**|**Updates to tests**|
### Examples:
- feat(recipe-service): add support for recipe audio playback
- fix(recipe-service): correct ingredient parsing logic
- docs(recipe-service): update GraphQL schema

---

## License
This project is licensed under the MIT License.

---

## Contact
### Cooknect Team
For issues or suggestions, please open a GitHub Issue.
