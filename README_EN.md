# ConsiliumAPI - Project and Task Management API

Modern REST API for project and task management using Spring Boot 3, JWT Authentication and complete observability stack. This project offers an enterprise-grade solution with layered architecture, comprehensive testing and professional documentation.

## 🎯 Features

- ✅ **Complete JWT Authentication**: Robust system with Spring Security 6 and BCrypt
- ✅ **Projects and Tasks CRUD**: Complete operations with validations
- ✅ **Dynamic Filters**: Specification API for complex and composable queries
- ✅ **Soft Delete**: History preservation with deletion flag
- ✅ **Full-Stack Observability**: Grafana + Loki + Tempo + Prometheus
- ✅ **62 Automated Tests**: Unit, integration, repository and E2E
- ✅ **Docker Ready**: Complete stack configured
- ✅ **Interactive Documentation**: Integrated Swagger/OpenAPI

## 🗃️ Architecture

### Layered Architecture

The project follows a well-defined layered architecture, chosen for its simplicity and familiarity with the Spring ecosystem:

```
Controller (REST API)
    ↓
Service (Business Logic)
    ↓
Repository (Data Access)
    ↓
Entity (Domain Model)
```

**Why layers instead of Hexagonal?**

I opted for layers because for this project's scope:
- It's simpler and more straightforward
- Reduces configuration overhead
- Maintains clarity and productivity
- It's widely known in the community

### Technology Stack

**Backend Core**
- **Java 17** - LTS version with new features
- **Spring Boot 3.2.5** - Main framework
- **Spring Security 6** - Authentication and authorization
- **Spring Data JPA** - Persistence layer
- **MapStruct 1.5.x** - Automatic DTO mapping

**Database**
- **PostgreSQL** - Main database for production
- **H2** - In-memory database for fast tests
- **Flyway** - Schema versioning and migrations

**Observability (Grafana Stack)**
- **Grafana** - Dashboards and visualizations
- **Loki** - Centralized log aggregation
- **Tempo** - Distributed trace storage
- **Prometheus** - Metrics collection
- **OpenTelemetry** - Trace instrumentation

**Testing**
- **JUnit 5** - Testing framework
- **Mockito** - Mocks for unit tests
- **RestAssured** - E2E API tests
- **@DataJpaTest** - Isolated repository tests

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose (recommended)
- PostgreSQL 16+ (if running without Docker)

## 🚀 Quick Start

### Development with Docker Compose

The fastest way to run the complete project with the entire observability stack:

```bash
# Clone repository
git clone https://github.com/thiagodifaria/ConsiliumAPI.git
cd ConsiliumAPI

# Build project
mvn clean package -DskipTests

# Start complete stack
cd docker
docker-compose up -d

# Follow logs
docker-compose logs -f app
```

**Available services:**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090

### Local Development (Without Docker)

```bash
# Create database
createdb consilium

# Configure variables (optional)
export DB_HOST=localhost
export DB_NAME=consilium
export DB_USER=postgres
export DB_PASSWORD=postgres

# Run application
mvn spring-boot:run
```

### Test Profile (H2 in memory)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

## ⚙️ Configuration

### Environment Variables

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/consilium
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# JWT
JWT_SECRET=your-secret-key-minimum-256-bits-change-in-production
JWT_EXPIRATION=86400000

# Observability (default URLs for Docker)
LOKI_URL=http://loki:3100
TEMPO_URL=http://tempo:4318/v1/traces
```

### Available Profiles

- `dev` - Development (default)
- `test` - Tests with H2 in memory
- `prod` - Production (disables Swagger, optimizes logs)

## 📊 API Usage

### Authentication

#### Register User

```bash
curl -X POST "http://localhost:8080/api/v1/auth/register" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "user",
       "email": "user@example.com",
       "password": "Pass@123"
     }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "username": "user",
    "email": "user@example.com",
    "role": "USER"
  }
}
```

#### Login

```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "user",
       "password": "Pass@123"
     }'
