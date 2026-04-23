# Data Model — Backend Kotlin/Ktor

## Entity: User

- **id**: UUID (PK)
- **nome**: String (3..120, obrigatório)
- **email**: String (formato válido, obrigatório, único)
- **senha_hash**: String (obrigatório, nunca retornado em API)
- **data_criacao**: Instant (obrigatório)
- **ultimo_login**: Instant? (opcional)

### Validation rules
- Email único no sistema.
- Senha mínima de 6 caracteres no cadastro antes do hash.

## Entity: Task

- **id**: UUID (PK)
- **id_usuario**: UUID (FK -> User.id, obrigatório)
- **titulo**: String (3..100, obrigatório)
- **descricao**: String? (opcional)
- **status**: Enum `PENDENTE | EM_ANDAMENTO | CONCLUIDA` (default `PENDENTE`)
- **prioridade**: Enum `BAIXA | MEDIA | ALTA` (default `MEDIA`)
- **data_vencimento**: Instant? (opcional)
- **data_criacao**: Instant (obrigatório)
- **data_atualizacao**: Instant (obrigatório)
- **data_conclusao**: Instant? (preenchido quando status = `CONCLUIDA`)

### Validation rules
- `titulo` obrigatório com tamanho entre 3 e 100.
- `data_conclusao` só pode existir quando `status = CONCLUIDA`.
- Escopo de acesso sempre por `id_usuario`.

## Relationship

- User 1 ---- N Task
- Exclusão de User: fora de escopo do MVP (definir política futura de cascata/soft-delete).

## State transitions (Task.status)

- `PENDENTE -> EM_ANDAMENTO`
- `PENDENTE -> CONCLUIDA`
- `EM_ANDAMENTO -> CONCLUIDA`
- `EM_ANDAMENTO -> PENDENTE` (permitido para replanejamento)
- `CONCLUIDA -> EM_ANDAMENTO` (permitido, deve limpar `data_conclusao`)

## Query capabilities mapped from requirements

- Filtros: `status`, `prioridade`
- Busca textual: `q` em `titulo` e `descricao`
- Ordenação: `data_vencimento`, `data_criacao` (asc/desc)
- Paginação: `page`, `limit`

