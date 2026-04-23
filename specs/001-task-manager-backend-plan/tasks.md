---

description: "Task list for Backend Kotlin/Ktor para Gerenciador de Tarefas"
---

# Tasks: Backend Kotlin/Ktor para Gerenciador de Tarefas

**Input**: Design documents from `/specs/001-task-manager-backend-plan/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Included because the feature spec explicitly requires unit, integration, and contract test coverage.

**Organization**: Tasks are grouped by user story to support independent implementation and validation.

## Format: `[ID] [P?] [Story] Description with file path`

- **[P]**: Can run in parallel with other marked tasks
- **[Story]**: User story label, e.g. `[US1]`, `[US2]`
- Paths below are exact repository paths

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and alignment with the backend scope

- [ ] T001 Update Gradle dependencies and test libraries in `build.gradle.kts` for Ktor JWT, StatusPages, CORS, ContentNegotiation, Exposed, Flyway, Koin, bcrypt, MockK, and Testcontainers
- [ ] T002 Configure runtime properties and placeholders in `src/main/resources/application.conf` for `postgres.url`, `postgres.user`, `postgres.password`, `jwt.secret`, `jwt.issuer`, `jwt.audience`, and CORS settings
- [ ] T003 [P] Create the backend package structure and application bootstrap files in `src/main/kotlin/main.kt`, `src/main/kotlin/Routing.kt`, `src/main/kotlin/Security.kt`, `src/main/kotlin/StatusPages.kt`, `src/main/kotlin/Serialization.kt`, `src/main/kotlin/Koin.kt`, `src/main/kotlin/auth/`, `src/main/kotlin/tasks/`, `src/main/kotlin/users/`, and `src/main/kotlin/shared/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core platform pieces required before any user story can be implemented

**⚠️ CRITICAL**: No user story work should begin until this phase is complete

- [ ] T004 Create the initial Flyway migration for users and tasks in `src/main/resources/db/migration/V1__create_task_manager_schema.sql`
- [ ] T005 [P] Implement database wiring and Exposed table definitions for users and tasks in `src/main/kotlin/shared/DatabaseFactory.kt` and `src/main/kotlin/shared/Tables.kt`
- [ ] T006 [P] Implement shared JSON serialization and snake_case DTO support in `src/main/kotlin/Serialization.kt` and `src/main/kotlin/shared/JsonConfig.kt`
- [ ] T007 [P] Implement centralized API error envelope models and StatusPages mappings in `src/main/kotlin/StatusPages.kt` and `src/main/kotlin/shared/ApiError.kt`
- [ ] T008 [P] Implement JWT configuration and authenticated principal helpers in `src/main/kotlin/Security.kt` and `src/main/kotlin/shared/AuthPrincipal.kt`
- [ ] T009 [P] Implement Koin module composition, CORS, and application route registration in `src/main/kotlin/Koin.kt` and `src/main/kotlin/Routing.kt`

**Checkpoint**: Foundation ready — user story implementation can now begin

---

## Phase 3: User Story 1 - Authentication register/login (Priority: P1)

**Goal**: Allow a user to register, log in, and receive a JWT plus the authenticated user payload

**Independent Test**: `POST /api/auth/register` returns `201` with `usuario` and `token`; `POST /api/auth/login` returns `200`; invalid credentials and duplicate email return the contract error envelope

### Tests for User Story 1

- [ ] T010 [P] [US1] Define auth contract tests for register and login in `src/test/kotlin/contract/AuthContractTest.kt`
- [ ] T011 [P] [US1] Add unit tests for registration validation, duplicate email handling, and invalid login in `src/test/kotlin/unit/AuthServiceTest.kt`

### Implementation for User Story 1

- [ ] T012 [P] [US1] Define auth request/response DTOs and user view models in `src/main/kotlin/auth/AuthDtos.kt`
- [ ] T013 [P] [US1] Implement the user repository with unique email lookup and insert operations in `src/main/kotlin/users/UserRepository.kt`
- [ ] T014 [US1] Implement the auth service with bcrypt hashing, login verification, JWT issuance, and last-login updates in `src/main/kotlin/auth/AuthService.kt`
- [ ] T015 [US1] Implement `POST /api/auth/register` and `POST /api/auth/login` routes in `src/main/kotlin/auth/AuthRoutes.kt`
- [ ] T016 [US1] Wire auth routes into the application module in `src/main/kotlin/Routing.kt` and `src/main/kotlin/main.kt`

**Checkpoint**: Authentication should work end-to-end and be independently demonstrable

---

