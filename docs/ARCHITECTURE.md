# 🏗️ Arquitetura do Sistema - ConsiliumAPI

Neste documento vou explicar as decisões arquiteturais que tomei ao desenvolver a ConsiliumAPI e o raciocínio por trás de cada escolha.

---

## 📐 Visão Geral

Optei por uma **arquitetura em camadas (Layered Architecture)** com Spring Boot 3. Embora arquitetura hexagonal esteja na moda, escolhi camadas por questões de praticidade e familiaridade com o ecossistema Spring.

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                   │
│                 (REST Controllers + DTOs)               │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                    Service Layer                        │
│              (Business Logic + Validations)             │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                  Repository Layer                       │
│         (Data Access + Specifications + Queries)        │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                   Database Layer                        │
│              (PostgreSQL + H2 for tests)                │
└─────────────────────────────────────────────────────────┘
```

### Camadas Principais

1. **Controllers (Presentation)**
   - Exposição de endpoints REST
   - Validação de entrada (@Valid)
   - Serialização JSON
   - Documentação OpenAPI

2. **Services (Business Logic)**
   - Regras de negócio
   - Validações customizadas
   - Orquestração de operações
   - Transações (@Transactional)

3. **Repositories (Data Access)**
   - Acesso ao banco de dados
   - Queries customizadas
   - Specifications para filtros dinâmicos

4. **Entities (Domain)**
   - Modelos de domínio
   - Mapeamento JPA/Hibernate
   - Auditoria automática

---

## 🤔 Por quê Arquitetura em Camadas?

### Escolhi Layered Architecture ao invés de Hexagonal/Onion porque:

#### ✅ Vantagens para este Projeto

1. **Simplicidade**
   - Curva de aprendizado menor
   - Menos boilerplate
   - Estrutura familiar para desenvolvedores Spring

2. **Produtividade**
   - Setup rápido
   - Spring Boot já otimizado para camadas
   - Menos interfaces e abstrações

3. **Adequação ao Escopo**
   - Sistema CRUD predominante
   - Lógica de negócio moderada
   - Não há múltiplos adapters externos complexos

4. **Manutenibilidade**
   - Estrutura clara e direta
   - Fácil onboarding de novos desenvolvedores
   - Padrão amplamente documentado

#### ⚠️ Trade-offs Considerados

**Hexagonal seria melhor se:**
- Houvesse múltiplos adapters (GraphQL, gRPC, Message Queues)
- Regras de negócio fossem complexas e independentes de frameworks
- Sistema precisasse ser altamente testável em isolamento
- Houvesse necessidade de trocar frameworks frequentemente

**Para este projeto específico:**
- Tenho apenas REST API (não preciso de múltiplos adapters)
- Spring Boot é framework estável que pretendo manter
- Lógica de negócio é direta e bem definida
- Consegui boa testabilidade com mocks e slices do Spring

---

## 🔑 Por quê UUID ao invés de Long?

### Decisão: Decidi usar UUID como identificador

Esta foi uma das primeiras decisões que tomei, pesando segurança vs performance.

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

#### ✅ Vantagens

1. **Segurança**
   ```
   Long:  /api/v1/projects/1, /api/v1/projects/2
   UUID:  /api/v1/projects/550e8400-e29b-41d4-a716-446655440000
   ```
   - Evita enumeração de recursos
   - IDs não sequenciais dificultam ataques

2. **Distribuição**
   - Geração sem conflitos entre instâncias
   - Facilita sharding futuro
   - Merge de bancos simplificado

3. **Integração**
   - IDs únicos globalmente
   - Geração client-side possível
   - Sincronização entre sistemas facilitada

4. **Auditoria**
   - IDs não revelam ordem de criação
   - Melhor para LGPD/GDPR

#### ⚠️ Trade-offs

**Desvantagens do UUID:**
- Maior uso de espaço (16 bytes vs 8 bytes)
- Índices ligeiramente menos eficientes
- URLs mais longas
- Menos human-readable

**Mitigações que implementei:**
- Escolhi PostgreSQL que é otimizado para UUID
- Criei índices B-tree eficientes
- Usei UUIDs v4 (random) para maximizar segurança

**Quando eu usaria Long:**
- Sistemas puramente internos sem exposição externa
- Aplicações com bilhões de registros onde performance é crítica
- Quando ordenação cronológica por ID fosse requisito

---

## 🗑️ Por quê Soft Delete?

### Decisão: Implementei flag `deleted` ao invés de DELETE físico

Soft delete é uma daquelas decisões que parecem overhead no início, mas salvam você depois.

```java
@Entity
public class Task {
    private boolean deleted = false;
    // ...
}
```

#### ✅ Vantagens

1. **Auditoria e Compliance**
   - Histórico completo preservado
   - Rastreabilidade para auditorias
   - Conformidade com LGPD (logs de exclusão)

2. **Recuperação de Dados**
   - Usuário pode desfazer exclusões
   - Proteção contra exclusões acidentais
   - Backup implícito

3. **Integridade Referencial**
   - Foreign keys permanecem válidas
   - Relatórios históricos funcionam
   - Queries antigas não quebram

4. **Performance**
   - DELETE físico pode ser custoso (cascades, índices)
   - Soft delete é um simples UPDATE
   - Vacuum/reorganização evitada

#### ⚠️ Implementação

```java
// Repository - Excluir apenas ativos
@Query("SELECT t FROM Task t WHERE t.deleted = false")
List<Task> findAllActive();

