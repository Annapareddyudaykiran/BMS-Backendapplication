# Dot-source this file in every backend PowerShell terminal:
#   . .\dev-env.ps1 -Service auth
# The generated signing key is kept under the current Windows user's LocalAppData,
# outside the repository, and reused by every backend service.
param(
    [ValidateSet('gateway', 'auth', 'attendance', 'work-orders', 'assets', 'inventory', 'hse', 'amc', 'iot')]
    [string]$Service = 'gateway'
)

function Set-MicroBmsDevEnvironment {
    $ErrorActionPreference = 'Stop'
    $secretDirectory = Join-Path ([Environment]::GetFolderPath('LocalApplicationData')) 'MicroBms'
    $secretFile = Join-Path $secretDirectory 'jwt-secret.txt'

    if (-not (Test-Path -LiteralPath $secretFile)) {
        New-Item -ItemType Directory -Path $secretDirectory -Force | Out-Null
        $secretBytes = New-Object byte[] 32
        $randomGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        try {
            $randomGenerator.GetBytes($secretBytes)
        } finally {
            $randomGenerator.Dispose()
        }
        [System.IO.File]::WriteAllText($secretFile, [Convert]::ToBase64String($secretBytes))
    }

    $env:JWT_SECRET = [System.IO.File]::ReadAllText($secretFile).Trim()
    if ([Convert]::FromBase64String($env:JWT_SECRET).Length -lt 32) {
        throw "The local JWT secret at '$secretFile' is invalid. Remove it and run this script again."
    }
    Write-Host 'Loaded the shared local JWT signing key for this PowerShell session.'

    if ($Service -ne 'gateway') {
        if ($Service -eq 'auth') {
            $urlName = 'AUTH_DB_URL'
            $usernameName = 'AUTH_DB_USERNAME'
            $passwordName = 'AUTH_DB_PASSWORD'
            $defaultUrl = 'jdbc:postgresql://localhost:5432/authentication_db'
            $userPrompt = 'PostgreSQL user for authentication_db [postgres]'
        } elseif ($Service -eq 'attendance') {
            $urlName = 'ATTENDANCE_DB_URL'
            $usernameName = 'ATTENDANCE_DB_USERNAME'
            $passwordName = 'ATTENDANCE_DB_PASSWORD'
            $defaultUrl = 'jdbc:postgresql://localhost:5432/attendance_db'
            $userPrompt = 'PostgreSQL user for attendance_db [postgres]'
        } elseif ($Service -eq 'work-orders') {
            $urlName = 'WORK_ORDERS_DB_URL'; $usernameName = 'WORK_ORDERS_DB_USERNAME'; $passwordName = 'WORK_ORDERS_DB_PASSWORD'
            $defaultUrl = 'jdbc:postgresql://localhost:5432/work_orders_db'; $userPrompt = 'PostgreSQL user for work_orders_db [postgres]'
        } elseif ($Service -eq 'assets') {
            $urlName = 'ASSETS_DB_URL'; $usernameName = 'ASSETS_DB_USERNAME'; $passwordName = 'ASSETS_DB_PASSWORD'
            $defaultUrl = 'jdbc:postgresql://localhost:5432/assets_ppm_db'; $userPrompt = 'PostgreSQL user for assets_ppm_db [postgres]'
        } elseif ($Service -eq 'hse') {
            $urlName = 'HSE_DB_URL'; $usernameName = 'HSE_DB_USERNAME'; $passwordName = 'HSE_DB_PASSWORD'
            $defaultUrl = 'jdbc:postgresql://localhost:5432/hse_safety_db'; $userPrompt = 'PostgreSQL user for hse_safety_db [postgres]'
        } elseif ($Service -eq 'amc') {
            $urlName = 'AMC_DB_URL'; $usernameName = 'AMC_DB_USERNAME'; $passwordName = 'AMC_DB_PASSWORD'
            $defaultUrl = 'jdbc:postgresql://localhost:5432/amc_vendor_db'; $userPrompt = 'PostgreSQL user for amc_vendor_db [postgres]'
        } elseif ($Service -eq 'iot') {
            $urlName = 'IOT_DB_URL'; $usernameName = 'IOT_DB_USERNAME'; $passwordName = 'IOT_DB_PASSWORD'
            $defaultUrl = 'jdbc:postgresql://localhost:5432/iot_monitoring_db'; $userPrompt = 'PostgreSQL user for iot_monitoring_db [postgres]'
        } else {
            $urlName = 'INVENTORY_DB_URL'; $usernameName = 'INVENTORY_DB_USERNAME'; $passwordName = 'INVENTORY_DB_PASSWORD'
            $defaultUrl = 'jdbc:postgresql://localhost:5432/inventory_procurement_db'; $userPrompt = 'PostgreSQL user for inventory_procurement_db [postgres]'
        }

        if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($urlName, 'Process'))) {
            [Environment]::SetEnvironmentVariable($urlName, $defaultUrl, 'Process')
        }
        if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($usernameName, 'Process'))) {
            $databaseUser = Read-Host $userPrompt
            if ([string]::IsNullOrWhiteSpace($databaseUser)) { $databaseUser = 'postgres' }
            [Environment]::SetEnvironmentVariable($usernameName, $databaseUser, 'Process')
        }
        if ([string]::IsNullOrEmpty([Environment]::GetEnvironmentVariable($passwordName, 'Process'))) {
            $securePassword = Read-Host 'PostgreSQL password (input is hidden)' -AsSecureString
            $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
            try {
                $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
            } finally {
                [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
                $securePassword.Dispose()
            }
            if ([string]::IsNullOrEmpty($plainPassword)) { throw 'A non-empty PostgreSQL password is required.' }
            [Environment]::SetEnvironmentVariable($passwordName, $plainPassword, 'Process')
            $plainPassword = $null
        }
        Write-Host "Loaded database connection settings for $Service-service into this PowerShell session."
    }
}

Set-MicroBmsDevEnvironment
Remove-Item Function:\Set-MicroBmsDevEnvironment
