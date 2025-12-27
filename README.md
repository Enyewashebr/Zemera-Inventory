# Zemera Inventory

A comprehensive inventory and sales management system for cafeterias, built with Angular frontend and Vert.x backend.

## Project Structure

```
├── backend/          # Java/Vert.x backend API
├── frontend/         # Angular frontend application
└── db/              # Database initialization scripts
```

## Prerequisites

- **Java 17+** (for backend)
- **Node.js LTS** (for frontend)
- **PostgreSQL** (database)

## Setup Instructions

### 1. Database Setup

1. Install PostgreSQL if not already installed
2. Create a database:
   ```sql
   CREATE DATABASE zemera_inventory;
   CREATE USER zemera_user WITH PASSWORD 'zemera123';
   GRANT ALL PRIVILEGES ON DATABASE zemera_inventory TO zemera_user;
   ```
3. Run the initialization script:
   ```bash
   psql -U zemera_user -d zemera_inventory -f db/init.sql
   ```

### 2. Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build and run with Maven:
   ```bash
   mvn clean install
   mvn vertx:run
   ```
   The backend will start on `http://localhost:8080`

### 3. Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm start
   ```
   The frontend will be available at `http://localhost:4200`

## Configuration

Backend configuration is in `backend/src/main/resources/application.json`:
- HTTP port: 8080
- Database connection settings

## Features

- **Dashboard**: Overview of inventory metrics and low-stock alerts
- **Products**: Manage product catalog with categories and pricing
- **Purchases**: Record incoming inventory purchases
- **Orders**: Create and manage sales orders with ticket printing
- **Inventory**: View and filter current stock levels
- **Reports**: Generate sales and profit reports
- **Settings**: System configuration

## Development

The application is fully responsive and works on desktop, tablet, and mobile devices.
