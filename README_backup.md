# ERP-SMB Monorepo

A full-stack, enterprise-style monorepo for the ERP-SMB application. Backend is a Maven multi-module Spring Boot microservices stack (Java 21, Spring Boot 3.x, PostgreSQL, Flyway, JWT, Swagger/OpenAPI). Frontend is a React/Vite app. The project includes a Spring Cloud Config Server, a Eureka Discovery Server, and a lightweight reverse-proxy gateway with service discovery.

## Project structure

```
.
├─ backend/
│  ├─ pom.xml                      # Maven parent (dependency management, modules)
│  ├─ .env.example                 # Backend env vars (DB + JWT)
│  ├─ config-service/              # Spring Cloud Config Server
│  │  ├─ src/main/resources/application.yml
│  │  └─ src/main/resources/configs/           # placeholder (can switch to Git backend)
│  ├─ discovery-service/           # Eureka service registry
│  │  └─ src/main/resources/application.yml
│  ├─ gateway-service/             # Reverse proxy, JWT-aware; routes in resources/routes
│  │  ├─ src/main/resources/application.yml
│  │  └─ src/main/resources/routes/{auth,users,products,orders,sales,finance,hrms,enquiry}.yml
│  ├─ common-lib/                  # Shared code used by services
│  │  ├─ src/main/java/com/erp/smb/common/
│  │  │  ├─ annotations/{CreatedAt,UpdatedAt}.java
│  │  │  ├─ dto/{BaseResponse,ErrorDTO,PageRequestDTO,PageResponse}.java
│  │  │  ├─ exception/GlobalExceptionHandler.java
│  │  │  └─ security/{JwtUtils,JwtAuthFilter}.java
│  ├─ auth-service/                # Auth (signup/login/refresh) issues JWT
│  │  └─ src/main/resources/db/migration/V{1..}.sql
│  ├─ user-service/                # User profiles
│  ├─ product-service/             # Inventory items
│  ├─ order-service/               # Sales orders
│  ├─ sales-service/               # Invoices
│  ├─ finance-service/             # Transactions + KPIs endpoint
│  ├─ hrms-service/                # Employees
│  └─ enquiry-service/             # Enquiries
│
├─ frontend/
│  ├─ src/
│  │  ├─ api/
│  │  │  ├─ clients/{http.js,index.js}
│  │  │  ├─ interceptors/token.js
│  │  │  └─ config/baseUrl.js
│  │  ├─ components/               # UI components
│  │  ├─ pages/                    # Screens (Dashboard, Orders, Sales, etc.)
│  │  ├─ assets/
│  │  └─ api/client.js             # back-compat re-export of API clients
│  ├─ vite.config.ts               # Dev proxy to gateway (:8080)
│  └─ package.json
│
├─ infrastructure/
│  ├─ docker/                      # Docker notes/placeholders
│  ├─ kubernetes/                  # K8s manifests (add Deployments/Services/Ingress)
│  └─ scripts/
│     ├─ check_bom_and_escapes.sh  # CI/Local check to prevent bad file encodings
│     └─ check_bom_and_escapes.ps1
│
├─ .github/workflows/quality.yml   # CI: BOM/escape checks (Linux + Windows)
├─ docker-compose.yml              # Full stack (Postgres, Config, Eureka, Gateway, Services)
├─ .githooks/pre-commit            # Optional local hook to run quality checks
└─ README.md
```

## Key features

