Accident Reporting backend — developer guide
==========================================

Overview
--------
This is a small Spring Boot backend for the Accident Reporting subsystem. It expects a MySQL
database (we use Aiven in development). Database connection settings are supplied via environment
variables (or via the local `.env` file when using `run-with-aiven.ps1`).

Prerequisites
-------------
- Java 21 SDK installed and on `PATH`
- (Windows) PowerShell available
- Maven wrapper is included (`mvnw` / `mvnw.cmd`) — no global Maven required

Important files
---------------
- `.env.example` — copy to `.env` and fill in your credentials. **Do NOT commit `.env`**.
- `run-with-aiven.ps1` — helper that loads `.env` into environment and runs the app.
- `run-all.ps1` (repo root) — starts backend and frontend together and will use `run-with-aiven.ps1` for the backend.

Environment variables
---------------------
- `JDBC_DATABASE_URL` — full JDBC URL. Example for Aiven MySQL (with CA verification):
  `jdbc:mysql://HOST:PORT/DBNAME?sslMode=VERIFY_CA&requireSSL=true&verifyServerCertificate=true`
- `JDBC_DATABASE_USERNAME` — database username
- `JDBC_DATABASE_PASSWORD` — database password
- `JPA_DDL_AUTO` — optional Hibernate schema action (`update`, `validate`, `none`, etc.). Defaults to `update`.

Running locally
---------------
Recommended (Aiven / .env):

1. Copy `.env.example` to `.env` and fill values (do not commit `.env`).
2. From `operator-system/backend` run:

```powershell
.\run-with-aiven.ps1
```

Or run directly with environment variables (PowerShell example):

```powershell
$env:JDBC_DATABASE_URL = 'jdbc:mysql://localhost:3306/accident_reporting'
$env:JDBC_DATABASE_USERNAME = 'root'
$env:JDBC_DATABASE_PASSWORD = 'root'
.\mvnw spring-boot:run
```

Seeding / test credentials
--------------------------
- On startup the application runs a small data loader that will insert a demo admin user if missing:
  - Email: `admin@local`
  - Password: `password`

Security notes
--------------
- The demo loader stores plaintext passwords for convenience only. In production, **always** store
  hashed passwords (bcrypt/argon2) and use secure authentication flows.
- The local `.env` contains real credentials. Ensure `.env` is not tracked by git (this repo's
  `backend/.gitignore` contains `.env`). If you accidentally committed secrets, rotate them immediately
  and remove the file from the repo history.

Commit safety check
-------------------
Files you should NOT commit or should confirm are ignored before pushing:

- `backend/.env` (contains DB password)
- `backend/target/` (build artifacts)
- `frontend/node_modules/` and any frontend build output
- `frontend-mobile/app/build/` and other IDE/build caches

Quick commands to verify and unstage secrets (run from repo root):

```powershell
git status --porcelain
git ls-files --error-unmatch operator-system/backend/.env  # returns non-zero if not tracked
# If .env is tracked accidentally:
git rm --cached operator-system/backend/.env
git commit -m "Remove accidental secret file"
```
