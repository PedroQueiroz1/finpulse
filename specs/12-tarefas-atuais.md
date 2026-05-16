# Tarefas Atuais

> Checklist executável das FASES 8-11. Marque conforme completar.
> **Comece sempre pelo topo. Não pule passos.**

---

## 🎯 FASE 8.1 — Observability no notes-service

> **Tempo estimado:** 4-6 horas
> **Pré-requisitos:** auth-service com FASE 7 completa (referência)

### Bloco 1: Dependências e Build

- [ ] **8.1.1** — Abrir `backend/notes-service/pom.xml`
- [ ] **8.1.2** — Adicionar dependência `spring-boot-starter-actuator`
- [ ] **8.1.3** — Adicionar dependência `micrometer-registry-prometheus` (runtime)
- [ ] **8.1.4** — Adicionar dependência `prometheus-metrics-exposition-formats` (runtime)
- [ ] **8.1.5** — Adicionar `testcontainers-bom` versão 1.20.4
- [ ] **8.1.6** — Adicionar `testcontainers` (test scope)
- [ ] **8.1.7** — Adicionar `testcontainers/junit-jupiter` (test)
- [ ] **8.1.8** — Adicionar `testcontainers/mongodb` (test) ← **diferença vs auth!**
- [ ] **8.1.9** — Adicionar `spring-boot-testcontainers` (test)
- [ ] **8.1.10** — Adicionar plugin Jacoco 0.8.12 com 3 execuções (prepare-agent, report, check)
- [ ] **8.1.11** — Validar build: `mvn clean install -DskipTests` → deve retornar BUILD SUCCESS

### Bloco 2: Configurações YAML

- [ ] **8.1.12** — Reduzir `notes-service/src/main/resources/application.yml` pra apenas bootstrap (8 linhas)
- [ ] **8.1.13** — Criar `notes-service/src/main/resources/application-test.yml` (perfil test, sem Config Server, sem Eureka, MongoDB injection)
- [ ] **8.1.14** — Substituir conteúdo de `config-server/src/main/resources/configs/notes-service.yml` (base comum)
- [ ] **8.1.15** — Criar `config-server/src/main/resources/configs/notes-service-dev.yml`
- [ ] **8.1.16** — Criar `config-server/src/main/resources/configs/notes-service-prod.yml`
- [ ] **8.1.17** — Reiniciar Config Server (Ctrl+C e `mvn spring-boot:run` de novo)

### Bloco 3: Filter e Logback

- [ ] **8.1.18** — Criar pasta `notes-service/src/main/java/com/finpulse/notes/filter/`
- [ ] **8.1.19** — Criar `CorrelationIdFilter.java` (copiar do auth, ajustar pacote)
- [ ] **8.1.20** — Criar `notes-service/src/main/resources/logback-spring.xml` (copiar do auth, ajustar logger `com.finpulse.notes`)

### Bloco 4: Security (se aplicável)

- [ ] **8.1.21** — Verificar se notes-service tem SecurityConfig
- [ ] **8.1.22** — Se sim: adicionar `/actuator/**` em `permitAll()`

### Bloco 5: Testes

- [ ] **8.1.23** — Criar pasta `notes-service/src/test/java/com/finpulse/notes/`
- [ ] **8.1.24** — Criar `AbstractIntegrationTest.java` com `MongoDBContainer`
- [ ] **8.1.25** — Criar `TestcontainersSetupTest.java` (sanity check)
- [ ] **8.1.26** — Rodar `mvn test` → sanity test deve passar
- [ ] **8.1.27** — Inspecionar `NotesController.java` para descobrir endpoints exatos
- [ ] **8.1.28** — Inspecionar DTOs em `notes-service/src/main/java/com/finpulse/notes/dto/`
- [ ] **8.1.29** — Criar `JwtTestHelper.java` em `src/test/java/.../helper/` (geração de tokens de teste)
- [ ] **8.1.30** — Criar `NotesControllerIntegrationTest.java` com 20+ testes (ver `07-plano-de-testes.md`)
- [ ] **8.1.31** — Rodar `mvn clean test` → todos os testes devem passar

### Bloco 6: Validação Manual

