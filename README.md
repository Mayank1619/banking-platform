# Banking Platform

Full-stack digital banking project with a React/Vite frontend and a Spring Boot backend.

Start here if you are new to the project:

- [Project onboarding guide](docs/onboarding.md)
- Frontend source: `src/`
- Backend source: `backend/src/main/java/com/group1/banking/`
- Feature specs: `specs/` and `SpecFiles/`
- Spec Kit workflow assets: `.specify/`, `.github/agents/`, and `.github/prompts/`

Quick local run:

```powershell
# Backend
cd backend
.\mvnw.cmd spring-boot:run

# Frontend, in a second terminal from the repo root
npm ci
npm run dev
```

Then open:

- Frontend: http://localhost:5173
- Backend health: http://localhost:8080/actuator/health
- Backend Swagger UI: http://localhost:8080/swagger-ui/index.html
