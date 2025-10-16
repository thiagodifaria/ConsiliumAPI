# 📊 Guia de Observabilidade - ConsiliumAPI

Neste guia vou te ensinar como usar a stack completa de observabilidade que implementei na ConsiliumAPI. Se você nunca trabalhou com Grafana, Loki ou Tempo, este tutorial vai te ajudar a começar.

---

## 🎯 Os Três Pilares

Implementei os **três pilares da observabilidade**. Pensa assim:

```
┌──────────────────────────────────────────────────┐
│                   OBSERVABILITY                  │
├──────────────┬──────────────┬────────────────────┤
│    LOGS      │   TRACES     │      METRICS       │
│              │              │                    │
│  "O que      │  "Por que    │  "Quanto           │
│   aconteceu?"│   demorou?"  │   aconteceu?"      │
│              │              │                    │
│   Loki       │    Tempo     │    Prometheus      │
└──────────────┴──────────────┴────────────────────┘
                       │
                ┌──────▼──────┐
                │   Grafana   │
                │ (Unified UI)│
                └─────────────┘
```

---

## 📝 Logs (Loki)

### O que são Logs?

Logs são **registros de eventos** que acontecem na aplicação. Pensa neles como o "diário" do sistema:
- Requisições HTTP (quem acessou o quê)
- Exceções e erros (o que quebrou)
- Operações de banco de dados (queries executadas)
- Ações de negócio (projeto criado, tarefa atualizada)

### Como Configurei

**logback-spring.xml**:
```xml
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

### Como Acessar (Passo a Passo)

Vou te mostrar como navegar nos logs:

1. **Abra o Grafana**: http://localhost:3000 (usuário: admin, senha: admin)
2. **Vá em Explore** (ícone de bússola no menu lateral)
3. **Selecione datasource**: `Loki` no dropdown superior
4. **Execute queries** (copie e cole os exemplos abaixo):

#### Queries Úteis que Preparei

**Ver todos os logs da aplicação**:
```logql
{app="consilium-api"}
```
Resultado: Mostra tudo que está acontecendo em tempo real.

**Filtrar apenas erros** (útil para debug):
```logql
{app="consilium-api"} |= "ERROR"
```
Resultado: Lista só os problemas.

**Logs com parsing JSON** (formato estruturado):
```logql
{app="consilium-api"} | json | level="ERROR"
```
Resultado: Parseia o JSON e filtra pelo campo `level`.

**Logs de um endpoint específico** (ex: projetos):
```logql
{app="consilium-api"} |= "/api/v1/projects"
```
Resultado: Mostra apenas chamadas ao endpoint de projetos.

**Logs de um trace específico** (correlação):
```logql
{app="consilium-api"} | json | trace_id="abc123def456"
```
Resultado: Mostra todos os logs de uma requisição específica (mágico!).

**Logs das últimas 5 minutos com erro**:
```logql
{app="project-management-system"} | json | level="ERROR" [5m]
```

### Estrutura do Log

Cada log possui:
```json
{
  "level": "INFO",
  "class": "c.s.p.controller.ProjectController",
  "thread": "http-nio-8080-exec-1",
  "message": "Creating new project: Sistema de Vendas",
  "trace_id": "550e8400e29b41d4a716446655440000",
  "span_id": "446655440000"
}
```

### Labels e Filtros

**Labels** (indexados, busca rápida):
- `app`: Nome da aplicação
- `level`: Nível do log (INFO, WARN, ERROR)

**Campos JSON** (extraídos em runtime):
- `class`: Classe que gerou o log
- `thread`: Thread de execução
- `trace_id`: ID do trace (correlação)
- `span_id`: ID do span

---

## 🔍 Traces (Tempo)

### O que são Traces?

Traces rastreiam **o caminho completo de uma requisição** através do sistema. É como ver um raio-X da sua request:

**Exemplo real**: Quando você faz GET /api/v1/projects, o trace mostra:

```
GET /api/v1/projects
      │
      ├─ SecurityFilter (5ms)
      │     └─ JwtTokenProvider.validateToken (3ms)
      │
      ├─ ProjectController.findAll (50ms)
      │     └─ ProjectService.findAll (45ms)
      │           └─ ProjectRepository.findAll (40ms)
      │                 └─ Database Query (38ms)
      │
      └─ Response Serialization (2ms)