```

### Projects

#### Create Project

```bash
curl -X POST "http://localhost:8080/api/v1/projects" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "New Project",
       "description": "Project description",
       "startDate": "2025-01-15",
       "endDate": "2025-12-31"
     }'
```

#### List Projects

```bash
curl "http://localhost:8080/api/v1/projects" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Tasks

#### Create Task

```bash
curl -X POST "http://localhost:8080/api/v1/tasks" \
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

#### Filter Tasks

```bash
# Combined filter: status + priority + project
curl "http://localhost:8080/api/v1/tasks?status=TODO&priority=HIGH&projectId=uuid" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

#### Update Status

```bash
curl -X PUT "http://localhost:8080/api/v1/tasks/{id}/status" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE" \
     -H "Content-Type: application/json" \
     -d '{"status": "DONE"}'
```

## 📝 Main Endpoints

| POST | `/api/v1/auth/register` | Register user | ❌ |
| POST | `/api/v1/auth/login` | Authenticate user | ❌ |
| POST | `/api/v1/projects` | Create project | ✅ |
| GET | `/api/v1/projects` | List projects | ✅ |
| GET | `/api/v1/projects/{id}` | Get project | ✅ |
| POST | `/api/v1/tasks` | Create task | ✅ |
| GET | `/api/v1/tasks` | List/filter tasks | ✅ |
| PUT | `/api/v1/tasks/{id}/status` | Update status | ✅ |
| DELETE | `/api/v1/tasks/{id}` | Delete task (soft) | ✅ |
| GET | `/actuator/health` | Health check | ❌ |

## 🧪 Tests

### Run Tests

```bash
# All tests (62)
mvn test

# Unit only
mvn test -Dtest="*ServiceTest"

# Integration only
mvn test -Dtest="*ControllerTest"

# E2E only
mvn test -Dtest="TaskWorkflowIntegrationTest"

# With coverage
mvn clean test jacoco:report
```

### Test Coverage

- ✅ **20 Unit Tests** - Services with Mockito
- ✅ **17 Integration Tests** - Controllers with MockMvc
- ✅ **21 Repository Tests** - Queries and Specifications
- ✅ **4 E2E Tests** - Complete workflow with RestAssured

**Total: 62 tests | Status: 100% passing ✅**

## 📈 Performance

### Typical Benchmarks

- **Authentication (login)**: ~150ms
- **Projects CRUD**: <100ms
- **Queries with Filters**: <150ms
- **Health Check**: <50ms

### Implemented Optimizations

- **Database Indexes**: Optimized filterable fields
- **UUID vs Long**: Security vs performance trade-off
- **HikariCP**: Configured connection pooling
- **Specification API**: Type-safe and composable queries
- **Indexed Soft Delete**: Compound index project_id + deleted

## 📊 Observability

### Accessing Grafana

1. Open http://localhost:3000
2. Login: `admin` / `admin`
3. Pre-configured dashboards:
   - **JVM Micrometer** - Virtual machine metrics
   - **Spring Boot Statistics** - Framework metrics
   - **Application Metrics** - Custom metrics

### Querying Logs (Loki)

```bash
# In Grafana Explore, select Loki
{app="consilium-api"} | json

# Filter by level
{app="consilium-api"} | json | level="ERROR"

# Search by trace
{app="consilium-api"} | json | trace_id="abc123"
```

### Visualizing Traces (Tempo)

1. In Grafana, go to **Explore**
2. Select **Tempo**
3. Filter by service: `consilium-api`
4. Click on a trace to see the entire call chain

### Automatic Correlation

All logs have `trace_id` and `span_id`, allowing navigation from log to distributed trace with a click in Grafana.

## 🐳 Docker Deployment

### Production

```bash
# Build image
docker build -t consilium-api:latest .

# Run with docker-compose
docker-compose -f docker-compose.prod.yml up -d
```

### Essential Environment Variables

```env
# REQUIRED in production
JWT_SECRET=change-this-to-a-secure-256-bit-key
DATABASE_PASSWORD=strong-password-here
```

