# CI/CD Setup Guide

This project includes comprehensive CI/CD setup using both Jenkins and GitHub Actions.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Jenkins Setup](#jenkins-setup)
3. [GitHub Actions Setup](#github-actions-setup)
4. [Pipeline Stages](#pipeline-stages)
5. [Comparison](#comparison)
6. [Troubleshooting](#troubleshooting)

## Quick Start

### Choose Your CI/CD Platform

#### Option 1: Jenkins (Recommended for On-Premises)
- Best for: Self-hosted deployments
- Controls: Full control over infrastructure
- Location: `./jenkins/`

```bash
# For Windows (PowerShell)
.\jenkins\jenkins.ps1 setup

# For Linux/macOS (Bash)
chmod +x ./jenkins/jenkins.sh
./jenkins/jenkins.sh setup
```

#### Option 2: GitHub Actions (Recommended for Cloud)
- Best for: GitHub-hosted projects
- Simplicity: No server management
- Location: `./.github/workflows/ci-cd.yaml`

No setup needed beyond GitHub secrets configuration.

## Jenkins Setup

### Prerequisites

- Docker and Docker Compose
- At least 4GB RAM available
- Ports 8082, 9000, 5000, 5432 available

### Installation

1. **Start Jenkins with all services:**

   ```bash
   # Windows
   .\jenkins\jenkins.ps1 setup

   # Linux/macOS
   ./jenkins/jenkins.sh setup
   ```

2. **Access Jenkins:**
   - URL: http://localhost:8082/jenkins
   - Initial password will be displayed

3. **Configure Credentials:**

   ```
   Manage Jenkins → Manage Credentials → System → Global credentials
   ```

   Add:
   - Docker Hub credentials (ID: `docker-registry-credentials`)
   - GitHub token (ID: `github-credentials`)
   - SonarQube token (ID: `sonarqube-token`)

4. **Create Pipeline Job:**

   ```
   New Item → Pipeline
   - Name: MoskowStockBackend
   - Definition: Pipeline script from SCM
   - SCM: Git
   - Repository: https://github.com/your-repo/MoskowStockBackend.git
   - Script Path: Jenkinsfile
   ```

5. **Configure Webhooks (Optional):**

   In GitHub: Settings → Webhooks
   - Payload URL: http://your-jenkins-server/github-webhook/
   - Content type: application/json
   - Events: Push events, Pull request events

### Jenkins Commands

```bash
cd ./jenkins

# Start services
docker compose up -d

# Stop services
docker compose down

# View logs
docker compose logs -f jenkins

# Check status
docker compose ps

# Get initial password
docker compose exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Scale services
docker compose up -d --scale jenkins-agent=3
```

### Jenkins Shared Library

Located in `./jenkins/vars/`, provides reusable functions:

- `buildDockerImage()` - Build Docker image
- `deployWithCompose()` - Deploy using Docker Compose
- `pushDockerImage()` - Push to registry
- `notifyBuild()` - Send notifications

Usage:

```groovy
@Library('MoskowStockBackend') _

pipeline {
    stages {
        stage('Build Docker') {
            steps {
                buildDockerImage(imageName: 'moskowstock/backend', tag: env.BUILD_NUMBER)
            }
        }
    }
}
```

## GitHub Actions Setup

### Prerequisites

- GitHub repository access
- Docker Hub account (for pushing images)

### Configuration

1. **Add Secrets to GitHub:**

   Go to: Settings → Secrets and variables → Actions

   Add these secrets:
   ```
   DOCKER_USERNAME=<your-docker-username>
   DOCKER_PASSWORD=<your-docker-password>
   SONAR_HOST_URL=<your-sonarqube-url>
   SONAR_TOKEN=<your-sonarqube-token>
   SLACK_WEBHOOK_URL=<your-slack-webhook>
   
   # For deployment (optional)
   DEV_SERVER=<dev-server-ip>
   DEV_USER=<deploy-user>
   DEV_SSH_KEY=<private-ssh-key>
   PROD_SERVER=<prod-server-ip>
   PROD_USER=<deploy-user>
   PROD_SSH_KEY=<private-ssh-key>
   ```

2. **Workflow is automatically enabled:**

   File: `./.github/workflows/ci-cd.yaml`
   
   Triggers on:
   - Push to `main` or `develop` branches
   - Push of version tags (v*.*.*)
   - Pull requests to `main` or `develop`

3. **View Workflow Runs:**

   GitHub → Actions → CI/CD Pipeline

### Branch Protection Rules (Recommended)

1. Go to: Settings → Branches
2. Add rule for `main` branch:
   - Require status checks to pass before merging
   - Require code reviews before merging
   - Require branches to be up to date before merging

## Pipeline Stages

### Shared Stages (Both Jenkins & GitHub Actions)

#### 1. Checkout
- Checks out source code from Git

#### 2. Build
- Compiles Java code with Maven
- Runs: `./mvnw clean package -DskipTests`

#### 3. Unit Tests
- Runs all unit tests
- Generates JaCoCo coverage reports
- Publishes test results

#### 4. Code Quality Analysis
- Runs SonarQube analysis
- Checks code coverage
- Reports security issues

#### 5. Build Docker Image
- Builds Docker image from Dockerfile
- Tags with build number and latest

#### 6. Push to Registry
- Pushes image to Docker Hub
- Only on `main` branch
- Requires Docker credentials

#### 7. Deploy to Dev
- Deploys to development environment
- Only on `develop` branch
- Uses Docker Compose

#### 8. Deploy to Production
- Deploys to production environment
- Only on version tags (v1.0.0)
- Requires manual approval (Jenkins only)

#### 9. Health Check
- Verifies application is running
- Checks Swagger UI endpoint
- Retries with backoff

#### 10. Notifications
- Sends Slack notifications
- Sends email notifications
- Includes build status and logs

## Comparison

| Aspect | Jenkins | GitHub Actions |
|--------|---------|-----------------|
| **Setup** | More complex | Simple |
| **Maintenance** | Server management required | Managed by GitHub |
| **Cost** | Self-hosted (free) | Free for public repos, paid for private |
| **Scale** | Agents for parallelization | Built-in matrix builds |
| **Customization** | Very flexible | Limited to GitHub Actions |
| **On-premises** | Full support | Not available |
| **Cloud-native** | Optional | Primary use case |
| **Learning curve** | Steep | Gentle |

## Branch Strategy

### Development Flow

```
feature/feature-name
    ↓
develop → [deploy to dev] → [smoke tests]
    ↓
main → [push to registry] → [manual approval]
    ↓
v1.0.0 (tag) → [deploy to prod]
```

### Branch Rules

- `main`: Stable code, tagged for releases
- `develop`: Integration branch for features
- `feature/*`: Feature branches for new work
- `hotfix/*`: Emergency fixes for production

## Environment Variables

### Jenkins

Set in Jenkins UI or docker-compose.yaml:

```
JAVA_OPTS=-Xmx1024m -Xms512m
SONARQUBE_TOKEN=<token>
DOCKER_USERNAME=<username>
DOCKER_PASSWORD=<password>
```

### GitHub Actions

Set as Repository Secrets:

```
DOCKER_USERNAME=<username>
DOCKER_PASSWORD=<password>
SONARQUBE_TOKEN=<token>
SLACK_WEBHOOK_URL=<url>
```

## Troubleshooting

### Jenkins Issues

**Jenkins won't start**
```bash
docker compose logs jenkins
docker volume ls
docker volume rm jenkins_jenkins_home
```

**Docker build fails**
```bash
docker compose exec jenkins docker ps
docker system prune -a
```

**Maven build fails**
```bash
docker compose exec jenkins ./mvnw clean package -X
docker compose exec jenkins rm -rf ~/.m2/repository
```

### GitHub Actions Issues

**Workflow not running**
- Check: Actions tab for errors
- Verify: Branch protection rules
- Ensure: Secrets are configured

**Deployment fails**
- Check SSH keys are correct
- Verify server connectivity
- Review deployment logs in Actions

**Docker push fails**
- Verify Docker credentials in secrets
- Check Docker Hub token validity
- Ensure image name matches registry

## Performance Optimization

### Jenkins

```yaml
# Increase memory
JAVA_OPTS: -Xmx2048m -Xms1024m

# Use parallel agents
docker compose up -d --scale jenkins-agent=3

# Cache Maven dependencies
volumes:
  - ~/.m2:/root/.m2
```

### GitHub Actions

```yaml
# Use caching
- uses: actions/cache@v3
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven
    
# Use matrix builds
strategy:
  matrix:
    java-version: [17, 21]
```

## Security Recommendations

1. **Secrets Management**
   - Never commit credentials
   - Use credential providers
   - Rotate keys regularly

2. **Access Control**
   - Restrict branch protection
   - Require code reviews
   - Use RBAC for Jenkins

3. **Monitoring**
   - Monitor build logs
   - Alert on failures
   - Audit deployments

4. **Image Security**
   - Scan Docker images for vulnerabilities
   - Use image signing
   - Keep base images updated

## Integration Examples

### Slack Notifications

**Jenkins:**
```groovy
slackSend(
    channel: '#deployments',
    message: "Build ${BUILD_NUMBER} completed"
)
```

**GitHub Actions:**
```yaml
- uses: slackapi/slack-github-action@v1
  with:
    webhook-url: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### Email Notifications

**Jenkins:**
```groovy
emailext(
    subject: "Build ${BUILD_NUMBER} ${BUILD_STATUS}",
    body: "See ${BUILD_URL} for details",
    to: "team@example.com"
)
```

**GitHub Actions:**
```yaml
- name: Send Email
  uses: dawidd6/action-send-mail@v3
  with:
    server_address: ${{ secrets.EMAIL_SERVER }}
    to: team@example.com
    subject: Build ${{ job.status }}
```

## References

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Maven Build Tool](https://maven.apache.org/)

## Support

For issues or questions:

1. **Jenkins:** Check `./jenkins/README.md`
2. **GitHub Actions:** Check `./.github/workflows/ci-cd.yaml`
3. **General:** Review pipeline logs
4. **Community:** Visit Jenkins/GitHub Forums

---

**Last Updated**: 2024
**Version**: 1.0.0

