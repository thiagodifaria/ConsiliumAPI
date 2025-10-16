# 📡 API Reference - ConsiliumAPI

Esta é a documentação completa de todos os endpoints que implementei na ConsiliumAPI. Aqui você encontra exemplos práticos e detalhes de cada operação.

**Base URL**: `http://localhost:8080/api/v1`

**Formato**: JSON

**Autenticação**: Bearer Token (JWT)

---

## 🔓 Autenticação

Implementei um sistema de autenticação JWT completo. Estes são os únicos endpoints públicos da API - todos os demais exigem autenticação.

### Registrar Novo Usuário

Este endpoint cria uma nova conta de usuário e já retorna o token JWT para facilitar o uso imediato da API.

**Endpoint**: `POST /auth/register`

**Autenticação**: Não requerida

**Request Body**:
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Validações**:
- `username`: Obrigatório, 3-50 caracteres, único
- `email`: Obrigatório, formato válido, único
- `password`: Obrigatório, mínimo 8 caracteres

**Response** `201 Created`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "johndoe",
  "role": "USER"
}
```

**Possíveis Erros**:
- `400 Bad Request`: Username ou email já existe
- `400 Bad Request`: Dados de validação inválidos

**Exemplo curl**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

---

### Login

Use este endpoint para autenticar um usuário existente e obter um novo token JWT.

**Endpoint**: `POST /auth/login`

**Autenticação**: Não requerida

**Request Body**:
```json
{
  "username": "johndoe",
  "password": "SecurePass123"
}
```

**Response** `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "johndoe",
  "role": "USER"
}
```

**Possíveis Erros**:
- `401 Unauthorized`: Credenciais inválidas

**Exemplo curl**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123"
  }'
```

---

## 📁 Projetos

Implementei operações CRUD completas para projetos. Todos estes endpoints requerem autenticação via token JWT.

### Criar Projeto

Cria um novo projeto. Optei por tornar o nome único (case-insensitive) para evitar duplicações acidentais.

**Endpoint**: `POST /projects`

**Autenticação**: Requerida (Bearer Token)

**Request Body**:
```json
{
  "name": "Sistema de Vendas",
  "description": "Aplicação para gerenciar vendas online",
  "startDate": "2025-01-15",
  "endDate": "2025-12-31"
}
```

**Validações**:
- `name`: Obrigatório, 3-100 caracteres, único (case-insensitive)
- `description`: Opcional, máximo 500 caracteres
- `startDate`: Obrigatório, formato ISO-8601 (YYYY-MM-DD)
- `endDate`: Opcional, deve ser maior que startDate

**Response** `201 Created`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Sistema de Vendas",
  "description": "Aplicação para gerenciar vendas online",
  "startDate": "2025-01-15",
  "endDate": "2025-12-31",
  "taskCount": 0,
  "createdAt": "2025-01-10T10:30:00",
  "updatedAt": "2025-01-10T10:30:00"
}
```

**Headers**:
- `Location`: `/api/v1/projects/{id}` - URL do recurso criado

**Possíveis Erros**:
- `400 Bad Request`: Nome já existe
- `400 Bad Request`: Data de início maior que data de término
- `400 Bad Request`: Validação de campos falhou
- `401 Unauthorized`: Token não fornecido ou inválido

**Exemplo curl**:
```bash
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sistema de Vendas",
    "description": "Aplicação para gerenciar vendas online",
    "startDate": "2025-01-15",
    "endDate": "2025-12-31"
  }'
```

---

### Listar Projetos

Retorna todos os projetos do usuário. Incluí o contador de tarefas (`taskCount`) para dar uma visão rápida do volume de trabalho.

**Endpoint**: `GET /projects`

**Autenticação**: Requerida (Bearer Token)

**Response** `200 OK`:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Sistema de Vendas",
    "description": "Aplicação para gerenciar vendas online",
    "startDate": "2025-01-15",
    "endDate": "2025-12-31",
    "taskCount": 5,
    "createdAt": "2025-01-10T10:30:00",
    "updatedAt": "2025-01-10T10:30:00"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Migração de Dados",
    "description": "Migração do banco legado",
    "startDate": "2025-02-01",
    "endDate": "2025-03-31",
    "taskCount": 12,
    "createdAt": "2025-01-11T14:20:00",
    "updatedAt": "2025-01-11T14:20:00"
  }
]
```

**Possíveis Erros**:
- `401 Unauthorized`: Token não fornecido ou inválido

