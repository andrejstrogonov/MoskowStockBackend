# Jenkins Go Utilities

This directory contains Go utilities for Jenkins CI/CD pipeline operations, replacing Groovy Shared Library functions.

## Overview

Four main utilities are provided:

- **build-docker** - Build Docker images
- **push-docker** - Push images to registries
- **deploy-compose** - Deploy using Docker Compose with health checks
- **notify-build** - Send build notifications to Slack/Email

## Directory Structure

```
jenkins/go/
├── Dockerfile                  # Multi-stage Docker build
├── Makefile                    # Build configuration
├── go.mod                      # Go module definition
├── go.sum                      # Dependency lock file (auto-generated)
├── bin/                        # Compiled binaries (generated)
│   ├── build-docker
│   ├── push-docker
│   ├── deploy-compose
│   └── notify-build
└── cmd/                        # Source code
    ├── build-docker/
    │   └── main.go
    ├── push-docker/
    │   └── main.go
    ├── deploy-compose/
    │   └── main.go
    └── notify-build/
        └── main.go
```

## Requirements

- Go 1.21 or later
- Docker
- Docker Compose
- Make (for building)

## Building

### Build all utilities for current OS

```bash
cd jenkins/go
make build
```

### Build for specific platform

```bash
# Linux x86_64
make build-linux

# Windows x86_64
make build-windows

# Both platforms
make build-all
```

### Individual tool builds

```bash
make build-docker-builder
make build-docker-pusher
make build-compose-deployer
make build-notifier
```

## Usage

### build-docker

Builds Docker images with support for build arguments.

```bash
./bin/build-docker \
  --image moskowstock/backend \
  --tag latest \
  --dockerfile Dockerfile \
  --build-args "JAVA_VERSION=21,BUILD_ENV=prod" \
  --verbose
```

**Options:**
- `--image` (required) - Docker image name
- `--tag` (default: latest) - Image tag
- `--dockerfile` (default: Dockerfile) - Path to Dockerfile
- `--build-args` - Comma-separated build arguments
- `--verbose` - Enable verbose output

### push-docker

Pushes Docker images to registry with authentication.

```bash
./bin/push-docker \
  --registry docker.io \
  --image moskowstock/backend \
  --tag latest \
  --username your-username \
  --password your-password \
  --verbose
```

**Options:**
- `--registry` (default: docker.io) - Docker registry
- `--image` (required) - Docker image name
- `--tag` (default: latest) - Image tag
- `--username` (required) - Registry username
- `--password` (required) - Registry password
- `--skip-logout` - Don't logout after push
- `--verbose` - Enable verbose output

**Note:** Password can be provided via command line or `DOCKER_PASSWORD` environment variable.

### deploy-compose

Deploys application using Docker Compose with automatic health checks.

```bash
./bin/deploy-compose \
  --environment dev \
  --compose-files "compose.yaml,compose.prod.yaml" \
  --healthcheck-url "http://localhost:8080/swagger-ui.html" \
  --max-wait 120 \
  --verbose
```

