# 📦 Inventory Management System

A full-stack web application for managing products, orders, and users with role-based access control. Built with **Spring Boot**, **Thymeleaf**, **PostgreSQL**, and **Docker**.

## 🌐 Live Demo

**🔗 [https://inventory-management-znt3.onrender.com](https://inventory-management-znt3.onrender.com)**

### Demo Accounts

| Role   | Username    | Password   |
| ------ | ----------- | ---------- |
| Admin  | admin_demo  | Admin@123  |
| Seller | seller_demo | Seller@123 |
| Buyer  | buyer_demo  | Buyer@123  |

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [ER Diagram](#-er-diagram)
- [Tech Stack](#-tech-stack)
- [API Endpoints](#-api-endpoints)
- [Getting Started](#-getting-started)
- [Running Tests](#-running-tests)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Project Structure](#-project-structure)

---

## ✨ Features

### Role-Based Access Control

- **ADMIN**: Full access - manage users, products, and all orders
- **SELLER**: Manage products and view all orders
- **BUYER**: Browse products, place orders, and manage own orders

### Core Functionality

- 🔐 Secure authentication with BCrypt password encryption
- 📦 Full CRUD operations for Products
- 🛒 Order management with stock validation
- 👥 User management for administrators
- 🧩 Admin role assignment (single or multiple roles) from User Management
- 🖼️ Product image support (file upload or URL)
- 📊 Dashboard with statistics

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                            │
│                    (Thymeleaf Templates)                        │
│   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│   │  Login   │ │Dashboard │ │ Products │ │  Orders  │           │
│   └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER                           │
│  ┌────────────────┐ ┌─────────────────┐ ┌────────────────┐      │
│  │ AuthController │ │ProductController│ │ OrderController│      │
│  └────────────────┘ └─────────────────┘ └────────────────┘      │
│                    ┌────────────────┐                           │
│                    │ ViewController │ (Thymeleaf routes)        │
│                    └────────────────┘                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER                             │
│  ┌─────────────┐  ┌───────────────┐  ┌──────────────┐           │
│  │ AuthService │  │ProductService │  │ OrderService │           │
│  └─────────────┘  └───────────────┘  └──────────────┘           │
│                 ┌───────────────────────┐                       │
│                 │  ImageStorageService  │                       │
│                 └───────────────────────┘                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     REPOSITORY LAYER                            │
│  ┌────────────────┐ ┌─────────────────┐ ┌──────────────────┐    │
│  │ UserRepository │ │ProductRepository│ │ OrderRepository  │    │
│  └────────────────┘ └─────────────────┘ └──────────────────┘    │
│  ┌────────────────┐ ┌─────────────────┐                         │
│  │ RoleRepository │ │OrderItemRepository│                       │
│  └────────────────┘ └─────────────────┘                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE LAYER                             │
│                    ┌───────────────┐                            │
│                    │  PostgreSQL   │                            │
│                    │    (Render)   │                            │
│                    └───────────────┘                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📊 ER Diagram

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│      users      │       │   user_roles    │       │      roles      │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │───┐   │ user_id (FK)    │   ┌───│ id (PK)         │
│ username        │   └──►│ role_id (FK)    │◄──┘   │ name            │
│ email           │       └─────────────────┘       └─────────────────┘
│ password        │              M:M
└────────┬────────┘
         │
         │ 1:M
         ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│     orders      │       │   order_items   │       │    products     │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │───┐   │ id (PK)         │   ┌───│ id (PK)         │
│ order_date      │   └──►│ order_id (FK)   │   │   │ name            │
│ user_id (FK)    │       │ product_id (FK) │◄──┘   │ description     │
└─────────────────┘       │ quantity        │       │ price           │
                          │ unit_price      │       │ stock_quantity  │
                          └─────────────────┘       │ image_url       │
                                 M:1                └─────────────────┘
```

### Entity Relationships

| Relationship        | Type         | Description                             |
| ------------------- | ------------ | --------------------------------------- |
| User ↔ Role         | Many-to-Many | Users can have multiple roles           |
| User → Order        | One-to-Many  | Users can place multiple orders         |
| Order → OrderItem   | One-to-Many  | Orders contain multiple items           |
| Product → OrderItem | One-to-Many  | Products appear in multiple order items |

---

## 🛠 Tech Stack

| Layer                | Technology                                           |
| -------------------- | ---------------------------------------------------- |
| **Backend**          | Spring Boot 3.5, Spring Security, Spring Data JPA    |
| **Frontend**         | Thymeleaf, Bootstrap CSS                             |
| **Database**         | PostgreSQL 16                                        |
| **Security**         | BCrypt password encryption, Role-based authorization |
| **Containerization** | Docker, Docker Compose                               |
| **CI/CD**            | GitHub Actions                                       |
| **Deployment**       | Render (Web Service + PostgreSQL)                    |
| **Testing**          | JUnit 5, Mockito, Spring Boot Test, H2 (test DB)     |

---

## 🔌 API Endpoints

### Authentication

| Method | Endpoint             | Access | Description       |
| ------ | -------------------- | ------ | ----------------- |
| POST   | `/api/auth/register` | Public | Register new user |

### Products

| Method | Endpoint             | Access               | Description       |
| ------ | -------------------- | -------------------- | ----------------- |
| GET    | `/api/products`      | ADMIN, SELLER, BUYER | List all products |
| GET    | `/api/products/{id}` | ADMIN, SELLER, BUYER | Get product by ID |
| POST   | `/api/products`      | ADMIN, SELLER        | Create product    |
| PUT    | `/api/products/{id}` | ADMIN, SELLER        | Update product    |
| DELETE | `/api/products/{id}` | ADMIN, SELLER        | Delete product    |

### Orders

| Method | Endpoint           | Access        | Description     |
| ------ | ------------------ | ------------- | --------------- |
| GET    | `/api/orders`      | ADMIN, SELLER | List all orders |
| GET    | `/api/orders/me`   | BUYER         | List own orders |
| POST   | `/api/orders`      | BUYER         | Place new order |
| DELETE | `/api/orders/{id}` | ADMIN, BUYER  | Cancel order    |

### Users (Admin)

| Method | Endpoint                | Access | Description       |
| ------ | ----------------------- | ------ | ----------------- |
| PUT    | `/api/users/{id}/roles` | ADMIN  | Update user roles |

### Web Pages

| Endpoint       | Access        | Description        |
| -------------- | ------------- | ------------------ |
| `/login`       | Public        | Login page         |
| `/register`    | Public        | Registration page  |
| `/dashboard`   | Authenticated | User dashboard     |
| `/products`    | Authenticated | Product management |
| `/orders`      | ADMIN, SELLER | All orders view    |
| `/orders/me`   | BUYER         | My orders view     |
| `/admin/users` | ADMIN         | User management    |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.9+ (or use included `mvnw`)

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**

   ```bash
   git clone https://github.com/Farid-43/Inventory_Management.git
   cd Inventory_Management
   ```

2. **Create environment file (optional)**

   ```bash
   cp .env.example .env
   # Edit .env with your values
   ```

   Defaults are already defined in `compose.yaml`, so this step can be skipped for quick local runs.

3. **Start the application**

   ```bash
   docker compose up --build
   ```

4. **Access the app**
   - Open http://localhost:8081

### Option 2: Local Development

1. **Start PostgreSQL** (using Docker)

   ```bash
   docker run -d --name postgres \
     -e POSTGRES_DB=inventory \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:16-alpine
   ```

2. **Set environment variables**

   **Bash/Zsh:**

   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/inventory
   export DB_USER=postgres
   export DB_PASSWORD=postgres
   ```

   **Windows cmd:**

   ```cmd
   set DB_URL=jdbc:postgresql://localhost:5432/inventory
   set DB_USER=postgres
   set DB_PASSWORD=postgres
   ```

3. **Run the application**

   ```bash
   ./mvnw spring-boot:run
   ```

   ```cmd
   mvnw.cmd spring-boot:run
   ```

4. **Access the app**
   - Open http://localhost:8081

---

## 🧪 Running Tests

The project includes **49 tests** covering unit tests, integration tests, and data layer tests.

### Run all tests

```bash
./mvnw clean test
```

```cmd
mvnw.cmd clean test
```

### Test categories

| Test File                      | Type        | Coverage                       |
| ------------------------------ | ----------- | ------------------------------ |
| `AuthServiceTest`              | Unit        | Authentication & registration  |
| `ProductServiceTest`           | Unit        | Product CRUD operations        |
| `OrderServiceTest`             | Unit        | Order placement & cancellation |
| `CustomUserDetailsServiceTest` | Unit        | Security user loading          |
| `ControllerIntegrationTests`   | Integration | API endpoints & security       |
| `InventoryDataJpaTests`        | Data JPA    | Repository operations          |

---

## ⚙️ CI/CD Pipeline

### GitHub Actions Workflow

The CI pipeline automatically runs on:

- Pull requests to `develop` and `main`
- Pushes to `develop` and `main`

```yaml
# .github/workflows/ci.yml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - Checkout code
      - Setup Java 17 (Temurin)
      - Run: ./mvnw clean test
```

### Deployment

- **Platform**: Render.com
- **Auto-deploy**: Connected to `main` branch
- **Database**: Render PostgreSQL (managed)

---

## 📁 Project Structure

```
inventory-management/
├── .github/
│   └── workflows/
│       └── ci.yml              # CI pipeline
├── src/
│   ├── main/
│   │   ├── java/com/example/inventory_management/
│   │   │   ├── config/         # WebConfig, DemoDataSeeder
│   │   │   ├── controller/     # REST & View controllers
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── exception/      # Custom exceptions & handler
│   │   │   ├── model/          # JPA Entities
│   │   │   ├── repository/     # Spring Data repositories
│   │   │   ├── security/       # Security configuration
│   │   │   └── service/        # Business logic
│   │   └── resources/
│   │       ├── templates/      # Thymeleaf templates
│   │       ├── static/         # CSS, JS, images
│   │       └── application.yaml
│   └── test/                   # Unit & integration tests
├── Dockerfile                  # Multi-stage Docker build
├── compose.yaml                # Docker Compose configuration
├── .env.example                # Environment template
└── pom.xml                     # Maven dependencies
```

---

## 👥 Contributors

- **Farid-43** - [GitHub Profile](https://github.com/Farid-43)
- **Torikul-048** - [GitHub Profile](https://github.com/Torikul-048)

---

## 📄 License

This project is developed for CSE 3220 Software Engineering Lab.

---

## 🔗 Links

- **Live Application**: https://inventory-management-znt3.onrender.com
- **Repository**: https://github.com/Farid-43/Inventory_Management
