# OkazX

E-commerce backend monolith built with Spring Boot 4.0.5. Handles user auth, product catalog, cart management, and order processing. Uses JWT for stateless sessions, PostgreSQL for persistence, and Docker for containerization. Built for production with proper security, testing, and observability.

[![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=java)](https://openjdk.java.net/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-green?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Supported-2496ED?style=flat-square&logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-blueviolet?style=flat-square)](LICENSE)

---

## Quick Setup

**Requirements:** JDK 25, Maven 3.8+, PostgreSQL 15 (or Docker Compose)

```bash
git clone https://github.com/abdulrahman-09/okazx-monolithic-ecommerce && cd okazx

# Create .env (example)
cat > .env << EOF
DB_URL=jdbc:postgresql://localhost:5433/okazx
DB_USER=postgres
DB_PASS=your_password
JWT_SECRET_KEY=your-32-char-minimum-secret-key
JWT_EXPIRATION_TIME=86400000
admin.email=admin@okazx.com
admin.password=AdminPass123!
EOF

# Run with Docker (recommended)
docker-compose up -d --build

# Or run locally (recommended: use the included Maven wrapper)
./mvnw spring-boot:run
# On Windows:
mvnw.cmd spring-boot:run
# Or with system Maven:
mvn spring-boot:run
```

Notes:
- The docker-compose file defines an internal `db` service. The app container connects to it using `jdbc:postgresql://db:5432/okazx` (this value is set explicitly in docker-compose and overrides `.env`'s DB_URL for the app service).
- The Postgres container maps its container port 5432 to host port 5433 (`ports: "5433:5432"`). If you need to connect to Postgres from your host machine, use `jdbc:postgresql://localhost:5433/okazx`. If you run Postgres locally on the default 5432 port, set `DB_URL` accordingly.
- docker-compose's `depends_on` ensures the DB container starts but does not wait for Postgres to be ready; consider using a startup wait-for script or a container healthcheck if you see connection errors during app startup.
- Admin user creation: If `admin.email`/`admin.password` are set in `.env`, an admin user is seeded on first successful application startup.

---

## Architecture Overview

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Controllers   │───▶│   Services       │───▶│  Repositories   │
│                 │    │                  │    │                 │
│ - REST APIs     │    │ - Business Logic │    │ - Data Access   │
│ - Validation    │    │ - Transactions   │    │ - Queries       │
│ - DTO Mapping   │    │ - Security       │    │ - Hibernate     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   DTOs          │    │   Entities       │    │   PostgreSQL    │
│                 │    │                  │    │                 │
│ - Request/Resp  │    │ - JPA Mappings   │    │ - ACID          │
│ - Validation    │    │ - Relationships  │    │ - Indexing      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

JWT Flow:

Client → POST /api/v1/auth/login → Authenticate credentials → Return JWT (HS256-signed, contains subject and roles, expires).

On requests: JwtAuthenticationFilter extracts the Bearer token from the Authorization header, verifies the signature and expiration, extracts the subject (username/email), loads UserDetails, confirms the token subject matches the loaded user, and sets the SecurityContext with the user's authorities. Failures return 401 with appropriate messages (e.g., token expired, invalid token, user not found).

---

## Tech Stack

| Component | Choice | Why                                               |
|-----------|--------|---------------------------------------------------|
| **Framework** | [Spring Boot 4.0.5](https://spring.io/projects/spring-boot) | Latest stable with improved startup times         |
| **Auth** | [JJWT 0.13.0](https://github.com/jwtk/jjwt), BCrypt | Stateless JWT with HS256; secure password hashing |
| **Database** | [PostgreSQL 15](https://www.postgresql.org/), Hibernate | ACID compliance; auto-schema generation           |
| **Mapping** | [MapStruct 1.6.1](https://mapstruct.org/) | Compile-time DTO mapping, no runtime overhead     |
| **API Docs** | [SpringDoc OpenAPI 3.0.2](https://springdoc.org/) | Auto-generates specs from annotations             |
| **Testing** | JUnit 5, Mockito | Unit tests for service layer methods              |
| **Container** | Docker, Eclipse Temurin JDK 25 | Multi-stage builds for smaller images             |

---

## Key Technical Implementations

### JWT Authentication

Custom `JwtAuthenticationFilter` handles token extraction and validation. Filter runs before Spring Security checks, extracting Bearer tokens from headers and validating HS256 signatures.

```java
// SecurityConfig.java - filter chain
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

Tokens embed the username (JWT subject) and roles. Expiration configurable via `JWT_EXPIRATION_TIME` (default 24h). Passwords are hashed with BCrypt (strength 10).

### Spring Security & RBAC

Method-level authorization with `@PreAuthorize`. Two roles: CUSTOMER, ADMIN. Enforced at both URL patterns and individual endpoints.

```java
// Pattern-based (SecurityConfig)
.requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("ADMIN")

// Method-based (Controllers)
@PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #id == authentication.principal.id)")
```

CSRF disabled for REST API. Custom handlers return 401/403 for auth failures.

### OpenAPI & Swagger

`OpenApiConfig` bean sets up JWT security scheme. Swagger UI auto-generates models from DTOs with Jakarta validation annotations.

```java
// OpenApiConfig.java
.addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
.components(new Components()
  .addSecuritySchemes("Bearer Auth",
    new SecurityScheme().type(SecurityScheme.Type.HTTP)
      .scheme("bearer").bearerFormat("JWT")))
```

Endpoints tagged by functionality. Full spec available at `/v3/api-docs`.

### JPA Entity Relationships

Repository-based data access with Hibernate. Entities use `@OneToMany`, `@ManyToOne` with cascade deletes where logical.

```java
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "address_id")
private Address address;
```

Auto-schema update on startup. Timestamps via `@CreationTimestamp`, `@UpdateTimestamp`.

### MapStruct DTO Mapping

Compile-time generated mappers eliminate boilerplate. Configured in Maven compiler plugin to avoid Lombok conflicts.

```
src/main/java/mapper/
└── (Generated interfaces)
```

### Actuator Observability

Endpoints exposed: `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/env`, `/actuator/loggers`. Health public; metrics require ADMIN.

```properties
management.endpoints.web.exposure.include=health,info,metrics,env,loggers
management.endpoint.health.show-details=never
```

Used for monitoring in production deployments.

### Input Validation

Jakarta Validation on DTOs with `@Valid`, `@NotNull`, `@Min`, `@Max`. Centralized in request/response contracts. Violations return 400 with details.

### Pagination

List endpoints support pagination via query parameters:

- page (integer, default 0): zero-based page index.
- size (integer, default 20): number of items per page. The server may enforce a maximum (commonly 100).
- sort (string, optional): one or more `field,direction` rules (e.g. `sort=price,desc`). Repeat `sort` to apply multiple criteria: `?sort=price,desc&sort=name,asc`.

Requests validate `page >= 0` and `size > 0`; invalid values return 400 Bad Request.

Responses use a common PageResponse<T> JSON wrapper with these fields:

{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 123,
  "totalPages": 7,
  "sort": "name,asc"
}

This pagination pattern is used across product and order listing endpoints.

### Admin Seeder

`ApplicationRunner` checks for existing admin on startup. Creates one if missing using `.env` credentials.

```java
if (userRepository.findByEmail(adminEmail).isEmpty()) {
  User admin = User.builder()
    .email(adminEmail)
    .password(passwordEncoder.encode(adminPassword))
    .userRole(UserRole.ADMIN)
    .build();
  userRepository.save(admin);
}
```

---

## Project Structure

```
src/main/java/com/am9/okazx/
├── config/
│   ├── SecurityConfig.java          # JWT filter, RBAC rules
│   ├── AdminSeeder.java             # Initial admin creation
│   └── OpenApiConfig.java           # Swagger setup
├── controller/                      # 5 REST controllers
├── service/impl/                    # Business logic implementations
├── repository/                      # Spring Data JPA repos
├── model/
│   ├── entity/                      # 6 JPA entities
│   └── enums/                       # UserRole, OrderStatus
├── dto/
│   ├── request/                     # Input DTOs with validation
│   └── response/                    # Paginated responses
├── mapper/                          # MapStruct interfaces
├── security/
│   ├── filter/JwtAuthenticationFilter.java
│   └── dto/                         # Auth-related DTOs
└── exception/                       # Custom exception handlers

resources/
└── application.properties           # Config properties

test/java/com/am9/okazx/
├── OkazxApplicationTests.java       # Context loading
└── service/impl/                    # 5 service unit tests
```

---

## API Endpoints

Base: `http://localhost:8080/api/v1`

| Method | Path | Role | Purpose |
|--------|------|------|---------|
| POST | `/auth/register` | Public | Customer signup |
| POST | `/auth/admin/register` | ADMIN | Admin user creation |
| POST | `/auth/login` | Public | JWT token generation |
| GET | `/products` | Public | Paginated product list |
| GET | `/products/{id}` | Public | Get product by ID |
| GET | `/products/search` | Public | Search products by keyword |
| POST | `/products` | ADMIN | Create product |
| PUT | `/products/{id}` | ADMIN | Update product |
| DELETE | `/products/{id}` | ADMIN | Delete product |
| GET | `/cart` | CUSTOMER | View cart |
| POST | `/cart` | CUSTOMER | Add to cart |
| DELETE | `/cart` | CUSTOMER | Remove from cart |
| POST | `/orders` | CUSTOMER | Create order from cart |
| GET | `/orders` | ADMIN | All orders (paginated) |
| GET | `/orders/my` | CUSTOMER | User's orders |
| GET | `/orders/{id}` | ADMIN/CUSTOMER | Get order by ID |
| PATCH | `/orders/{id}/status` | ADMIN | Update order status |
| PATCH | `/orders/{id}/cancel` | ADMIN/CUSTOMER | Cancel order |
| GET | `/users` | ADMIN | List users (paginated) |
| GET | `/users/{id}` | ADMIN/CUSTOMER | Get user by ID |
| PUT | `/users/{id}` | CUSTOMER | Update user profile |
| DELETE | `/users/{id}` | ADMIN/CUSTOMER | Delete user |

Full docs: `http://localhost:8080/swagger-ui.html` (or `http://localhost:8080/swagger-ui/index.html`). OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Database Schema

6 entities with Hibernate auto-generation:

- **users** (id, email, firstName, lastName, phone, password, userRole, address_id, timestamps)
- **addresses** (id, street, city, governorate, country, zipCode)
- **products** (id, name, description, price, stockQuantity, category, imageUrl, active, timestamps)
- **cart_items** (id, user_id, product_id, quantity, price, timestamps)
- **orders** (id, user_id, totalPrice, status, timestamps)
- **order_items** (id, order_id, product_id, quantity, price, timestamps)

Relationships: User ↔ Address (1:1), User ↔ Cart/Order (1:M), Cart/Order ↔ Product (N:1), Order ↔ OrderItem (1:M with cascade).

---

## Testing

```bash
# Run all tests
mvn test

# Specific class
mvn test -Dtest=AuthenticationServiceImplTest

# Skip during build
mvn clean package -DskipTests
```

6 test classes covering service layer. Uses H2 in-memory DB for isolation. No external DB required.

---

## Docker Deployment

Multi-stage Dockerfile: JDK 25 build stage, JRE 25 runtime for smaller images.

```bash
# Build image
docker build -t okazx:latest .

# Run with external DB
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host:5432/okazx \
  -e JWT_SECRET_KEY=your-key \
  okazx:latest

# Or use docker-compose
docker-compose up -d --build
```

Services communicate via internal network. App waits for DB dependency.

---

## Configuration

Key properties in `application.properties`:

```properties
spring.datasource.url=${DB_URL}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

security.jwt.secret-key=${JWT_SECRET_KEY}
security.jwt.expiration-time=${JWT_EXPIRATION_TIME}

management.endpoints.web.exposure.include=health,info,metrics,env,loggers
springdoc.swagger-ui.enabled=true
```

Env vars loaded from `.env` via `spring.config.import`.

---

## Troubleshooting

**Port in use:** Change `server.port` in properties or kill process on 8080.

**DB connection failed:** Check PostgreSQL running, verify `.env` vars, inspect `docker-compose logs db`.

**JWT signature error:** Ensure `JWT_SECRET_KEY` matches (min 32 chars), regenerate tokens.

**Admin not created:** Check DB for admin user, verify `.env`, restart app.

**Actuator 403:** Login as ADMIN, include Bearer token.

Debug: Add `logging.level.com.am9.okazx=DEBUG` to properties.

---

## References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/index.html)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JJWT Library](https://github.com/jwtk/jjwt)
- [MapStruct User Guide](https://mapstruct.org/documentation/stable/reference/html/)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [PostgreSQL Manual](https://www.postgresql.org/docs/15/index.html)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

Built this to demonstrate production-ready backend skills: clean architecture, security best practices, comprehensive testing, and containerization. Everything's implemented hands-on, no shortcuts.

---

## Maintainers / Contact

- Abdulrahman Mujahid — abdulrahman.mujahid09@gmail.com
