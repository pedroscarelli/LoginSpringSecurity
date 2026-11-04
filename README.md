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
   git clone https://github.com/pedro-scarelli/LoginSpringSecurity.git
   cd LoginSpringSecurity
   ```

2. Start the application with Docker Compose:
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
- Sends a confirmation e-mail to activate the account.

### 2. Account activation

```bash
GET /v1/user/activate/{USER_ID}
```

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

### 8. Request password reset

```bash
POST /v1/auth/redefine-password/activate
Content-Type: application/json

{
  "email": "{USER_EMAIL}"
}
```
- Sends an e-mail with an OTP code.

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

---

## 📸 Screenshots

![User registration](https://github.com/user-attachments/assets/512f464a-dee8-4265-b9ae-5c91f74d7daa)

![Password reset OTP](https://github.com/user-attachments/assets/6201f6af-e628-4d66-b8e3-c4344ac99455)

---
