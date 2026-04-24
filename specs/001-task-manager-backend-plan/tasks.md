---

description: "Task list for Backend Kotlin/Ktor para Gerenciador de Tarefas"
---

# Tasks: Backend Kotlin/Ktor para Gerenciador de Tarefas

**Entrada**: Documentos de design em `/specs/001-task-manager-backend-plan/`
**Pré-requisitos**: plan.md (obrigatório), spec.md (obrigatório para histórias de usuário), research.md, data-model.md, contracts/

**Testes**: Incluídos porque a especificação da funcionalidade exige explicitamente cobertura de testes unitários, de integração e de contrato.

**Organização**: As tarefas são agrupadas por história de usuário para suportar implementação e validação independentes.

## Formato: `[ID] [P?] [Story] Descrição com caminho do arquivo`

- **[P]**: Pode ser executada em paralelo com outras tarefas marcadas
- **[Story]**: Rótulo da história de usuário, ex.: `[US1]`, `[US2]`
- Os caminhos abaixo são caminhos exatos do repositório

## Fase 1: Setup (Infraestrutura Compartilhada)

**Objetivo**: Inicialização do projeto e alinhamento com o escopo do backend

- [X] T001 Atualizar dependências do Gradle e bibliotecas de teste em `build.gradle.kts` para Ktor JWT, StatusPages, CORS, ContentNegotiation, Exposed, Flyway, Koin, bcrypt, MockK e Testcontainers
- [X] T002 Configurar propriedades de runtime e placeholders em `src/main/resources/application.conf` para `postgres.url`, `postgres.user`, `postgres.password`, `jwt.secret`, `jwt.issuer`, `jwt.audience` e configurações de CORS
- [X] T003 [P] Criar a estrutura de pacotes do backend e os arquivos de bootstrap da aplicação em `src/main/kotlin/main.kt`, `src/main/kotlin/Routing.kt`, `src/main/kotlin/Security.kt`, `src/main/kotlin/StatusPages.kt`, `src/main/kotlin/Serialization.kt`, `src/main/kotlin/Koin.kt`, `src/main/kotlin/auth/`, `src/main/kotlin/tasks/`, `src/main/kotlin/users/` e `src/main/kotlin/shared/`

---

## Fase 2: Fundacional (Pré-requisitos Bloqueantes)

**Objetivo**: Componentes centrais da plataforma exigidos antes de qualquer história de usuário ser implementada

**⚠️ CRÍTICO**: Nenhum trabalho de história de usuário deve começar até que esta fase esteja concluída

- [X] T004 Criar a migração inicial do Flyway para usuários e tarefas em `src/main/resources/db/migration/V1__create_task_manager_schema.sql`
- [X] T005 [P] Implementar configuração de banco de dados e definições de tabelas Exposed para usuários e tarefas em `src/main/kotlin/shared/DatabaseFactory.kt` e `src/main/kotlin/shared/Tables.kt`
- [X] T006 [P] Implementar serialização JSON compartilhada e suporte a DTOs em snake_case em `src/main/kotlin/Serialization.kt` e `src/main/kotlin/shared/JsonConfig.kt`
- [X] T007 [P] Implementar modelos centralizados de envelope de erro da API e mapeamentos de StatusPages em `src/main/kotlin/StatusPages.kt` e `src/main/kotlin/shared/ApiError.kt`
- [X] T008 [P] Implementar configuração de JWT e helpers de principal autenticado em `src/main/kotlin/Security.kt` e `src/main/kotlin/shared/AuthPrincipal.kt`
- [X] T009 [P] Implementar composição de módulos Koin, CORS e registro de rotas da aplicação em `src/main/kotlin/Koin.kt` e `src/main/kotlin/Routing.kt`

**Checkpoint**: Fundação pronta — a implementação das histórias de usuário pode começar

---

## Fase 3: História de Usuário 1 - Autenticação cadastro/login (Prioridade: P1)

**Objetivo**: Permitir que um usuário se cadastre, faça login e receba um JWT mais os dados do usuário autenticado

**Teste Independente**: `POST /api/auth/register` retorna `201` com `usuario` e `token`; `POST /api/auth/login` retorna `200`; credenciais inválidas e e-mail duplicado retornam o envelope de erro de contrato

### Testes da História de Usuário 1

- [X] T010 [P] [US1] Definir testes de contrato de autenticação para cadastro e login em
  `src/test/kotlin/contract/AuthContractTest.kt`
