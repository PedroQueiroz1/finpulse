# Decisões Técnicas

> Documento de **registros arquiteturais** (ADRs - Architecture Decision Records) das FASES 8-11.
> Para cada decisão importante: contexto, opções, escolha e justificativa.

---

## 📚 Como ler este documento

Cada decisão segue o formato:

> **[ADR-XXX] Título da decisão**
> **Contexto:** problema que motivou a decisão
> **Opções:** alternativas consideradas
> **Decisão:** o que foi escolhido
> **Consequências:** trade-offs aceitos

---

## ADR-001: Estratégia Híbrida de Configurações

**Fase:** 8.1 (replicar do auth)

**Contexto:**
Microsserviços precisam de configurações diferentes em dev/test/prod. Como organizar?

**Opções:**
1. Tudo no Config Server (incluindo test)
2. Tudo local nos JARs
3. Híbrido: Config Server pra dev/prod, local pra test

**Decisão:** Opção 3 (Híbrido).

**Justificativa:**
- Em CI/CD (GitHub Actions), Config Server não está rodando — testes precisam funcionar independente
- Em dev/prod, queremos as vantagens do Config Server: hot reload, single source of truth, versionamento
- Padrão usado em empresas com Spring Cloud (Netflix, ING, Allianz)

**Consequências:**
- Levemente mais complexo (2 locais de config), mas isolamento de testes vale a pena
- `application-test.yml` precisa ter `spring.config.import: ""` pra desabilitar Config Server

---

## ADR-002: Testcontainers ao invés de Bancos Embutidos

**Fase:** 7.4 (já decidido), reforçado em 8.1 e 8.2

**Contexto:**
Testes de integração precisam de banco. Quais opções?

**Opções:**
1. **H2** (PostgreSQL) / **Fongo** (MongoDB) — banco em memória
2. **Testcontainers** — banco real em Docker

**Decisão:** Testcontainers em **TODOS** os serviços.

**Justificativa:**

H2 não é PostgreSQL. Diferenças críticas que causam bugs em prod:

| Cenário | PostgreSQL | H2 |
|---|---|---|
| `JSONB` | Suportado | Não existe |
| `UUID` | Tipo nativo | String |
| Window functions | Total | Limitado |
| `FOR UPDATE SKIP LOCKED` | Sim | Não |
| Triggers | Robusto | Parcial |

Fongo (mock do Mongo) tem comportamento ligeiramente diferente em queries complexas, agregações, indexes.

**Consequências:**
- Testes 5-10s mais lentos (mas reuse compensa)
- Precisa Docker na máquina do dev (não é problema em 2026)
- Em troca: bugs detectados em test, não em prod

---

## ADR-003: WireMock para APIs Externas em Testes

**Fase:** 8.2

**Contexto:**
stock-service depende de Alpha Vantage e Finnhub. Como testar sem chamar APIs reais?

**Opções:**
1. Chamar APIs reais nos testes (free tier limitado, lentidão, instabilidade)
2. **MockServer** (Java, robusto)
3. **WireMock** (mais popular, leve)
4. **OkHttp MockWebServer** (simples, mas limitado)

**Decisão:** WireMock.

**Justificativa:**
- Padrão da indústria pra mockar HTTP em testes Java
- Suporta gravação de chamadas reais (`record mode`) pra criar fixtures
- Integração nativa com JUnit 5 via `@RegisterExtension`
- Stubs declarativos com builder fluente
- Permite simular cenários complexos (latência, erros 5xx, headers)

**Consequências:**
- 1 dependência extra (~5MB)
- Curva de aprendizado pequena
- Em troca: testes confiáveis, rápidos, isolados

---

## ADR-004: Resilience4j ao invés de Hystrix

**Fase:** 9

**Contexto:**
Precisamos de Circuit Breaker, Retry, Bulkhead. Qual biblioteca?

**Opções:**
1. **Hystrix** (Netflix) — clássico mas em manutenção, último release 2018
2. **Resilience4j** — successor moderno, lightweight
3. **Sentinel** (Alibaba) — mais recurso, menos adoção fora da China

**Decisão:** Resilience4j.

