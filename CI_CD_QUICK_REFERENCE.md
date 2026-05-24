# CI/CD Quick Reference Card

## 🚀 START HERE

### For Windows PowerShell Users
```powershell
# Start Jenkins with all services
.\jenkins\jenkins.ps1 setup

# Then visit: http://localhost:8082/jenkins
```

### For Linux/macOS Users
```bash
chmod +x ./jenkins/jenkins.sh
./jenkins/jenkins.sh setup

# Then visit: http://localhost:8082/jenkins
```

### For GitHub Users
- Configure secrets in repository settings
- Push code to trigger workflows

---

## 📋 Common Commands

### Jenkins Control
```bash
# Start services
.\jenkins\jenkins.ps1 start

# Stop services
.\jenkins\jenkins.ps1 stop

# Restart services
.\jenkins\jenkins.ps1 restart

# View logs
.\jenkins\jenkins.ps1 logs

# Check status
.\jenkins\jenkins.ps1 status

# Get initial password
docker compose -f jenkins/compose-dev.yaml exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### Docker Management
```bash
# Start application
docker compose up -d

# Stop application
docker compose down

# View logs
docker compose logs -f app

# Rebuild image
docker compose up -d --build

# Clean up
docker system prune -a
```

### Maven Build
```bash
# Build locally
./mvnw clean package

# Run tests
./mvnw test

# Run specific test
./mvnw test -Dtest=TestClass

# Skip tests
./mvnw clean package -DskipTests
```

---

## 🔑 Required Credentials

### Jenkins Setup
1. Docker Hub username/password
2. GitHub token (optional)
3. SonarQube token (optional)
4. Slack webhook (optional)

Add in Jenkins UI:
```
Manage Jenkins → Manage Credentials → System → Global credentials
```

### GitHub Actions Setup
Add as Repository Secrets:
```
DOCKER_USERNAME
DOCKER_PASSWORD
SONARQUBE_TOKEN
SLACK_WEBHOOK_URL
```

---

## 🌿 Git Workflow

```
# Create feature branch
git checkout -b feature/my-feature

# Make changes and commit
git add .
git commit -m "Add new feature"

# Push to develop
git push origin feature/my-feature
git checkout develop
git pull origin develop
git merge feature/my-feature
git push origin develop

# Create release tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

---

## 🐳 Service URLs

| Service | URL | Default Credentials |
|---------|-----|-------------------|
| Jenkins | http://localhost:8082/jenkins | See startup log |
| SonarQube | http://localhost:9000 | admin/admin |
| PostgreSQL | localhost:5432 | postgres/password |
| Docker Registry | http://localhost:5000/v2/_catalog | N/A |
| Application | http://localhost:8080 | N/A |
| Swagger UI | http://localhost:8080/swagger-ui.html | N/A |

---

## 📊 Pipeline Stages (Automatic)

```
1. Checkout          → Clone code
2. Setup             → Prepare environment
3. Build             → Maven compile
4. Unit Tests        → Run tests + coverage
5. Code Quality      → SonarQube scan
6. Build Docker      → Create image
7. Push Registry     → Push to Hub (main only)
8. Deploy Dev        → Deploy (develop only)
9. Deploy Prod       → Deploy (tags only)
10. Health Check     → Verify app
11. Smoke Tests      → Basic tests
12. Notifications    → Send alerts
```

---

## 🔄 Build Triggers

| Event | Branch | Action |
|-------|--------|--------|
| Git Push | feature/* | Build & Test |
| Git Push | develop | Deploy to Dev |
| Git Push | main | Push to Registry |
| Git Tag | v*.*.* | Deploy to Prod |
| Pull Request | any | Build & Test |

---

## ⚡ Quick Troubleshooting

### Jenkins won't start
```bash
docker compose -f jenkins/compose-dev.yaml logs jenkins
docker system prune -a
docker compose -f jenkins/compose-dev.yaml restart
```

### Build fails
- Check console output in Jenkins
- Run locally: `./mvnw clean package`
- Check Docker: `docker ps`

### Test failures
```bash
./mvnw test -X
docker compose logs app
```

### Push to registry fails
- Verify Docker credentials configured
- Check Docker Hub token validity
- Ensure image name matches

### GitHub Actions not running
- Check Actions tab for errors
- Verify branch protection settings
- Confirm all secrets configured

---

## 📖 Documentation

| Document | Purpose |
|----------|---------|
| `CI_CD_README.md` | Overview & quick start |
| `CI_CD_SETUP.md` | Complete setup guide |
| `CI_CD_IMPLEMENTATION_SUMMARY.md` | What was created |
| `JENKINS_SETUP.md` | Jenkins details |
| `jenkins/README.md` | Jenkins advanced |
| `Jenkinsfile` | Pipeline definition |
| `.github/workflows/ci-cd.yaml` | GitHub Actions |

---

## 🔒 Security Checklist

- [ ] Never commit credentials
- [ ] Use credential providers
- [ ] Enable branch protection
- [ ] Use signed commits
- [ ] Scan Docker images
- [ ] Rotate tokens every 90 days
- [ ] Keep Jenkins updated
- [ ] Monitor build logs

---

## 🎯 Common Scenarios

### Scenario 1: Fix a bug
```bash
git checkout -b bugfix/issue-name
# Make changes
git commit -m "Fix issue"
git push origin bugfix/issue-name
# Build & test runs automatically
git checkout develop
git merge bugfix/issue-name
git push origin develop
# Deploys to dev automatically
```

### Scenario 2: Release to production
```bash
git checkout main
git pull origin develop
git tag -a v1.0.1 -m "Release 1.0.1"
git push origin v1.0.1
# All stages run automatically
# Manual approval required for production
```

### Scenario 3: Rollback deployment
```bash
# In Jenkins: Select previous build → Replay
# Or manually:
docker pull docker.io/moskowstock/backend:123
docker tag docker.io/moskowstock/backend:123 latest
docker compose up -d
```

---

## 📞 When You Need Help

1. **Check the docs**: Start with `CI_CD_README.md`
2. **View logs**: `docker compose logs -f`
3. **Jenkins UI**: http://localhost:8082/jenkins → Console Output
4. **GitHub**: Actions tab → Workflow run details

---

## 🚦 Health Check

```bash
# All services running?
docker compose ps

# Jenkins ready?
curl http://localhost:8082/jenkins

# Application ready?
curl http://localhost:8080/swagger-ui.html

# All tests passing?
./mvnw test

# Build successful?
./mvnw clean package
```

---

**Version**: 1.0.0  
**Last Updated**: 2024  
**For**: MoskowStock Backend

