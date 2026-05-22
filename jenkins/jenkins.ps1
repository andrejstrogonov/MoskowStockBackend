# Quick Jenkins Setup and Start Script for Windows PowerShell
# Run this script to quickly set up and start Jenkins with all required services

param(
    [ValidateSet('start', 'stop', 'restart', 'logs', 'status', 'setup')]
    [string]$Action = 'start'
)

$jenkinsDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$rootDir = Split-Path -Parent $jenkinsDir

Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   MoskowStock Jenkins Setup Script     ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan

# Check if Docker is installed
try {
    $dockerVersion = docker --version
    Write-Host "✅ Docker found: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Docker Desktop from: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# Check if Docker Compose is available
try {
    $composeVersion = docker compose version
    Write-Host "✅ Docker Compose found: $composeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker Compose is not available" -ForegroundColor Red
    exit 1
}

function Start-Jenkins {
    Write-Host "`n▶️  Starting Jenkins and supporting services..." -ForegroundColor Cyan

    Push-Location $jenkinsDir

    try {
        docker compose up -d

        if ($LASTEXITCODE -eq 0) {
            Write-Host "`n✅ Services started successfully!" -ForegroundColor Green

            Write-Host "`n📊 Service URLs:" -ForegroundColor Cyan
            Write-Host "  Jenkins:     http://localhost:8082/jenkins" -ForegroundColor Yellow
            Write-Host "  SonarQube:   http://localhost:9000" -ForegroundColor Yellow
            Write-Host "  Docker Hub:  http://localhost:5000/v2/_catalog" -ForegroundColor Yellow
            Write-Host "  PostgreSQL:  localhost:5432" -ForegroundColor Yellow

            Write-Host "`n⏳ Waiting for Jenkins to be ready..." -ForegroundColor Cyan

            $maxAttempts = 60
            $attempt = 0

            while ($attempt -lt $maxAttempts) {
                try {
                    $response = curl.exe -s -o /dev/null -w "%{http_code}" "http://localhost:8082/jenkins"
                    if ($response -eq "200" -or $response -eq "403") {
                        Write-Host "✅ Jenkins is ready!" -ForegroundColor Green

                        Write-Host "`n🔑 Initial Admin Password:" -ForegroundColor Cyan
                        $password = docker compose exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>$null
                        if ($password) {
                            Write-Host $password -ForegroundColor Yellow
                        } else {
                            Write-Host "Run: docker compose -f jenkins/compose.yaml exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword" -ForegroundColor Yellow
                        }

                        return
                    }
                } catch {
                    # Jenkins not ready yet
                }

                Start-Sleep -Seconds 2
                $attempt++
                Write-Host "  Attempt $attempt/$maxAttempts..." -ForegroundColor Gray
            }

            Write-Host "`n⚠️  Jenkins is starting up. Please wait a bit longer." -ForegroundColor Yellow
            Write-Host "You can check the logs with: docker compose logs -f jenkins" -ForegroundColor Gray

        } else {
            Write-Host "❌ Failed to start services" -ForegroundColor Red
        }
    } finally {
        Pop-Location
    }
}

function Stop-Jenkins {
    Write-Host "`n⏹️  Stopping Jenkins and supporting services..." -ForegroundColor Cyan

    Push-Location $jenkinsDir

    try {
        docker compose down

        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Services stopped successfully!" -ForegroundColor Green
        } else {
            Write-Host "❌ Failed to stop services" -ForegroundColor Red
        }
    } finally {
        Pop-Location
    }
}

function Restart-Jenkins {
    Stop-Jenkins
    Start-Sleep -Seconds 3
    Start-Jenkins
}

function Show-Logs {
    Write-Host "`n📋 Showing Jenkins logs (follow mode). Press Ctrl+C to exit." -ForegroundColor Cyan

    Push-Location $jenkinsDir

    try {
        docker compose logs -f jenkins
    } finally {
        Pop-Location
    }
}

function Show-Status {
    Write-Host "`n📊 Service Status:" -ForegroundColor Cyan

    Push-Location $jenkinsDir

    try {
        docker compose ps
    } finally {
        Pop-Location
    }
}

function Setup-Jenkins {
    Write-Host "`n⚙️  Running initial Jenkins setup..." -ForegroundColor Cyan

    Write-Host "`n✓ Step 1: Starting services..." -ForegroundColor Gray
    Start-Jenkins

    Write-Host "`n✓ Step 2: Configuration complete!" -ForegroundColor Green

    Write-Host "`nNext steps:" -ForegroundColor Cyan
    Write-Host "  1. Open Jenkins: http://localhost:8082/jenkins" -ForegroundColor Yellow
    Write-Host "  2. Use the initial admin password shown above" -ForegroundColor Yellow
    Write-Host "  3. Follow the setup wizard" -ForegroundColor Yellow
    Write-Host "  4. Install recommended plugins" -ForegroundColor Yellow
    Write-Host "  5. Create your first admin user" -ForegroundColor Yellow
    Write-Host "  6. Add credentials (Docker, GitHub, etc.)" -ForegroundColor Yellow
    Write-Host "  7. Create a new Pipeline job" -ForegroundColor Yellow
    Write-Host "`n📖 For detailed instructions, see: $jenkinsDir\README.md" -ForegroundColor Gray
}

# Execute action
switch ($Action) {
    'start' {
        Start-Jenkins
    }
    'stop' {
        Stop-Jenkins
    }
    'restart' {
        Restart-Jenkins
    }
    'logs' {
        Show-Logs
    }
    'status' {
        Show-Status
    }
    'setup' {
        Setup-Jenkins
    }
}

Write-Host "`n" -ForegroundColor Gray

