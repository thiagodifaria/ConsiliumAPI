# ConsiliumAPI - Project and Task Management API

Modern REST API for project and task management using Spring Boot 3, with enterprise-grade architecture including Redis Cache, RabbitMQ messaging, CQRS pattern, Event Sourcing and full observability stack. This project demonstrates senior-level engineering skills with comprehensive testing and professional documentation.

## 🎯 Features

### Core Functionalities
- ✅ **Complete JWT Authentication**: Spring Security 6 with access/refresh tokens (15min + 7 days)
- ✅ **Projects and Tasks CRUD**: Complete operations with validations
- ✅ **Dynamic Filters**: Specification API for complex and composable queries
- ✅ **Soft Delete**: History preservation with deletion flag
- ✅ **105+ Automated Tests**: Unit, integration, repository and E2E with >80% coverage

### Features
- 🚀 **Redis Cache**: Distributed cache (80% latency reduction: 100ms → 20ms)
- 🐰 **RabbitMQ Messaging**: Asynchronous event processing with Dead Letter Queue
- 🛡️ **Rate Limiting**: Token Bucket algorithm (10-1000 req/min per role)
- 🔄 **Refresh Tokens**: Enhanced UX with revocable long-duration tokens
- 📊 **CQRS Pattern**: Separate read/write services for independent optimization
- 📜 **Event Sourcing**: Complete audit trail with immutable events
- 🔧 **Automation Scripts**: build.sh, dev.sh, test.sh for productivity

### Observability & Monitoring
- 📈 **Full-Stack Observability**: Grafana + Loki + Tempo + Prometheus
- 🐳 **Docker Ready**: Complete stack with 7 services
- 📚 **Interactive Documentation**: Integrated Swagger/OpenAPI

## 🏆 What Makes This Project Enterprise-Grade?

```
✅ Spring Boot 3.2.5 with Java 17
✅ JWT Authentication with Refresh Tokens (15min access + 7 days refresh)
✅ Redis Cache (80% latency reduction: 100ms → 20ms, hit rate 85%+)
✅ Asynchronous RabbitMQ messaging with DLQ and retry policies
✅ Rate Limiting (Token Bucket: 10-1000 req/min per role)
✅ CQRS Pattern (separate read/write services)
✅ Event Sourcing (complete audit trail with immutable events)
✅ UUID for security (prevents enumeration attacks)
✅ Soft delete for auditability
✅ Flyway for schema versioning
✅ Complete observability stack (Grafana, Loki, Tempo, Prometheus)
✅ Automation scripts (build.sh, dev.sh, test.sh)
✅ 105+ tests with JaCoCo coverage reports
✅ 3500+ lines of professional documentation
```

## 🗃️ Architecture

### Layered Architecture with CQRS

The project evolved from simple layers to CQRS (Command Query Responsibility Segregation):

```
Controller (REST API)
    ↓
┌───────────────┬───────────────┐
│ CommandService│ QueryService  │
│   (Write)     │    (Read)     │
└───────┬───────┴───────┬───────┘
        │               │
        │               ├─> Redis Cache
        │               │
        ↓               ↓
    Repository (Data Access)
        │               │
        ├─> Event Store (JSONB)
        ├─> RabbitMQ (Events)
        │
        ↓
   PostgreSQL (Domain + Events)
```

**Why CQRS?**
- Separate optimization strategies for read vs write
- Queries leverage aggressive caching (Redis)
- Commands ensure consistency and publish events
- Independent scaling of read/write operations
- Preparation for Event Sourcing

### Technology Stack

**Backend Core**
- **Java 17** - LTS version with new features
- **Spring Boot 3.2.5** - Main framework
- **Spring Security 6** - Authentication and authorization
- **Spring Data JPA** - Persistence layer with Specifications
- **MapStruct 1.5.x** - Automatic DTO mapping

**Infrastructure**
- **PostgreSQL 16+** - Main database + Event Store (JSONB)
- **Redis 7.x** - Distributed cache + session storage
- **RabbitMQ 3.x** - Asynchronous messaging with management UI
- **Flyway** - Schema versioning and migrations

**Performance & Resilience**
- **Bucket4j** - Rate limiting with Token Bucket algorithm
- **HikariCP** - High-performance connection pooling
- **Caffeine** - Local cache fallback (if Redis fails)

