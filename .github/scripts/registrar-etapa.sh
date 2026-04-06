#!/usr/bin/env bash
# =============================================================
# registrar-etapa.sh
# Registra uma etapa do pipeline com timestamp no log do GitHub Actions.
# Facilita a depuracao e rastreio de progresso por futuros desenvolvedores.
#
# Uso: bash .github/scripts/registrar-etapa.sh "Descricao da etapa"
# =============================================================

set -euo pipefail

ETAPA="${1:-Etapa sem descricao}"
TIMESTAMP=$(date -u '+%Y-%m-%dT%H:%M:%SZ')

echo "::notice title=Pipeline PDV::[$TIMESTAMP] $ETAPA"
echo "---"
echo "Etapa : $ETAPA"
echo "Hora  : $TIMESTAMP"
echo "Commit: ${GITHUB_SHA:-local}"
echo "Run   : ${GITHUB_RUN_NUMBER:-0}"
echo "---"
