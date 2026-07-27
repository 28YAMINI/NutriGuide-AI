# 10 — DEPLOYMENT GUIDE

| **Field**             | **Value**                                    |
| --------------------- | -------------------------------------------- |
| **Project Name**      | NutriGuide AI                                |
| **Document Title**    | Deployment & Operations Guide                |
| **Version**           | 1.0                                          |
| **Author**            | Software Architecture Team                   |
| **Created On**        | 2026-07-27                                   |
| **Last Updated**      | 2026-07-27                                   |
| **Status**            | Draft                                        |
| **References**        | `01_PROJECT_CONTEXT.md`, `03_SYSTEM_ARCHITECTURE.md`, `07_TASKS.md`, `08_CODING_STANDARDS.md`, `09_TESTING.md` |

---

## Table of Contents

1. [Docker](#1-docker)
2. [Docker Compose](#2-docker-compose)
3. [Environment Variables](#3-environment-variables)
4. [Azure Deployment](#4-azure-deployment)
5. [Backend Deployment](#5-backend-deployment)
6. [Frontend Deployment](#6-frontend-deployment)
7. [Database Deployment](#7-database-deployment)
8. [CI/CD Pipeline](#8-cicd-pipeline)
9. [Health Checks](#9-health-checks)
10. [Monitoring & Logging](#10-monitoring--logging)
11. [Backup Strategy](#11-backup-strategy)

---

## 1. Docker

### 1.1 Backend Dockerfile

Create `backend/Dockerfile`:

```dockerfile
# ─────────────────────────────────────────────────────────
# STAGE 1: Build with Maven
# ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Download dependencies (layer caching)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -B

# ─────────────────────────────────────────────────────────
# STAGE 2: Runtime with JRE
# ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose Spring Boot default port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Entry point
ENTRYPOINT ["java", \
    "-jar", "app.jar", \
    "--spring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}"]
```

### 1.2 Frontend Dockerfile

Create `frontend/Dockerfile`:

```dockerfile
# ─────────────────────────────────────────────────────────
# STAGE 1: Build React app
# ─────────────────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files
COPY package.json package-lock.json ./

# Install dependencies
RUN npm ci --only=production

# Copy source and build
COPY . .
RUN npm run build

# ─────────────────────────────────────────────────────────
# STAGE 2: Serve with Nginx
# ─────────────────────────────────────────────────────────
FROM nginx:1.25-alpine AS runtime

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config
COPY docker/nginx/default.conf /etc/nginx/conf.d/

# Copy built static files
COPY --from=builder /app/dist /usr/share/nginx/html

# Create non-root user and set permissions
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:80/health || exit 1

# Run as non-root user
USER nginx

CMD ["nginx", "-g", "daemon off;"]
```

### 1.3 Nginx Configuration

Create `docker/nginx/default.conf`:

```nginx
upstream backend {
    server backend:8080;
    keepalive 64;
}

server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript image/svg+xml;
    gzip_min_length 1000;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self' https://api.nutriguideai.com;" always;

    # Frontend static files
    location / {
        try_files $uri $uri/ /index.html;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # API proxy to backend
    location /api/ {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Authorization $http_authorization;
        proxy_pass_header Authorization;
        proxy_read_timeout 60s;
        proxy_buffering off;
    }

    # Health check endpoint (served by nginx, not proxied)
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    # Deny access to hidden files
    location ~ /\. {
        deny all;
        access_log off;
        log_not_found off;
    }

    # Deny access to sensitive files
    location ~ (\.env|composer\.json|package\.json|yarn\.lock)$ {
        deny all;
    }
}
```

### 1.4 .dockerignore

Create `backend/.dockerignore`:

```
**/.classpath
**/.dockerignore
**/.env
**/.git
**/.gitignore
**/.idea
**/.project
**/.settings
**/.vscode
**/node_modules
**/target
**/*.md
**/logs
**/test
```

Create `frontend/.dockerignore`:

```
**/.dockerignore
**/.env.local
**/.env.development
**/.git
**/.gitignore
**/.idea
**/.vscode
**/node_modules
**/src
**/*.md
**/tests
**/coverage
```

---

## 2. Docker Compose

### 2.1 Local Development

Create `docker/docker-compose.yml`:

```yaml
version: '3.8'

name: nutriguideai

services:
  # ── Database ──
  mysql:
    image: mysql:8.0
    container_name: nutriguideai-mysql
    restart: unless-stopped
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-rootpassword}
      MYSQL_DATABASE: nutriguideai
      MYSQL_USER: nutriguideai_user
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-userpassword}
    volumes:
      - mysql_data:/var/lib/mysql
      - ./mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - nutriguideai-network

  # ── Redis Cache (optional) ──
  redis:
    image: redis:7-alpine
    container_name: nutriguideai-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    command: redis-server --appendonly no --maxmemory 128mb --maxmemory-policy allkeys-lru
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5
    profiles:
      - with-cache
    networks:
      - nutriguideai-network

  # ── Backend API ──
  backend:
    build:
      context: ../backend
      dockerfile: Dockerfile
    container_name: nutriguideai-backend
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/nutriguideai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: nutriguideai_user
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD:-userpassword}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      JWT_SECRET: ${JWT_SECRET:-dev-jwt-secret-key-at-least-256-bits-long}
    depends_on:
      mysql:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 60s
    networks:
      - nutriguideai-network

  # ── Frontend ──
  frontend:
    build:
      context: ../frontend
      dockerfile: Dockerfile
    container_name: nutriguideai-frontend
    restart: unless-stopped
    ports:
      - "80:80"
    depends_on:
      backend:
        condition: service_healthy
    networks:
      - nutriguideai-network

volumes:
  mysql_data:
    driver: local

networks:
  nutriguideai-network:
    driver: bridge
```

### 2.2 Docker Compose Usage

```bash
# Start full stack (without Redis)
docker compose -f docker/docker-compose.yml up -d

# Start with Redis caching
docker compose -f docker/docker-compose.yml --profile with-cache up -d

# View logs
docker compose -f docker/docker-compose.yml logs -f backend

# Stop all services
docker compose -f docker/docker-compose.yml down

# Stop and remove volumes (destroys data)
docker compose -f docker/docker-compose.yml down -v

# Rebuild specific service
docker compose -f docker/docker-compose.yml build backend

# Scale backend (for load testing)
docker compose -f docker/docker-compose.yml up -d --scale backend=3

# Check service health
docker compose -f docker/docker-compose.yml ps
```

### 2.3 MySQL Init Script

Create `docker/mysql/init.sql`:

```sql
-- Run on first container start to initialize the database
CREATE DATABASE IF NOT EXISTS nutriguideai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Application user
CREATE USER IF NOT EXISTS 'nutriguideai_user'@'%' IDENTIFIED BY 'userpassword';
GRANT SELECT, INSERT, UPDATE, DELETE ON nutriguideai.* TO 'nutriguideai_user'@'%';
FLUSH PRIVILEGES;

-- Note: Schema migrations are managed by Flyway
-- Tables are created automatically on Spring Boot startup
```

---

## 3. Environment Variables

### 3.1 Complete Variable Reference

| **Variable**                            | **Required** | **Default (Dev)**                    | **Description**                              |
| --------------------------------------- | ----------- | ------------------------------------ | -------------------------------------------- |
| **Spring Boot**                         |             |                                      |                                              |
| `SPRING_PROFILES_ACTIVE`                | ✅          | `dev`                                | Active Spring profile                        |
| `SERVER_PORT`                           | ❌          | `8080`                               | HTTP server port                             |
| **Database**                            |             |                                      |                                              |
| `SPRING_DATASOURCE_URL`                 | ✅          | `jdbc:mysql://localhost:3306/nutriguideai` | MySQL JDBC connection URL            |
| `SPRING_DATASOURCE_USERNAME`            | ✅          | `nutriguideai_user`                  | Database username                            |
| `SPRING_DATASOURCE_PASSWORD`            | ✅          | —                                    | Database password                            |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME`   | ❌          | `com.mysql.cj.jdbc.Driver`           | JDBC driver class                            |
| **JWT**                                 |             |                                      |                                              |
| `JWT_SECRET`                            | ✅          | —                                    | HS256 signing key (≥ 256 bits, base64)       |
| `JWT_EXPIRATION_MS`                     | ❌          | `86400000` (24 hours)                | Token expiry in milliseconds                 |
| **Redis (Optional)**                    |             |                                      |                                              |
| `SPRING_REDIS_HOST`                     | ❌          | —                                    | Redis server hostname                        |
| `SPRING_REDIS_PORT`                     | ❌          | `6379`                               | Redis server port                            |
| `SPRING_REDIS_PASSWORD`                 | ❌          | —                                    | Redis password (if configured)               |
| **Flyway**                              |             |                                      |                                              |
| `SPRING_FLYWAY_ENABLED`                 | ❌          | `true`                               | Enable/disable Flyway migrations             |
| `SPRING_FLYWAY_LOCATIONS`               | ❌          | `classpath:db/migration`             | Migration script location                    |
| **Logging**                             |             |                                      |                                              |
| `LOGGING_LEVEL_COM_NUTRIGUIDEAI`        | ❌          | `DEBUG` (dev), `INFO` (prod)         | Application log level                        |
| `LOGGING_FILE_PATH`                     | ❌          | —                                    | Path to log file (empty = console only)      |
| **Azure (Production)**                  |             |                                      |                                              |
| `AZURE_APP_SERVICE_NAME`                | ✅ (prod)   | —                                    | Azure App Service instance name              |
| `AZURE_MYSQL_HOST`                      | ✅ (prod)   | —                                    | Azure MySQL server hostname                  |
| `AZURE_MYSQL_SSL_ENABLED`               | ❌          | `true`                               | Require SSL for MySQL connection             |
| **CORS**                                |             |                                      |                                              |
| `CORS_ALLOWED_ORIGINS`                  | ❌          | `http://localhost:5173`              | Comma-separated allowed origins              |

### 3.2 Environment Variable Files

#### `.env.example` (project root)

```bash
# ── Required ──
JWT_SECRET=your-256-bit-base64-encoded-secret-key-here
SPRING_DATASOURCE_PASSWORD=your-database-password-here

# ── Database ──
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/nutriguideai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=nutriguideai_user

# ── Redis (optional) ──
# SPRING_REDIS_HOST=localhost
# SPRING_REDIS_PORT=6379

# ── Profile ──
SPRING_PROFILES_ACTIVE=dev
```

#### `backend/src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: nutriguideai

  # ── Datasource ──
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/nutriguideai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
    username: ${SPRING_DATASOURCE_USERNAME:nutriguideai_user}
    password: ${SPRING_DATASOURCE_PASSWORD:devpassword}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      idle-timeout: 30000
      connection-timeout: 20000
      max-lifetime: 1800000

  # ── JPA / Hibernate ──
  jpa:
    hibernate:
      ddl-auto: validate  # Never 'create' or 'update' in production
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
        default_batch_fetch_size: 20

  # ── Flyway ──
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  # ── Redis (optional) ──
  redis:
    host: ${SPRING_REDIS_HOST:}
    port: ${SPRING_REDIS_PORT:6379}
    password: ${SPRING_REDIS_PASSWORD:}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 4
        min-idle: 1

  # ── Jackson ──
  jackson:
    serialization:
      write-dates-as-timestamps: false
    date-format: yyyy-MM-dd'T'HH:mm:ss'Z'
    time-zone: UTC

# ── JWT ──
jwt:
  secret: ${JWT_SECRET:dev-jwt-secret-key-at-least-256-bits-long}
  expiration-ms: ${JWT_EXPIRATION_MS:86400000}

# ── Logging ──
logging:
  level:
    root: WARN
    com.nutriguideai: ${LOGGING_LEVEL_COM_NUTRIGUIDEAI:DEBUG}
    org.springframework.web: INFO
    org.hibernate: WARN
  pattern:
    console: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"

# ── Actuator ──
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env,loggers
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  health:
    db:
      enabled: true
    redis:
      enabled: true

# ── Server ──
server:
  error:
    include-stacktrace: never
    include-message: always

---

# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/nutriguideai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
  jpa:
    show-sql: true
  flyway:
    enabled: true

logging:
  level:
    com.nutriguideai: DEBUG

jwt:
  secret: dev-jwt-secret-key-at-least-256-bits-long

---

# application-prod.yml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    hikari:
      maximum-pool-size: 20
  jpa:
    show-sql: false
  flyway:
    enabled: true

logging:
  level:
    com.nutriguideai: INFO

server:
  error:
    include-stacktrace: never
```

---

## 4. Azure Deployment

### 4.1 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         AZURE CLOUD                              │
│                                                                  │
│   ┌─────────────────────────┐     ┌───────────────────────────┐ │
│   │   Azure App Service     │     │   Azure Static Web Apps    │ │
│   │   (Backend - Spring Boot)│     │   (Frontend - React)       │ │
│   │                        │     │                           │ │
│   │   nutriguideai-api     │     │   nutriguideai-web         │ │
│   │   ┌─────────────────┐  │     │                           │ │
│   │   │  Java 21 + JAR  │  │     │   CDN + SSL + Custom      │ │
│   │   └─────────────────┘  │     │   Domain + Auth            │ │
│   └───────────┬─────────────┘     └─────────────┬─────────────┘ │
│               │                                  │              │
│               ▼                                  ▼              │
│   ┌─────────────────────────┐     ┌───────────────────────────┐ │
│   │ Azure Database for MySQL│     │   Azure Cache for Redis   │ │
│   │   (Flexible Server)     │     │   (Optional - Production) │ │
│   │                        │     │                           │ │
│   │   nutriguideai-mysql   │     │   nutriguideai-cache      │ │
│   │   Tier: B1ms (1 vCore) │     │   Tier: C0 (250 MB)       │ │
│   │   Storage: 20 GB       │     │                           │ │
│   └─────────────────────────┘     └───────────────────────────┘ │
│                                                                  │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │              Azure Monitor & Application Insights       │   │
│   │              (Logging, Metrics, Alerts)                 │   │
│   └─────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Azure Prerequisites

| **Resource**                       | **SKU**                    | **Estimated Monthly Cost** |
| ---------------------------------- | -------------------------- | -------------------------- |
| Azure App Service (Backend)        | B1 (1 vCPU, 1.75 GB RAM)   | ~$15/month                 |
| Azure Static Web Apps (Frontend)   | Free tier                  | $0                         |
| Azure Database for MySQL           | B1ms (1 vCore, 2 GB RAM)   | ~$15/month                 |
| Azure Cache for Redis (Optional)   | C0 (250 MB)                | ~$15/month                 |
| Azure Application Insights         | Pay-as-you-go              | ~$5/month                  |
| **Total (without Redis)**          |                            | **~$35/month**             |
| **Total (with Redis)**             |                            | **~$50/month**             |

### 4.3 Azure CLI Setup Script

```bash
#!/bin/bash
# 01-azure-setup.sh — Run once to create Azure resources

# Configuration
RESOURCE_GROUP="nutriguideai-rg"
LOCATION="eastus"
BACKEND_APP_NAME="nutriguideai-api"
FRONTEND_APP_NAME="nutriguideai-web"
MYSQL_SERVER_NAME="nutriguideai-mysql"
MYSQL_DB_NAME="nutriguideai"
REDIS_NAME="nutriguideai-cache"

# Login
az login

# Create resource group
az group create \
    --name $RESOURCE_GROUP \
    --location $LOCATION

# ── 1. MySQL Database ──
az mysql flexible-server create \
    --resource-group $RESOURCE_GROUP \
    --name $MYSQL_SERVER_NAME \
    --database-name $MYSQL_DB_NAME \
    --sku-name Standard_B1ms \
    --tier Burstable \
    --storage-size 20 \
    --admin-user nutriguideai_admin \
    --admin-password "YourStrongPassword!2026" \
    --public-access 0.0.0.0 \
    --yes

# Allow Azure services to access MySQL
az mysql flexible-server firewall-rule create \
    --resource-group $RESOURCE_GROUP \
    --name $MYSQL_SERVER_NAME \
    --rule-name AllowAzureServices \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 0.0.0.0

# ── 2. Redis Cache (Optional) ──
# az redis create \
#     --resource-group $RESOURCE_GROUP \
#     --name $REDIS_NAME \
#     --sku Basic \
#     --vm-size c0

# ── 3. Backend App Service ──
az appservice plan create \
    --resource-group $RESOURCE_GROUP \
    --name "nutriguideai-plan" \
    --sku B1 \
    --is-linux

az webapp create \
    --resource-group $RESOURCE_GROUP \
    --plan "nutriguideai-plan" \
    --name $BACKEND_APP_NAME \
    --runtime "JAVA:21-java21" \
    --startup-file "java -jar app.jar --spring.profiles.active=prod"

# ── 4. Frontend Static Web Apps ──
# Note: Frontend is deployed separately via CI/CD or GitHub Actions

echo "Azure setup complete!"
echo "Backend URL: https://$BACKEND_APP_NAME.azurewebsites.net"
```

### 4.4 Azure App Service Configuration

```bash
#!/bin/bash
# 02-configure-appservice.sh — Configure backend app settings

BACKEND_APP_NAME="nutriguideai-api"
RESOURCE_GROUP="nutriguideai-rg"

# Set environment variables
az webapp config appsettings set \
    --resource-group $RESOURCE_GROUP \
    --name $BACKEND_APP_NAME \
    --settings \
        SPRING_PROFILES_ACTIVE=prod \
        SPRING_DATASOURCE_URL="jdbc:mysql://nutriguideai-mysql.mysql.database.azure.com:3306/nutriguideai?useSSL=true&requireSSL=true&serverTimezone=UTC" \
        SPRING_DATASOURCE_USERNAME="nutriguideai_admin" \
        SPRING_DATASOURCE_PASSWORD="YourStrongPassword!2026" \
        JWT_SECRET="production-256-bit-base64-secret-key" \
        JWT_EXPIRATION_MS=86400000 \
        CORS_ALLOWED_ORIGINS="https://nutriguideai-web.azurewebsites.net" \
        LOGGING_LEVEL_COM_NUTRIGUIDEAI=INFO \
        SPRING_FLYWAY_ENABLED=true \
        WEBSITES_PORT=8080

# Configure CORS for the frontend
az webapp cors add \
    --resource-group $RESOURCE_GROUP \
    --name $BACKEND_APP_NAME \
    --allowed-origins "https://nutriguideai-web.azurewebsites.net" "http://localhost:5173"

# Enable HTTPS only
az webapp update \
    --resource-group $RESOURCE_GROUP \
    --name $BACKEND_APP_NAME \
    --https-only true
```

---

## 5. Backend Deployment

### 5.1 Build and Deploy

#### Option A: GitHub Actions (Recommended)

Deployment is automated via CI/CD (see Section 8). On every push to `main`, the pipeline:

1. Builds the JAR with Maven
2. Runs unit tests
3. Builds Docker image
4. Pushes to Azure Container Registry (optional)
5. Deploys to Azure App Service

#### Option B: Manual Deployment

```bash
# 1. Build the JAR
cd backend
./mvnw clean package -DskipTests

# 2. Deploy via ZIP deploy (no Docker required)
az webapp deploy \
    --resource-group nutriguideai-rg \
    --name nutriguideai-api \
    --src-path target/nutriguideai-1.0.0.jar \
    --type jar

# 3. Verify deployment
az webapp log tail \
    --resource-group nutriguideai-rg \
    --name nutriguideai-api

# 4. Test health endpoint
curl https://nutriguideai-api.azurewebsites.net/actuator/health
```

#### Option C: Docker Deployment

```bash
# 1. Build Docker image
docker build -t nutriguideai-backend:latest ./backend

# 2. Tag for Azure Container Registry (if using ACR)
docker tag nutriguideai-backend:latest nutriguideaiacr.azurecr.io/backend:latest

# 3. Push to registry
docker push nutriguideaiacr.azurecr.io/backend:latest

# 4. Deploy to App Service (configured for ACR)
az webapp config container set \
    --resource-group nutriguideai-rg \
    --name nutriguideai-api \
    --docker-registry-server-url https://nutriguideaiacr.azurecr.io \
    --docker-custom-image-name nutriguideaiacr.azurecr.io/backend:latest \
    --docker-registry-server-user <acr-username> \
    --docker-registry-server-password <acr-password>
```

### 5.2 Backend Production Checklist

```markdown
## Backend Production Checklist
- [ ] application-prod.yml configured with production values
- [ ] JWT_SECRET is a strong, unique base64-encoded key (≥ 256 bits)
- [ ] SPRING_DATASOURCE_PASSWORD is strong and stored in Azure Key Vault or App Settings
- [ ] spring.jpa.hibernate.ddl-auto = validate (never create/update)
- [ ] Flyway migrations are up to date and tested
- [ ] HikariCP pool size tuned for expected load (start: max=20)
- [ ] CORS origins restricted to frontend domain(s) only
- [ ] HTTPS enforced (Azure App Service setting)
- [ ] Stack traces disabled in error responses
- [ ] Actuator endpoints secured (health, info, metrics)
- [ ] Log level set to INFO for production
- [ ] Redis caching configured (if available)
```

---

## 6. Frontend Deployment

### 6.1 Build and Deploy

#### Option A: Azure Static Web Apps (Recommended)

```bash
# 1. Build the React app
cd frontend
npm ci
npm run build

# 2. Deploy via GitHub Actions (automatic with Static Web Apps)
# The workflow file is generated by Azure when connecting the repo.
# See Section 8 for the CI/CD pipeline.
```

#### Option B: Deploy to Azure Storage Static Website

```bash
# 1. Build
cd frontend
npm ci
npm run build

# 2. Create storage account (one-time)
az storage account create \
    --resource-group nutriguideai-rg \
    --name nutriguideaifrontend \
    --kind StorageV2 \
    --location eastus

# 3. Enable static website
az storage blob service-properties update \
    --account-name nutriguideaifrontend \
    --static-website \
    --index-document index.html \
    --404-document index.html

# 4. Upload build
az storage blob upload-batch \
    --account-name nutriguideaifrontend \
    --source ./dist \
    --destination \$web \
    --overwrite

# 5. Get public URL
az storage account show \
    --name nutriguideaifrontend \
    --resource-group nutriguideai-rg \
    --query "primaryEndpoints.web" \
    --output tsv
```

#### Option C: Deploy to Vercel (Alternative)

```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
vercel --prod

# Configure environment variables in Vercel dashboard:
# VITE_API_BASE_URL = https://nutriguideai-api.azurewebsites.net/api/v1
```

### 6.2 Frontend Production Checklist

```markdown
## Frontend Production Checklist
- [ ] VITE_API_BASE_URL points to production backend URL
- [ ] All environment variables set in hosting platform
- [ ] Build optimized (minified, tree-shaken)
- [ ] Source maps disabled
- [ ] Custom domain configured
- [ ] SSL/HTTPS enforced
- [ ] CDN caching configured (immutable file hashing)
- [ ] 404 fallback to index.html for SPA routing
- [ ] Content Security Policy headers configured
- [ ] Lighthouse audit passed (Performance ≥ 90, Accessibility ≥ 90)
- [ ] Error tracking configured (Sentry or equivalent)
```

### 6.3 Frontend Environment

Create `frontend/.env.production`:

```env
VITE_API_BASE_URL=https://nutriguideai-api.azurewebsites.net/api/v1
VITE_APP_NAME=NutriGuide AI
VITE_APP_VERSION=1.0.0
VITE_SENTRY_DSN=  # Optional: Sentry error tracking
VITE_GA_ID=        # Optional: Google Analytics
```

---

## 7. Database Deployment

### 7.1 Azure MySQL Flexible Server

```bash
# ── Connect to MySQL from local machine ──
mysql -h nutriguideai-mysql.mysql.database.azure.com \
    -u nutriguideai_admin \
    -pYourStrongPassword!2026 \
    --ssl-ca=DigiCertGlobalRootCA.crt.pem

# ── Verify SSL connection ──
mysql> SHOW STATUS LIKE 'Ssl_cipher';

# ── Create application database (if not created already) ──
mysql> CREATE DATABASE IF NOT EXISTS nutriguideai
       CHARACTER SET utf8mb4
       COLLATE utf8mb4_unicode_ci;

# ── Verify Flyway migrations (from backend application logs) ──
# Spring Boot will automatically run Flyway migrations on startup.
# Check: SELECT * FROM flyway_schema_history;
```

### 7.2 MySQL Connection String Formats

| **Environment** | **Connection String**                                                        |
| --------------- | ---------------------------------------------------------------------------- |
| Local (Docker)  | `jdbc:mysql://localhost:3306/nutriguideai?useSSL=false&allowPublicKeyRetrieval=true` |
| Azure           | `jdbc:mysql://nutriguideai-mysql.mysql.database.azure.com:3306/nutriguideai?useSSL=true&requireSSL=true&serverTimezone=UTC` |

### 7.3 Production Database Settings

```sql
-- Recommended MySQL production settings (server parameters)

-- Connection pool
SET GLOBAL max_connections = 100;

-- Timeout values
SET GLOBAL wait_timeout = 300;
SET GLOBAL interactive_timeout = 300;

-- Query cache (MySQL 8.0 removed query cache; use application-level caching instead)

-- InnoDB settings
SET GLOBAL innodb_buffer_pool_size = 512M;     -- 50-70% of available RAM
SET GLOBAL innodb_log_file_size = 128M;
SET GLOBAL innodb_flush_log_at_trx_commit = 2;  -- Better performance (slightly less durability)
SET GLOBAL innodb_lock_wait_timeout = 50;

-- Character set
SET GLOBAL character_set_server = utf8mb4;
SET GLOBAL collation_server = utf8mb4_unicode_ci;

-- Enable slow query log for monitoring
SET GLOBAL slow_query_log = ON;
SET GLOBAL long_query_time = 2;
```

### 7.4 Flyway Migration Management

```bash
# Migration files: backend/src/main/resources/db/migration/
# Naming convention: V{version}__{description}.sql
#
# Examples:
#   V1__create_users_table.sql
#   V2__create_profiles_table.sql
#   V3__create_health_vitals_table.sql
#   V4__seed_food_catalog.sql

# ── Check migration status (from application logs) ──
# On startup, look for:
# INFO o.f.c.i.d.ConnectExecutor  - Successfully applied N migrations

# ── Query migration history ──
mysql> SELECT version, description, installed_on, success
       FROM nutriguideai.flyway_schema_history
       ORDER BY installed_on DESC;

# ── Manual repair (if migration fails) ──
# Run on the server:
curl -X POST https://nutriguideai-api.azurewebsites.net/actuator/flyway
# Or via Maven:
cd backend && ./mvnw flyway:repair
```

---

## 8. CI/CD Pipeline

### 8.1 GitHub Actions Workflow

Create `.github/workflows/ci-cd.yml`:

```yaml
name: NutriGuide AI CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

env:
  JAVA_VERSION: '21'
  NODE_VERSION: '20'
  AZURE_WEBAPP_NAME: nutriguideai-api
  AZURE_WEBAPP_PACKAGE_PATH: './backend/target/*.jar'
  MYSQL_DB_URL: jdbc:mysql://localhost:3306/nutriguideai_test
  MYSQL_DB_USER: root
  MYSQL_DB_PASSWORD: root

jobs:
  # ── Job 1: Test & Build Backend ──
  backend:
    name: Backend - Build & Test
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: nutriguideai_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd "mysqladmin ping -h localhost"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Build and Test Backend
        run: |
          cd backend
          ./mvnw clean verify -B
        env:
          SPRING_DATASOURCE_URL: ${{ env.MYSQL_DB_URL }}
          SPRING_DATASOURCE_USERNAME: ${{ env.MYSQL_DB_USER }}
          SPRING_DATASOURCE_PASSWORD: ${{ env.MYSQL_DB_PASSWORD }}
          JWT_SECRET: ci-test-secret-key-for-unit-tests-only

      - name: Upload Backend Artifact
        uses: actions/upload-artifact@v4
        with:
          name: backend-jar
          path: backend/target/*.jar
          retention-days: 1

  # ── Job 2: Test & Build Frontend ──
  frontend:
    name: Frontend - Build & Lint
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up Node.js ${{ env.NODE_VERSION }}
        uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        run: cd frontend && npm ci

      - name: Lint
        run: cd frontend && npm run lint

      - name: Build
        run: cd frontend && npm run build

      - name: Upload Frontend Artifact
        uses: actions/upload-artifact@v4
        with:
          name: frontend-build
          path: frontend/dist
          retention-days: 1

  # ── Job 3: Security Scan ──
  security:
    name: Security Scan
    runs-on: ubuntu-latest
    needs: [backend]

    steps:
      - uses: actions/checkout@v4

      - name: OWASP Dependency Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          project: 'NutriGuide AI'
          path: './backend'
          format: 'HTML'
          out: 'reports'

      - name: Upload Security Report
        uses: actions/upload-artifact@v4
        with:
          name: security-report
          path: reports
          retention-days: 30

  # ── Job 4: Deploy (main branch only) ──
  deploy:
    name: Deploy to Azure
    runs-on: ubuntu-latest
    needs: [backend, frontend, security]
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'

    steps:
      - uses: actions/checkout@v4

      - name: Download Backend Artifact
        uses: actions/download-artifact@v4
        with:
          name: backend-jar
          path: ./backend/target

      - name: Deploy Backend to Azure App Service
        uses: azure/webapps-deploy@v3
        with:
          app-name: ${{ env.AZURE_WEBAPP_NAME }}
          publish-profile: ${{ secrets.AZURE_WEBAPP_PUBLISH_PROFILE }}
          package: ${{ env.AZURE_WEBAPP_PACKAGE_PATH }}

      - name: Deploy Frontend to Azure Static Web Apps
        uses: Azure/static-web-apps-deploy@v1
        with:
          azure_static_web_apps_api_token: ${{ secrets.AZURE_STATIC_WEB_APPS_API_TOKEN }}
          repo_token: ${{ secrets.GITHUB_TOKEN }}
          action: upload
          app_location: 'frontend'
          output_location: 'dist'

      - name: Run Smoke Tests
        run: |
          echo "Waiting for deployment to stabilize..."
          sleep 30
          curl -sSf https://nutriguideai-api.azurewebsites.net/actuator/health
          curl -sSf -o /dev/null -w "%{http_code}" https://nutriguideai-web.azurewebsites.net/
```

### 8.2 Branch Strategy Triggers

| **Branch**   | **Event**              | **Actions**                                          |
| ------------ | ---------------------- | ---------------------------------------------------- |
| `feature/*`  | Push, PR → develop     | Build + Test (backend + frontend)                    |
| `develop`    | Push                   | Build + Test + Security Scan                         |
| `main`       | Push (after PR merge)  | Build + Test + Security Scan + Deploy to Azure       |
| `release/*`  | Push                   | Build + Test + Full Regression + Deploy to Staging   |
| `hotfix/*`   | Push, PR → main        | Build + Test + Security Scan + Deploy to Production  |

### 8.3 Secrets Required in GitHub

| **Secret Name**                       | **Description**                          |
| ------------------------------------- | ---------------------------------------- |
| `AZURE_WEBAPP_PUBLISH_PROFILE`        | Azure App Service publish profile        |
| `AZURE_STATIC_WEB_APPS_API_TOKEN`     | Azure Static Web Apps deployment token   |
| `JWT_SECRET`                          | Production JWT signing secret            |
| `SPRING_DATASOURCE_PASSWORD`          | Production database password             |

---

## 9. Health Checks

### 9.1 Actuator Endpoints

| **Endpoint**                    | **Method** | **Purpose**                        | **Secured** |
| ------------------------------- | ---------- | ---------------------------------- | ----------- |
| `/actuator/health`              | GET        | Overall system health              | No          |
| `/actuator/health/liveness`     | GET        | Liveness probe (Kubernetes)        | No          |
| `/actuator/health/readiness`    | GET        | Readiness probe (Kubernetes)       | No          |
| `/actuator/info`                | GET        | Application metadata               | No          |
| `/actuator/metrics`             | GET        | JVM, CPU, memory, HTTP metrics     | Yes         |
| `/actuator/env`                 | GET        | Environment properties             | Yes         |
| `/actuator/loggers`             | GET/POST   | View/change log levels at runtime  | Yes         |
| `/actuator/flyway`              | GET        | Flyway migration status            | Yes         |

### 9.2 Health Check Response

```json
// GET /actuator/health
{
    "status": "UP",
    "components": {
        "db": {
            "status": "UP",
            "details": {
                "database": "MySQL",
                "validationQuery": "isValid()"
            }
        },
        "redis": {
            "status": "UP",
            "details": {
                "version": "7.0.12"
            }
        },
        "diskSpace": {
            "status": "UP",
            "details": {
                "total": 107374182400,
                "free": 53687091200,
                "threshold": 10485760
            }
        },
        "ping": {
            "status": "UP"
        }
    }
}
```

### 9.3 Health Check Configurations

#### Azure App Service Health Check

Configure in Azure Portal → App Service → Health Check:

```
Health Check Path: /actuator/health
Health Check Interval: 1 minute
Unhealthy Instance Count: 1
```

#### Docker HEALTHCHECK

Both `Dockerfile` files include `HEALTHCHECK` instructions (see Sections 1.1 and 1.2).

#### Load Balancer Health Probe

For Azure App Service, the platform automatically pings the health check path and removes unhealthy instances from rotation.

---

## 10. Monitoring & Logging

### 10.1 Monitoring Stack

| **Component**               | **Tool**                     | **Purpose**                            |
| --------------------------- | ---------------------------- | -------------------------------------- |
| Application Logs            | SLF4J + Logback              | Structured JSON logs                   |
| Log Aggregation             | Azure Application Insights   | Centralized log search and analysis    |
| Metrics & Alerts            | Azure Monitor                | CPU, memory, HTTP errors, custom metrics |
| Performance Monitoring      | Spring Actuator + Micrometer | Request latency, DB query times        |
| Uptime Monitoring           | Azure Monitor + Pingdom      | External health check every 5 minutes  |
| Error Tracking              | Sentry (Optional)            | Frontend and backend error aggregation |
| Database Monitoring         | Azure MySQL Insights         | Slow queries, connections, storage     |

### 10.2 Key Metrics to Monitor

| **Metric**                          | **Source**              | **Warning**       | **Critical**       |
| ----------------------------------- | ----------------------- | ----------------- | ------------------ |
| CPU Percentage                      | App Service             | > 70%             | > 90%              |
| Memory Percentage                   | App Service             | > 75%             | > 90%              |
| HTTP 5xx Errors (per minute)        | App Service             | > 5               | > 20               |
| Average Response Time (p95)         | Application Insights    | > 500ms           | > 1s               |
| Active Database Connections         | MySQL                   | > 50              | > 80               |
| Database Storage Usage              | MySQL                   | > 70%             | > 90%              |
| Meal Plan Generation Time           | Custom metric           | > 3s              | > 5s               |
| JVM Heap Usage                      | Actuator                | > 70%             | > 90%              |

### 10.3 Log Aggregation Configuration

```xml
<!-- logback-spring.xml — Production JSON logging -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>userId</includeMdcKeyName>
            <includeMdcKeyName>requestId</includeMdcKeyName>
            <fieldNames>
                <timestamp>timestamp</timestamp>
                <level>level</level>
                <logger>logger</logger>
                <message>message</message>
            </fieldNames>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/nutriguideai/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/nutriguideai/application-%d{yyyy-MM-dd}.log.gz</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{ISO8601} [%thread] %-5level %logger{36} [%X{userId}] - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### 10.4 Alert Rules

| **Alert Name**                   | **Condition**                                   | **Action**                      |
| -------------------------------- | ----------------------------------------------- | ------------------------------- |
| High CPU Usage                   | CPU > 85% for 5 minutes                         | Email team, auto-scale          |
| High Error Rate                  | HTTP 5xx > 1% of requests for 10 minutes         | Email team, check logs          |
| Slow API Response                | p95 latency > 1s for 5 minutes                   | Email team, investigate query   |
| Database Connection Spike        | Active connections > 80% for 5 minutes           | Email team, check pool settings |
| Meal Plan Generation Failure     | Exception rate > 5% for 5 minutes                | Email team, check AI service    |
| Application Down                 | Health check fails for 3 consecutive attempts    | Email + SMS team, auto-restart  |
| SSL Certificate Expiry           | Certificate expires in < 30 days                 | Email ops team, renew cert      |

### 10.5 Application Insights Setup

```xml
<!-- backend/pom.xml — Add dependency -->
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>applicationinsights-spring-boot-starter</artifactId>
    <version>3.4.19</version>
</dependency>
```

```yaml
# application-prod.yml — App Insights config
azure:
  application-insights:
    connection-string: ${AZURE_APPINSIGHTS_CONNECTION_STRING}
    web:
      enabled: true
    enable-grpc-interceptor: false
    enable-http-client-interceptor: false
```

---

## 11. Backup Strategy

### 11.1 Database Backup

#### Automated Backups (Azure MySQL Flexible Server)

Azure MySQL Flexible Server provides automated backups by default:

| **Setting**               | **Value**                    |
| ------------------------- | ---------------------------- |
| Backup retention          | 7 days                       |
| Backup type               | Full (daily) + Transaction log (every 5 min) |
| Point-in-time restore     | Yes (any point in last 7 days) |
| Geo-redundant backup      | Optional (additional cost)   |

#### Manual Backup Commands

```bash
# ── Export full database ──
mysqldump -h nutriguideai-mysql.mysql.database.azure.com \
    -u nutriguideai_admin \
    -p \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    nutriguideai > nutriguideai-backup-$(date +%Y%m%d_%H%M%S).sql

# ── Compress backup ──
gzip nutriguideai-backup-*.sql

# ── Restore from backup ──
mysql -h nutriguideai-mysql.mysql.database.azure.com \
    -u nutriguideai_admin \
    -p \
    nutriguideai < nutriguideai-backup-20260727_120000.sql

# ── Azure CLI automated backup download ──
az mysql flexible-server backup list \
    --resource-group nutriguideai-rg \
    --name nutriguideai-mysql

az mysql flexible-server restore \
    --resource-group nutriguideai-rg \
    --name nutriguideai-mysql-restored \
    --source-server nutriguideai-mysql \
    --restore-point-in-time "2026-07-27T12:00:00Z"
```

### 11.2 Backup Schedule

| **Backup Type**          | **Frequency**     | **Retention**     | **Storage**            |
| ------------------------ | ----------------- | ----------------- | ---------------------- |
| Automated DB backup      | Daily (full) + continuous (log) | 7 days | Azure-managed           |
| Manual DB dump           | Weekly (Sunday)   | 30 days           | Azure Blob Storage     |
| Application config       | On change         | Indefinite        | GitHub (versioned)     |
| Docker images            | Per release       | 10 latest         | Azure Container Registry |
| Uploaded files (v2)      | Daily             | 30 days           | Azure Blob Storage     |

### 11.3 Backup Automation Script

Create `scripts/backup.sh`:

```bash
#!/bin/bash
# Database backup script — run via cron or Azure Automation

set -e

# Configuration
BACKUP_DIR="/backups/nutriguideai"
DB_HOST="${DB_HOST:-nutriguideai-mysql.mysql.database.azure.com}"
DB_USER="${DB_USER:-nutriguideai_admin}"
DB_NAME="${DB_NAME:-nutriguideai}"
STORAGE_CONTAINER="${STORAGE_CONTAINER:-nutriguideai-backups}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${TIMESTAMP}.sql.gz"

# Create backup directory
mkdir -p $BACKUP_DIR

# Take backup
echo "Starting backup: ${DB_NAME} at ${TIMESTAMP}"
mysqldump \
    -h $DB_HOST \
    -u $DB_USER \
    -p${DB_PASSWORD} \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    $DB_NAME | gzip > $BACKUP_FILE

# Verify backup
if [ -f "$BACKUP_FILE" ]; then
    BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo "Backup completed: ${BACKUP_FILE} (${BACKUP_SIZE})"
else
    echo "ERROR: Backup failed!"
    exit 1
fi

# Upload to Azure Blob Storage (if configured)
if [ -n "$AZURE_STORAGE_CONNECTION_STRING" ]; then
    az storage blob upload \
        --connection-string "$AZURE_STORAGE_CONNECTION_STRING" \
        --container-name "$STORAGE_CONTAINER" \
        --file "$BACKUP_FILE" \
        --name "nutriguideai_${TIMESTAMP}.sql.gz"
    echo "Backup uploaded to Azure Blob Storage"
fi

# Cleanup old backups (keep last 30 days)
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

echo "Backup process complete."
```

### 11.4 Disaster Recovery

| **Scenario**                  | **RTO** | **RPO** | **Recovery Steps**                                    |
| ----------------------------- | ------- | ------- | ----------------------------------------------------- |
| Application crash (backend)   | 5 min   | 0       | Azure App Service auto-restart; or manual restart     |
| Application crash (frontend)  | 1 min   | 0       | Azure Static Web Apps auto-recovery                   |
| Database corruption           | 1 hour  | 5 min   | Point-in-time restore to 5 minutes before corruption  |
| Full region outage            | 4 hours | 1 hour  | Deploy to secondary region, restore latest backup     |
| Accidental data deletion      | 2 hours | 1 day   | Restore from daily backup, replay transaction logs    |
| Security breach               | 2 hours | 1 hour  | Restore from pre-breach backup, patch vulnerability   |

### 11.5 Backup Verification

```bash
# Monthly backup restore test
./scripts/backup-verify.sh

# This script:
# 1. Restores the latest backup to a test database
# 2. Runs a set of integrity queries
# 3. Verifies row counts match expected ranges
# 4. Deletes the test database
# 5. Emails the report to the team
```

---

## Document Version History

| **Version** | **Date**     | **Author** | **Changes**          |
| ----------- | ------------ | ---------- | -------------------- |
| 1.0         | 2026-07-27   | Architect  | Initial draft        |

---

*End of Document — 10_DEPLOYMENT.md*