// Specification - Filtro automático
public static Specification<Task> isNotDeleted() {
    return (root, query, cb) -> cb.isFalse(root.get("deleted"));
}
```

#### 🔄 Quando Purgar Dados

**Estratégias de purge:**
1. **Job agendado**: Deletar fisicamente após X dias
2. **Archive table**: Mover para tabela de arquivo
3. **Compressão**: Comprimir dados antigos
4. **Particionamento**: Particionar por deleted=true

**Para este projeto:**
- Mantive soft delete indefinido por simplicidade
- Em produção eu implementaria: purge automático após 365 dias ou migração para tabela de arquivo

---

## 🔍 Por quê Specification API?

### Decisão: Escolhi Spring Data JPA Specifications para filtros dinâmicos

Essa decisão surgiu quando percebi que precisava de filtros combináveis. @Query simples ia virar uma explosão combinatorial.

```java
public class TaskSpecification {
    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("status"), status);
    }
}

// Composição
Specification<Task> spec = Specification.where(hasStatus(status))
    .and(hasPriority(priority))
    .and(belongsToProject(projectId))
    .and(isNotDeleted());
```

#### ✅ Vantagens

1. **Type-Safety**
   - Validação em compile-time
   - Refactoring seguro
   - IDE autocomplete

2. **Composição**
   - Combinar filtros dinamicamente
   - Reutilizar predicados
   - Lógica AND/OR simples

3. **DRY (Don't Repeat Yourself)**
   - Evita queries duplicadas
   - Filtros reutilizáveis
   - Menos código boilerplate

4. **Manutenibilidade**
   - Centralização de queries
   - Fácil adicionar novos filtros
   - Testes unitários simples

#### 🆚 Alternativas Consideradas

**@Query com JPQL:**
```java
// Problema: Queries combinatórias explodem
@Query("SELECT t FROM Task t WHERE t.status = :status")
List<Task> findByStatus(@Param("status") TaskStatus status);

@Query("SELECT t FROM Task t WHERE t.status = :status AND t.priority = :priority")
List<Task> findByStatusAndPriority(...);

