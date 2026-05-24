# MoskowStock Backend - CI/CD Implementation

This document summarizes the CI/CD setup that has been added to the MoskowStock Backend project.

## 📋 Overview

A comprehensive CI/CD pipeline has been implemented supporting both **Jenkins** (self-hosted) and **GitHub Actions** (cloud-based) approaches.

## 🚀 Quick Start

### For Jenkins (Self-Hosted)

```powershell
# Windows PowerShell
.\jenkins\jenkins.ps1 setup

# Access: http://localhost:8082/jenkins
```

```bash
# Linux/macOS
chmod +x ./jenkins/jenkins.sh
./jenkins/jenkins.sh setup

# Access: http://localhost:8082/jenkins
```

### For GitHub Actions (Cloud)

1. Push this repository to GitHub
2. Configure secrets: Settings → Secrets and variables → Actions
3. Workflows automatically trigger on push/PR

## 📁 File Structure

```
MoskowStockBackend/
├── Jenkinsfile                          # Jenkins pipeline definition
├── JENKINS_SETUP.md                     # Jenkins setup guide (detailed)
├── CI_CD_SETUP.md                       # General CI/CD guide (both platforms)
├── compose.prod.yaml                    # Production Docker Compose override
│
├── jenkins/                             # Jenkins configuration
│   ├── compose.yaml                     # Docker Compose for Jenkins stack
│   ├── casc.yaml                        # Jenkins Configuration as Code
│   ├── plugins.txt                      # Required plugins list
│   ├── jenkins.sh                       # Quick start script (Linux/macOS)
│   ├── jenkins.ps1                      # Quick start script (PowerShell)
│   ├── README.md                        # Jenkins documentation
│   └── vars/                            # Shared Library functions
│       ├── buildDockerImage.groovy
│       ├── deployWithCompose.groovy
│       ├── pushDockerImage.groovy
│       └── notifyBuild.groovy
│
├── .github/workflows/                   # GitHub Actions workflows
│   └── ci-cd.yaml                       # GitHub Actions CI/CD pipeline
│
└── [existing project files]
```

## 📚 Documentation

### Getting Started
- **New to CI/CD?** Start with [CI_CD_SETUP.md](./CI_CD_SETUP.md)
- **Using Jenkins?** Read [JENKINS_SETUP.md](./JENKINS_SETUP.md)
- **Jenkins details?** See [jenkins/README.md](README.md)

### Quick Reference

| Task | Command/Action |
|------|----------------|
| Start Jenkins | `.\jenkins\jenkins.ps1 start` |
| Stop Jenkins | `.\jenkins\jenkins.ps1 stop` |
| View Jenkins logs | `.\jenkins\jenkins.ps1 logs` |
| Get initial password | See Jenkins startup output |
| Access Jenkins UI | http://localhost:8082/jenkins |
| Access SonarQube | http://localhost:9000 |
| Configure GitHub Actions | Settings → Secrets for your GitHub repo |

## 🔄 Pipeline Stages

Both Jenkins and GitHub Actions implement these stages:

1. **Checkout** - Clone repository
2. **Build** - Compile with Maven
3. **Unit Tests** - Run tests and generate coverage
4. **Code Quality** - SonarQube analysis
5. **Build Docker Image** - Create container image
6. **Push to Registry** - Push to Docker Hub (main branch)
7. **Deploy to Dev** - Deploy to dev environment (develop branch)
8. **Deploy to Production** - Deploy with approval (version tags)
9. **Health Check** - Verify application
10. **Notifications** - Email & Slack alerts

## 🔧 Configuration

### Jenkins
- Location: `./jenkins/casc.yaml`
- Environment variables in `docker-compose.yaml`
- Credentials in Jenkins UI

### GitHub Actions
- Location: `./.github/workflows/ci-cd.yaml`
- Secrets in GitHub: Settings → Secrets
- Branch protection rules recommended

## 🐳 Docker Services

When running Jenkins locally, these services start:

- **Jenkins** - CI/CD server (port 8082)
- **SonarQube** - Code quality (port 9000)
- **PostgreSQL** - Database (port 5432)
- **Docker Registry** - Private registry (port 5000)
- **Docker-in-Docker** - For Docker builds