- Java 21, Spring Boot 3.x, Maven multi-module
- Spring Cloud Config Server (config-service) and Eureka (discovery-service)
- Gateway with service discovery (lb://service) route resolution
- Per-service database schemas with Flyway migrations
- Shared common-lib: DTOs, error handler, security (JwtUtils + JwtAuthFilter), basic annotations
- OpenAPI/Swagger in every backend service (springdoc)
- React/Vite UI, Axios API clients with token interceptor
- Docker Compose for local development
- CI quality checks for BOM/escape issues; optional pre-commit hook

## Services

- config-service (port 8888): Spring Cloud Config (native profile by default; can switch to Git backend)
- discovery-service (port 8761): Eureka registry
- gateway-service (port 8080): reverse proxy + JWT validation, route files in resources/routes
- auth-service: /api/auth (signup, login, refresh)
- user-service: /api/users
- product-service: /api/products
- order-service: /api/orders
- sales-service: /api/sales
- finance-service: /api/finance and /api/finance/kpis
- hrms-service: /api/hrms
- enquiry-service: /api/enquiry

All list endpoints support pagination via query params (page, size) returning PageResponse.

## Environment & configuration

Backend environment variables (.env.example in backend/):

```
POSTGRES_USER=erp
POSTGRES_PASSWORD=erp
POSTGRES_DB=erp
APP_JWT_SECRET=PLEASE_CHANGE_ME_32_CHARS_MIN
```

- Services read DB URL, credentials, and JWT secret from environment variables.
- For externalized configuration, set in Config Server and add to services:

```
spring:
  config:
    import: optional:configserver:http://config-service:8888
```

- Eureka client in services:

```
eureka:
  client:
    service-url:
      defaultZone: http://discovery-service:8761/eureka/
```

## Swagger / OpenAPI

Every service exposes Swagger UI when running:

- http://localhost:<service-port>/swagger-ui.html

Dependency used:

```
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.5.0</version>
</dependency>
```

## Security

- auth-service issues JWT tokens (login, refresh)
- All other services validate Authorization: Bearer <token> via shared JwtAuthFilter from common-lib
- JwtUtils in common-lib issues/validates tokens (services construct JwtUtils with secret + TTL via properties)
- Gateway permits /api/auth/** and requires auth for others

## Building & running

### 1) Docker Compose (recommended local run)

From repo root:

```
cp .env.example .env              # optional: set root envs if needed
cd backend
cp .env.example .env              # backend-level envs
cd ..
docker compose up --build
```

This will start:
- postgres (5432), pgAdmin (5050), config-service (8888), discovery-service (8761)
- gateway-service (8080) + all microservices

The Dockerfiles use a root-level Maven build stage for consistency and cache reuse:

```
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/backend/<service-name>/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
```

### 2) Build from source (without Docker)

```
cd backend
mvn clean package -DskipTests
```

Run selected service:

```
java -jar backend/<service>/target/<service>-*.jar
```

Make sure PostgreSQL is running and env variables are set.

## Frontend

Dev run:

```
cd frontend
npm install
npm run dev
```

- Vite dev server proxies /api to http://localhost:8080 (gateway)
- Axios instance in src/api/clients/http.js attaches Authorization header from localStorage

## Quality checks

- GitHub Actions: .github/workflows/quality.yml runs BOM/escape checks on Linux and Windows
- Local optional hook:

```
git config core.hooksPath .githooks
```

- Manual check scripts:
  - Linux/macOS: ./infrastructure/scripts/check_bom_and_escapes.sh
  - Windows: ./infrastructure/scripts/check_bom_and_escapes.ps1

## Notes & next steps

- Consider migrating the gateway to Spring Cloud Gateway for advanced routing/filters
- Add DTOs + MapStruct mappers per service and avoid exposing entities directly
- Add role-based authorization policies per domain
- Add Flyway V2__seed.sql to populate demo data across services
- Add Kubernetes manifests (Deployments/Services/Ingress) under infrastructure/kubernetes
- Add integration tests with Testcontainers for each service

## Troubleshooting

- If builds fail with encoding warnings, ensure file encoding is UTF-8 and run quality scripts.
- If Docker builds are slow, ensure Maven/BuildKit caches are not being invalidated by unnecessary file changes.
- If gateway cannot route, check discovery (Eureka dashboard at :8761) and confirm services are registered; route files are under gateway-service/src/main/resources/routes.

