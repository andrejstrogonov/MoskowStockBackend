# Jenkins CI/CD Setup  

This directory contains all Jenkins-related configuration and pipeline definitions for the MoskowStock Backend project.

## Directory Structure

```
jenkins/
├── vars/                          # Jenkins Shared Library functions
│   ├── buildDockerImage.groovy   # Docker image building function
│   ├── deployWithCompose.groovy  # Docker Compose deployment function
│   ├── pushDockerImage.groovy    # Docker image push function
│   └── notifyBuild.groovy        # Build notification function
├── casc.yaml                      # Jenkins Configuration as Code
├── compose.yaml                   # Docker Compose for Jenkins infrastructure
├── plugins.txt                    # List of required Jenkins plugins
└── README.md                      # This file
```

## Quick Start

### Option 1: Run Jenkins with Docker Compose (Recommended)

```bash
cd jenkins

# Start all services (Jenkins, SonarQube, PostgreSQL, Docker Registry)
docker compose up -d

# View logs
docker compose logs -f jenkins

# Stop services
docker compose down
```

Jenkins will be available at: **http://localhost:8082/jenkins**

### Option 2: Run Jenkins on Host Machine

1. Install Jenkins from [official website](https://www.jenkins.io/)
2. Follow the setup wizard
3. Install plugins from `jenkins/plugins.txt`
4. Import configuration from `jenkins/casc.yaml`

## Initial Setup

### 1. Access Jenkins UI

Navigate to: `http://localhost:8082/jenkins` (or `http://localhost:8080` if running locally)

### 2. Unlock Jenkins

```bash
# Get initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 3. Create First Admin User

Follow Jenkins setup wizard to create your first admin user.

### 4. Install Recommended Plugins

Jenkins will suggest plugins during setup. Install at least:
- Pipeline
- Docker Pipeline
- Git
- Email Extension
- SonarQube Scanner
- JUnit
- Jacoco

### 5. Add Credentials

#### Docker Registry Credentials
```
Manage Jenkins → Manage Credentials → System → Global credentials
- ID: docker-registry-credentials
- Username: <your-docker-hub-username>
- Password: <your-docker-hub-token>
```

#### SonarQube Token (Optional)
```
- ID: sonarqube-token
- Secret: <your-sonarqube-token>
```

#### GitHub Credentials (Optional)
```
- ID: github-credentials
- Personal Access Token: <your-github-pat>
```

### 6. Create New Pipeline Job

1. Click "New Item"
2. Enter name: `MoskowStockBackend`
3. Select: "Pipeline"
4. Configure:
   - Definition: "Pipeline script from SCM"
   - SCM: "Git"
   - Repository URL: `https://github.com/your-repo/MoskowStockBackend.git`
   - Credentials: Select GitHub credentials
   - Branch: `**`
   - Script Path: `Jenkinsfile`
5. Save

## Jenkins Shared Library

The `vars/` directory contains reusable Groovy functions for Jenkins pipelines.

### Usage in Jenkinsfile

```groovy
@Library('MoskowStockBackend') _

pipeline {
    stages {
        stage('Build Docker') {
            steps {
                buildDockerImage(
                    imageName: 'moskowstock/backend',
                    tag: env.BUILD_NUMBER
                )
            }
        }
        
        stage('Deploy') {
            steps {
                deployWithCompose(
                    environment: 'dev',
                    composeFiles: ['compose.yaml']
                )
            }
        }
        
        stage('Push Image') {
            steps {
                pushDockerImage(
                    imageName: 'moskowstock/backend',
                    tag: env.BUILD_NUMBER
                )
            }
        }
    }
    post {
        always {
            notifyBuild(
                status: currentBuild.result,
                slackChannel: '#deployments'
            )
        }
    }
}
```

## Configuration as Code (JCasC)

The `casc.yaml` file allows automated setup of Jenkins through environment variables.

### Required Environment Variables

```
DOCKER_USERNAME=<your-docker-username>
DOCKER_PASSWORD=<your-docker-password>
SONARQUBE_TOKEN=<your-sonarqube-token>
SLACK_WEBHOOK_URL=<your-slack-webhook>
SMTP_USERNAME=<your-email@gmail.com>
SMTP_PASSWORD=<your-gmail-app-password>
JENKINS_EMAIL=<jenkins-notification-email>
MAVEN_HOME=/usr/share/maven
JAVA_HOME=/usr/local/openjdk-21
```

### Loading JCasC Configuration

The Docker Compose file automatically loads `casc.yaml`. To use it manually:

```bash
export CASC_JENKINS_CONFIG=/path/to/casc.yaml
docker run -e CASC_JENKINS_CONFIG jenkins/jenkins:lts
```

## Managing Plugins

### Install Plugins via Docker

Add plugins to `jenkins/plugins.txt`:

```
job-dsl:latest
github-cli:latest
```

Then rebuild the Docker image:

```bash
docker compose down jenkins
docker compose up -d jenkins
```

### Install Plugins via UI

Manage Jenkins → Manage Plugins → Search and install

## Troubleshooting

### Jenkins won't start

```bash
# Check logs
docker compose logs jenkins

# Check volume permissions
docker compose exec jenkins ls -la /var/jenkins_home

# Restart service
docker compose restart jenkins
```

### Pipeline fails at Docker stage

```bash
# Verify Docker socket is accessible
docker compose exec jenkins docker ps

# Check Docker daemon
docker ps
```

### Maven build fails

```bash
# Check Java version
docker compose exec jenkins java -version

# Clear Maven cache
docker compose exec jenkins rm -rf /root/.m2/repository
```

### SonarQube connection fails

```bash
# Check SonarQube is running
curl http://localhost:9000

# Verify token is correct
# Admin → Security → Users → Tokens
```

## Performance Tuning

### Increase Jenkins Memory

Edit `docker compose.yaml`:

```yaml
services:
  jenkins:
    environment:
      - JAVA_OPTS=-Xmx2048m -Xms1024m
```

### Enable Build Caching

In Jenkinsfile:

```groovy
options {
    buildDiscarder(logRotator(numToKeepStr: '20'))
    disableConcurrentBuilds()
}
```

### Use Jenkins Agents

Create additional Jenkins agents for parallel builds:

```yaml
services:
  jenkins-agent:
    image: jenkins/agent
    environment:
      - JENKINS_URL=http://jenkins:8080
      - JENKINS_SECRET=<secret>
      - JENKINS_AGENT_NAME=agent1
```

## Backup and Recovery

### Backup Jenkins Data

```bash
docker compose exec jenkins tar -czf /backup/jenkins-backup-$(date +%Y%m%d).tar.gz /var/jenkins_home
```

### Restore Jenkins Data

```bash
docker compose exec jenkins tar -xzf /backup/jenkins-backup-20240101.tar.gz -C /
docker compose restart jenkins
```

## Security Best Practices

1. **Use Jenkins behind a proxy** (Nginx, Apache)
2. **Enable HTTPS** with valid SSL certificates
3. **Set up authentication** (LDAP, OAuth2, SAML)
4. **Use Jenkins Credentials Plugin** for all secrets
5. **Restrict job permissions** using Matrix Authorization
6. **Enable CSRF protection** (enabled by default)
7. **Regularly update plugins** and Jenkins version
8. **Audit Jenkins logs** regularly
9. **Use Jenkins in a private network** without public internet access
10. **Rotate credentials** periodically

## Monitoring and Metrics

### Jenkins Metrics

Available at: `http://localhost:8082/jenkins/metrics`

### SonarQube Dashboard

Available at: `http://localhost:9000`

### Docker Registry

Browse images at: `http://localhost:5000/v2/_catalog`

## Advanced Configuration

### Slack Notifications

Configure in Jenkins:
```
Manage Jenkins → Configure System → Slack Notifications
- Workspace: your-workspace
- Credential: slack-webhook
```

### Email Notifications

Configure in Jenkins:
```
Manage Jenkins → Configure System → Extended E-mail Notification
- SMTP Server: smtp.gmail.com
- SMTP Port: 587
- Use SMTP Authentication: enabled
```

### GitHub Webhooks

In GitHub repository settings:
```
Settings → Webhooks → Add webhook
- Payload URL: http://your-jenkins/github-webhook/
- Content type: application/json
- Events: Push events, Pull request events
```

## References

- [Jenkins Official Documentation](https://www.jenkins.io/doc/)
- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Jenkins Shared Libraries](https://www.jenkins.io/doc/book/pipeline/shared-libraries/)
- [Jenkins Configuration as Code](https://plugins.jenkins.io/configuration-as-code/)
- [Docker Pipeline Plugin](https://plugins.jenkins.io/docker-workflow/)
- [SonarQube Integration](https://docs.sonarqube.org/)

## Support

For issues or questions:
1. Check Jenkins logs: `docker compose logs jenkins`
2. Review pipeline execution: Jenkins UI → Job → Console Output
3. Check plugin documentation
4. Consult Jenkins community forums

---

**Last Updated**: 2024
**Created for**: MoskowStock Backend