**Justificativa:**
- **Hystrix está em modo manutenção desde 2018**, Netflix não recomenda mais
- Resilience4j é **lightweight** (sem dependências pesadas como Archaius)
- Integração nativa com Spring Boot 3 + Micrometer
- API funcional (Java 8+), encaixa em estilos modernos
- Recomendado pela própria comunidade Spring Cloud

**Consequências:**
- Sintaxe diferente do Hystrix (curva de aprendizado pequena)
- Métricas integradas com Prometheus automaticamente

---

## ADR-005: Spring AOP nativo ao invés de AspectJ puro

**Fase:** 10

**Contexto:**
Implementar logging, métricas e auditoria via aspects. Qual abordagem?

**Opções:**
1. **Spring AOP** (proxy-based)
2. **AspectJ** (compile-time ou load-time weaving)
3. Decorator pattern manual (sem AOP)

**Decisão:** Spring AOP.

**Justificativa:**
- Spring AOP cobre 95% dos casos (intercepta métodos public de beans Spring)
- AspectJ é mais poderoso (intercepta tudo, até métodos private), mas adiciona complexidade
- Não temos necessidade de interceptar construtores ou campos
- AspectJ requer agent JVM ou plugin Maven, mais friction

**Consequências:**
- Não conseguimos interceptar:
  - Chamadas dentro da mesma classe (`this.metodo()`)
  - Métodos private
  - Construtores
- Pra esses casos: refatorar pra outro bean ou aceitar como limitação

---

## ADR-006: Spring Cloud Gateway ao invés de Zuul

**Fase:** 11

**Contexto:**
Implementar API Gateway. Qual tecnologia?

**Opções:**
1. **Zuul 1** (blocking) — Netflix legado
2. **Zuul 2** (reactive) — pouco adotado fora Netflix
3. **Spring Cloud Gateway** — successor oficial do Spring
4. **Nginx** + Lua scripts
5. **Kong** ou **Traefik** (gateways standalone)

**Decisão:** Spring Cloud Gateway.

**Justificativa:**

| Critério | Spring Cloud Gateway | Zuul 1 | Nginx |
|---|---|---|---|
| Integração Spring | ✅ Nativa | ⚠️ Legacy | ❌ Externa |
| Modelo | Reactive (Netty) | Blocking | Não-blocking C |
| Service Discovery | ✅ Eureka nativo | ✅ Eureka nativo | ❌ Manual |
| Performance | Alta | Média | Muito alta |
| Curva | Média | Baixa | Alta |
| Comunidade | Crescendo | Encolhendo | Massiva |

- **Stack já é Spring**, faz sentido continuar
- Reactive (não bloqueia threads) escala melhor que Zuul 1
- Padrão atual recomendado pelo Spring desde 2019

**Consequências:**
- Modelo reativo exige aprendizado (`Mono`, `Flux`)
- Filtros customizados são `GlobalFilter` ao invés de servlet filters
- Compensação: integração mágica com Eureka e Spring Security

---

## ADR-007: Validação JWT no Gateway (offload)

**Fase:** 11

**Contexto:**
Hoje cada serviço valida JWT. Com Gateway, fazer isso uma vez só ou manter duplicado?

**Opções:**
1. Gateway valida, serviços confiam (offload)
2. Gateway + serviços validam (defense in depth)
3. Apenas serviços validam (gateway só roteia)

**Decisão:** Opção 1 (Gateway valida, serviços confiam via header).

**Justificativa:**
- **Performance**: validar JWT custa ~5ms (parsing + verificação de assinatura). Em 5 serviços, são 25ms desperdiçados
- **Single source of truth**: política de auth muda em UM lugar (Gateway)
- **Padrão de mercado**: Netflix, Uber, Airbnb fazem assim
- **Defesa em profundidade fica em layer de rede**: serviços internos não devem ser acessíveis fora do cluster Kubernetes

**Consequências:**
- Serviços internos **NÃO PODEM** ser expostos pra internet diretamente
- Necessário configurar firewall/network policy adequado em prod
- Em dev local: precisamos confiar que ninguém vai bypassar o Gateway
- Pra dev local, manter ambas as portas abertas mas documentar que produção tem gateway-only access

---

## ADR-008: Correlation ID via Header HTTP

**Fase:** 7.5 e 11 (propagação no Gateway)

**Contexto:**
Como rastrear requisição cruzando múltiplos serviços?

