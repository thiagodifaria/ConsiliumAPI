#!/bin/bash

set -e

BLUE='\033[0;34m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m'

log() { echo -e "${BLUE}[TEST]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }

echo ""
echo -e "${CYAN}         ConsiliumAPI - Test Suite         ${NC}"
echo -e "${CYAN}            com Coverage Report            ${NC}"
echo ""

log "Executando testes com coverage..."
echo ""

mvn clean test jacoco:report

echo ""
success "Testes concluídos!"

TEST_COUNT=$(find target/surefire-reports -name "TEST-*.xml" 2>/dev/null | wc -l)
echo ""
echo "📊 Estatísticas:"
echo "   - Suites executadas: $TEST_COUNT"
echo "   - Coverage report:   target/site/jacoco/index.html"
echo ""

if [ -f "target/site/jacoco/index.html" ]; then
    COVERAGE=$(grep -o "Total[^%]*[0-9]\+%" target/site/jacoco/index.html | head -n 1 | grep -o "[0-9]\+%" || echo "?")
    echo "   - Coverage total:    $COVERAGE"
    echo ""
fi

if command -v xdg-open &> /dev/null; then
    log "Abrindo coverage report no browser..."
    xdg-open target/site/jacoco/index.html
elif command -v open &> /dev/null; then
    log "Abrindo coverage report no browser..."
    open target/site/jacoco/index.html
else
    echo "💡 Abra manualmente: target/site/jacoco/index.html"
fi

echo ""
success "Test suite finalizado!"
echo ""