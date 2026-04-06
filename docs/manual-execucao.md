# Manual de Execucao

## Rodar a aplicacao integrada

Use `gradle bootRun` para iniciar o sistema localmente com banco H2 em memoria.

## Rodar os workflows localmente por equivalencia

A esteira principal corresponde a `gradle clean check` seguido de `gradle bootJar`.

A validacao pos-deploy corresponde a iniciar a aplicacao publicada, definir a variavel `PDV_BASE_URL` e executar `gradle postDeployTest`.

## Interpretar a aba Actions

O workflow `CI Principal` mostra o estado do build, dos testes e da cobertura. Os artefatos publicados contem relatorios HTML para depuracao.

O workflow `Seguranca` concentra a revisao de dependencias em pull requests e a analise semantica com CodeQL.

O workflow `CD e Validacao Pos-Deploy` organiza o pacote, o deploy remoto opcional, a homologacao automatizada, os testes Selenium e o ZAP baseline. Os logs ficam publicados como artefato para consulta posterior.
