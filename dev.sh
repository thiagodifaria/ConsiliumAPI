#!/bin/bash

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

log() { echo -e "${BLUE}[DEV]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }

echo ""
echo -e "${CYAN}╔═══════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║   ConsiliumAPI - Modo Desenvolvimento    ║${NC}"
echo -e "${CYAN}║         Hot Reload Ativado               ║${NC}"
echo -e "${CYAN}╚═══════════════════════════════════════════╝${NC}"
echo ""

log "Iniciando dependências no Docker..."

# Inicia apenas dependências (sem a aplicação)
cd docker
docker-compose up -d postgres redis rabbitmq grafana loki tempo prometheus

success "Dependências iniciadas"
log "Aguardando serviços ficarem prontos..."

# Aguarda PostgreSQL
for i in {1..30}; do
    if docker exec consilium-postgres pg_isready -U postgres >/dev/null 2>&1; then
        success "PostgreSQL pronto"
        break
    fi
    sleep 1
done

# Aguarda Redis
for i in {1..30}; do
    if docker exec consilium-redis redis-cli ping >/dev/null 2>&1; then
        success "Redis pronto"
        break
    fi
    sleep 1
done

# Aguarda RabbitMQ
for i in {1..30}; do
    if docker exec consilium-rabbitmq rabbitmq-diagnostics ping >/dev/null 2>&1; then
        success "RabbitMQ pronto"
        break
    fi
    sleep 1
done

cd ..

echo ""
echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo -e "${GREEN}Dependências prontas!${NC}"
echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo ""
echo "🔧 Spring Boot DevTools ativo (hot reload)"
echo "📊 Actuator: http://localhost:8080/actuator"
echo "📝 Swagger:  http://localhost:8080/swagger-ui"
echo ""
echo "💡 Dica: Edite código e salve. A aplicação recarrega automaticamente!"
echo ""

# Spring Boot DevTools ativa hot reload automaticamente
log "Iniciando aplicação em modo DEV..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev
