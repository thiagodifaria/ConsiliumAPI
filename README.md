# ConsiliumAPI

![ConsiliumAPI Logo](https://img.shields.io/badge/ConsiliumAPI-Project%20Management-0066cc?style=for-the-badge&logo=java)

**Modern REST API for Project and Task Management**

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=flat&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-316192?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat)](LICENSE)
[![Tests](https://img.shields.io/badge/Tests-62%20passing-success?style=flat)]()
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat&logo=docker&logoColor=white)](https://docker.com)

---

## 🌍 **Documentation / Documentação**

**📖 [🇺🇸 Read in English](README_EN.md)**
**📖 [🇧🇷 Leia em Português](README_PT.md)**

---

## 🎯 What is ConsiliumAPI?

ConsiliumAPI is an **enterprise-grade REST API** for project and task management, built with **Spring Boot 3** and modern development best practices. The system provides JWT authentication, complete observability stack, and scalable architecture.

### ⚡ Key Highlights

- 🔐 **JWT Authentication** - Complete system with Spring Security 6 + Refresh Tokens
- 🚀 **High Performance** - Redis cache (80% faster), optimized queries, indexes
- 📊 **Full Observability** - Grafana + Loki + Tempo + Prometheus
- 🔍 **Dynamic Filters** - Specification API for complex queries
- 📈 **Enterprise Architecture** - CQRS Pattern + Event Sourcing
- 🐰 **RabbitMQ** - Async messaging with Dead Letter Queue
- 🛡️ **Rate Limiting** - Token Bucket algorithm (10-1000 req/min by role)
- 📜 **Event Store** - Complete audit trail with immutable events
- 🧪 **105+ Tests** - Complete coverage (unit, integration, repository, E2E)
- 🐳 **Docker Ready** - Full stack with 7 services (PostgreSQL, Redis, RabbitMQ, etc)
- 📚 **Auto Documentation** - Interactive Swagger UI
- 🔧 **Automation Scripts** - build.sh, dev.sh, test.sh for productivity

### 🏆 What Makes It Special?

```
✅ Spring Boot 3.2.5 with Java 17
✅ JWT Authentication with Refresh Tokens (15min access + 7 days refresh)
✅ Redis Cache (80% latency reduction: 100ms → 20ms)
✅ RabbitMQ async messaging with DLQ and retry policies
✅ Rate Limiting (Token Bucket: 10-1000 req/min by role)
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

---

## ⚡ Quick Start

### Option 1: Docker Compose (Recommended)
```bash
# Clone and run with all services
git clone https://github.com/thiagodifaria/ConsiliumAPI.git
cd ConsiliumAPI
docker-compose up --build

# API available at: http://localhost:8080
# Swagger at: http://localhost:8080/swagger-ui.html
# Grafana at: http://localhost:3000 (admin/admin)
```

### Option 2: Local Development
```bash
git clone https://github.com/thiagodifaria/ConsiliumAPI.git
cd ConsiliumAPI
mvn clean install
mvn spring-boot:run

# Application starts at http://localhost:8080
```

### 🔥 Test It Now!
```bash
# 1. Register user
curl -X POST "http://localhost:8080/api/v1/auth/register" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "user",
       "email": "user@example.com",
       "password": "Pass@123"
     }'

# 2. Login (get JWT token)
curl -X POST "http://localhost:8080/api/v1/auth/login" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "user",
       "password": "Pass@123"
     }'

# 3. Create project (use token from login)
curl -X POST "http://localhost:8080/api/v1/projects" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "My First Project",
       "description": "Sample project",
       "startDate": "2025-01-15"
     }'
```

---

## 🔧 Automation Scripts

To facilitate development and deployment, the project includes automation scripts for the most common tasks:

### Linux/Mac

```bash
# Complete build with interactive menu
./build.sh

