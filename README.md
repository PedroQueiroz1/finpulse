# FinPulse 📈

> Plataforma de mercado financeiro e produtividade pessoal construída com arquitetura de microsserviços.

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.13-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.1-blue)](https://spring.io/projects/spring-cloud)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7-green?logo=mongodb)](https://www.mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-28-blue?logo=docker)](https://www.docker.com/)
[![Coverage](https://img.shields.io/badge/Coverage-77%25-success)](https://github.com/PedroQueiroz1/finpulse)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 🎯 Sobre o Projeto

**FinPulse** é uma plataforma SaaS que combina monitoramento de mercado financeiro em tempo real com ferramentas de produtividade pessoal. O projeto foi construído com foco em **arquitetura de microsserviços profissional**, aplicando padrões de design, observability completa e práticas modernas de desenvolvimento.

### O que torna o projeto especial

- **Arquitetura distribuída** com Service Discovery, Configuration Server centralizado e API Gateway *(planejado)*
- **Observability completa** no `auth-service`: métricas Prometheus, perfis por ambiente, cobertura de testes monitorada, testes de integração com containers reais e correlation ID para rastreamento distribuído
- **Padrões de Design** aplicados na prática: Strategy, Adapter, Facade, Repository, Template Method, Chain of Responsibility
- **77% de cobertura de código** (line) e 70% (branch) no `auth-service`, validados via Jacoco com gate automático no build
- **27 testes de integração** rodando contra PostgreSQL real via Testcontainers

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────────────┐
│                          FinPulse Backend                           │
│                                                                     │
│  ┌─────────────┐      ┌──────────────┐      ┌──────────────────┐    │
│  │ Config      │      │   Eureka     │      │  API Gateway     │    │
│  │ Server      │      │   Server     │      │  (planejado)     │    │
│  │  :8888      │      │   :8761      │      │                  │    │
│  └─────────────┘      └──────────────┘      └──────────────────┘    │
│         ▲                     ▲                                     │
│         │                     │                                     │
│         │  ┌──────────────────┴──────────────────┐                  │
│         │  │                                     │                  │
│  ┌──────┴──┴──────┐  ┌──────────────┐  ┌──────────────┐             │
│  │ Auth Service   │  │ Notes        │  │ Stock        │             │
│  │ :8081          │  │ Service      │  │ Service      │             │
│  │                │  │ :8082        │  │ :8083        │             │
│  │ JWT, BCrypt    │  │ CRUD, Groups │  │ Cache, APIs  │             │
│  └────────┬───────┘  └──────┬───────┘  └──────┬───────┘             │
│           │                 │                 │                     │
│           ▼                 ▼                 ▼                     │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐                │
│  │ PostgreSQL  │   │  MongoDB    │   │ Redis Cache │                │
│  │ (users)     │   │  (notes)    │   │ (quotes)    │                │
│  └─────────────┘   └─────────────┘   └─────────────┘                │
│                                                                     │
│                                          ┌─────────────┐            │
│                                          │ Alpha Vant. │            │
│                                          │ + Finnhub   │            │
│                                          │ (external)  │            │
│                                          └─────────────┘            │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Stack Tecnológico

### Backend
- **Java 17** — LTS, com Records, sealed classes e var inference
- **Spring Boot 3.4.13** — framework principal
- **Spring Cloud 2024.0.1** — service discovery e config centralizado
- **Spring Security 6** — autenticação JWT stateless
- **Spring Data JPA** — persistência relacional
- **Spring Data MongoDB** — persistência de documentos
- **Spring Data Redis** — caching de cotações
- **Maven** — build e gerenciamento de dependências

### Persistência
- **PostgreSQL 16** — dados relacionais (users, auth)
- **MongoDB 7** — dados de documento (notes)
- **Redis 7** (Memurai no Windows) — cache de baixa latência
- **Flyway** — versionamento de schemas SQL

### Observability *(aplicada no auth-service)*
- **Spring Boot Actuator** — endpoints de health, metrics, info
- **Micrometer + Prometheus** — métricas no formato Prometheus
- **Jacoco 0.8.12** — cobertura de testes com gate de qualidade
- **Testcontainers 1.20.4** — testes de integração com PostgreSQL real
- **Logback + MDC** — logs estruturados com correlation ID

### Documentação & Testes
- **OpenAPI 3 (Swagger UI)** — documentação interativa da API
- **JUnit 5** — testes unitários e de integração
- **AssertJ** — fluent assertions
- **Mockito** — mocks em testes unitários

### Frontend *(planejado)*
- **Angular 17+** — versão principal
- **React 18+** — versão alternativa (toggle entre as duas)

### DevOps *(planejado)*
- **Docker** — containerização
- **Kubernetes** — orquestração
- **GitHub Actions** — CI/CD
- **Prometheus + Grafana** — monitoramento

---

## 🚀 Microsserviços

### 1. Config Server (`:8888`)
Servidor de configuração centralizada usando Spring Cloud Config em modo nativo. Distribui YAMLs específicos por serviço e por perfil de execução.

**Estratégia híbrida:** configs de `dev` e `prod` no Config Server, configs de `test` locais no JAR.

### 2. Eureka Server (`:8761`)
Service Discovery — cada microsserviço se registra ao subir e descobre os outros pelo nome lógico. Permite scaling horizontal sem hardcoding de URLs.

**Dashboard:** http://localhost:8761

### 3. Auth Service (`:8081`)
Autenticação e gerenciamento de usuários.

**Funcionalidades:**
- Registro e login com BCrypt (custo 12)
- JWT com access token (15min) + refresh token (7 dias)
- Validação de credenciais e geração de tokens
- Endpoint `/me` para retornar dados do usuário autenticado
- 5 perfis de Role: `USER`, `PREMIUM`, `ADMIN`, `SUPER_ADMIN`, `GUEST`
- **Observability completa:** Actuator, Prometheus, Jacoco, Testcontainers, Correlation ID
- **77% de cobertura de código** (line) / **70%** (branch)
- **27 testes de integração** rodando com PostgreSQL real via Docker

**Endpoints:**
```
POST   /api/auth/register   → Cria novo usuário (201)
POST   /api/auth/login      → Autentica e retorna tokens (200)
POST   /api/auth/refresh    → Renova access token (200)
GET    /api/auth/me         → Dados do usuário autenticado (200)
GET    /api/auth/health     → Health check (200)
```

### 4. Notes Service (`:8082`)
CRUD de notas pessoais com agrupamento e busca.

**Funcionalidades:**
- CRUD completo com soft delete
- Agrupamento por tags
- Busca textual full-text (MongoDB Atlas Search ready)
- Validação de autoria via JWT

**Endpoints:**
```
GET    /api/notes              → Lista todas as notas do usuário
POST   /api/notes              → Cria nova nota
GET    /api/notes/{id}         → Detalhes de uma nota
PUT    /api/notes/{id}         → Atualiza nota
DELETE /api/notes/{id}         → Soft delete
GET    /api/notes/groups       → Lista grupos/tags
```

### 5. Stock Service (`:8083`)
Cotações de ações em tempo real com cache inteligente.

**Funcionalidades:**
- Integração com **Alpha Vantage** e **Finnhub** (failover automático)
- Cache Redis com TTLs diferenciados:
  - Cotações: **60 segundos**
  - Dados de empresa: **24 horas**
- Padrões de design aplicados:
  - **Strategy** — escolha de provider em tempo de execução
  - **Adapter** — normalização das respostas (cada API retorna JSON diferente)
  - **Facade** — `StockService` esconde complexidade do orquestrador

**Endpoints:**
```
GET    /api/stocks/{symbol}/quote     → Cotação atual (cache 60s)
GET    /api/stocks/{symbol}/company   → Dados da empresa (cache 24h)
GET    /api/stocks/providers          → Status dos providers
```

---

## 📊 Observability *(implementada no auth-service)*

### Métricas via Spring Boot Actuator

| Endpoint | Descrição |
|----------|-----------|
| `GET /actuator/health` | Status detalhado (DB, Redis, Eureka, Config) |
| `GET /actuator/health/liveness` | Kubernetes liveness probe |
| `GET /actuator/health/readiness` | Kubernetes readiness probe |
| `GET /actuator/info` | Metadados (versão, Java, descrição) |
| `GET /actuator/metrics` | Lista de métricas disponíveis |
| `GET /actuator/metrics/{name}` | Detalhes de uma métrica específica |
| `GET /actuator/prometheus` | **Formato Prometheus** (texto puro) |

**Métricas customizadas configuradas:**
- Histograma de latência HTTP (p50, p95, p99)
- Tag automática `application=auth-service` em todas as métricas
- Métricas JVM (heap, GC, threads, classes)
- Métricas Hikari (connection pool do PostgreSQL)

### Correlation ID *(rastreamento distribuído)*

Toda requisição HTTP recebe um UUID único (header `X-Correlation-ID`). Esse ID:

1. É **gerado** se a requisição não trouxe um (primeira entrada no sistema)
2. É **reutilizado** se já veio no header (propagação entre serviços)
3. Aparece em **todos os logs** daquela requisição via MDC do SLF4J
4. É **devolvido** na resposta HTTP para o cliente referenciar

**Exemplo de log:**
```
14:23:15.123 INFO  [auth-service] [a3f9b2c1-4d5e-...] AuthController : POST /api/auth/login
14:23:15.234 INFO  [auth-service] [a3f9b2c1-4d5e-...] AuthService    : Login bem-sucedido
```

### Perfis de Logback

- **`dev`** — console colorido, formato legível, DEBUG ativo
- **`prod`** — JSON estruturado (pronto para Grafana Loki / ELK)
- **`test`** — minimal, só WARN+ para não poluir output dos testes

---

## ⚙️ Como Rodar Localmente

### Pré-requisitos

- Java 17 (Temurin ou OpenJDK)
- Maven 3.9+
- Docker Desktop *(para Testcontainers)*
- PostgreSQL 16 *(local ou Docker)*
- MongoDB 7 *(local ou Docker)*
- Redis 7 *(Memurai no Windows)*

### Passo 1: Clonar o repositório

```bash
git clone https://github.com/PedroQueiroz1/finpulse.git
cd finpulse
```

### Passo 2: Configurar variáveis de ambiente

Crie o arquivo `backend/stock-service/.env`:

```env
ALPHA_VANTAGE_API_KEY=sua_chave_aqui
FINNHUB_API_KEY=sua_chave_aqui
```

> Pegue chaves gratuitas em [alphavantage.co](https://www.alphavantage.co/support/#api-key) e [finnhub.io](https://finnhub.io/dashboard).

### Passo 3: Subir os serviços (ordem importante!)

Em **5 terminais separados**, na ordem:

```bash
# Terminal 1 — Config Server
cd backend/config-server
mvn spring-boot:run

# Terminal 2 — Eureka Server (aguarde Config Server estar UP)
cd backend/eureka-server
mvn spring-boot:run

# Terminal 3 — Auth Service
cd backend/auth-service
mvn spring-boot:run

# Terminal 4 — Notes Service
cd backend/notes-service
mvn spring-boot:run

# Terminal 5 — Stock Service
cd backend/stock-service
mvn spring-boot:run
```

### Passo 4: Validar que tudo subiu

- **Eureka Dashboard:** http://localhost:8761 — deve mostrar 3 serviços registrados
- **Health do auth-service:** http://localhost:8081/actuator/health
- **Swagger do auth-service:** http://localhost:8081/swagger-ui.html

---

## 🧪 Como Rodar os Testes

### Testes do auth-service (com Testcontainers)

```bash
cd backend/auth-service
mvn clean test
```

**O que acontece:**
1. Maven baixa imagem `postgres:16-alpine` *(primeira vez)*
2. Testcontainers sobe PostgreSQL real em Docker
3. Spring conecta no container
4. Flyway aplica migrations no banco de teste
5. 27 testes de integração executam
6. Jacoco gera relatório de cobertura

**Tempo médio:** 25-30 segundos *(com reuse de container ativado)*

### Visualizar relatório de cobertura

```bash
start backend/auth-service/target/site/jacoco/index.html
```

### Gate de qualidade

O build **falha automaticamente** se a cobertura cair abaixo de:
- **70% de line coverage**
- **60% de branch coverage**

Esse é o padrão de empresas sérias — código sem teste suficiente **não vai pra main**.

---

## 🔬 Perfis Spring

O `auth-service` suporta 3 perfis. Para escolher:

### Dev (padrão)
```bash
mvn spring-boot:run
# ou explicitamente:
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Test
Ativado automaticamente quando `mvn test` é executado.

### Prod (simulação local)
```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=prod" \
  "-Dspring-boot.run.jvmArguments=-DDB_URL=jdbc:postgresql://localhost:5432/finpulse_auth -DDB_USER=finpulse -DDB_PASSWORD=finpulse123 -DJWT_SECRET=seu-secret-aqui -DEUREKA_URL=http://localhost:8761/eureka/"
```

---

## 📐 Padrões de Design Aplicados

| Padrão | Onde | Por quê |
|--------|------|---------|
| **Strategy** | `stock-service/strategy/` | Trocar provider de cotações em runtime |
| **Adapter** | `stock-service/adapter/` | Normalizar respostas de APIs diferentes |
| **Facade** | `StockService.java` | Esconder complexidade do orquestrador |
| **Repository** | `*/repository/` | Abstrair acesso a dados (JPA / Mongo) |
| **Template Method** | `BusinessException` | Hierarquia de exceções de negócio |
| **Chain of Responsibility** | `GlobalExceptionHandler` | Handler por tipo de exception |
| **Builder** | DTOs com `@Builder` *(planejado)* | Construção de objetos complexos |
| **DI (Spring IoC)** | Todo o projeto | Desacoplamento e testabilidade |

---

## 📈 Métricas do Projeto

> *Dados reais do `auth-service` (validados via Jacoco)*

| Métrica | Valor |
|---------|-------|
| **Cobertura de linhas** | 77% |
| **Cobertura de branches** | 70% |
| **Total de testes** | 33 (27 integração + 6 unidade) |
| **Testes passando** | 100% ✅ |
| **Tempo médio de build** | 25s *(com reuse)* |
| **Classes cobertas** | 23 |
| **Endpoints documentados** | 100% via Swagger |

### Cobertura por pacote

| Pacote | Coverage |
|--------|----------|
| `dto` | 100% 🟢 |
| `enums` | 100% 🟢 |
| `mapper` | 95% 🟢 |
| `service` | 87% 🟢 |
| `config` | 75% 🟢 |
| `exception` | 74% 🟢 |
| `entity` | 59% 🟡 |
| `controller` | 50% 🟡 *(há código legado a remover)* |

---

## 🗺️ Roadmap

### ✅ Concluído
- [x] **FASE 1** — Setup do monorepo, Maven multi-módulo
- [x] **FASE 2** — Config Server + Eureka Server
- [x] **FASE 3** — Auth Service (JWT, BCrypt, refresh tokens)
- [x] **FASE 4** — Notes Service (MongoDB, CRUD, grupos)
- [x] **FASE 5** — Stock Service (Redis cache, Alpha Vantage + Finnhub)
- [x] **FASE 6** — Documentação OpenAPI/Swagger
- [x] **FASE 7** — Observability completa *(auth-service)*
  - [x] 7.1 — Spring Boot Actuator + Prometheus
  - [x] 7.2 — Perfis dev/test/prod
  - [x] 7.3 — Jacoco com gate automático
  - [x] 7.4 — Testcontainers
  - [x] 7.5 — Logback + Correlation ID

### 🚧 Em planejamento
- [ ] **FASE 8** — Replicar FASE 7 em `notes-service` e `stock-service`
- [ ] **FASE 9** — Resilience4j (Circuit Breaker, Retry, Bulkhead)
- [ ] **FASE 10** — Spring AOP (logging, métricas customizadas)
- [ ] **FASE 11** — API Gateway (Spring Cloud Gateway)
- [ ] **FASE 12** — Mensageria assíncrona (RabbitMQ ou Kafka)
- [ ] **FASE 13** — Frontend Angular 17+
- [ ] **FASE 14** — Frontend React 18+ *(versão alternativa)*
- [ ] **FASE 15** — Dockerização completa (docker-compose)
- [ ] **FASE 16** — CI/CD com GitHub Actions
- [ ] **FASE 17** — Deploy em Kubernetes
- [ ] **FASE 18** — Prometheus + Grafana dashboard

---

## 📚 Aprendizados Documentados

Durante o desenvolvimento, decisões técnicas importantes foram tomadas:

- **NÃO uso Lombok** — prefiro Records do Java + getters/setters explícitos quando necessário, para visibilidade no debug
- **BigDecimal obrigatório para dinheiro** — nunca `double` (problemas de precisão)
- **Strategy híbrida de configs** — Config Server pra `dev`/`prod`, local pra `test` (testes precisam rodar isolados em CI/CD)
- **Testcontainers ao invés de H2** — H2 não é PostgreSQL, esconde bugs específicos do banco real
- **Correlation ID via header HTTP** — base do rastreamento distribuído antes de adotar OpenTelemetry no futuro

---

## 👨‍💻 Autor

**Pedro Queiroz**

- GitHub: [@PedroQueiroz1](https://github.com/PedroQueiroz1)
- LinkedIn: *(adicionar)*

---

## 📄 Licença

Este projeto está sob a licença MIT. Veja [LICENSE](LICENSE) para mais detalhes.

---

## 🙏 Agradecimentos

Projeto desenvolvido como estudo aprofundado de:
- Arquitetura de microsserviços com Spring Cloud
- Observability moderna (Actuator, Prometheus, Jacoco, Testcontainers)
- Padrões de design aplicados em contexto real
- Boas práticas de desenvolvimento Java em 2026

---

<div align="center">

**⭐ Se este projeto te ajudou a aprender algo, considere dar uma estrela!**

</div>
