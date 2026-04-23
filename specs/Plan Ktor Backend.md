# **Plano Técnico Detalhado: Back-end Kotlin \+ Ktor**

## **1\. Visão Geral e Paradigma**

A escolha do **Ktor** em conjunto com **Kotlin** visa explorar o desenvolvimento de APIs assíncronas de altíssima performance utilizando *Coroutines*. Diferente de frameworks pesados e opinativos (como Spring Boot), o Ktor é modular, explícito e constrói a aplicação através da instalação de *Plugins*. Este projeto servirá como estudo de caso para arquiteturas leves, não-bloqueantes e altamente idiomáticas.

## **2\. Decisões de Stack e Tecnologias**

* **Motor (Engine):** Utilizaremos o **Netty**. Embora o Ktor suporte CIO (Coroutines I/O puramente em Kotlin), o Netty é um servidor assíncrono maduro, amplamente testado na indústria e oferece excelente performance sob carga, além de documentação vasta.  
* **Serialização de Dados:** O plugin ContentNegotiation será configurado juntamente com a biblioteca nativa kotlinx-serialization.  
  * *Mapeamento JSON:* Para respeitar o Contrato de API (que exige snake\_case), configuraremos o serializador para traduzir automaticamente as *Data Classes* do Kotlin escritas em camelCase (ex: dataVencimento) para o formato JSON correto da API (data\_vencimento).  
* **Banco de Dados e ORM:** Utilizaremos o **PostgreSQL** para persistência física. Para a interação no código, adotaremos a biblioteca **Exposed** (mantida pela JetBrains).  
  * *Abordagem DSL:* O Exposed nos permite escrever queries SQL type-safe utilizando uma DSL (Domain Specific Language) muito próxima ao Kotlin nativo, oferecendo o equilíbrio perfeito entre controle total da query e abstração orientada a objetos.  
  * *Migrações:* Utilizaremos o **Flyway** integrado ao projeto para versionar a criação das tabelas (User e Task) de forma automatizada durante a inicialização (startup) do servidor.  
* **Injeção de Dependência:** Utilizaremos o **Koin**. Diferente do Dagger/Hilt que usam geração de código estática, o Koin é um framework de DI puramente em Kotlin baseado em DSL, o que se alinha perfeitamente com a filosofia declarativa do Ktor e não impacta o tempo de build.  
* **Criptografia:** Utilizaremos Bcrypt (via biblioteca como mindrot:jbcrypt) para garantir que as senhas sejam salvas com *hash* de forma irreversível e com *salt* dinâmico.

## **3\. Arquitetura e Estrutura de Camadas**

O projeto fugirá de estruturas monolíticas desorganizadas e adotará uma separação em camadas bem definida, onde cada função interage apenas com a camada imediatamente inferior. Toda a cadeia operará de forma suspensa (suspend fun), garantindo que nenhuma *thread* do SO seja bloqueada durante chamadas de banco de dados.

* **Plugins (Configuração Inicial):** O ponto de entrada (Application.module) será responsável apenas por instalar e configurar os módulos de infraestrutura: CORS (para permitir a conexão dos front-ends na porta 4200/3000), CallLogging (para auditoria de requisições) e StatusPages (gestão de erros).  
* **Routes (Apresentação):** Os arquivos de rotas definirão os blocos de routing {} e focarão exclusivamente em mapear os verbos HTTP (GET, POST), extrair parâmetros da URL ou do *body*, e retornar as respostas JSON adequadas.  
* **Controller/Service (Regras de Negócio):** Camada intermediária que contém a "inteligência". Aqui residirão as lógicas de validação (ex: RN01 \- verificar se o título possui 3 ou mais caracteres), regras de negócio avançadas e a formatação das respostas finais. Esta camada jamais deve conhecer a linguagem SQL diretamente.  
* **Repository (Acesso a Dados):** A única camada que se comunica com o banco de dados via Exposed DSL. As funções de repositório serão abstraídas em interfaces (ex: TaskRepository) para facilitar os testes unitários da camada de serviço através da injeção de *Mocks*.

## **4\. Estratégia de Segurança e JWT**

A segurança será gerida pelo plugin Authentication do Ktor, fornecendo uma experiência nativa de autorização.

* **Emissão de Token (Login):** Quando o usuário autenticar com sucesso, o Ktor gerará um JWT assinado usando o algoritmo HS256, com validade pré-definida (ex: 24 horas), atribuindo o ID do usuário como o sujeito (sub ou um *claim* customizado) do token. Validaremos issuer (quem emitiu) e audience (para quem foi emitido).  
* **Proteção de Rotas:** Todas as rotas de gerenciamento de tarefas serão envoltas pelo bloco authenticate("jwt") { ... }.  
* **Isolamento de Dados Estrito (Multi-tenant):** Uma vez autenticado, a requisição Ktor disponibiliza o Principal. Extrairemos o userId desse *Principal* e o repassaremos aos serviços. O Repository será responsável por anexar uma cláusula WHERE user\_id \= :userId em todas as consultas SQL das tarefas, garantindo que usuários mal-intencionados jamais consigam manipular dados de terceiros via ID da tarefa na URL.

## **5\. Tratamento de Erros Resiliente (StatusPages)**

Para manter o Contrato de API inviolado, o Ktor não deve "vazar" *Stack Traces* (exceções do servidor) de volta para o Front-end.

* Implementaremos o plugin StatusPages. Este componente interceptará qualquer *Exception* lançada ao longo do ciclo de vida da requisição (do Repository às rotas).  
* Criaremos exceções de negócio semânticas como TaskNotFoundException ou UnauthorizedResourceException.  
* O StatusPages mapeará a TaskNotFoundException diretamente para uma resposta HTTP 404 (Not Found), convertendo-a para um JSON padronizado com campos de status, erro e mensagem detalhada.  
* Erros de serialização ou campos em branco lançarão um mapeamento para 400 (Bad Request).

## **6\. Abordagem de Testes Acadêmicos**

Como se trata de um projeto para fins de estudo:

* **Testes de Integração:** Usaremos a API de testApplication do Ktor. Essa ferramenta poderosa permite levantar todo o ciclo de vida HTTP do servidor em memória, possibilitando disparar requisições GET e POST simuladas sem consumir as portas reais de rede do sistema operacional.  
* **Testcontainers:** Para garantir a integridade do Exposed SQL, utilizaremos instâncias efêmeras do PostgreSQL via Docker geridas pelo *Testcontainers* para validar queries complexas do banco antes da submissão.  
* **Mocking:** Na camada de Serviço, utilizaremos o MockK (biblioteca de mocks nativa do Kotlin e compatível com corrotinas) para isolar o código durante os testes unitários.