- [X] T011 [P] [US1] Adicionar testes unitários para validação de cadastro, tratamento de e-mail duplicado e login
  inválido em `src/test/kotlin/unit/AuthServiceTest.kt`

### Implementação da História de Usuário 1

- [X] T012 [P] [US1] Definir DTOs de request/response de autenticação e view models de usuário em
  `src/main/kotlin/auth/AuthDtos.kt`
- [X] T013 [P] [US1] Implementar o repositório de usuários com busca por e-mail único e operações de inserção em
  `src/main/kotlin/users/UserRepository.kt`
- [X] T014 [US1] Implementar o serviço de autenticação com hash bcrypt, verificação de login, emissão de JWT e
  atualização do último login em `src/main/kotlin/auth/AuthService.kt`
- [X] T015 [US1] Implementar as rotas `POST /api/auth/register` e `POST /api/auth/login` em
  `src/main/kotlin/auth/AuthRoutes.kt`
- [X] T016 [US1] Integrar as rotas de autenticação ao módulo da aplicação em `src/main/kotlin/Routing.kt` e
  `src/main/kotlin/main.kt`

**Checkpoint**: A autenticação deve funcionar de ponta a ponta e ser demonstrável de forma independente

---

## Fase 4: História de Usuário 2 - Criar tarefa e ver a tarefa criada na própria lista (Prioridade: P2)

**Objetivo**: Permitir que um usuário autenticado crie uma tarefa com valores padrão e recupere suas próprias tarefas

**Teste Independente**: `POST /api/tasks` retorna `201`, aplica `status=PENDENTE` e `prioridade=MEDIA` por padrão quando omitidos, e a tarefa criada fica visível apenas para o usuário dono via `GET /api/tasks`

### Testes da História de Usuário 2

- [X] T017 [P] [US2] Definir testes de contrato para criação de tarefa e listagem autenticada básica em `src/test/kotlin/contract/TaskCreateListContractTest.kt`
- [X] T018 [P] [US2] Adicionar um teste de integração comprovando que uma tarefa criada é visível apenas para o usuário dono em `src/test/kotlin/integration/TaskVisibilityTest.kt`

### Implementação da História de Usuário 2

- [X] T019 [P] [US2] Definir DTOs de request/response de tarefa e modelos de metadados da lista em `src/main/kotlin/tasks/TaskDtos.kt`
- [X] T020 [P] [US2] Implementar o repositório de tarefas com operações de criação e listagem com escopo por dono em `src/main/kotlin/tasks/TaskRepository.kt`
- [X] T021 [US2] Implementar o serviço de tarefas para valores padrão de criação, validação de título e atribuição de dono em `src/main/kotlin/tasks/TaskService.kt`
- [X] T022 [US2] Implementar as rotas autenticadas `POST /api/tasks` e a rota base `GET /api/tasks` em `src/main/kotlin/tasks/TaskRoutes.kt`

**Checkpoint**: Criação de tarefa e listagem apenas do dono devem funcionar de ponta a ponta

---

## Fase 5: História de Usuário 3 - Atualizar status da tarefa (Prioridade: P3)

**Objetivo**: Permitir que o dono atualize o status da tarefa preservando regras de transição e timestamps

**Teste Independente**: `PATCH /api/tasks/{id}` aceita transições válidas, rejeita transições inválidas e atualiza `data_conclusao` ao mover para ou de `CONCLUIDA` conforme as regras do modelo

### Testes da História de Usuário 3

- [X] T023 [P] [US3] Definir testes de contrato para transições de status permitidas e não permitidas em `src/test/kotlin/contract/TaskStatusUpdateContractTest.kt`
- [X] T024 [P] [US3] Adicionar testes unitários para transições de status de tarefa permitidas e não permitidas em `src/test/kotlin/unit/TaskStatusPolicyTest.kt`

### Implementação da História de Usuário 3

- [X] T025 [US3] Estender o repositório de tarefas com operações de busca por id e atualização de status com escopo por `id_usuario` em `src/main/kotlin/tasks/TaskRepository.kt`
- [X] T026 [US3] Implementar lógica de transição de status da tarefa e manutenção de `data_conclusao` em `src/main/kotlin/tasks/TaskService.kt`
- [X] T027 [US3] Implementar o comportamento da rota `PATCH /api/tasks/{id}` com mapeamento de `403` e `404` em `src/main/kotlin/tasks/TaskRoutes.kt`
- [X] T028 [US3] Adicionar um teste de integração para atualização de status apenas pelo dono em `src/test/kotlin/integration/TaskStatusUpdateIntegrationTest.kt`

