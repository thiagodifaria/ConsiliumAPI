# ConsiliumAPI - API de Gestão de Projetos e Tarefas

API REST moderna para gerenciamento de projetos e tarefas utilizando Spring Boot 3, JWT Authentication e stack completa de observabilidade. Este projeto oferece uma solução enterprise-grade com arquitetura em camadas, testes abrangentes e documentação profissional.

## 🎯 Funcionalidades

- ✅ **Autenticação JWT completa**: Sistema robusto com Spring Security 6 e BCrypt
- ✅ **CRUD de Projetos e Tarefas**: Operações completas com validações
- ✅ **Filtros Dinâmicos**: Specification API para queries complexas e combináveis
- ✅ **Soft Delete**: Preservação de histórico com flag de deleção
- ✅ **Observabilidade Full-Stack**: Grafana + Loki + Tempo + Prometheus
- ✅ **62 Testes Automatizados**: Unitários, integração, repositório e E2E
- ✅ **Docker Ready**: Stack completa configurada
- ✅ **Documentação Interativa**: Swagger/OpenAPI integrado

## 🏗️ Arquitetura

### Arquitetura em Camadas

O projeto segue uma arquitetura em camadas bem definida, escolhida pela sua simplicidade e familiaridade com o ecossistema Spring:

```
Controller (REST API)
    ↓
Service (Regras de Negócio)
    ↓
Repository (Acesso a Dados)
    ↓
Entity (Modelo de Domínio)
```

**Por que camadas ao invés de Hexagonal?**

Optei por camadas porque para o escopo deste projeto:
- É mais simples e direto
- Reduz overhead de configuração
- Mantém clareza e produtividade
- É amplamente conhecida na comunidade

### Stack Tecnológica

**Backend Core**
- **Java 17** - LTS version com novas features
- **Spring Boot 3.2.5** - Framework principal
- **Spring Security 6** - Autenticação e autorização
- **Spring Data JPA** - Camada de persistência
- **MapStruct 1.5.x** - Mapeamento automático de DTOs

**Banco de Dados**
- **PostgreSQL** - Banco principal para produção
- **H2** - Banco em memória para testes rápidos
- **Flyway** - Versionamento e migrations de schema

**Observabilidade (Grafana Stack)**
- **Grafana** - Dashboards e visualizações
- **Loki** - Agregação centralizada de logs
- **Tempo** - Armazenamento de traces distribuídos
- **Prometheus** - Coleta de métricas
- **OpenTelemetry** - Instrumentação de traces

**Testes**
- **JUnit 5** - Framework de testes
- **Mockito** - Mocks para testes unitários
- **RestAssured** - Testes E2E de API
- **@DataJpaTest** - Testes de repositório isolados

## 📋 Pré-requisitos

- Java 17 ou superior
- Maven 3.8+
- Docker e Docker Compose (recomendado)
- PostgreSQL 16+ (se rodar sem Docker)

## 🚀 Instalação Rápida

### Desenvolvimento com Docker Compose

A forma mais rápida de rodar o projeto completo com toda a stack de observabilidade:

```bash
# Clonar repositório
git clone https://github.com/thiagodifaria/ConsiliumAPI.git
cd ConsiliumAPI

# Buildar projeto
mvn clean package -DskipTests

# Iniciar stack completa
cd docker
docker-compose up -d

# Acompanhar logs
docker-compose logs -f app
```

**Serviços disponíveis:**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Grafana: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090

### Desenvolvimento Local (Sem Docker)

```bash
# Criar banco de dados
createdb consilium

# Configurar variáveis (opcional)
export DB_HOST=localhost
export DB_NAME=consilium
export DB_USER=postgres
export DB_PASSWORD=postgres

# Rodar aplicação
mvn spring-boot:run
```

### Perfil de Teste (H2 em memória)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

## ⚙️ Configuração

### Variáveis de Ambiente

```env
# Banco de Dados
DATABASE_URL=jdbc:postgresql://localhost:5432/consilium
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# JWT
JWT_SECRET=your-secret-key-minimum-256-bits-change-in-production
JWT_EXPIRATION=86400000

# Observabilidade (URLs padrão para Docker)
LOKI_URL=http://loki:3100
TEMPO_URL=http://tempo:4318/v1/traces
```

### Profiles Disponíveis

- `dev` - Desenvolvimento (padrão)
- `test` - Testes com H2 em memória
- `prod` - Produção (desabilita Swagger, otimiza logs)

## 📊 Uso da API

### Autenticação

#### Registrar Usuário

```bash
curl -X POST "http://localhost:8080/api/v1/auth/register" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "usuario",
       "email": "usuario@example.com",
       "password": "Senh@123"
     }'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "username": "usuario",
    "email": "usuario@example.com",
    "role": "USER"
  }
}
```

#### Login

