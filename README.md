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
- Tela de detalhe do fiado, com editar, marcar como pago e excluir (com confirmação)
- Marcar um fiado como pago
- Total devido exibido na lista
- Filtro da lista (todos, pendentes e pagos), com a última escolha salva no aparelho
- Perfil com os dados do usuário e botão Sair
- Login mantido ao reabrir o app

## Tecnologias

| Área | Tecnologia |
| --- | --- |
| Front-end | React Native 0.88 com Expo SDK 58, em TypeScript |
| Navegação | Expo Router (rotas por arquivo em `frontend/src/app`), com rotas públicas e protegidas |
| Estado | Context API (`AuthContext`) |
| Requisições | axios, com token JWT em toda requisição, timeout de 10 s e logout automático em 401 |
| Armazenamento no app | `expo-secure-store` para o token JWT; AsyncStorage para dados não sensíveis (último filtro da lista) |
| Interface | Componentes do React Native com estilo próprio (`StyleSheet`) e cores centralizadas em `theme/colors.ts` |
| Back-end | Java 21, Spring Boot 4.1.2 |
| Build | Maven (via Maven Wrapper `mvnw`, não precisa instalar o Maven) |
| Persistência | Spring Data JPA (Hibernate) com o dialeto SQLite do `hibernate-community-dialects` |
| Banco de dados | SQLite (driver `sqlite-jdbc`), arquivo `fiado.db` |
| Segurança | Spring Security com autenticação JWT (biblioteca `jjwt` 0.12.6, algoritmo HS256) e senhas com hash BCrypt |
| Validação | Spring Boot Validation (Bean Validation: `@NotBlank`, `@Email`, `@Positive`...) |
| API | Spring Web MVC (REST) |
| Produtividade | Lombok (getters, setters e construtores gerados) |
| Testes | JUnit 5 com os starters de teste do Spring Boot |

## Estrutura do repositório

```
caderneta-de-fiado/
├─ backend/    API REST em Spring Boot
└─ frontend/   App em React Native (Expo)
```

Pastas do front-end (`frontend/src`):

| Pasta | Conteúdo |
| --- | --- |
| `app` | Rotas do Expo Router: `_layout.tsx` (pilha e proteção das rotas), `login`, `cadastro`, `index` (lista), `detalhe/[id]`, `formulario` e `perfil` |
| `screens` | Telas: `LoginScreen`, `CadastroScreen`, `ListaFiadosScreen`, `DetalheFiadoScreen`, `FormularioFiadoScreen`, `PerfilScreen` |
| `contexts` | `AuthContext`: token, carregamento, `signIn` e `signOut` |
| `services` | Cliente HTTP (`api.ts`), autenticação, token no SecureStore e filtro no AsyncStorage |
| `utils` | Formatação de moeda e data e confirmações que funcionam no celular e na web |
| `theme` | Cores do app |
| `types` | Tipos `Usuario`, `Fiado` e `AuthResponse` |

Pacotes do back-end (`backend/src/main/java/br/com/senac/cadernetafiado`):

| Pacote | Conteúdo |
| --- | --- |
| `model` | Entidades JPA (`Usuario`, `Fiado`) |
| `repository` | Interfaces Spring Data (`UsuarioRepository`, `FiadoRepository`) |
| `service` | Regras de negócio (`FiadoService`, `AutenticacaoService`) |
| `controller` | Camada HTTP (`AuthController`, `UsuarioController`, `FiadoController`) |
| `dto` | Objetos de entrada e saída da API |
| `exception` | Exceções de domínio e `GlobalExceptionHandler` |
| `security` | Autenticação JWT: `JwtService`, `JwtFiltro`, `UsuarioDetalhes` |
| `config` | `SecurityConfig` e dados de exemplo (`DadosExemplo`) |

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

## Segurança (JWT)

Quem chama a API precisa provar quem é. O login devolve um token, e esse token vai em toda requisição seguinte.

### Rotas

| Rota | Precisa de token? |
| --- | --- |
| `/auth/**` (cadastro e login) | Não |
| Todas as outras | Sim |

A regra é **negar por padrão**: endpoint novo já nasce protegido, sem precisar mexer na configuração de segurança.

### O token

