# Modelo de Dados

> Entidades, collections e schemas usados nas FASES 8-11. Documento de referência para Claude Code não chutar nomes de campos.

---

## 🗄️ PostgreSQL (auth-service)

### Tabela `users`

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

**Entidade JPA:** `com.finpulse.auth.entity.User`

**Roles válidos** (enum `com.finpulse.auth.enums.Role`):
- `USER` — padrão
- `PREMIUM` — pago
- `ADMIN` — admin de cliente
- `SUPER_ADMIN` — admin do sistema
- `GUEST` — convidado limitado

**Status:** ✅ Já existe (FASE 3)

---

## 🗄️ MongoDB (notes-service)

### Collection `notes`

```javascript
{
    "_id": ObjectId("..."),
    "userId": "uuid-do-usuario-do-postgres",  // FK lógica pro PG
    "title": "Minha nota importante",
    "content": "Conteúdo da nota em markdown...",
    "tags": ["trabalho", "urgente"],
    "deleted": false,                           // soft delete
    "createdAt": ISODate("2026-05-14T10:00:00Z"),
    "updatedAt": ISODate("2026-05-14T10:00:00Z")
}
```

**Indexes esperados:**
```javascript
db.notes.createIndex({ "userId": 1 })
db.notes.createIndex({ "userId": 1, "deleted": 1 })
db.notes.createIndex({ "tags": 1 })
db.notes.createIndex({ "title": "text", "content": "text" })  // full-text search
```

**Document Java:** `com.finpulse.notes.entity.Note`

**Status:** ✅ Já existe (FASE 4)

**ATENÇÃO:** Os campos exatos podem variar — Claude Code deve **inspecionar** o arquivo `Note.java` antes de assumir nomes.

---

## 🗄️ PostgreSQL (stock-service) — se aplicável

### Tabela `companies` (cache de longa duração)

```sql
CREATE TABLE companies (
    symbol VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255),
    sector VARCHAR(100),
    industry VARCHAR(100),
    description TEXT,
    market_cap BIGINT,
    last_fetched_at TIMESTAMP,
    source VARCHAR(20)  -- 'alpha-vantage' ou 'finnhub'
);
```

**Status:** ⚠️ Verificar — pode não existir ainda. Claude Code deve inspecionar.

---

## 🗄️ Redis (stock-service)

### Estrutura de chaves

| Padrão | TTL | Uso |
|---|---|---|
| `stock:quote:{symbol}` | 60s | Cotação atual |
| `stock:company:{symbol}` | 24h | Dados da empresa |
| `stock:provider:status` | 5min | Health dos providers |
| `ratelimit:gw:{ip}` | 1min | Rate limit do Gateway (FASE 11) |

**Exemplo de valor (JSON serializado):**
```json
{
  "symbol": "AAPL",
  "price": 182.34,
  "currency": "USD",
  "changePercent": 0.45,
  "timestamp": "2026-05-14T10:23:14Z",
  "provider": "alpha-vantage"
}
```

**Status:** ✅ Estrutura definida (FASE 5)

---

## 🗄️ MongoDB (Auditoria - FASE 10)

### Collection `audit_logs`

A criar na FASE 10:

```javascript
{
    "_id": ObjectId("..."),
    "userId": "uuid-do-usuario",
    "action": "USER_LOGIN",                      // de @Audited(action="...")
    "method": "com.finpulse.auth.service.AuthService.login",
    "parameters": {                              // sanitizado!
        "email": "pedro@finpulse.com"
        // password OMITIDO
    },
    "result": "SUCCESS",                         // SUCCESS | FAILURE
    "errorMessage": null,                        // preenchido se FAILURE
    "correlationId": "a3f9b2c1-...",
    "ipAddress": "192.168.1.10",
    "userAgent": "Mozilla/5.0...",
    "timestamp": ISODate("2026-05-14T10:23:14Z"),
    "durationMs": 234
}
```

**Indexes:**
```javascript
db.audit_logs.createIndex({ "userId": 1, "timestamp": -1 })
db.audit_logs.createIndex({ "action": 1, "timestamp": -1 })
db.audit_logs.createIndex({ "correlationId": 1 })
db.audit_logs.createIndex({ "timestamp": -1 })
db.audit_logs.createIndex({ "timestamp": 1 }, { expireAfterSeconds: 7776000 })  // TTL 90 dias
```

**Sanitização obrigatória:** campos com nomes `password`, `token`, `secret`, `creditCard`, `cvv` → substituir por `"***"`.

---

## 📦 DTOs Compartilhados

### `ErrorResponse` (padrão para todos os endpoints de erro)

```java
public record ErrorResponse(
    int status,                             // HTTP status code
    String error,                           // "Bad Request", "Unauthorized", etc.
    String message,                         // mensagem amigável
    String path,                            // URI da requisição
    LocalDateTime timestamp,                // timestamp do erro
    Map<String, String> validationErrors    // se @Valid falhar
) {}
```

**Localização:** `com.finpulse.{service}.dto.ErrorResponse` em cada serviço

**Status:** ✅ Existe no auth-service. Replicar nos outros.

---

## 🔐 Estrutura do JWT

### Payload (claims)

```json
{
    "sub": "pedro@finpulse.com",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "role": "USER",
    "iat": 1715680000,
    "exp": 1715680900,
    "iss": "finpulse-auth",
    "jti": "uuid-unico-do-token"
}
```

