# PowerShell script to build Go utilities for Jenkins
# Usage: .\build.ps1 [build-all|build-linux|build-windows|clean|help]

param(
    [ValidateSet('build', 'build-all', 'build-linux', 'build-windows', 'clean', 'test', 'fmt', 'vet', 'help')]
    [string]$Action = 'help',
    [string]$Goos = '',
    [string]$Goarch = 'amd64'
)

$ErrorActionPreference = "Stop"

# Color definitions
$colors = @{
    'Green'  = '[92m'
    'Cyan'   = '[96m'
    'Yellow' = '[93m'
    'Red'    = '[91m'
    'Reset'  = '[0m'
}

function Write-Colored {
    param(
        [string]$Message,
        [string]$Color = 'Reset'
    )
    Write-Host -NoNewline "$($colors[$Color])$Message$($colors['Reset'])"
}

function Write-ColoredLine {
    param(
        [string]$Message,
        [string]$Color = 'Reset'
    )
    Write-Host "$($colors[$Color])$Message$($colors['Reset'])"
}

function Get-OSInfo {
    if ([System.Environment]::OSVersion.Platform -eq 'Win32NT') {
        return 'windows'
    } else {
        return 'linux'
    }
}

function Show-Help {
    Write-ColoredLine "`nJenkins Go Utilities Build Script" -Color 'Cyan'
    Write-ColoredLine "=" * 50 -Color 'Cyan'
    Write-Host ""
    Write-Host "Usage: .\build.ps1 [command]"
    Write-Host ""
    Write-ColoredLine "Commands:" -Color 'Yellow'
    Write-Host "  build           Build all tools for current OS (default)"
    Write-Host "  build-all       Build for Linux and Windows"
    Write-Host "  build-linux     Build for Linux"
    Write-Host "  build-windows   Build for Windows"
    Write-Host "  clean           Remove build artifacts"
    Write-Host "  test            Run tests"
    Write-Host "  fmt             Format code"
    Write-Host "  vet             Run go vet"
    Write-Host "  help            Show this help message"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\build.ps1 build-all           # Build for all platforms"
    Write-Host "  .\build.ps1 build-windows       # Build Windows binaries"
    Write-Host ""
}

function Invoke-Build {
    param(
        [string]$TargetOS = '',
        [string]$BuildArgs = ''
    )

    $currentOS = Get-OSInfo

    if ($TargetOS) {
        Write-ColoredLine "Building for $TargetOS..." -Color 'Cyan'
        $env:GOOS = $TargetOS
        $env:GOARCH = $Goarch
    } else {
        Write-ColoredLine "Building for current OS ($currentOS)..." -Color 'Cyan'
    }

    # Ensure bin directory exists
    if (!(Test-Path "bin")) {
        New-Item -ItemType Directory -Path "bin" | Out-Null
        Write-ColoredLine "Created bin directory" -Color 'Green'
    }

    # Build each utility
    $tools = @(
        @{ Name = 'build-docker'; Path = 'cmd/build-docker' },
        @{ Name = 'push-docker'; Path = 'cmd/push-docker' },
        @{ Name = 'deploy-compose'; Path = 'cmd/deploy-compose' },
        @{ Name = 'notify-build'; Path = 'cmd/notify-build' }
    )

    $execExt = if ($TargetOS -eq 'windows' -or ($TargetOS -eq '' -and $currentOS -eq 'windows')) { '.exe' } else { '' }

    foreach ($tool in $tools) {
        Write-Colored "Building " -Color 'Cyan'
        Write-ColoredLine "$($tool.Name)..." -Color 'Yellow'

        $outputFile = "bin/$($tool.Name)$execExt"

        try {
            & go build -o "$outputFile" "$($tool.Path)"

            if ($LASTEXITCODE -eq 0) {
                Write-ColoredLine "✅ Built: $outputFile" -Color 'Green'
            } else {
                Write-ColoredLine "❌ Failed to build $($tool.Name)" -Color 'Red'
                return $false
            }
        } catch {
            Write-ColoredLine "❌ Error building $($tool.Name): $_" -Color 'Red'
            return $false
        }
    }

    # Clear environment variables
    if ($TargetOS) {
        Remove-Item -Path Env:\GOOS -ErrorAction SilentlyContinue
        Remove-Item -Path Env:\GOARCH -ErrorAction SilentlyContinue
    }

    return $true
}

function Invoke-Clean {
    Write-ColoredLine "Cleaning build artifacts..." -Color 'Cyan'

    if (Test-Path "bin") {
        Remove-Item -Path "bin" -Recurse -Force
        Write-ColoredLine "✅ Removed bin directory" -Color 'Green'
    }

    & go clean
    Write-ColoredLine "✅ Clean completed" -Color 'Green'
}

function Invoke-Test {
    Write-ColoredLine "Running tests..." -Color 'Cyan'
    & go test -v .\...
    if ($LASTEXITCODE -eq 0) {
        Write-ColoredLine "✅ Tests passed" -Color 'Green'
    } else {
        Write-ColoredLine "❌ Tests failed" -Color 'Red'
    }
}

function Invoke-Format {
    Write-ColoredLine "Formatting code..." -Color 'Cyan'
    & go fmt .\...
    Write-ColoredLine "✅ Code formatted" -Color 'Green'
}

function Invoke-Vet {
    Write-ColoredLine "Running go vet..." -Color 'Cyan'
    & go vet .\...
    if ($LASTEXITCODE -eq 0) {
        Write-ColoredLine "✅ Vet checks passed" -Color 'Green'
    } else {
        Write-ColoredLine "⚠️  Vet found issues" -Color 'Yellow'
    }
}

# Check if Go is installed
try {
    $goVersion = & go version
    Write-ColoredLine "✅ Go found: $goVersion" -Color 'Green'
} catch {
    Write-ColoredLine "❌ Go is not installed or not in PATH" -Color 'Red'
    exit 1
}

# Execute action
switch ($Action) {
    'build' {
        if (Invoke-Build) {
            Write-ColoredLine "`n✅ All tools built successfully`n" -Color 'Green'
        } else {
            exit 1
        }
    }
    'build-all' {
        Write-ColoredLine "Building for all platforms..." -Color 'Cyan'
        if (Invoke-Build -TargetOS 'linux') {
            if (Invoke-Build -TargetOS 'windows') {
                Write-ColoredLine "`n✅ All cross-platform builds completed`n" -Color 'Green'
            }
        } else {
            exit 1
        }
    }
    'build-linux' {
        if (Invoke-Build -TargetOS 'linux') {
            Write-ColoredLine "`n✅ Linux builds completed`n" -Color 'Green'
        } else {
            exit 1
        }
    }
    'build-windows' {
        if (Invoke-Build -TargetOS 'windows') {
            Write-ColoredLine "`n✅ Windows builds completed`n" -Color 'Green'
        } else {
            exit 1
        }
    }
    'clean' {
        Invoke-Clean
    }
    'test' {
        Invoke-Test
    }
    'fmt' {
        Invoke-Format
    }
    'vet' {
        Invoke-Vet
    }
    'help' {
        Show-Help
    }
    default {
        Write-ColoredLine "Unknown action: $Action" -Color 'Red'
        Show-Help
        exit 1
    }
}

