# Detailed Execution Plan: Simple Inventory Management (Shift-Left DevOps Approach)

This updated serialized plan incorporates a "Shift Left" approach. We will set up Docker and a GitHub Actions CI pipeline right away. From that point on, **every feature branch must include relevant tests**, and the CI pipeline will verify those tests automatically before any PR can be merged.

## Prerequisite: The Standard Workflow (MUST READ)

Since direct pushes to `main` equal automatic failure, you must follow this exact loop for **every single phase** below:

1. Create a feature branch off `develop` (e.g., `git checkout -b feature/database-setup`).
2. Write the code **AND the required tests** for your feature.
3. Commit and push: `git push origin YOUR_BRANCH_NAME`.
4. Open a **Pull Request (PR)** on GitHub from the feature branch into `develop`.
5. **CI/CD Automation runs:** GitHub Actions will automatically build the project and run your tests.
6. The **other member** reviews the code, ensures the CI check is green (Passed), approves, and merges.
7. Both members pull the latest `develop` locally.

---

## Phase 1: GitHub Settings & Repository Setup (Status: Complete)

_Requirement Check:_ Branch protection configured, No direct push to main.

- Repository created, collaborators invited, `main` branch protected.
- `develop` branch initialized.

## Phase 2: Dockerization & Base CI/CD Pipeline

_Requirement Check:_ Dockerfile, GitHub Actions, Postgres Docker Compose.

- **Assignee:** Member 1
- **Branch:** `feature/ci-cd-docker`
- **Task Details:**
  1. Create a `Dockerfile` for the Spring Boot app.
  2. Create a `compose.yaml` to spin up a local PostgreSQL database (`5432:5432`).
  3. Create `.github/workflows/ci.yml`. Configure it to trigger on PRs to `develop` and `main`. It should: Checkout code, Setup Java 17/21, and run `mvn clean test`.
  4. Once merged, update GitHub Branch Protection for `develop` and `main` to **Require status checks to pass** (selecting the newly created test workflow).
- **Git Action:** Push PR. Member 2 reviews. Must pass CI. Merge.

## Phase 3: Entities, Repositories, & DB Connection (Status: Complete)

_Requirement Check:_ DB connected, 4+ tables, Entity relationships (1:M, M:1, M:M), Tests.

- **Assignee:** Member 2
- **Branch:** `feature/entities-db`
- **Task Details:**
  1. Setup Spring Datasource pointing to PostgreSQL in `application.yaml` using environment variables.
  2. Create `User`, `Role`, `Product`, `Order`, `OrderItem` entities with proper mappings.
  3. Create Spring Data JPA Repositories.
  4. **Testing:** Write basic `@DataJpaTest` tests to verify that your entities save correctly to the database.
- **Git Action:** Phase implementation completed on `feature/entities-db`; push branch, open PR to `develop`, ensure CI is green, then complete review and merge.

## Phase 4: Foundational Spring Security (Status: Complete)

_Requirement Check:_ Spring Security, BCrypt Password encryption.

- **Assignee:** Member 1
- **Branch:** `feature/security`
- **Task Details:**
  1. Create `SecurityConfig` with `BCryptPasswordEncoder`.
  2. Implement `UserDetailsService` connecting to `UserRepository`.
  3. Configure basic `HttpSecurity` (form login, allow `/register` publicly).
  4. **Testing:** Write Unit tests mocking the `UserRepository` to test your `UserDetailsService`.
- **Git Action:** Phase implementation completed on `security`; push branch, open PR to `develop`, ensure CI is green, then complete review and merge.

## Phase 5: Business Logic, DTOs & Exceptions (Status: Complete)

_Requirement Check:_ DTO usage, Exception handling, Layered architecture.

- **Assignee:** Member 2
- **Branch:** `feature/business-logic`
- **Task Details:**
  1. Create `@ControllerAdvice` for global exceptions (e.g., `ResourceNotFoundException`).
  2. Create DTOs (`ProductRequestDto`, `ProductResponseDto`, `UserRegistrationDto`).
  3. Create `ProductService` and `AuthService`.
  4. **Testing:** Write highly focused Unit Tests using `@ExtendWith(MockitoExtension.class)` for all Service classes.
- **Git Action:** Phase implementation completed on `feature/business-logic`; push branch, open PR to `develop`, ensure CI is green, then complete review and merge.

## Phase 6: REST Controllers & Roles Authorization (Status: Complete)

_Requirement Check:_ 3 Controllers, Roles enforced (ADMIN, SELLER, BUYER), REST principles.

- **Assignee:** Member 1
- **Branch:** `feature/rest-controllers`
- **Task Details:**
  1. Create `ProductController`, `OrderController`, `AuthController`.
  2. Restrict methods using `@PreAuthorize` based on roles.
  3. **Testing:** Write Integration Tests using `@SpringBootTest` and `MockMvc` to verify endpoint routing, DTO validation, and security access.
- **Git Action:** Phase implementation completed on `feature/rest-controllers`; push branch, open PR to `develop`, ensure CI is green, then complete review and merge.

## Phase 7: UI Integration (Thymeleaf Templates) (Status: Complete)

_Requirement Check:_ Thymeleaf, Complete App Flow.

- **Assignee:** Member 2
- **Branch:** `feature/ui-templates`
- **Task Details:**
  1. Create UI views: `login.html`, `register.html`, `dashboard.html`.
  2. Integrate Thymeleaf Security Dialect for role-based UI rendering.
  3. **Testing:** Add/update `MockMvc` tests to ensure the endpoints return the correct view names and HTTP status codes (200 OK).
- **Git Action:** Phase implementation completed on `feature/ui-templates`; push branch, open PR to `develop`, ensure CI is green, then complete review and merge.

## Phase 8: Automated Cloud Deployment (CD)

_Requirement Check:_ App deployed publicly automatically.

- **Assignee:** Member 1
- **Branch:** `feature/automated-deployment`
- **Task Details:**
  1. Register on **Render** (or similar platform).
  2. Create the managed PostgreSQL database on Render.
  3. Either append to the existing Action or create a `.github/workflows/deploy.yml` that pulls the image or hooks deeply into Render's auto-deploy upon commits to `main`.
- **Git Action:** Push PR. Member 2 reviews and merges.

## Phase 9: Final Documentation & Demo

_Requirement Check:_ README, Diagrams, Clean code, Demo prep.

- **Assignee:** Member 2 (assisted by Member 1)
- **Branch:** Final PR `develop` -> `main`
- **Task Details:**
  1. Make sure everything works smoothly.
  2. Write a stellar `README.md` with Architecture Diagrams, ER Diagrams, and the Live URL.
  3. Merge `develop` into `main`. Verify the deployment works beautifully on the public cloud.
  4. Prepare the 5-Minute Demo.