**Checkpoint**: Atualizações de status devem ser seguras, validadas e persistidas corretamente

---

## Fase 6: História de Usuário 4 - Editar e excluir tarefa (Prioridade: P4)

**Objetivo**: Permitir que o dono edite parcialmente uma tarefa e a exclua com a semântica HTTP exigida

**Teste Independente**: `PATCH /api/tasks/{id}` retorna a tarefa atualizada em caso de sucesso, e `DELETE /api/tasks/{id}` retorna `204`; ambas as operações rejeitam acesso entre usuários com o envelope de erro de contrato

### Testes da História de Usuário 4

- [X] T029 [P] [US4] Definir testes de contrato para comportamento de edição parcial e exclusão em `src/test/kotlin/contract/TaskEditDeleteContractTest.kt`
- [X] T030 [P] [US4] Adicionar testes unitários para validação parcial, tratamento de não encontrado e comportamento de exclusão em `src/test/kotlin/unit/TaskEditDeleteServiceTest.kt`

### Implementação da História de Usuário 4

- [X] T031 [US4] Estender o repositório de tarefas com operações de atualização parcial e exclusão com escopo por `id_usuario` em `src/main/kotlin/tasks/TaskRepository.kt`
- [X] T032 [US4] Implementar tratamento de atualização parcial e semântica de exclusão em `src/main/kotlin/tasks/TaskService.kt`
- [X] T033 [US4] Implementar o tratamento de resposta de `PATCH /api/tasks/{id}` e `DELETE /api/tasks/{id}` em `src/main/kotlin/tasks/TaskRoutes.kt`
- [X] T034 [US4] Adicionar um teste de integração para edição e exclusão apenas da tarefa do usuário autenticado em `src/test/kotlin/integration/TaskEditDeleteIntegrationTest.kt`

**Checkpoint**: Edição e exclusão devem ser testáveis de forma independente e seguras para o dono

---

## Fase 7: História de Usuário 5 - Buscar, filtrar e ordenar tarefas (Prioridade: P5)

**Objetivo**: Suportar o contrato completo de consulta de listagem com filtros, busca, ordenação e paginação

**Teste Independente**: `GET /api/tasks` suporta `status`, `prioridade`, `q`, `sort`, `page` e `limit`, retorna `TaskListResponse`, e o bloco `meta` corresponde ao conjunto de resultados

### Testes da História de Usuário 5

- [X] T035 [P] [US5] Definir testes de contrato para busca, filtragem, ordenação e paginação em `src/test/kotlin/contract/TaskQueryContractTest.kt`
- [X] T036 [P] [US5] Adicionar testes unitários para normalização de consulta, ordenação e cálculo de metadados de paginação em `src/test/kotlin/unit/TaskQuerySpecTest.kt`

### Implementação da História de Usuário 5

- [X] T037 [US5] Estender o construtor de consulta de listagem do repositório de tarefas com filtros, busca textual, ordenação e paginação em `src/main/kotlin/tasks/TaskRepository.kt`
- [X] T038 [US5] Implementar normalização da consulta de listagem e montagem dos metadados de resposta em `src/main/kotlin/tasks/TaskService.kt`
- [X] T039 [US5] Atualizar `GET /api/tasks` para interpretar parâmetros de query e retornar `TaskListResponse` em `src/main/kotlin/tasks/TaskRoutes.kt`
- [X] T040 [US5] Adicionar um teste de integração para comportamento combinado de busca/filtro/ordenação/paginação em `src/test/kotlin/integration/TaskQueryIntegrationTest.kt`

**Checkpoint**: O endpoint de listagem deve corresponder totalmente ao contrato OpenAPI

---

## Fase 8: Acabamento e Preocupações Transversais

**Objetivo**: Melhorias que afetam múltiplas histórias de usuário

- [X] T041 [P] Remover ou substituir código de exemplo obsoleto e classes helper não utilizadas em `src/main/kotlin/HelloService.kt`, `src/main/kotlin/UsersService.kt`, `src/main/kotlin/CitySchema.kt`, `src/main/kotlin/Http.kt` e `src/main/kotlin/Postgres.kt`
- [X] T042 [P] Validar o fluxo de quickstart e reconciliar lacunas de setup restantes em `specs/001-task-manager-backend-plan/quickstart.md`, `src/main/resources/application.conf` e `src/main/resources/db/migration/`
- [X] T043 [P] Verificar alinhamento de snake_case e consistência do envelope de erro em todos os DTOs e rotas sob `src/main/kotlin/`