```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
     -H "Content-Type: application/json" \
     -d '{
       "username": "usuario",
       "password": "Senh@123"
     }'
```

### Projetos

#### Criar Projeto

```bash
curl -X POST "http://localhost:8080/api/v1/projects" \
     -H "Authorization: Bearer SEU_TOKEN_AQUI" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Novo Projeto",
       "description": "Descrição do projeto",
       "startDate": "2025-01-15",
       "endDate": "2025-12-31"
     }'
```

#### Listar Projetos

```bash
curl "http://localhost:8080/api/v1/projects" \
     -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

### Tarefas

#### Criar Tarefa

```bash
curl -X POST "http://localhost:8080/api/v1/tasks" \
     -H "Authorization: Bearer SEU_TOKEN_AQUI" \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Implementar autenticação",
       "description": "Desenvolver sistema de login com JWT",
       "status": "TODO",
       "priority": "HIGH",
       "dueDate": "2025-02-15",
       "projectId": "uuid-do-projeto"
     }'
```

#### Filtrar Tarefas

```bash
# Filtro combinado: status + prioridade + projeto
curl "http://localhost:8080/api/v1/tasks?status=TODO&priority=HIGH&projectId=uuid" \
     -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

#### Atualizar Status

```bash
curl -X PUT "http://localhost:8080/api/v1/tasks/{id}/status" \
     -H "Authorization: Bearer SEU_TOKEN_AQUI" \
     -H "Content-Type: application/json" \
     -d '{"status": "DONE"}'
```

## 🔍 Endpoints Principais

| POST | `/api/v1/auth/register` | Registrar usuário | ❌ |
| POST | `/api/v1/auth/login` | Autenticar usuário | ❌ |
| POST | `/api/v1/projects` | Criar projeto | ✅ |
| GET | `/api/v1/projects` | Listar projetos | ✅ |
| GET | `/api/v1/projects/{id}` | Buscar projeto | ✅ |
| POST | `/api/v1/tasks` | Criar tarefa | ✅ |
| GET | `/api/v1/tasks` | Listar/filtrar tarefas | ✅ |
| PUT | `/api/v1/tasks/{id}/status` | Atualizar status | ✅ |
| DELETE | `/api/v1/tasks/{id}` | Deletar tarefa (soft) | ✅ |
| GET | `/actuator/health` | Health check | ❌ |

## 🧪 Testes

### Executar Testes

```bash
# Todos os testes (62)
mvn test

# Apenas unitários
mvn test -Dtest="*ServiceTest"

# Apenas integração
mvn test -Dtest="*ControllerTest"

# Apenas E2E
mvn test -Dtest="TaskWorkflowIntegrationTest"

# Com cobertura
mvn clean test jacoco:report
```

### Cobertura de Testes

- ✅ **20 Testes Unitários** - Services com Mockito
- ✅ **17 Testes de Integração** - Controllers com MockMvc
- ✅ **21 Testes de Repositório** - Queries e Specifications
- ✅ **4 Testes E2E** - Workflow completo com RestAssured

**Total: 62 testes | Status: 100% passando ✅**

## 📈 Performance

### Benchmarks Típicos

- **Autenticação (login)**: ~150ms
- **CRUD de Projetos**: <100ms
- **Queries com Filtros**: <150ms
- **Health Check**: <50ms

### Otimizações Implementadas

- **Índices no Banco**: Campos filtráveis otimizados
- **UUID vs Long**: Trade-off segurança vs performance
- **HikariCP**: Connection pooling configurado
- **Specification API**: Queries type-safe e composáveis
- **Soft Delete Indexado**: Índice composto project_id + deleted

## 📊 Observabilidade

### Acessando o Grafana

1. Abra http://localhost:3000
2. Login: `admin` / `admin`
3. Dashboards pré-configurados:
   - **JVM Micrometer** - Métricas da máquina virtual
   - **Spring Boot Statistics** - Métricas do framework
   - **Application Metrics** - Métricas customizadas

### Consultando Logs (Loki)

```bash
# No Grafana Explore, selecione Loki
{app="consilium-api"} | json

# Filtrar por nível
{app="consilium-api"} | json | level="ERROR"

# Buscar por trace
{app="consilium-api"} | json | trace_id="abc123"
```

### Visualizando Traces (Tempo)

1. No Grafana, vá em **Explore**
2. Selecione **Tempo**
3. Filtre por service: `consilium-api`
4. Clique em um trace para ver toda a cadeia de chamadas

### Correlação Automática

Todos os logs possuem `trace_id` e `span_id`, permitindo navegar do log para o trace distribuído com um clique no Grafana.

## 🐳 Deploy com Docker

### Produção

```bash
# Build da imagem
docker build -t consilium-api:latest .

# Run com docker-compose
docker-compose -f docker-compose.prod.yml up -d
```

### Variáveis de Ambiente Essenciais

