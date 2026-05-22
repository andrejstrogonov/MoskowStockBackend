# Jenkins Setup and Configuration Guide

## Overview
This document describes the Jenkins CI/CD pipeline setup for the MoskowStock Backend application.

## Prerequisites

### System Requirements
- Jenkins 2.387 or later
- Docker and Docker Compose
- Maven 3.9+
- Java 21 (for compilation)

### Jenkins Plugins Required

Install the following plugins in Jenkins:
- **Pipeline** (already included in Jenkins)
- **Docker Pipeline** - for Docker operations
- **Docker** - Docker API plugin
- **Email Extension** - for email notifications
- **Jacoco** - for code coverage reports
- **JUnit** - for test reporting
- **SonarQube Scanner** - for code quality analysis
- **Timestamper** - for timestamp logs
- **AnsiColor** - for colored console output
- **Git** - for Git operations

## Installation Steps

### 1. Install Required Plugins

Via Jenkins UI:
```
Manage Jenkins → Manage Plugins → Available Plugins
Search and install:
- Pipeline
- Docker Pipeline
- Email Extension
- Jacoco
- SonarQube Scanner
- AnsiColor
```

Or via Jenkins Script Console:
```groovy
Jenkins.instance.pluginManager.plugins.each {
  println "${it.getShortName()}:${it.getVersion()}"
}
```

### 2. Configure Docker Registry Credentials

Via Jenkins UI:
```
Manage Jenkins → Manage Credentials → System → Global credentials
Add Credentials:
- Kind: Username with password
- ID: docker-registry-credentials
- Username: <your-docker-username>
- Password: <your-docker-password>
```

### 3. Configure SonarQube Integration (Optional)

```
Manage Jenkins → Configure System → SonarQube Servers
Name: SonarQube
Server URL: http://sonarqube:9000
Server authentication token: <your-sonarqube-token>
```

### 4. Configure Email Notifications

```
Manage Jenkins → Configure System → Extended E-mail Notification
SMTP Server: <your-smtp-server>
SMTP Port: 587
Use SMTP Authentication: true
Username: <your-email>
Password: <your-email-password>
```

### 5. Create Pipeline Job

1. Create a new item: **New Item**
2. Enter name: `MoskowStockBackend`
3. Select: **Pipeline**
4. Click OK
5. Under Pipeline section, select:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/your-repo/MoskowStockBackend.git`
   - Branch: `**` (all branches)
   - Script Path: `Jenkinsfile`

## Pipeline Stages

### 1. **Checkout**
Checks out the source code from Git repository.

### 2. **Setup**
Prepares the environment and makes Maven wrapper executable.

### 3. **Build**
Compiles the Java code and creates the package using Maven.

### 4. **Unit Tests**
Runs unit tests and generates coverage reports with JaCoCo.

### 5. **Code Quality Analysis**
Analyzes code using SonarQube scanner (optional).

### 6. **Build Docker Image**
Creates a Docker image from the Dockerfile.

### 7. **Push to Registry**
Pushes the image to Docker registry (only on `main` branch).

### 8. **Deploy to Dev**
Deploys to development environment using Docker Compose (only on `develop` branch).

### 9. **Deploy to Production**
Deploys to production with manual approval (only on version tags like `v1.0.0`).

### 10. **Health Check**
Verifies that the application is running correctly.

### 11. **Smoke Tests**
Runs basic smoke tests to ensure application responsiveness.

## Branch Strategy

- **main**: Production-ready code. Triggers Docker push and can be deployed to production with approval.
- **develop**: Development branch. Triggers deployment to dev environment.
- **feature/***: Feature branches. Runs tests only.

## Tag-Based Deployment

To deploy to production, create a tag:
```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

This will trigger the production deployment stage with manual approval.

## Environment Variables

Set these in Jenkins global environment or in the job:

```
REGISTRY=docker.io
IMAGE_NAME=moskowstock/backend
SONARQUBE_TOKEN=<your-sonarqube-token>
DB_PASSWORD=<your-db-password>
```

## Running Locally

To test the pipeline locally with Docker:

```bash
# Build
./mvnw clean package -DskipTests

# Test
./mvnw test

# Build Docker image
docker build -t moskowstock/backend:latest .

# Run with Docker Compose
docker compose -f compose.yaml up -d
```

## Troubleshooting

### Pipeline fails at Docker stage
- Ensure Docker daemon is running
- Check Docker permissions: `docker ps` should work without sudo
- Verify Docker credentials are configured correctly

### Maven build fails
- Ensure Java 21 is installed
- Check Maven settings: `./mvnw --version`
- Clear Maven cache: `rm -rf ~/.m2/repository`

### Tests fail
- Check logs: `./mvnw test -X`
- Ensure PostgreSQL and RabbitMQ are running
- Check database credentials in application.properties

### SonarQube integration fails
- Verify SonarQube server is running: `curl http://localhost:9000`
- Check authentication token is valid
- Ensure project key matches configuration

## Jenkins Shared Library

Create a Jenkins Shared Library for reusable code:

Location: `vars/` directory in SCM with Jenkins configuration

Example functionality:
- Docker image build verification
- Deployment scripts
- Notification helpers

See `jenkins/` directory for shared library examples.

## Monitoring and Logs

### View build logs
```
Jenkins UI → Job → Build → Console Output
```

### Docker logs
```bash
docker logs <container-id>
docker compose logs -f app
```

### Application logs
```bash
docker exec <container-id> tail -f /var/log/application.log
```

## Rollback Procedure

If deployment fails:

1. Identify the previous working build number
2. Click "Build Now" or go to that build
3. Click "Replay" to re-run that build
4. Or manually deploy previous image:

```bash
docker pull docker.io/moskowstock/backend:<previous-build-number>
docker tag docker.io/moskowstock/backend:<previous-build-number> docker.io/moskowstock/backend:latest
docker compose up -d
```

## Performance Optimization

### For faster builds:
- Enable Maven offline mode: `./mvnw -o clean package`
- Use Jenkins agents to parallelize tests
- Cache dependencies using Maven settings

### For faster deployments:
- Use Docker layer caching
- Minimize Docker image size
- Pre-pull base images

## Security Best Practices

1. **Use Credentials Plugin** for storing secrets
2. **Enable Jenkins authentication** and authorization
3. **Use branch protection** on main branch
4. **Sign tags** for production releases
5. **Scan Docker images** for vulnerabilities
6. **Use private Docker registry** for sensitive images
7. **Rotate credentials** regularly

## References

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Docker Pipeline Plugin](https://plugins.jenkins.io/docker-workflow/)
- [SonarQube Integration](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-jenkins/)

