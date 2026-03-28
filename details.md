# Progress Details: Inventory Management Project

This document captures everything completed so far (Phase 1, Phase 2, Phase 3, Phase 4, Phase 5, Phase 6, and Phase 7), including technical decisions, workflow rules, and likely teacher viva questions.

## 1. Current Project Status

- Current working branch: `develop`
- Planning style in use: Shift-Left DevOps
- Completed phases:
  - Phase 1: GitHub setup and branch strategy
  - Phase 2: Dockerization + base CI pipeline
  - Phase 3: Entities, repositories, and DB connection
  - Phase 4: Foundational Spring Security
  - Phase 5: Business logic, DTOs, and exception handling
  - Phase 6: REST controllers and role authorization
  - Phase 7: UI integration with Thymeleaf templates
- Test status (latest local run): 19 passed, 0 failed

## 2. Phase 1 Completed Work (GitHub Governance)

### 2.1 Repository and Collaboration

- GitHub repository created.
- `develop` branch created and pushed.
- Team workflow established: feature branch -> PR to `develop` -> review by other member -> merge.

### 2.2 Branch Protection Model Used

For `main`, the team enabled the policy aligned with teacher requirements:

- Require a pull request before merging.
- Require approvals: 1.
- No direct push workflow allowed.

Meaning in practice:

- A developer cannot merge to `main` without opening a PR.
- The person who created the PR cannot self-approve to satisfy the requirement.
- The second teammate must review and approve.

### 2.3 Why This Model

- Enforces code review discipline in a 2-member team.
- Protects `main` from unreviewed commits.
- Matches rubric requirement: no direct push to `main`, at least one review.
- `develop` rule now enforces required CI status check (`test`) before merge.

## 3. Phase 2 Completed Work (Docker + CI)

### 3.1 Docker Compose for PostgreSQL

File: `compose.yaml`

Implemented:

- PostgreSQL image pinned to `postgres:16-alpine`.
- Environment variables configured:
  - `POSTGRES_DB=mydatabase`
  - `POSTGRES_USER=myuser`
  - `POSTGRES_PASSWORD=secret`
- Port mapping fixed to `5432:5432`.

Why important:

- Gives a deterministic local DB setup for development.
- Explicit host-to-container mapping avoids connection confusion.

### 3.2 Application Containerization

File: `Dockerfile`

Implemented multi-stage build:

1. Build stage uses Maven + JDK 17 image.
2. Runs dependency prefetch and `mvn clean package -DskipTests`.
3. Runtime stage uses lightweight JRE 17 alpine image.
4. App jar copied and exposed on port `8081`.

Why important:

- Smaller production image.
- Faster deploy startup with only runtime dependencies.

### 3.3 Docker Ignore Rules

File: `.dockerignore`

Implemented exclusions for build and IDE clutter:

- `target/`, `.git/`, `.idea/`, `.vscode/`, `.mvn/`, wrapper scripts, logs, etc.

Why important:

- Faster Docker build context transfer.
- Smaller, cleaner Docker layer cache behavior.

### 3.4 CI Pipeline (GitHub Actions)

File: `.github/workflows/ci.yml`

Workflow behavior:

- Triggered on:
  - Pull requests to `develop` and `main`
  - Pushes to `develop` and `main`
- Steps:
  1. Checkout source
  2. Setup Java 17 (Temurin) + Maven cache
  3. Make Maven wrapper executable
  4. Run `./mvnw -B clean test`

Why important:

- Every feature branch PR is automatically validated.
- Prevents merging broken code if status checks are enforced.

### 3.5 Test Runtime Stabilization for CI

Files:

- `pom.xml`
- `src/test/resources/application.yaml`

Implemented:

- Added H2 in-memory DB dependency (test scope).
- Added test datasource config using H2:
  - `jdbc:h2:mem:testdb`
  - Hibernate `create-drop`
  - H2 dialect

Why important:

- CI tests run without needing external Postgres service.
- Reduces flaky pipeline failures due to DB connectivity in CI runners.

## 4. Tests Executed So Far

- Local test execution performed with the integrated runner.
- Result: 19 tests passed, 0 failed.
- Existing test classes currently include:
  - `InventoryManagementApplicationTests.java`
  - `InventoryDataJpaTests.java`
  - `CustomUserDetailsServiceTest.java`
  - `AuthServiceTest.java`
  - `ProductServiceTest.java`
  - `ControllerIntegrationTests.java`

## 5. Workflow You Are Following Now

For each feature branch:

1. Create branch from `develop`.
2. Implement feature.
3. Add/update relevant tests in same branch.
4. Push branch.
5. Open PR to `develop`.
6. Wait for CI to pass.
7. Teammate reviews and approves.
8. Merge.

This is exactly the behavior your teacher requested.

## 6. What Is Not Done Yet

- Phase 8 onwards (deployment, final docs/demo) are pending.

## 6.1 Phase 3 Completed Work (Entities + Repositories + DB Config)

Files:

- `src/main/resources/application.yaml`
- `src/main/java/com/example/inventory_management/model/User.java`
- `src/main/java/com/example/inventory_management/model/Role.java`
- `src/main/java/com/example/inventory_management/model/Product.java`
- `src/main/java/com/example/inventory_management/model/CustomerOrder.java`
- `src/main/java/com/example/inventory_management/model/OrderItem.java`
- `src/main/java/com/example/inventory_management/repository/UserRepository.java`
- `src/main/java/com/example/inventory_management/repository/RoleRepository.java`
- `src/main/java/com/example/inventory_management/repository/ProductRepository.java`
- `src/main/java/com/example/inventory_management/repository/CustomerOrderRepository.java`
- `src/main/java/com/example/inventory_management/repository/OrderItemRepository.java`
- `src/test/java/com/example/inventory_management/InventoryDataJpaTests.java`

Implemented:

- Main datasource now reads PostgreSQL config from environment variables with defaults:
  - `DB_URL` (default `jdbc:postgresql://localhost:5432/mydatabase`)
  - `DB_USER` (default `myuser`)
  - `DB_PASSWORD` (default `secret`)
- Added 5 entities with required relationships:
  - `User` <-> `Role` as `M:M`
  - `User` -> `CustomerOrder` as `1:M`
  - `CustomerOrder` -> `OrderItem` as `1:M`
  - `OrderItem` -> `Product` as `M:1`
- Data model now creates 6 tables in DB:
  - `users`
  - `roles`
  - `products`
  - `orders`
  - `order_items`
  - `user_roles` (join table for many-to-many)
- Added Spring Data JPA repositories for all entities.
- Added focused `@DataJpaTest` tests to verify persistence and relationships.

Validation performed:

- `mvn -B clean test` passed (`3 passed, 0 failed`).
- JPA test output confirms schema creation and relationship constraints.
- Application startup tested with PostgreSQL from Docker Compose and datasource connection established successfully.

Why important:

- Satisfies the Phase 3 requirement of database connection, multiple tables, and relationship mapping.
- Gives a stable persistence baseline before starting security and service layers.

## 6.2 Phase 4 Completed Work (Foundational Security)

Files:

- `src/main/java/com/example/inventory_management/security/SecurityConfig.java`
- `src/main/java/com/example/inventory_management/security/CustomUserDetailsService.java`
- `src/test/java/com/example/inventory_management/security/CustomUserDetailsServiceTest.java`

Implemented:

- Added `SecurityConfig` with:
  - `BCryptPasswordEncoder` bean for password hashing.
  - `@EnableMethodSecurity` to support future role-based method authorization.
  - `SecurityFilterChain` rules:
    - `"/register"` and `"/h2-console/**"` are public.
    - All other routes require authentication.
    - Form login enabled.
    - CSRF ignored only for H2 console and frame options set to same origin for H2 console use.
