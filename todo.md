# Requirement-Focused TODO (Remaining Work)

Main goal: complete the lab requirements and avoid automatic failure conditions.

## Current Snapshot

- Project runs locally with Spring Boot + PostgreSQL + Docker.
- Role-based access is implemented (ADMIN, SELLER, BUYER).
- REST controllers exist (Auth, Product, Order).
- Tests are passing (latest run summary: 30 total tests).
- CI test workflow exists.

## High Priority (Do These First)

## 1) Add README deliverable (Required for grading)

- [ ] Create `README.md` in project root.
- [ ] Include project description and chosen theme.
- [ ] Include architecture diagram.
- [ ] Include ER diagram.
- [ ] Include API endpoints summary.
- [ ] Include run instructions (`docker compose up --build`, local run, test run).
- [ ] Include CI/CD explanation.
- [ ] Add deployed live URL and repository URL.

Reason: README is explicitly required in deliverables and has direct rubric marks.

## 2) Add deployment automation from `main` to Render

- [ ] Add deploy workflow (new `.github/workflows/deploy.yml` or extend existing workflow).
- [ ] Trigger deployment on push to `main` only.
- [ ] Use GitHub secrets for Render auth/service identifiers.
- [ ] Keep test job required before deployment starts.
- [ ] Verify one successful deployment from `main` and store proof (screenshot/logs).

Reason: Requirement says CI/CD must build, test, and deploy automatically.

## 3) Remove hardcoded credentials from compose/app configs

- [ ] Replace hardcoded values in `compose.yaml` with env expansion:
  - `POSTGRES_DB=${POSTGRES_DB}`
  - `POSTGRES_USER=${POSTGRES_USER}`
  - `POSTGRES_PASSWORD=${POSTGRES_PASSWORD}`
  - `DB_URL=${DB_URL}`
  - `DB_USER=${DB_USER}`
  - `DB_PASSWORD=${DB_PASSWORD}`
- [ ] Add `.env.example` with placeholder values (no real secrets).
- [ ] Keep actual `.env` ignored in `.gitignore`.
- [ ] Re-test `docker compose up --build` after env changes.

Reason: Rubric asks for environment-variable-based config and no hardcoded credentials.

## Medium Priority (Likely Rubric Risk)

## 4) Ensure "CRUD for at least 2 main entities" is unambiguous

Current status:

- Product has full CRUD.
- Order currently has create + reads, but no update/delete API.

Pick one safe path:

- [x] Option A (recommended): add order cancel/delete endpoint for BUYER/ADMIN as allowed by your domain rules.
- [ ] Option B: add full CRUD for another entity (for example admin user management endpoints).

Reason: To avoid deduction from strict CRUD interpretation.

## 5) Strengthen service-layer unit test count to 15+

Current service/security unit tests: 16.

- [x] Add at least 5 more service-layer unit tests (AuthService/ProductService/OrderService).
- [ ] Suggested new test scenarios:
  - OrderService: insufficient stock -> BadRequestException.
  - OrderService: quantity <= 0 -> BadRequestException.
  - OrderService: missing user -> ResourceNotFoundException.
  - ProductService: update preserves image when blank imageUrl.
  - AuthService: invalid role on registration -> BadRequestException (already present; add another edge case).

Reason: Requirement text explicitly asks for minimum 15 unit tests.

## Final Delivery Checks (Before Demo)

## 6) GitHub governance verification (manual settings)

- [ ] `main` protected.
- [ ] Direct push blocked.
- [ ] PR required with at least 1 review.
- [ ] Required status checks enabled (CI test job).

## 7) Deployment proof and demo readiness

- [ ] Public Render URL works.
- [ ] Core flow demo script prepared (Admin, Seller, Buyer).
- [ ] 5-minute demo rehearsal done.
- [ ] Keep fallback local demo plan ready.

## 8) Automatic failure guard checklist

- [ ] Role-based access control present and tested.
- [ ] No direct push to main branch.
- [ ] Dockerization present and working.
- [ ] Tests implemented and passing in CI.
- [ ] App deployed publicly.

## Suggested Execution Order (Fastest to full compliance)

1. README first.
2. Env/credential cleanup.
3. Deploy workflow to Render.
4. CRUD second-entity clarification.
5. Add 5+ service tests.
6. Final validation + screenshots + demo script.