Vai no cabeçalho, assim:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Dentro dele só tem o e-mail do usuário (`sub`), a hora de emissão (`iat`) e a de expiração (`exp`, 24 horas depois). Senha e dado pessoal nunca entram no token.

### Chave secreta

A chave que assina o token vem da variável de ambiente `JWT_SECRET` e **não fica no repositório**. Precisa de pelo menos 32 caracteres.

```bash
# Windows (PowerShell)
$env:JWT_SECRET = "uma-chave-longa-e-aleatoria-com-32-ou-mais"
```

```bash
# Linux / Mac
export JWT_SECRET="uma-chave-longa-e-aleatoria-com-32-ou-mais"
```

Sem a variável, a aplicação **sobe normalmente** em desenvolvimento, com uma chave sorteada e um aviso no log — ninguém fica travado. O preço é que reiniciar a aplicação invalida os tokens, e quem estava logado precisa entrar de novo. Em produção (perfil `prod`) a variável é obrigatória: faltando, a aplicação não sobe.

### Como pegar o usuário logado no controller

Nunca confie no id que vem na URL ou no corpo da requisição — senão um usuário consegue ler o fiado do outro. O dono da requisição sai sempre do token:

```java
@GetMapping("/fiados")
public List<Fiado> listar(@AuthenticationPrincipal UsuarioDetalhes logado) {
    Long idDoDono = logado.getUsuario().getId();
    return servico.listarPorUsuario(idDoDono);
}
```

Para o cadastro e o login, o pacote `seguranca` publica também um `PasswordEncoder` (BCrypt), um `AuthenticationManager` e o próprio `JwtService` — basta injetar.

### Erros

| Situação | Resposta |
| --- | --- |
| Sem token, token vencido, adulterado ou de usuário que não existe mais | `401` |
| Autenticado, mas sem acesso ao recurso | `403` |

Sempre em JSON, no mesmo formato:

```json
{ "status": 401, "erro": "nao_autorizado", "mensagem": "Token ausente ou inválido" }
```

O app usa o `401` para apagar o token e voltar para a tela de login.

### Conferindo na mão

```bash
curl -i http://localhost:8080/fiados
```

Sem token, a resposta é `401`. Com `-H "Authorization: Bearer <token>"`, a requisição passa pela segurança normalmente.

## Como executar o front-end

### Pré-requisitos

- **Node.js 22.13 ou superior** (22 ou 24 LTS) instalado — confira com `node -v`. O Expo SDK 58 não roda no Node 18 nem no 20
- **Expo Go** instalado no celular (Android/iOS), emulador configurado no computador ou um navegador para a versão web

### Configuração da API

Na pasta `frontend/`, crie `.env.local` (ignorado pelo Git) com o endereço da API:

```dotenv
# Emulador Android
EXPO_PUBLIC_API_URL=http://10.0.2.2:8080
```

Para um celular físico com Expo Go, troque `10.0.2.2` pelo IPv4 do computador na mesma rede Wi-Fi, por exemplo `http://192.168.1.10:8080`. O `localhost` do celular aponta para o próprio celular.

Em dois terminais, a partir da raiz do repositório:

```powershell
cd backend
$env:JWT_SECRET = "uma-chave-local-com-mais-de-32-caracteres"
.\mvnw.cmd spring-boot:run
```

```powershell
cd frontend
npm ci
npx expo start --lan
```

Abra o app no Expo Go ou no emulador. Para usar no navegador, aperte `w` no terminal do Expo (ou rode `npx expo start --web`); sem `.env.local`, a web acessa a API em `http://localhost:8080` (se o `.env.local` apontar para `10.0.2.2`, a web não conecta). Para testar, use `demo@caderneta.com` / `senha123` ou crie uma conta. O cadastro salva o usuário em `backend/fiado.db` e entra automaticamente. Ao iniciar de novo, o app confere o token em `/usuarios/me`; mantenha `JWT_SECRET` igual entre reinícios da API para preservar a sessão.

## Como contribuir

1. Crie uma branch a partir da `main`: `funcionalidade/nome-da-tarefa`
2. Faça commits pequenos e com mensagens claras, em português
3. Abra um Pull Request para a `main` e preencha o checklist do template
4. Atualize o [Plano de Execução](PLANO_DE_EXECUCAO.md) marcando as tarefas concluídas
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