- [ ] **8.1.32** — Subir o notes-service: `mvn spring-boot:run`
- [ ] **8.1.33** — Testar `GET http://localhost:8082/actuator/health` → 200 com MongoDB UP
- [ ] **8.1.34** — Testar `GET http://localhost:8082/actuator/info` → metadados
- [ ] **8.1.35** — Testar `GET http://localhost:8082/actuator/prometheus` → texto Prometheus
- [ ] **8.1.36** — Testar uma requisição e verificar `X-Correlation-ID` na resposta
- [ ] **8.1.37** — Verificar logs do startup com `[no-cid]`
- [ ] **8.1.38** — Fazer uma requisição e verificar UUID nos logs

### Bloco 7: Coverage e Gate

- [ ] **8.1.39** — Abrir `target/site/jacoco/index.html` no navegador
- [ ] **8.1.40** — Anotar coverage total (Total Line e Total Branch)
- [ ] **8.1.41** — Se >= 70% line e 60% branch: atualizar gate no `pom.xml`
- [ ] **8.1.42** — Rodar `mvn test` de novo → confirmar que gate passa

### Bloco 8: Commit

- [ ] **8.1.43** — `git status` → revisar mudanças
- [ ] **8.1.44** — `git add backend/notes-service/`
- [ ] **8.1.45** — `git add backend/config-server/src/main/resources/configs/notes-service*.yml`
- [ ] **8.1.46** — `git commit -m "feat(observability): add Actuator + Testcontainers + Correlation ID to notes-service"`
- [ ] **8.1.47** — `git push`

**🎯 FASE 8.1 CONCLUÍDA**

---

## 🎯 FASE 8.2 — Observability no stock-service

> **Tempo estimado:** 6-8 horas
> **Pré-requisitos:** FASE 8.1 concluída
> **Novidade:** WireMock para mockar APIs externas

### Bloco 1: Dependências

- [ ] **8.2.1** — Abrir `backend/stock-service/pom.xml`
- [ ] **8.2.2** — Adicionar dependências do Actuator + Prometheus (igual 8.1)
- [ ] **8.2.3** — Adicionar Testcontainers BOM + core + junit-jupiter
- [ ] **8.2.4** — Adicionar `testcontainers/postgresql` SE o stock-service usa PG
- [ ] **8.2.5** — Adicionar `testcontainers` Redis genérico (`GenericContainer`)
- [ ] **8.2.6** — Adicionar `wiremock-standalone` versão 3.9.1 (test)
- [ ] **8.2.7** — Adicionar plugin Jacoco (mesmo do 8.1, mínimos 0.65/0.55)
- [ ] **8.2.8** — Build: `mvn clean install -DskipTests`

### Bloco 2: Inspeção do código atual

- [ ] **8.2.9** — Listar arquivos em `stock-service/src/main/java/com/finpulse/stock/`
- [ ] **8.2.10** — Inspecionar `StockController.java`
- [ ] **8.2.11** — Inspecionar adapters (`AlphaVantageAdapter.java`, `FinnhubAdapter.java`)
- [ ] **8.2.12** — Inspecionar configuração atual de cache (Redis)

### Bloco 3: Configurações YAML

- [ ] **8.2.13** — Reduzir `application.yml` do stock-service
- [ ] **8.2.14** — Criar `application-test.yml` com WireMock e Testcontainers
- [ ] **8.2.15** — Atualizar `stock-service.yml` no Config Server
- [ ] **8.2.16** — Criar `stock-service-dev.yml` no Config Server
- [ ] **8.2.17** — Criar `stock-service-prod.yml` no Config Server (variáveis de ambiente)
- [ ] **8.2.18** — Reiniciar Config Server

### Bloco 4: Filter, Logback, Aspects

- [ ] **8.2.19** — Criar `CorrelationIdFilter.java`
- [ ] **8.2.20** — Criar `logback-spring.xml`

### Bloco 5: Testes com WireMock

- [ ] **8.2.21** — Criar `AbstractIntegrationTest.java` com Redis + WireMock + (opcional) PG
- [ ] **8.2.22** — Criar `WireMockSetupTest.java`
- [ ] **8.2.23** — Criar pasta `src/test/resources/wiremock/` para fixtures JSON
- [ ] **8.2.24** — Criar fixtures:
  - `alpha-vantage-aapl-quote-success.json`
  - `alpha-vantage-aapl-company-success.json`
  - `finnhub-aapl-quote-success.json`
  - `alpha-vantage-rate-limited.json`
- [ ] **8.2.25** — Criar `StockApiStubs.java` (helper de stubs reutilizáveis)
- [ ] **8.2.26** — Criar `AlphaVantageAdapterTest.java` (testes unitários do adapter)
- [ ] **8.2.27** — Criar `FinnhubAdapterTest.java`
- [ ] **8.2.28** — Criar `StockControllerIntegrationTest.java` (15+ testes)
- [ ] **8.2.29** — Rodar `mvn clean test` → todos passam