## Phase 4: User Story 2 - Criar tarefa e ver a tarefa criada na própria lista (Priority: P2)

**Goal**: Let an authenticated user create a task with default values and retrieve their own tasks

**Independent Test**: `POST /api/tasks` returns `201`, applies default `status=PENDENTE` and `prioridade=MEDIA` when omitted, and the created task is visible only to the owning user through `GET /api/tasks`

### Tests for User Story 2

- [ ] T017 [P] [US2] Define contract tests for task creation and baseline authenticated listing in `src/test/kotlin/contract/TaskCreateListContractTest.kt`
- [ ] T018 [P] [US2] Add an integration test proving a created task is visible only to the owning user in `src/test/kotlin/integration/TaskVisibilityTest.kt`

### Implementation for User Story 2

- [ ] T019 [P] [US2] Define task request/response DTOs and list metadata models in `src/main/kotlin/tasks/TaskDtos.kt`
- [ ] T020 [P] [US2] Implement the task repository with create and owner-scoped list operations in `src/main/kotlin/tasks/TaskRepository.kt`
- [ ] T021 [US2] Implement the task service for creation defaults, title validation, and owner assignment in `src/main/kotlin/tasks/TaskService.kt`
- [ ] T022 [US2] Implement authenticated `POST /api/tasks` and baseline `GET /api/tasks` routes in `src/main/kotlin/tasks/TaskRoutes.kt`

**Checkpoint**: Task creation and owner-only listing should work end-to-end

---

## Phase 5: User Story 3 - Atualizar status da tarefa (Priority: P3)

**Goal**: Allow the owner to update task status while preserving transition rules and timestamps

**Independent Test**: `PATCH /api/tasks/{id}` accepts valid transitions, rejects invalid transitions, and updates `data_conclusao` when moving to or from `CONCLUIDA` according to the model rules

### Tests for User Story 3

- [ ] T023 [P] [US3] Define contract tests for allowed and disallowed status transitions in `src/test/kotlin/contract/TaskStatusUpdateContractTest.kt`
- [ ] T024 [P] [US3] Add unit tests for allowed and disallowed task status transitions in `src/test/kotlin/unit/TaskStatusPolicyTest.kt`

### Implementation for User Story 3

- [ ] T025 [US3] Extend the task repository with find-by-id and status update operations scoped by `id_usuario` in `src/main/kotlin/tasks/TaskRepository.kt`
- [ ] T026 [US3] Implement task status transition logic and `data_conclusao` maintenance in `src/main/kotlin/tasks/TaskService.kt`
- [ ] T027 [US3] Implement `PATCH /api/tasks/{id}` route behavior with `403` and `404` mapping in `src/main/kotlin/tasks/TaskRoutes.kt`
- [ ] T028 [US3] Add an integration test for owner-only status updates in `src/test/kotlin/integration/TaskStatusUpdateIntegrationTest.kt`

**Checkpoint**: Status updates should be secure, validated, and persist correctly

---

## Phase 6: User Story 4 - Editar e excluir tarefa (Priority: P4)

**Goal**: Let the owner partially edit a task and delete it with the required HTTP semantics

**Independent Test**: `PATCH /api/tasks/{id}` returns the updated task on success, and `DELETE /api/tasks/{id}` returns `204`; both operations reject cross-user access with the contract error envelope

### Tests for User Story 4

- [ ] T029 [P] [US4] Define contract tests for partial edit and delete behavior in `src/test/kotlin/contract/TaskEditDeleteContractTest.kt`
- [ ] T030 [P] [US4] Add unit tests for partial validation, not-found handling, and delete behavior in `src/test/kotlin/unit/TaskEditDeleteServiceTest.kt`

### Implementation for User Story 4

- [ ] T031 [US4] Extend the task repository with partial update and delete operations scoped by `id_usuario` in `src/main/kotlin/tasks/TaskRepository.kt`
- [ ] T032 [US4] Implement partial update handling and delete semantics in `src/main/kotlin/tasks/TaskService.kt`
- [ ] T033 [US4] Implement `PATCH /api/tasks/{id}` and `DELETE /api/tasks/{id}` response handling in `src/main/kotlin/tasks/TaskRoutes.kt`
- [ ] T034 [US4] Add an integration test for editing and deleting only the authenticated user’s task in `src/test/kotlin/integration/TaskEditDeleteIntegrationTest.kt`

**Checkpoint**: Editing and deletion should be independently testable and owner-safe

---

## Phase 7: User Story 5 - Buscar, filtrar e ordenar tarefas (Priority: P5)

**Goal**: Support the full list query contract with filters, search, ordering, and pagination

