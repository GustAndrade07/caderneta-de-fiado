# Caderneta de Fiado

Aplicativo mobile para pequenos comerciantes registrarem vendas fiado: quem deve, quanto deve e quando pagou — substituindo o caderno de papel.

Projeto da P3 — Atividade final (Projeto Mobile: Aplicação Full Stack).

## Problema

<!-- Dados do IBGE (PNAD Contínua) sobre informalidade, com link da fonte -->

## Funcionalidades

- Cadastro e login de usuário com JWT
- Cadastro, consulta, edição e exclusão de fiados
- Marcar um fiado como pago
- Total devido exibido na lista
- Login mantido ao reabrir o app

## Tecnologias

| Área | Tecnologia |
| --- | --- |
| Front-end | React Native (Expo) — demais escolhas definidas pela equipe de front |
| Back-end | Spring Boot — demais escolhas definidas pela equipe de back |
| Banco de dados | SQLite |

## Estrutura do repositório

```
caderneta-de-fiado/
├─ backend/    API REST em Spring Boot
└─ frontend/   App em React Native (Expo)
```

## Como executar

<!-- Será preenchido quando o back-end e o front-end estiverem prontos -->

## Como contribuir

1. Crie uma branch a partir da `main`: `feature/nome-da-tarefa`
2. Faça commits pequenos e com mensagens claras
3. Abra um Pull Request para a `main` e preencha o checklist do template
4. Atualize o [Plano de Execução](https://claude.ai/artifact/K1DiyxEiQdNSsdgbUCeeNg) marcando as tarefas concluídas
5. Uma pessoa da mesma área revisa e aprova antes do merge

## Integrantes

| Nome completo | RM | Função |
| --- | --- | --- |
| Gustavo Oliveira Fernandes de Andrade | | Banco, entidades e coordenação |
| Leandro Sato | | Segurança JWT |
| Pedro Henrique Teixeira | | API do Fiado |
| Ryan Santos | | Login, cadastro e erros da API |
| João Pedro Cavanhas | | Base do app |
| Nicolas Thomaz | | Lista e Detalhe |
| Vinicius Soares | | Login e Cadastro |
| Eduardo Campos | | Formulário e Perfil |
