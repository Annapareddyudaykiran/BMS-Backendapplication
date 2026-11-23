param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('auth', 'attendance', 'gateway')]
    [string]$Service
)

$ErrorActionPreference = 'Stop'
$serviceDirectory = switch ($Service) {
    'auth' { 'auth-service' }
    'attendance' { 'attendance-service' }
    'gateway' { 'api-gateway' }
}

Push-Location $PSScriptRoot
try {
    . .\dev-env.ps1 -Service $Service
    & mvn -f (Join-Path $serviceDirectory 'pom.xml') clean spring-boot:run
    if ($LASTEXITCODE -ne 0) {
        throw "$Service did not start successfully (Maven exit code $LASTEXITCODE)."
    }
} finally {
    Pop-Location
}
