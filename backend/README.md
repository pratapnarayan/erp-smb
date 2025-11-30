ERP-SMB Backend (Spring Boot microservices)

Modules: gateway-service, auth-service, user-service, product-service, order-service, sales-service, finance-service, hrms-service, enquiry-service, common-lib.

Run: docker compose up --build in backend/. Configure .env as per .env.example.

Each service uses: Java 21, Spring Boot 3.x, Web, Data JPA, PostgreSQL, Flyway, Validation, MapStruct, Testcontainers (test scope).

PostgreSQL single DB with per-service schemas; initial migrations under db/migration.

JWT: auth-service issues tokens; gateway and services validate Authorization: Bearer <token>.
