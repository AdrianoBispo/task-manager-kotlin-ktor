# Phase 0 Research — Backend Kotlin/Ktor

## 1) Persistência e acesso a dados

- **Decision**: PostgreSQL como banco principal, Exposed DSL para camada de repositório e Flyway para migrações versionadas.
- **Rationale**: PostgreSQL atende ACID e filtros/ordenação/paginação com boa previsibilidade; Exposed mantém type-safety em Kotlin; Flyway garante evolução controlada de schema.
- **Alternatives considered**:
  - Hibernate/JPA: mais abstração e curva maior para escopo acadêmico.
  - SQL puro: maior verbosidade e menor padronização de acesso.
  - Banco NoSQL: desalinhado ao requisito de persistência relacional/ACID.

## 2) Estratégia de serialização snake_case

- **Decision**: Padronizar DTOs de contrato com `@SerialName` para campos externos e manter nomes idiomáticos em Kotlin internamente.
- **Rationale**: Garante conformidade explícita do contrato em endpoints críticos e evita ambiguidades entre convenções camelCase/snake_case.
- **Alternatives considered**:
  - Naming strategy global do serializer: reduz boilerplate, mas pode ocultar divergências de contrato em campos sensíveis.
  - DTOs já em snake_case no código: reduz legibilidade idiomática em Kotlin.

## 3) Autenticação e segurança

- **Decision**: JWT stateless com HS256, claim `sub` (user id), `exp` de 24h, validação de issuer/audience, e bcrypt para hash de senha.
- **Rationale**: Alinha ao contrato técnico e aos princípios constitucionais de segurança e isolamento.
- **Alternatives considered**:
  - Sessão em memória: não atende requisito stateless/interoperável.
  - Algoritmos de hash fracos (SHA simples): inadequados para senha.

## 4) Isolamento multi-tenant

- **Decision**: Todas as operações em tarefas incluem filtro por `user_id` extraído do principal JWT; respostas 403/404 conforme contrato.
- **Rationale**: Evita vazamento de dados entre usuários e cumpre regra RF04.1.
- **Alternatives considered**:
  - Verificação apenas na camada de rota: maior risco de bypass acidental.
  - ACL externa complexa: excesso para escopo do MVP.

## 5) Estratégia de testes

- **Decision**: Pirâmide enxuta com testes unitários (serviço), integração (repositório + banco real via Testcontainers), e contrato (endpoints com testApplication).
- **Rationale**: Cobre regras de negócio, isolamento de segurança e conformidade HTTP/JSON.
- **Alternatives considered**:
  - Somente testes de integração: execução lenta e diagnóstico difícil.
  - Somente unitários: baixa confiança no contrato HTTP.

## 6) Tratamento de erros

- **Decision**: Centralizar mapeamento de exceções com StatusPages para envelope padrão `{status, erro, mensagem, detalhes?}`.
- **Rationale**: Mantém consistência entre stacks e melhora UX de erro no cliente.
- **Alternatives considered**:
  - Tratamento local por rota: duplicação e inconsistência de respostas.

## 7) CORS e interoperabilidade de cliente

- **Decision**: Configurar CORS para métodos GET/POST/PATCH/DELETE/OPTIONS e headers Content-Type/Authorization.
- **Rationale**: Necessário para frontends desacoplados em portas/domínios distintos.
- **Alternatives considered**:
  - Wildcard sem restrições: menos seguro para evolução futura.

## Resultado

Não há itens pendentes de **NEEDS CLARIFICATION** após consolidação das decisões acima.

