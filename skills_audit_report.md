# Codebase Compliance Audit: Project Skills

This report summarizes the compliance level of the `hotel-booking-service` codebase against the 19 project skills located in the [.agents/skills/](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills) directory. 

Ten specialized subagents audited the codebase concurrently, exploring the package structures, controllers, services, database configurations, schemas, tests, and configurations.

---

## 1. Compliance Matrix

| Skill Group / Name | Status | Key Observation |
|---|---|---|
| **Architecture & Structure** | | |
| [layered-architecture](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/layered-architecture/SKILL.md) | ⚠️ Partial Match | Layer separation is clean, but enum conversion and field injection violate rules. |
| [hexagonal-architecture](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/hexagonal-architecture/SKILL.md) | ❌ Not Used | Monolithic layered structure; core domain is coupled with Spring/JPA annotations. |
| **Domain & Data Flow** | | |
| [domain-driven-design](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/domain-driven-design/SKILL.md) | ❌ Not Used | Domain model is anemic; no value objects or aggregate root boundaries. |
| [transactional-patterns](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/transactional-patterns/SKILL.md) | ⚠️ Partial Match | Core transactional boundaries are missing on many writes; JTA imports are used. |
| [spring-data-jpa](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/spring-data-jpa/SKILL.md) | ❌ Deviating | Legacy entities use auto-increment keys, EAGER mappings, and lack projections. |
| [flyway-migrations](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/flyway-migrations/SKILL.md) | ⚠️ Partial Match | Naming convention is correct, but many foreign keys are unindexed. |
| **API & Security** | | |
| [openapi-first](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/openapi-first/SKILL.md) | ❌ Not Used | Code-first approach generating Swagger at runtime; no OpenAPI generator. |
| [rest-api-conventions](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/rest-api-conventions/SKILL.md) | ⚠️ Partial Match | Custom envelopes and pagination are inconsistent; status codes default to 200. |
| [spring-security-jwt](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/spring-security-jwt/SKILL.md) | ⚠️ Partial Match | Uses custom filter; refresh token hashing suffers from BCrypt truncation vulnerability. |
| [oauth2-resource-server](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/oauth2-resource-server/SKILL.md) | ❌ Not Used | App functions as an independent JWT issuer rather than an OAuth2 resource server. |
| **Quality & Operations** | | |
| [testing-pyramid](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/testing-pyramid/SKILL.md) | ⚠️ Partial Match | Integrations/unit tests exist, but controller and repository slice tests are missing. |
| [problem-details-rfc9457](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/problem-details-rfc9457/SKILL.md) | ❌ Not Used | Custom response envelopes wrap errors; raw 500 stack traces/messages are leaked. |
| [hateoas](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/hateoas/SKILL.md) | ❌ Not Used | HATEOAS is not implemented; REST APIs do not provide hypermedia transitions. |
| [spring-data-redis](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/spring-data-redis/SKILL.md) | ❌ Not Used | Caching is absent; `spring-boot-starter-data-redis` is not in POM. |
| [spring-batch](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/spring-batch/SKILL.md) | ❌ Not Used | Batch jobs are absent; `spring-boot-starter-batch` is not in POM. |
| [spring-ai-integration](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/spring-ai-integration/SKILL.md) | ❌ Not Used | AI integrations are absent; `spring-ai` dependencies are missing. |
| [ai-observability](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/ai-observability/SKILL.md) | ❌ Not Used | General Actuator/Prometheus metrics exist, but no AI-specific tracking is set up. |
| [multi-module-maven](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/multi-module-maven/SKILL.md) | ❌ Not Used | Project is built as a single Maven module. |
| [mcp-server](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/.agents/skills/mcp-server/SKILL.md) | ❌ Not Used | Excluded from the application; only referenced in `harness.db` tools schema. |

---

## 2. Critical Findings & Security Risks

