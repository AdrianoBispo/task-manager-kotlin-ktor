# Feature Specification: Backend Kotlin/Ktor para Gerenciador de Tarefas

**Feature Branch**: `001-task-manager-backend-plan`  
**Created**: 2026-04-23  
**Status**: Draft consolidado a partir de artefatos existentes  
**Fontes**: `specs/PRD - Gerenciador de Tarefas.md`, `specs/Especificação Técnica e API.md`, `specs/Plan Ktor Backend.md`

## Contexto e Objetivo

Implementar um backend RESTful em Kotlin + Ktor para suportar o ciclo de vida completo de tarefas de usuários autenticados, preservando o contrato de API compartilhado para interoperabilidade multi-stack.

## Escopo Funcional

### Módulo de autenticação
- Cadastro de usuário (`POST /api/auth/register`) com `nome`, `email`, `senha`
- Login (`POST /api/auth/login`) com `email`, `senha`
- Retorno de usuário autenticado + JWT

### Módulo de tarefas (autenticado)
- Listagem com paginação, filtros e ordenação (`GET /api/tasks`)
- Criação (`POST /api/tasks`)
- Atualização parcial (`PATCH /api/tasks/{id}`)
- Exclusão (`DELETE /api/tasks/{id}`)

### Regras de domínio centrais
- Título obrigatório (3..100 caracteres)
- Email único no cadastro
- Status padrão: `PENDENTE`
- Prioridade padrão: `MEDIA`
- Isolamento multi-tenant: usuário só acessa suas próprias tarefas

## Requisitos de Contrato

- API REST em JSON
- Payload externo em `snake_case`
- JWT no header `Authorization: Bearer <token>`
- Erros padronizados com envelope JSON (`status`, `erro`, `mensagem`, `detalhes?`)
- Códigos HTTP obrigatórios: 200, 201, 204, 400, 401, 403, 404, 500

## Requisitos Não Funcionais

- Persistência em banco relacional (PostgreSQL)
- Segurança de senha com hash (bcrypt)
- CORS habilitado para frontends desacoplados
- Testes em camadas (unitário, integração, contrato)
- Estrutura de código com fronteiras claras (routes, service, repository)

## Histórias e Critérios de Aceite (consolidados)

1. **Cadastro/Login**: usuário cria conta e autentica com feedback claro em erros de credencial.
2. **Criar tarefa**: nova tarefa aparece imediatamente ao consultar listagem.
3. **Atualizar status**: transições rápidas (ex.: para `CONCLUIDA`) e persistidas.
4. **Editar/Excluir tarefa**: edição retorna entidade atualizada; exclusão retorna `204`.
5. **Buscar/Filtrar/Ordenar**: filtros compostos por status/prioridade e busca textual em título/descrição.

## Exclusões de Escopo para esta fase

- Fluxo completo de troca de senha (avançado/opcional)
- Métricas analíticas avançadas de produtividade
- Funcionalidades de frontend

## Dependências técnicas previstas

- Ktor (Netty, Authentication JWT, StatusPages, ContentNegotiation, CORS)
- Kotlinx Serialization
- PostgreSQL + Exposed DSL + Flyway
- Koin (injeção de dependência)
- Testes com Ktor testApplication, MockK e Testcontainers

