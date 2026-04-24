# Task Manager API (Ktor + Kotlin)

Backend API para gerenciamento de tarefas com autenticacao JWT, persistencia em PostgreSQL e testes de contrato/integracao/unidade.

## Stack do projeto

- Kotlin JVM 21
- Ktor 3.4.x (Netty, Auth JWT, CORS, ContentNegotiation, StatusPages)
- kotlinx-serialization (JSON em `snake_case`)
- Exposed (JDBC)
- Flyway (migracoes SQL)
- Koin (DI)
- BCrypt (hash de senha)
- Testes com Ktor Test Host, JUnit 5, MockK, Testcontainers (dependencias ja configuradas)

## Arquitetura resumida

- Entrada da aplicacao: `src/main/kotlin/main.kt`
- Modulos de configuracao Ktor:
  - `src/main/kotlin/Serialization.kt`
  - `src/main/kotlin/Security.kt`
  - `src/main/kotlin/StatusPages.kt`
  - `src/main/kotlin/Koin.kt`
  - `src/main/kotlin/Routing.kt`
- Camadas de dominio:
  - `auth/` (registro, login, emissao de token)
  - `tasks/` (CRUD parcial de tarefas + filtros/paginacao)
  - `users/` (repositorio de usuarios)
  - `shared/` (tabelas, erros, JSON config, DB factory)

## Pre-requisitos

- JDK 21
- Docker (opcional, para PostgreSQL local)

## Configuracao

O projeto le configuracoes em `src/main/resources/application.conf` com fallback local.

Variaveis de ambiente suportadas:

- `PORT` (default `8080`)
- `POSTGRES_URL` (default `jdbc:postgresql://localhost:5432/task_manager`)
- `POSTGRES_USER` (default `task_manager`)
- `POSTGRES_PASSWORD` (default `task_manager`)
- `JWT_SECRET` (default `task-manager-dev-secret`)
- `JWT_ISSUER` (default `task-manager`)
- `JWT_AUDIENCE` (default `task-manager-users`)
- `JWT_REALM` (default `task-manager`)
- `CORS_ALLOWED_HOSTS` (default `localhost:3000,127.0.0.1:3000,localhost:5173,127.0.0.1:5173`)
- `CORS_ALLOW_CREDENTIALS` (default `false`)

### PostgreSQL local rapido (opcional)

```bash
docker run --name task-manager-postgres \
  -e POSTGRES_DB=task_manager \
  -e POSTGRES_USER=task_manager \
  -e POSTGRES_PASSWORD=task_manager \
  -p 5432:5432 \
  -d postgres:16
```

## Como executar

```bash
./gradlew run
```

Endpoints basicos:

- `GET /` -> `task-manager-backend`
- `GET /health` -> `ok`

## Testes

Os testes usam `src/test/resources/application.conf` com H2 em memoria (modo PostgreSQL) e reset de schema por teste.

```bash
./gradlew test
```

Status atual validado: build de testes concluida com sucesso localmente.

## API

Base path funcional: `/api`

### Autenticacao

#### Registrar usuario

- `POST /api/auth/register`
- Body:

```json
{
  "nome": "Maria",
  "email": "maria@email.com",
  "senha": "senha123"
}
```

Retorno: `201 Created`

#### Login

- `POST /api/auth/login`
- Body:

```json
{
  "email": "maria@email.com",
  "senha": "senha123"
}
```

Retorno: `200 OK`

Ambos retornam:

```json
{
  "usuario": {
	"id": "uuid",
	"nome": "Maria",
	"email": "maria@email.com"
  },
  "token": "jwt"
}
```

### Tarefas (requer `Authorization: Bearer <token>`)

#### Criar tarefa

- `POST /api/tasks`
- Body minimo:

```json
{
  "titulo": "Estudar Ktor"
}
```

Campos opcionais: `descricao`, `status`, `prioridade`, `data_vencimento` (ISO-8601).

#### Listar tarefas

- `GET /api/tasks`
- Query params opcionais:
  - `status`: `PENDENTE|EM_ANDAMENTO|CONCLUIDA`
  - `prioridade`: `BAIXA|MEDIA|ALTA`
  - `q`: busca textual em titulo/descricao
  - `sort`: `data_vencimento|-data_vencimento|data_criacao|-data_criacao`
  - `page`: inteiro >= 1 (default 1)
  - `limit`: inteiro >= 1 (default 10)

Resposta:

```json
{
  "dados": [],
  "meta": {
	"total_itens": 0,
	"total_paginas": 0,
	"pagina_atual": 1,
	"itens_por_pagina": 10
  }
}
```

#### Atualizar tarefa parcialmente

- `PATCH /api/tasks/{id}`
- Permite atualizar `titulo`, `descricao`, `status`, `prioridade`, `data_vencimento`

Regra de transicao de status:

- `PENDENTE -> EM_ANDAMENTO|CONCLUIDA`
- `EM_ANDAMENTO -> PENDENTE|CONCLUIDA`
- `CONCLUIDA -> EM_ANDAMENTO`

#### Excluir tarefa

- `DELETE /api/tasks/{id}`
- Retorno: `204 No Content`

## Modelo de erro

Erros seguem envelope padrao:

```json
{
  "status": 400,
  "erro": "validacao",
  "mensagem": "Descricao do erro",
  "detalhes": []
}
```

Codigos comuns observados:

- `400 Bad Request` (validacao, parametros invalidos)
- `401 Unauthorized` (credenciais/token invalidos)
- `403 Forbidden` (acesso a recurso de outro usuario)
- `404 Not Found` (tarefa inexistente)
- `422 Unprocessable Entity` (email duplicado)

## Banco de dados e migracoes

- Migracoes em `src/main/resources/db/migration`
- Versao atual: `V1__create_task_manager_schema.sql`
- Tabelas:
  - `usuarios`
  - `tarefas`

No runtime com PostgreSQL, Flyway executa `migrate()` automaticamente ao inicializar.

## Estrutura de diretorios

```text
src/main/kotlin
  auth/
  tasks/
  users/
  shared/

src/main/resources
  application.conf
  db/migration/

src/test/kotlin
  contract/
  integration/
  unit/
```

## Exemplos rapidos com curl

```bash
# registrar
curl -s -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Maria","email":"maria@email.com","senha":"senha123"}'

# login
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"maria@email.com","senha":"senha123"}'
```

## Troubleshooting

- Erro de conexao com banco: valide `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`.
- Token invalido: confirme `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE` e formato `Bearer`.
- CORS no frontend local: ajuste `CORS_ALLOWED_HOSTS`.
- Se houver mudancas de schema, crie nova migracao em vez de editar versoes ja aplicadas.