## 🔐 Credentials Required

### For Jenkins

Add in Jenkins UI:
- **docker-registry-credentials**: Docker Hub auth
- **sonarqube-token**: SonarQube token
- **github-credentials**: GitHub personal token

### For GitHub Actions

Add as Repository Secrets:
- DOCKER_USERNAME
- DOCKER_PASSWORD
- SONARQUBE_TOKEN
- SLACK_WEBHOOK_URL

## 🌿 Branch Strategy

```
feature/... → develop → main → v1.0.0 (tag)
             |test|     |build| |deploy
```

- **develop**: Deploy to dev (automatic)
- **main**: Push to registry (automatic)
- **v*.*.***: Deploy to production (requires approval)

## 📊 Pipeline Results

### Jenkins
- Builds: http://localhost:8082/jenkins/job/MoskowStockBackend
- Reports: SonarQube, Test results, Coverage

### GitHub Actions
- Runs: GitHub → Actions tab
- Reports: GitHub Checks, Pull Request comments

## 🚀 Deployment

### Development
```bash
# Automatic on develop branch
# Services: DB, RabbitMQ, App
# URL: http://localhost:8080
```

### Production
```bash
# Manual approval on version tags
# Services: DB, RabbitMQ, App (with health checks)
# Requires: docker compose -f compose-dev.yaml -f compose-prod.yaml
```

## 🐛 Troubleshooting

### Jenkins won't start?
```bash
docker compose -f jenkins/compose-dev.yaml logs jenkins
docker system prune -a
docker compose -f jenkins/compose-dev.yaml up -d
```

### Build fails?
- Check logs in Jenkins UI: Build → Console Output
- Run locally: `./mvnw clean package`
- Verify Docker: `docker ps`

### GitHub Actions workflow not running?
- Check Actions tab for error messages
- Verify branch protection rules
- Confirm secrets are configured

See detailed trouble-shooting in [CI_CD_SETUP.md](./CI_CD_SETUP.md#troubleshooting)

## 📈 Performance

### Optimize Jenkins
- Increase Java memory in `jenkins/compose.yaml`
- Add Jenkins agents for parallel builds
- Enable build caching

### Optimize GitHub Actions
- Use caching for Maven dependencies
- Enable matrix builds
- Use faster runners (Ubuntu latest)

## 🔒 Security Best Practices

1. **Never commit credentials** to repository
2. **Rotate tokens** periodically
3. **Use branch protection** rules
4. **Sign commits** for releases
5. **Scan Docker images** for vulnerabilities
6. **Enable two-factor authentication** on GitHub
7. **Limit deployment permissions** to admins
8. **Use private registries** for sensitive images

## 📖 Additional Resources

- [Jenkinsfile syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [GitHub Actions syntax](https://docs.github.com/en/actions/using-workflows)
- [Docker best practices](https://docs.docker.com/develop/dev-best-practices/)
- [Spring Boot deployment](https://spring.io/guides/topicals/spring-boot-docker/)

## ✅ Verification Checklist

- [ ] Jenkins starts without errors
- [ ] All services are healthy
- [ ] Docker credentials configured
- [ ] Pipeline job created
- [ ] Test build passes
- [ ] GitHub Actions secrets configured
- [ ] Webhook configured (if using Jenkins)
- [ ] Branch protection rules set
- [ ] First deployment successful

## 📞 Support

For issues:
1. Review logs in Jenkins/GitHub
2. Check [CI_CD_SETUP.md](./CI_CD_SETUP.md)
3. Check [JENKINS_SETUP.md](./JENKINS_SETUP.md)
4. Check [jenkins/README.md](README.md)
5. Review pipeline definitions

## 📝 Next Steps

1. **Configure your environment:**
   - Set credentials for Docker Hub
   - Configure SonarQube (optional)
   - Set up Slack notifications

2. **Test the pipeline:**
   - Push to develop branch
   - Monitor build in Jenkins/GitHub
   - Verify deployment

3. **Optimize for your needs:**
   - Adjust resource limits
   - Add custom stages
   - Integrate with your tools

---

**Version**: 1.0.0  
**Last Updated**: 2024  
**Project**: MoskowStock Backend

