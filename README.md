# PDV System — Ponto de Venda

[![CI](https://github.com/EstevezCodando/pdv/actions/workflows/ci.yml/badge.svg)](https://github.com/EstevezCodando/pdv/actions/workflows/ci.yml)
[![CD](https://github.com/EstevezCodando/pdv/actions/workflows/cd.yml/badge.svg)](https://github.com/EstevezCodando/pdv/actions/workflows/cd.yml)
[![CodeQL](https://github.com/EstevezCodando/pdv/actions/workflows/security.yml/badge.svg)](https://github.com/EstevezCodando/pdv/actions/workflows/security.yml)
[![Coverage](https://img.shields.io/badge/cobertura-%E2%89%A591%25-brightgreen)](https://github.com/EstevezCodando/pdv/actions/workflows/ci.yml)

Sistema de gerenciamento de ponto de venda desenvolvido como trabalho prático (TP5) na disciplina de Projeto De Bloco

---

## Sobre

Aplicação web Spring Boot para gerenciamento de usuarios, produtos e registro de vendas, com controle de estoque integrado.

Funcionalidades:

- Cadastro e gerenciamento de usuarios com perfis (ADMIN, OPERADOR)
- Cadastro e controle de estoque de produtos
- Registro de vendas com baixa automática de estoque
- Dashboard com métricas consolidadas (faturamento, estoque, vendas)
- Autenticação com login/logout e remember-me
- API REST com autenticação HTTP Basic

---

## Pré-requisitos

- Java 21
- Gradle 8.12+ (ou use o wrapper `./gradlew`)
- Docker e Docker Compose (para PostgreSQL local)

---

## Configuração

### Perfil H2 (desenvolvimento — banco em memória)

Nenhuma configuração necessária. Os dados iniciais (usuarios e produtos) são criados automaticamente ao subir.

### Perfil PostgreSQL (produção)

1. Copie o arquivo de exemplo e preencha as credenciais reais:

   ```bash
   cp .env.example .env
   ```

2. Suba o banco PostgreSQL local:

   ```bash
   docker-compose up -d
   ```

3. Carregue as variáveis de ambiente antes de executar:
   ```bash
   export $(cat .env | xargs)
   ```

---

## Executando

### Com banco H2 (padrão para desenvolvimento)

```bash
./gradlew bootRun
```

Acesse: http://localhost:8080

### Com PostgreSQL

```bash
./gradlew bootRun --args='--spring.profiles.active=postgres'
```

---

## Testes

### Testes unitários e de integração

```bash
./gradlew clean check
```

O relatório de cobertura JaCoCo é gerado em `build/reports/jacoco/test/html/index.html`.

### Testes Selenium (pós-deploy)

Requerem a aplicação rodando em `http://localhost:8080` e as variáveis de ambiente `PDV_ADMIN_USER` e `PDV_ADMIN_PASS`:

```bash
PDV_ADMIN_USER=admin@pdv.com PDV_ADMIN_PASS=admin123 ./gradlew seleniumTest
```

---

## CI/CD

### `ci.yml` — Integração Contínua

Executado em todo push e pull request para `main`:

1. Build e testes com Gradle
2. Relatório de cobertura JaCoCo (mínimo 90%)
3. OWASP Dependency Check (falha em CVEs com CVSS >= 9)
4. Upload de artefatos de relatório

### `cd.yml` — Entrega Contínua

Executado em push para `main` após CI passar:

1. Empacota o JAR versionado com SHA do commit
2. Deploy local/remoto via SSH
3. Smoke tests com autenticação HTTP Basic
4. Testes Selenium pós-deploy
5. ZAP DAST (baseline scan)

### Secrets necessários no GitHub

| Secret           | Descrição                                |
| ---------------- | ---------------------------------------- |
| `DB_URL`         | URL JDBC do banco PostgreSQL de produção |
| `DB_USERNAME`    | Usuário do banco                         |
| `DB_PASSWORD`    | Senha do banco                           |
| `DEPLOY_HOST`    | Host do servidor de deploy               |
| `DEPLOY_USER`    | Usuário SSH do servidor                  |
| `DEPLOY_SSH_KEY` | Chave privada SSH                        |
| `PDV_ADMIN_USER` | Email do admin para smoke/Selenium tests |
| `PDV_ADMIN_PASS` | Senha do admin para smoke/Selenium tests |

> **Sobre OIDC:** O pipeline utiliza webhook autenticado via `PDV_DEPLOY_WEBHOOK` secret para deploy remoto.
> Autenticação OIDC com provedor de nuvem (AWS/Azure/GCP) pode ser habilitada substituindo o step de webhook
> pela action oficial do provedor (`aws-actions/configure-aws-credentials`, `azure/login`, etc.)
> quando a infraestrutura cloud for provisionada.

---

## Credenciais de desenvolvimento

Criadas automaticamente ao subir com o perfil `h2`:

| Email            | Senha    | Perfil   |
| ---------------- | -------- | -------- |
| admin@pdv.com    | admin123 | ADMIN    |
| operador@pdv.com | op123    | OPERADOR |
| maria@pdv.com    | maria123 | OPERADOR |

> **Atenção:** credenciais para TESTE ! não são para produção.

---

## Estrutura principal

```
src/main/java/com/pdv/pontovenda/
├── config/          # SecurityConfig, DataInitializer, filtros, listeners
├── controller/      # MVC controllers (web) e ApiControllers (REST)
├── dto/             # Request/Response e form DTOs
├── entity/          # Entidades JPA
├── exception/       # Exceções de negócio e handlers
├── repository/      # Spring Data JPA repositories
└── service/         # Lógica de negócio
```

## Arquitetura do Sistema

O PDV adota arquitetura **MVC em camadas** com Spring Boot 3. Cada camada tem responsabilidade única e não conhece detalhes das camadas adjacentes.

### Camadas e responsabilidades

| Camada | Responsabilidade |
|--------|-----------------|
| `entity` | Domínio persistido. `Venda` e `ItemVenda` são imutáveis por construção — sem setters públicos, criação obrigatória via factory `criar()`. |
| `repository` | Acesso a dados via Spring Data JPA. Queries derivadas por convenção de nomes; nenhum SQL explícito. |
| `service` | Regras de negócio: validação de estoque, unicidade de e-mail/código de barras, orquestração transacional. Única camada com `@Transactional`. |
| `controller` MVC | Requisições web. Delega ao service, devolve views Thymeleaf. Sem lógica de negócio. |
| `controller` REST | API `/api/**` com Records imutáveis como DTOs. Validação com `@Valid` + fail-early guards. |
| `dto` | Records Java 21. Contratos imutáveis e sem boilerplate entre API e service. |
| `exception` | Hierarquia: `RegraDeNegocioException` → `EstoqueInsuficienteException`. `GlobalExceptionHandler` centraliza respostas MVC e REST sem expor stack trace. |
| `config` | Spring Security (BCrypt, roles ADMIN/OPERADOR, remember-me), filtro MDC, listener de autenticação. |

### Fluxo de uma venda

```
Browser / API
  → VendaController / VendaApiController
  → VendaService.registrarVenda()
      → UsuarioService.buscarAtivoPorId()    [guard: operador deve estar ativo]
      → ProdutoService.buscarAtivoPorId()    [guard: produto deve estar ativo]
      → validarEstoque()                     [lança EstoqueInsuficienteException se falhar]
      → ProdutoService.baixarEstoque()
      → Venda.criar() + ItemVenda.criar()    [factory imutável — sem setter público]
      → VendaRepository.save()
      → LOG: venda-concluida id= operador= total= itens=
```

### Decisões técnicas

| Decisão | Justificativa |
|---------|---------------|
| Records para DTOs | Contratos imutáveis; sem Lombok/boilerplate. |
| Factory em entidades | Impede construção em estado inválido. |
| Perfis Spring (`h2`, `postgres`, `test`) | Isolamento de ambiente sem alterar código. |
| BCrypt + validação pré-encoding | Senha mínima de 4 chars validada antes de codificar; hash de 60 chars não quebra `@Size`. |
| MDC requestId | Correlação de logs de uma mesma requisição em produção. |
| JaCoCo com exclusões de infra | `SecurityConfig` e `DataInitializer` excluídos — cobertura reflete exclusivamente lógica de negócio. |

---

## Execucao padronizada via Docker

Para garantir build e validacao da suite no mesmo ambiente, use a sequencia abaixo.

### 1. Build do projeto com Gradle em container

```bash
docker compose --profile build run --rm pdv-build
```

### 2. Testes unitarios e de integracao

```bash
docker compose --profile test run --rm pdv-test
```

### 3. Testes end-to-end com Selenium

```bash
docker compose --profile selenium run --rm pdv-selenium
```

### 4. Aplicacao publicada para validacao pos-deploy

```bash
docker compose up -d postgres pdv-app
docker compose --profile postdeploy run --rm pdv-postdeploy-test
```

Os containers de teste publicam logs customizados no inicio e no fim da execucao e exibem automaticamente os arquivos `build/reports/tests/resumo.txt` e `build/reports/tests/falhas_detalhadas.txt` quando existirem.
