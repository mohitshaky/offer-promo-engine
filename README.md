# Offer & Promo Engine

> Production-ready Spring Boot microservice for managing promo codes, vendor/category eligibility, validation, usage analytics, and monitoring — with Kubernetes manifests and Prometheus/Grafana observability.

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Kubernetes](https://img.shields.io/badge/Kubernetes-ready-326CE5?logo=kubernetes)
![Prometheus](https://img.shields.io/badge/Prometheus-monitored-E6522C?logo=prometheus)

---

## Features

- 🏷️ **Promo Code Management** — create, update, activate/deactivate, search with filters
- 🏪 **Vendor & Category Eligibility** — restrict promo codes to specific vendors or product categories
- ✅ **Promo Validation** — real-time validation with usage limits, expiry, vendor/category checks
- 📊 **Usage Analytics** — track redemptions per vendor, category, and time period
- ⚡ **Redis Caching** — promo validation results cached (TTL configurable)
- 🔐 **OAuth2 / JWT Security** — resource server with configurable issuer URI
- 📈 **Prometheus + Grafana** — metrics endpoint exposed, full monitoring stack via Docker Compose
- ☸️ **Kubernetes Ready** — Deployment, Service, ConfigMap, Secret, Ingress manifests included
- 🗄️ **Flyway Migrations** — versioned DB schema management
- 📄 **Swagger UI** — full OpenAPI 3.0 documentation

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Database | PostgreSQL 15 + Flyway |
| Cache | Redis 7 |
| Security | Spring Security + OAuth2 Resource Server (JWT) |
| Monitoring | Prometheus + Grafana |
| Container | Docker + Docker Compose |
| Orchestration | Kubernetes (manifests in `k8s/`) |
| Build | Maven |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |

---

## Quick Start (Dev)

```bash
# 1) Start infrastructure (PostgreSQL, Redis, Prometheus, Grafana)
docker-compose up -d postgres redis prometheus grafana

# 2) Run service in dev mode (security relaxed)
cd promo-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 3) Access
# API     : http://localhost:8081
# Swagger : http://localhost:8081/swagger-ui.html
# Health  : http://localhost:8081/actuator/health
# Metrics : http://localhost:8081/actuator/prometheus
# Grafana : http://localhost:3000  (admin/admin)
```

---

## Full Stack via Docker

```bash
# Build jar
cd promo-service
mvn -DskipTests package

# Build and start all services
docker-compose up -d

# Verify
curl http://localhost:8081/actuator/health
```

---

## API Examples

### Create Promo Code (Admin)
```bash
curl -X POST http://localhost:8081/admin/promos \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SAVE20",
    "type": "PERCENTAGE",
    "discountValue": 20.0,
    "maxUsageLimit": 500,
    "expiryDate": "2025-12-31",
    "vendorIds": [1, 2],
    "categoryIds": [10]
  }'
```

### Validate Promo Code
```bash
curl -X POST http://localhost:8081/vendor/promos/validate \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SAVE20",
    "vendorId": 1,
    "categoryId": 10,
    "orderAmount": 500.00
  }'
```

### Get Promo Analytics
```bash
curl http://localhost:8081/admin/promos/SAVE20/analytics
```

---

## Project Structure

```
offer-promo-engine/
├── promo-service/                  # Spring Boot application
│   ├── src/main/java/
│   │   ├── controller/
│   │   │   ├── AdminPromoController.java   # Admin CRUD APIs
│   │   │   └── VendorPromoController.java  # Vendor validation APIs
│   │   ├── service/
│   │   │   ├── PromoCodeService.java
│   │   │   ├── PromoValidationService.java
│   │   │   └── PromoAnalyticsService.java
│   │   ├── model/                  # JPA entities
│   │   ├── repository/             # Spring Data JPA repos
│   │   ├── config/                 # Security, Cache, Async configs
│   │   └── exception/              # Global exception handler
│   ├── src/main/resources/
│   │   ├── application.yml         # All config (env var driven)
│   │   └── db/migration/           # Flyway SQL scripts
│   ├── Dockerfile
│   └── pom.xml
├── k8s/                            # Kubernetes manifests
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── ingress.yaml
│   └── prometheus.yml
├── docker-compose.yml              # Full stack (app + infra)
└── README.md
```

---

## Kubernetes Deployment

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check status
kubectl get pods -l app=promo-service
kubectl get svc promo-service
```

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | PostgreSQL host |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | promo_db | Database name |
| `DB_USERNAME` | postgres | DB username |
| `DB_PASSWORD` | admin | DB password |
| `REDIS_HOST` | localhost | Redis host |
| `REDIS_PORT` | 6379 | Redis port |
| `REDIS_PASSWORD` | *(empty)* | Redis password |
| `JWT_ISSUER_URI` | — | OAuth2 issuer URI |
| `JWT_JWK_SET_URI` | — | OAuth2 JWKS endpoint |
| `JWT_SECRET` | — | JWT signing secret |

---

## Profiles

| Profile | Description |
|---------|-------------|
| `dev` | Security relaxed, verbose SQL logging |
| `prod` | JWT auth enforced, HTTPS ready, minimal logging |
| `docker` | DB/Redis via Docker network hostnames |

---

## Author

**Mohit Shakya** — Java Backend Developer, 6+ years
🌐 [Portfolio](https://mohitshaky.github.io) · 💻 [GitHub](https://github.com/mohitshaky)