// Precisa de N! métodos para N filtros
```

**Criteria API pura:**
```java
// Problema: Verbosidade excessiva
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Task> cq = cb.createQuery(Task.class);
Root<Task> task = cq.from(Task.class);
List<Predicate> predicates = new ArrayList<>();
// ... 20 linhas de código
```

**Specifications (o que implementei):**
```java
// Solução: Elegante, composável e type-safe
taskRepository.findAll(
    hasStatus(status)
    .and(hasPriority(priority))
    .and(belongsToProject(projectId))
);
```

Foi a melhor escolha técnica que fiz no projeto. Zero overhead e máxima flexibilidade.

---

## 🎯 Estratégia de Testes

### Pirâmide de Testes que Implementei

Segui a pirâmide de testes clássica, priorizando testes rápidos e isolados na base.

```           
             / \
            /   \           4 E2E Tests
           /     \          (RestAssured)
          /  E2E  \         
         /_________\        
        /           \       17 Integration Tests
       / Integration \      (@SpringBootTest)
      /______________ \     
     /                 \    41 Tests
    /    Unit + Repo    \   (Mockito + @DataJpaTest)
   /____________________ \  
```

### 1. Testes Unitários (20 testes)

**Objetivo**: Testar lógica de negócio isolada

**Ferramentas**: JUnit 5 + Mockito

**Exemplo**:
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AuthServiceImpl authService;

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given, When, Then
    }
}
```

**O que testamos:**
- Validações de negócio
- Exceções customizadas
- Fluxos condicionais
- Interações com dependências

### 2. Testes de Repositório (21 testes)

**Objetivo**: Validar queries e Specifications

**Ferramentas**: @DataJpaTest (H2 in-memory)

**Exemplo**:
```java
@DataJpaTest
@Import(TestJpaAuditingConfig.class)
class TaskRepositoryTest {
    @Autowired private TaskRepository repository;

    @Test
    void shouldFilterByStatusAndPriority() {
        // Test Specification combinations
    }
}
```

**O que testamos:**
- Queries customizadas
- Specifications compostas
- Índices e performance
- Constraints do banco

### 3. Testes de Integração (17 testes)

**Objetivo**: Testar camadas integradas (Controller → Service → Repository)

**Ferramentas**: @SpringBootTest + MockMvc

**Exemplo**:
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void shouldCreateProjectSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(projectJson))
            .andExpect(status().isCreated());
    }
}
```

**O que testamos:**
- Endpoints completos
- Autenticação JWT
- Validações de entrada
- Serialização JSON
- Status codes HTTP

### 4. Testes E2E (4 testes)

**Objetivo**: Simular usuário real (workflow completo)

**Ferramentas**: RestAssured + @SpringBootTest(webEnvironment = RANDOM_PORT)

**Exemplo**:
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class TaskWorkflowIntegrationTest {
    @Test
    void shouldCompleteFullTaskWorkflow() {
        // 1. Register user
        // 2. Login (get token)
        // 3. Create project
        // 4. Create 3 tasks
        // 5. List tasks
        // 6. Update status
        // 7. Delete task
        // 8. Verify results
    }
}
```

**O que testamos:**
- Workflow completo da aplicação
- Integração entre recursos
- Comportamento como cliente externo
- Scenarios de uso real

### Por quê escolhi essa estrutura?

1. **Rapidez**: Testes unitários rodam em ~1s (feedback rápido durante desenvolvimento)
2. **Confiança**: Integration testa o fluxo completo sem mock excessivo
3. **Realismo**: E2E valida a experiência real do usuário
4. **Manutenibilidade**: Pirâmide equilibrada (não caí na armadilha do ice cream cone antipattern)

---

## 📊 Stack de Observabilidade

### Por quê investi em Observabilidade Completa?

Observabilidade não era requisito do desafio, mas implementei porque é essencial para produção.

#### Três Pilares que Implementei

```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│    Logs     │  │   Traces    │  │   Metrics   │
│   (Loki)    │  │   (Tempo)   │  │(Prometheus) │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       └────────────────┴────────────────┘
                        │
                  ┌─────▼───────┐
                  │  Grafana    │
                  │ (Dashboards)│
                  └─────────────┘
```

#### 1. Logs (Loki)

**O quê**: Eventos estruturados da aplicação

**Por quê**:
- Debug de problemas específicos
- Auditoria de ações
- Correlação com traces (traceId)

