# Detailed Execution Plan: Simple Inventory Management (2-Person Team)

This step-by-step serialized plan incorporates the mandatory Git & GitHub pull request workflow to ensure you safely avoid "Direct push to main" and meet all project evaluation rubrics exactly.

## Prerequisite: The Standard Workflow (MUST READ)

Since direct pushes to `main` equal automatic failure, you must follow this exact loop for **every single phase** below:

1. One team member creates a feature branch off `develop` (e.g., `git checkout -b feature/database-setup`).
2. That member completes the assigned code/tasks.
3. The member commits and pushes to GitHub: `git push origin YOUR_BRANCH_NAME`.
4. Open a **Pull Request (PR)** on GitHub from the feature branch into `develop`.
5. The **other member** reviews the code, leaves a comment, approves the PR, and merges it into `develop`.
6. Both members pull the latest `develop` locally: `git checkout develop && git pull`.

---

## Phase 1: GitHub Settings & Repository Setup (Do This First)

_Requirement Check:_ Branch protection configured, No direct push to main.

- **Member 1:** Go to GitHub and create a new repository. Invite Member 2 as a Collaborator.
- **Member 1:** Push this initialized Spring Boot project to `main` branch.
- **Member 1:** Go to the Repository Settings in GitHub -> **Branches** -> Add a branch protection rule for `main`.
  - Check: _Require a pull request before merging_.
  - Check: _Require approvals_ (Set to 1).
  - Check: _Do not allow bypassing the above settings_.
- **Member 1:** Locally, create the `develop` branch and push it: `git checkout -b develop && git push -u origin develop`.
- **Member 2:** Accept the GitHub invite, clone the repo locally, and checkout the `develop` branch.
- _Status:_ Workspace is primed. Branch protection is active.

## Phase 2: Docker & Database Connection

_Requirement Check:_ Postgres, proper environment variables.

- **Assignee:** Member 1
- **Branch:** `feature/db-setup`
- **Task Details:**
  1. In `compose.yaml`, map Postgres port `5432:5432`.
  2. In `src/main/resources/application.yaml`, setup Spring Datasource pointing to PostgreSQL.
  3. Use environment variables to avoid hardcoded credentials (e.g., `url: ${DB_URL:jdbc:postgresql://localhost:5432/mydatabase}`, `username: ${DB_USER:myuser}`).
- **Git Action:** Member 1 pushes the code, opens PR. Member 2 reviews and merges.

## Phase 3: Entity Creation & Database Relationships

_Requirement Check:_ 4+ tables, proper entity relationships (1:M, M:1, M:M), clean code.

- **Assignee:** Member 2
- **Branch:** `feature/entities-repositories`
- **Task Details:**
  1. Create package `com.example.inventory_management.entity`.
  2. Create `User` (id, username, password).
  3. Create `Role` (id, name). Map Many-to-Many to `User` (creates `user_roles`).
  4. Create `Product` (id, name, description, price, stockQuantity).
  5. Create `Order` (id, date, status). Map Many-to-One to `User` (a user has many orders).
  6. Create `OrderItem` (id, quantity). Map Many-to-One to `Order` and `Product`.
  7. Create package `com.example.inventory_management.repository` and add JPA Repository interfaces for these entities.
- **Git Action:** Member 2 pushes code, opens PR. Member 1 reviews and merges.

## Phase 4: Foundational Spring Security

_Requirement Check:_ Spring Security implemented, Password encryption (BCrypt).

- **Assignee:** Member 1
- **Branch:** `feature/basic-security`
- **Task Details:**
  1. Create package `com.example.inventory_management.security`.
  2. Create `SecurityConfig` class. Define `BCryptPasswordEncoder` bean.
  3. Implement a custom `UserDetailsService` to fetch users by username from the `UserRepository`.
  4. Configure basic HttpSecurity (e.g., allow `/register` publicly, require login for everything else, enable form login).
- **Git Action:** Member 1 pushes code, opens PR. Member 2 reviews and merges.

## Phase 5: Business Logic, DTOs & Exceptions

_Requirement Check:_ DTO usage, Exception handling, Layered architecture.

