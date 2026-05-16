# FinPulse — Visão Geral do Projeto

> **Documento de contexto para Claude Code.** Lê este arquivo primeiro antes de qualquer outro.

---

## 🎯 O que é o FinPulse

Plataforma SaaS de monitoramento de mercado financeiro com ferramentas de produtividade pessoal. Arquitetura baseada em microsserviços Spring Boot.

**Repositório:** https://github.com/PedroQueiroz1/finpulse
**Diretório raiz do projeto:** `C:\finpulse`

---

## 📦 Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 (Temurin) |
| Framework | Spring Boot 3.4.13 |
| Cloud | Spring Cloud 2024.0.1 |
| Build | Maven 3.9+ |
| Banco relacional | PostgreSQL 16 |
| Banco documental | MongoDB 7 |
| Cache | Redis 7 (Memurai no Windows) |
| Auth | JWT com BCrypt |
| Docs | OpenAPI 3 / Swagger UI |
| Testes | JUnit 5 + AssertJ + Testcontainers 1.20.4 |
| Coverage | Jacoco 0.8.12 |
| Containerização | Docker 28.x |

---

## 🏗️ Microsserviços Existentes

| Serviço | Porta | Banco | Status |
|---|---|---|---|
| `config-server` | 8888 | — | ✅ Operacional |
| `eureka-server` | 8761 | — | ✅ Operacional |
| `auth-service` | 8081 | PostgreSQL | ✅ **Com observability completa** |
| `notes-service` | 8082 | MongoDB | ⚠️ Sem observability |
| `stock-service` | 8083 | Redis (cache) | ⚠️ Sem observability |
| `api-gateway` | 8080 | — | 🔜 A ser criado na FASE 11 |

---

## 📜 Histórico Resumido (FASES 1-7 concluídas)

### FASE 1-2 — Infraestrutura base
- Monorepo Maven multi-módulo
- Config Server (modo nativo, classpath)
- Eureka Server (service discovery)

### FASE 3 — auth-service
- JWT (access 15min + refresh 7 dias)
- BCrypt cost 12
- PostgreSQL + Flyway
- 5 Roles: USER, PREMIUM, ADMIN, SUPER_ADMIN, GUEST
- Endpoints: register, login, refresh, me, health

### FASE 4 — notes-service
- CRUD de notas
- MongoDB
- Agrupamento por tags
- Validação de autoria via JWT (extraído do header)

### FASE 5 — stock-service
- Integração Alpha Vantage + Finnhub (failover)
- Redis cache (60s cotação, 24h dados de empresa)
- Padrões: Strategy + Adapter + Facade

### FASE 6 — Documentação
- OpenAPI 3 / Swagger UI em todos os serviços

### FASE 7 — Observability (apenas auth-service)
- **7.1** Spring Boot Actuator + Prometheus
- **7.2** Perfis dev/test/prod (estratégia híbrida: Config Server pra dev/prod, local pra test)
- **7.3** Jacoco com gate 70% line / 60% branch
- **7.4** Testcontainers (PostgreSQL real em testes)
- **7.5** Logback + Correlation ID via MDC

**Métricas do auth-service:**
- 77% cobertura de linhas
- 70% cobertura de branches
- 27 testes de integração + 6 unidade

---

## 🎯 Escopo dos Specs (FASE 8-11)

### FASE 8 — Replicar Observability
Aplicar tudo que está no auth-service nos demais serviços:
- **8.1** notes-service (com MongoDBContainer)
- **8.2** stock-service (com PostgreSQLContainer + WireMock pra APIs externas)

### FASE 9 — Resilience4j
- Circuit Breaker (stock-service chamando APIs externas)
- Retry com backoff exponencial
- Bulkhead (isolamento de threads)
- Rate Limiter
- TimeLimiter

### FASE 10 — Spring AOP
- Aspect de logging automático em controllers
- Aspect de métricas customizadas (`@Timed`, `@Counted`)
- Aspect de auditoria (quem chamou o quê e quando)

### FASE 11 — API Gateway
- Spring Cloud Gateway (porta 8080)
- Roteamento baseado em path
- Filtros globais (CORS, rate limiting, correlation ID propagation)
- Integração com Eureka pra service discovery
- Validação JWT no gateway (single point of auth)

---

## 🚫 Fora de Escopo (não fazer agora)

- Frontend (Angular/React) — FASE 13-14
- Mensageria (RabbitMQ/Kafka) — FASE 12
- Dockerização completa — FASE 15
- CI/CD — FASE 16
- Kubernetes — FASE 17
- Grafana dashboard — FASE 18

---

## 🧭 Princípios Norteadores

Sempre que tomar decisão técnica, seguir estes princípios:

1. **NÃO usar Lombok** — preferir Records do Java + getters/setters explícitos
2. **BigDecimal obrigatório para dinheiro** — nunca double
3. **Testes sempre em PostgreSQL/MongoDB reais via Testcontainers** — nunca H2 ou Fongo
4. **Config Server pra dev/prod, local pra test** — testes isolados rodam em qualquer CI
5. **Observability primeiro** — todo serviço novo deve ter Actuator antes de qualquer feature
6. **JWT secret nunca hardcoded em prod** — sempre `${JWT_SECRET}` com fallback nulo
7. **Correlation ID em todos os serviços** — rastreamento distribuído
8. **Commit por feature pequena** — não acumular muito em um único commit
9. **Cobertura mínima 70% line / 60% branch** — gate automático no Maven

---

## 📂 Estrutura de Diretórios

```
C:\finpulse\
├── backend/
│   ├── config-server/
│   │   └── src/main/resources/configs/    ← YAMLs centralizados
│   ├── eureka-server/
│   ├── auth-service/                       ← ✅ FASE 7 completa
│   ├── notes-service/                      ← 🚧 FASE 8.1
│   ├── stock-service/                      ← 🚧 FASE 8.2
│   └── api-gateway/                        ← 🚧 FASE 11 (criar)
├── .specs/                                 ← Estes documentos
├── README.md
└── .gitignore
```

---

## 📚 Documentos Relacionados

- `01-requisitos-tecnicos.md` — requisitos técnicos por fase
- `05-arquitetura.md` — diagramas e fluxos
- `06-plano-de-implementacao.md` — passo a passo de cada fase
- `07-plano-de-testes.md` — estratégia de testes
- `08-modelo-de-dados.md` — entidades e collections
- `09-contrato-api.md` — endpoints novos e modificados
- `10-decisoes-tecnicas.md` — justificativas técnicas
- `11-guia-de-execucao.md` — comandos prontos
- `12-tarefas-atuais.md` — checklist executável

---

**Próximo passo:** Lê `01-requisitos-tecnicos.md` para entender os requisitos específicos de cada fase.
