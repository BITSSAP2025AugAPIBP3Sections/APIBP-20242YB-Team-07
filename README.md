# Cooknect - Recipe & Nutrition Platform

**Cooknect** is a full-featured recipe and nutrition platform that enables users to create, browse, listen to, and track recipes while managing their dietary preferences and nutrition goals.

---

## Features

### Recipe Creator
- Create, Update and Delete Recipes.
- Tag recipes with categories (vegan, keto, high-protein, etc.)
- Enrich recipes with nutritional data and serving details

### Everyday User
- Discover recipes by ingredients
- Save favorites and build personal recipe collections
- Listen to recipes hands-free with **Text-to-Speech (TTS)**
- Track calories and nutrition goals via logged meals

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
| **Recipe Service** | CRUD operations for recipes, categorization, and external recipe fetch (Spoonacular/Edamam). |
| **User Service** | Handles registration, authentication, dietary preferences, and health goals. |
| **Audio Service** | Converts recipe steps into speech for hands-free cooking. |
| **Nutrition Service** | Analyzes calories and macros, tracks meal logs, and generates nutrition dashboards. |
| **Challenge Service** | Hosts recipe challenges where users submit recipes, vote, and view results. |
| **Notification Service** | Sends alerts, reminders, and updates (e.g., new challenges, meal reminders). |
| **Meal Planner Service** | Generates personalized weekly meal plans based on user preferences and nutrition goals. |

---

## Tech Stack

- **Backend Framework:** Spring Boot  
- **Database:** PostgreSQL  
- **API Layers:** REST + GraphQL  
- **Authentication:** JWT with Role-Based Access Control  
- **External APIs:** Spoonacular / Edamam  
- **Documentation:** Swagger UI  
- **Containerization:** Docker (optional)

---

## 📖 API Documentation

Cooknect provides a detailed, interactive API documentation using **Swagger UI**.

Once the backend is running locally, you can explore all available REST endpoints here:

> 🌐 **Swagger UI:** [http://localhost:8089/swagger-ui/index.html](http://localhost:8089/swagger-ui/index.html)

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
