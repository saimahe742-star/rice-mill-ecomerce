# Modern Rice Mill — Online Delivery System

Full-stack e-commerce demo: **Spring Boot 3** (REST + session login), **MySQL** + **JPA/Hibernate**, static **HTML/CSS/JavaScript** UI with **Bootstrap 5**.

## Features (summary)

- Product catalog with hover effects and **spark animation** on click; click adds to **session cart**.
- **Register / login** (BCrypt passwords, HTTP session).
- **Checkout** with **Cash on Delivery** or **QR / UPI** (sample QR image in `static/images`).
- **Order tracking** with **5s polling** for status updates.
- **Return & feedback** (reason, 1–5 rating, comments) stored in `feedback` table.
- **Admin dashboard**: all orders (update status: Pending / Shipped / Delivered, mark item received, delete order), full **product CRUD**, **user CRUD** (list/edit role & email, reset password, delete with safeguards).
- **My account**: update name or password (`/account.html`), delete own account when no orders/feedback.
- **Email to admin** on new order when `app.mail.enabled=true` and SMTP is configured; **SMS simulated** in application logs.
- Footer with business **phone, email, address, hours**.

## Project structure

```
rice-mill-ecommerce/
├── pom.xml
├── README.md
├── database/
│   ├── schema.sql          # Reference DDL for MySQL
│   └── seed.sql            # Optional product INSERTs (matches app seed)
└── src/main/
    ├── java/com/modernricemill/
    │   ├── RiceMillApplication.java
    │   ├── api/            # REST controllers
    │   ├── config/         # Security, MVC, seed data
    │   ├── dto/
    │   ├── entity/
    │   ├── repository/
    │   ├── security/
    │   ├── service/
    │   └── web/            # Global exception handler
    └── resources/
        ├── application.properties
        └── static/         # HTML, CSS, JS, sample images
            ├── index.html, login.html, register.html, account.html
            ├── products.html, cart.html, checkout.html
            ├── tracking.html, feedback.html, admin.html
            ├── css/styles.css
            ├── js/layout.js, spark.js
            └── images/*.svg
```

## Prerequisites

- **JDK 17+**
- **Maven 3.9+**
- **MySQL 8+** (user/password with rights to create DB)

## Run locally (step by step)

1. **Create the database** (optional if using `createDatabaseIfNotExist` in JDBC URL):

   ```sql
   CREATE DATABASE rice_mill_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

   Optional: run `database/schema.sql` then `database/seed.sql` to create tables and load products without relying on Hibernate seed (set `spring.jpa.hibernate.ddl-auto=none` if you manage schema entirely via SQL).

2. **Configure MySQL** in `src/main/resources/application.properties`:

   - `spring.datasource.username`
   - `spring.datasource.password`

3. **Build and run** from the project root:

   ```bash
   mvn spring-boot:run
   ```

4. Open **http://localhost:8080/**

### Demo accounts (seeded on first run)

| Role     | Email                 | Password     |
|----------|------------------------|--------------|
| Admin    | `admin@ricemill.local` | `admin123`   |
| Customer | `customer@ricemill.local` | `customer123` |

Products are **seeded automatically** on first startup (all items from the specification).

### Email notifications (optional)

In `application.properties` or environment variables:

- `MAIL_ENABLED=true`
- `MAIL_USERNAME=your-gmail@gmail.com`
- `MAIL_PASSWORD=your-16-char-app-password`

Admin notification address is `app.admin.email` (default: `deviricemill@gmail.com`). If mail is disabled, the full email body is **logged** instead.

> **Security note:** CSRF is **disabled** for simpler static-page + REST usage. For production, enable CSRF (e.g. cookie token) or move to token-based APIs.

## Deploying

### Option A — Run as executable JAR (simplest)

```bash
mvn clean package -DskipTests
java -jar target/rice-mill-delivery-1.0.0.jar
```

Set environment variables on the host for datasource and mail (e.g. `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).

### Option B — Apache Tomcat (WAR)

1. Add to `pom.xml` inside `<project>`:

   ```xml
   <packaging>war</packaging>
   ```

2. Change the main class to extend `SpringBootServletInitializer` and override `configure` (Spring Boot docs: “Deploying a Spring Boot application”).

3. Mark embedded Tomcat as `provided`:

   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-tomcat</artifactId>
     <scope>provided</scope>
   </dependency>
   ```

4. `mvn clean package` and copy `target/*.war` to Tomcat `webapps/`. Configure JNDI or system properties for the MySQL datasource.

### Option C — Render / Railway

1. Push the project to GitHub.
2. Create a **MySQL** add-on (or use external MySQL).
3. New **Web Service** with build command `mvn clean package -DskipTests` and start command  
   `java -jar target/rice-mill-delivery-1.0.0.jar`.
4. Set environment variables: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and optionally mail variables.
5. Ensure the platform exposes **port 8080** (or set `SERVER_PORT`).

### Option D — AWS (high level)

- **EC2**: install JDK + MySQL client; run the JAR behind **systemd**; use **ALB** + **HTTPS**.
- **RDS**: MySQL instance; point `SPRING_DATASOURCE_URL` to the RDS endpoint.
- **Elastic Beanstalk** or **ECS Fargate**: package the JAR in a Docker image or use the Java platform; inject env vars from **Secrets Manager** / **SSM**.

## REST API overview

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/products` | List products |
| POST | `/api/auth/register` | Register |
| GET | `/api/auth/me` | Current user (or anonymous) |
| GET / PATCH / DELETE | `/api/users/me` | Profile / update / delete own account (authenticated) |
| GET/POST | `/api/cart`, `/api/cart/add`, … | Session cart |
| POST | `/api/checkout` | Place order (auth) |
| GET | `/api/orders/mine`, `/api/orders/{id}` | Customer orders |
| POST | `/api/feedback` | Return / feedback |
| GET/PATCH/DELETE | `/api/admin/orders`, … | Admin |
| POST/PUT/DELETE | `/api/admin/products`, … | Admin |
| GET / GET id / PATCH / DELETE | `/api/admin/users`, `/api/admin/users/{id}` | Admin user management |

Form login: `POST /login` with `username` (email) and `password`. Logout: `GET /logout`.

## License

Educational / demonstration use.