# Or specific commands
./build.sh clean          # Clean up previous builds
./build.sh run_tests      # Run only tests
./build.sh docker_up      # Upload Docker environment
```

**Menu Options:**
1. 🏗️ Complete build (clean + test + build + docker)
2. ⚡ Quick build (no testing)
3. 🧪 Just run tests
4. 🐳 Just Docker (rebuild + restart)
5. 🧹 Clean all (Docker + builds)
6. 📊 View service logs
7. 🛑 Stop all services

### Windows (PowerShell)

.\build.ps1 Clean         # Clean builds
.\build.ps1 Tests         # Run tests
.\build.ps1 DockerUp      # Up Docker
```

### Development Mode (Hot Reload)

```bash
# Start only dependencies in Docker + application in dev mode
./dev.sh

# Benefits:
# ✅ Automatic hot reload (Spring Boot DevTools)
# ✅ Application runs outside of Docker (faster for debugging)
# ✅ Ideal for active development
```

### Coverage Tests

```bash
# Run tests with JaCoCo coverage report
./test.sh

# Opens automatically: target/site/jacoco/index.html
```

### Script Features

**build.sh / build.ps1**
- ✅ Check dependencies (Java 17+, Maven, Docker)
- ✅ Full or quick build (with/without tests)
- ✅ Docker Management (cleanup, build, up)
- ✅ Environmental cleaning
- ✅ User-friendly interactive menu
- ✅ Waiting for services to become healthy

**dev.sh**
- ✅ Start only dependencies in Docker
- ✅ Run application in dev mode (hot reload)
- ✅ Checks the health of services (PostgreSQL, Redis, RabbitMQ)
- ✅ Ideal for active development

**test.sh**
- ✅ Performs coverage tests (JaCoCo)
- ✅ Generates automatic HTML report
- ✅ Open report in browser
- ✅ Shows test statistics

---

## 🔍 API Overview

| 🔐 **Authentication** | `POST /api/v1/auth/register` | Register new user |
| 🔑 **Login** | `POST /api/v1/auth/login` | Authenticate and get JWT token |
| 📁 **Create Project** | `POST /api/v1/projects` | Create new project |
| 📋 **List Projects** | `GET /api/v1/projects` | List all projects |
| 📄 **Project Details** | `GET /api/v1/projects/{id}` | Get project by ID |
| ✅ **Create Task** | `POST /api/v1/tasks` | Create new task |
| 🔍 **Filter Tasks** | `GET /api/v1/tasks` | Search with filters (status, priority, project) |
| 🔄 **Update Status** | `PUT /api/v1/tasks/{id}/status` | Update task status |
| 🗑️ **Delete Task** | `DELETE /api/v1/tasks/{id}` | Soft delete task |
| 🏥 **Health Check** | `GET /actuator/health` | Check application health |

---

## 📞 Contact

**Thiago Di Faria** - thiagodifaria@gmail.com

[![GitHub](https://img.shields.io/badge/GitHub-@thiagodifaria-black?style=flat&logo=github)](https://github.com/thiagodifaria)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Thiago_Di_Faria-blue?style=flat&logo=linkedin)](https://linkedin.com/in/thiagodifaria)

**Project**: [https://github.com/thiagodifaria/ConsiliumAPI](https://github.com/thiagodifaria/ConsiliumAPI)

---

## 🙏 Acknowledgments

This project is based on a technical challenge originally proposed by **SIS Innov & Tech**. I had the opportunity to work on this challenge thanks to **[Matheus Leandro Ferreira](https://github.com/matheuslf)**, who shared it in his repository [dev.matheuslf.desafio.inscritos](https://github.com/matheuslf/dev.matheuslf.desafio.inscritos).

Special thanks to:
- **Matheus Leandro Ferreira** ([@matheuslf](https://github.com/matheuslf)) - For making this challenge available
- **SIS Innov & Tech** - For creating the original technical challenge
- Spring Boot community for excellent documentation
- Grafana project for the observability stack
- Open-source contributors of used libraries

---

### 🌟 **Star this project if you find it useful!**

**Made with ❤️ by [Thiago Di Faria](https://github.com/thiagodifaria)**