Total: 57ms
```

**Por que isso é útil?** Você descobre onde a aplicação está lenta: se é o controller, o service, ou a query do banco.

### Como Configurei

**application.yml**:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% sampling (dev), use 0.1 (10%) in prod
  otlp:
    tracing:
      endpoint: http://tempo:4318/v1/traces
```

**ObservabilityConfig.java**:
```java
@Configuration
public class ObservabilityConfig {
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }
}
```

### Como Acessar (Passo a Passo)

Te ensino a explorar os traces:

1. **Abra o Grafana**: http://localhost:3000
2. **Vá em Explore** (mesmo lugar dos logs)
3. **Selecione datasource**: `Tempo` (troca o dropdown)
4. **Busque traces de várias formas**:

#### 1. Buscar por Service (ver tudo do sistema)

```
Service Name: consilium-api
Operation: GET /api/v1/projects
```
Resultado: Lista todas as chamadas ao endpoint de projetos.

#### 2. Buscar por TraceID (correlacionar com logs)

Se você pegou um `traceId` de um log:
```
Trace ID: 550e8400e29b41d4a716446655440000
```
Resultado: Mostra exatamente aquela requisição específica.

#### 3. Buscar por Duração (encontrar requisições lentas)

```
Min Duration: 100ms
Max Duration: 5000ms
```
Resultado: Lista requisições que demoraram entre 100ms e 5s (ótimo para performance tuning).

### Visualizando um Trace (O Mais Legal!)

Quando você clica em um trace, a mágica acontece:

**Timeline View**:
```
Controller     ████████████████ 50ms
  Service      █████████████ 45ms
    Repository ████████████ 40ms
      Database ███████████ 38ms
```

**Span Details**:
- **Operation**: Nome da operação
- **Duration**: Tempo total
- **Tags**: Metadata (http.method, http.status_code)
- **Logs**: Eventos dentro do span

### Correlação Logs ↔ Traces (Magia da Observabilidade!)

Implementei correlação automática. Veja como funciona:

**Do Log para o Trace**:
1. Você está vendo logs no Loki
2. Vê um log interessante
3. Clica no `trace_id` (aparece como link azul)
4. Grafana abre automaticamente o trace no Tempo
5. Você vê toda a requisição em detalhes!

**Do Trace para os Logs**:
1. Você está vendo um trace no Tempo
2. Clica em "Logs for this span"
3. Grafana mostra todos os logs daquela requisição
4. Você vê exatamente o que o código estava fazendo!

---

## 📈 Metrics (Prometheus)

### O que são Metrics?

Metrics são **números** sobre a aplicação ao longo do tempo. Pensa em gráficos:
- Quantas requisições por segundo? (throughput)
- Quantos erros estão acontecendo? (taxa de erro)
- Qual o uso de memória? (resource usage)
- Quanto tempo leva cada requisição? (latência)

**Por que são úteis?** Você consegue ver tendências e anomalias.

### Como Configurei

**application.yml**:
```yaml
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

**prometheus.yml**:
```yaml
scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:8080']
```

### Como Acessar (Duas Formas)

#### 1. Prometheus UI (Interface Simples)

**URL**: http://localhost:9090

Abra e vá em "Graph". Cole as queries abaixo na caixa de texto:

```promql
# Requisições por segundo (últimominuto)
rate(http_server_requests_seconds_count[1m])
→ Mostra quantas requests/segundo a API está recebendo

