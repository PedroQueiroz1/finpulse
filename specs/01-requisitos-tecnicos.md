# Requisitos Técnicos por Fase

> **Pré-requisitos antes de qualquer implementação:** Docker rodando, Config Server e Eureka up, banco MongoDB e Redis disponíveis.

---

## 🎯 FASE 8 — Replicar Observability

### FASE 8.1 — notes-service

#### Requisitos Funcionais
- [ ] Endpoint `/actuator/health` retorna status com componente MongoDB
- [ ] Endpoint `/actuator/prometheus` retorna métricas no formato Prometheus
- [ ] Endpoint `/actuator/info` retorna metadados (nome, descrição, versão, java)
- [ ] Probes `/actuator/health/liveness` e `/actuator/health/readiness` ativos
- [ ] Correlation ID gerado/propagado em todas as requisições
- [ ] Header `X-Correlation-ID` presente em todas as respostas HTTP

#### Requisitos de Qualidade
- [ ] Cobertura de testes: mínimo 70% line / 60% branch
- [ ] Mínimo 20 testes de integração cobrindo NotesController
- [ ] Testes rodam contra MongoDB real via Testcontainers
- [ ] Build falha automaticamente se cobertura cair abaixo do mínimo (gate Jacoco)
- [ ] Tempo de build com reuse de container: < 60 segundos

#### Requisitos de Configuração
- [ ] `application.yml` local enxuto (apenas bootstrap)
- [ ] Configurações de dev/prod no Config Server
- [ ] Configuração de test local (sem dependência de Config Server)
- [ ] Logback configurado com perfis (dev colorido, prod JSON)
- [ ] Variáveis de ambiente em prod: `MONGODB_URI`, `MONGODB_DATABASE`, `JWT_SECRET`, `EUREKA_URL`

---

### FASE 8.2 — stock-service

#### Requisitos Funcionais
- [ ] Mesmos requisitos de Actuator/Prometheus/Correlation ID da 8.1
- [ ] Health check inclui status do Redis
- [ ] Health check inclui status dos providers externos (Alpha Vantage, Finnhub) — opcional

#### Requisitos de Qualidade
- [ ] Cobertura de testes: mínimo 65% line / 55% branch (mais complexo, mais tolerante)
- [ ] Testes de integração usando **WireMock** pra mockar APIs externas
- [ ] Testes do cache Redis com Testcontainers (RedisContainer)
- [ ] Mínimo 15 testes de integração cobrindo StockController

#### Requisitos de Configuração
- [ ] Mesma estratégia de perfis da 8.1
- [ ] Variáveis de ambiente em prod: `REDIS_URL`, `ALPHA_VANTAGE_API_KEY`, `FINNHUB_API_KEY`, `JWT_SECRET`, `EUREKA_URL`

---

## 🎯 FASE 9 — Resilience4j

### Requisitos Funcionais
- [ ] Circuit Breaker ativo nas chamadas a Alpha Vantage e Finnhub
- [ ] Retry com backoff exponencial (3 tentativas, 100ms, 200ms, 400ms)
- [ ] Bulkhead isolando threads de chamadas externas
- [ ] Rate Limiter no stock-service: máx 100 req/s por endpoint
- [ ] TimeLimiter: timeout de 3s em todas as chamadas externas
- [ ] Fallbacks definidos para cada padrão (retorna cache antigo ou erro 503)

### Requisitos de Observability
- [ ] Métricas do Circuit Breaker expostas em `/actuator/prometheus`
  - `resilience4j_circuitbreaker_state` (estados: closed, open, half-open)
  - `resilience4j_circuitbreaker_calls_total`
  - `resilience4j_retry_calls_total`
- [ ] Endpoint `/actuator/circuitbreakers` listando todos os CBs
- [ ] Eventos de mudança de estado logados (com correlation ID)

### Requisitos de Configuração
- [ ] Configurações no Config Server (`stock-service.yml`)
- [ ] Threshold de falha: 50% das últimas 10 chamadas
- [ ] Tempo em open state: 30 segundos
- [ ] Permitir 3 chamadas em half-open antes de decidir

