Operator-system — Accident Reporting subsystem
=============================================

Quick start for colleagues
--------------------------

This folder contains a minimal backend (Spring Boot) and a frontend (Vite/React) for the
Accident Reporting demo service.

Prerequisites
- Java 21
- Node >= 16 (for frontend)
- PowerShell for the provided helper scripts (Windows)

Run everything
--------------

From the repository root you can run the convenience script to start both services (PowerShell):

```powershell
.\run-all.ps1
```

What the script does
- Loads `operator-system/backend/.env` (if present) and starts the Spring Boot backend using `run-with-aiven.ps1`.
- Starts the frontend dev server in `operator-system/frontend` using `npm run dev`.

If you prefer to run services individually:
- Backend: `cd operator-system/backend` then copy `.env.example` → `.env` and run `.\run-with-aiven.ps1` or set env vars and `./mvnw spring-boot:run`.
- Frontend: `cd operator-system/frontend` then `npm install` and `npm run dev`.

Security & commit hygiene
- Do NOT commit `operator-system/backend/.env` or any file containing secrets.
- Avoid committing any `build/`, `target/`, or `node_modules/` directories.
