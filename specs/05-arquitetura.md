# Arquitetura

> Visão arquitetural do FinPulse após conclusão das FASES 8-11.

---

## 🏗️ Arquitetura Atual (antes da FASE 11)

```
                      ┌─────────────────────┐
                      │  Cliente (HTTP)     │
                      └──────────┬──────────┘
                                 │
                  ┌──────────────┼──────────────┐
                  │              │              │
                  ▼              ▼              ▼
            ┌──────────┐   ┌──────────┐   ┌──────────┐
            │  Auth    │   │  Notes   │   │  Stock   │
            │  :8081   │   │  :8082   │   │  :8083   │
            └────┬─────┘   └────┬─────┘   └────┬─────┘
                 │              │              │
                 │         ┌────┴────────┐     │
                 │         │             │     │
                 ▼         ▼             ▼     ▼
           ┌─────────┐  ┌─────────┐  ┌─────────┐
           │Postgres │  │ MongoDB │  │  Redis  │
           └─────────┘  └─────────┘  └─────────┘

   Cross-cutting:
   ┌─────────────────┐    ┌─────────────────┐
   │ Config Server   │    │ Eureka Server   │
   │     :8888       │    │     :8761       │
   └─────────────────┘    └─────────────────┘
```

**Problemas atuais:**
- Cliente precisa conhecer URL de cada serviço
- Cada serviço valida JWT individualmente (duplicação)
- Sem rate limiting global
- Sem ponto único de observability

---

## 🎯 Arquitetura Alvo (após FASE 11)

```
                      ┌─────────────────────┐
                      │  Cliente (HTTPS)    │
                      └──────────┬──────────┘
                                 │
                                 ▼
                      ┌─────────────────────┐
                      │   API Gateway       │
                      │     :8080           │
                      │                     │
                      │  - CORS             │
                      │  - Correlation ID   │
                      │  - JWT validation   │
                      │  - Rate Limiting    │
                      │  - Circuit Breaker  │
                      └──────────┬──────────┘
                                 │
                  ┌──────────────┼──────────────┐
                  │              │              │
                  ▼              ▼              ▼
            ┌──────────┐   ┌──────────┐   ┌──────────┐
            │  Auth    │   │  Notes   │   │  Stock   │
            │  :8081   │   │  :8082   │   │  :8083   │
            │          │   │          │   │          │
            │ Actuator │   │ Actuator │   │ Actuator │
            │  +AOP    │   │  +AOP    │   │ +CB +AOP │
            └────┬─────┘   └────┬─────┘   └────┬─────┘
                 │              │              │
                 ▼              ▼              ▼
           ┌─────────┐  ┌─────────┐  ┌─────────┐
           │Postgres │  │ MongoDB │  │  Redis  │
           └─────────┘  └─────────┘  └─────────┘
                                         │
                                         ▼
                                  ┌─────────────┐
                                  │ Alpha Vant. │
                                  │  + Finnhub  │
                                  └─────────────┘
```

---

## 🔄 Fluxo de Requisição (após FASE 11)

### Exemplo: cliente busca cotação de AAPL

```
1. Cliente envia:
   GET https://gateway.finpulse.com/api/stocks/AAPL/quote
   Authorization: Bearer eyJ...
   ↓
2. API Gateway recebe:
   a) Adiciona Correlation ID (X-Correlation-ID: a3f9b2c1...)
   b) Aplica CORS
   c) Verifica rate limit (1000 req/min/IP)
   d) Valida JWT → extrai userId
   e) Resolve "stock-service" via Eureka → encontra http://10.0.0.3:8083
   f) Encaminha requisição adicionando headers:
      - X-Correlation-ID: a3f9b2c1...
      - X-User-Id: 7e9f01...
   ↓
3. Stock Service recebe:
   a) CorrelationIdFilter detecta header e reusa
   b) LoggingAspect loga entrada do método
   c) MetricsAspect inicia timer
   d) Tenta buscar cache Redis (chave: stock:AAPL:quote)
      → Cache MISS
   e) Circuit Breaker chama Alpha Vantage
      → Sucesso (200 OK em 234ms)
   f) Salva resultado no Redis (TTL 60s)
   g) Retorna 200 OK
   ↓
4. Gateway propaga resposta com:
   X-Correlation-ID: a3f9b2c1...
   Body: {"symbol":"AAPL","price":182.34}
```

---

## 🧩 Componentes Detalhados

### 1. API Gateway (FASE 11)

**Stack:** Spring Cloud Gateway (reactive, baseado em Netty)

**Responsabilidades:**
- Roteamento por path para serviços internos
- Validação JWT (offload dos serviços)
- Rate limiting (Redis-backed)
- CORS centralizado
- Correlation ID propagation
- Circuit Breaker pra serviços downstream

**Filtros configurados:**

| Ordem | Filtro | Função |
|---|---|---|
| 1 | `CorrelationIdGlobalFilter` | Gera/propaga UUID |
| 2 | `CorsFilter` | Headers CORS |
| 3 | `RateLimiterFilter` | Throttling por IP |
| 4 | `JwtAuthFilter` | Valida JWT, extrai userId |
| 5 | `LoggingFilter` | Log de cada requisição |
| 6 | `RoutingFilter` | Encaminha pro downstream |

---

### 2. Observability Pattern (FASES 8 + 9)

**Cada microsserviço implementa:**