- Added `CustomUserDetailsService`:
  - Loads users by username from `UserRepository`.
  - Throws `UsernameNotFoundException` for unknown users.
  - Maps DB roles to Spring Security authorities with `ROLE_` prefix normalization.
- Added focused unit tests in `CustomUserDetailsServiceTest`:
  - Successful user load returns correct username, password, and authorities.
  - Missing user path throws `UsernameNotFoundException`.

Validation performed:

- Full clean build and tests passed via `mvnw.cmd -B clean test`.
- Test summary after Phase 4 additions: `5 passed, 0 failed, 0 errors`.
- Security wiring confirmed at startup log level (`customUserDetailsService` registered in authentication manager).

Why important:

- Satisfies Phase 4 requirement for foundational Spring Security + password encoder + custom user loading.
- Creates the base required for Phase 5 registration logic and Phase 6 role authorization.

## 6.3 Phase 5 Completed Work (Business Logic + DTOs + Exceptions)

Files:

- `src/main/java/com/example/inventory_management/dto/ProductRequestDto.java`
- `src/main/java/com/example/inventory_management/dto/ProductResponseDto.java`
- `src/main/java/com/example/inventory_management/dto/UserRegistrationDto.java`
- `src/main/java/com/example/inventory_management/exception/ResourceNotFoundException.java`
- `src/main/java/com/example/inventory_management/exception/BadRequestException.java`
- `src/main/java/com/example/inventory_management/exception/GlobalExceptionHandler.java`
- `src/main/java/com/example/inventory_management/service/ProductService.java`
- `src/main/java/com/example/inventory_management/service/AuthService.java`
- `src/test/java/com/example/inventory_management/service/ProductServiceTest.java`
- `src/test/java/com/example/inventory_management/service/AuthServiceTest.java`

Implemented:

- Added DTO layer for product create/update/response and user registration payload.
- Added service layer classes:
  - `ProductService` with create, getById, getAll, update, and delete operations.
  - `AuthService` with registration flow (duplicate checks, password encoding, default role assignment).
- Added global exception architecture:
  - `ResourceNotFoundException` for missing resources.
  - `BadRequestException` for invalid registration/business constraints.
  - `GlobalExceptionHandler` using `@ControllerAdvice` to produce consistent error responses.
- Added focused Mockito unit tests for service layer behaviors:
  - `ProductServiceTest` covers create path, not-found path, and delete path.
  - `AuthServiceTest` covers successful registration, duplicate username validation, and default role creation when missing.

Validation performed:

- Full clean build and tests passed via `mvnw.cmd -B clean test`.
- Test summary after Phase 5 additions: `11 passed, 0 failed, 0 errors`.
- Existing Phase 3 and Phase 4 tests still pass, confirming no regression.

Why important:

- Satisfies Phase 5 requirement for layered architecture and DTO usage.
- Establishes reusable business logic before controller implementation in Phase 6.
- Provides centralized exception handling foundation for future REST endpoints.

## 6.4 Phase 6 Completed Work (REST Controllers + Role Authorization)

Files:

- `src/main/java/com/example/inventory_management/controller/ProductController.java`
- `src/main/java/com/example/inventory_management/controller/AuthController.java`
- `src/main/java/com/example/inventory_management/controller/OrderController.java`
- `src/main/java/com/example/inventory_management/service/OrderService.java`
- `src/main/java/com/example/inventory_management/dto/OrderCreateRequestDto.java`
- `src/main/java/com/example/inventory_management/dto/OrderItemRequestDto.java`
- `src/main/java/com/example/inventory_management/dto/OrderItemResponseDto.java`
- `src/main/java/com/example/inventory_management/dto/OrderResponseDto.java`
- `src/main/java/com/example/inventory_management/repository/CustomerOrderRepository.java`
- `src/main/java/com/example/inventory_management/security/SecurityConfig.java`
- `src/main/java/com/example/inventory_management/exception/GlobalExceptionHandler.java`
- `src/test/java/com/example/inventory_management/controller/ControllerIntegrationTests.java`

