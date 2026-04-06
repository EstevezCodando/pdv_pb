#!/usr/bin/env bash
set -euo pipefail

TAREFA_GRADLE="${GRADLE_TASK:-test}"
ARGUMENTOS_GRADLE="${GRADLE_ARGS:---stacktrace --warning-mode all}"

printf '\n============================================================\n'
printf 'PDV :: Execucao Gradle em Docker\n'
printf 'Tarefa      : %s\n' "$TAREFA_GRADLE"
printf 'Diretorio   : %s\n' "$(pwd)"
printf 'Chrome bin  : %s\n' "${CHROME_BIN:-nao definido}"
printf 'Chromedriver: %s\n' "${CHROMEDRIVER_PATH:-nao definido}"
printf '============================================================\n\n'

gradle --version

set +e
gradle --no-daemon $TAREFA_GRADLE $ARGUMENTOS_GRADLE
STATUS=$?
set -e

printf '\n============================================================\n'
printf 'PDV :: Resultado da execucao\n'
printf 'Status: %s\n' "$STATUS"
printf '============================================================\n\n'

if [[ -f build/reports/tests/resumo.txt ]]; then
  printf 'Resumo consolidado:\n'
  cat build/reports/tests/resumo.txt
  printf '\n'
fi

if [[ -f build/reports/tests/falhas_detalhadas.txt ]]; then
  printf 'Falhas detalhadas:\n'
  cat build/reports/tests/falhas_detalhadas.txt
  printf '\n'
fi

printf 'Relatorios disponiveis em build/reports\n'
exit "$STATUS"