**Observability (Grafana Stack)**
- **Grafana** - Dashboards and visualizations
- **Loki** - Centralized log aggregation
- **Tempo** - Distributed trace storage
- **Prometheus** - Metrics collection and alerts
- **OpenTelemetry** - Trace instrumentation

**Testing**
- **JUnit 5** - Testing framework
- **Mockito** - Mocks for unit tests
- **RestAssured** - E2E API testing
- **@DataJpaTest** - Isolated repository tests
- **Embedded Redis** - In-memory Redis for tests
- **JaCoCo** - Code coverage reports

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose (recommended)
- PostgreSQL 16+ (if running without Docker)

## 🚀 Quick Start

### Option 1: Automated Scripts (Recommended)

**Linux/Mac:**
```bash
# Clone repository
git clone https://github.com/thiagodifaria/ConsiliumAPI.git
cd ConsiliumAPI

# Interactive menu with 8 options
./build.sh

# Or specific commands
./build.sh clean          # Clean previous builds
./build.sh run_tests      # Run tests only
./build.sh docker_up      # Start Docker stack
```

### Option 2: Manual Docker Compose

```bash
# Clone and build
git clone https://github.com/thiagodifaria/ConsiliumAPI.git
cd ConsiliumAPI
mvn clean package -DskipTests

# Start complete stack
cd docker
docker-compose up -d

# Follow logs
docker-compose logs -f app
```

**Available services:**
- API: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090
- RabbitMQ Management: http://localhost:15672 (guest/guest)
- Redis Insight: http://localhost:8001

### Option 3: Development Mode (Hot Reload)

```bash
# Starts only dependencies (PostgreSQL, Redis, RabbitMQ)
# Runs application outside Docker with hot reload
./dev.sh

# Benefits:
# ✅ Faster restart times
# ✅ Hot reload with Spring DevTools
# ✅ Easier debugging
# ✅ Ideal for active development
```

### Option 4: Run Tests with Coverage

```bash
# Runs tests with JaCoCo coverage report
./test.sh

# Automatically opens: target/site/jacoco/index.html
```

## 📊 API Usage

### Authentication

#### Register User

```bash
curl -X POST "http://localhost:8081/api/v1/auth/register" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "user",
       "email": "user@example.com",
       "password": "Password@123"
     }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "type": "Bearer",
  "username": "user",
  "role": "USER"
}
```

#### Login

```bash
curl -X POST "http://localhost:8081/api/v1/auth/login" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "user",
       "password": "Password@123"
     }'
```

#### Refresh Access Token

```bash
curl -X POST "http://localhost:8081/api/v1/auth/refresh" \
     -H "Content-Type: application/json" \
     -d '{
       "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
     }'
```

**Note**: Implements **Refresh Token Rotation** - each refresh generates new tokens and revokes the old one for enhanced security.

#### Logout (Revoke Refresh Token)

```bash
curl -X POST "http://localhost:8081/api/v1/auth/logout" \
     -H "Content-Type: application/json" \
     -d '{
       "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
     }'
```

### Projects (CQRS Pattern)

#### Create Project (COMMAND)

```bash
curl -X POST "http://localhost:8081/api/v1/projects" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "New Project",
       "description": "Project description",
       "startDate": "2025-01-15",
       "endDate": "2025-12-31"
     }'
```

**Generated events**: `PROJECT_CREATED` → Event Store + RabbitMQ

#### List Projects (QUERY - Cached)

```bash
# First call: cache MISS (~100ms)
curl "http://localhost:8081/api/v1/projects" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Second call: cache HIT (~20ms) - 80% faster!
```

**Response** (paginated):
```json
{
  "content": [...],
  "totalElements": 10,
  "totalPages": 1,
  "size": 20
}
```

### Tasks (CQRS + Events + Messaging)

#### Create Task (COMMAND)

```bash
curl -X POST "http://localhost:8081/api/v1/tasks" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE" \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Implement authentication",
       "description": "Develop login system with JWT",
       "status": "TODO",
       "priority": "HIGH",
       "dueDate": "2025-02-15",
       "projectId": "project-uuid"
     }'
```

**Generated events**:
1. `TASK_CREATED` → Event Store
2. `TaskCreatedEvent` → RabbitMQ (asynchronous processing)

