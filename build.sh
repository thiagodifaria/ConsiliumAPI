#!/bin/bash

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }
warn() { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[✗]${NC} $1"; }

banner() {
    echo ""
    echo "   ConsiliumAPI - Build & Deploy      "
    echo ""
}

check_dependencies() {
    log "Verificando dependências..."

    command -v java >/dev/null 2>&1 || { error "Java não encontrado. Instale Java 17+"; exit 1; }
    command -v mvn >/dev/null 2>&1 || { error "Maven não encontrado. Instale Maven 3.8+"; exit 1; }
    command -v docker >/dev/null 2>&1 || { error "Docker não encontrado. Instale Docker"; exit 1; }

    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        error "Java 17+ é necessário. Versão atual: $JAVA_VERSION"
        exit 1
    fi

    success "Todas dependências encontradas (Java $JAVA_VERSION)"
}

clean() {
    log "Limpando builds anteriores..."
    mvn clean -q
    rm -rf target/ 2>/dev/null || true
    success "Limpeza concluída"
}

run_tests() {
    log "Executando testes..."
    mvn test

    TEST_COUNT=$(find target/surefire-reports -name "TEST-*.xml" 2>/dev/null | wc -l)
    success "Testes passaram ($TEST_COUNT suites de teste)"
}

build() {
    log "Construindo projeto..."
    mvn package -DskipTests -q

    if [ -f "target/*.jar" ]; then
        JAR_SIZE=$(du -h target/*.jar | cut -f1)
        success "Build concluído (JAR: $JAR_SIZE)"
    else
        success "Build concluído"
    fi
}

docker_cleanup() {
    log "Limpando ambiente Docker..."

    if [ -f "docker/docker-compose.yml" ]; then
        cd docker
        docker-compose down --volumes --remove-orphans 2>/dev/null || true
        cd ..
    fi

    docker images | grep consilium | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true

    success "Ambiente Docker limpo"
}

docker_build() {
    log "Construindo imagens Docker..."

    cd docker
    docker-compose build --no-cache
    cd ..

    success "Imagens Docker construídas"
}

docker_up() {
    log "Iniciando serviços..."

    cd docker
    docker-compose up -d
    cd ..

    log "Aguardando serviços ficarem prontos (max 60s)..."
    for i in {1..60}; do
        HEALTHY=$(docker ps --format '{{.Status}}' | grep -c 'healthy' || echo "0")
        if [ "$HEALTHY" -gt 0 ]; then
            success "Serviços iniciados e prontos!"
            return 0
        fi
        sleep 1
    done

    warn "Serviços iniciados mas health check ainda pendente"
}

show_info() {
    echo ""
    echo "              SERVIÇOS DISPONÍVEIS                    "
    echo " API:              http://localhost:8081              "
    echo " Swagger UI:       http://localhost:8081/swagger-ui   "
    echo " Actuator:         http://localhost:8081/actuator     "
    echo "                                                      "
    echo " Grafana:          http://localhost:3000              "
    echo " Prometheus:       http://localhost:9090              "
    echo " Redis Insight:    http://localhost:8001              "
    echo " RabbitMQ:         http://localhost:15672             "
    echo ""
    echo " Ver logs:       cd docker && docker-compose logs -f"
    echo " Parar:          cd docker && docker-compose down"
    echo " Health Check:   curl http://localhost:8081/actuator/health"
    echo ""
}

menu() {
    banner
    echo "Selecione uma opção:"
    echo ""
    echo "  1) Build completo (clean + test + build + docker)"
    echo "  2) Build rápido (sem testes)"
    echo "  3) Apenas rodar testes"
    echo "  4) Apenas Docker (rebuild + restart)"
    echo "  5) Limpar tudo (Docker + builds)"
    echo "  6) Ver logs dos serviços"
    echo "  7) Parar todos os serviços"
    echo "  8) Sair"
    echo ""
    read -p "Opção: " choice

    case $choice in
        1)
            check_dependencies
            clean
            run_tests
            build
            docker_cleanup
            docker_build
            docker_up
            show_info
            ;;
        2)
            check_dependencies
            clean
            build
            docker_cleanup
            docker_build
            docker_up
            show_info
            ;;
        3)
            check_dependencies
            run_tests
            ;;
        4)
            docker_cleanup
            docker_build
            docker_up
            show_info
            ;;
        5)
            docker_cleanup
            clean
            success "Limpeza completa realizada"
            ;;
        6)
            cd docker
            docker-compose logs -f
            ;;
        7)
            cd docker
            docker-compose down
            success "Todos os serviços foram parados"
            ;;
        8)
            exit 0
            ;;
        *)
            error "Opção inválida"
            exit 1
            ;;
    esac
}

if [ $# -eq 0 ]; then
    menu
else
    "$@"
fi