```
┌────────────────────────────────────────────────┐
│           Microsserviço Spring Boot            │
│                                                │
│  ┌──────────────────────────────────────────┐  │
│  │  Camada Web (Controller)                 │  │
│  │  + CorrelationIdFilter (FASE 7.5)        │  │
│  │  + LoggingAspect (FASE 10)               │  │
│  │  + MetricsAspect (FASE 10)               │  │
│  └──────────────────────────────────────────┘  │
│                    │                           │
│                    ▼                           │
│  ┌──────────────────────────────────────────┐  │
│  │  Camada Service (Lógica de Negócio)      │  │
│  │  + @Audited (FASE 10)                    │  │
│  │  + @Timed (FASE 10)                      │  │
│  └──────────────────────────────────────────┘  │
│                    │                           │
│                    ▼                           │
│  ┌──────────────────────────────────────────┐  │
│  │  Camada Integração Externa               │  │
│  │  + Circuit Breaker (FASE 9)              │  │
│  │  + Retry (FASE 9)                        │  │
│  │  + Bulkhead (FASE 9)                     │  │
│  │  + TimeLimiter (FASE 9)                  │  │
│  └──────────────────────────────────────────┘  │
│                                                │
│  Endpoints expostos:                           │
│  - /actuator/health                            │
│  - /actuator/info                              │
│  - /actuator/metrics                           │
│  - /actuator/prometheus  ← scraped externamente│
└────────────────────────────────────────────────┘
```

---

### 3. Fluxo de Correlation ID Entre Serviços

```
Cliente                Gateway              Auth-Service
  │                       │                       │
  │  POST /api/auth/login │                       │
  ├──────────────────────►│                       │
  │  (sem X-Correlation)  │                       │
  │                       │ Gera UUID = abc123    │
  │                       │ MDC.put("cid", abc)   │
  │                       │                       │
  │                       │ Forward + Header      │
  │                       │ X-Correlation: abc123 │
  │                       ├──────────────────────►│
  │                       │                       │ Filter detecta header
  │                       │                       │ Reusa "abc123"
  │                       │                       │ MDC.put("cid", abc)
  │                       │                       │
  │                       │                       │ Log: [cid=abc123]
  │                       │                       │ "Login attempt"
  │                       │                       │
  │                       │                       │ Retorna 200 OK
  │                       │◄──────────────────────┤
  │                       │ Header: X-Corr: abc   │
  │                       │                       │
  │ Response 200 OK       │                       │
  │◄──────────────────────┤                       │
  │ X-Correlation: abc123 │                       │
  │                       │                       │
```

**Resultado:** cliente pode usar o `X-Correlation-ID` recebido pra abrir ticket de suporte. Suporte filtra logs por esse ID e vê a jornada inteira em todos os serviços.

---

## 🗄️ Decisões Arquiteturais

### Por que API Gateway na FASE 11 e não antes?

**Pros do Gateway:**
- Ponto único de auth, rate limit, observability
- Cliente não precisa conhecer topologia interna
- Permite mudar microsserviços sem afetar cliente

**Por que não fizemos antes:**
- Sem Gateway, cada serviço aprende a se proteger sozinho (didático)
- FASE 8 e 9 ensinam observability e resilience nos serviços primeiro
- Gateway sem serviços robustos seria fachada falsa

### Por que Resilience4j antes de AOP?

- Circuit Breaker é caso de uso CONCRETO (stock-service chamando APIs externas)
- AOP é abstração que serve PRA TUDO — perigoso aplicar sem entender uso real
- Ordem permite aplicar AOP **junto** com Resilience4j (ambos via aspects)

### Por que Spring Cloud Gateway e não Zuul/Nginx?

**Comparação:**

| Critério | Spring Cloud Gateway | Zuul | Nginx |
|---|---|---|---|
| Linguagem | Java (Spring) | Java (Netflix) | C |
| Modelo | Reativo (Netty) | Blocking | Não-blocking |
| Integração Spring | ✅ Nativa | ⚠️ Legacy | ❌ Externa |
| Performance | Alta | Média | Muito alta |
| Curva de aprendizado | Média | Baixa | Alta |

**Decisão:** Spring Cloud Gateway pela integração nativa com Eureka e Spring Boot.

---

## 🧪 Estratégia de Testes

```
                Pirâmide de Testes
                 ╱─────────────╲
                ╱  E2E (manual) ╲
               ╱─────────────────╲
              ╱  Integration       ╲
             ╱  (Testcontainers)    ╲
            ╱─────────────────────────╲
           ╱       Slice Tests          ╲
          ╱     (@WebMvcTest, etc)       ╲
         ╱─────────────────────────────────╲
        ╱            Unit Tests              ╲
       ╱      (JUnit + Mockito puro)          ╲
      ─────────────────────────────────────────
```

| Camada | % do total | Velocidade | Fidelidade |
|---|---|---|---|
| Unit | ~60% | ⚡ ms | Baixa |
| Slice | ~25% | 🚗 segundos | Média |
| Integration | ~15% | 🐢 dezenas de seg | Alta |
| E2E | manual | minutos | Total |

---

## 📡 Métricas Coletadas (Prometheus)

### Por todos os serviços
- `jvm_memory_used_bytes`
- `jvm_gc_pause_seconds`
- `http_server_requests_seconds` (com p50, p95, p99)
- `application_ready_time_seconds`

### Específicas da FASE 9
- `resilience4j_circuitbreaker_state`
- `resilience4j_circuitbreaker_calls_total`
- `resilience4j_retry_calls_total`
- `resilience4j_ratelimiter_available_permissions`

### Específicas do Gateway (FASE 11)
- `spring_cloud_gateway_requests_seconds`
- `spring_cloud_gateway_routes_total`

---

**Próximo:** `06-plano-de-implementacao.md` para o passo a passo de cada fase.
