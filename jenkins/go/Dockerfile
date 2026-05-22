# Multi-stage builder for Jenkins helper tools

# Stage 1: Build the tools
FROM golang:1.21-alpine AS builder

WORKDIR /workspace

# Install dependencies
RUN apk add --no-cache make git

# Copy go mod files
COPY go.mod go.mod

# Copy source code
COPY cmd ./cmd

# Build all tools
RUN make build-linux

# Stage 2: Create final image with all tools
FROM alpine:latest

# Install runtime dependencies
RUN apk add --no-cache \
    docker-cli \
    docker-compose \
    curl \
    ca-certificates

# Copy built tools from builder
COPY --from=builder /workspace/bin/* /usr/local/bin/

# Verify installations
RUN build-docker --help && \
    push-docker --help && \
    deploy-compose --help && \
    notify-build --help

# Create non-root user
RUN addgroup -S jenkins && adduser -S jenkins -G jenkins
USER jenkins

WORKDIR /workspace

ENTRYPOINT ["/bin/sh"]
CMD ["-c", "echo 'Jenkins helper tools are available. Use --help for usage information.'"]