**Algoritmo:** HS256
**Secret:** mínimo 256 bits (32 caracteres)

**Headers que o Gateway injeta (FASE 11) após validar JWT:**
- `X-User-Id`: userId do JWT
- `X-User-Email`: subject do JWT
- `X-User-Role`: role do JWT
- `X-Correlation-ID`: já gerado pelo CorrelationIdGlobalFilter

---

## 🌐 Variáveis de Ambiente (Produção)

### auth-service
| Variável | Exemplo |
|---|---|
| `DB_URL` | `jdbc:postgresql://prod-db:5432/finpulse_auth` |
| `DB_USER` | `finpulse_prod` |
| `DB_PASSWORD` | `senha_segura_aqui` |
| `JWT_SECRET` | (string 256+ bits) |
| `EUREKA_URL` | `http://eureka:8761/eureka/` |

### notes-service
| Variável | Exemplo |
|---|---|
| `MONGODB_URI` | `mongodb://prod-mongo:27017/finpulse_notes` |
| `MONGODB_DATABASE` | `finpulse_notes` |
| `JWT_SECRET` | (mesmo do auth) |
| `EUREKA_URL` | `http://eureka:8761/eureka/` |

### stock-service
| Variável | Exemplo |
|---|---|
| `REDIS_URL` | `redis://prod-redis:6379` |
| `REDIS_PASSWORD` | (se aplicável) |
| `ALPHA_VANTAGE_API_KEY` | (chave da API) |
| `FINNHUB_API_KEY` | (chave da API) |
| `JWT_SECRET` | (mesmo do auth) |
| `EUREKA_URL` | `http://eureka:8761/eureka/` |

### api-gateway (FASE 11)
| Variável | Exemplo |
|---|---|
| `JWT_SECRET` | (mesmo do auth) |
| `EUREKA_URL` | `http://eureka:8761/eureka/` |
| `REDIS_URL` | (se usar rate limiter Redis-backed) |
| `CORS_ALLOWED_ORIGINS` | `https://app.finpulse.com,https://admin.finpulse.com` |

---

## 📐 Convenções de Nomenclatura

### Tabelas (PostgreSQL)
- `snake_case`
- Plural (`users`, `companies`)
- Colunas em `snake_case` (`created_at`, `user_id`)

### Collections (MongoDB)
- `snake_case`
- Plural (`notes`, `audit_logs`)
- Campos em `camelCase` (não snake_case — convenção Mongo)

### Classes Java
- Entities/Documents: singular (`User`, `Note`, `AuditLog`)
- DTOs: sufixo Request/Response (`RegisterRequest`, `AuthResponse`)
- Repositories: `{Entity}Repository`
- Services: `{Entity}Service`
- Controllers: `{Entity}Controller`

### Enums Java
- `PascalCase` no nome
- `SCREAMING_SNAKE_CASE` nos valores

```java
public enum Role {
    USER, PREMIUM, ADMIN, SUPER_ADMIN, GUEST
}
```

---

## 🔄 Migrações de Schema

### auth-service (Flyway)

Localização: `backend/auth-service/src/main/resources/db/migration/`

Convenção: `V{numero}__{descricao}.sql`

Exemplos:
- `V1__create_users_table.sql`
- `V2__add_index_role.sql`

**FASE 8.2 pode adicionar:**
- `V1__create_companies_table.sql` (se for criar tabela em stock-service)

### notes-service (sem Flyway — MongoDB cria sob demanda)

Mas pode usar **Mongock** se quiser versionamento explícito. Decisão: **não usar Mongock por enquanto** — manter simples.

Indexes devem ser criados via:
- `auto-index-creation: true` no application.yml (dev/test)
- Scripts manuais em prod (mais controle)

---

## 📊 Mapeamento Entity ↔ DTO

### Exemplo: User → UserResponse

```
User (Entity)              UserResponse (DTO)
─────────────              ─────────────────
id: UUID         ────►     id: UUID
name: String     ────►     name: String
email: String    ────►     email: String
passwordHash     ─X─       (omitido!)
role: Role       ────►     role: Role
active: boolean  ────►     active: boolean
createdAt        ─X─       (omitido)
updatedAt        ─X─       (omitido)
```

Mapper usado: **MapStruct** (gera código em build time)

---

## 🔍 Queries Importantes (referência)

### auth-service

```java
// Já existem em UserRepository
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
List<User> findByRole(Role role);
List<User> findByActiveTrue();

// Native query (estatísticas)
@Query(value = """
    SELECT u.role, COUNT(*) as total,
           SUM(CASE WHEN u.active THEN 1 ELSE 0 END) as active_count
    FROM users u GROUP BY u.role
    """, nativeQuery = true)
List<Object[]> getUserStatsByRole();
```

### notes-service (esperado)

```java
List<Note> findByUserIdAndDeletedFalse(UUID userId);
Optional<Note> findByIdAndUserId(String id, UUID userId);
List<Note> findByUserIdAndTagsContaining(UUID userId, String tag);

@Query("{ '$text': { '$search': ?0 }, 'userId': ?1 }")
List<Note> searchByText(String text, UUID userId);
```

---

**Próximo:** `09-contrato-api.md` para endpoints completos.
