# Contributing to Cooknect

Thank you for your interest in contributing to **Cooknect – Recipe & Nutrition Platform**!  
We’re thrilled to have you join our open-source community and help improve this project.

Cooknect is an open-source platform that combines recipes, nutrition tracking, and personalized meal planning.  
We welcome contributions of all kinds — from bug fixes and new features to documentation and UI improvements.

---

## 🧭 Getting Started

### 1. Fork & Clone
Fork this repository on GitHub and clone it locally:
```bash
git clone https://github.com/BITSSAP2025AugAPIBP3Sections/APIBP-20242YB-Team-07.git
cd cooknect
```

### 2. Set UP Your Environment
Ensure you have:
- Java 17+
- Maven 3.9+
- PostgreSQL
- (Optional) Docker for containerized setup
Then follow the setuo instructions in [README.md](https://github.com/BITSSAP2025AugAPIBP3Sections/APIBP-20242YB-Team-07/blob/main/README.md)

### 3. Create A New Branch
Before making changes:
```bash
git checkout -b feature/your-feature-name
```
Use a clear, descriptive name like:
- **feat/recipe-service**
- **fix/recipe-search-bug**
- **docs/update-readme**

### 4. Types Of Contributions
You can contribute in several ways:
- Bug Fixes - Identify and resolve existing issues
- New Features - Add new endpoints, features, or integrations
- Tests - Write or improve unit/integration tests
- Documnetation - Enhance API docs or usage guides
- Design & UX - Improve user experience or UI consistency
- Localization - Add support for new languages or locales

### 5. Code Guidelines
- Follow clean code principles and consistent naming conventions
- Keep methods short and focused — avoid long monolithic functions
- Use DTOs, service layers, and repositories appropriately
- Maintain Swagger annotations for all public REST APIs
- Add unit and integration tests for new functionality
- Document all new or changed endpoints in Swagger

### 6. Running Tests
Before commiting your changes:
```bash
mvn test
```
Ensure:
- All existing tests pass
- Any new features are covered by tests

### 7. Commit Message Convention
We follow the Conventional Commits Format:
```php-template
<type>: <short summary>
```
Examples:
- **feat: add voting endpoint for recipe challenges**
- **fix: handle null nutrition data gracefully**
- **docs: update API setup instructions**
- **test: add unit tests for recipe service**
Common commit types:

| Type | Description |
|----------|--------------------|
| **feat** | **New Feature** |
| **fix** | **Bug Fix** |
| **docs** | **Document Changes** |
| **test** | **Adding or improving tests** |
| **refactor** | **Code restructure without changing behaviour** |
| **chore** | **Build tasks, dependencies, etc.** |

---
## Pull Request Process
1. Ensure your branch is up to date with main:
   ```bash
    git fetch origin
    git merge origin/main
    ```
2. Run all tests locally and confirm successful build.
3. Push your branch to your fork:
    ```bash
    git push origin feature/your-feature-name
    ```
4. Open a Pull Request (PR) to the main repo:
     - Describe your changes and why they’re needed.
     - Link related issues (e.g., “Fixes #42”).
     - Include screenshots or API samples if applicable.
5. Wait for review from the maintainers - be ready to make adjustments if requested.

---

## Code Of Conduct
By participating in this project, you agree to uphold our values of:
- Respectful and inclusive communication
- Collaboration and transparency
- Constructive feedback

---

## License
By contributing, you agree that your contributions will be licensed under the MIT License included in this repository.

---

## Thank You
Your contributions make Cooknect better for everyone.
Whether you’re fixing a typo or building a new feature, we’re grateful for your support!

The Cooknect Team