### 🚨 BCrypt Truncation on JWT Refresh Tokens
In [AuthAccountService.java](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/src/main/java/org/example/hotelbookingservice/services/AuthAccountService.java), JWT refresh tokens are hashed and validated using `BCryptPasswordEncoder`. 
* **The Vulnerability**: BCrypt has a hard limit of 72 bytes. Input strings longer than 72 bytes are silently truncated. Because JWT refresh tokens are typically 150+ characters, **only the first 72 characters of the token are verified**. This bypasses cryptographic signature checks on the client token.
* **The Performance Issue**: BCrypt is intentionally slow and CPU-heavy. Evaluating `matches()` on every token refresh request wastes CPU cycles and adds 100–200ms latency.

### 🚨 Database Schema Discrepancy & Bypassed JPA Entities
A critical gap exists between persistence entities and migration schemas:
* **Migrations**: [V1__init_schema.sql](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/src/main/resources/db/migration/V1__init_schema.sql) defines a modern schema using `UUID` primary keys, snake_case columns, and PostgreSQL `timestamptz`.
* **Entities**: Entities in [entity/](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/src/main/java/org/example/hotelbookingservice/entity/) represent a legacy MySQL schema with auto-incrementing `Integer` IDs, camelCase columns, and backticks.
* **Bypass**: Because the JPA entities cannot run against the migration schema, the core operations services (e.g. `BookingOperationsService`) bypass JPA entirely and run raw SQL queries using `NamedParameterJdbcTemplate` or `JdbcTemplate`.