### Bloco 6: Validação Manual

- [ ] **8.2.30** — Subir stock-service
- [ ] **8.2.31** — Testar `GET /actuator/health` (deve mostrar Redis UP)
- [ ] **8.2.32** — Testar `GET /actuator/prometheus`
- [ ] **8.2.33** — Fazer requisição de cotação e verificar Correlation ID

### Bloco 7: Coverage e Commit

- [ ] **8.2.34** — Verificar Jacoco → deve ter >= 65% line / 55% branch
- [ ] **8.2.35** — Atualizar gate se necessário
- [ ] **8.2.36** — Commit: `feat(observability): add Actuator + Testcontainers + WireMock to stock-service`
- [ ] **8.2.37** — Push

**🎯 FASE 8.2 CONCLUÍDA**

---

## 🎯 FASE 9 — Resilience4j (stock-service)

> **Tempo estimado:** 4-6 horas
> **Pré-requisitos:** FASE 8.2 concluída

### Bloco 1: Dependências

- [ ] **9.1** — Adicionar `resilience4j-spring-boot3` versão 2.2.0
- [ ] **9.2** — Adicionar `resilience4j-reactor` (se WebFlux) ou nada se Spring Web tradicional
- [ ] **9.3** — Adicionar `resilience4j-micrometer` (métricas)
- [ ] **9.4** — Build: `mvn clean install -DskipTests`

### Bloco 2: Configuração

- [ ] **9.5** — Adicionar bloco `resilience4j:` em `stock-service.yml` (Config Server)
- [ ] **9.6** — Configurar Circuit Breaker `alpha-vantage` (failure 50%, wait 30s)
- [ ] **9.7** — Configurar Circuit Breaker `finnhub` (mesmas configs)
- [ ] **9.8** — Configurar Retry (3 tentativas, backoff exponencial)
- [ ] **9.9** — Configurar TimeLimiter (3s)
- [ ] **9.10** — Configurar Bulkhead (20 concurrent calls)
- [ ] **9.11** — Configurar RateLimiter (100 req/s)
- [ ] **9.12** — Adicionar `circuitbreakers` e `circuitbreakerevents` em `management.endpoints.web.exposure.include`
- [ ] **9.13** — Reiniciar Config Server

### Bloco 3: Aplicar Annotations

- [ ] **9.14** — Em `AlphaVantageAdapter.getQuote()`: adicionar `@CircuitBreaker(name="alpha-vantage", fallbackMethod="fallbackGetQuote")`
- [ ] **9.15** — Adicionar `@Retry(name="alpha-vantage")`
- [ ] **9.16** — Adicionar `@TimeLimiter(name="external-apis")`
- [ ] **9.17** — Adicionar `@Bulkhead(name="external-apis")`
- [ ] **9.18** — Criar método `fallbackGetQuote(String symbol, Exception ex)` retornando cache
- [ ] **9.19** — Repetir 9.14-9.18 em `FinnhubAdapter`
- [ ] **9.20** — Aplicar nos métodos `getCompany()` também

### Bloco 4: Testes de Resilience

- [ ] **9.21** — Criar `CircuitBreakerIntegrationTest.java`:
  - `deveCircuitBreakerEstarFechadoInicialmente`
  - `deveAbrirAposFalhasConsecutivas`
  - `naoDevePermitirChamadasQuandoAberto`
  - `deveTransicionarParaHalfOpenAposEspera`
  - `deveFecharNovamenteAposChamadasBemSucedidas`
- [ ] **9.22** — Criar `RetryIntegrationTest.java`:
  - `deveRetentar3VezesAposFalhaTransitoria`
  - `deveAplicarBackoffExponencial`
- [ ] **9.23** — Criar `TimeLimiterTest.java`:
  - `deveTimeoutChamadaMaisLongaQue3s`
- [ ] **9.24** — Criar `RateLimiterTest.java`:
  - `deveRejeitarChamadasAcimaDoLimite`
- [ ] **9.25** — Rodar `mvn clean test` → tudo passa

### Bloco 5: Validação Manual

- [ ] **9.26** — Subir stock-service
- [ ] **9.27** — `curl http://localhost:8083/actuator/circuitbreakers` → lista CBs
- [ ] **9.28** — Forçar falha (chave de API errada) e verificar transição de estado
- [ ] **9.29** — `curl http://localhost:8083/actuator/circuitbreakerevents` → ver eventos
- [ ] **9.30** — Verificar métricas: `curl http://localhost:8083/actuator/prometheus | Select-String resilience`

