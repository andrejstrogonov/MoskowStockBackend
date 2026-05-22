#!/bin/bash

# Quick Jenkins Setup and Start Script for Linux/macOS
# Usage: ./jenkins.sh [start|stop|restart|logs|status|setup]

set -e

JENKINS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$JENKINS_DIR")"
ACTION="${1:-start}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Print header
echo -e "${CYAN}╔════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║   MoskowStock Jenkins Setup Script     ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════╝${NC}"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed${NC}"
    echo -e "${YELLOW}Please install Docker from: https://www.docker.com/${NC}"
    exit 1
fi

DOCKER_VERSION=$(docker --version)
echo -e "${GREEN}✅ Docker found: $DOCKER_VERSION${NC}"

# Check if Docker Compose is available
if ! docker compose version &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not available${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker Compose is available${NC}"

# Functions
start_jenkins() {
    echo -e "\n${CYAN}▶️  Starting Jenkins and supporting services...${NC}"

    cd "$JENKINS_DIR"

    docker compose up -d

    if [ $? -eq 0 ]; then
        echo -e "\n${GREEN}✅ Services started successfully!${NC}"

        echo -e "\n${CYAN}📊 Service URLs:${NC}"
        echo -e "  ${YELLOW}Jenkins:     http://localhost:8082/jenkins${NC}"
        echo -e "  ${YELLOW}SonarQube:   http://localhost:9000${NC}"
        echo -e "  ${YELLOW}Docker Hub:  http://localhost:5000/v2/_catalog${NC}"
        echo -e "  ${YELLOW}PostgreSQL:  localhost:5432${NC}"

        echo -e "\n${CYAN}⏳ Waiting for Jenkins to be ready...${NC}"

        local max_attempts=60
        local attempt=0

        while [ $attempt -lt $max_attempts ]; do
            if curl -s -f "http://localhost:8082/jenkins" &> /dev/null; then
                echo -e "${GREEN}✅ Jenkins is ready!${NC}"

                echo -e "\n${CYAN}🔑 Initial Admin Password:${NC}"
                docker compose exec -T jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || \
                    echo -e "${YELLOW}Run: docker compose -f jenkins/compose.yaml exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword${NC}"

                return 0
            fi

            sleep 2
            attempt=$((attempt + 1))
            echo -e "  ${CYAN}Attempt $attempt/$max_attempts...${NC}"
        done

        echo -e "\n${YELLOW}⚠️  Jenkins is starting up. Please wait a bit longer.${NC}"
        echo -e "${YELLOW}You can check the logs with: docker compose logs -f jenkins${NC}"
    else
        echo -e "${RED}❌ Failed to start services${NC}"
        exit 1
    fi
}

stop_jenkins() {
    echo -e "\n${CYAN}⏹️  Stopping Jenkins and supporting services...${NC}"

    cd "$JENKINS_DIR"

    docker compose down

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Services stopped successfully!${NC}"
    else
        echo -e "${RED}❌ Failed to stop services${NC}"
        exit 1
    fi
}

restart_jenkins() {
    stop_jenkins
    sleep 3
    start_jenkins
}

show_logs() {
    echo -e "\n${CYAN}📋 Showing Jenkins logs (follow mode). Press Ctrl+C to exit.${NC}"

    cd "$JENKINS_DIR"
    docker compose logs -f jenkins
}

show_status() {
    echo -e "\n${CYAN}📊 Service Status:${NC}"

    cd "$JENKINS_DIR"
    docker compose ps
}

setup_jenkins() {
    echo -e "\n${CYAN}⚙️  Running initial Jenkins setup...${NC}"

    echo -e "\n${CYAN}✓ Step 1: Starting services...${NC}"
    start_jenkins

    echo -e "\n${GREEN}✓ Step 2: Configuration complete!${NC}"

    echo -e "\n${CYAN}Next steps:${NC}"
    echo -e "  ${YELLOW}1. Open Jenkins: http://localhost:8082/jenkins${NC}"
    echo -e "  ${YELLOW}2. Use the initial admin password shown above${NC}"
    echo -e "  ${YELLOW}3. Follow the setup wizard${NC}"
    echo -e "  ${YELLOW}4. Install recommended plugins${NC}"
    echo -e "  ${YELLOW}5. Create your first admin user${NC}"
    echo -e "  ${YELLOW}6. Add credentials (Docker, GitHub, etc.)${NC}"
    echo -e "  ${YELLOW}7. Create a new Pipeline job${NC}"
    echo -e "\n${YELLOW}📖 For detailed instructions, see: $JENKINS_DIR/README.md${NC}"
}

# Execute action
case $ACTION in
    start)
        start_jenkins
        ;;
    stop)
        stop_jenkins
        ;;
    restart)
        restart_jenkins
        ;;
    logs)
        show_logs
        ;;
    status)
        show_status
        ;;
    setup)
        setup_jenkins
        ;;
    *)
        echo -e "${RED}Unknown action: $ACTION${NC}"
        echo "Available actions: start, stop, restart, logs, status, setup"
        exit 1
        ;;
esac

echo -e "\n${NC}"