### 🚨 Long-Running Database Transactions with Network I/O
In [HotelServiceImpl.java](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/src/main/java/org/example/hotelbookingservice/services/impl/HotelServiceImpl.java#L47) and [RoomServiceImpl.java](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/src/main/java/org/example/hotelbookingservice/services/impl/RoomServiceImpl.java), synchronous Cloudinary file uploads are executed inside `@Transactional` blocks.
* **The Impact**: The database connection is held open during the external network round-trip, leading to database connection pool exhaustion under load.

### 🚨 Information Leakage in Exception Handler
In [GlobalExceptionHandler.java](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/src/main/java/org/example/hotelbookingservice/exception/GlobalExceptionHandler.java), unhandled generic exceptions (`Exception.class`) return raw error messages back to the API client:
```java
ApiResponse.<String>builder()
    .status(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
    .message("Internal Server Error: " + ex.getMessage()) // ⚠️ Leaks exception details/paths
    .build()
```

---

## 3. General Architecture & Design Violations

### A. Layering & Clean Code
* **Enum Try-Catch in Controller**: In [RoomController.java](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/src/main/java/org/example/hotelbookingservice/controller/RoomController.java#L65-L70), the controller manually try-catches standard enum conversions rather than letting exceptions be resolved by a `@RestControllerAdvice`.
* **Field Injection in MapStruct**: Abstract MapStruct mappers (such as `BookingMapper.java` and `RoomMapper.java`) use field injection (`@Autowired protected`) instead of constructor injection.
* **Typo**: Configuration file `SecurtyFilter.java` is misspelled (should be `SecurityFilter.java`).

### B. Transaction Management
* **JTA Imports**: Services (e.g., `BookingServiceImpl`, `HotelServiceImpl`) import `jakarta.transaction.Transactional` (JTA) rather than Spring's `@Transactional`. This prevents the use of Spring-specific transaction parameters like `readOnly = true`.
* **Lack of Read-Only Optimization**: No class or method in the codebase is configured with `@Transactional(readOnly = true)`.
* **Writes without Transactions**: Multiple writing operations in `BookingServiceImpl`, `UserServiceImpl`, `PhysicalRoomServiceImpl`, `HotelServiceImpl`, and others lack any transaction context (no `@Transactional` defined).
* **Self-Invocation**: [BookingOperationsService.java](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/src/main/java/org/example/hotelbookingservice/services/BookingOperationsService.java) calls transactional methods internally (e.g., `expireBookingIfDue`) using `this`, causing the transaction context to be ignored.

### C. Spring Data JPA & Migrations
* **N+1 Eager Loading**: In [Booking.java](file:///mnt/Working/Working/School/Y4_2/Thesis/hotel-booking-service/src/main/java/org/example/hotelbookingservice/entity/Booking.java), the `@ManyToOne` association to `User` does not specify `fetch = FetchType.LAZY`. It defaults to `EAGER`, causing N+1 query patterns.
* **Anemic Entities**: Entities use public constructors and class-level `@Setter`/`@Data` annotations, leaving them with no encapsulation or behavior.
* **Unindexed Foreign Keys**: Over 20 foreign key columns in `V1__init_schema.sql` lack database indexes, posing performance risks as data scales.

### D. Testing & Quality
* **Missing Slice Tests**: No `@WebMvcTest` controller tests or `@DataJpaTest` repository slice tests are implemented.
* **Disabled Legacy Tests**: Unit tests targeting legacy integer-ID structures (like `BookingServiceImplTest`) are disabled using `@Disabled`.
* **Manual Integration Bootstrapping**: Integration tests programmatically configure database connections and context registrations, bypassing Spring Boot's unified `@SpringBootTest` configuration.

---

## 4. Remediation Roadmap

To resolve these violations and align your codebase with the repository's project skills, follow these steps:

### Phase 1: Security & Exception Fixes (Immediate)
1. **Fix Refresh Token Hashing**: Replace BCrypt encoding in `AuthAccountService` with a fast SHA-256 hash lookup.
2. **Remove Network I/O from Transactions**: Refactor file uploads in `HotelServiceImpl` and `RoomServiceImpl` to execute *before* opening a database transaction.
3. **Secure Exception Messages**: Remove the concatenation of `ex.getMessage()` on 500 errors in `GlobalExceptionHandler`.
4. **Fix Conflicting Authorization**: Remove `/api/v1/hotels/my-hotels` from the HTTP filter's `permitAll()` list in `SecurtyFilter.java`.

### Phase 2: Unify DB Schema & Persistence Layer (High Priority)
1. **Rebuild JPA Entities**:
   * Refactor JPA entities in the `entity` package to match the PostgreSQL structure (rename `User` to `Account`, migrate `Integer` primary keys to `UUID`s, and map snake_case columns).
   * Update the service layer to use modern Spring Data JPA repository queries rather than fallback JDBC template queries.
2. **Add Missing Indexes**: Create a migration file `V3__add_missing_foreign_key_indexes.sql` to index all foreign keys in PostgreSQL.
3. **Configure Batching**: Turn on JDBC batch inserts by setting `spring.jpa.properties.hibernate.jdbc.batch_size: 50` in your profiles.

### Phase 3: Architecture & API Alignment
1. **Standardize Transactions**:
   * Replace all JTA `@Transactional` imports with Spring's `@Transactional`.
   * Set `@Transactional(readOnly = true)` at the class level of read-only services.
   * Ensure writing services are annotated with writing `@Transactional` settings.
2. **Adopt RFC 9457 Problem Details**: Enable `spring.mvc.problemdetails.enabled: true` in your YAML and migrate `GlobalExceptionHandler` to return `ProblemDetail`.
3. **Remove Verbs from URLs**: Standardize endpoints in traditional controllers (e.g. migrate `POST /api/v1/hotels/add` to `POST /api/v1/hotels`).
4. **Implement Spring Data Pageable**: Unify pagination by injecting `Pageable` into list endpoints instead of relying on custom `limit` and `offset` request parameters.

### Phase 4: Test Suite Standardizing
1. **Configure Controller & Repository Slices**: Implement `@WebMvcTest` and `@DataJpaTest` configurations.
2. **Unify Integration Tests**: Refactor integration tests to use `@SpringBootTest` alongside a shared `@Testcontainers` configuration class.
