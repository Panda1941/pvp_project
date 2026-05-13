# Accident Reporting frontend

Minimal demo frontend for the Accident Reporting subsystem. Includes simple pages for Login and Main.

## Install and run

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173`.

## Backend

This frontend expects the backend API under the `/api` path (the local Spring Boot backend runs on port `8080` by default).

## Notes

- This is a lightweight demo; replace hard-coded/demo endpoints before production.
Additional developer notes
------------------------

- Authentication: the frontend uses `AuthContext` to store the logged-in user in `localStorage`.
	- Login POST: `/api/auth/login` expects `{ email, password }` and returns `{ email, role }` on success.
	- On successful login the top navigation shows the user email and a Logout button (top-right).

- Demo credentials (created automatically by the backend data loader):
	- Email: `admin@local`
	- Password: `password`

- To start backend + frontend together, run the repository `run-all.ps1` from the workspace root (PowerShell).

- Do NOT commit any `.env` files or real credentials. Backend `.env` belongs in `operator-system/backend` and is
	listed in `.gitignore`.
