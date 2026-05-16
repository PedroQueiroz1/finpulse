# Contrato de API

> Documentação dos endpoints existentes e novos das FASES 8-11.
> Esta é a **fonte da verdade** sobre paths, métodos HTTP, payloads e status codes.

---

## 🌍 URLs Base

### Em desenvolvimento (sem Gateway)
- Auth: `http://localhost:8081/api/auth`
- Notes: `http://localhost:8082/api/notes`
- Stock: `http://localhost:8083/api/stocks`

### Em desenvolvimento (com Gateway — pós-FASE 11)
- Tudo: `http://localhost:8080/api/{auth|notes|stocks}/...`

### Em produção (pós-FASE 11)
- `https://api.finpulse.com/api/{auth|notes|stocks}/...`

---

## 🔐 auth-service (FASE 3 ✅)

### `POST /api/auth/register`
Cria novo usuário e retorna tokens.

**Request:**
```json
{
    "name": "Pedro Queiroz",
    "email": "pedro@finpulse.com",
    "password": "senha12345"
}
```

**Validações:**
- `name`: obrigatório, 2-150 caracteres
- `email`: obrigatório, formato válido
- `password`: obrigatório, 8-100 caracteres

**Response 201 Created:**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "user": {
        "id": "uuid",
        "name": "Pedro Queiroz",
        "email": "pedro@finpulse.com",
        "role": "USER",
        "active": true
    }
}
```

**Erros:**
- `400`: validação falhou (campos `validationErrors` no body)
- `409`: email já cadastrado

---

### `POST /api/auth/login`
Autentica usuário existente.

**Request:**
```json
{
    "email": "pedro@finpulse.com",
    "password": "senha12345"
}
```

**Response 200 OK:** Mesmo formato do register.

**Erros:**
- `400`: validação
- `401`: credenciais inválidas (email não existe OU senha errada — mesma mensagem por segurança)

---

### `POST /api/auth/refresh`
Renova access token usando refresh token.

**Headers:**
```
Authorization: Bearer {refreshToken}
```

**Response 200 OK:** Mesmo formato do login.

**Erros:**
- `401`: refresh token inválido ou expirado

---

### `GET /api/auth/me`
Retorna dados do usuário autenticado.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response 200 OK:**
```json
{
    "id": "uuid",
    "name": "Pedro Queiroz",
    "email": "pedro@finpulse.com",
    "role": "USER",
    "active": true
}
```

**Erros:**
- `401`: sem JWT ou JWT inválido (Spring Security retorna 403, mas após FASE 11 com Gateway retorna 401)

---

### `GET /api/auth/health`
Health check simples.

**Response 200 OK:** `"Auth Service is running!"`

---

## 📝 notes-service (FASE 4 ✅, observability na 8.1)

**ATENÇÃO:** Os endpoints exatos devem ser verificados pelo Claude Code inspecionando `NotesController.java`. Este contrato é a **estrutura esperada**.

### `GET /api/notes`
Lista todas as notas do usuário autenticado.

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Query params (opcionais):**
- `tag`: filtrar por tag
- `search`: busca por texto

**Response 200 OK:**
```json
[
    {
        "id": "65a1b2c3d4e5f6789012",
        "title": "Reunião com cliente",
        "content": "Discutir requisitos do projeto X...",
        "tags": ["trabalho", "cliente-x"],
        "createdAt": "2026-05-14T10:00:00Z",
        "updatedAt": "2026-05-14T10:30:00Z"
    }
]
```

---

### `POST /api/notes`
Cria nova nota.

**Request:**
```json
{
    "title": "Nova nota",
    "content": "Conteúdo em markdown",
    "tags": ["tag1", "tag2"]
}
```

**Validações:**
- `title`: obrigatório
- `content`: opcional
- `tags`: opcional, array de strings

**Response 201 Created:** Nota criada com `id` gerado.

---

### `GET /api/notes/{id}`
Detalhes de uma nota.

**Response 200 OK:** Objeto Note.

**Erros:**
- `404`: nota não existe
- `403`: nota é de outro usuário

---

### `PUT /api/notes/{id}`
Atualiza nota.

**Request:** Mesmo formato do POST.

**Response 200 OK:** Nota atualizada.

**Erros:**
- `403`: nota de outro usuário
- `404`: nota não existe

---

### `DELETE /api/notes/{id}`
Soft delete (marca `deleted=true`).

**Response 204 No Content**

**Erros:**
- `403` / `404`

---

### `GET /api/notes/groups`
Lista tags/grupos do usuário.

**Response 200 OK:**
```json
[
    {"name": "trabalho", "count": 12},
    {"name": "pessoal", "count": 5}
]
```

---

## 📈 stock-service (FASE 5 ✅, observability na 8.2)

### `GET /api/stocks/{symbol}/quote`
Cotação atual do ativo.

**Path params:**
- `symbol`: ticker (ex: AAPL, MSFT)

**Response 200 OK:**
```json
{
    "symbol": "AAPL",
    "price": 182.34,
    "currency": "USD",
    "changePercent": 0.45,
    "previousClose": 181.52,
    "timestamp": "2026-05-14T10:23:14Z",
    "provider": "alpha-vantage",
    "cached": false
}
```

**Headers de resposta:**
- `X-Cache-Status`: `HIT` ou `MISS`
- `X-Provider`: `alpha-vantage` ou `finnhub`

**Erros:**
- `400`: symbol inválido
- `503`: ambos providers falharam (sem cache fallback)

---

### `GET /api/stocks/{symbol}/company`
Dados detalhados da empresa.

**Response 200 OK:**
```json
{
    "symbol": "AAPL",
    "name": "Apple Inc.",
    "sector": "Technology",
    "industry": "Consumer Electronics",
    "description": "...",
    "marketCap": 2800000000000,
    "lastFetchedAt": "2026-05-14T10:00:00Z"
}
```

---

### `GET /api/stocks/providers`
Status dos providers.

**Response 200 OK:**
```json
{
    "providers": [
        {
            "name": "alpha-vantage",
            "status": "UP",
            "circuitBreakerState": "CLOSED",
            "lastSuccessfulCall": "2026-05-14T10:22:00Z"
        },
        {
            "name": "finnhub",
            "status": "UP",
            "circuitBreakerState": "CLOSED",
            "lastSuccessfulCall": "2026-05-14T10:22:00Z"
        }
    ]
}
```

---

## 📊 Endpoints de Observability (FASES 7 e 8)

Disponíveis em **todos os serviços** após implementação:

### `GET /actuator/health`
Status geral.

**Response 200 OK:**
```json
{
    "status": "UP",
    "groups": ["liveness", "readiness"],
    "components": {
        "db": { "status": "UP", ... },
        "discoveryComposite": { "status": "UP", ... },
        "diskSpace": { "status": "UP", ... }
    }
}
```

### `GET /actuator/health/liveness`
Kubernetes liveness probe.

### `GET /actuator/health/readiness`
Kubernetes readiness probe.

### `GET /actuator/info`
Metadados.

```json
{
    "app": {
        "name": "auth-service",
        "description": "...",
        "version": "0.0.1-SNAPSHOT",
        "java-version": 17
    }
}
```

### `GET /actuator/metrics`
Lista de métricas.

### `GET /actuator/metrics/{name}`
Detalhes de métrica específica.

### `GET /actuator/prometheus`
**Texto puro** no formato Prometheus.

---

## 🔄 Endpoints de Resilience4j (FASE 9)

### `GET /actuator/circuitbreakers`
Lista todos os Circuit Breakers configurados.

**Response 200 OK:**
```json
{
    "circuitBreakers": ["alpha-vantage", "finnhub"]
}
```

### `GET /actuator/circuitbreakerevents`
Histórico de eventos.

**Response 200 OK:**
```json
{
    "circuitBreakerEvents": [
        {
            "circuitBreakerName": "alpha-vantage",
            "type": "STATE_TRANSITION",
            "creationTime": "2026-05-14T10:00:00Z",
            "stateTransition": "CLOSED_TO_OPEN"
        }
    ]
}
```

### `GET /actuator/circuitbreakerevents/{name}`
Eventos de um CB específico.

---

## 🌐 API Gateway (FASE 11 — NOVO)

### `GET /actuator/gateway/routes`
Lista todas as rotas configuradas.

**Response 200 OK:**
```json
[
    {
        "route_id": "auth-service",
        "uri": "lb://auth-service",
        "predicates": ["Path=/api/auth/**"],
        "filters": []
    },
    {
        "route_id": "notes-service",
        "uri": "lb://notes-service",
        "predicates": ["Path=/api/notes/**"],
        "filters": ["JwtAuthFilter"]
    }
]
```

### `POST /actuator/gateway/refresh`
Recarrega rotas do Config Server.

### `GET /actuator/gateway/globalfilters`
Lista filtros globais ativos.

---

## 🔄 Headers Propagados (FASE 11)

Todos os requests através do Gateway recebem estes headers ao chegar nos serviços internos:

| Header | Origem | Uso |
|---|---|---|
| `X-Correlation-ID` | Gerado pelo Gateway (ou propagado se já veio) | Rastreamento |
| `X-User-Id` | Extraído do JWT pelo Gateway | Identificação do usuário |
| `X-User-Email` | Extraído do JWT | Logs |
| `X-User-Role` | Extraído do JWT | Autorização interna |
| `X-Forwarded-For` | IP original do cliente | Rate limiting, audit |
| `X-Real-IP` | IP imediato | Rate limiting |

---

## 📦 Padrão de Erros (todos os serviços)

Formato consistente do `ErrorResponse`:

```json
{
    "status": 400,
    "error": "Bad Request",
    "message": "Erro de validação nos campos enviados",
    "path": "/api/auth/register",
    "timestamp": "2026-05-14T10:23:14.123",
    "validationErrors": {
        "email": "E-mail deve ser válido",
        "password": "Senha deve ter entre 8 e 100 caracteres"
    }
}
```

**Quando `validationErrors` é preenchido:**
- Apenas em 400 Bad Request de validação
- Em outros erros (401, 403, 404, 409, 500), é `null`

---

## 🚦 Status Codes Utilizados

| Code | Significado | Quando |
|---|---|---|
| `200` | OK | Operação bem-sucedida |
| `201` | Created | Recurso criado (POST) |
| `204` | No Content | Delete bem-sucedido |
| `400` | Bad Request | Validação falhou ou input inválido |
| `401` | Unauthorized | Não autenticado (sem token) |
| `403` | Forbidden | Autenticado mas sem permissão / Spring Security default |
| `404` | Not Found | Recurso não existe |
| `409` | Conflict | Conflito (ex: email duplicado) |
| `429` | Too Many Requests | Rate limit excedido (FASE 11) |
| `500` | Internal Server Error | Erro não tratado |
| `503` | Service Unavailable | Dependência externa fora do ar |

---

## 🔒 Endpoints Públicos vs Protegidos

### Públicos (sem JWT, mesmo com Gateway)
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/health`
- `GET /actuator/health/**`
- `GET /actuator/info`

### Protegidos (exigem JWT)
- `POST /api/auth/refresh` (mas usa refresh token, não access)
- `GET /api/auth/me`
- Tudo de `/api/notes/**`
- Tudo de `/api/stocks/**`

### Internos (apenas admin / restritos)
- `/actuator/env` (vaza configs)
- `/actuator/loggers` (permite mudar logs)
- `/actuator/heapdump` (perigo!)
- Em prod: todo `/actuator/**` exceto health/info deve exigir auth admin

---

## 📚 Swagger UI

Cada serviço tem documentação interativa:

- Auth: http://localhost:8081/swagger-ui.html
- Notes: http://localhost:8082/swagger-ui.html
- Stock: http://localhost:8083/swagger-ui.html

OpenAPI spec em formato JSON:
- http://localhost:8081/v3/api-docs

**Após FASE 11:**
- Gateway pode agregar todas as docs em http://localhost:8080/swagger-ui.html

---

**Próximo:** `10-decisoes-tecnicas.md` para justificativas técnicas.