### Bloco 6: Commit

- [ ] **9.31** — Commit: `feat(resilience): add Circuit Breaker + Retry + TimeLimiter to stock-service`
- [ ] **9.32** — Push

**🎯 FASE 9 CONCLUÍDA**

---

## 🎯 FASE 10 — Spring AOP

> **Tempo estimado:** 3-5 horas
> **Pré-requisitos:** FASE 9 concluída

### Bloco 1: Dependências (em CADA serviço)

- [ ] **10.1** — Adicionar `spring-boot-starter-aop` no `auth-service/pom.xml`
- [ ] **10.2** — Adicionar `spring-boot-starter-aop` no `notes-service/pom.xml`
- [ ] **10.3** — Adicionar `spring-boot-starter-aop` no `stock-service/pom.xml`

### Bloco 2: Annotations Customizadas

- [ ] **10.4** — Em `auth-service`: criar pacote `com.finpulse.auth.annotation`
- [ ] **10.5** — Criar `Audited.java`:
  - `@Target(ElementType.METHOD)`
  - `@Retention(RetentionPolicy.RUNTIME)`
  - String `action()` default ""
  - boolean `sanitize()` default true
- [ ] **10.6** — Replicar `Audited.java` em notes-service e stock-service (com ajuste de pacote)

### Bloco 3: Aspects

#### LoggingAspect (em cada serviço)

- [ ] **10.7** — Criar pacote `com.finpulse.{servico}.aspect`
- [ ] **10.8** — Criar `LoggingAspect.java`:
  - `@Aspect @Component`
  - `@ConditionalOnProperty(name="aop.logging-enabled", havingValue="true")`
  - Pointcut: `execution(* com.finpulse.{servico}.controller.*.*(..))`
  - `@Around`: logar entrada, saída com tempo, exceções
  - **Sanitizar** parâmetros (senha, token, secret)

#### MetricsAspect (em cada serviço)

- [ ] **10.9** — Criar `MetricsAspect.java`:
  - Injetar `MeterRegistry`
  - `@Around` em controllers e services
  - Criar timer com `Timer.builder("method.duration")`
  - Tags: classe, método, status (success/failure)

#### AuditAspect (apenas em auth-service inicialmente)

- [ ] **10.10** — Criar `AuditAspect.java`:
  - `@Around("@annotation(audited)")`
  - Capturar: user, action, params (sanitizados), result, correlationId
  - Salvar no MongoDB
  - Importante: NÃO falhar a operação principal se a auditoria falhar (try/catch)

- [ ] **10.11** — Criar `AuditLog.java` em `auth-service/entity/`
- [ ] **10.12** — Criar `AuditLogRepository.java`
- [ ] **10.13** — Adicionar conexão com MongoDB no `auth-service` (compartilhado com notes? ou DB próprio?)

### Bloco 4: Configurações AOP por Perfil

- [ ] **10.14** — No Config Server, adicionar em cada `*-service.yml`:
  ```yaml
  aop:
    logging-enabled: ${AOP_LOGGING:true}
    metrics-enabled: true
    audit-enabled: true
  ```
- [ ] **10.15** — Em `*-prod.yml`: desativar logging verbose (`AOP_LOGGING=false`)

### Bloco 5: Aplicar Annotations nos Serviços

- [ ] **10.16** — Marcar `AuthService.login()` com `@Audited(action="USER_LOGIN")`
- [ ] **10.17** — Marcar `AuthService.register()` com `@Audited(action="USER_REGISTER")`
- [ ] **10.18** — Marcar métodos relevantes com `@Timed`

### Bloco 6: Testes

- [ ] **10.19** — Criar `LoggingAspectTest.java`:
  - Verificar log de entrada
  - Verificar log de saída com tempo
  - **Verificar sanitização** (senha não aparece nos logs)
- [ ] **10.20** — Criar `MetricsAspectTest.java`:
  - Verificar criação de Timer
  - Verificar incremento de counter
- [ ] **10.21** — Criar `AuditAspectTest.java`:
  - Verificar persistência de audit log
  - Verificar que NÃO falha quando audit DB está fora
- [ ] **10.22** — Rodar `mvn clean test`

### Bloco 7: Commit

