# Caderneta de Fiado

Aplicativo mobile para pequenos comerciantes registrarem vendas fiado: quem deve, quanto deve e quando pagou — substituindo o caderno de papel.

Projeto da P3 — Atividade final (Projeto Mobile: Aplicação Full Stack).

## Problema

Grande parte do comércio de bairro no Brasil funciona na informalidade, e o "fiado" ainda é anotado em cadernos de papel, que se perdem, rasgam e geram dúvidas sobre quanto cada cliente deve.

Segundo a **PNAD Contínua do IBGE**, no trimestre encerrado em setembro de 2026 (jul-ago-set), a **taxa de informalidade foi de 37,5% da população ocupada**, o equivalente a **38,8 milhões de trabalhadores informais** (contra 37,4% no trimestre anterior e 37,8% no mesmo trimestre de 2025).

Fonte: IBGE — Agência de Notícias, divulgação de 30/10/2026: [Taxa de desocupação é de 5,2% e taxa de subutilização é de 13,0% no trimestre encerrado em setembro](https://agenciadenoticias.ibge.gov.br/agencia-sala-de-imprensa/2013-agencia-de-noticias/releases/48409-30-10-release)

A Caderneta de Fiado oferece a esses comerciantes uma forma simples e gratuita de controlar as vendas a prazo pelo celular.

## Funcionalidades

- Cadastro e login de usuário com JWT
- Cadastro, consulta, edição e exclusão de fiados
- Marcar um fiado como pago
- Total devido exibido na lista
- Login mantido ao reabrir o app

## Tecnologias

| Área | Tecnologia |
| --- | --- |
| Front-end | React Native (Expo) — demais escolhas a definir pela equipe de front |
| Back-end | Java 21, Spring Boot 4.1.2 |
| Build | Maven (via Maven Wrapper `mvnw`, não precisa instalar o Maven) |
| Persistência | Spring Data JPA (Hibernate) com o dialeto SQLite do `hibernate-community-dialects` |
| Banco de dados | SQLite (driver `sqlite-jdbc`), arquivo `fiado.db` |
| Segurança | Spring Security (senhas com hash BCrypt) |
| Validação | Spring Boot Validation (Bean Validation: `@NotBlank`, `@Email`, `@Positive`...) |
| API | Spring Web MVC (REST) |
| Produtividade | Lombok (getters, setters e construtores gerados) |
| Testes | JUnit 5 com os starters de teste do Spring Boot |

## Estrutura do repositório

```
caderneta-de-fiado/
├─ backend/    API REST em Spring Boot
└─ frontend/   App em React Native (Expo) — a ser criado pela equipe de front
```

Pacotes do back-end (`backend/src/main/java/br/com/senac/cadernetafiado`):

| Pacote | Conteúdo |
| --- | --- |
| `modelo` | Entidades JPA (`Usuario`, `Fiado`) |
| `repositorio` | Interfaces Spring Data (`UsuarioRepositorio`, `FiadoRepositorio`) |
| `configuracao` | Dados de exemplo para a demonstração (`DadosExemplo`) |

## Modelo de dados

As tabelas são criadas automaticamente pelo Hibernate a partir das entidades (`spring.jpa.hibernate.ddl-auto=update`).

### Tabela `usuarios` (entidade `Usuario`)

Dono da caderneta: a pessoa que faz login no aplicativo.

| Campo | Tipo | Regras |
| --- | --- | --- |
| `id` | inteiro | Chave primária, gerada automaticamente |
| `nome` | texto | Obrigatório |
| `email` | texto | Obrigatório, formato de e-mail válido, **único** |
| `senha` | texto | Obrigatório; guarda somente o hash BCrypt, nunca a senha digitada |

### Tabela `fiados` (entidade `Fiado`)

Uma anotação na caderneta: quem comprou, quanto deve e quando.

| Campo | Tipo | Regras |
| --- | --- | --- |
| `id` | inteiro | Chave primária, gerada automaticamente |
| `nome_cliente` | texto | Obrigatório |
| `valor` | decimal (10,2) | Obrigatório, maior que zero (`BigDecimal` para não errar centavos) |
| `data` | data | Obrigatório |
| `descricao` | texto | Opcional (o que foi comprado) |
| `pago` | booleano | Obrigatório, começa como `false` |
| `usuario_id` | inteiro | Obrigatório, chave estrangeira para `usuarios.id` |

### Relacionamento

**Um usuário tem vários fiados** (1:N). Cada fiado pertence a exatamente um usuário, pela coluna `usuario_id` (`@ManyToOne` em `Fiado`).

```
usuarios (1) ──────< (N) fiados
   id  ◄──────────────── usuario_id
```

## Como executar o back-end

### Pré-requisitos

- **Java 21 ou superior** (JDK) instalado — confira com `java -version`
- Não é preciso instalar Maven nem SQLite: o Maven Wrapper baixa o Maven e o driver JDBC já cria o banco

### Rodando a API

Entre na pasta do back-end:

```bash
cd backend
```

Windows (PowerShell ou CMD):

```bash
mvnw.cmd spring-boot:run
```

Linux / Mac:

```bash
./mvnw spring-boot:run
```

A API sobe em **http://localhost:8080**.

### Banco de dados

O arquivo **`fiado.db`** é criado automaticamente na pasta **`backend/`** na primeira execução, já com as tabelas. Ele está no `.gitignore` e não deve ser commitado. Para recomeçar do zero, pare a aplicação e apague o `backend/fiado.db`.

### Dados de exemplo

Na primeira execução (banco vazio), a classe `configuracao/DadosExemplo.java` cria dados fictícios para a demonstração. Se já existir algum usuário, nada é inserido, então rodar o app várias vezes não duplica dados.

Usuário de demonstração:

| Nome | E-mail | Senha |
| --- | --- | --- |
| Seu Joaquim | `demo@caderneta.com` | `senha123` |

Fiados desse usuário (datas relativas ao dia em que o banco é criado):

| Cliente | Valor | Data | Descrição | Situação |
| --- | --- | --- | --- | --- |
| Dona Maria | R$ 35,90 | ontem | Arroz, feijão e óleo | Em aberto |
| Seu Zé | R$ 12,50 | há 3 dias | Pão e leite | Em aberto |
| Carla | R$ 58,00 | há 7 dias | Produtos de limpeza | Em aberto |
| Antônio | R$ 20,00 | há 10 dias | Café e açúcar | Pago |
| Dona Maria | R$ 47,30 | há 15 dias | Compras do mês | Pago |

Total em aberto: **R$ 106,40**.

### Rodando os testes

Windows:

```bash
mvnw.cmd test
```

Linux / Mac:

```bash
./mvnw test
```

## Como executar o front-end

<!-- Será preenchido pela equipe de front -->

> Será preenchido pela equipe de front.

## Como contribuir

1. Crie uma branch a partir da `main`: `funcionalidade/nome-da-tarefa`
2. Faça commits pequenos e com mensagens claras, em português
3. Abra um Pull Request para a `main` e preencha o checklist do template
4. Atualize o [Plano de Execução](https://claude.ai/artifact/K1DiyxEiQdNSsdgbUCeeNg) marcando as tarefas concluídas
5. Uma pessoa da mesma área revisa e aprova antes do merge

## Integrantes

| Nome completo | RM | Função |
| --- | --- | --- |
| Gustavo Oliveira Fernandes de Andrade | 1142439157 | Banco, entidades e coordenação |
| Leandro Sato | 1142446679 | Segurança JWT |
| Pedro Henrique Teixeira | 1143155053 | API do Fiado |
| Ryan Santos | 1142463435 | Login, cadastro e erros da API |
| João Pedro Cavanhas | 1143032617 | Base do app |
| Nicolas Thomaz | 1142440716 | Lista e Detalhe |
| Vinicius Soares | 1142945398 | Login e Cadastro |
| Eduardo Campos | 1143154427 | Formulário e Perfil |
