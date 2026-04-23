# **Product Requirements Document (PRD)**

**Projeto:** Sistema Full Stack de Gerenciamento de Tarefas (Task Manager)

**Propósito:** Projeto Acadêmico (Estudo de Arquitetura e Engenharia de Software)

**Status:** Planejamento Detalhado

**Data:** Abril de 2026

## **1\. Visão Geral e Objetivo**

O objetivo primordial deste projeto é desenvolver uma aplicação de gerenciamento de tarefas (Task Manager) que permita aos usuários organizar, monitorar e concluir suas atividades diárias, acadêmicas ou profissionais de maneira eficiente.

Sendo um projeto de cunho estritamente acadêmico, o sistema tem como propósito servir como um laboratório prático para o estudo aprofundado do desenvolvimento full stack. A escolha de um gerenciador de tarefas justifica-se por ser um domínio de problema perfeitamente dimensionado: ele engloba todos os desafios fundamentais da engenharia de software contemporânea, incluindo operações de CRUD complexas, gerenciamento de estado da interface, autenticação, persistência de dados, e design de API, sem a sobrecarga de regras de negócios obscuras de indústrias específicas.

Nesta fase de concepção, o projeto é mantido propositalmente independente e **agnóstico de frameworks**. As decisões sobre usar React, Angular ou Vue para o front-end, e Node.js, Python, Java ou C\# para o back-end (assim como a escolha entre SQL e NoSQL) serão tomadas em um documento posterior de Arquitetura e Especificação Técnica, baseando-se nas necessidades aqui levantadas.

## **2\. Descrição do Problema e Justificativa**

No ambiente de alta pressão atual, seja no meio acadêmico ou no dia a dia profissional, os indivíduos frequentemente lidam com múltiplas demandas simultâneas, prazos concorrentes e diferentes níveis de prioridade. A falta de uma ferramenta centralizada e metodologicamente estruturada para acompanhar essas demandas resulta em diversos problemas:

* **Perda de Prazos (Missed Deadlines):** Sem lembretes visuais ou ordenação cronológica, tarefas cruciais acabam sendo esquecidas.  
* **Aumento da Carga Mental (Cognitive Overload):** A tentativa de reter todas as obrigações "na memória" gera estresse, ansiedade e reduz a capacidade de foco nas atividades de execução.  
* **Falta de Visibilidade de Progresso:** A ausência de um sistema que mostre o que já foi feito diminui a motivação e a sensação de produtividade.

O problema a ser resolvido é a necessidade de um sistema que encontre o equilíbrio perfeito entre simplicidade e poder. Muitas ferramentas comerciais no mercado sofrem de *overengineering* (são complexas demais, como o Jira para uso pessoal) ou são simples demais (como blocos de notas genéricos sem metadados de prazo ou status). A nossa solução visa preencher essa lacuna com uma interface limpa, focada inteiramente na jornada de vida da tarefa.

## **3\. Público-Alvo e Personas**

Para guiar o desenvolvimento da interface e da experiência do usuário (UX), definimos as seguintes personas focais para este projeto acadêmico:

* **Persona 1: O Estudante Universitário (João, 21 anos):** Lida com provas, trabalhos em grupo e leituras extracurriculares. Precisa de uma ferramenta rápida de acessar pelo celular durante as aulas e no computador quando está em casa. Valoriza muito a capacidade de organizar tarefas por datas de entrega para evitar a procrastinação.  
* **Persona 2: A Profissional Autônoma / Freelancer (Maria, 29 anos):** Gerencia diferentes projetos de clientes e tarefas administrativas próprias. Precisa categorizar as tarefas por nível de prioridade (Alta, Média, Baixa) para saber exatamente por onde começar o seu dia de trabalho. Valoriza interfaces limpas que não a distraiam.

**Nível de Conhecimento Técnico:** O usuário padrão não possui conhecimento técnico avançado. A interface deve ser autoexplicativa, utilizando ícones universais e fluxos de navegação intuitivos.

## **4\. Casos de Uso Principais (User Stories) e Critérios de Aceite**

Os casos de uso abaixo mapeiam as interações esperadas entre o usuário e o sistema, incluindo os critérios mínimos para que a funcionalidade seja considerada pronta.

