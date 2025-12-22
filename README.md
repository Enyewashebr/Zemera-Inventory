# Zemera Inventory

## Current state
- Backend: Vert.x skeleton with health check at `/health`
- Frontend: Placeholder static page (will be replaced with Angular app)
- Infrastructure: Docker Compose with Postgres, backend, frontend

## Run locally (Docker, once Docker is installed)
```bash
docker compose up --build
```
Services:
- Backend: http://localhost:8080
- Angular frontend: http://localhost:4200
- Postgres: localhost:5432 (user/password/db: zemera/zemera/zemera_inventory)

## Run Angular frontend in dev mode (without Docker)
1. Install Node.js (LTS) from the official site.
2. In a terminal:
   ```bash
   cd frontend
   npm install
   npm start
   ```
3. Open `http://localhost:4200` in your browser.

## Next steps
1) Add Angular app (replace `frontend` static placeholder)
2) Add database schema + migrations
3) Implement product endpoints with validation and tests
4) Build UI screens module by module (products, purchases, orders, reports)
