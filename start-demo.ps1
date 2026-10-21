# Starts the whole demo from a clean state and opens the browser.
#   .\start-demo.ps1          reset data, start all 4 services, open http://localhost:9080
#   .\start-demo.ps1 -Keep    same, but keep existing data (no reseed)
#   .\start-demo.ps1 -Stop    stop all 4 services
param([switch]$Keep, [switch]$Stop, [switch]$NoBrowser)

$root = $PSScriptRoot
$ports = 9083, 9082, 9081, 9080   # user, pass, event, client

function Stop-Demo {
    foreach ($p in $ports) {
        Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue |
            ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
    }
}

function Wait-Port($port) {
    for ($i = 0; $i -lt 90; $i++) {
        try { $c = New-Object Net.Sockets.TcpClient('localhost', $port); $c.Close(); return }
        catch { Start-Sleep -Seconds 1 }
    }
    throw "Port $port did not come up. Check the minimized window for that service."
}

function Start-Svc($name, $port) {
    Write-Host "Starting $name ..."
    $dir = Join-Path $root $name
    Start-Process cmd.exe -ArgumentList '/c', "`"$dir\mvnw.cmd`" spring-boot:run" -WorkingDirectory $dir -WindowStyle Minimized
    Wait-Port $port
}

Stop-Demo
if ($Stop) { Write-Host 'Stopped.'; return }

$env:JAVA_HOME = Join-Path $root '.tools\jdk-21'
if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe")) { throw "JDK not found at $env:JAVA_HOME" }

if (-not $Keep) {
    Get-ChildItem -Path "$root\*\data\*.db" -ErrorAction SilentlyContinue | Remove-Item -Force
    Write-Host 'Data reset (demo users, events and passes will be reseeded).'
}

# order matters: event-service seeds passes through pass-service and needs user ids to exist
Start-Svc 'user-service' 9083
Start-Svc 'pass-service' 9082
Start-Svc 'event-service' 9081
Start-Svc 'client' 9080

Write-Host 'Ready: http://localhost:9080   (stop with .\start-demo.ps1 -Stop)'
if (-not $NoBrowser) { Start-Process 'http://localhost:9080' }
