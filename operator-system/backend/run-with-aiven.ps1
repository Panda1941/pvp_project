<#
run-with-aiven.ps1

Loads environment variables from a local .env file (if present) and starts the Spring Boot app.
Create a `.env` file in this folder (copy from `.env.example`) and fill in the password.

Usage: .\run-with-aiven.ps1
#>

$scriptRoot = $PSScriptRoot
$envFile = Join-Path $scriptRoot ".env"

if (Test-Path $envFile) {
    Write-Host "Loading environment variables from $envFile"
    Get-Content $envFile | ForEach-Object {
        $_ = $_.Trim()
        if ($_ -eq "" -or $_.StartsWith("#")) { return }
        $parts = $_ -split "=", 2
        if ($parts.Length -eq 2) {
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            Write-Host "Setting $name"
            Set-Item -Path Env:\$name -Value $value
        }
    }
} else {
    Write-Host "No .env file found in $scriptRoot. Using existing environment variables."
}

Write-Host "Starting Spring Boot (mvnw)..."
Start-Process -FilePath ".\\mvnw.cmd" -ArgumentList "spring-boot:run" -NoNewWindow -Wait
