

# Plano de Execução – Caderneta de Fiado

> Última atualização: 16/11/2026. Este arquivo é o plano oficial da equipe: quem conclui uma tarefa marca a caixa e cola o link do PR no próprio PR.

O app Caderneta de Fiado será entregue em 17/11/2026 por 8 pessoas: 4 no back-end (Spring Boot + SQLite) e 4 no front-end (React Native). Cada pessoa é dona de arquivos próprios, faz commits do próprio código e precisa explicar a sua parte e o fluxo geral.

## O projeto

O dono de um pequeno comércio registra no app quem comprou fiado, quanto deve e quando pagou, substituindo o caderno de papel. Segundo a PNAD Contínua (IBGE), a taxa de informalidade foi de 37,5% da população ocupada no trimestre encerrado em setembro de 2026; a fonte completa está no [README](README.md#problema).

**Banco:** SQLite. É um banco relacional, guardado em um único arquivo (`fiado.db`), sem precisar instalar servidor nem usar Docker. No Spring Boot, use as dependências `org.xerial:sqlite-jdbc` e `org.hibernate.orm:hibernate-community-dialects`, configure `spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect` e `spring.jpa.hibernate.ddl-auto=update`, e coloque `fiado.db` no `.gitignore`.

**Tabelas**

| Tabela | Campos |
| --- | --- |
| usuario | id, nome, email (único), senha (hash BCrypt) |
| fiado | id, nome_cliente, valor, data, descricao, pago (sim/não), usuario_id (FK) |

**Telas (6, o mínimo é 5)**

| Tela | O que faz | Responsável |
| --- | --- | --- |
| Login | E-mail e senha, recebe o JWT | Vinicius |
| Cadastro | Cria a conta | Vinicius |
| Lista de fiados | Lista, total devido no topo, filtro todos/pendentes/pagos | Nicolas |
| Detalhe do fiado | Dados completos, botões de editar, marcar como pago e excluir | Nicolas |
| Formulário de fiado | Criar e editar um fiado | Eduardo |
| Perfil | Dados do usuário e botão Sair | Eduardo |

A navegação entre as telas, o estado do login e o armazenamento local ficam com o João Pedro.

**Endpoints da API**

| Método e rota | Faz | Responsável |
| --- | --- | --- |
| POST /auth/register | Cria usuário | Ryan |
| POST /auth/login | Devolve o JWT | Ryan |
| GET /usuarios/me | Dados do usuário logado | Ryan |
| GET /fiados | Lista os fiados do usuário logado | Pedro |
| GET /fiados/{id} | Busca um fiado | Pedro |
| POST /fiados | Cria um fiado | Pedro |
| PUT /fiados/{id} | Atualiza um fiado | Pedro |
| PATCH /fiados/{id}/pagar | Marca como pago | Pedro |
| DELETE /fiados/{id} | Exclui um fiado | Pedro |

Com exceção de /auth/*, todas as rotas exigem o token no cabeçalho `Authorization: Bearer <token>`, validado pelo filtro do Leandro.

## Stacks

O enunciado e o curso já fixam a base do projeto. **As escolhas que faltam serão decididas entre os integrantes de cada área, até 13/11, e registradas no README.** Os 4 do back decidem juntos as escolhas do back; os 4 do front, as do front.

| Área | Já definido | A área decide entre si | Quem decide |
| --- | --- | --- | --- |
| Front-end | React Native · navegação entre telas · gerenciamento de estado · AsyncStorage · Expo (recomendação oficial no curso) | TypeScript ou JavaScript · React Navigation ou Expo Router · React Native Paper ou estilo próprio · fetch ou axios · Context API ou Zustand · Expo Go ou development build | João Pedro, Nicolas, Vinicius, Eduardo |
| Back-end | Spring Boot · API REST · autenticação JWT | Versão do Java · Maven ou Gradle · biblioteca de JWT · Lombok · Swagger · uso de DTOs | Gustavo, Leandro, Pedro, Ryan |
| Banco | SQLite (relacional, em arquivo) | ddl-auto=update ou scripts SQL de criação · dados de exemplo (seed) | Gustavo |
| Ferramentas | GitHub · README com nomes e RMs | Insomnia ou Postman · editor de cada um | Cada área |

Ao decidir, levem em conta o que o curso ensina: os exemplos usam TypeScript, React Navigation, React Native Paper, fetch e Context API. Escolher o que está no curso facilita na hora das perguntas, mas a decisão é da área.

## Regras do curso

A regra mais importante: **o token JWT não pode ser guardado no AsyncStorage** (curso, slides 232–233). O token vai no `expo-secure-store` e é apagado no logout. O AsyncStorage, que o enunciado exige, guarda apenas dados não sensíveis: nome do usuário, último filtro da lista e uma cópia da lista para ver sem internet.

| Evitar | Fazer | Onde está no curso | Quem aplica |
| --- | --- | --- | --- |
| Token no AsyncStorage | Token no `expo-secure-store`, apagado no logout | Módulo 07 | João Pedro, Eduardo |
| Segredo no código do app ou no GitHub | Chave do JWT só no back, em variável de ambiente, fora do repositório | Módulos 02 e 07 | Leandro |
| `TouchableOpacity` ou `TouchableHighlight` | `Pressable` (ou o botão da biblioteca de UI escolhida) | Módulo 04 | Todo o front |
| `ScrollView` para listas | `FlatList` | Módulo 01 | Nicolas |
| `keyExtractor` com o índice | `keyExtractor` com o `id` do fiado | Módulo 01 | Nicolas |
| Confiar que a requisição falha sozinha | Checar `response.ok`, usar `try/catch/finally` e mostrar carregamento | Módulo 07 | Todo o front |
| Requisição sem tempo limite | Timeout com `AbortController` | Módulo 07 | João Pedro |
| `push` em array do estado | Novo array: `[...lista, novo]`, `map`, `filter` | Módulo J2 | Nicolas, Eduardo |
| `var` e `==` | `const` ou `let`, e `===` | Módulo J1 | Todo o front |
| Cores fixas espalhadas | Tema centralizado em um arquivo (`theme`) | Módulo 06 | João Pedro |
| Botão só com ícone, sem descrição | `accessibilityLabel` | Módulo 07 | Todo o front |
| Cabeçalho duplicado com abas e pilha | `headerShown: false` | Módulo 04 | João Pedro |
| Instalar biblioteca do Expo com `npm install` | `npx expo install` | Módulo 02 | Todo o front |

**HTTP no desenvolvimento:** o curso pede HTTPS sempre. Localmente, o app acessa `http://IP-do-PC:8080`. Se o professor perguntar: "em produção usaríamos HTTPS; aqui é ambiente de desenvolvimento".

## Plano por pessoa — back-end

Antes de começar a programar, os 4 do back decidem juntos as stacks pendentes da área (versão do Java, Maven ou Gradle, biblioteca de JWT, Lombok, Swagger, DTOs).

### 1. Gustavo Andrade — Banco, entidades e coordenação

- [x] Criar o repositório no GitHub, adicionar os 7 colaboradores e proteger a branch `main`
  main protegida: só recebe código por PR, com 1 aprovação. Convites enviados aos 7 em 12/11; cada um precisa aceitar o convite no GitHub.
- [x] Criar o projeto Spring Boot e configurar o SQLite em `application.properties` (`fiado.db` no `.gitignore`)
  PR: [#1 Configuração inicial do backend](https://github.com/GustAndrade07/caderneta-de-fiado/pull/1)
- [x] Entidades `Usuario` e `Fiado` com `@ManyToOne` (cada fiado pertence a um usuário) e validações
  PR: [#2 Entidades Usuario e Fiado e seus repositórios](https://github.com/GustAndrade07/caderneta-de-fiado/pull/2)
- [x] `UsuarioRepositorio` (com `buscarPorEmail`) e `FiadoRepositorio` (com `listarPorUsuario` e soma do total devido)
  PR: [#2 Entidades Usuario e Fiado e seus repositórios](https://github.com/GustAndrade07/caderneta-de-fiado/pull/2)
- [x] Dados de exemplo (seed) para a demonstração
  PR: [#3 Dados de exemplo (seed) para a demonstração](https://github.com/GustAndrade07/caderneta-de-fiado/pull/3)
- [x] README completo: problema, dados do IBGE, como rodar, integrantes com nome e RM
  PR: [#4 Documentação: README completo](https://github.com/GustAndrade07/caderneta-de-fiado/pull/4)
  Parte do Gustavo concluída (problema com dado do IBGE, tecnologias e execução do back, modelo de dados, integrantes com RM). A seção "Como executar o front-end" entrou no PR #10. Tecnologias e pastas do front, funcionalidades novas e a versão do Node atualizadas no PR [#19](https://github.com/GustAndrade07/caderneta-de-fiado/pull/19).
- [x] Revisar os PRs do back e acompanhar o cronograma
  PRs revisados por Gustavo até o [#22](https://github.com/GustAndrade07/caderneta-de-fiado/pull/22); cronograma cumprido até a entrega de 17/11.

**Estudar:** JPA vs Hibernate vs Spring Data, por que SQLite é relacional, chave primária e estrangeira, o que o `ddl-auto` faz.

### 2. Leandro Sato — Segurança JWT

- [x] `JwtService`: gerar o token com o e-mail do usuário e um tempo de expiração, validar e ler o token
  PR: [#5 Segurança JWT: token, filtro e rotas protegidas](https://github.com/GustAndrade07/caderneta-de-fiado/pull/5)
- [x] `JwtFilter`: ler `Authorization: Bearer`, validar e autenticar a requisição
  PR: [#5 Segurança JWT: token, filtro e rotas protegidas](https://github.com/GustAndrade07/caderneta-de-fiado/pull/5)
- [x] `SecurityConfig`: liberar `/auth/**`, proteger o resto, sessão stateless, CORS liberado para o app
  PR: [#5 Segurança JWT: token, filtro e rotas protegidas](https://github.com/GustAndrade07/caderneta-de-fiado/pull/5)
- [x] Chave secreta do JWT em variável de ambiente, nunca no GitHub
  PR: [#5 Segurança JWT: token, filtro e rotas protegidas](https://github.com/GustAndrade07/caderneta-de-fiado/pull/5)
- [x] Testar no Insomnia ou Postman: sem token dá 401, com token dá 200
  Conferido no PR #5 por curl e por teste automático em servidor real (sem token 401 com corpo JSON, com token a requisição passa). A coleção desse teste entrou no repositório pelo PR [#16 Coleção do Insomnia e evidência do teste 401/200](https://github.com/GustAndrade07/caderneta-de-fiado/pull/16) (`backend/insomnia-seguranca-jwt.json`), com as respostas registradas em `backend/EVIDENCIA-CA19.md`. As prints estão em `backend/prints/` e no corpo do PR. Ajuste posterior no teste da rota liberada: [#8 Teste de 404 em URL que ninguém vai criar](https://github.com/GustAndrade07/caderneta-de-fiado/pull/8).

**Estudar:** as três partes do JWT, por que ele é stateless, o que acontece quando o token expira, por que o segredo fica só no back.

### 3. Pedro Henrique Teixeira — API do Fiado (CRUD)

- [x] `FiadoService`: listar, buscar, criar, atualizar, marcar como pago e excluir, sempre filtrando pelo usuário logado
  PR: [#9 API REST de fiados](https://github.com/GustAndrade07/caderneta-de-fiado/pull/9)
- [x] `FiadoController`: os 6 endpoints de /fiados, com os códigos HTTP certos (200, 201, 204, 404)
  PR: [#9 API REST de fiados](https://github.com/GustAndrade07/caderneta-de-fiado/pull/9). Também inclui `GET /fiados/total` (total devido).
- [x] Impedir que um usuário veja ou altere o fiado de outro
  PR: [#9 API REST de fiados](https://github.com/GustAndrade07/caderneta-de-fiado/pull/9) (teste de isolamento entre usuários em `FiadoControllerTestes`).
- [x] Testar todos os endpoints no Insomnia ou Postman e salvar a coleção no repositório
  PR: [#22 Padroniza pacotes, corrige CORS e adiciona coleção Postman](https://github.com/GustAndrade07/caderneta-de-fiado/pull/22). A coleção `backend/postman/caderneta-fiado-endpoints.postman_collection.json` cobre os 7 endpoints de `/fiados` (listar, buscar, criar, editar, pagar, excluir e total), com os casos 401, 400 e 404 e testes em cada requisição.

**Estudar:** os verbos HTTP e quando usar cada um, o caminho controller → service → repository, os códigos de status.

### 4. Ryan Santos — Login, cadastro e erros da API

- [x] `AuthController`: `POST /auth/register` (senha com BCrypt) e `POST /auth/login` (devolve o token do `JwtService`)
  PR: [#7 Cadastro, login e perfil do usuário](https://github.com/GustAndrade07/caderneta-de-fiado/pull/7)
- [x] `UsuarioController`: `GET /usuarios/me`
  PR: [#7 Cadastro, login e perfil do usuário](https://github.com/GustAndrade07/caderneta-de-fiado/pull/7)
- [x] Recusar e-mail duplicado e senha curta
  E-mail duplicado recusado (409) no PR #7. Senha com no mínimo 8 caracteres (`@Size(min = 8, max = 72)` em `CadastroUsuarioRequest`) entrou pelo PR [#15 Login e cadastro](https://github.com/GustAndrade07/caderneta-de-fiado/pull/15).
- [x] `GlobalExceptionHandler`: mensagens de erro claras em JSON (400, 401, 404, 409)
  PR: [#7 Cadastro, login e perfil do usuário](https://github.com/GustAndrade07/caderneta-de-fiado/pull/7)

**Estudar:** por que a senha é guardada com hash e nunca em texto puro, o fluxo de login de ponta a ponta, como o `@ControllerAdvice` trata os erros.

## Plano por pessoa — front-end

Antes de começar a programar, os 4 do front decidem juntos as stacks pendentes da área (TypeScript ou JavaScript, biblioteca de navegação, biblioteca de UI, fetch ou axios, Context ou Zustand). Enquanto o back não fica pronto, cada um trabalha com dados de exemplo fixos no código.

### 5. João Pedro Cavanhas — Base do app

- [x] Criar o projeto Expo e a estrutura de pastas (`screens`, `services`, `context`, `theme`)
  PR: [#10 Base do app](https://github.com/GustAndrade07/caderneta-de-fiado/pull/10) (pastas `screens`, `services`, `contexts`, `routes`, `theme` e `types`, em TypeScript).
- [x] Navegação: telas de Login e Cadastro quando deslogado; abas Fiados e Perfil quando logado; Detalhe e Formulário na pilha
  Rotas públicas e privadas no PR #10. Abas Fiados e Perfil (grupo `(tabs)` do Expo Router), com Detalhe e Formulário na pilha por cima, no PR [#20 Abas, usuário no AuthContext e espaçamentos no tema](https://github.com/GustAndrade07/caderneta-de-fiado/pull/20).
- [x] Estado do login (`AuthContext` ou equivalente): `login`, `logout`, `usuario`, `carregando`
  `token`, `loading`, `signIn` e `signOut` no PR #10; `usuario` no PR [#20](https://github.com/GustAndrade07/caderneta-de-fiado/pull/20).
- [x] Token no `expo-secure-store`; nome do usuário e último filtro no AsyncStorage; manter o login ao reabrir o app
  Token no SecureStore e login mantido ao reabrir (PR #10); último filtro no AsyncStorage (PR [#19](https://github.com/GustAndrade07/caderneta-de-fiado/pull/19)); dados do usuário (nome e e-mail) no AsyncStorage, apagados no logout (PR [#20](https://github.com/GustAndrade07/caderneta-de-fiado/pull/20)).
- [x] Serviço de API: URL base, envio do token em toda requisição, checagem de `response.ok`, timeout, logout automático em 401
  URL base (`EXPO_PUBLIC_API_URL`), token em toda requisição e logout automático em 401 no PR #10; o axios já rejeita respostas fora de 2xx (equivale ao `response.ok`). Timeout de 10 s (`timeout: 10000` em `services/api.ts`) entrou pelo PR [#15 Login e cadastro](https://github.com/GustAndrade07/caderneta-de-fiado/pull/15).
- [x] Tema centralizado (cores e espaçamentos)
  Cores em `theme/colors.ts` no PR #10; `spacing` e `radius` no mesmo arquivo pelo PR [#20](https://github.com/GustAndrade07/caderneta-de-fiado/pull/20). As telas ainda usam valores fixos em vez dessas escalas.
- [x] Dar suporte aos outros 3 do front
  João Pedro deu suporte ao Nicolas, ao Vinicius e ao Eduardo durante a integração.

**Estudar:** como funciona a navegação em pilha e em abas, Context e `useContext`, a diferença entre SecureStore e AsyncStorage e por que o token não fica no AsyncStorage.

### 6. Nicolas Thomaz — Lista e Detalhe

- [x] Tela de lista com `FlatList` (`keyExtractor` usando o `id`), carregamento, lista vazia e puxar para atualizar
  PR: [#11 Tela de lista de fiados com total devido, pagamento e exclusão](https://github.com/GustAndrade07/caderneta-de-fiado/pull/11)
- [x] Total devido no topo, somando os fiados não pagos
  PR: [#11 Tela de lista de fiados com total devido, pagamento e exclusão](https://github.com/GustAndrade07/caderneta-de-fiado/pull/11)
- [x] Filtro todos/pendentes/pagos, salvo no AsyncStorage
  PR: [#19 Tela de detalhe e filtro de fiados salvo no AsyncStorage](https://github.com/GustAndrade07/caderneta-de-fiado/pull/19) (feito por Gustavo)
- [x] Tela de detalhe: botões de editar, marcar como pago e excluir (com confirmação)
  PR: [#19 Tela de detalhe e filtro de fiados salvo no AsyncStorage](https://github.com/GustAndrade07/caderneta-de-fiado/pull/19) (feito por Gustavo). A lista abre o detalhe ao tocar no card e também tem botão Editar.
- [x] Atualizar a lista sem `push`: usar `map` e `filter`
  PR: [#11 Tela de lista de fiados com total devido, pagamento e exclusão](https://github.com/GustAndrade07/caderneta-de-fiado/pull/11)

**Estudar:** `useState` e `useEffect`, por que a `FlatList` e não a `ScrollView`, o caminho de um GET do app até o banco.

### 7. Vinicius Soares — Login e Cadastro

- [x] Tela de login: e-mail, senha (`secureTextEntry`), botão com carregamento, mensagem de erro
  PR: [#15 Login e cadastro](https://github.com/GustAndrade07/caderneta-de-fiado/pull/15)
- [x] Tela de cadastro: nome, e-mail, senha e confirmação de senha
  PR: [#15 Login e cadastro](https://github.com/GustAndrade07/caderneta-de-fiado/pull/15)
- [x] Validar os campos antes de enviar e mostrar o erro que a API devolve
  PR: [#15 Login e cadastro](https://github.com/GustAndrade07/caderneta-de-fiado/pull/15)
- [x] Usar `KeyboardAvoidingView` para o teclado não cobrir os campos
  PR: [#15 Login e cadastro](https://github.com/GustAndrade07/caderneta-de-fiado/pull/15)
- [x] Integrar com o `login` do João Pedro e os endpoints do Ryan
  PR: [#15 Login e cadastro](https://github.com/GustAndrade07/caderneta-de-fiado/pull/15)

**Estudar:** `TextInput` controlado, o que acontece do toque em "Entrar" até o token salvo, `async/await`.

### 8. Eduardo Campos — Formulário e Perfil

- [x] Formulário de fiado: cliente, valor, data e descrição; o mesmo formulário cria (POST) e edita (PUT)
  PR: [#14 Formulário de fiado e tela de perfil](https://github.com/GustAndrade07/caderneta-de-fiado/pull/14)
- [x] Validar valor maior que zero e cliente obrigatório
  PR: [#14 Formulário de fiado e tela de perfil](https://github.com/GustAndrade07/caderneta-de-fiado/pull/14)
- [x] Voltar para a lista atualizada depois de salvar
  PR: [#14 Formulário de fiado e tela de perfil](https://github.com/GustAndrade07/caderneta-de-fiado/pull/14)
- [x] Tela de perfil: dados do `/usuarios/me` e botão Sair, que apaga o token do SecureStore
  PR: [#14 Formulário de fiado e tela de perfil](https://github.com/GustAndrade07/caderneta-de-fiado/pull/14)
- [x] Corrigir o botão Sair na versão web: o `Alert.alert` com botões não aparece no navegador, então o logout não acontecia
  PR: [#19 Tela de detalhe e filtro de fiados salvo no AsyncStorage](https://github.com/GustAndrade07/caderneta-de-fiado/pull/19) (feito por Gustavo). O Sair passou a usar `confirmar()` e os avisos de erro do formulário usam `avisar()`, ambos de `utils/confirmar.ts`.

**Estudar:** como a tela recebe parâmetros pela navegação (`route.params`), a diferença entre POST e PUT, o que o logout apaga e por quê.

## Situação em 17/11/2026: projeto completo

Todas as tarefas do plano estão concluídas e na `main` (até o PR #22).

- **Back-end:** completo. API protegida por JWT (cadastro, login, perfil, CRUD de fiados, total devido, erros em JSON), senha com mínimo de 8 caracteres, CORS liberado para o app web, banco SQLite com dados de exemplo, 54 testes automáticos passando, evidência do teste 401/200 e coleção do Postman com todos os endpoints de `/fiados`.
- **Front-end:** completo. Login, Cadastro, abas Fiados e Perfil, Lista (com filtro salvo no AsyncStorage), Detalhe e Formulário, integrados com a API, com token no SecureStore, usuário no `AuthContext` e no AsyncStorage, timeout e logout em 401. Testado por todos os integrantes no celular, com Expo Go, e também na web.
- **Documentação:** README completo, com back e front (tecnologias, pastas, execução e Node 22.13+).

## Cronograma

Até 15/11 o código de cada um precisa estar na `main`. O dia 16/11 fica só para testes e ensaio.

| Data | Back-end | Front-end | Gate |
| --- | --- | --- | --- |
| Qui 12/11 | Gustavo cria o repositório e o projeto Spring Boot | João Pedro cria o projeto Expo | Todos com acesso ao repositório |
| Sex 13/11 | **Decisão das stacks do back.** Gustavo: entidades e repositories. Leandro e Ryan começam o JWT e o login | **Decisão das stacks do front.** João Pedro: navegação e estado do login. Os outros começam as telas com dados fixos | Stacks no README |
| Sáb 14/11 | Pedro: CRUD de fiados. Ryan: tratamento de erros. Gustavo: seed | Nicolas, Vinicius e Eduardo terminam as telas | API inteira funcionando no Postman |
| Dom 15/11 | Correções da integração | Integração com a API real (duplas Ryan–Vinicius e Pedro–Nicolas–Eduardo) | Fluxo completo rodando no celular |
| Seg 16/11 | Testes, README final, congelar o código | Testes no celular | Ensaio com perguntas cruzadas |
| Ter 17/11 | Entrega | Entrega | Link do repositório entregue |

## Regras de Git e commits

O professor penaliza commits artificiais. Cada pessoa commita apenas o próprio código, da própria conta do GitHub, aos poucos ao longo dos dias.

1. Uma branch por tarefa: `funcionalidade/nome-da-tarefa` (ex.: `funcionalidade/seguranca-jwt`).
2. Commits pequenos e frequentes, com mensagem clara: `feat: endpoint de login`, `fix: validação de valor no formulário`.
3. Abrir um Pull Request para a `main`; uma pessoa da mesma área revisa antes do merge.
4. Ninguém commita pelo outro e ninguém sobe código de outra pessoa.
5. Nunca subir `fiado.db`, `node_modules`, `target` nem a chave do JWT.
6. Antes do merge, rodar o projeto e conferir que nada quebrou.
7. **Todo PR atualiza este plano.** Antes de pedir revisão, o autor marca neste arquivo (`PLANO_DE_EXECUCAO.md`) as tarefas que o PR concluiu e cola o link do PR ao lado delas. A alteração do plano vai no próprio PR. O template de PR do repositório (`.github/pull_request_template.md`) traz essa caixa de seleção obrigatória, e quem revisa só aprova o merge depois de conferir que o plano foi atualizado.

## Preparação para a apresentação

O professor escolhe qualquer integrante e a nota depende da resposta. Todos precisam saber explicar a própria parte em detalhe e o fluxo completo em linhas gerais:

> Login → o back confere a senha (BCrypt) → gera o JWT → o app guarda o token no SecureStore → toda requisição envia o token → o filtro valida → controller → service → repository → SQLite.

**Perguntas que todos devem saber responder**

- Onde cada requisito do enunciado aparece no código (telas, navegação, JWT, REST, estado, AsyncStorage, banco, CRUD)
- Por que o token fica no SecureStore e o que vai no AsyncStorage
- Por que SQLite conta como banco relacional
- Qual problema real o app resolve

**Ensaio em 16/11:** cada pessoa responde 3 perguntas sorteadas sobre uma parte que não é a dela.

**Roteiro da demonstração:** cadastrar → entrar → criar um fiado → editar → marcar como pago → excluir → fechar e reabrir o app (continua logado) → sair.

## Pendências

- [x] RM dos 8 integrantes, para o README — Gustavo coleta
- [x] Stacks do back decididas e registradas no README — Gustavo, Leandro, Pedro, Ryan, até 13/11
  Registradas na tabela de tecnologias do README (Java 21, Maven, jjwt, Lombok, DTOs). Swagger não foi adotado.
- [x] Stacks do front decididas e registradas no README — João Pedro, Nicolas, Vinicius, Eduardo, até 13/11
  Registradas na tabela de tecnologias do README pelo PR [#19](https://github.com/GustAndrade07/caderneta-de-fiado/pull/19): TypeScript, Expo Router, Context API, axios, SecureStore, AsyncStorage e estilo próprio.
- [x] App no celular (Expo Go) não conectava à API: sem `.env`, o app chamava `localhost:8080`, que no celular é o próprio aparelho — Gustavo
  PR: [#24 App descobre sozinho o IP da API no celular](https://github.com/GustAndrade07/caderneta-de-fiado/pull/24). O `services/api.ts` passou a usar o IP do PC que rodou o `npx expo start`; o README explica Wi-Fi e firewall.
- [x] Confirmar o número de informalidade no site do IBGE — Gustavo
  README traz 37,5% (trimestre encerrado em setembro de 2026) com link para o release do IBGE.

