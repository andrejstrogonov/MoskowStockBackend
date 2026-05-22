.PHONY: build clean build-all build-linux build-windows help

# Build directory
BUILD_DIR := bin
GO := go
GOOS ?= $(OS)
GOARCH ?= amd64

# Detect OS if not specified
ifeq ($(OS),Windows_NT)
    GOOS := windows
    GOEXT := .exe
else
    GOOS := linux
    GOEXT :=
endif

# Version info
VERSION ?= 1.0.0
GIT_COMMIT := $(shell git rev-parse --short HEAD 2>/dev/null || echo "unknown")
BUILD_TIME := $(shell date -u '+%Y-%m-%dT%H:%M:%SZ')

LDFLAGS := -ldflags "-X main.Version=$(VERSION) -X main.GitCommit=$(GIT_COMMIT) -X main.BuildTime=$(BUILD_TIME)"

# Colors for output
GREEN := \033[0;32m
CYAN := \033[0;36m
NC := \033[0m # No Color

help:
	@echo "$(CYAN)Jenkins Helpers - Go Build System$(NC)"
	@echo ""
	@echo "Available targets:"
	@echo "  $(GREEN)build$(NC)          Build all tools for current OS"
	@echo "  $(GREEN)build-all$(NC)       Build all tools for Linux and Windows"
	@echo "  $(GREEN)build-linux$(NC)     Build all tools for Linux"
	@echo "  $(GREEN)build-windows$(NC)   Build all tools for Windows"
	@echo "  $(GREEN)clean$(NC)           Remove build artifacts"
	@echo "  $(GREEN)test$(NC)            Run tests"
	@echo "  $(GREEN)fmt$(NC)             Format code"
	@echo "  $(GREEN)vet$(NC)             Run go vet"

# Build all tools for current OS
build: build-docker-builder build-docker-pusher build-compose-deployer build-notifier
	@echo "$(GREEN)✅ All tools built successfully$(NC)"

# Build all tools for Linux
build-linux:
	@echo "$(CYAN)Building for Linux...$(NC)"
	GOOS=linux GOARCH=amd64 make build
	@echo "$(GREEN)✅ Linux builds completed$(NC)"

# Build all tools for Windows
build-windows:
	@echo "$(CYAN)Building for Windows...$(NC)"
	GOOS=windows GOARCH=amd64 make build
	@echo "$(GREEN)✅ Windows builds completed$(NC)"

# Build all tools for both platforms
build-all: build-linux build-windows
	@echo "$(GREEN)✅ All cross-platform builds completed$(NC)"

# Build individual tools
build-docker-builder:
	@echo "$(CYAN)Building build-docker$(NC)"
	$(GO) build -o $(BUILD_DIR)/build-docker$(GOEXT) $(LDFLAGS) ./cmd/build-docker

build-docker-pusher:
	@echo "$(CYAN)Building push-docker$(NC)"
	$(GO) build -o $(BUILD_DIR)/push-docker$(GOEXT) $(LDFLAGS) ./cmd/push-docker

build-compose-deployer:
	@echo "$(CYAN)Building deploy-compose$(NC)"
	$(GO) build -o $(BUILD_DIR)/deploy-compose$(GOEXT) $(LDFLAGS) ./cmd/deploy-compose

build-notifier:
	@echo "$(CYAN)Building notify-build$(NC)"
	$(GO) build -o $(BUILD_DIR)/notify-build$(GOEXT) $(LDFLAGS) ./cmd/notify-build

# Clean build artifacts
clean:
	@echo "$(CYAN)Cleaning build artifacts...$(NC)"
	rm -rf $(BUILD_DIR)
	$(GO) clean
	@echo "$(GREEN)✅ Clean completed$(NC)"

# Run tests
test:
	@echo "$(CYAN)Running tests...$(NC)"
	$(GO) test -v ./...

# Format code
fmt:
	@echo "$(CYAN)Formatting code...$(NC)"
	$(GO) fmt ./...
	@echo "$(GREEN)✅ Code formatted$(NC)"

# Run go vet
vet:
	@echo "$(CYAN)Running go vet...$(NC)"
	$(GO) vet ./...
	@echo "$(GREEN)✅ Vet checks passed$(NC)"

# Install tools locally
install: build
	@echo "$(CYAN)Installing tools...$(NC)"
	@mkdir -p $(HOME)/.local/bin 2>/dev/null || true
	@cp $(BUILD_DIR)/* $(HOME)/.local/bin/ 2>/dev/null || cp $(BUILD_DIR)/* /usr/local/bin/ 2>/dev/null || echo "Please copy files from $(BUILD_DIR) to your PATH"
	@echo "$(GREEN)✅ Tools installed$(NC)"

# Build Docker image for tools
docker-build:
	@echo "$(CYAN)Building Docker image for Jenkins helpers...$(NC)"
	docker build -t jenkins-helpers:latest .
	@echo "$(GREEN)✅ Docker image built$(NC)"

.DEFAULT_GOAL := help

