# API de Gerenciamento de Usuários

API REST para gerenciamento de usuários, construída com **Java 17**, **Spring Boot 3**, **MongoDB**, DTOs, validação, tratamento global de exceções e documentação OpenAPI.

Projeto do portfólio baseado no item **“API de Gerenciamento de Usuários | Java, Spring Boot, MongoDB”** do currículo.

## Funcionalidades

- CRUD completo de usuários (`POST`, `GET`, `PUT`, `PATCH`, `DELETE`)
- Paginação, ordenação, busca por nome/e-mail e filtros por perfil/status
- DTOs de entrada e saída separados da entidade de persistência
- Validação com Jakarta Bean Validation
- E-mails normalizados e protegidos por índice único no MongoDB
- Tratamento de erros padronizado com `ProblemDetail` (RFC 9457)
- Ativação/desativação de usuários
- Documentação interativa com Swagger UI
- Health check com Spring Boot Actuator
- MongoDB embarcado no perfil `dev` (roda sem instalar nada)
- Testes de integração com MongoDB embarcado
- Docker e Docker Compose para execução com MongoDB real

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.5, Spring Web, Spring Data MongoDB |
| Banco | MongoDB 8 |
| Validação | Jakarta Validation |
| Documentação | Springdoc OpenAPI / Swagger UI |
| Testes | JUnit 5, MockMvc, AssertJ, Flapdoodle Embedded MongoDB |
| Infra | Maven, Docker, Docker Compose |

## Arquitetura

```text
src/main/java/com/eliascampos/usermanagement
├── config/          # OpenAPI
├── exception/       # Exceções de negócio + handler global
├── user/
│   ├── dto/         # Requests, responses e paginação
│   ├── User.java    # Documento MongoDB
│   ├── UserRole.java
│   ├── UserRepository.java
│   ├── UserService.java
│   └── UserController.java
└── UserManagementApiApplication.java
```

Fluxo: `Controller → Service → Repository/MongoTemplate → MongoDB`.

## Como rodar

### Pré-requisitos

- JDK 17+
- Maven 3.9+ (ou use o wrapper `./mvnw`)

### Desenvolvimento (sem instalar MongoDB)

```bash
mvn spring-boot:run
```

O perfil `dev` sobe automaticamente um **MongoDB embarcado** em uma porta aleatória. Na primeira execução, o binário do MongoDB é baixado e fica em cache (pode levar alguns minutos).

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### Com MongoDB real

```bash
# Linux/macOS
SPRING_PROFILES_ACTIVE=prod MONGODB_URI="mongodb://localhost:27018/usuarios" mvn spring-boot:run

# PowerShell
$env:SPRING_PROFILES_ACTIVE="prod"; $env:MONGODB_URI="mongodb://localhost:27018/usuarios"; mvn spring-boot:run
```

Em produção também é possível apontar para o MongoDB Atlas usando a mesma variável `MONGODB_URI`.

Com o perfil `prod`, `MONGODB_URI` é **obrigatória**: se faltar, a aplicação falha na subida em vez de tentar um banco local. Nesse perfil o health check também não expõe detalhes internos.

O índice único de e-mail é criado na subida (`auto-index-creation`). Para rodar várias instâncias ou fazer deploy sem downtime, mova a criação de índices para um passo de migração antes de iniciar a aplicação.

### Docker Compose

```bash
docker compose up --build
```

Sobe a API em `http://127.0.0.1:8081` e o MongoDB apenas na rede interna do Compose, sem publicar porta no host. Isso permite rodar o Compose junto com a API local (`8080`) e com o MongoDB local (`27018`) sem conflito.

Para inspecionar o banco do container: `docker compose exec mongo mongosh usuarios`.

## Endpoints

Base: `/api/v1/users`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/users` | Cria usuário |
| `GET` | `/api/v1/users` | Lista com paginação e filtros |
| `GET` | `/api/v1/users/{id}` | Busca por ID |
| `PUT` | `/api/v1/users/{id}` | Atualiza dados |
| `PATCH` | `/api/v1/users/{id}/status` | Ativa/desativa |
| `DELETE` | `/api/v1/users/{id}` | Remove usuário |

### Criar usuário

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Maria Silva",
    "email": "maria.silva@email.com",
    "role": "USER"
  }'
```

Resposta `201 Created`:

```json
{
  "id": "665f1c2a9b1e4a3d8c7b6a51",
  "name": "Maria Silva",
  "email": "maria.silva@email.com",
  "role": "USER",
  "active": true,
  "createdAt": "2026-11-07T18:00:00Z",
  "updatedAt": "2026-11-07T18:00:00Z"
}
```

### Listar com filtros

```bash
curl "http://localhost:8080/api/v1/users?page=0&size=10&sort=name,asc&search=maria&role=USER&active=true"
```

Parâmetros: `page`, `size` (máx. 50), `sort`, `search`, `role` (`USER`/`ADMIN`), `active` (`true`/`false`).

### Atualizar e desativar

```bash
curl -X PUT http://localhost:8080/api/v1/users/{id} \
  -H "Content-Type: application/json" \
  -d '{"name":"Maria Silva Santos","email":"maria.santos@email.com","role":"ADMIN"}'

curl -X PATCH http://localhost:8080/api/v1/users/{id}/status \
  -H "Content-Type: application/json" \
  -d '{"active":false}'
```

### Erros

Os erros seguem o formato `application/problem+json`:

```json
{
  "type": "about:blank",
  "title": "Dados invalidos",
  "status": 400,
  "detail": "Um ou mais campos estao invalidos",
  "instance": "/api/v1/users",
  "code": "VALIDATION_ERROR",
  "errors": {
    "email": "Informe um e-mail valido"
  },
  "timestamp": "2026-11-07T18:00:00Z"
}
```

Códigos: `VALIDATION_ERROR` (400), `INVALID_REQUEST` (400), `RESOURCE_NOT_FOUND` (404), `EMAIL_ALREADY_EXISTS` (409), `INTERNAL_ERROR` (500).

## Testes

```bash
mvn test
```

Os testes sobem um MongoDB embarcado e cobrem criação, leitura, atualização, ativação, exclusão, paginação, busca, filtros, e-mail duplicado, validação e recurso inexistente.

## Decisões técnicas

- **DTOs em vez de expor a entidade**: evita acoplamento entre contrato da API e modelo de persistência.
- **E-mail normalizado para minúsculas**: e-mails são case-insensitive na prática; o índice único garante unicidade.
- **`ProblemDetail`**: erros padronizados e compatíveis com o padrão HTTP `application/problem+json`.
- **`MongoTemplate` na listagem**: permite montar filtros dinâmicos com `Criteria` sem proliferar métodos no repositório.
- **MongoDB embarcado em dev/test**: experiência de execução sem dependência externa, mantendo MongoDB real em produção.
- **Perfil `prod` desativa o MongoDB embarcado**: evita subir banco efêmero por engano em produção.

## Próximos passos sugeridos

- Autenticação/autorização com Spring Security + JWT
- Testes unitários do service com Mockito
- CI com GitHub Actions
- Observabilidade com Micrometer/Tracing