**Independent Test**: `GET /api/tasks` supports `status`, `prioridade`, `q`, `sort`, `page`, and `limit`, returns `TaskListResponse`, and the `meta` block matches the result set

### Tests for User Story 5

- [ ] T035 [P] [US5] Define contract tests for search, filtering, ordering, and pagination in `src/test/kotlin/contract/TaskQueryContractTest.kt`
- [ ] T036 [P] [US5] Add unit tests for query normalization, sorting, and pagination meta calculation in `src/test/kotlin/unit/TaskQuerySpecTest.kt`

### Implementation for User Story 5

- [ ] T037 [US5] Extend the task repository listing query builder with filters, text search, ordering, and pagination in `src/main/kotlin/tasks/TaskRepository.kt`
- [ ] T038 [US5] Implement list query normalization and response metadata assembly in `src/main/kotlin/tasks/TaskService.kt`
- [ ] T039 [US5] Update `GET /api/tasks` to parse query parameters and return `TaskListResponse` in `src/main/kotlin/tasks/TaskRoutes.kt`
- [ ] T040 [US5] Add an integration test for combined search/filter/sort/pagination behavior in `src/test/kotlin/integration/TaskQueryIntegrationTest.kt`

**Checkpoint**: The list endpoint should fully match the OpenAPI contract

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T041 [P] Remove or replace obsolete sample code and unused helper classes in `src/main/kotlin/HelloService.kt`, `src/main/kotlin/UsersService.kt`, `src/main/kotlin/CitySchema.kt`, `src/main/kotlin/Http.kt`, and `src/main/kotlin/Postgres.kt`
- [ ] T042 [P] Validate the quickstart flow and reconcile any remaining setup gaps in `specs/001-task-manager-backend-plan/quickstart.md`, `src/main/resources/application.conf`, and `src/main/resources/db/migration/`
- [ ] T043 [P] Verify snake_case alignment and error-envelope consistency across all DTOs and routes under `src/main/kotlin/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion; blocks all user stories
- **User Stories (Phase 3+)**: Depend on the foundational platform pieces; recommended order is P1 → P2 → P3 → P4 → P5
- **Polish (Final Phase)**: Depends on completion of the desired user stories

### User Story Dependencies

- **US1 Authentication**: Can start after Foundational; no dependency on task data
- **US2 Task creation/list baseline**: Can start after Foundational, but is listed after US1 for MVP sequencing
- **US3 Status updates**: Depends on the task module established in US2
- **US4 Edit/delete**: Depends on the task module and status handling established in US2-US3
- **US5 Search/filter/sort**: Depends on the baseline task list implementation from US2 and can reuse the same route and repository layer

### Within Each User Story

- Tests are written before the implementation tasks they validate
- DTOs and models before service logic
- Service logic before route wiring
- Repository changes before route behavior that depends on them
- Each story should be complete and independently demoable before moving to the next priority

### Parallel Opportunities

- Setup task T003 can run in parallel with other setup work after the initial dependency decision is made
- Foundational tasks T005-T009 can run in parallel because they touch separate files and layers
- Within each user story, the contract-test and unit-test tasks can usually run in parallel
- DTO/model tasks can usually run in parallel with test-writing tasks for the same story
- Different user stories can be implemented by different developers once the foundational phase is complete

---

## Parallel Example: User Story 2

```bash
# These can proceed together once the foundation is ready:
Task: "Define task request/response DTOs and list metadata models in `src/main/kotlin/tasks/TaskDtos.kt`"
Task: "Add an integration test proving a created task is visible only to the owning user in `src/test/kotlin/integration/TaskVisibilityTest.kt`"
```

---

## Implementation Strategy

### MVP First

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. Validate authentication end-to-end before proceeding

### Incremental Delivery

1. Foundation ready
2. Add authentication and verify login/register independently
3. Add task creation and baseline owner-only listing
4. Add status updates
5. Add edit/delete behavior
6. Add search/filter/sort and pagination
7. Finish with polish and cleanup

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. After foundation:
   - Developer A: Authentication
   - Developer B: Task creation/list baseline
   - Developer C: Test scaffolding and contract validation
3. Later task stories can be split across developers while keeping route/service/repository boundaries clear

---

## Notes

- `[P]` tasks should touch different files and have no dependency on incomplete work
- `[Story]` labels are required for user-story phases only
- Each story should remain independently testable
- Avoid cross-story coupling that breaks incremental delivery
- The contract in `contracts/task-manager-api.openapi.yaml` is the source of truth for request/response shapes and HTTP status codes