**Opções:**
1. **Correlation ID via header HTTP**
2. **OpenTelemetry** com Jaeger/Zipkin (distributed tracing completo)
3. **Spring Cloud Sleuth** (descontinuado em 2024)

**Decisão:** Correlation ID **agora**, OpenTelemetry **depois** (FASE 18+).

**Justificativa:**
- Correlation ID cobre **80% dos casos** com **5% do esforço**
- OpenTelemetry é poderoso mas tem curva enorme:
  - Coletor (collector) na infraestrutura
  - Backend (Jaeger, Tempo, Honeycomb)
  - Instrumentação de spans
  - Sampling strategy
- Pra projeto de aprendizado, Correlation ID é didático e suficiente
- Pode migrar pra OpenTelemetry depois sem retrabalho (header pode coexistir)

**Consequências:**
- Sem trace spans (timing por etapa)
- Sem visualização gráfica de jornada
- Pra debug: usa Grafana Loki ou Elasticsearch com query `correlationId=abc123`

---

## ADR-009: Manter SecurityConfig nos Serviços Internos (mesmo com Gateway)

**Fase:** 11

**Contexto:**
Após Gateway validar JWT, serviços internos precisam de SecurityConfig?

**Opções:**
1. Remover Spring Security dos serviços internos (depois do Gateway)
2. Manter Spring Security simplificado (apenas leitura de `X-User-Id`)
3. Manter validação JWT também internamente (paranoid mode)

**Decisão:** Opção 2.

**Justificativa:**
- Sem Spring Security, perdemos: `@PreAuthorize`, `SecurityContext`, integração com `@AuthenticationPrincipal`
- Com leitura simplificada de `X-User-Id`:
  - Cria-se um filter que lê o header e popula o `SecurityContext`
  - Beans Spring continuam funcionando (`@AuthenticationPrincipal String email`)
  - Em dev (sem Gateway), serviços individualmente continuam funcionando

**Consequências:**
- Cada serviço tem 2 modos:
  - **Standalone**: valida JWT diretamente (dev local sem Gateway)
  - **Behind Gateway**: confia em `X-User-Id` (prod)
- Configuração via perfil ou property: `security.behind-gateway: true|false`

---

## ADR-010: Não usar Spring Cloud Sleuth ou Micrometer Tracing

**Fase:** 8-11

**Contexto:**
Para FASE 8 vamos só logar `correlationId` no MDC. Por que não Micrometer Tracing?

**Opções:**
1. **Logback + MDC manual** (atual)
2. **Micrometer Tracing** (sucessor do Sleuth)
3. **OpenTelemetry SDK direto**

**Decisão:** Manter atual (Logback + MDC).

**Justificativa:**
- Micrometer Tracing exige backend (Jaeger, Zipkin)
- Pra simples logs, MDC é suficiente
- Pode adicionar Micrometer Tracing depois sem mudar código de aplicação (apenas dependências e configurações)

**Consequências:**
- Sem distributed tracing visual
- Logs ainda são a fonte principal de debug

---

## ADR-011: Pasta `commons` Compartilhada vs Duplicação

**Fase:** 10 (aspects)

**Contexto:**
Os aspects (LoggingAspect, MetricsAspect) podem ser compartilhados entre serviços. Como organizar?

**Opções:**
1. **Módulo Maven compartilhado** (`backend/commons/`)
2. **Duplicar código em cada serviço**
3. **Biblioteca interna publicada** (mvn install local)

**Decisão:** Duplicar inicialmente, refatorar pra `commons` quando houver 3+ usos.

**Justificativa:**
- DRY é importante, mas YAGNI também
- Compartilhar agora cria acoplamento prematuro
- Quando os 3 serviços tiverem aspects funcionais, fica claro o que extrair
- Padrão "Rule of Three": só refatora quando o mesmo código aparece 3 vezes

**Consequências:**
- Curto prazo: código duplicado em 3 lugares
- Médio prazo: extração consciente para `backend-commons/` na FASE 10.5 (futura)

---

## ADR-012: Métricas Customizadas via `@Timed` e `@Counted`

**Fase:** 10

**Contexto:**
Como criar métricas de negócio (não só técnicas)?

