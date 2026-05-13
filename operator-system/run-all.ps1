<#
run-all.ps1

Starts the backend (using the included mvnw.cmd) and the frontend (npm dev server),
monitors both, and restarts both if either process exits. Designed for Windows PowerShell.

Usage: .\run-all.ps1
#>

param(
    [int]$RestartDelay = 1
)

function Start-Processes {
    Write-Host "Starting backend (with .env if present)..."
    $scriptRoot = $PSScriptRoot
    $backendDir = Join-Path $scriptRoot "backend"
    # Use the existing run-with-aiven.ps1 to load .env and start the app
    $backendRunner = Join-Path $backendDir "run-with-aiven.ps1"
    if (-not (Test-Path $backendRunner)) {
        Write-Host "run-with-aiven.ps1 not found in $backendDir; falling back to mvnw.cmd"
        $backendCmd = "cd `"$backendDir`"; if (Test-Path '.\\mvnw.cmd') { .\\mvnw.cmd spring-boot:run } else { Write-Host 'mvnw.cmd not found in $backendDir'; exit 1 }"
        $backend = Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit","-Command", $backendCmd -PassThru
    } else {
        $backendCmd = "cd `"$backendDir`"; .\" + (Split-Path $backendRunner -Leaf) + ""
        $backend = Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit","-Command", $backendCmd -PassThru
    }

    Write-Host "Starting frontend..."
    # Start frontend in a separate PowerShell window and keep it open while dev server runs
    $frontendDir = Join-Path $scriptRoot "frontend"
    $frontendCmd = "cd `"$frontendDir`"; if (-not (Test-Path 'node_modules')) { npm install } ; npm run dev"
    $frontend = Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit","-Command", $frontendCmd -PassThru
    return @($backend, $frontend)
}
# Start both processes and exit this script immediately.
$procs = Start-Processes
Write-Host "Started backend (PID: $($procs[0].Id)) and frontend (PID: $($procs[1].Id)). Exiting run-all script." -ForegroundColor Green