### Critérios Mensuráveis
- [ ] Se Alpha Vantage retornar 5 erros 500 seguidos, o CB abre (estado OPEN)
- [ ] Após 30s, CB transiciona para HALF_OPEN automaticamente
- [ ] Em HALF_OPEN, próximas 3 chamadas decidem se volta pra OPEN ou CLOSED
- [ ] Fallback retorna o último valor cacheado se o CB estiver OPEN

---

## 🎯 FASE 10 — Spring AOP

### Requisitos Funcionais
- [ ] Aspect `LoggingAspect` em todos os controllers (qualquer serviço)
  - Loga entrada do método com parâmetros
  - Loga saída do método com tempo de execução
  - Loga exceções com stack trace
- [ ] Aspect `MetricsAspect` registrando métricas customizadas via Micrometer
  - Counter de chamadas por método
  - Timer de duração por método
- [ ] Annotation customizada `@Audited` para auditoria
  - Salva no MongoDB: user, método, parâmetros (sanitizados), timestamp, resultado
- [ ] Annotation customizada `@Timed` (wrapper do Micrometer)

### Requisitos Não-Funcionais
- [ ] Overhead dos aspects < 5ms por requisição
- [ ] Logs de aspect contêm correlation ID (já está no MDC)
- [ ] Senhas e tokens **nunca** logados (sanitização obrigatória)

### Requisitos de Configuração
- [ ] Aspects ativados apenas em pacotes `com.finpulse.*.controller` e `com.finpulse.*.service`
- [ ] Configuração no Config Server pra ativar/desativar aspects por perfil
- [ ] Em prod: apenas `MetricsAspect` (sem `LoggingAspect` verboso)

---

## 🎯 FASE 11 — API Gateway

### Requisitos Funcionais
- [ ] Novo serviço `api-gateway` na porta 8080
- [ ] Roteamento baseado em path:
  - `/api/auth/**` → auth-service
  - `/api/notes/**` → notes-service
  - `/api/stocks/**` → stock-service
- [ ] Service discovery via Eureka (rotear por nome lógico, não por URL)
- [ ] Filtro global de CORS configurável
- [ ] Filtro global de Correlation ID (gera/propaga UUID)
- [ ] Rate Limiter global: 1000 req/min por IP
- [ ] Validação JWT no Gateway (offload dos serviços internos)
- [ ] Endpoint `/actuator/gateway/routes` lista todas as rotas configuradas

### Requisitos de Segurança
- [ ] JWT validado **antes** de chegar nos serviços internos
- [ ] Serviços internos confiam no header `X-User-Id` injetado pelo Gateway
- [ ] Endpoints públicos (login, register) bypassam validação JWT
- [ ] HTTPS obrigatório em prod (HTTP só em dev)

### Requisitos de Observability
- [ ] Métricas do Gateway expostas em `/actuator/prometheus`
- [ ] Latência por rota (p50, p95, p99)
- [ ] Contador de requisições por status code
- [ ] Circuit Breaker nos downstream services (Resilience4j)

### Requisitos de Configuração
- [ ] Configuração 100% no Config Server (`api-gateway.yml`)
- [ ] Suporte aos 3 perfis (dev/test/prod)
- [ ] Em prod: HTTPS via cert configurado por env vars

---

## 🧪 Requisitos Transversais (todas as fases)

### Build & Testes
- Toda PR (mental, antes de commit) deve passar `mvn clean install`
- Coverage validado pelo Jacoco gate
- Testes de integração com Testcontainers sempre que tocar em banco
- WireMock sempre que houver dependência externa

### Documentação
- Toda mudança em endpoint deve atualizar Swagger (annotations OpenAPI)
- Decisões técnicas importantes documentadas em `10-decisoes-tecnicas.md`
- README do projeto atualizado ao final de cada FASE

### Git
- Commits descritivos seguindo convenção: `feat(escopo): mensagem`
- Tipos: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`
- Escopos: `auth`, `notes`, `stock`, `gateway`, `observability`, `resilience`, `aop`

---

**Próximo:** `05-arquitetura.md` para entender como tudo se encaixa.
