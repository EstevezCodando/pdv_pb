#!/usr/bin/env bash
# =============================================================
# aguardar-aplicacao.sh
# Aguarda a aplicacao responder no endpoint de health check.
# Util para garantir que o deploy foi concluido antes dos testes.
#
# Uso: bash .github/scripts/aguardar-aplicacao.sh <URL> [max_tentativas] [intervalo_seg]
# Exemplo: bash .github/scripts/aguardar-aplicacao.sh http://localhost:8080/actuator/health 30 5
# =============================================================

set -euo pipefail

URL="${1:?Informe a URL do health check}"
MAX_TENTATIVAS="${2:-30}"
INTERVALO="${3:-5}"

echo "Aguardando aplicacao em: $URL"
echo "Maximo de tentativas   : $MAX_TENTATIVAS"
echo "Intervalo entre checks : ${INTERVALO}s"

for i in $(seq 1 "$MAX_TENTATIVAS"); do
    HTTP_STATUS=$(curl --silent --output /dev/null --write-out "%{http_code}" "$URL" || echo "000")

    if [[ "$HTTP_STATUS" == "200" ]]; then
        echo "::notice::Aplicacao respondeu com HTTP $HTTP_STATUS apos $i tentativa(s)"
        exit 0
    fi

    echo "Tentativa $i/$MAX_TENTATIVAS — HTTP $HTTP_STATUS — aguardando ${INTERVALO}s..."
    sleep "$INTERVALO"
done

echo "::error::Aplicacao nao respondeu apos $MAX_TENTATIVAS tentativas (ultima resposta: HTTP $HTTP_STATUS)"
exit 1
