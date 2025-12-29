# Zemera Inventory

## Current state
- Backend: Vert.x skeleton with health check at `/health`
- Frontend: Placeholder static page (will be replaced with Angular app)
- Infrastructure: Docker Compose with Postgres, backend, frontend

## Run locally (Docker)
```
docker compose up --build
```
Services:
- Backend: http://localhost:8080
- Frontend placeholder: http://localhost:4200
- Postgres: localhost:5432 (user/password/db: zemera/zemera/zemera_inventory)

## Next steps
1) Add Angular app (replace `frontend` static placeholder)
2) Add database schema + migrations
3) Implement product endpoints with validation and tests
4) Build UI screens module by module (products, purchases, orders, reports)