```env
# OBRIGATÓRIO em produção
JWT_SECRET=change-this-to-a-secure-256-bit-key
DATABASE_PASSWORD=strong-password-here
```

## 🔒 Segurança

### Implementações de Segurança

- ✅ **JWT com HMAC-SHA256** - Tokens assinados
- ✅ **BCrypt** - Senhas com cost factor 12
- ✅ **CORS Configurado** - Origens permitidas específicas
- ✅ **UUID** - IDs não sequenciais (previne enumeration)
- ✅ **Soft Delete** - Mantém auditoria
- ✅ **Variáveis de Ambiente** - Segredos não commitados
- ✅ **Swagger Desabilitado** - Em produção

### Boas Práticas

- Nunca commitar `JWT_SECRET`
- Usar senhas fortes (mínimo 8 caracteres, maiúsculas, números, especiais)
- Renovar tokens regularmente (expiração padrão: 24h)
- Configurar HTTPS em produção

## 📝 Documentação

### Documentação Completa

- 📖 [**API Reference**](docs/API.md) - Todos os endpoints com exemplos
- 🏛️ [**Architecture**](docs/ARCHITECTURE.md) - Decisões arquiteturais
- 📊 [**Observability**](docs/OBSERVABILITY.md) - Guia da stack Grafana
- 📮 [**Postman**](postman/postman.md) - Collection e testes automatizados

### Swagger / OpenAPI

```bash
# Documentação interativa
http://localhost:8080/swagger-ui.html

# Schema OpenAPI JSON
http://localhost:8080/v3/api-docs
```

## 🏗️ Decisões Arquiteturais

### UUID vs Long

Escolhi UUID porque:
- **Segurança**: Previne enumeration attacks
- **Distribuição**: Facilita sharding futuro
- **Integração**: IDs globalmente únicos

### Soft Delete

Implementei soft delete (flag `deleted`) para:
- **Auditoria**: Preservar histórico completo
- **Recuperação**: Possibilidade de restaurar
- **Integridade**: Manter relacionamentos

### Specification API

Uso Specifications para:
- **Flexibilidade**: Filtros dinâmicos sem queries manuais
- **Type-Safety**: Validação em compile-time
- **Composição**: Combinar filtros com `and()` e `or()`

Exemplo:
```java
Specification<Task> spec = Specification.where(hasStatus(status))
    .and(hasPriority(priority))
    .and(belongsToProject(projectId))
    .and(isNotDeleted());
```

### Stack de Observabilidade

Implementei observabilidade completa porque:
- **Debugging**: Rastreamento de problemas em runtime
- **Performance**: Identificação de gargalos
- **Produção-Ready**: Requisito para ambientes profissionais
- **Três Pilares**: Logs, Traces e Metrics integrados

## 📄 Licença

Distribuído sob a licença MIT. Veja `LICENSE` para mais informações.

## 📞 Contato

**Thiago Di Faria**
- Email: thiagodifaria@gmail.com
- GitHub: [@thiagodifaria](https://github.com/thiagodifaria)
- LinkedIn: [Thiago Di Faria](https://linkedin.com/in/thiagodifaria)
- Projeto: [https://github.com/thiagodifaria/ConsiliumAPI](https://github.com/thiagodifaria/ConsiliumAPI)

---

## 🙏 Agradecimentos

Este projeto é baseado em um desafio técnico originalmente proposto pela **SIS Innov & Tech**. Tive a oportunidade de trabalhar neste desafio graças ao **[Matheus Leandro Ferreira](https://github.com/matheuslf)**, que o compartilhou em seu repositório [dev.matheuslf.desafio.inscritos](https://github.com/matheuslf/dev.matheuslf.desafio.inscritos).

Agradecimentos especiais para:
- **Matheus Leandro Ferreira** ([@matheuslf](https://github.com/matheuslf)) - Por disponibilizar este desafio
- **SIS Innov & Tech** - Por criar o desafio técnico original
- Comunidade Spring Boot pela excelente documentação
- Projeto Grafana pela stack de observabilidade
- Contribuidores open-source das bibliotecas utilizadas

---

## 📚 Referências e Links Úteis

### Spring Framework
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring REST Docs](https://spring.io/projects/spring-restdocs)

### Observabilidade
- [Grafana Documentation](https://grafana.com/docs/)
- [Grafana Loki](https://grafana.com/docs/loki/latest/)
- [Grafana Tempo](https://grafana.com/docs/tempo/latest/)
- [Prometheus](https://prometheus.io/docs/introduction/overview/)
- [OpenTelemetry Java](https://opentelemetry.io/docs/instrumentation/java/)
- [Micrometer](https://micrometer.io/docs)

### Ferramentas & Bibliotecas
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

### 🌟 **Star este projeto se você o achou útil!**

**Feito com ❤️ por [Thiago Di Faria](https://github.com/thiagodifaria)**