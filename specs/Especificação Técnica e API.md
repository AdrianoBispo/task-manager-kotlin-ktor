# **Especificação Técnica e Contrato de API**

**Projeto:** Sistema Full Stack de Gerenciamento de Tarefas (Task Manager)

**Estratégia:** Multi-Stack (Várias implementações do mesmo PRD)

**Padrão Arquitetural:** Cliente-Servidor (Desacoplado) via RESTful API

## **1\. Estratégia de Arquitetura Multi-Stack e Fundamentos**

O projeto será desenvolvido em módulos estritamente independentes. O objetivo central é garantir a total interoperabilidade, caracterizando um verdadeiro ecossistema agnóstico: qualquer Front-end (React, Angular, Vue, Next.js) deve ser capaz de consumir qualquer Back-end (Node.js, Java Spring, Kotlin Ktor) sem a necessidade de alterar uma única linha de código no cliente.

Para que essa premissa funcione, todos os times e desenvolvedores envolvidos devem seguir este contrato à risca.

### **1.1. Comunicação e Formato de Dados**

* **Protocolo:** Todas as requisições serão feitas via protocolo HTTP/HTTPS seguindo o padrão arquitetural REST (Representational State Transfer).  
* **Formato Universal (JSON):** A troca de mensagens bidirecional (Request e Response) será estritamente no formato application/json. Todos os back-ends devem ignorar requisições que não contenham o cabeçalho Content-Type: application/json para rotas de mutação de dados (POST, PUT, PATCH).  
* **Nomenclatura (Casing):** Para manter a consistência entre diferentes linguagens (onde Java prefere camelCase e Python prefere snake\_case), padronizaremos os payloads JSON utilizando **snake\_case** (ex: data\_vencimento, id\_usuario). O front-end e o back-end deverão criar seus próprios serializadores (mappers) internos se desejarem trabalhar com camelCase em seus códigos fontes.

### **1.2. Autenticação e Segurança (JWT)**

* **Padrão JWT:** A autenticação será do tipo *Stateless* baseada em tokens (JSON Web Token \- JWT). O servidor não deve armazenar sessões em memória.  
* **Ciclo de Vida:** O token gerado no login terá uma validade sugerida de 24 horas. O payload do JWT deve conter, no mínimo, o sub (Subject/ID do usuário) e a exp (Data de expiração).  
* **Transporte:** O front-end deverá armazenar este token (preferencialmente em memória ou localStorage para simplificação neste projeto acadêmico) e enviá-lo obrigatoriamente no cabeçalho de todas as requisições privadas no formato: Authorization: Bearer \<token\_aqui\>.

### **1.3. CORS (Cross-Origin Resource Sharing)**

Devido à natureza desacoplada do projeto, os clientes (Front-end) rodarão em portas diferentes dos servidores (Back-end) no ambiente de desenvolvimento (ex: cliente na porta 3000, servidor na 8080).

* Todos os back-ends **devem** ser explicitamente configurados para permitir requisições de origens cruzadas (CORS).  
* **Métodos Permitidos:** GET, POST, PUT, PATCH, DELETE, OPTIONS.  
* **Headers Permitidos:** Content-Type, Authorization.  
* Requisições de *preflight* (OPTIONS) devem ser respondidas com status 200 ou 204 imediatamente.

### **1.4. Padronização de Tratamento de Erros**

Para que o Front-end consiga exibir mensagens consistentes para o usuário, qualquer erro no Back-end (status 400, 401, 403, 404 ou 500\) deve retornar um corpo JSON com a seguinte estrutura padronizada:

{  
  "status": 400,  
  "erro": "Bad Request",  
  "mensagem": "Ocorreu um erro de validação nos dados enviados.",  
  "detalhes": \[  
    {  
      "campo": "senha",  
      "erro": "A senha deve conter no mínimo 8 caracteres."  
    }  
  \]  
}

*(Nota: O array detalhes é opcional, mas altamente recomendado para o status 400 \- Validação).*

## **2\. Contrato da API RESTful (Endpoints)**

Abaixo estão definidos detalhadamente todos os *endpoints* que compõem o escopo da aplicação.

### **2.1. Módulo de Autenticação (/api/auth)**

O módulo de autenticação é público e não requer envio prévio de token.

#### **A. Registrar Novo Usuário**

Responsável por criar uma conta no sistema e já retornar o usuário autenticado.

* **Método:** POST  
* **Rota:** /api/auth/register  
* **Regras de Validação:**  
  * nome: String, obrigatório, mínimo de 3 caracteres.  
  * email: String, obrigatório, formato de e-mail válido, deve ser único no banco.  
  * senha: String, obrigatório, mínimo de 6 caracteres.  
* **Body (Request):**  
  {  
    "nome": "João Silva",  
    "email": "joao@email.com",  
    "senha": "senhaSegura123"  
  }

* **Response (Sucesso \- 201 Created):**  
  A senha em texto plano jamais deve ser retornada na resposta.  
  {  
    "usuario": {  
      "id": "123e4567-e89b-12d3-a456-426614174000",  
      "nome": "João Silva",  
      "email": "joao@email.com"  
    },  
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."  
  }

* **Response (Erro \- 400 Bad Request / 422 Unprocessable Entity):** Em caso de e-mail já existente ou senha muito curta.

#### **B. Login de Usuário Existente**

Autentica um usuário fornecendo um token de acesso para uso subsequente.

* **Método:** POST  
* **Rota:** /api/auth/login  
* **Body (Request):**  
  {  
    "email": "joao@email.com",  
    "senha": "senhaSegura123"  
  }

* **Response (Sucesso \- 200 OK):** Mesmo payload do Registro (Retorna o Usuário e o Token).  
* **Response (Erro \- 401 Unauthorized):** Retornado genéricamente para "Credenciais inválidas" (não diferencie se o erro foi no e-mail ou na senha, por motivos de segurança).

