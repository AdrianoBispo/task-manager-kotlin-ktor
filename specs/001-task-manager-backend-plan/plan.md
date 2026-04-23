# Implementation Plan: Backend Kotlin/Ktor para Gerenciador de Tarefas

**Branch**: `001-task-manager-backend-plan` | **Date**: 2026-04-23 | **Spec**: `/specs/001-task-manager-backend-plan/spec.md`  
**Input**: Feature specification from `/specs/001-task-manager-backend-plan/spec.md`

## Summary

Construir um backend Ktor em Kotlin que implemente autenticação JWT e CRUD de tarefas com isolamento por usuário, aderindo ao contrato REST compartilhado (payloads em snake_case, códigos HTTP definidos e envelope de erro padronizado), com persistência em PostgreSQL e arquitetura em camadas (routes/service/repository).

## Technical Context

**Language/Version**: Kotlin (JVM 21)  
**Primary Dependencies**: Ktor (Netty, Auth JWT, StatusPages, CORS, ContentNegotiation), kotlinx-serialization, Exposed, Flyway, Koin, bcrypt  
**Storage**: PostgreSQL (produção) + Testcontainers PostgreSQL (testes de integração)  
**Testing**: kotlin-test, Ktor `testApplication`, MockK, Testcontainers  
**Target Platform**: Linux server (containerizável)  
**Project Type**: web-service REST API  
**Performance Goals**: p95 < 250ms em operações CRUD simples; suporte inicial para ~100 req/s em ambiente acadêmico  
**Constraints**: contrato API imutável entre stacks, isolamento multi-tenant obrigatório, senha nunca em claro, CORS para clientes desacoplados  
**Scale/Scope**: MVP para usuários individuais autenticados; paginação padrão 10 itens por página

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Interoperabilidade Orientada a Contrato**: PASS — plano preserva endpoints, payloads e semântica HTTP definidos na especificação técnica.
- **II. Simplicidade Centrada no Usuário**: PASS — ciclo completo da tarefa coberto (criar, listar, atualizar, excluir, filtrar/buscar/ordenar).
- **III. Privacidade, Segurança e Isolamento**: PASS — JWT obrigatório, hash de senha e escopo por `user_id` em consultas.
- **IV. Qualidade Verificável por Testes**: PASS — estratégia explícita de testes unitários, integração e contrato.
- **V. Evolução Sustentável por Fronteiras Claras**: PASS — arquitetura em camadas com responsabilidades separadas.

## Project Structure

### Documentation (this feature)

```text
specs/001-task-manager-backend-plan/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── task-manager-api.openapi.yaml
└── tasks.md            # criado em /speckit.tasks
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── kotlin/
│   │   ├── main.kt
│   │   ├── Security.kt
│   │   ├── StatusPages.kt
│   │   ├── Serialization.kt
│   │   ├── Routing.kt
│   │   ├── auth/
│   │   ├── tasks/
│   │   ├── users/
│   │   └── shared/
│   └── resources/
│       ├── application.conf
│       └── db/migration/
└── test/
    └── kotlin/
        ├── contract/
        ├── integration/
        └── unit/
```

**Structure Decision**: Projeto único Ktor (web-service) com modularização por domínio em `src/main/kotlin` e testes em camadas em `src/test/kotlin`.

## Phase 0: Research Summary

Resultados consolidados em `/specs/001-task-manager-backend-plan/research.md`.

## Phase 1: Design & Contracts

- Modelo de dados: `/specs/001-task-manager-backend-plan/data-model.md`
- Contrato de interface: `/specs/001-task-manager-backend-plan/contracts/task-manager-api.openapi.yaml`
- Fluxo de execução: `/specs/001-task-manager-backend-plan/quickstart.md`
- Contexto do agente atualizado via script de contexto

## Constitution Check (Post-Design)

- **I. Interoperabilidade Orientada a Contrato**: PASS — OpenAPI documenta o mesmo contrato dos artefatos-base.
- **II. Simplicidade Centrada no Usuário**: PASS — defaults e validações mínimas preservam UX esperada.
- **III. Privacidade, Segurança e Isolamento**: PASS — contrato e modelo incluem restrições de escopo por usuário.
- **IV. Qualidade Verificável por Testes**: PASS — quickstart define execução de testes por camada.
- **V. Evolução Sustentável por Fronteiras Claras**: PASS — entidades, contratos e fluxo operacional desacoplados.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
