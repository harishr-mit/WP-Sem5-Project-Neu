# Starts the whole demo from a clean state and opens the browser.
#   .\start-demo.ps1                       reset data, start all 4 services, open http://localhost:9080
#   .\start-demo.ps1 -Keep                 same, but keep existing data (no reseed)
#   .\start-demo.ps1 -Only event-service   restart ONE service (keeps data). Names: user-service, pass-service, event-service, client
#   .\start-demo.ps1 -Stop                 stop all 4 services (same as .\stop-demo.ps1)
#   add -NoBrowser to skip opening the browser
param([switch]$Keep, [switch]$Stop, [switch]$NoBrowser, [string]$Only)

$root = $PSScriptRoot
$ports = [ordered]@{ 'user-service' = 9083; 'pass-service' = 9082; 'event-service' = 9081; 'client' = 9080 }

function Wait-Port($port) {
    for ($i = 0; $i -lt 90; $i++) {
        try { $c = New-Object Net.Sockets.TcpClient('localhost', $port); $c.Close(); return }
        catch { Start-Sleep -Seconds 1 }
    }
    throw "Port $port did not come up. Check the minimized window for that service."
}

function Start-Svc($name) {
    Write-Host "Starting $name ..."
    $dir = Join-Path $root $name
    Start-Process cmd.exe -ArgumentList '/c', "`"$dir\mvnw.cmd`" spring-boot:run" -WorkingDirectory $dir -WindowStyle Minimized
    Wait-Port $ports[$name]
}

if ($Stop) { & "$root\stop-demo.ps1"; return }
if ($Only -and -not $ports.Contains($Only)) { throw "Unknown service '$Only'. Use: $($ports.Keys -join ', ')" }

$env:JAVA_HOME = Join-Path $root '.tools\jdk-21'
if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe")) { throw "JDK not found at $env:JAVA_HOME" }

if ($Only) {
    & "$root\stop-demo.ps1" -Only $Only
    Start-Svc $Only
    Write-Host "$Only restarted (data kept)."
    return
}

& "$root\stop-demo.ps1"
if (-not $Keep) {
    Get-ChildItem -Path "$root\*\data\*.db" -ErrorAction SilentlyContinue | Remove-Item -Force
    Write-Host 'Data reset (demo users, events and passes will be reseeded).'
}

# order matters: event-service seeds passes through pass-service and needs user ids to exist
foreach ($name in $ports.Keys) { Start-Svc $name }

Write-Host 'Ready: http://localhost:9080   (stop with .\stop-demo.ps1)'
if (-not $NoBrowser) { Start-Process 'http://localhost:9080' }