### **2.2. Módulo de Tarefas (/api/tasks)**

Este é o módulo principal do sistema. **Atenção:** Absolutamente todas as rotas deste módulo exigem o Header Authorization: Bearer \<token\>. O back-end deve interceptar a requisição, extrair o ID do usuário do token, e utilizá-lo para filtrar e inserir os dados.

#### **A. Listar Tarefas do Usuário (Com Paginação e Filtros)**

Retorna a lista de tarefas associadas ao usuário logado.

* **Método:** GET  
* **Rota:** /api/tasks  
* **Query Params (Filtros e Paginação):** \* status (opcional): PENDENTE, EM\_ANDAMENTO, CONCLUIDA.  
  * prioridade (opcional): BAIXA, MEDIA, ALTA.  
  * page (opcional, default 1): Número da página desejada.  
  * limit (opcional, default 10): Quantidade de itens por página.  
  * sort (opcional): Campo para ordenação (ex: data\_vencimento, data\_criacao).  
* **Response (Sucesso \- 200 OK):**  
  A resposta deve ser envelopada incluindo metadados de paginação para facilitar o desenvolvimento do Front-end.  
  {  
    "dados": \[  
      {  
        "id": "1",  
        "titulo": "Implementar JWT no Back-end",  
        "descricao": "Adicionar biblioteca de criptografia e interceptor de rotas.",  
        "status": "PENDENTE",  
        "prioridade": "ALTA",  
        "data\_vencimento": "2026-04-20T23:59:59Z",  
        "data\_criacao": "2026-04-18T10:00:00Z"  
      }  
    \],  
    "meta": {  
      "total\_itens": 15,  
      "total\_paginas": 2,  
      "pagina\_atual": 1,  
      "itens\_por\_pagina": 10  
    }  
  }

#### **B. Criar Nova Tarefa**

Cria um novo registro atrelado ao usuário que fez a requisição.

* **Método:** POST  
* **Rota:** /api/tasks  
* **Regras de Validação:**  
  * titulo: Obrigatório.  
  * Demais campos são opcionais. Se status for omitido, o back-end deve assumir PENDENTE. Se prioridade for omitida, assumir MEDIA.  
* **Body (Request):**  
  {  
    "titulo": "Estudar React Query",  
    "descricao": "Melhorar a gestão de estado assíncrono no frontend",  
    "status": "PENDENTE",   
    "prioridade": "MEDIA",  
    "data\_vencimento": "2026-05-10T12:00:00Z"   
  }

* **Response (Sucesso \- 201 Created):** Retorna o objeto completo recém-criado, incluindo o id gerado pelo banco e os timestamps (data\_criacao).

#### **C. Atualizar Tarefa (Edição Completa ou Status)**

Permite a atualização de atributos específicos de uma tarefa.

* **Método:** PUT (Substituição completa) ou PATCH (Substituição parcial). Para este contrato, padronizaremos o suporte ao PATCH, que permite enviar apenas o que mudou.  
* **Rota:** /api/tasks/:id (O :id é capturado via path parameter).  
* **Body (Request \- Exemplo mudando apenas o status para concluir a tarefa):**  
  {  
    "status": "CONCLUIDA"  
  }

* **Response (Sucesso \- 200 OK):** Retorna o objeto da tarefa atualizada por completo.  
* **Response (Erro \- 404 Not Found):** Tarefa solicitada não existe no banco.  
* **Response (Erro \- 403 Forbidden):** A tarefa existe, mas pertence a outro ID de usuário (violação grave de isolamento de dados).

#### **D. Excluir Tarefa**

Remove uma tarefa do sistema (seja via exclusão lógica no banco ou remoção física).

* **Método:** DELETE  
* **Rota:** /api/tasks/:id  
* **Response (Sucesso \- 204 No Content):** Sucesso na operação. O back-end não retorna nenhum JSON no corpo (Body) da resposta, apenas o status de cabeçalho.  
* **Response (Erro \- 404 Not Found / 403 Forbidden):** Mesmas regras da atualização.

## **3\. Catálogo Definitivo de Códigos HTTP**

O uso correto dos status codes faz parte do contrato. Os clientes usarão esses códigos para decidir se mostram uma tela de erro, se redirecionam o usuário para a tela de login, ou se mostram uma mensagem de sucesso verde.

* **Sucesso (2xx):**  
  * 200 OK: A requisição foi bem sucedida. Usado em GETs, PUTs e PATCHs.  
  * 201 Created: Um novo recurso foi gerado com sucesso. Exclusivo para POST (Registro de usuário ou criação de tarefa).  
  * 204 No Content: Ação executada com sucesso, mas o servidor não tem dados a retornar. Exclusivo para DELETE.  
* **Erros de Cliente (4xx):**  
  * 400 Bad Request: Erro geral de entrada. O front-end enviou JSON mal formatado ou faltaram campos obrigatórios.  
  * 401 Unauthorized: Falha de autenticação. O token de acesso não foi enviado, expirou, ou a senha do login estava errada. **Ação no Front-end:** Deslogar usuário e redirecionar para /login.  
  * 403 Forbidden: Problema de permissão (Autorização). O usuário está logado, enviou o token certo, mas está tentando acessar ou modificar o dado (Tarefa) que pertence a outro usuário.  
  * 404 Not Found: A URL acessada ou o Recurso específico (ID da Tarefa) não existe no banco de dados.  
* **Erros de Servidor (5xx):**  
  * 500 Internal Server Error: Algo quebrou no Back-end (banco de dados caiu, erro de lógica no código). O front-end deve exibir uma mensagem genérica de "Sistema indisponível no momento".