**Opções:**
1. **Annotations do Micrometer** (`@Timed`, `@Counted`)
2. **Programático** (`MeterRegistry` injetado)
3. **AOP customizado** (pra controlar mais)

**Decisão:** Mix de annotations e MeterRegistry direto.

**Justificativa:**
- `@Timed` é elegante para timer simples
- MeterRegistry é necessário pra contadores condicionais (ex: contar só logins bem-sucedidos)
- Aspects customizados ficam pra casos sem suporte nativo

**Exemplo:**
```java
@Timed(value = "auth.login.duration")
public AuthResponse login(LoginRequest request) {
    // Timer automático
    if (success) {
        meterRegistry.counter("auth.login.success").increment();
    }
}
```

---

## ADR-013: Estratégia de Cache no stock-service

**Fase:** Já decidida na FASE 5, reforçada com Circuit Breaker na FASE 9

**Contexto:**
Cache de cotações e dados de empresa. Como balancear freshness vs custo?

**Decisão:**
- **Cotações**: TTL 60s (próximo de tempo real, mas economiza API calls)
- **Dados de empresa**: TTL 24h (mudam raramente)
- **Em caso de Circuit Breaker aberto**: retornar último cache (mesmo expirado)

**Justificativa:**
- Free tier do Alpha Vantage: 5 calls/min, 500/dia
- Cache de 60s reduz drasticamente uso
- Em falhas, dados antigos > dados nenhum (degradação graciosa)

---

## ADR-014: Não Persistir Refresh Tokens (Stateless)

**Fase:** Já decidida (FASE 3), relevante pra FASE 11

**Contexto:**
Refresh tokens permitem renovar acesso por 7 dias. Persistir em banco ou não?

**Opções:**
1. **Stateless**: refresh token também é JWT (auto-contido)
2. **Stateful**: salvar hash do refresh token em tabela

**Decisão:** Stateless (já implementado).

**Justificativa:**
- Não precisa hit no banco a cada refresh
- Simplifica scaling horizontal

**Consequências:**
- **Não há revogação fácil** (se vazar, válido até expirar)
- Pra futuro: adicionar lista negra (Redis) se necessário
- **Importante**: avisar usuário pra trocar senha em caso de comprometimento

---

## ADR-015: API Gateway escuta na porta 8080 (não 80)

**Fase:** 11

**Contexto:**
Em dev local, qual porta o Gateway usa?

**Opções:**
1. Porta 80 (HTTP padrão)
2. Porta 443 (HTTPS padrão)
3. Porta 8080 (alternativa comum)

**Decisão:** Porta 8080 em dev.

**Justificativa:**
- Porta 80/443 exige privilégio root no Linux/Mac
- Em dev local, simplifica
- Em prod, atrás de load balancer (k8s ingress), o LB usa 443 → 8080 internamente

---

## 📋 Resumo Tabular

| # | Decisão | Fase | Razão Principal |
|---|---|---|---|
| 001 | Config híbrido (Config Server + local test) | 8.1 | Isolamento de CI/CD |
| 002 | Testcontainers em todos | 8 | Fidelidade vs banco real |
| 003 | WireMock pra APIs externas | 8.2 | Testes isolados e rápidos |
| 004 | Resilience4j (não Hystrix) | 9 | Hystrix em manutenção |
| 005 | Spring AOP (não AspectJ) | 10 | Suficiente, menos complexo |
| 006 | Spring Cloud Gateway (não Zuul) | 11 | Stack Spring, reativo |
| 007 | JWT validado no Gateway | 11 | Performance + simplicidade |
| 008 | Correlation ID > OpenTelemetry (por agora) | 11 | 80/20 |
| 009 | Spring Security simplificado nos serviços | 11 | Manter `@AuthenticationPrincipal` |
| 010 | MDC manual > Micrometer Tracing | 8 | Não precisa backend de tracing |
| 011 | Duplicar antes de extrair commons | 10 | YAGNI |
| 012 | Mix `@Timed` + MeterRegistry | 10 | Flexibilidade |
| 013 | Cache fallback no CB aberto | 9 | Degradação graciosa |
| 014 | Refresh tokens stateless | 3 | Simplicidade e scaling |
| 015 | Gateway na porta 8080 | 11 | Dev sem root |

---

**Próximo:** `11-guia-de-execucao.md` para comandos prontos.