* **UC01: Cadastro e Autenticação**  
  * *História:* Como usuário, quero poder criar uma conta e fazer login de forma segura para que minhas tarefas sejam salvas de forma privada e acessíveis de qualquer dispositivo.  
  * *Critérios de Aceite:* O sistema deve rejeitar senhas fracas. O usuário deve receber feedback claro se errar a senha no login. O sistema deve manter o usuário logado via token ou sessão.  
* **UC02: Criação de Nova Tarefa**  
  * *História:* Como usuário, quero adicionar uma nova tarefa com título, descrição detalhada, prazo e prioridade para não me esquecer do escopo do que preciso fazer.  
  * *Critérios de Aceite:* O formulário deve exigir o título. Após salvar, a tarefa deve aparecer imediatamente no topo da lista (ou na ordenação atual) sem necessidade de recarregar a página inteira.  
* **UC03: Gestão de Status de Execução**  
  * *História:* Como usuário, quero alterar o status da minha tarefa (ex: de "Pendente" para "Em Andamento" e depois "Concluída") para acompanhar meu progresso e me sentir motivado.  
  * *Critérios de Aceite:* A mudança de status deve ser rápida (ex: através de um checkbox ou botão de arrastar). Tarefas concluídas devem ter uma diferenciação visual clara (ex: texto riscado ou opacidade reduzida).  
* **UC04: Edição e Manutenção**  
  * *História:* Como usuário, quero editar ou excluir uma tarefa existente caso os requisitos do meu projeto mudem ou eu tenha cometido um erro durante a criação.  
  * *Critérios de Aceite:* A exclusão deve solicitar uma confirmação ("Tem certeza que deseja excluir?") para evitar perda acidental de dados. A edição deve carregar os dados atuais no formulário.  
* **UC05: Organização, Filtragem e Busca**  
  * *História:* Como usuário, quero poder visualizar uma lista de todas as minhas tarefas, buscar por palavras-chave e filtrá-las por status ou prioridade, para focar apenas no que importa no momento.  
  * *Critérios de Aceite:* O sistema deve permitir visualizar apenas tarefas "Pendentes". A busca por texto deve olhar para o título e a descrição da tarefa.

## **5\. Requisitos Funcionais (RF)**

Os Requisitos Funcionais descrevem rigorosamente as capacidades e serviços que o sistema back-end e front-end devem fornecer.

* **RF01 \- Módulo de Autenticação e Perfil:**  
  * **RF01.1:** O sistema deve permitir o cadastro (Sign Up) coletando Nome, E-mail e Senha.  
  * **RF01.2:** O sistema deve permitir autenticação (Log In) e encerramento de sessão (Log Out).  
  * **RF01.3:** (Opcional/Avançado) O sistema deve prever um fluxo para alteração de senha caso o usuário deseje.  
* **RF02 \- Módulo de Gerenciamento de Tarefas (CRUD Completo):**  
  * **RF02.1:** Criação de tarefas contendo: Título (obrigatório, texto curto), Descrição (opcional, texto longo), Data de Vencimento (opcional, formato data/hora), Prioridade (Baixa, Média, Alta) e Status (Pendente, Em Andamento, Concluída).  
  * **RF02.2:** Leitura e listagem de tarefas com suporte a paginação ou *infinite scroll* caso o volume de dados seja alto.  
  * **RF02.3:** Atualização total ou parcial (PATCH/PUT) de qualquer atributo de uma tarefa existente.  
  * **RF02.4:** Exclusão lógica (marcar como deletada no banco) ou física (remover permanentemente) de uma tarefa.  
* **RF03 \- Filtragem, Ordenação e Busca:**  
  * **RF03.1:** O sistema deve permitir a aplicação de múltiplos filtros simultâneos (ex: Status \= Pendente E Prioridade \= Alta).  
  * **RF03.2:** O sistema deve fornecer um campo de busca textual (Search) simples.  
  * **RF03.3:** Ordenação ascendente e descendente baseada na Data de Vencimento e na Data de Criação.  
* **RF04 \- Segurança e Isolamento Multi-tenant:**  
  * **RF04.1:** O sistema deve garantir de forma absoluta que um usuário autenticado acesse única e exclusivamente os recursos (tarefas) atrelados ao seu próprio ID de usuário. Tentativas de acesso a IDs de terceiros devem retornar erro de autorização.

## **6\. Requisitos Não Funcionais (RNF)**

Os Requisitos Não Funcionais estabelecem as restrições, premissas tecnológicas e critérios de qualidade do software.

