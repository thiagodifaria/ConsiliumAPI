# 📮 Postman Collection - ConsiliumAPI

Criei esta collection completa do Postman para facilitar os testes de todos os endpoints da ConsiliumAPI. Se você está começando a explorar o projeto, esta collection vai te poupar bastante tempo.

---

## 📥 Como Importar

### Opção 1: Importar Arquivos

1. **Abra o Postman**
2. **Clique em "Import"** (botão superior esquerdo)
3. **Arraste os arquivos** ou clique em "Upload Files":
   - `consilium-api.collection.json`
   - `consilium-api.environment.json`
4. **Clique em "Import"**

### Opção 2: Via URL (GitHub)

Se preferir, você pode importar direto do repositório:

1. **Abra o Postman**
2. **Clique em "Import"**
3. **Selecione "Link"**
4. **Cole a URL** do arquivo JSON bruto do GitHub
5. **Clique em "Continue"** e depois **"Import"**

---

## ⚙️ Configuração do Environment

Configurei as variáveis de ambiente para automatizar ao máximo os testes. Após importar:

1. **Selecione o environment** "ConsiliumAPI - Local" no dropdown superior direito
2. **Variáveis disponíveis** (a maioria é preenchida automaticamente):

| `base_url` | URL base da API | `http://localhost:8080/api/v1` | Manual |
| `token` | Token JWT de autenticação | *(vazio)* | Automático |
| `project_id` | ID do último projeto criado | *(vazio)* | Automático |
| `task_id` | ID da última tarefa criada | *(vazio)* | Automático |

### Alterar URL para outro ambiente

Você pode facilmente apontar para outro ambiente editando a variável `base_url`:

- **Docker**: `http://localhost:8080/api/v1`
- **Produção**: `https://api.seu-dominio.com/api/v1`
- **Outro servidor**: `http://192.168.1.100:8080/api/v1`

---

## 🚀 Como Usar

Preparei um fluxo passo a passo para você começar rapidamente.

### Passo 1: Iniciar a Aplicação

Primeiro, certifique-se de que a aplicação está rodando:

```bash
# Com Docker
cd docker
docker-compose up -d

# Ou localmente
mvn spring-boot:run
```

Verifique: http://localhost:8080/actuator/health

### Passo 2: Executar Setup (Obter Token)

Implementei scripts que salvam o token automaticamente. **Execute em ordem**:

1. **Setup → Register User**
   - Registra um novo usuário
   - Token é salvo automaticamente em `{{token}}`
   - ✅ Se já registrou antes, pule para o próximo

2. **Setup → Login**
   - Autentica o usuário
   - Token é atualizado automaticamente
   - ✅ Execute sempre que o token expirar (24h)

**Verificação**: Após executar, vá em Environment e veja que `token` foi preenchido.

### Passo 3: Testar Projetos

Execute na ordem:

1. **Projects → Create Project**
   - Cria um projeto
   - `project_id` é salvo automaticamente
   - ✅ Status esperado: 201 Created

2. **Projects → List All Projects**
   - Lista todos os projetos
   - ✅ Status esperado: 200 OK

3. **Projects → Get Project by ID**
   - Busca o projeto criado
   - Usa `{{project_id}}` automaticamente
   - ✅ Status esperado: 200 OK

### Passo 4: Testar Tarefas

Execute na ordem:

1. **Tasks → Create Task**
   - Cria uma tarefa no projeto
   - `task_id` é salvo automaticamente
   - ✅ Status esperado: 201 Created

2. **Tasks → List All Tasks**
   - Lista todas as tarefas
   - ✅ Status esperado: 200 OK

3. **Tasks → Filter Tasks by Status**
   - Filtra tarefas TODO
   - ✅ Status esperado: 200 OK

4. **Tasks → Filter Tasks by Priority**
   - Filtra tarefas HIGH
   - ✅ Status esperado: 200 OK

5. **Tasks → Filter Tasks by Project**
   - Filtra tarefas do projeto atual
   - ✅ Status esperado: 200 OK

6. **Tasks → Filter Tasks (Combined)**
   - Combina todos os filtros
   - ✅ Status esperado: 200 OK

7. **Tasks → Update Task Status**
   - Muda status para DONE
   - ✅ Status esperado: 200 OK

8. **Tasks → Delete Task (Soft Delete)**
   - Deleta logicamente a tarefa
   - ✅ Status esperado: 204 No Content

### Passo 5: Health Check

**Health Check → Application Health**
- Verifica se a aplicação está UP
- ✅ Status esperado: 200 OK

---

## 🧪 Testes Automatizados

Adicionei testes automatizados em cada request para validar as respostas. Isso te ajuda a identificar rapidamente se algo quebrou:

### Setup Requests

✅ **Register User**:
- Token é retornado
- Token é salvo no environment
- Status é 201

✅ **Login**:
- Status é 200
- Token é retornado
- Username está correto
- Token é salvo no environment

### Projects Requests

✅ **Create Project**:
- Status é 201
- Project ID é retornado
- Location header está presente
- Project ID é salvo no environment

✅ **List All Projects**:
- Status é 200
- Response é um array

✅ **Get Project by ID**:
- Status é 200
- Projeto possui campos: id, name, taskCount

### Tasks Requests

✅ **Create Task**:
- Status é 201
- Task ID é retornado
- Task possui projectId e projectName
- Task ID é salvo no environment

✅ **List All Tasks**:
- Status é 200
- Response é um array

✅ **Filter Tasks by Status**:
- Status é 200
- Todas as tasks retornadas têm status TODO