# Taxa de erro (últimos 5 minutos)
rate(http_server_requests_seconds_count{status="500"}[5m])
→ Quantos erros 500 estão acontecendo

# Uso de memória JVM (heap)
jvm_memory_used_bytes{area="heap"}
→ Quanto de memória a aplicação está usando

# Threads ativas
jvm_threads_live_threads
→ Quantas threads estão rodando

# Conexões do banco
hikaricp_connections_active
→ Quantas conexões com PostgreSQL estão ativas
```

#### 2. Grafana Dashboards (Interface Bonita)

**URL**: http://localhost:3000

Já importei dashboards prontos para você:

1. **JVM Micrometer**
   - Heap/Non-Heap memory
   - Garbage Collection
   - Threads
   - Class loading

2. **Spring Boot Statistics**
   - HTTP requests
   - Response times (p50, p95, p99)
   - Error rates
   - Active sessions

3. **Application Metrics**
   - Custom metrics
   - Business metrics
   - Database metrics

### Métricas Disponíveis

#### HTTP Metrics

```promql
# Total de requisições
http_server_requests_seconds_count

# Latência (percentis)
http_server_requests_seconds{quantile="0.95"}

# Por endpoint
http_server_requests_seconds_count{uri="/api/v1/projects"}

# Por status code
http_server_requests_seconds_count{status="200"}
```

#### JVM Metrics

```promql
# Memória heap usada
jvm_memory_used_bytes{area="heap"}

# Memória heap máxima
jvm_memory_max_bytes{area="heap"}

# Threads
jvm_threads_live_threads
jvm_threads_daemon_threads

# GC
jvm_gc_pause_seconds_count
jvm_gc_memory_allocated_bytes_total
```

#### Database Metrics

```promql
# Conexões ativas
hikaricp_connections_active

# Conexões idle
hikaricp_connections_idle

# Tempo de aquisição de conexão
hikaricp_connections_acquire_seconds
```

#### Custom Metrics (Futuro)

```java
@Service
public class ProjectService {
    private final Counter projectsCreated;

    public ProjectService(MeterRegistry registry) {
        this.projectsCreated = Counter.builder("projects_created_total")
            .description("Total de projetos criados")
            .register(registry);
    }

    public void createProject() {
        // ...
        projectsCreated.increment();
    }
}
```

---

## 🎨 Grafana Dashboards

### Dashboards Incluídos

#### 1. JVM Micrometer Dashboard

**O que monitora**:
- Heap e Non-Heap memory
- Garbage Collection (count, pause time)
- Thread states (live, daemon, peak)
- Class loading (loaded, unloaded)
- CPU usage

**Quando usar**:
- Investigar memory leaks
- Otimizar GC tuning
- Analisar thread pools

#### 2. Spring Boot Statistics

**O que monitora**:
- Request rate (req/s)
- Response time (p50, p95, p99)
- Error rate (4xx, 5xx)
- Endpoint breakdown
- Database connections

**Quando usar**:
- Performance tuning
- SLA monitoring
- Identificar endpoints lentos

#### 3. Application Metrics

**O que monitora**:
- Business metrics customizadas
- Métricas de domínio
- Contadores de eventos

**Quando usar**:
- Métricas de negócio
- KPIs da aplicação

### Como Adicionar Novo Dashboard

1. **Acesse Grafana**: http://localhost:3000
2. **Clique em "+"** → **Import**
3. **Cole o ID do dashboard** ou JSON
4. **Selecione datasource**: Prometheus
5. **Clique em Import**

**Dashboards recomendados**:
- **11378**: JVM Micrometer
- **6756**: Spring Boot 2.1 Statistics
- **12900**: Spring Boot APM

---

## 🔗 Correlação entre Pilares

### Tutorial Prático: Debugar uma Requisição Lenta

Deixa eu te mostrar um exemplo real de como eu uso a stack completa para debugar problemas.

**Cenário**: Usuários reclamando que a listagem de tarefas está lenta.

#### Passo 1: Começar com Metrics (Prometheus)

```promql
# Identificar endpoints lentos
topk(5,
  histogram_quantile(0.95,
    rate(http_server_requests_seconds_bucket[5m])
  )
)
```

**Resultado**: `/api/v1/tasks?projectId=xxx` tem p95 = 2.5s

**Conclusão**: Confirma o problema! 95% das requisições demoram 2.5 segundos (muito lento).

#### Passo 2: Ir para Traces (Tempo)

**Buscar**:
- Service: `project-management-system`
- Operation: `GET /api/v1/tasks`
- Min Duration: 2000ms

**Encontrar** trace específico lento:
```
GET /api/v1/tasks
  TaskController (10ms)
    TaskService (2400ms)
      TaskRepository.findAll (2390ms)  ← Gargalo!
        Database Query (2385ms)