**Implementação**:
```xml
<!-- logback-spring.xml -->
<appender name="LOKI" class="com.github.loki4j.logback.Loki4jAppender">
    <http>
        <url>http://loki:3100/loki/api/v1/push</url>
    </http>
    <format>
        <label>
            <pattern>app=project-management-system,level=%level</pattern>
        </label>
        <message>
            <pattern>
                {
                  "level":"%level",
                  "class":"%logger{36}",
                  "thread":"%thread",
                  "message":"%message",
                  "trace_id":"%X{traceId}",
                  "span_id":"%X{spanId}"
                }
            </pattern>
        </message>
    </format>
</appender>
```

#### 2. Traces (Tempo)

**O quê**: Rastreamento de requisições entre serviços

**Por quê**:
- Identificar gargalos
- Visualizar latência
- Entender fluxo de dados

**Implementação**:
```yaml
# application.yml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% em dev, 10% em prod
  otlp:
    tracing:
      endpoint: http://tempo:4318/v1/traces
```

#### 3. Metrics (Prometheus)

**O quê**: Métricas numéricas (CPU, memória, requests)

**Por quê**:
- Alertas proativos
- Capacity planning
- SLA monitoring

**Implementação**:
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    tags:
      application: ${spring.application.name}
    export:
      prometheus:
        enabled: true
```

#### 4. Correlação Automática

**Logs ↔ Traces**:
- Cada log possui `traceId` e `spanId`
- Click no log → visualizar trace completo
- Click no trace → visualizar logs relacionados

**Grafana Datasources**:
```yaml
datasources:
  - name: Loki
    derivedFields:
      - name: TraceID
        matcherRegex: "trace_id\":\"([0-9a-f]+)"
        url: "$${__value.raw}"
        datasourceUid: tempo
```

### Por quê investi tempo nisso?

1. **Production-Ready**
   - Quero que este projeto seja referência de produção, não só um desafio
   - É algo esperado em code reviews e entrevistas técnicas reais

2. **Debugging Rápido**
   - Já me salvou várias vezes: encontrar bugs em minutos ao invés de horas
   - Correlação automática entre logs e traces é game changer

3. **Performance Tuning**
   - Consigo identificar queries lentas olhando os traces
   - Otimizei vários endpoints com base nos dados do Grafana

4. **Aprendizado**
   - Grafana Stack é padrão da indústria
   - Distributed tracing é skill valorizada no mercado

---

## 🔐 Segurança

### JWT (JSON Web Token)

**Fluxo de Autenticação**:

```

Cliente                                    Backend
                                          
  ========== Register/Login ==========>   
                                           • Valida credenciais
                                           • Gera token JWT
  <========== Return Token ============   
                                          
  ========== Request + JWT Token =====>   
                                           • Valida token
                                           • Extrai username
                                           • Carrega UserDetails
                                           • Verifica permissões
  <========== Return Data =============
```

**Configuração**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

**Token Structure**:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9  # Header
.eyJzdWIiOiJqb2huZG9lIiwiaWF0Ijox...  # Payload
.SflKxwRJSMeKKF2QT4fwpMeJf36POk...     # Signature
```

---

## 🗄️ Banco de Dados

### Estratégia de Schema

**Naming Conventions**:
- Tabelas: `snake_case` (users, projects, tasks)
- Colunas: `snake_case` (created_at, project_id)
- Índices: `idx_<table>_<column>` (idx_task_status)
- Foreign keys: Auto-gerado pelo Hibernate