- **Assignee:** Member 2
- **Branch:** `feature/business-logic`
- **Task Details:**
  1. Create a `@ControllerAdvice` class in an `exception` package to handle errors cleanly (e.g., `ResourceNotFoundException`).
  2. Create a `dto` package (e.g., `ProductRequestDto`, `ProductResponseDto`, `UserRegistrationDto`).
  3. Create `ProductService` for handling CRUD operations. Ensure it returns DTOs instead of raw Entities.
  4. Create `AuthService` handling user registration (encrypting the password before saving to DB).
- **Git Action:** Member 2 pushes code, opens PR. Member 1 reviews and merges.

## Phase 6: REST Controllers & Roles Authorization

_Requirement Check:_ 3 Controllers, Roles enforced (ADMIN, SELLER, BUYER), REST principles.

- **Assignee:** Member 1
- **Branch:** `feature/rest-controllers`
- **Task Details:**
  1. Restrict methods using `@PreAuthorize` tags based on roles (e.g., Only `ADMIN` or `SELLER` can create/delete products).
  2. Create `ProductController` with standard REST mapped endpoints (GET, POST, PUT, DELETE).
  3. Create `OrderController` for placing and fetching orders.
  4. Create `AuthController` to handle the registration endpoint.
- **Git Action:** Member 1 pushes code, opens PR. Member 2 reviews and merges.

## Phase 7: UI Integration (Thymeleaf Templates)

_Requirement Check:_ Thymeleaf, Complete App Flow.

- **Assignee:** Member 2
- **Branch:** `feature/ui-templates`
- **Task Details:**
  1. Create `login.html` and `register.html` in `src/main/resources/templates/`.
  2. Create `dashboard.html` for listing products.
  3. Integrate the Thymeleaf Security Dialect to dynamically show "Add Product" buttons only if the logged-in user is an ADMIN or SELLER.
- **Git Action:** Member 2 pushes code, opens PR. Member 1 reviews and merges.

## Phase 8: Automated Tests

_Requirement Check:_ 15 Unit tests, 3 Integration tests.

- **Assignee:** Member 1 & Member 2 split
- **Branch:** `feature/tests`
- **Task Details:**
  1. **Member 1:** Write 15 Unit tests in `src/test/...`. Focus purely on testing services (e.g., `ProductService`) using `@ExtendWith(MockitoExtension.class)` and `@Mock`.
  2. **Member 2:** Write 3 Integration tests. Use `@SpringBootTest` and `MockMvc` to test controller endpoints from request to database level.
- **Git Action:** Can be done separately on branching. Push code, open PR(s), Review and Merge into `develop`.

## Phase 9: Dockerization & CI/CD Pipeline

_Requirement Check:_ GitHub Actions, Dockerfile, Tests run in CI.

- **Assignee:** Member 1
- **Branch:** `feature/ci-cd`
- **Task Details:**
  1. Create a `Dockerfile` to build and run the Spring Boot app.
  2. Sign up on **Render**. Set up the Postgres database.
  3. Create a `.github/workflows/deploy.yml` file. Configure it to run on pushes to `main`.
  4. Provide steps to: Checkout code, Setup Java 17, run `mvn test` (satisfies CI requirement).
  5. Set up Render deployment (using Render's Auto-Deploy hooking with GitHub).
- **Git Action:** Member 1 pushes code, opens PR. Member 2 reviews and merges into `develop`.

## Phase 10: Final Deployment & Documentation

_Requirement Check:_ App deployed publicly, README, Diagrams, clean.

- **Assignee:** Member 2 (with Member 1 assistance)
- **Branch:** Final Pull Request `develop` -> `main`
- **Task Details:**
  1. Open PR from `develop` to `main`. Member 1 reviews and merges.
  2. Watch GitHub Actions run the tests automatically and see the app deploy to Render.
  3. Both members log into Render and verify the deployment is publicly accessible.
  4. Update `README.md` on `main` directly (or via a quick docs branch): Add ER diagram, Architecture diagram, run instructions, and live URL.
  5. Ensure the 5-Minute Demo is prepared successfully.