**Exemplo curl**:
```bash
curl http://localhost:8080/api/v1/projects \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### Buscar Projeto por ID

Busca um projeto específico pelo ID. Usei UUID ao invés de Long para adicionar uma camada de segurança.

**Endpoint**: `GET /projects/{id}`

**Autenticação**: Requerida (Bearer Token)

**Parâmetros de Path**:
- `id` (UUID): ID do projeto

**Response** `200 OK`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Sistema de Vendas",
  "description": "Aplicação para gerenciar vendas online",
  "startDate": "2025-01-15",
  "endDate": "2025-12-31",
  "taskCount": 5,
  "createdAt": "2025-01-10T10:30:00",
  "updatedAt": "2025-01-10T10:30:00"
}
```

**Possíveis Erros**:
- `404 Not Found`: Projeto não encontrado
- `401 Unauthorized`: Token não fornecido ou inválido

**Exemplo curl**:
```bash
curl http://localhost:8080/api/v1/projects/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## ✅ Tarefas

Aqui estão os endpoints para gerenciar tarefas. Implementei filtros dinâmicos usando Specification API para permitir queries flexíveis.

### Criar Tarefa

Cria uma nova tarefa vinculada a um projeto. A tarefa sempre retorna o nome do projeto junto (`projectName`) para facilitar a exibição em UIs.

**Endpoint**: `POST /tasks`

**Autenticação**: Requerida (Bearer Token)

**Request Body**:
```json
{
  "title": "Implementar autenticação",
  "description": "Desenvolver sistema de login com JWT",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-02-15",
  "projectId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Validações**:
- `title`: Obrigatório, 3-150 caracteres
- `description`: Opcional, máximo 1000 caracteres
- `status`: Obrigatório, valores: `TODO`, `DOING`, `DONE`
- `priority`: Obrigatório, valores: `LOW`, `MEDIUM`, `HIGH`
- `dueDate`: Opcional, formato ISO-8601 (YYYY-MM-DD)
- `projectId`: Obrigatório, deve ser um projeto existente

**Response** `201 Created`:
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "title": "Implementar autenticação",
  "description": "Desenvolver sistema de login com JWT",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-02-15",
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "projectName": "Sistema de Vendas",
  "createdAt": "2025-01-12T09:15:00",
  "updatedAt": "2025-01-12T09:15:00"
}
```

**Possíveis Erros**:
- `404 Not Found`: Projeto não encontrado
- `400 Bad Request`: Validação de campos falhou
- `401 Unauthorized`: Token não fornecido ou inválido

**Exemplo curl**:
```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implementar autenticação",
    "description": "Desenvolver sistema de login com JWT",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2025-02-15",
    "projectId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

---

### Listar Tarefas com Filtros

Este é um dos endpoints mais flexíveis que implementei. Você pode combinar vários filtros dinamicamente - status, prioridade, projeto - e eles funcionam de forma composável.

**Endpoint**: `GET /tasks`

**Autenticação**: Requerida (Bearer Token)

**Query Parameters** (todos opcionais):
- `status` (string): Filtrar por status (`TODO`, `DOING`, `DONE`)
- `priority` (string): Filtrar por prioridade (`LOW`, `MEDIUM`, `HIGH`)
- `projectId` (UUID): Filtrar por projeto

**Exemplos de uso**:
- Todas as tarefas: `/tasks`
- Tarefas TODO: `/tasks?status=TODO`
- Tarefas de alta prioridade: `/tasks?priority=HIGH`
- Tarefas TODO de alta prioridade: `/tasks?status=TODO&priority=HIGH`
- Tarefas de um projeto: `/tasks?projectId=550e8400-e29b-41d4-a716-446655440000`

**Response** `200 OK`:
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "title": "Implementar autenticação",
    "description": "Desenvolver sistema de login com JWT",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2025-02-15",
    "projectId": "550e8400-e29b-41d4-a716-446655440000",
    "projectName": "Sistema de Vendas",
    "createdAt": "2025-01-12T09:15:00",
    "updatedAt": "2025-01-12T09:15:00"
  },
  {
    "id": "880e8400-e29b-41d4-a716-446655440003",
    "title": "Criar banco de dados",
    "description": "Modelar e criar estrutura do banco",
    "status": "DOING",
    "priority": "HIGH",
    "dueDate": "2025-02-10",
    "projectId": "550e8400-e29b-41d4-a716-446655440000",
    "projectName": "Sistema de Vendas",
    "createdAt": "2025-01-12T10:00:00",
    "updatedAt": "2025-01-13T14:30:00"
  }
]
```

**Observação**: Implementei soft delete, então tarefas marcadas como deletadas (deleted=true) não aparecem nas listagens, mas continuam no banco para auditoria.

**Possíveis Erros**:
- `401 Unauthorized`: Token não fornecido ou inválido

**Exemplo curl**:
```bash
# Todas as tarefas
curl "http://localhost:8080/api/v1/tasks" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Tarefas TODO de alta prioridade
curl "http://localhost:8080/api/v1/tasks?status=TODO&priority=HIGH" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Tarefas de um projeto específico
curl "http://localhost:8080/api/v1/tasks?projectId=550e8400-e29b-41d4-a716-446655440000" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### Atualizar Status da Tarefa