**Auditoria Automática**:
```java
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class Auditable {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**Índices Estratégicos**:
```java
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_priority", columnList = "priority"),
    @Index(name = "idx_task_project_id", columnList = "project_id"),
    @Index(name = "idx_task_deleted", columnList = "deleted"),
    @Index(name = "idx_task_project_deleted", columnList = "project_id, deleted")
})
public class Task { }
```

### PostgreSQL vs H2

**PostgreSQL (Produção)**:
- Banco relacional robusto
- ACID completo
- Índices eficientes
- JSON/JSONB support futuro

**H2 (Testes)**:
- In-memory (ultra-rápido)
- Compatível com PostgreSQL mode
- Setup/teardown automático
- Sem dependências externas

---

## 📦 Estrutura de Código

### Organização de Pacotes

```
com.sisinnov.pms/
├── config/              # Configurações Spring
│   ├── SecurityConfig
│   ├── OpenApiConfig
│   ├── WebConfig
│   ├── JpaAuditingConfig
│   └── ObservabilityConfig
├── controller/          # REST Controllers
│   ├── AuthController
│   ├── ProjectController
│   └── TaskController
├── dto/                 # Data Transfer Objects
│   ├── request/        # Input DTOs
│   └── response/       # Output DTOs
├── entity/              # JPA Entities
│   ├── Auditable       # Base class
│   ├── User
│   ├── Project
│   └── Task
├── enums/               # Enumerações
│   ├── UserRole
│   ├── TaskStatus
│   └── TaskPriority
├── exception/           # Exceções customizadas
│   ├── BusinessException
│   ├── AuthenticationException
│   ├── ResourceNotFoundException
│   └── GlobalExceptionHandler
├── mapper/              # MapStruct mappers
│   ├── UserMapper
│   ├── ProjectMapper
│   └── TaskMapper
├── repository/          # Spring Data JPA
│   ├── UserRepository
│   ├── ProjectRepository
│   ├── TaskRepository
│   └── specification/   # Specifications
│       └── TaskSpecification
├── security/            # JWT & Security
│   ├── JwtTokenProvider
│   ├── JwtAuthenticationFilter
│   ├── JwtAuthenticationEntryPoint
│   └── CustomUserDetailsService
└── service/             # Business Logic
    ├── AuthService
    ├── ProjectService
    ├── TaskService
    └── impl/            # Implementações
```

### Por quê essa estrutura?

1. **Feature-first**: Organizado por funcionalidade
2. **Separação clara**: Cada pacote com responsabilidade única
3. **Escalabilidade**: Fácil adicionar novos módulos
4. **Navegabilidade**: IDE autocomplete eficiente

---

## 🚀 Performance Considerations

### Otimizações Implementadas

1. **Lazy Loading**
   ```java
   @OneToMany(fetch = FetchType.LAZY)
   private List<Task> tasks;
   ```

2. **Query Optimization**
   ```java
   // Projection para evitar carregar dados desnecessários
   @Query("SELECT new TaskResponse(t.id, t.title) FROM Task t")
   List<TaskResponse> findAllLite();
   ```

3. **Índices Compostos**
   ```sql
   CREATE INDEX idx_task_project_deleted ON tasks(project_id, deleted);
   ```

4. **Caching (futuro)**
   ```java
   @Cacheable(value = "projects", key = "#id")
   public ProjectResponse findById(UUID id) { }
   ```

### Monitoring de Performance

- **Slow Queries**: Hibernate Statistics
- **N+1 Problems**: Detectar com `spring.jpa.show-sql=true`
- **APM**: Traces do Tempo mostram latência de cada operação

---

## 🔄 Evolução Futura

### Próximas Iterações

1. **Caching com Redis**
   - Cache de projetos/tasks
   - Session storage distribuído

2. **Eventos Assíncronos**
   - Spring Events para notificações
   - Kafka para integrações

3. **API Versioning**
   - `/api/v2/` para breaking changes
   - Deprecation headers

4. **Rate Limiting**
   - Bucket4j para throttling
   - Proteção contra abuse

5. **Multi-tenancy**
   - Tenant ID em todas as queries
   - Isolamento de dados por organização

---

## 📚 Referências que Consultei

Durante o desenvolvimento, estas foram minhas principais fontes:

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/) - Sempre a primeira fonte
- [Spring Data JPA Specifications](https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/) - Para entender Specifications
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725) - Segurança em autenticação
- [OpenTelemetry](https://opentelemetry.io/docs/) - Setup de observabilidade
- [The Twelve-Factor App](https://12factor.net/) - Princípios para apps modernas

---

## 🔗 Links Úteis

- [README Principal](../README.md)
- [API Reference](API.md)
- [Guia de Observabilidade](OBSERVABILITY.md)
- [Collection Postman](../postman/consilium-api.collection.json)

---

**ConsiliumAPI** - Desenvolvido por [Thiago Di Faria](https://github.com/thiagodifaria)