✅ **Update Task Status**:
- Status é 200
- Task status mudou para DONE

✅ **Delete Task**:
- Status é 204

### Health Check

✅ **Application Health**:
- Status é 200
- Application status é UP

---

## 🔄 Runner (Executar Toda a Collection)

Você pode executar todos os testes de uma vez usando o Runner:

1. **Clique em "ConsiliumAPI"** (nome da collection)
2. **Clique em "Run"**
3. **Selecione o environment** "ConsiliumAPI - Local"
4. **Configure**:
   - ✅ Save responses
   - ✅ Keep variable values
   - Iterations: 1
5. **Clique em "Run Project Management API"**

**Resultado esperado**: Todos os testes devem passar ✅

---

## 📝 Exemplos de Uso

### Criar Múltiplos Projetos

Se você quiser popular o banco com dados de teste, duplique a request "Create Project" e altere o body:

```json
{
  "name": "E-commerce Platform",
  "description": "Loja virtual completa",
  "startDate": "2025-02-01",
  "endDate": "2025-08-31"
}
```

### Criar Tarefas com Diferentes Status

**TODO**:
```json
{
  "title": "Desenhar mockups",
  "status": "TODO",
  "priority": "MEDIUM",
  "projectId": "{{project_id}}"
}
```

**DOING**:
```json
{
  "title": "Implementar backend",
  "status": "DOING",
  "priority": "HIGH",
  "projectId": "{{project_id}}"
}
```

**DONE**:
```json
{
  "title": "Configurar CI/CD",
  "status": "DONE",
  "priority": "LOW",
  "projectId": "{{project_id}}"
}
```

### Testar Validações

Implementei várias validações na API. Aqui estão alguns exemplos para testar:

**Nome duplicado** (deve retornar 400):
```json
{
  "name": "Sistema de Vendas",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31"
}
```

**Data inválida** (deve retornar 400):
```json
{
  "name": "Projeto Teste",
  "startDate": "2025-12-31",
  "endDate": "2025-01-01"
}
```

**Projeto inexistente** (deve retornar 404):
```json
{
  "title": "Tarefa teste",
  "status": "TODO",
  "priority": "HIGH",
  "projectId": "00000000-0000-0000-0000-000000000000"
}
```

---

## 🔐 Autenticação

### Como funciona

Implementei autenticação JWT no projeto. O fluxo funciona assim:

1. **Registre ou faça login** → Recebe token JWT
2. **Token é salvo automaticamente** em `{{token}}`
3. **Todas as requests autenticadas** usam `Authorization: Bearer {{token}}`

### Token Expirado

**Sintoma**: Requests retornam 401 Unauthorized

**Solução**: Execute novamente **Setup → Login**

### Alterar Usuário

Para criar outro usuário, edite o body de **Register User**:

```json
{
  "username": "novousuario",
  "email": "novo@example.com",
  "password": "NovaSenh@123"
}
```

---

## 🐛 Troubleshooting

### ❌ Erro: "Could not get any response"

**Causa**: Aplicação não está rodando

**Solução**:
```bash
# Verificar se está UP
curl http://localhost:8080/actuator/health

# Iniciar aplicação
docker-compose up -d
# ou
mvn spring-boot:run
```

### ❌ Erro: 401 Unauthorized

**Causa**: Token não foi gerado ou expirou

**Solução**:
1. Execute **Setup → Login**
2. Verifique se `{{token}}` tem valor no environment
3. Verifique se o header `Authorization` está configurado

### ❌ Erro: 404 Not Found (em /tasks/{{task_id}})

**Causa**: Variável `task_id` está vazia

**Solução**:
1. Execute **Tasks → Create Task** primeiro
2. Verifique se `{{task_id}}` foi preenchido no environment

### ❌ Erro: 400 Bad Request (duplicate name)

**Causa**: Já existe um projeto com esse nome

**Solução**:
- Altere o nome do projeto no body
- Ou delete o projeto existente

---

## 📚 Estrutura da Collection

Organizei a collection em pastas lógicas para facilitar a navegação:

```
ConsiliumAPI/
├── Setup/
│   ├── Register User (POST /auth/register)
│   └── Login (POST /auth/login)
├── Projects/
│   ├── Create Project (POST /projects)
│   ├── List All Projects (GET /projects)
│   └── Get Project by ID (GET /projects/:id)
├── Tasks/
│   ├── Create Task (POST /tasks)
│   ├── List All Tasks (GET /tasks)
│   ├── Filter Tasks by Status (GET /tasks?status=TODO)
│   ├── Filter Tasks by Priority (GET /tasks?priority=HIGH)
│   ├── Filter Tasks by Project (GET /tasks?projectId=xxx)
│   ├── Filter Tasks (Combined) (GET /tasks?status&priority&projectId)
│   ├── Update Task Status (PUT /tasks/:id/status)
│   └── Delete Task (DELETE /tasks/:id)
└── Health Check/
    └── Application Health (GET /actuator/health)
```

**Total**: 13 requests organizadas em 4 pastas

---

## 🔗 Links Úteis

- [README Principal](../README.md)
- [API Reference](../docs/API.md)
- [Arquitetura](../docs/ARCHITECTURE.md)
- [Observabilidade](../docs/OBSERVABILITY.md)
- [Postman Learning Center](https://learning.postman.com/)

---

## 📄 Licença

Esta collection é parte do projeto **ConsiliumAPI** sob licença MIT.

---

**ConsiliumAPI** - Desenvolvido por [Thiago Di Faria](https://github.com/thiagodifaria)