```

**Conclusão**: Achei o gargalo! A query do banco está demorando 2.4s dos 2.5s totais.

#### Passo 3: Ver Logs (Loki)

**Query**:
```logql
{app="project-management-system"}
| json
| trace_id="550e8400e29b41d4a716446655440000"
```

**Logs revelam** (o SQL exato):
```json
{
  "level": "DEBUG",
  "message": "Executing query: SELECT * FROM tasks WHERE project_id = ? AND deleted = false",
  "trace_id": "550e8400e29b41d4a716446655440000"
}
```

**Conclusão**: A query está filtrando por `project_id` e `deleted`, mas provavelmente não tem índice!

#### Passo 4: Solução

**Diagnóstico**: Query sem índice composto

**Fix aplicado**:
```sql
CREATE INDEX idx_task_project_deleted ON tasks(project_id, deleted);
```

**Validação do fix (usando a stack)**:
1. **Metrics**: p95 caiu de 2.5s para 50ms ✅
2. **Traces**: Database span caiu de 2.4s para 45ms ✅
3. **Logs**: Query agora usa o índice (verificado com EXPLAIN) ✅

**Resultado**: Problema resolvido em 10 minutos! Isso é o poder da observabilidade completa.

---

## 🚨 Alerting (Futuro)

### Configurar Alertas no Grafana

```yaml
# grafana/provisioning/alerting/rules.yml
groups:
  - name: application
    interval: 1m
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "Taxa de erro acima de 5%"
          description: "{{ $value }} erros/s nos últimos 5 minutos"

      - alert: HighLatency
        expr: histogram_quantile(0.95,
                rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 10m
        annotations:
          summary: "P95 latency > 1s"
          description: "Endpoints estão lentos"

      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} /
              jvm_memory_max_bytes{area="heap"} > 0.9
        for: 5m
        annotations:
          summary: "Uso de memória > 90%"
          description: "Possível memory leak"
```

### Canais de Notificação

**Slack**:
```yaml
contact_points:
  - name: slack-alerts
    type: slack
    settings:
      url: https://hooks.slack.com/services/YOUR/WEBHOOK/URL
      text: |
        {{ range .Alerts }}
        *Alert*: {{ .Labels.alertname }}
        *Summary*: {{ .Annotations.summary }}
        *Description*: {{ .Annotations.description }}
        {{ end }}
```

**Email**, **PagerDuty**, **Teams** também suportados

---

## 📊 Melhores Práticas

### 1. Logging

**✅ Bom**:
```java
log.info("Creating project: name={}, startDate={}",
    project.getName(), project.getStartDate());
```

**❌ Ruim**:
```java
log.info("Creating project: " + project.toString());
```

**Por quê**:
- Structured logging (JSON parsing)
- Performance (lazy evaluation)
- Segurança (não logar senhas/tokens)

### 2. Tracing

**✅ Bom**:
```java
@Observed(name = "project.create")
public ProjectResponse create(CreateProjectRequest request) {
    // ...
}
```

**❌ Ruim**:
- Não criar spans demais (overhead)
- Não ignorar exceptions em spans

### 3. Metrics

**✅ Bom**:
```java
// Reuse meter registry
Counter counter = Counter.builder("projects_created")
    .tag("status", "success")
    .register(registry);
