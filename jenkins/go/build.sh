#!/bin/bash

# Bash script to build Go utilities for Jenkins
# Usage: ./build.sh [build|build-all|build-linux|build-windows|clean|test|fmt|vet|help]

set -e

ACTION="${1:-help}"
GOOS="${GOOS:=}"
GOARCH="${GOARCH:=amd64}"

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Functions
show_help() {
    echo -e "${CYAN}Jenkins Go Utilities Build Script${NC}"
    echo "======================================"
    echo ""
    echo "Usage: ./build.sh [command]"
    echo ""
    echo -e "${YELLOW}Commands:${NC}"
    echo "  build           Build all tools for current OS (default)"
    echo "  build-all       Build for Linux and Windows"
    echo "  build-linux     Build for Linux"
    echo "  build-windows   Build for Windows"
    echo "  clean           Remove build artifacts"
    echo "  test            Run tests"
    echo "  fmt             Format code"
    echo "  vet             Run go vet"
    echo "  help            Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./build.sh build-all           # Build for all platforms"
    echo "  ./build.sh build-linux         # Build Linux binaries"
    echo ""
}

invoke_build() {
    local target_os="${1:=}"

    if [ -n "$target_os" ]; then
        echo -e "${CYAN}Building for $target_os...${NC}"
        export GOOS="$target_os"
        export GOARCH="$GOARCH"
    else
        local current_os
        current_os=$(uname -s | tr '[:upper:]' '[:lower:]')
        echo -e "${CYAN}Building for current OS ($current_os)...${NC}"
    fi

    # Ensure bin directory exists
    mkdir -p bin

    # Build each utility
    local tools=(
        "build-docker:cmd/build-docker"
        "push-docker:cmd/push-docker"
        "deploy-compose:cmd/deploy-compose"
        "notify-build:cmd/notify-build"
    )

    for tool_entry in "${tools[@]}"; do
        IFS=':' read -r tool_name tool_path <<< "$tool_entry"

        echo -ne "${CYAN}Building ${YELLOW}$tool_name${NC}..."

        if go build -o "bin/$tool_name" "$tool_path"; then
            echo -e " ${GREEN}✅${NC}"
        else
            echo -e " ${RED}❌${NC}"
            return 1
        fi
    done

    # Unset environment variables
    if [ -n "$target_os" ]; then
        unset GOOS
        unset GOARCH
    fi

    return 0
}

invoke_clean() {
    echo -e "${CYAN}Cleaning build artifacts...${NC}"

    if [ -d "bin" ]; then
        rm -rf bin
        echo -e "${GREEN}✅ Removed bin directory${NC}"
    fi

    go clean
    echo -e "${GREEN}✅ Clean completed${NC}"
}

invoke_test() {
    echo -e "${CYAN}Running tests...${NC}"
    if go test -v ./...; then
        echo -e "${GREEN}✅ Tests passed${NC}"
    else
        echo -e "${RED}❌ Tests failed${NC}"
        return 1
    fi
}

invoke_format() {
    echo -e "${CYAN}Formatting code...${NC}"
    go fmt ./...
    echo -e "${GREEN}✅ Code formatted${NC}"
}

invoke_vet() {
    echo -e "${CYAN}Running go vet...${NC}"
    go vet ./... && echo -e "${GREEN}✅ Vet checks passed${NC}" || echo -e "${YELLOW}⚠️  Vet found issues${NC}"
}

# Check if Go is installed
if ! command -v go &> /dev/null; then
    echo -e "${RED}❌ Go is not installed or not in PATH${NC}"
    exit 1
fi

go_version=$(go version)
echo -e "${GREEN}✅ Go found: $go_version${NC}"

# Execute action
case $ACTION in
    build)
        if invoke_build; then
            echo -e "\n${GREEN}✅ All tools built successfully${NC}\n"
        else
            exit 1
        fi
        ;;
    build-all)
        echo -e "${CYAN}Building for all platforms...${NC}"
        if invoke_build "linux" && invoke_build "windows"; then
            echo -e "\n${GREEN}✅ All cross-platform builds completed${NC}\n"
        else
            exit 1
        fi
        ;;
    build-linux)
        if invoke_build "linux"; then
            echo -e "\n${GREEN}✅ Linux builds completed${NC}\n"
        else
            exit 1
        fi
        ;;
    build-windows)
        if invoke_build "windows"; then
            echo -e "\n${GREEN}✅ Windows builds completed${NC}\n"
        else
            exit 1
        fi
        ;;
    clean)
        invoke_clean
        ;;
    test)
        invoke_test
        ;;
    fmt)
        invoke_format
        ;;
    vet)
        invoke_vet
        ;;
    help)
        show_help
        ;;
    *)
        echo -e "${RED}Unknown action: $ACTION${NC}"
        show_help
        exit 1
        ;;
esac

