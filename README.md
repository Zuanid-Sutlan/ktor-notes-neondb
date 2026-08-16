# Ktor Notes API (Clean Architecture)

A secure, scalable, and fully documented Notes Management API built with Ktor, Kotlin, and Exposed ORM. This project follows **Clean Architecture** principles to ensure a clear separation of concerns between business logic, data persistence, and the presentation layer.

## 🚀 Key Features

### 🔐 Authentication & Account Management
- **Registration**: Immediate user creation with a verified status flag. Returns JWT tokens and user profile instantly.
- **2FA Login**: Secure two-step login. Password verification followed by an email OTP code.
- **JWT Authorization**: Stateless security using Access (1h) and Refresh (30d) tokens.
- **Account Deletion Cooldown**: 30-day "soft delete" period. Users can restore their accounts during this window.
- **Password Reset**: Full "Forgot Password" flow with email OTP verification.
- **Real Email Delivery**: Integrated with **Resend API** for reliable OTP and notification delivery.

### 📝 Notes Management
- **Full CRUD**: Create, Read, Update, and Delete notes.
- **Data Isolation**: Users can only access and manage their own notes.
- **Pagination**: Optimized `GET /notes` endpoint with `page` and `pageSize` support.

### 🛠 Developer Experience
- **Interactive Documentation**: Full Swagger UI and OpenAPI 3.0 specification.
- **Automated Testing**: Comprehensive integration test suite using H2 in-memory database.
- **Environment Safety**: Credentials managed via `.env` files (git-ignored).

---

## 🛠 Tech Stack

- **Framework**: [Ktor 3.x](https://ktor.io/)
- **Language**: [Kotlin 2.x](https://kotlinlang.org/)
- **ORM**: [Exposed](https://jetbrains.github.io/Exposed/)
- **Database**: PostgreSQL (Neon.tech for production, H2 for testing)
- **Connection Pool**: [HikariCP](https://github.com/brettwooldridge/HikariCP)
- **Authentication**: JWT (JSON Web Tokens)
- **Hashing**: BCrypt
- **Email**: Resend HTTP API
- **Documentation**: Swagger UI / OpenAPI

---

## 🏗 Architecture Overview

The project is organized into three main layers:
1. **Domain**: Pure business logic. Entities (`Note`, `User`) and Repository Interfaces.
2. **Data**: Infrastructure and implementation. Database schemas (`Table` objects), Repository implementations, and External Services (Email).
3. **Presentation**: API handling. Ktor Routes, JWT configuration, and Response standardizers.

---

## ⚙️ Setup & Installation

### 1. Prerequisites
- JDK 21 or higher
- [Resend API Key](https://resend.com/) (Free tier available)
- PostgreSQL database (Local or [Neon.tech](https://neon.tech/))

### 2. Environment Configuration
Create a `.env` file in the project root and add the following credentials:

```env
# Database Configuration
DB_JDBC_URL=jdbc:postgresql://your-host:5432/your-db?sslmode=require
DB_USER=your_db_user
DB_PASSWORD=your_db_password
DB_DRIVER=org.postgresql.Driver

# Security
JWT_SECRET=your_random_secure_secret_key

# Email Service (Resend)
RESEND_API_KEY=re_your_api_key
EMAIL_FROM=onboarding@resend.dev
```

### 3. Build the Project
```bash
./gradlew build
```

---

## 🏃 Running the App

Start the Ktor server:
```bash
./gradlew run
```
The server will be available at `http://localhost:8080`.

---

## 🧪 Running Tests

The project includes automated integration tests that use an in-memory database. You don't need a real database to run these.

```bash
./gradlew test
```

---

## 📖 API Documentation

Once the server is running, you can explore the API and test endpoints directly from your browser:

- **Swagger UI**: [http://localhost:8080/swagger](http://localhost:8080/swagger)
- **OpenAPI Spec**: [http://localhost:8080/openapi](http://localhost:8080/openapi)

### How to test protected routes in Swagger:
1. Use `/auth/register` or `/auth/login-verify` to get an `accessToken`.
2. Click the **"Authorize"** button at the top of the Swagger UI.
3. Enter `Bearer <your_token>` and click Authorize.
4. Now you can use the Notes endpoints!

---

## 📁 Project Structure

```text
src/
├── main/
│   ├── kotlin/com/dreamcode/
│   │   ├── domain/           # Business Logic & Entities
│   │   ├── data/             # DB Logic & Implementations
│   │   ├── presentation/     # API Routes & Configuration
│   │   └── Routing.kt        # Main Route Registry
│   └── resources/
│       ├── openapi/          # API Specification
│       └── application.yaml  # Ktor Config
└── test/                     # Integration Tests
```
