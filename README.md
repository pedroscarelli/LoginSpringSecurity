**LoginSpringSecurity**

Authentication and user management API built with Java, Spring Boot and Spring Security. It offers professional features such as soft delete, roles and permissions, sending e-mails via SMTP and more.

---

## 🔧 Technologies

- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT
- Flyway (migrations)
- Docker & Docker Compose
- Swagger
- SMTP

---

## 🚀 Features

- User registration with e-mail activation
- Login with JWT
- User soft delete
- Role and permission management
- Password reset via OTP (code sent by e-mail)
- Paginated user listing (Admin only)
- User CRUD

---

## 📦 Prerequisites

- Docker
- Docker Compose
- Git

---

## ⚙️ Setup and running

1. Clone this repository and navigate to the project folder:
   ```bash
   git clone https://github.com/pedroscarelli/LoginSpringSecurity.git
   cd LoginSpringSecurity
   ```

2. Create your `.env` and set a real `JWT_SECRET` (at least 32 characters) and your SMTP credentials:
   ```bash
   cp .env.example .env
   ```

3. Start the application with Docker Compose:
   ```bash
   docker compose up --build
   ```

The API will be available at `http://localhost:8080`.

---
## 📋 Documentation

Swagger link: http://localhost:8080/swagger-ui/index.html

## Endpoints

### 1. User registration

```bash
POST /v1/user
Content-Type: application/json

{
  "name": "{USER_NAME}",
  "email": "{USER_EMAIL}",
  "password": "{USER_PASSWORD}"
}
```
- Returns `201`, or `409` if the e-mail is already registered.
- Sends a confirmation e-mail with a one-time activation link.

### 2. Account activation

```bash
GET /v1/user/activate/{ACTIVATION_TOKEN}
```
- The token is a random value sent only by e-mail, and it can be used once.

### 3. Login

```bash
POST /v1/auth/login
Content-Type: application/json

{
  "email": "{USER_EMAIL}",
  "password": "{USER_PASSWORD}"
}
```
- For accounts created by Flyway, use the default password: `senha123`.
- Returns `401` for a wrong e-mail or password, and `403` if the account has not been activated yet.

### 4. List users (Admin only)

```bash
GET /v1/user?page={PAGE_NUMBER}&items={ITEM_COUNT}
Authorization: Bearer {JWT_TOKEN}
```

### 5. Get user

```bash
GET /v1/user/{USER_ID}
Authorization: Bearer {JWT_TOKEN}
```

### 6. Update user

```bash
PATCH /v1/user/{USER_ID}
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}

{
  "name": "{NEW_NAME}",
  "password": "{NEW_PASSWORD}"
}
```

### 7. Delete user (soft delete)

```bash
DELETE /v1/user/{USER_ID}
Authorization: Bearer {JWT_TOKEN}
```
- Marks the user as deleted and removes them from searches.
- Users can only read, update or delete their own account (`403` otherwise). Admins can manage any account.

### 8. Request password reset

```bash
POST /v1/auth/redefine-password/activate
Content-Type: application/json

{
  "email": "{USER_EMAIL}"
}
```
- Sends an e-mail with a 6-digit OTP code, valid for 5 minutes.
- Always returns success, so the endpoint can't be used to find out which e-mails are registered.

### 9. Reset password

```bash
PATCH /v1/auth/redefine-password
Content-Type: application/json

{
  "email": "{USER_EMAIL}",
  "otpCode": "{OTP_CODE}",
  "newPassword": "{NEW_PASSWORD}"
}
```
- The code is locked after 5 wrong attempts; request a new one.

---

## 🧪 Running the tests

```bash
mvn test
```

Unit tests cover login, activation, the OTP flow, partial updates, JWT validation and the security filter.

---

## 📸 Screenshots

![User registration](https://github.com/user-attachments/assets/512f464a-dee8-4265-b9ae-5c91f74d7daa)

![Password reset OTP](https://github.com/user-attachments/assets/6201f6af-e628-4d66-b8e3-c4344ac99455)

---