* **RNF01 \- Arquitetura (Desacoplada/Agnóstica):** O sistema deve ser concebido seguindo o padrão cliente-servidor estrito. O Front-end (SPA, SSR, ou Mobile) deve operar de forma independente do Back-end, comunicando-se exclusivamente via APIs padronizadas (RESTful ou GraphQL) utilizando JSON.  
* **RNF02 \- Persistência de Dados (ACID/Base):** O sistema deve garantir a integridade e persistência dos dados utilizando um Sistema de Gerenciamento de Banco de Dados (SGBD). Estruturas voláteis (em memória) só podem ser usadas para cache.  
* **RNF03 \- Responsividade (Mobile-First):** A interface de usuário (UI) deve adotar princípios de design responsivo. A experiência não deve ser degradada ao acessar a aplicação a partir de telas de smartphones, tablets ou monitores ultrawide.  
* **RNF04 \- Criptografia e Segurança:** Nenhuma senha pode transitar em texto plano na rede (uso obrigatório de HTTPS na implementação final) e deve ser gravada no banco de dados com algoritmos de hash modernos (ex: bcrypt, Argon2). A API deve ser protegida por tokens (ex: JWT) ou sessões seguras com cookies HttpOnly.  
* **RNF05 \- Experiência do Usuário (Feedback Loop):** O sistema não deve deixar o usuário "no escuro". Processos assíncronos (como chamadas de rede para salvar uma tarefa) devem exibir indicadores de carregamento (*spinners*, *skeletons*). Ações de sucesso ou falha devem renderizar *toasts* ou alertas claros.  
* **RNF06 \- Acessibilidade Básica (a11y):** O sistema deve permitir navegação via teclado nas principais funções (uso correto da tecla TAB) e garantir um contraste adequado de cores para leitura.

## **7\. Regras de Negócio (RN)**

As regras de negócio ditam como o sistema lida com a lógica intrínseca aos dados.

* **RN01 (Validação de Domínio):** Uma tarefa não pode ser persistida no banco de dados sem que o campo Título possua no mínimo 3 caracteres e no máximo 100 caracteres.  
* **RN02 (Semântica de Prazos):** Se uma tarefa for submetida sem uma Data de Vencimento atrelada, ela adquire a semântica de tarefa "Sem Prazo" ou "Algum Dia" (Someday/Maybe).  
* **RN03 (Integridade de Identidade):** O e-mail utilizado no momento do cadastro (Sign Up) age como chave natural única e não pode ser duplicado em todo o sistema.  
* **RN04 (Ciclo de Vida Padrão):** O status inicial de qualquer tarefa recém-criada, caso não seja especificado pelo usuário na submissão, deve ser obrigatoriamente atribuído como "Pendente".  
* **RN05 (Gestão de Conclusão):** Opcionalmente, quando uma tarefa for alterada para o status "Concluída", o sistema deve registrar a data e hora exatas dessa ação (Data de Conclusão) para futuras métricas.

## **8\. Estrutura Abstrata de Dados (Modelo de Entidades)**

*(Este modelo reflete as entidades lógicas e seus atributos principais, e deverá ser mapeado futuramente para tabelas de um banco relacional ou coleções de um banco de dados orientado a documentos).*

**Entidade 1: Usuário (User)**

* ID (Identificador Único, UUID ou Auto-incremento)  
* Nome (String)  
* Email (String, Único, Índice)  
* Senha\_Hash (String Encriptada)  
* Data\_Criacao (Timestamp)  
* Ultimo\_Login (Timestamp, Opcional)

**Entidade 2: Tarefa (Task)**

* ID (Identificador Único)  
* ID\_Usuario (Chave Estrangeira / Referência Obrigatória ao dono)  
* Titulo (String, Máx 100 chars)  
* Descricao (Text / String Longa, Opcional)  
* Status (Enum: PENDENTE | EM\_ANDAMENTO | CONCLUIDA)  
* Prioridade (Enum: BAIXA | MEDIA | ALTA \- Default: MEDIA)  
* Data\_Vencimento (Timestamp / Date, Opcional)  
* Data\_Criacao (Timestamp, Gerado automaticamente)  
* Data\_Atualizacao (Timestamp, Atualizado a cada edição)  
* Data\_Conclusao (Timestamp, Preenchido quando o status muda para Concluída)