**Options:**
- `--environment` (default: dev) - Environment name
- `--compose-files` (default: compose.yaml) - Comma-separated compose files
- `--healthcheck-url` (default: http://localhost:8080/swagger-ui.html) - Health check URL
- `--max-wait` (default: 120) - Max time to wait for health (seconds)
- `--verbose` - Enable verbose output

**Features:**
- Pulls latest images
- Stops and removes old containers
- Starts new services
- Performs automatic health checks
- Displays service status

### notify-build

Sends build status notifications to Slack and/or email.

```bash
./bin/notify-build \
  --status SUCCESS \
  --job-name "MoskowStock Build" \
  --build-number "123" \
  --build-url "http://jenkins:8080/job/123" \
  --slack-webhook "https://hooks.slack.com/..." \
  --slack-channel "#deployments" \
  --email-recipients "team@example.com,admin@example.com" \
  --verbose
```

**Options:**
- `--status` (default: UNKNOWN) - Build status (SUCCESS, FAILURE, UNSTABLE)
- `--job-name` (default: Unknown Job) - Jenkins job name
- `--build-number` - Build number
- `--build-url` (required) - Build URL
- `--slack-webhook` - Slack webhook URL (or SLACK_WEBHOOK_URL env var)
- `--slack-channel` - Slack channel name
- `--email-recipients` - Comma-separated email addresses
- `--verbose` - Enable verbose output

**Note:** Slack webhook can be provided via `SLACK_WEBHOOK_URL` environment variable.

## Using in Jenkins

### Option 1: Pre-built binaries

Include compiled binaries in Jenkins Docker image:

```dockerfile
COPY jenkins/go/bin/* /usr/local/bin/
```

### Option 2: Build during pipeline

```groovy
stage('Build Tools') {
    steps {
        sh '''
            cd jenkins/go
            make build
            export PATH=${WORKSPACE}/jenkins/go/bin:$PATH
        '''
    }
}
```

### Example Jenkinsfile usage

```groovy
stage('Build Docker Image') {
    steps {
        sh '''
            build-docker \
              --image ${REGISTRY}/${IMAGE_NAME} \
              --tag ${BUILD_NUMBER} \
              --verbose
        '''
    }
}

stage('Deploy') {
    steps {
        sh '''
            deploy-compose \
              --environment dev \
              --compose-files "compose.yaml"
        '''
    }
}

post {
    success {
        sh '''
            notify-build \
              --status SUCCESS \
              --job-name "${JOB_NAME}" \
              --build-number "${BUILD_NUMBER}" \
              --build-url "${BUILD_URL}"
        '''
    }
}
```

See `Jenkinsfile.go` for a complete example pipeline.

## Docker Image

Build Docker image containing all utilities:

```bash
cd jenkins/go
make docker-build
```

This creates `jenkins-helpers:latest` image with all tools pre-installed.

Use in Jenkins Docker Compose:

```yaml
services:
  jenkins:
    image: jenkins/jenkins:lts
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    environment:
      - PATH=/usr/local/bin:/usr/bin:/bin
```

## Development

### Code formatting

```bash
make fmt
```

### Run linter

```bash
make vet
```

### Run tests

```bash
make test
```

### Clean build artifacts

```bash
make clean
```

## Advantages over Groovy

| Aspect | Groovy | Go |
|--------|--------|-----|
| **Performance** | Slower (JVM startup) | Faster (compiled binary) |
| **Dependencies** | Heavy (Groovy libraries) | Minimal (Go stdlib) |
| **Cross-platform** | JVM-dependent | Native binaries |
| **Testability** | Groovy-specific tests | Standard Go tests |
| **Error handling** | Less explicit | Explicit error handling |
| **Compilation** | Interpreted | Pre-compiled |
| **Learning curve** | Requires Groovy knowledge | Standard Go knowledge |

## Troubleshooting

### Build fails with "command not found"

Ensure Go is installed and in PATH:
```bash
go version
```

### Docker command not found in tools

Ensure Docker is in PATH inside Jenkins:
```bash
docker --version
```

### Tools not executing

Check file permissions:
```bash
chmod +x bin/*
```

### Slack notification fails

Verify webhook URL:
```bash
echo $SLACK_WEBHOOK_URL
```

## Performance Metrics

Typical execution times (on 4-core CPU):

- **build-docker** - 30-60 seconds (Docker build time)
- **push-docker** - 10-30 seconds (network dependent)
- **deploy-compose** - 40-80 seconds (includes health checks)
- **notify-build** - <1 second

## Migration from Groovy

### Old Groovy code
```groovy
buildDockerImage(imageName: 'app', tag: 'latest')
```

### New Go code
```groovy
sh 'build-docker --image app --tag latest'
```

See `Jenkinsfile.go` for complete migration example.

## Contributing

To add new tools:

1. Create new directory in `cmd/`
2. Implement `main.go`
3. Add build target in `Makefile`
4. Update this README

## License

These utilities are provided for internal use with the MoskowStock Backend project.

## References

- [Go Documentation](https://golang.org/doc/)
- [Docker CLI Reference](https://docs.docker.com/engine/reference/commandline/cli/)
- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)

---

**Version:** 1.0.0  
**Last Updated:** 2024  
**Go Version:** 1.21+

