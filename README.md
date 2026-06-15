# Repairo — Backend

Repairo is a car repair marketplace where car owners post repair leads and nearby repair shops submit quotes. The platform handles real-time quote delivery, escrow-based payments, Firebase chat, and an admin panel for full oversight.

---

## Tech Stack

- Java 17, Spring Boot 3
- PostgreSQL + PostGIS (geospatial queries)
- Redis (OTP, rate limiting)
- Firebase Admin SDK (FCM notifications + Firestore chat)
- AWS S3 (document and image storage)
- WebSocket with STOMP/SockJS (real-time quotes)
- Flyway (database migrations)
- JWT with rotating refresh tokens
- Docker

---

## Roles

- CAR_OWNER — posts leads, accepts quotes, makes payments
- SHOP_OWNER — views nearby leads, submits quotes, receives payouts
- ADMIN — manages users, shops, leads, disputes via admin panel

---

## System Architecture

```mermaid
graph TD
    Android[Android App] -->|REST APIS + WebSocket| Backend[Spring Boot Backend]
    AdminPanel[Next.js Admin Panel] -->|REST APIS| Backend
    Backend -->|JPA| PostgreSQL[(PostgreSQL + PostGIS)]
    Backend -->|Cache + OTP| Redis[(Redis)]
    Backend -->|Notifications| FCM[Firebase FCM]
    Backend -->|Chat| Firestore[Firebase Firestore]
    Backend -->|File Storage| S3[AWS S3]
    Backend -->|OTP Emails| Gmail[Gmail SMTP]
```

---

## Car Owner Flow

```mermaid
sequenceDiagram
    participant CO as Car Owner
    participant BE as Backend
    participant Redis
    participant Gmail

    CO->>BE: POST /auth/signup
    BE->>Redis: Save OTP (TTL 10 min)
    BE->>Gmail: Send OTP email
    CO->>BE: POST /auth/verify-otp
    BE->>CO: JWT + Refresh Token

    CO->>BE: POST /leads (location, car details, images)
    BE->>CO: Lead created

    BE-->>CO: FCM — new quote received
    CO->>BE: GET /leads/{id}/quotes
    CO->>BE: POST /quotes/{quoteId}/accept

    CO->>BE: POST /api/payments/initiate
    BE->>CO: Mock payment URL

    CO->>BE: GET /api/payments/{paymentId}/mock-pay
    BE->>BE: Escrow status = FUNDS_RECEIVED

    CO->>BE: POST /api/payments/{paymentId}/release-immediately
    BE->>BE: Escrow status = RELEASED_TO_SHOP
```

---

## Shop Owner Flow

```mermaid
sequenceDiagram
    participant SO as Shop Owner
    participant BE as Backend
    participant Redis
    participant FCM

    SO->>BE: POST /auth/shop/request-otp
    BE->>Redis: Save OTP
    SO->>BE: POST /auth/shop/verify-otp
    BE->>SO: JWT + Refresh Token

    SO->>BE: POST /shops/documents (CNIC, business docs)
    BE->>SO: Pending approval

    Note over SO,BE: Admin approves shop

    SO->>BE: GET /repair-shop/leads/nearby (lat, lng, radius)
    BE->>SO: Nearby open leads (PostGIS query)

    SO->>BE: POST /quotes (price, message)
    BE->>FCM: Notify car owner

    Note over SO,BE: Car owner accepts quote

    BE-->>SO: FCM — quote accepted
    SO->>BE: POST /leads/{leadId}/mark-work-done
    BE->>BE: Job progress updated
```

---

## Escrow Payment Flow

```mermaid
sequenceDiagram
    participant CO as Car Owner
    participant BE as Backend
    participant SO as Shop Owner

    CO->>BE: POST /api/payments/initiate
    BE->>BE: escrowStatus = INITIATED

    CO->>BE: GET /api/payments/{paymentId}/mock-pay
    BE->>BE: escrowStatus = FUNDS_RECEIVED

    SO->>BE: POST /leads/{leadId}/mark-work-done
    BE->>BE: Job step = SHOP_MARKED_DONE

    CO->>BE: POST /api/payments/{paymentId}/release-immediately
    BE->>BE: escrowStatus = RELEASED_TO_SHOP
    BE->>BE: ShopPayout created

    Note over BE: EscrowReleaseJob runs every hour
    Note over BE: Auto-releases if eligibleReleaseAt passed
```

---