## 🔒 Security

### Security Implementations

- ✅ **JWT with HMAC-SHA256** - Signed tokens
- ✅ **BCrypt** - Passwords with cost factor 12
- ✅ **Configured CORS** - Specific allowed origins
- ✅ **UUID** - Non-sequential IDs (prevents enumeration)
- ✅ **Soft Delete** - Maintains audit trail
- ✅ **Environment Variables** - Secrets not committed
- ✅ **Swagger Disabled** - In production

### Best Practices

- Never commit `JWT_SECRET`
- Use strong passwords (minimum 8 characters, uppercase, numbers, special)
- Renew tokens regularly (default expiration: 24h)
- Configure HTTPS in production

## 📚 Documentation

### Complete Documentation

- 📖 [**API Reference**](docs/API.md) - All endpoints with examples
- 🛠️ [**Architecture**](docs/ARCHITECTURE.md) - Architectural decisions
- 📊 [**Observability**](docs/OBSERVABILITY.md) - Grafana stack guide
- 📮 [**Postman**](postman/postman.md) - Collection and automated tests

### Swagger / OpenAPI

```bash
# Interactive documentation
http://localhost:8080/swagger-ui.html

# OpenAPI JSON schema
http://localhost:8080/v3/api-docs
```

## 🗃️ Architectural Decisions

### UUID vs Long

I chose UUID because:
- **Security**: Prevents enumeration attacks
- **Distribution**: Facilitates future sharding
- **Integration**: Globally unique IDs

### Soft Delete

I implemented soft delete (`deleted` flag) to:
- **Audit**: Preserve complete history
- **Recovery**: Ability to restore
- **Integrity**: Maintain relationships

### Specification API

I use Specifications for:
- **Flexibility**: Dynamic filters without manual queries
- **Type-Safety**: Compile-time validation
- **Composition**: Combine filters with `and()` and `or()`

Example:
```java
Specification<Task> spec = Specification.where(hasStatus(status))
    .and(hasPriority(priority))
    .and(belongsToProject(projectId))
    .and(isNotDeleted());
```

### Observability Stack

I implemented complete observability because:
- **Debugging**: Problem tracing in runtime
- **Performance**: Bottleneck identification
- **Production-Ready**: Requirement for professional environments
- **Three Pillars**: Integrated Logs, Traces and Metrics

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
- [Spring REST Docs](https://spring.io/projects/spring-restdocs)

### Observability
- [Grafana Documentation](https://grafana.com/docs/)
- [Grafana Loki](https://grafana.com/docs/loki/latest/)
- [Grafana Tempo](https://grafana.com/docs/tempo/latest/)
- [Prometheus](https://prometheus.io/docs/introduction/overview/)
- [OpenTelemetry Java](https://opentelemetry.io/docs/instrumentation/java/)
- [Micrometer](https://micrometer.io/docs)

### Tools & Libraries
- [MapStruct Reference](https://mapstruct.org/documentation/stable/reference/html/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [JWT.io](https://jwt.io/) - JWT Debugger
- [RestAssured](https://rest-assured.io/) - REST API Testing
- [Testcontainers](https://www.testcontainers.org/) - Integration Testing

### Best Practices & Patterns
- [12 Factor App](https://12factor.net/)
- [RESTful API Design](https://restfulapi.net/)
- [Clean Code (Robert Martin)](https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882)
- [Effective Java (Joshua Bloch)](https://www.amazon.com/Effective-Java-Joshua-Bloch/dp/0134685997)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)

### Tools & Resources
- [Postman Learning Center](https://learning.postman.com/)
- [Docker Documentation](https://docs.docker.com/)
- [Maven Central](https://search.maven.org/)
- [IntelliJ IDEA Tips](https://www.jetbrains.com/idea/guide/)
- [Baeldung - Spring Tutorials](https://www.baeldung.com/)

---

### 🌟 **Star this project if you found it useful!**

**Made with ❤️ by [Thiago Di Faria](https://github.com/thiagodifaria)**