param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('auth', 'attendance', 'gateway', 'work-orders', 'assets', 'inventory', 'hse', 'amc', 'iot')]
    [string]$Service
)

$ErrorActionPreference = 'Stop'
$serviceDirectory = switch ($Service) {
    'auth' { 'auth-service' }
    'attendance' { 'attendance-service' }
    'gateway' { 'api-gateway' }
    'work-orders' { 'work-order-service' }
    'assets' { 'asset-service' }
    'inventory' { 'inventory-service' }
    'hse' { 'hse-safety-service' }
    'amc' { 'amc-vendor-service' }
    'iot' { 'iot-service' }
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