## OTP Auth Flow

```mermaid
sequenceDiagram
    participant User
    participant BE as Backend
    participant Redis
    participant Gmail

    User->>BE: POST /auth/signup (email, phone, name)
    BE->>Redis: pending_user:<email> (TTL 10 min)
    BE->>Redis: otp:<email> (TTL 10 min)
    BE->>Gmail: Send OTP

    User->>BE: POST /auth/verify-otp
    BE->>Redis: Validate OTP
    BE->>BE: Create user in DB
    BE->>User: JWT + Refresh Token

    User->>BE: POST /auth/refresh
    BE->>BE: Rotate refresh token
    BE->>User: New JWT + Refresh Token
```

---

## Prerequisites

- Java 17
- Docker (for PostgreSQL + Redis)
- Firebase project with FCM and Firestore enabled
- AWS S3 bucket
- Gmail account with App Password enabled

---

## Environment Variables

Create an `application.properties` or use environment variables:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/carrepair
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_pass

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET=your_jwt_secret
JWT_EXPIRY_MS=900000
JWT_REFRESH_EXPIRY_MS=604800000

AWS_ACCESS_KEY=your_key
AWS_SECRET_KEY=your_secret
AWS_S3_BUCKET=carrepair-media
AWS_REGION=your_region

FIREBASE_CREDENTIALS_PATH=path/to/firebase-adminsdk.json

MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_app_password
```

---

## How to Run

Start PostgreSQL and Redis with Docker:

```bash
docker run --name carrepair-db -e POSTGRES_PASSWORD=yourpass -e POSTGRES_DB=carrepair -p 5432:5432 -d postgres
docker run --name carrepair-redis -p 6379:6379 -d redis
```

Run the backend:

```bash
./mvnw spring-boot:run
```

Backend runs on `http://localhost:8080`

---

## API Endpoints

**Auth**
```
POST   /auth/signup
POST   /auth/verify-otp
POST   /auth/resend-otp
POST   /auth/login
POST   /auth/refresh
POST   /auth/logout
POST   /auth/shop/request-otp
POST   /auth/shop/verify-otp
POST   /admin/auth/login
```

**Users**
```
GET    /users/me
POST   /users/fcm-token
GET    /admin/users
GET    /admin/users/counts
GET    /admin/users/{id}
POST   /admin/users/{id}/block
POST   /admin/users/{id}/unblock
```

**Shops**
```
POST   /shops/documents
GET    /shops/my-status
GET    /shops/credits
POST   /shops/credits/purchase
GET    /repair-shop/leads/nearby
GET    /admin/shops
GET    /admin/shops/counts
GET    /admin/shops/{id}
GET    /admin/shops/pending
POST   /admin/shops/{id}/approve
POST   /admin/shops/{id}/reject
```

**Leads**
```
POST   /leads
GET    /leads/my
GET    /leads/{id}
PATCH  /leads/{id}/cancel
GET    /leads/{leadId}/chat-channel
GET    /leads/{leadId}/progress
POST   /leads/{leadId}/mark-work-done
GET    /leads/{leadId}/accepted-detail
GET    /admin/leads
GET    /admin/leads/{id}
```

**Quotes**
```
POST   /quotes
GET    /leads/{id}/quotes
POST   /leads/{leadId}/quotes/{quoteId}/accept
GET    /quotes/my
```

**Payments**
```
POST   /api/payments/initiate
GET    /api/payments/{paymentId}/mock-pay
GET    /api/payments/leads/{leadId}
POST   /api/payments/{paymentId}/release-immediately
```

**Chat**
```
POST   /chat/notify
```

**Media**
```
POST   /media/presigned-url
```

**Dashboard**
```
GET    /admin/dashboard/stats
GET    /admin/dashboard/recent-shops
GET    /admin/analytics
GET    /admin/analytics/export
```

**Activity**
```
GET    /admin/activity/recent
GET    /admin/activity
```

**WebSocket**
```
Endpoint : /ws (STOMP over SockJS)
Topic    : /topic/leads/{leadId}/quotes
```

---

## Key Decisions

- OTP via Gmail SMTP, not AWS SES
- Geospatial lead matching via PostGIS
- Chat via Firebase Firestore, not a third-party SDK
- Admin tokens stored in httpOnly cookies, all admin API calls proxied through Next.js
- JWT with rotating refresh tokens
- User block revokes all refresh tokens and triggers FCM notification instantly

---
