# API de Gerenciamento de Usuários

[![CI](https://github.com/EliasCamposDEV/api-usuarios/actions/workflows/ci.yml/badge.svg)](https://github.com/EliasCamposDEV/api-usuarios/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/github/license/EliasCamposDEV/api-usuarios)](LICENSE)

API REST para CRUD de usuários, com validação, paginação, filtros e documentação OpenAPI. Java 17, Spring Boot 3.5, MongoDB.

## Rodar

```bash
mvn spring-boot:run
```

Perfil `dev` sobe um MongoDB embarcado (primeira execução baixa o binário). API em http://localhost:8080.

Com MongoDB real (porta 27018):

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:MONGODB_URI="mongodb://localhost:27018/usuarios"
mvn spring-boot:run
```

`MONGODB_URI` é obrigatória em `prod`; sem ela a aplicação não sobe.

## Docker

```bash
docker compose up --build
```

API em http://127.0.0.1:8081, MongoDB apenas na rede interna do Compose. Para inspecionar o banco: `docker compose exec mongo mongosh usuarios`.

## Endpoints

Base: `/api/v1/users`

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/users` | Cria usuário |
| GET | `/api/v1/users` | Lista com paginação e filtros |
| GET | `/api/v1/users/{id}` | Busca por ID |
| PUT | `/api/v1/users/{id}` | Atualiza |
| PATCH | `/api/v1/users/{id}/status` | Ativa/desativa |
| DELETE | `/api/v1/users/{id}` | Remove |

Parâmetros de listagem: `page`, `size` (máx. 50), `sort`, `search`, `role` (USER/ADMIN), `active` (true/false).

Exemplo de criação:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Maria Silva","email":"maria.silva@email.com","role":"USER"}'
```

Erros retornam `application/problem+json` com `code`: `VALIDATION_ERROR`, `INVALID_REQUEST`, `RESOURCE_NOT_FOUND`, `EMAIL_ALREADY_EXISTS`, `INTERNAL_ERROR`.

## Testes

```bash
mvn test
```

5 testes de integração com MongoDB embarcado cobrindo criação, leitura, atualização, ativação, exclusão, paginação, busca, filtros, e-mail duplicado, validação e recurso inexistente.

## Swagger

http://localhost:8080/swagger-ui.html — documentação interativa e esquemas de request/response.

## Notas

- E-mail é normalizado para minúsculas e protegido por índice único.
- O índice único de e-mail é criado na subida (`auto-index-creation`). Para várias instâncias ou deploy sem downtime, mova a criação de índices para um passo de migração.