```

**❌ Ruim**:
- Alta cardinalidade em tags (user_id)
- Criar meters dentro de loops

### 4. Retenção de Dados

**Recomendações**:
- **Logs**: 7 dias (dev), 30 dias (prod)
- **Traces**: 2 dias (dev), 7 dias (prod)
- **Metrics**: 15 dias (dev), 90 dias (prod)

**Configuração (Loki)**:
```yaml
limits_config:
  retention_period: 168h  # 7 dias
```

---

## 🛠️ Troubleshooting

### Problema: Logs não aparecem no Loki

**Verificar**:
```bash
# 1. Container Loki está rodando?
docker ps | grep loki

# 2. Porta 3100 acessível?
curl http://localhost:3100/ready

# 3. Logs da aplicação
docker logs project-management-app | grep LOKI
```

**Solução comum**:
- Verificar `logback-spring.xml`: URL do Loki correta
- Firewall bloqueando porta 3100
- Loki sem espaço em disco

### Problema: Traces não aparecem no Tempo

**Verificar**:
```bash
# 1. OpenTelemetry endpoint correto?
curl http://localhost:4318/v1/traces

# 2. Sampling probability
# application.yml: management.tracing.sampling.probability: 1.0
```

**Solução comum**:
- `probability: 0.0` (nenhum trace coletado)
- Endpoint OTLP incorreto
- Porta 4318 bloqueada

### Problema: Métricas não aparecem no Prometheus

**Verificar**:
```bash
# 1. Actuator expondo /prometheus?
curl http://localhost:8080/actuator/prometheus

# 2. Prometheus scraping?
# Acesse: http://localhost:9090/targets
```

**Solução comum**:
- Actuator não habilitado
- Prometheus não configurado para scraping
- App não acessível da rede Docker

---

## 📚 Recursos que Usei para Aprender

### Documentação Oficial (Onde Aprendi)

- [Grafana Docs](https://grafana.com/docs/) - Comece aqui, é muito bem feita
- [Loki Docs](https://grafana.com/docs/loki/latest/) - Para entender LogQL
- [Tempo Docs](https://grafana.com/docs/tempo/latest/) - Distributed tracing
- [Prometheus Docs](https://prometheus.io/docs/) - PromQL queries
- [OpenTelemetry](https://opentelemetry.io/docs/) - Instrumentação moderna

### Tutoriais Recomendados

- [Grafana Fundamentals](https://grafana.com/tutorials/grafana-fundamentals/) - Tutorial interativo excelente
- [LogQL Tutorial](https://grafana.com/docs/loki/latest/logql/) - Aprender queries de logs
- [PromQL Basics](https://prometheus.io/docs/prometheus/latest/querying/basics/) - Queries de métricas

### Comunidade (Onde Tirar Dúvidas)

- [Grafana Community](https://community.grafana.com/) - Forum ativo e útil
- [CNCF Slack](https://slack.cncf.io/) - Canais #prometheus, #opentelemetry

**Dica**: Grafana tem uma das melhores documentações que já vi. Vale a pena ler.

---

## 🔗 Links Úteis

- [README Principal](../README.md)
- [API Reference](API.md)
- [Arquitetura](ARCHITECTURE.md)
- [Collection Postman](../postman/consilium-api.collection.json)

---

**ConsiliumAPI** - Desenvolvido por [Thiago Di Faria](https://github.com/thiagodifaria)

Se você chegou até aqui e conseguiu explorar a stack de observabilidade, parabéns! Você agora sabe mais sobre debugging em produção do que muita gente por aí.