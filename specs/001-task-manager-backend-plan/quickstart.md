# Quickstart — Backend Kotlin/Ktor

## 1. Pré-requisitos

- JDK 21
- Docker (para testes com Testcontainers)
- PostgreSQL local (opcional se usar container no ambiente de desenvolvimento)

## 2. Configuração

1. Definir variáveis de ambiente (recomendado, sem editar arquivo versionado):

```bash
export JWT_SECRET="task-manager-dev-secret"
export JWT_ISSUER="task-manager"
export JWT_AUDIENCE="task-manager-users"
export JWT_REALM="task-manager"

export POSTGRES_URL="jdbc:postgresql://localhost:5432/task_manager"
export POSTGRES_USER="task_manager"
export POSTGRES_PASSWORD="task_manager"

export CORS_ALLOWED_HOSTS="localhost:3000,127.0.0.1:3000,localhost:5173,127.0.0.1:5173"
```

2. Subir PostgreSQL local (exemplo rápido com Docker):

```bash
docker run --name task-manager-postgres \
  -e POSTGRES_DB=task_manager \
  -e POSTGRES_USER=task_manager \
  -e POSTGRES_PASSWORD=task_manager \
  -p 5432:5432 \
  -d postgres:16
```

3. Confirmar migrações Flyway disponíveis em `src/main/resources/db/migration/` (atual: `V1__create_task_manager_schema.sql`).

## 3. Executar aplicação

```bash
./gradlew run
```

Servidor esperado em `http://localhost:8080`.

No startup, o Flyway deve aplicar migrações e criar tabelas `usuarios` e `tarefas`.

## 4. Fluxo mínimo de validação manual

### Registrar usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"João","email":"joao@email.com","senha":"senha123"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@email.com","senha":"senha123"}'
```

### Criar tarefa

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Estudar Ktor","prioridade":"ALTA"}'
```

### Listar tarefas

```bash
curl -X GET "http://localhost:8080/api/tasks?page=1&limit=10&status=PENDENTE" \
  -H "Authorization: Bearer <TOKEN>"
```

## 5. Executar testes

```bash
./gradlew test
```

Para validar só o fluxo de contrato HTTP:

```bash
./gradlew test --tests "com.adrianobispo.contract.*"
```

## 6. Critérios de pronto (planning)

- Endpoints do contrato implementados
- Erros no envelope padronizado
- Isolamento por usuário validado em testes
- Migrações Flyway versionadas

