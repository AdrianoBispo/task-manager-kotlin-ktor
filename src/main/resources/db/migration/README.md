# Flyway migrations

Este diretório contém migrações SQL versionadas do backend.

## Convenções

- Nome: `V<numero>__<descricao>.sql`
- Sempre aditivo e idempotente quando possível
- Não editar migração já aplicada em ambientes compartilhados; criar nova versão

## Estado atual

- `V1__create_task_manager_schema.sql`
  - cria tabelas `usuarios` e `tarefas`
  - cria constraints de domínio (`status`, `prioridade`, `data_conclusao`)
  - cria índices básicos para consultas por usuário/status/prioridade