- [ ] **10.23** — Commit: `feat(aop): add LoggingAspect + MetricsAspect + AuditAspect`
- [ ] **10.24** — Push

**🎯 FASE 10 CONCLUÍDA**

---

## 🎯 FASE 11 — API Gateway

> **Tempo estimado:** 8-12 horas
> **Pré-requisitos:** FASES 8-10 concluídas

### Bloco 1: Criar Módulo Maven

- [ ] **11.1** — `cd C:\finpulse\backend`
- [ ] **11.2** — `mkdir api-gateway`
- [ ] **11.3** — Criar estrutura: `src/main/java/com/finpulse/gateway/`
- [ ] **11.4** — Criar `pom.xml` com dependencies:
  - `spring-cloud-starter-gateway`
  - `spring-cloud-starter-netflix-eureka-client`
  - `spring-cloud-starter-config`
  - `spring-boot-starter-actuator`
  - `micrometer-registry-prometheus`
  - `prometheus-metrics-exposition-formats`
  - `resilience4j-spring-cloud2`
  - JWT validation libs (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`)
- [ ] **11.5** — Adicionar `api-gateway` ao `backend/pom.xml` parent

### Bloco 2: Aplicação Principal

- [ ] **11.6** — Criar `ApiGatewayApplication.java`:
  - `@SpringBootApplication`
  - `@EnableDiscoveryClient`
- [ ] **11.7** — Criar `application.yml` minimal (bootstrap)

### Bloco 3: Configuração no Config Server

- [ ] **11.8** — Criar `config-server/src/main/resources/configs/api-gateway.yml`:
  - Configuração base (rotas, management, info)
- [ ] **11.9** — Criar `api-gateway-dev.yml`:
  - Eureka local
  - JWT secret hardcoded
  - CORS permissivo
- [ ] **11.10** — Criar `api-gateway-prod.yml`:
  - Variáveis de ambiente
  - CORS restritivo
  - HTTPS forçado
- [ ] **11.11** — Reiniciar Config Server

### Bloco 4: Rotas

- [ ] **11.12** — Em `api-gateway.yml`, definir rotas:
  ```yaml
  spring:
    cloud:
      gateway:
        routes:
          - id: auth-service
            uri: lb://auth-service
            predicates:
              - Path=/api/auth/**
          - id: notes-service
            uri: lb://notes-service
            predicates:
              - Path=/api/notes/**
            filters:
              - JwtAuthFilter
          - id: stock-service
            uri: lb://stock-service
            predicates:
              - Path=/api/stocks/**
            filters:
              - JwtAuthFilter
              - name: CircuitBreaker
                args:
                  name: stockCircuit
                  fallbackUri: forward:/fallback/stock
  ```

### Bloco 5: Filtros Globais

- [ ] **11.13** — Criar `CorrelationIdGlobalFilter.java`:
  - `implements GlobalFilter, Ordered`
  - `@Order(Ordered.HIGHEST_PRECEDENCE)`
  - Lê header → propaga
  - Se não veio → gera UUID
  - Adiciona header na resposta
- [ ] **11.14** — Criar `JwtAuthGatewayFilter.java`:
  - Extends `AbstractGatewayFilterFactory`
  - Valida JWT
  - Extrai userId, email, role
  - Injeta `X-User-Id`, `X-User-Email`, `X-User-Role` no request
  - Permite passagem (chain.filter)
- [ ] **11.15** — Criar `RateLimiterGlobalFilter.java` (opcional, pode usar built-in)

### Bloco 6: Endpoints Públicos

- [ ] **11.16** — Configurar rotas que NÃO exigem JWT:
  - `/api/auth/register`
  - `/api/auth/login`
  - `/api/auth/health`
  - `/actuator/**` (do gateway)

### Bloco 7: Fallback para Circuit Breaker

- [ ] **11.17** — Criar controller de fallback:
  ```java
  @RestController
  public class FallbackController {
      @GetMapping("/fallback/stock")
      public ResponseEntity<?> stockFallback() {
          return ResponseEntity.status(503).body(Map.of(
              "message", "Stock service indisponível, tente novamente"
          ));
      }
  }
  ```

### Bloco 8: Testes do Gateway

- [ ] **11.18** — Criar `GatewayApplicationTests.java` (smoke test)
- [ ] **11.19** — Criar `GatewayRoutingTest.java`:
  - Roteamento pra cada serviço
  - 401 sem JWT em endpoints protegidos
  - 200 com JWT válido
- [ ] **11.20** — Criar `GlobalFiltersTest.java`:
  - Correlation ID gerado
  - Correlation ID propagado
- [ ] **11.21** — Criar `RateLimitTest.java`
- [ ] **11.22** — Criar `CorsTest.java`

### Bloco 9: Adaptar Serviços Internos

Cada serviço (`auth`, `notes`, `stock`) precisa de uma flag para confiar no header `X-User-Id` quando atrás do Gateway:

- [ ] **11.23** — Adicionar property em cada serviço:
  ```yaml
  security:
    behind-gateway: ${SECURITY_BEHIND_GATEWAY:false}
  ```
- [ ] **11.24** — Criar `HeaderAuthFilter.java` em cada serviço:
  - Se `security.behind-gateway=true`, lê `X-User-Id` e popula SecurityContext
  - Senão, mantém comportamento atual (valida JWT)
- [ ] **11.25** — Em prod: setar `SECURITY_BEHIND_GATEWAY=true`

### Bloco 10: Validação Manual

- [ ] **11.26** — Subir Config Server, Eureka, todos os serviços e o Gateway
- [ ] **11.27** — `curl http://localhost:8761` → ver `api-gateway` registrado
- [ ] **11.28** — `curl http://localhost:8080/actuator/gateway/routes` → ver rotas
- [ ] **11.29** — Testar rotas:
  ```powershell
  # Login (público)
  curl -Method Post -Uri http://localhost:8080/api/auth/login `
       -ContentType "application/json" `
       -Body '{"email":"pedro@finpulse.com","password":"senha12345"}'
  
  # Notes (com JWT)
  curl -Uri http://localhost:8080/api/notes `
       -Headers @{ "Authorization" = "Bearer $token" }
  ```
- [ ] **11.30** — Verificar `X-Correlation-ID` propagado em todos os logs

### Bloco 11: Commit

- [ ] **11.31** — Commit do Gateway: `feat(gateway): add API Gateway with routing, JWT auth, CB, rate limiting`
- [ ] **11.32** — Commit dos serviços: `feat(security): add behind-gateway mode to services`
- [ ] **11.33** — Push

**🎯 FASE 11 CONCLUÍDA**

---

## 📊 Métricas Finais Esperadas

Ao concluir todas as fases:

| Serviço | Coverage Line | Coverage Branch | Testes |
|---|---|---|---|
| auth-service | 77%+ | 70%+ | 33+ |
| notes-service | 70%+ | 60%+ | 26+ |
| stock-service | 65%+ | 55%+ | 40+ (com WireMock + Resilience) |
| api-gateway | 60%+ | 50%+ | 15+ |

**Total:** ~115 testes, ~70% cobertura média.

---

## 🎓 Após Conclusão de Todas as Fases

- [ ] **FINAL.1** — Atualizar README.md com:
  - Badges de cobertura por serviço
  - Diagrama de arquitetura com Gateway
  - Roadmap marcando FASES 8-11 como concluídas
- [ ] **FINAL.2** — Criar tag de versão: `git tag -a v1.0-observability-complete -m "Observability completa em todos os serviços"`
- [ ] **FINAL.3** — Push da tag: `git push --tags`
- [ ] **FINAL.4** — Anotar aprendizados em um documento `LEARNINGS.md` (opcional)
- [ ] **FINAL.5** — Considerar próximas fases:
  - FASE 12: Mensageria (Kafka/RabbitMQ)
  - FASE 13-14: Frontends
  - FASE 15: Dockerização
  - FASE 16: CI/CD

---

## ⚠️ Regras de Ouro Durante Execução

1. **NÃO PULE PASSOS** — o checklist é sequencial por uma razão
2. **COMMIT FREQUENTE** — ao final de cada bloco, commit
3. **NÃO IGNORE TESTES** — se um teste falha, **PARE** e investigue
4. **DOCUMENTE DECISÕES NOVAS** — atualize `10-decisoes-tecnicas.md`
5. **CONSULTE OUTROS SPECS** — `06-plano-de-implementacao.md`, `07-plano-de-testes.md`, etc.
6. **VALIDAÇÃO MANUAL** — sempre teste no navegador/curl após cada fase
7. **EM CASO DE DÚVIDA** — leia o spec relacionado antes de improvisar

---

**Boa sorte na execução! 🚀**

Quando concluir a FASE 11, você terá um sistema de microsserviços com observability completa, resilience contra falhas, auditoria automática e API Gateway — pronto para qualquer entrevista de pleno/sênior em 2026.
