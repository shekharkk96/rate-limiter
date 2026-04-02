# Distributed Rate Limiter — Token Bucket Algorithm

A **distributed rate limiter** built with **Spring Boot** and **Redis**, implementing the **Token Bucket** algorithm. It limits the number of API requests a client can make per second using an API key, making it suitable for production-grade API throttling.

---

## 📐 How It Works

Each unique API key (passed via the `X-API-KEY` header) gets its own **token bucket** stored in Redis.

- **Bucket Capacity:** 10 tokens
- **Refill Rate:** 1 token per second
- On each request, one token is consumed from the bucket.
- If the bucket is empty, the request is **rejected with HTTP 429 (Too Many Requests)**.
- Tokens refill automatically over time based on elapsed seconds since the last request.

```
Client Request → Check Redis bucket for API key
                     ↓
            Tokens available?
           /               \
         YES                NO
          ↓                  ↓
   Consume 1 token     Return 429
   Return 200 OK       "Rate limit exceeded"
```

---

## 🏗️ Project Structure

```
rate-limiter/
├── docker-compose.yml                          # Redis Docker setup
├── pom.xml                                     # Maven dependencies
├── rate-limiter.postman_collection.json        # Postman test collection
└── src/
    └── main/
        ├── java/com/shekhar/rate_limiter/
        │   ├── RateLimiterApplication.java          # Spring Boot entry point
        │   ├── config/
        │   │   └── RedisConfiguration.java          # Redis (Lettuce) configuration
        │   ├── controller/
        │   │   └── RateLimiterController.java       # REST API controller
        │   ├── model/
        │   │   └── RateLimiterResponse.java         # Response model
        │   ├── redis/
        │   │   └── RedisTokenBucketRepository.java  # Token bucket logic (Redis)
        │   └── service/
        │       └── RateLimiterService.java          # Business logic
        └── resources/
            └── application.properties              # App configuration
```

---

## 🧰 Tech Stack

| Technology       | Version   | Purpose                          |
|-----------------|-----------|----------------------------------|
| Java            | 17        | Language                         |
| Spring Boot     | 3.5.13    | Web framework                    |
| Spring Data Redis | —       | Redis integration                |
| Lettuce         | —         | Redis client (default with SDR)  |
| Redis           | latest    | Token bucket state storage       |
| Lombok          | —         | Boilerplate reduction            |
| Maven           | —         | Build & dependency management    |
| Docker / Docker Compose | — | Running Redis locally           |

---

## ✅ Prerequisites

Make sure the following are installed before setting up the project:

- [Java 17+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/download.cgi) *(or use the included `mvnw` wrapper)*
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) *(for running Redis)*
- [Postman](https://www.postman.com/downloads/) *(optional, for testing)*

---

## 🚀 Setup & Run

### Step 1 — Clone the Repository

```bash
git clone <your-repo-url>
cd rate-limiter
```

### Step 2 — Start Redis with Docker

A `docker-compose.yml` file is included to spin up a Redis instance on the default port `6379`.

```bash
docker-compose up -d
```

Verify Redis is running:

```bash
docker ps
```

You should see a Redis container listed and running.

### Step 3 — Build the Application

Using the Maven wrapper (no Maven installation required):

```bash
# Linux / macOS
./mvnw clean install

# Windows (PowerShell)
.\mvnw.cmd clean install
```

### Step 4 — Run the Application

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

The application will start on **`http://localhost:8080`**.

---

## 🔌 API Reference

### `GET /api/data`

A protected endpoint that enforces rate limiting per API key.

**Request Header:**

| Header      | Required | Description                          |
|-------------|----------|--------------------------------------|
| `X-API-KEY` | ✅ Yes   | Unique identifier for the API client |

**Responses:**

| HTTP Status | Body                    | Meaning                          |
|-------------|-------------------------|----------------------------------|
| `200 OK`    | `Success`               | Request allowed, token consumed  |
| `429 Too Many Requests` | `Rate limit exceeded` | Bucket is empty, request denied |
| `400 Bad Request` | *(Spring default)* | `X-API-KEY` header is missing |

---

## 🧪 Testing

### Option 1 — Using cURL

**Single request (allowed):**

```bash
curl -H "X-API-KEY: user123" http://localhost:8080/api/data
```

**Spam requests to trigger rate limiting:**

```bash
for i in {1..15}; do curl -s -o /dev/null -w "%{http_code}\n" -H "X-API-KEY: user123" http://localhost:8080/api/data; done
```

*On Windows PowerShell:*

```powershell
1..15 | ForEach-Object { Invoke-WebRequest -Uri "http://localhost:8080/api/data" -Headers @{"X-API-KEY"="user123"} -UseBasicParsing | Select-Object -ExpandProperty StatusCode }
```

**Request without API key (expect 400):**

```bash
curl http://localhost:8080/api/data
```

### Option 2 — Using Postman

A Postman collection is included: **`rate-limiter.postman_collection.json`**

1. Open **Postman**.
2. Click **Import** → select `rate-limiter.postman_collection.json`.
3. The collection includes three ready-to-use requests:

| Request Name                  | Description                                       |
|-------------------------------|---------------------------------------------------|
| **Call Protected API**        | Single request with `X-API-KEY: user123`          |
| **Call API Without API Key**  | Request without header — expects `400`            |
| **Spam 15 Requests**          | Sends 15 rapid requests to trigger the rate limit |

### Option 3 — Run Unit Tests

```bash
# Linux / macOS
./mvnw test

# Windows (PowerShell)
.\mvnw.cmd test
```

---

## ⚙️ Configuration

The application uses default Redis connection settings (localhost:6379). You can override them in `src/main/resources/application.properties`:

```properties
spring.application.name=rate-limiter

# Redis connection (defaults shown)
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**Rate limiter settings** (configured in `RateLimiterService.java`):

| Parameter       | Default | Description                            |
|-----------------|---------|----------------------------------------|
| `BUCKET_CAPACITY` | `10`  | Max tokens per bucket (burst limit)    |
| `REFILL_RATE`     | `1`   | Tokens added per second                |

---

## 🐳 Stopping Redis

When done, stop the Redis Docker container:

```bash
docker-compose down
```

---

## 📋 Example Interaction

```
Request 1-10  →  200 OK      "Success"
Request 11    →  429         "Rate limit exceeded"
...wait 1 second...
Request 12    →  200 OK      "Success"   (1 token refilled)
```