---

## Dependências e Ordem de Execução

### Dependências entre Fases

- **Setup (Fase 1)**: Sem dependências; pode começar imediatamente
- **Fundacional (Fase 2)**: Depende da conclusão do Setup; bloqueia todas as histórias de usuário
- **Histórias de Usuário (Fase 3+)**: Dependem dos componentes fundacionais da plataforma; a ordem recomendada é P1 → P2 → P3 → P4 → P5
- **Acabamento (Fase Final)**: Depende da conclusão das histórias de usuário desejadas

### Dependências entre Histórias de Usuário

- **US1 Autenticação**: Pode começar após a fase Fundacional; sem dependência dos dados de tarefas
- **US2 Criação/listagem base de tarefas**: Pode começar após a fase Fundacional, mas está listada após a US1 para sequenciamento de MVP
- **US3 Atualizações de status**: Depende do módulo de tarefas estabelecido na US2
- **US4 Editar/excluir**: Depende do módulo de tarefas e do tratamento de status estabelecidos em US2-US3
- **US5 Buscar/filtrar/ordenar**: Depende da implementação base da listagem de tarefas da US2 e pode reutilizar a mesma camada de rota e repositório

### Dentro de Cada História de Usuário

- Os testes são escritos antes das tarefas de implementação que eles validam
- DTOs e modelos vêm antes da lógica de serviço
- A lógica de serviço vem antes da integração das rotas
- Alterações de repositório vêm antes do comportamento de rota que depende delas
- Cada história deve estar completa e ser demonstrável de forma independente antes de avançar para a próxima prioridade

### Oportunidades de Paralelismo

- A tarefa T003 de Setup pode rodar em paralelo com outros trabalhos de setup após a decisão inicial de dependências
- As tarefas fundacionais T005-T009 podem rodar em paralelo porque tocam arquivos e camadas separadas
- Dentro de cada história de usuário, tarefas de teste de contrato e teste unitário normalmente podem rodar em paralelo
- Tarefas de DTO/modelo normalmente podem rodar em paralelo com tarefas de escrita de testes da mesma história
- Histórias de usuário diferentes podem ser implementadas por desenvolvedores diferentes após a conclusão da fase fundacional

---

## Exemplo de Paralelismo: História de Usuário 2

```bash
# Estas podem avançar juntas assim que a fundação estiver pronta:
Tarefa: "Definir DTOs de request/response de tarefa e modelos de metadados da lista em `src/main/kotlin/tasks/TaskDtos.kt`"
Tarefa: "Adicionar um teste de integração comprovando que uma tarefa criada é visível apenas para o usuário dono em `src/test/kotlin/integration/TaskVisibilityTest.kt`"
```

---

## Estratégia de Implementação

### MVP Primeiro

1. Completar Fase 1: Setup
2. Completar Fase 2: Fundacional
3. Completar Fase 3: História de Usuário 1
4. Validar autenticação de ponta a ponta antes de prosseguir

### Entrega Incremental

1. Fundação pronta
2. Adicionar autenticação e verificar login/cadastro de forma independente
3. Adicionar criação de tarefa e listagem base apenas do dono
4. Adicionar atualizações de status
5. Adicionar comportamento de edição/exclusão
6. Adicionar busca/filtro/ordenação e paginação
7. Finalizar com acabamento e limpeza

### Estratégia de Time em Paralelo

Com múltiplos desenvolvedores:

1. O time conclui Setup + Fundacional em conjunto
2. Após a fundação:
   - Desenvolvedor A: Autenticação
   - Desenvolvedor B: Criação/listagem base de tarefas
   - Desenvolvedor C: Estrutura de testes e validação de contrato
3. Histórias de tarefa posteriores podem ser divididas entre desenvolvedores mantendo claras as fronteiras de rota/serviço/repositório

---

## Observações

- Tarefas `[P]` devem tocar arquivos diferentes e não ter dependência de trabalho incompleto
- Rótulos `[Story]` são obrigatórios apenas para fases de história de usuário
- Cada história deve permanecer testável de forma independente
- Evite acoplamento entre histórias que quebre a entrega incremental
- O contrato em `contracts/task-manager-api.openapi.yaml` é a fonte da verdade para formatos de request/response e códigos de status HTTP