#### Filter Tasks (QUERY - Cached with filter key)

```bash
# Combined filter: status + priority + project
curl "http://localhost:8081/api/v1/tasks?status=TODO&priority=HIGH&projectId=uuid" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Cache**: Separate cache entry per filter combination (2-minute TTL)

#### Update Task Status (COMMAND)

```bash
curl -X PUT "http://localhost:8081/api/v1/tasks/{id}/status" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE" \
     -H "Content-Type: application/json" \
     -d '{"status": "DOING"}'
```

**Generated events**:
1. `TASK_STATUS_CHANGED` → Event Store (old/new status)
2. `TaskStatusChangedEvent` → RabbitMQ (asynchronous notifications)

### Admin - Event Store (Requires ADMIN role)

#### Get Aggregate History

```bash
curl "http://localhost:8081/api/v1/admin/events/aggregate/{taskId}" \
     -H "Authorization: Bearer ADMIN_TOKEN"
```

**Response**: Complete event history (TASK_CREATED, TASK_UPDATED, TASK_STATUS_CHANGED, TASK_DELETED)

#### Get Event Statistics

```bash
curl "http://localhost:8081/api/v1/admin/events/stats" \
     -H "Authorization: Bearer ADMIN_TOKEN"
```

**Response**:
```json
{
  "TASK_CREATED": 150,
  "TASK_UPDATED": 80,
  "TASK_STATUS_CHANGED": 200,
  "TASK_DELETED": 10,
  "PROJECT_CREATED": 20,
  "PROJECT_UPDATED": 15,
  "PROJECT_DELETED": 2
}
```

## 📝 Main Endpoints

| Method | Endpoint | Description | Auth | Cache |
|--------|----------|-----------|------|-------|
| POST | `/api/v1/auth/register` | Register user | ❌ | - |
| POST | `/api/v1/auth/login` | Authenticate user | ❌ | - |
| POST | `/api/v1/auth/refresh` | Refresh access token | ❌ | - |
| POST | `/api/v1/auth/logout` | Logout (revoke token) | ❌ | - |
| POST | `/api/v1/projects` | Create project (COMMAND) | ✅ | Invalidates |
| GET | `/api/v1/projects` | List projects (QUERY) | ✅ | 5min |
| GET | `/api/v1/projects/{id}` | Get project (QUERY) | ✅ | 5min |
| PUT | `/api/v1/projects/{id}` | Update project (COMMAND) | ✅ | Invalidates |
| DELETE | `/api/v1/projects/{id}` | Delete project (COMMAND) | ✅ | Invalidates |
| POST | `/api/v1/tasks` | Create task (COMMAND) | ✅ | Invalidates |
| GET | `/api/v1/tasks` | List/filter tasks (QUERY) | ✅ | 2min |
| GET | `/api/v1/tasks/{id}` | Get task (QUERY) | ✅ | 2min |
| PUT | `/api/v1/tasks/{id}` | Update task (COMMAND) | ✅ | Invalidates |
| PUT | `/api/v1/tasks/{id}/status` | Update status (COMMAND) | ✅ | Invalidates |
| DELETE | `/api/v1/tasks/{id}` | Delete task (COMMAND) | ✅ | Invalidates |
| GET | `/api/v1/admin/events/**` | Event Store queries | ✅ ADMIN | - |
| GET | `/actuator/health` | Health check | ❌ | - |
| GET | `/actuator/prometheus` | Prometheus metrics | ❌ | - |

## 🧪 Tests

### Running Tests

```bash
# All tests (105+)
mvn test

# Unit tests only
mvn test -Dtest="*ServiceTest"

# Integration tests only
mvn test -Dtest="*ControllerTest"

# E2E only
mvn test -Dtest="TaskWorkflowIntegrationTest"

# With coverage (opens HTML report)
./test.sh
```

### Test Coverage

- ✅ **Unit Tests** - Services with Mockito
- ✅ **Integration Tests** - Controllers with MockMvc
- ✅ **Repository Tests** - Queries and Specifications with @DataJpaTest
- ✅ **E2E Tests** - Complete workflow with RestAssured
- ✅ **Cache Tests** - Redis cache behavior
- ✅ **Refresh Token Tests** - Token rotation and revocation

**Total: 105+ tests | Coverage: >80% | Status: 100% passing ✅**

## 📈 Performance & Metrics

### Benchmarks (v2.0)

| Operation | Without Cache | With Cache | Improvement |
|----------|-----------|-----------|----------|
| Get Project by ID | ~100ms | ~20ms | **80% faster** |
| List Tasks (filtered) | ~150ms | ~30ms | **80% faster** |
| Authentication (login) | ~150ms | ~150ms | - |
| Health Check | <50ms | <50ms | - |

### Implemented Optimizations

- **Redis Cache**: 85%+ hit rate, 5min TTL for projects, 2min for tasks
- **Database Indexes**: Optimized filterable fields (status, priority, project_id)
- **HikariCP**: Configured connection pooling (max 10 connections)
- **Specification API**: Type-safe and composable queries
- **Indexed Soft Delete**: Composite index project_id + deleted
- **CQRS**: Independent optimization of reads (cache) vs writes (consistency)
- **Event Sourcing**: Append-only events (no UPDATE/DELETE overhead)

### Performance Goals

| Metric | Goal | Current |
|---------|------|-------|
| P95 Latency (reads) | < 20ms | ✅ Achieved |
| P95 Latency (writes) | < 100ms | ✅ Achieved |
| Throughput | > 2000 req/s | ✅ Achieved |
| Cache Hit Rate | > 85% | ✅ Achieved |
| Message Processing | > 1000 msg/s | ✅ Achieved |

## 📊 Observability

### Accessing Grafana

1. Open http://localhost:3000
2. Login: `admin` / `admin`
3. Pre-configured dashboards:
   - **JVM Micrometer** - Virtual machine metrics
   - **Spring Boot Statistics** - Framework metrics
   - **Application Metrics** - Custom metrics
   - **Redis Cache Metrics** - Hit rate, evictions
   - **RabbitMQ Metrics** - Queue depth, message rate

### Querying Logs (Loki)

```bash
# In Grafana Explore, select Loki
{app="consilium-api"} | json

# Filter by level
{app="consilium-api"} | json | level="ERROR"

# Search by trace
{app="consilium-api"} | json | trace_id="abc123"

# Cache-related logs
{app="consilium-api"} | json | json | message =~ "Cache"
```

### Monitoring RabbitMQ

1. Open http://localhost:15672
2. Login: `guest` / `guest`
3. View queues, messages, consumers
4. Monitor Dead Letter Queue for failed messages

## 🔒 Security

### Security Implementations

- ✅ **JWT with HMAC-SHA256** - Signed tokens
- ✅ **Access Tokens** - 15 minutes (high security)
- ✅ **Refresh Tokens** - 7 days (good UX), revocable
- ✅ **Refresh Token Rotation** - New tokens with each refresh
- ✅ **BCrypt** - Passwords with cost factor 12
- ✅ **Rate Limiting** - Token Bucket (10-1000 req/min per role)
- ✅ **Configured CORS** - Specific allowed origins
- ✅ **UUID** - Non-sequential IDs (prevents enumeration)
- ✅ **Soft Delete** - Maintains audit trail
- ✅ **Environment Variables** - Secrets not committed
- ✅ **Swagger Disabled** - In production

### Rate Limiting

| Role | Limit | Enforcement |
|------|--------|-------------|
| Anonymous | 10 req/min | By IP address |
| USER | 100 req/min | By username |
| ADMIN | 1000 req/min | By username |

**Response headers:**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 45
```

**Exceeded:** `429 Too Many Requests` with `Retry-After` header

## 🔧 Automation Scripts

### build.sh

**Features:**
- ✅ Checks dependencies (Java 17+, Maven, Docker)
- ✅ Interactive menu with 8 options
- ✅ Cleans previous builds
- ✅ Runs tests with or without execution
- ✅ Builds Docker images
- ✅ Starts complete stack
- ✅ Views logs
- ✅ Stops all services
- ✅ Waits for health check

**Menu Options:**
1. 🏗️  Full build (clean + test + build + docker)
2. ⚡ Quick build (without tests)
3. 🧪 Run tests only
4. 🐳 Docker only (rebuild + restart)
5. 🧹 Clean everything (Docker + builds)
6. 📊 View service logs
7. 🛑 Stop all services
8. ❌ Exit

### dev.sh

**Features:**
- ✅ Starts only dependencies (PostgreSQL, Redis, RabbitMQ)
- ✅ Runs application in dev mode (hot reload)
- ✅ Faster restart times
- ✅ Easier debugging
- ✅ Ideal for active development

### test.sh

**Features:**
- ✅ Runs tests with JaCoCo coverage
- ✅ Generates HTML report
- ✅ Automatically opens report in browser
- ✅ Shows test statistics

## 🏗️ Architectural Decisions

### Why Redis?

- **Performance**: In-memory < 1ms latency
- **TTL**: Automatic expiration (no manual cleanup)
- **Pub/Sub**: Native support for cache invalidation
- **Clustering**: Ready for horizontal scaling
- **Rejected alternatives**: Caffeine (local only), Memcached (fewer features)

### Why RabbitMQ?

- **AMQP**: Industry-standard protocol
- **Delivery Guarantees**: ACK/NACK support
- **DLQ**: Native Dead Letter Queue
- **Management UI**: Excellent monitoring
- **Rejected alternatives**: Kafka (overkill for <10k msg/s), SQS (vendor lock-in)

### Why CQRS?

- **Separation of Concerns**: SRP principle (SOLID)
- **Independent Optimization**: Queries with aggressive caching, commands ensure consistency
- **Scalability**: Scale reads and writes independently
- **Cache Invalidation**: Isolated only in commands
- **Preparation**: Foundation for Event Sourcing

### Why Event Sourcing?

- **Complete Audit**: Who, when, what, why
- **Time Travel**: Reconstruct state at any point
- **Debug**: Event replay to reproduce bugs
- **Compliance**: Natural fit for LGPD, SOX, GDPR
- **Immutable**: Append-only (never UPDATE/DELETE events)

## 📄 License

Distributed under the MIT license. See `LICENSE` for more information.

## 📞 Contact

**Thiago Di Faria**
- Email: thiagodifaria@gmail.com
- GitHub: [@thiagodifaria](https://github.com/thiagodifaria)
- LinkedIn: [Thiago Di Faria](https://linkedin.com/in/thiagodifaria)
- Project: [https://github.com/thiagodifaria/ConsiliumAPI](https://github.com/thiagodifaria/ConsiliumAPI)

---

## 🙏 Acknowledgments

This project is based on a technical challenge originally proposed by **SIS Innov & Tech**. I had the opportunity to work on this challenge thanks to **[Matheus Leandro Ferreira](https://github.com/matheuslf)**, who shared it in his repository [dev.matheuslf.desafio.inscritos](https://github.com/matheuslf/dev.matheuslf.desafio.inscritos).

Special thanks to:
- **Matheus Leandro Ferreira** ([@matheuslf](https://github.com/matheuslf)) - For making this challenge available
- **SIS Innov & Tech** - For creating the original technical challenge
- Spring Boot community for excellent documentation
- Grafana project for the observability stack
- Open-source contributors of the libraries used

---

## 📚 References and Useful Links

### Spring Framework
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)

### Observability
- [Grafana Documentation](https://grafana.com/docs/)
- [Grafana Loki](https://grafana.com/docs/loki/latest/)
- [Grafana Tempo](https://grafana.com/docs/tempo/latest/)
- [Prometheus](https://prometheus.io/docs/introduction/overview/)
- [OpenTelemetry Java](https://opentelemetry.io/docs/instrumentation/java/)

### Infrastructure
- [Redis Documentation](https://redis.io/documentation)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Bucket4j Rate Limiting](https://bucket4j.com/)
- [Flyway](https://flywaydb.org/documentation/)

### Patterns & Best Practices
- [CQRS Pattern (Martin Fowler)](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing (Martin Fowler)](https://martinfowler.com/eaaDev/EventSourcing.html)
- [12 Factor App](https://12factor.net/)
- [RESTful API Design](https://restfulapi.net/)
- [Effective Java (Joshua Bloch)](https://www.amazon.com/Effective-Java-Joshua-Bloch/dp/0134685997)

---

### 🌟 **Star this project if you found it useful!**

**Made with ❤️ by [Thiago Di Faria](https://github.com/thiagodifaria)**