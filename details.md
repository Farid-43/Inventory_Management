# Progress Details: Inventory Management Project

This document captures everything completed so far (Phase 1 and Phase 2), including technical decisions, workflow rules, and likely teacher viva questions.

## 1. Current Project Status

- Current working branch: `feature/ci-cd-docker`
- Planning style in use: Shift-Left DevOps
- Completed phases:
  - Phase 1: GitHub setup and branch strategy
  - Phase 2: Dockerization + base CI pipeline
- Test status (latest local run): 2 passed, 0 failed

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
- Result: 2 tests passed, 0 failed.
- Existing test class currently includes:
  - `InventoryManagementApplicationTests.java`

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

- Phase 3 onwards (entities, repositories, services, security implementation, controllers, UI, deployment) are pending.
- Branch protection status checks requirement should be enabled after CI workflow exists and is recognized by GitHub checks list.

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
- Current baseline tests pass (2/2).

## 8. Recommended Next Action (Immediately After This)

- Push `feature/ci-cd-docker` and open PR to `develop`.
- After merge, enforce required status checks in branch protection for `develop` and `main`.
- Then start Phase 3 (`feature/entities-db`).
