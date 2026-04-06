# Manual de execução

## Como rodar a aplicação integrada

Na raiz do projeto, execute:

```bash
gradle bootRun
```

A aplicação sobe em `http://localhost:8080` com o perfil padrão H2 para execução local.

## Como executar os testes

Para build, testes e verificação de cobertura:

```bash
gradle clean check
```

Para executar apenas os testes end-to-end:

```bash
gradle seleniumTest
```

## Como interpretar os workflows do GitHub Actions

O workflow `CI` executa compilação, suíte principal de testes e verificação de cobertura. Quando falha, os logs do Gradle mostram os testes quebrados com stack trace completo, e os relatórios HTML de testes e cobertura são publicados como artefatos.

O workflow `E2E Selenium` fica separado e com disparo manual. Essa decisão reduz ruído no pipeline principal e preserva previsibilidade na validação de branches e pull requests.

## Runners adotados

Foi adotado `ubuntu-latest` hospedado pelo GitHub. Para este projeto, runner auto-hospedado não traz ganho proporcional, porque não há dependência obrigatória de rede privada, banco persistente dedicado ou infraestrutura específica do time.

## Evidências geradas

Os relatórios locais ficam em:

```text
build/reports/tests/test
build/reports/jacoco/test/html
```
