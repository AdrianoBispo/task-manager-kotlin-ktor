# Quickstart — Backend Kotlin/Ktor

## 1. Pré-requisitos

- JDK 21
- Docker (para testes com Testcontainers)
- PostgreSQL local (opcional se usar container no ambiente de desenvolvimento)

## 2. Configuração

1. Ajustar `src/main/resources/application.conf` com JWT e banco:
   - `jwt.secret`
   - `jwt.issuer`
   - `jwt.audience`
   - `postgres.url`
   - `postgres.user`
   - `postgres.password`
2. Garantir diretório de migrações Flyway em `src/main/resources/db/migration`.

## 3. Executar aplicação

```bash
./gradlew run
```

Servidor esperado em `http://localhost:8080`.

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

## 6. Critérios de pronto (planning)

- Endpoints do contrato implementados
- Erros no envelope padronizado
- Isolamento por usuário validado em testes
- Migrações Flyway versionadas