Criei um endpoint separado para atualizar apenas o status da tarefa. Isso é útil para UIs de kanban onde você só precisa mover tarefas entre colunas.

**Endpoint**: `PUT /tasks/{id}/status`

**Autenticação**: Requerida (Bearer Token)

**Parâmetros de Path**:
- `id` (UUID): ID da tarefa

**Request Body**:
```json
{
  "status": "DONE"
}
```

**Validações**:
- `status`: Obrigatório, valores: `TODO`, `DOING`, `DONE`
- Tarefa não pode estar deletada (soft delete)

**Response** `200 OK`:
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "title": "Implementar autenticação",
  "description": "Desenvolver sistema de login com JWT",
  "status": "DONE",
  "priority": "HIGH",
  "dueDate": "2025-02-15",
  "projectId": "550e8400-e29b-41d4-a716-446655440000",
  "projectName": "Sistema de Vendas",
  "createdAt": "2025-01-12T09:15:00",
  "updatedAt": "2025-01-15T16:45:00"
}
```

**Possíveis Erros**:
- `404 Not Found`: Tarefa não encontrada ou deletada
- `400 Bad Request`: Status inválido
- `401 Unauthorized`: Token não fornecido ou inválido

**Exemplo curl**:
```bash
curl -X PUT http://localhost:8080/api/v1/tasks/770e8400-e29b-41d4-a716-446655440002/status \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "DONE"}'
```

---

### Deletar Tarefa (Soft Delete)

Optei por soft delete ao invés de deleção física. A tarefa é apenas marcada como deletada, preservando o histórico para auditorias.

**Endpoint**: `DELETE /tasks/{id}`

**Autenticação**: Requerida (Bearer Token)

**Parâmetros de Path**:
- `id` (UUID): ID da tarefa

**Response** `204 No Content`

Sem corpo na resposta.

**Possíveis Erros**:
- `404 Not Found`: Tarefa não encontrada
- `401 Unauthorized`: Token não fornecido ou inválido

**Exemplo curl**:
```bash
curl -X DELETE http://localhost:8080/api/v1/tasks/770e8400-e29b-41d4-a716-446655440002 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Observação**: Escolhi manter as tarefas deletadas no banco por questões de compliance e rastreabilidade. Em produção, você pode implementar um job para limpar registros antigos.

---

## 📊 Health Check

### Verificar Status da Aplicação

**Endpoint**: `GET /actuator/health`

**Autenticação**: Não requerida

**Response** `200 OK`:
```json
{
  "status": "UP"
}
```

**Exemplo curl**:
```bash
curl http://localhost:8080/actuator/health
```

---

## 🔒 Autenticação JWT

### Como usar o token

Implementei autenticação JWT seguindo o padrão Bearer Token:

1. Faça login ou registre-se para obter o token
2. Adicione o header `Authorization` em todas as requisições protegidas
3. Formato: `Authorization: Bearer {seu_token_jwt}`

**Exemplo de header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Expiração do Token

Configurei os tokens para expirar em **24 horas** (86400000 ms) por questões de segurança:
- Tokens expirados retornam `401 Unauthorized`
- Para continuar usando a API, faça login novamente para obter um novo token

### Tratamento de Erros de Autenticação

**401 Unauthorized**:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT inválido ou expirado",
  "path": "/api/v1/projects"
}
```

---

## ❌ Códigos de Status HTTP

| `200` | OK | Requisição bem-sucedida (GET, PUT) |
| `201` | Created | Recurso criado com sucesso (POST) |
| `204` | No Content | Operação bem-sucedida sem corpo (DELETE) |
| `400` | Bad Request | Validação falhou ou dados inválidos |
| `401` | Unauthorized | Token ausente, inválido ou expirado |
| `404` | Not Found | Recurso não encontrado |
| `500` | Internal Server Error | Erro interno do servidor |

---

## 📖 Documentação Interativa

Integrei o Swagger/OpenAPI para você testar a API de forma interativa. Acesse:

**Swagger UI**: http://localhost:8080/swagger-ui.html

Na interface Swagger:
1. Clique em **Authorize**
2. Digite: `Bearer {seu_token}`
3. Clique em **Authorize** e **Close**
4. Agora você pode testar todos os endpoints protegidos

---

## 🔗 Links Úteis

- [README Principal](../README.md)
- [Arquitetura do Sistema](ARCHITECTURE.md)
- [Guia de Observabilidade](OBSERVABILITY.md)
- [Collection Postman](../postman/consilium-api.collection.json)

---

**ConsiliumAPI** - Desenvolvido por [Thiago Di Faria](https://github.com/thiagodifaria)