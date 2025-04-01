# Projeto Web Blog Simples 📝

Uma aplicação web para criar e gerenciar posts de blog com comentários, desenvolvida com Spring Boot e Thymeleaf.

## 📖 Descrição

Este repositório contém o código-fonte de uma aplicação web de blog desenvolvida com Java, Spring Boot e Thymeleaf. A aplicação permite aos usuários realizar as operações básicas de um blog: criar, visualizar, editar e deletar posts. Além disso, os usuários podem adicionar, visualizar, editar e deletar comentários associados a cada post. A interface utiliza Bootstrap 5 e um tema customizado para uma experiência visual agradável.

## ✨ Funcionalidades Principais

*   **Gerenciamento de Posts:**
    *   Criar novos posts (título, autor, conteúdo).
    *   Visualizar lista de posts com resumo.
    *   Visualizar detalhes completos de um post.
    *   Editar posts existentes.
    *   Deletar posts.
*   **Gerenciamento de Comentários:**
    *   Adicionar comentários a um post específico.
    *   Visualizar todos os comentários de um post.
    *   Editar comentários existentes.
    *   Deletar comentários.
*   **Interface:**
    *   Interface web responsiva utilizando Bootstrap 5.
    *   Tema customizado (Pink/Purple/Black).
    *   Navegação clara com Breadcrumbs.
    *   Ícones do Material Icons (Google).
*   **Validação:**
    *   Validação de dados nos formulários de criação/edição.
    *   Exibição de mensagens de erro/sucesso.

## 🛠️ Tecnologias Utilizadas

*   **Backend:**
    *   Java 17+ <!-- (Ajuste a versão se necessário) -->
    *   Spring Boot 3.x <!-- (Ajuste a versão se necessário) -->
    *   Spring Web
    *   Spring Data JPA / Hibernate
    *   Thymeleaf (Template Engine)
*   **Banco de Dados:**
    *   H2 Database (Banco em memória para desenvolvimento/teste) <!-- (Substitua se usar outro, ex: PostgreSQL, MySQL) -->
    *   *ou* PostgreSQL <!-- (Exemplo, descomente/adapte se usar outro BD) -->
    *   *ou* MySQL <!-- (Exemplo, descomente/adapte se usar outro BD) -->
*   **Frontend:**
    *   HTML5
    *   CSS3
    *   Bootstrap 5.3.3
    *   Material Icons
    *   Google Fonts (Roboto)
*   **Build Tool:**
    *   Maven <!-- (Ou Gradle, ajuste conforme seu projeto) -->

## ⚙️ Pré-requisitos

Antes de começar, certifique-se de ter instalado em sua máquina:

*   JDK (Java Development Kit) - Versão 17 ou superior (compatível com seu projeto).
*   Maven (ou Gradle) - Gerenciador de dependências e build.
*   Opcional: Uma IDE de sua preferência (IntelliJ IDEA, Eclipse, VS Code com extensões Java).
*   Opcional: Se não for usar o H2, um SGBD compatível (PostgreSQL, MySQL, etc.) instalado e rodando.

## 🚀 Instalação e Execução

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/seu-usuario/seu-repositorio.git
    # <!-- Lembre-se de substituir pelo URL do SEU repositório! -->
    ```
2.  **Navegue até o diretório do projeto:**
    ```bash
    cd seu-repositorio
    ```
3.  **Configuração do Banco de Dados (se não usar H2):**
    *   Abra o arquivo `src/main/resources/application.properties` (ou `application.yml`).
    *   Ajuste as propriedades `spring.datasource.url`, `spring.datasource.username`, e `spring.datasource.password` de acordo com a configuração do seu banco de dados.
    *   Se estiver usando H2, geralmente nenhuma alteração é necessária para a configuração padrão.
4.  **Compile e empacote o projeto:**
    *   **Usando Maven:**
        ```bash
        mvn clean package -DskipTests
        # O -DskipTests pula a execução dos testes unitários durante o build
        ```
    *   **Usando Gradle:**
        ```bash
        ./gradlew clean build -x test
        ```
5.  **Execute a aplicação:**
    *   **Via linha de comando:**
        ```bash
        java -jar target/nome-do-seu-artefato-0.0.1-SNAPSHOT.jar
        # <!-- Ajuste o nome do arquivo .jar gerado na pasta target/ ou build/libs/ -->
        ```
    *   **Via IDE:** Encontre a classe principal (anotada com `@SpringBootApplication`) e execute-a.
6.  **Acesse a aplicação:**
    Abra seu navegador e acesse `http://localhost:8080` (ou a porta configurada no `application.properties`). A página principal deve ser `http://localhost:8080/web/posts`.

## 🕹️ Como Usar

*   Acesse a página inicial `/web/posts` para ver a lista de posts existentes.
*   Clique no botão "Criar Novo Post" para adicionar um novo post. Preencha o formulário e salve.
*   Clique no título de um post ou no botão "Detalhes" para visualizar o post completo e seus comentários.
*   Na página de detalhes do post, use os botões "Editar Post" ou "Deletar Post" para gerenciar o post.
*   Abaixo do conteúdo do post, você encontrará a seção de comentários. Use o formulário para "Deixar um Comentário".
*   Para cada comentário existente, você verá opções para "Editar" ou "Deletar" o comentário.

## 🏗️ Estrutura do Projeto (Simplificada)
├── src
│ ├── main
│ │ ├── java
│ │ │ └── com/seu_pacote/ # Pacotes principais
│ │ │ ├── controller # Controladores Web (Thymeleaf)
│ │ │ ├── model # Entidades JPA (Post, Comment)
│ │ │ ├── repository # Interfaces Spring Data JPA
│ │ │ └── service # Lógica de negócio (opcional)
│ │ ├── resources
│ │ │ ├── static # Arquivos estáticos
│ │ │ │ ├── css # Folhas de estilo (custom-theme.css)
│ │ │ │ └── img # Imagens (favicon.png)
│ │ │ ├── templates # Templates Thymeleaf (HTML)
│ │ │ └── application.properties # Configurações da aplicação
│ └── test # Testes unitários/integração
├── pom.xml # (Ou build.gradle) Dependências e build
└── README.md # Este arquivo

