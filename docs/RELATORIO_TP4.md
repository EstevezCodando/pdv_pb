# Relatório TP4

## Objetivo da refatoração

A refatoração foi direcionada pelos requisitos do TP4 e pelos requisitos não funcionais de clareza, extensibilidade, cobertura mínima, previsibilidade do build e facilidade de depuração.

## Limpeza estrutural aplicada

A principal dualidade removida foi a coexistência entre Maven e Gradle. O projeto passou a ter um único caminho oficial de build, teste e execução. Também foram removidos artefatos gerados, caches locais e documentação conflitante.

## Refatorações relevantes

As validações transversais de identificadores foram centralizadas em `ValidadorDeIdentificador`, reduzindo duplicação nos endpoints REST. Os perfis de usuário foram centralizados em `PerfisUsuario`, reduzindo string literal espalhada na camada web. O `ProdutoController` passou a usar a mesma estratégia de preparação de formulário já adotada no módulo de usuários, reduzindo repetição e deixando o fluxo MVC mais consistente.

Os serviços de usuário e produto continuam concentrando regra de negócio e persistência coordenada, mantendo controllers finos e testáveis. O `ResumoIntegradoService` permanece como ponto explícito de integração entre os dois módulos, evitando que controllers ou templates façam composição de dados diretamente.

## Integração dos sistemas

A integração foi consolidada por meio da home com resumo operacional e pelo endpoint `GET /api/integracao/resumo`. Assim, os módulos deixam de existir como CRUDs isolados e passam a compor uma aplicação única com visão integrada.

## Testes e cobertura

A cobertura mínima de 85% foi mantida no Gradle com JaCoCo. Os logs de teste foram configurados para mostrar falhas com mais contexto, o que atende ao requisito de erros compreensíveis no CI. Os testes Selenium foram mantidos em tarefa e workflow separados para preservar estabilidade na esteira principal.

## Runners

Foi mantido `ubuntu-latest` hospedado pelo GitHub por simplicidade operacional, baixo custo de manutenção e compatibilidade suficiente para este projeto. A adoção de runner auto-hospedado não se justifica pelos requisitos atuais.
