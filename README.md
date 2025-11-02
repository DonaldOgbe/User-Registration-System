# User Registration System

A simple Spring Boot web application that handles user registration. It demonstrates basic usage of Spring components like controllers, services, and dependency injection, while also introducing test-driven development practices.

## 🛠️ Features

- Register new users via a RESTful endpoint
- Welcome email sent after registration
- In-memory data storage using a map
- Clean separation of concerns (Controller, Service, Repository, Model)
- TDD with unit tests

## 💻 API Reference

### Register User

```http
POST /users/register
```

**Request Body:**

```json
{
  "name": "Alice",
  "email": "alice@example.com"
}
```

**Response:**

```
Alice has been registered !
```

## 🔧 Run Locally

```bash
# Build and run the application
./mvnw spring-boot:run
```

## 🧬 Running Tests

```bash
./mvnw test
```

---

