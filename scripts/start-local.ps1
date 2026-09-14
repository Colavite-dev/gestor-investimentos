[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$envFile = Join-Path $projectRoot '.env'
$allowedKeys = @('DB_HOST', 'DB_PORT', 'DB_NAME', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET', 'APP_DEMO_ADMIN_ENABLED', 'APP_DEMO_ADMIN_USERNAME', 'APP_DEMO_ADMIN_PASSWORD', 'BRAPI_TOKEN', 'TWELVE_DATA_API_KEY')
$loadedValues = @{}
$originalValues = @{}
$environmentUpdated = $false

if (-not (Test-Path -LiteralPath $envFile -PathType Leaf)) {
    throw "Arquivo .env não encontrado na raiz do projeto. Crie-o com: Copy-Item .env.example .env"
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker não foi encontrado no PATH. Instale/inicie o Docker Desktop e tente novamente.'
}

$lineNumber = 0
foreach ($line in [System.IO.File]::ReadAllLines($envFile)) {
    $lineNumber++
    $normalizedLine = if ($lineNumber -eq 1) { $line.TrimStart([char]0xFEFF) } else { $line }

    if ($normalizedLine -match '^\s*(?:#|;|$)') {
        continue
    }

    if ($normalizedLine -notmatch '^(?<key>[A-Za-z_][A-Za-z0-9_]*)=(?<value>.*)$') {
        throw "Formato inválido no .env, linha $lineNumber. Use CHAVE=valor."
    }

    $key = $Matches['key']
    if ($allowedKeys -notcontains $key) {
        continue
    }

    if ($loadedValues.ContainsKey($key)) {
        throw "A variável $key aparece mais de uma vez no .env."
    }

    $loadedValues[$key] = $Matches['value']
}

if (-not $loadedValues.ContainsKey('DB_PASSWORD') -or [string]::IsNullOrWhiteSpace($loadedValues['DB_PASSWORD'])) {
    throw 'DB_PASSWORD deve estar definida no .env para iniciar o backend.'
}

if (-not $loadedValues.ContainsKey('JWT_SECRET') -or [string]::IsNullOrWhiteSpace($loadedValues['JWT_SECRET'])) {
    throw 'JWT_SECRET deve estar definida no .env para iniciar o backend.'
}

try {
    foreach ($key in $allowedKeys) {
        $originalValues[$key] = [Environment]::GetEnvironmentVariable($key, [EnvironmentVariableTarget]::Process)
        if ($loadedValues.ContainsKey($key)) {
            [Environment]::SetEnvironmentVariable($key, $loadedValues[$key], [EnvironmentVariableTarget]::Process)
        }
        else {
            [Environment]::SetEnvironmentVariable($key, $null, [EnvironmentVariableTarget]::Process)
        }
    }
    $environmentUpdated = $true

    Push-Location $projectRoot
    try {
        Write-Host 'Iniciando PostgreSQL pelo Docker Compose...'
        & docker compose --env-file $envFile up -d postgres
        if ($LASTEXITCODE -ne 0) {
            throw 'Não foi possível iniciar o serviço postgres pelo Docker Compose.'
        }

        Write-Host 'Verificando o serviço postgres...'
        & docker compose --env-file $envFile ps postgres
        if ($LASTEXITCODE -ne 0) {
            throw 'Não foi possível consultar o estado do serviço postgres no Docker Compose.'
        }

        $timeout = [TimeSpan]::FromSeconds(60)
        $deadline = [DateTime]::UtcNow.Add($timeout)
        $healthy = $false

        Write-Host 'Aguardando o PostgreSQL ficar healthy...'
        while ([DateTime]::UtcNow -lt $deadline) {
            $containerId = (& docker compose --env-file $envFile ps -q postgres).Trim()
            if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($containerId)) {
                break
            }

            $healthStatus = (& docker inspect --format '{{.State.Health.Status}}' $containerId).Trim()
            if ($LASTEXITCODE -eq 0 -and $healthStatus -eq 'healthy') {
                $healthy = $true
                break
            }

            Start-Sleep -Seconds 2
        }

        if (-not $healthy) {
            throw 'O PostgreSQL não ficou healthy em até 60 segundos. Consulte "docker compose --env-file .env ps" e os logs do serviço postgres.'
        }

        Write-Host 'PostgreSQL está healthy. Iniciando o backend Spring Boot...'
        & .\mvnw.cmd spring-boot:run
        $backendExitCode = $LASTEXITCODE
    }
    finally {
        Pop-Location
    }
}
finally {
    if ($environmentUpdated) {
        foreach ($key in $allowedKeys) {
            [Environment]::SetEnvironmentVariable($key, $originalValues[$key], [EnvironmentVariableTarget]::Process)
        }
    }
}

if ($backendExitCode -ne 0) {
    exit $backendExitCode
}