Implemented:

- Added 3 REST controllers required by Phase 6:
  - `ProductController`: CRUD endpoints for products.
  - `OrderController`: place order, get own orders, and get all orders.
  - `AuthController`: registration endpoint.
- Enforced role-based authorization with `@PreAuthorize`:
  - Product create/update/delete: `ADMIN` or `SELLER`.
  - Product read endpoints: authenticated `ADMIN`/`SELLER`/`BUYER`.
  - Order placement and own-order history: authenticated `ADMIN`/`SELLER`/`BUYER`.
  - All-orders endpoint: `ADMIN` or `SELLER`.
- Added `OrderService` business logic for placing and listing orders:
  - Validates item quantity and stock.
  - Decrements stock on successful order placement.
  - Maps order entities to response DTOs including line totals and grand total.
- Extended repository and security/exception config for controller flow:
  - Added user-specific and sorted order queries in `CustomerOrderRepository`.
  - Allowed `/api/auth/register` as public.
  - Added explicit `AccessDeniedException` mapping to HTTP 403.

Validation performed:

- Full clean build and tests passed via `mvnw.cmd -B clean test`.
- Test summary after Phase 6 additions: `16 tests run, 0 failures, 0 errors` (Maven clean run).
- Integration tests verify role restrictions and endpoint behavior via MockMvc.

Why important:

- Satisfies Phase 6 requirement of 3 controllers and role-based authorization.
- Establishes API layer contract for upcoming UI integration in Phase 7.
- Catches access-control regressions early using integration tests.

## 6.5 Phase 7 Completed Work (UI Integration with Thymeleaf)

Files:

- `src/main/java/com/example/inventory_management/controller/ViewController.java`
- `src/main/java/com/example/inventory_management/security/SecurityConfig.java`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/dashboard.html`
- `src/main/resources/static/css/app.css`
- `src/main/resources/static/js/register.js`
- `src/test/java/com/example/inventory_management/controller/ControllerIntegrationTests.java`

Implemented:

- Added MVC page routes for `GET /login`, `GET /register`, and `GET /dashboard`.
- Added form-based registration route `POST /register` connected to `AuthService`.
- Added custom Spring Security login page wiring and default post-login redirect to `/dashboard`.
- Added Thymeleaf templates for login, registration, and dashboard views.
- Integrated Thymeleaf Security Dialect (`sec:authorize`) on dashboard sections for role-specific UI visibility.
- Added responsive CSS and lightweight client-side UX enhancement for registration submit state.
- Added MockMvc integration tests to verify `login`, `register`, and `dashboard` return expected view names and `200 OK` for allowed access.

Validation performed:

- Full test suite passed via `mvnw.cmd test`.
- Test summary after Phase 7 additions: `19 tests run, 0 failures, 0 errors`.

Why important:

- Satisfies Phase 7 requirement for Thymeleaf pages and role-aware rendering.
- Provides a usable UI entry point for the API features completed in earlier phases.
- Keeps regression safety through integration testing of view routing and security behavior.

## 7. Likely Teacher Questions and Good Answers

### Q1. What branch protection did you use and why?

Answer idea:

- We enforced PR-required merge on `main` with minimum 1 approval.
- This ensures no direct push and mandatory peer review in our 2-member team.

### Q2. Why did you create a `develop` branch?

Answer idea:

- `main` remains stable and protected.
- All feature work is integrated through reviewed PRs into `develop` first.

### Q3. Which database are you using in development?

Answer idea:

- PostgreSQL via Docker Compose (`postgres:16-alpine`, mapped to `5432:5432`).

### Q4. Which database runs during CI tests?

Answer idea:

- H2 in-memory DB for tests.
- We configured test-only datasource so CI does not depend on a live Postgres instance.

### Q5. Why not use PostgreSQL directly in CI from day one?

Answer idea:

- In-memory H2 provides fast and reliable baseline checks.
- PostgreSQL integration tests can be added later if needed for stricter DB parity.

### Q6. What does your CI pipeline currently verify?

Answer idea:

- Source checkout, Java setup, and full Maven test run (`clean test`) on PR/push to `develop` and `main`.

### Q7. If CI fails, can you merge?

Answer idea:

- Once status checks are enforced in branch protection, merge is blocked until checks pass.

### Q8. What exactly did you containerize?

Answer idea:

- The Spring Boot app is containerized with a multi-stage Dockerfile.
- Postgres is separately containerized via Compose.

### Q9. Why multi-stage Docker build?

Answer idea:

- Keeps runtime image smaller and cleaner.
- Build tools stay in builder stage only.

### Q10. Did you write tests while coding?

Answer idea:

- Yes, our workflow requires adding relevant tests in each feature PR.
- Current baseline tests pass (19/19).

## 7.1 Phase 3 Specific Viva Questions and Answers

### Q11. Why did you use environment variables for datasource config?

Answer idea:

- It keeps credentials and host config environment-specific and avoids hardcoding secrets.
- Same code can run on local Docker, CI, and cloud with only environment value changes.

### Q12. Why did you create a separate `OrderItem` entity instead of direct many-to-many between `Order` and `Product`?

Answer idea:

- `OrderItem` stores extra business data (`quantity`, `unitPrice`) that cannot be modeled in a plain many-to-many join.
- It is the standard inventory/order design for future extensibility.

### Q13. What relationship types did you implement, and where?

Answer idea:

- Many-to-many: `User` and `Role`.
- One-to-many / many-to-one: `User` to `CustomerOrder`, and `CustomerOrder` to `OrderItem`.
- Many-to-one: `OrderItem` to `Product`.

### Q14. How did you verify that entity mappings are correct?

Answer idea:

- Added `@DataJpaTest` cases that persist linked objects and assert IDs plus relationship traversal.
- Hibernate SQL output shows table creation, foreign keys, and inserts for mapped entities.

### Q15. Why are tests using H2 while runtime uses PostgreSQL?

Answer idea:

- H2 keeps CI fast and deterministic for baseline checks.
- Runtime path is still validated against PostgreSQL locally via Docker Compose.

### Q16. What repositories did you add in Phase 3?

Answer idea:

- `UserRepository`, `RoleRepository`, `ProductRepository`, `CustomerOrderRepository`, and `OrderItemRepository`.
- This gives a clean DAO layer for service logic in upcoming phases.

### Q17. What is the benefit of finishing Phase 3 before implementing security?

Answer idea:

- Security depends on user and role persistence.
- Completing entities/repositories first reduces coupling and makes Phase 4 implementation straightforward.

## 7.2 Phase 4 Specific Viva Questions and Answers

### Q18. Why did you implement a custom `UserDetailsService` instead of using in-memory users?

Answer idea:

- In-memory users are temporary and not tied to application data.
- Custom `UserDetailsService` loads real users/roles from the database through `UserRepository`.

### Q19. Why use `BCryptPasswordEncoder`?

Answer idea:

- BCrypt is a strong adaptive hashing algorithm designed for password storage.
- It is the Spring Security standard choice for safely storing user passwords.

### Q20. Why did you prefix roles with `ROLE_`?

Answer idea:

- Spring Security role checks expect authorities like `ROLE_ADMIN`.
- Prefix normalization avoids failures if DB stores role names as plain `ADMIN`/`SELLER`/`BUYER`.

### Q21. Which endpoints are public in your current configuration?

Answer idea:

- `/register` is public by design for account creation.
- `/h2-console/**` is public only for local development/testing convenience.
- All other endpoints require login.

### Q22. Why is CSRF ignored for H2 console only?

Answer idea:

- H2 console uses frames and interactive form behavior that often fails under strict CSRF defaults.
- We scoped CSRF relaxation narrowly to H2 paths to keep default protection elsewhere.

### Q23. How did you test security in Phase 4?

Answer idea:

- We wrote unit tests for `CustomUserDetailsService` using Mockito.
- Tests verify both success and failure paths (`UsernameNotFoundException`).

### Q24. What is still missing before production-grade security?

Answer idea:

- Registration/login flows with encoded password persistence.
- Role-based endpoint restrictions with `@PreAuthorize` in controllers.
- Proper exception handling and potentially JWT/session-hardening based on architecture choice.

## 7.3 Flow-Based Viva Questions (Up To Phase 4)

### Q25. Explain the security flow of a protected request in your current app.

Answer idea:

1. Client requests a protected endpoint.
2. Spring Security filter chain checks authentication state.
3. If user is not logged in, request is redirected to form login.
4. On login submit, Spring calls `CustomUserDetailsService.loadUserByUsername(...)`.
5. Service fetches the user from `UserRepository` and returns Spring `UserDetails` with mapped authorities.
6. Password is verified using `BCryptPasswordEncoder`.
7. If valid, Spring creates authenticated session context and allows protected endpoint access.

### Q26. Explain the flow of `CustomUserDetailsServiceTest`.

Answer idea:

1. Test class runs with `@ExtendWith(MockitoExtension.class)`.
2. `UserRepository` is mocked and injected into `CustomUserDetailsService`.
3. Success test stubs repository to return a user with role `ADMIN`.
4. Test calls `loadUserByUsername("alice")` and verifies username/password/authority (`ROLE_ADMIN`).
5. Failure test stubs repository to return empty optional.
6. Test asserts `UsernameNotFoundException` is thrown with expected message content.

### Q27. Explain the flow of `InventoryDataJpaTests` in Phase 3.

Answer idea:

1. Test boots a JPA-only Spring context via `@DataJpaTest`.
2. Embedded H2 datasource is configured for isolated test execution.
3. Hibernate generates schema from entity mappings.
4. Test saves linked entities (`User`, `Role`, `Product`, `CustomerOrder`, `OrderItem`).
5. Assertions verify persisted IDs and relationship integrity.
6. This confirms table mappings and foreign-key relationships are valid.

### Q28. Explain the CI flow from push/PR to merge decision.

Answer idea:

1. Developer pushes feature branch and opens PR to `develop`.
2. GitHub Actions workflow `CI` triggers automatically.
3. Runner checks out code, sets up Java, and runs `mvn clean test`.
4. If tests fail, required status check fails and merge remains blocked.
5. If tests pass, teammate review approval is still required.
6. Only after both conditions pass (CI + review), PR can be merged.

### Q29. Explain the branch protection flow your team follows.

Answer idea:

1. Work starts from `develop` on a dedicated feature branch.
2. Direct merge/push bypass is disabled by rule.
3. PR is mandatory with at least one approval.
4. Required status check `test` must pass.
5. Another team member approves, then PR is merged into `develop`.
6. Team syncs local `develop` and continues to next phase.

## 7.4 Phase 5 Specific Viva Questions and Answers

### Q30. Why introduce DTOs instead of returning entities directly?

Answer idea:

- DTOs decouple API contracts from database entities.
- They prevent exposing internal model fields and make validation/versioning easier.

### Q31. What responsibilities are in `ProductService`?

Answer idea:

- Handles product business operations: create, read, update, delete.
- Converts between entity model and response DTO.
- Throws `ResourceNotFoundException` for invalid product IDs.

### Q32. What responsibilities are in `AuthService`?

Answer idea:

- Handles registration business rules.
- Checks duplicate username and email.
- Encodes password using configured encoder.
- Assigns default `BUYER` role (creates role if missing).

### Q33. Why use `@ControllerAdvice` now before controllers?

Answer idea:

- It standardizes error responses early and avoids repeated try/catch logic later.
- When controllers are added in Phase 6, exception mapping already exists.

### Q34. How are duplicate registrations prevented?

Answer idea:

- Service checks `UserRepository.findByUsername(...)` and `findByEmail(...)` first.
- If either exists, service throws `BadRequestException`.

### Q35. How did you validate Phase 5 logic quality?

Answer idea:

- Added focused service unit tests with Mockito for success and failure paths.
- Verified full test suite still passes after integration of new service layer.

## 7.5 Flow-Based Viva Questions (Phase 5)

### Q36. Explain the user registration flow implemented in Phase 5.

Answer idea:

1. Controller receives registration payload and passes `UserRegistrationDto` to `AuthService`.
2. `AuthService` validates username and email uniqueness via `UserRepository`.
3. Password is encoded using `PasswordEncoder`.
4. Default role `BUYER` is loaded from `RoleRepository` or created if absent.
5. Role is attached to user and user is persisted via `UserRepository`.
6. On validation failure, `BadRequestException` is thrown and mapped by `GlobalExceptionHandler`.

### Q37. Explain the product update flow in `ProductService`.

Answer idea:

1. Service receives product ID and `ProductRequestDto`.
2. Service loads existing product from repository.
3. If missing, throws `ResourceNotFoundException`.
4. If found, service updates mutable fields and saves entity.
5. Saved entity is mapped to `ProductResponseDto` and returned.

## 7.6 Phase 6 Specific Viva Questions and Answers

### Q38. Why did you use `@PreAuthorize` on controller methods?

Answer idea:

- It enforces role checks at method level and keeps authorization close to endpoint behavior.
- It is clearer and easier to audit than only URL-pattern-based access rules.

### Q39. Which roles can create or delete products?

Answer idea:

- Only `ADMIN` and `SELLER` can create, update, or delete products.
- `BUYER` is restricted to read-only product access.

### Q40. Why did you add an explicit `AccessDeniedException` handler?

Answer idea:

- Without it, denied authorization can bubble to generic exception mapping and produce incorrect 500 responses.
- Explicit mapping ensures correct HTTP 403 responses for forbidden access.

### Q41. What does `OrderService.placeOrder(...)` validate?

Answer idea:

- Order must contain at least one item.
- Item quantity must be positive.
- Product must exist.
- Product stock must be enough before reducing inventory.

### Q42. How do you compute order totals in response?

Answer idea:

- Each line total is `unitPrice * quantity` in `OrderItemResponseDto`.
- Grand total is the sum of all line totals in `OrderResponseDto`.

## 7.7 Flow-Based Viva Questions (Phase 6)

### Q43. Explain the product create API flow (authorized seller/admin).

Answer idea:

1. Authenticated user calls `POST /api/products`.
2. `@PreAuthorize` checks role (`ADMIN`/`SELLER`).
3. Controller forwards payload to `ProductService.createProduct`.
4. Service maps DTO to entity and saves via repository.
5. Service maps saved entity to response DTO.
6. API returns `201 Created`.

### Q44. Explain the order placement flow.

Answer idea:

1. Authenticated user calls `POST /api/orders` with item list.
2. Controller reads username from `Principal` and calls `OrderService.placeOrder`.
3. Service validates request, loads user and products, checks stock.
4. Service creates `CustomerOrder` and `OrderItem` entities.
5. Service reduces product stock and saves order transactionally.
6. API returns order summary with line totals and total amount.

### Q45. Explain the forbidden access test flow for buyer creating product.

Answer idea:

1. Test runs with `@WithMockUser(roles = "BUYER")`.
2. Test calls `POST /api/products`.
3. `@PreAuthorize` fails role check.
4. Access is denied and mapped to HTTP 403 by exception handler.
5. Test asserts forbidden response, confirming role policy is enforced.

## 8. Recommended Next Action (Immediately After This)

- Open PR from `feature/ui-templates` to `develop` for Phase 7 changes.
- Ensure CI is green, get teammate review approval, and merge.
- Start Phase 8 on a fresh branch from updated `develop`.
