# CA-19 - rota protegida: sem token 401, com token 200

Conferido na maquina local em 16/11/2026, com o backend na porta 8080 e os dados de exemplo do seed.
Usuario da demonstracao: demo@caderneta.com / senha123.

## 1) GET /fiados sem cabecalho Authorization -> 401

```http
HTTP/1.1 401 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json;charset=UTF-8
Content-Length: 78
Date: Mon, 16 Nov 2026 23:38:15 GMT

{"status":401,"erro":"nao_autorizado","mensagem":"Token ausente ou inválido"}
```

## 2) POST /auth/login -> 200 e devolve o token

```http
HTTP/1.1 200 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Content-Length: 197
Date: Mon, 16 Nov 2026 23:38:15 GMT

{"token":"eyJhbGciOiJIUzM4NCJ9...(cortado)","tipo":"Bearer"}
```

## 3) GET /fiados com Authorization: Bearer <token> -> 200

```http
HTTP/1.1 200 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Content-Length: 554
Date: Mon, 16 Nov 2026 23:38:15 GMT

[{"id":1,"nomeCliente":"Dona Maria","valor":35.9,"data":"2026-11-15","descricao":"Arroz, feijão e óleo","pago":false},{"id":2,"nomeCliente":"Seu Zé","valor":12.5,"data":"2026-11-13","descricao":"Pão e leite","pago":false},{"id":3,"nomeCliente":"Carla","valor":58,"data":"2026-11-09","descricao":"Produtos de limpeza","pago":false},{"id":4,"nomeCliente":"Antônio","valor":20,"data":"2026-11-06","descricao":"Café e açúcar","pago":true},{"id":5,"nomeCliente":"Dona Maria","valor":47.3,"data":"2026-11-01","descricao":"Compras do mês","pago":true}]
```

## 4) Extra - GET /fiados com o token adulterado -> 401, nao 500

```http
HTTP/1.1 401 
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json;charset=UTF-8
Content-Length: 78
Date: Mon, 16 Nov 2026 23:38:15 GMT

{"status":401,"erro":"nao_autorizado","mensagem":"Token ausente ou inválido"}
```

Os mesmos casos estao cobertos por teste automatizado em `SegurancaHttpTestes` e `SegurancaWebTestes`.
