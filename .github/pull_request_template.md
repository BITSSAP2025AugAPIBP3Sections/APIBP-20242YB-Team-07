## 📝 Description

Please include a clear and concise summary of the change.  
Explain **what** your PR does and **why** it’s needed.


---

## 🧩 Related Issue
Fixes: #<issue_number>  
(If applicable — please link to the related GitHub issue.)

---

## 🚀 Type of Change

Select all that apply:

- [ ] 🐛 Bug Fix  
- [ ] ✨ New Feature  
- [ ] ♻️ Refactor (no functional changes, only code improvements)  
- [ ] 🧪 Tests Added / Updated  
- [ ] 📝 Documentation Update  
- [ ] ⚙️ DevOps / CI-CD / Config Change  

---

## ✅ Checklist

Before submitting your PR, please verify all of the following:

### 🔐 Authentication & Authorization
- [ ] JWT validation logic tested successfully  
- [ ] Role-based access verified for protected routes  
- [ ] No sensitive information (e.g., keys, secrets) is exposed in the code  

### 🧱 Microservice Integration
- [ ] Routes added/updated in **gateway-service** and tested  
- [ ] Internal service communication tested (e.g., user ↔ recipe)  
- [ ] No hardcoded service URLs; use environment variables  

### 🐳 Docker & Deployment
- [ ] Dockerfile builds successfully  
- [ ] Container runs locally without errors  
- [ ] Service health endpoint verified (e.g., `/actuator/health`)  

### 🧪 Code & Testing
- [ ] Code follows project conventions and naming style  
- [ ] Linting and formatting checks pass  
- [ ] Unit/integration tests added or updated  
- [ ] Existing tests pass (`mvn test` / `npm test` / relevant command)  

### 🧾 Documentation
- [ ] README or API documentation updated (if required)  
- [ ] Added example usage for any new endpoint  

---

## 📸 Screenshots / Logs (If Applicable)
If your change affects UI or API output, include screenshots or curl/postman logs here.

---

## 🧠 Affected Modules / Services
Select all that apply:

- [ ] gateway-service 
- [ ] user-service 
- [ ] recipe-service  
- [ ] nutrition-service  
- [ ] meal-planner-service  
- [ ] audio-service
- [ ] frontend  

---

## 💬 Additional Context
Add any other information that reviewers should know about your PR (limitations, next steps, dependencies, etc.)

---

### ❤️ Thank You!
Thank you for contributing to this project! Please ensure your PR title follows the conventional commit style:
> Example:  
> - `feat(gateway): add JWT middleware`  
> - `fix(recipe): resolve null pointer in recipe validation`
> - `docs: update API usage in README`
> - `refactor(nutrition): simplify calculation logic`
> - `chore: update dependencies`
> - `test(user): add unit tests for login API`
> - `ci: update GitHub Actions workflow`
