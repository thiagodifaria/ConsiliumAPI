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

- 🔐 **JWT Authentication** - Complete system with Spring Security 6
- 🚀 **High Performance** - Optimized queries, indexes, and soft delete
- 📊 **Full Observability** - Grafana + Loki + Tempo + Prometheus
- 🔍 **Dynamic Filters** - Specification API for complex queries
- 📈 **Enterprise Architecture** - Well-defined layers and separation of concerns
- 🧪 **62 Tests** - Complete coverage (unit, integration, repository, E2E)
- 🐳 **Docker Ready** - Full stack with observability
- 📚 **Auto Documentation** - Interactive Swagger UI

### 🏆 What Makes It Special?

```
✅ Spring Boot 3.2.5 with Java 17
✅ JWT Authentication with BCrypt
✅ UUID for security (prevents enumeration attacks)
✅ Soft delete for auditability
✅ Flyway for schema versioning
✅ Complete observability stack
✅ Postman collection with automated tests
✅ 2925+ lines of professional documentation
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