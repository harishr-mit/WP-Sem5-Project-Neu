# Stops the demo services (everything listening on 9080-9083).
#   .\stop-demo.ps1                      stop all 4
#   .\stop-demo.ps1 -Only event-service  stop one (user-service, pass-service, event-service, client)
param([string]$Only)

$ports = @{ 'user-service' = 9083; 'pass-service' = 9082; 'event-service' = 9081; 'client' = 9080 }
if ($Only -and -not $ports.ContainsKey($Only)) { throw "Unknown service '$Only'. Use: $($ports.Keys -join ', ')" }
$targets = if ($Only) { @($ports[$Only]) } else { $ports.Values }

foreach ($p in $targets) {
    Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue |
        ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
}
Start-Sleep -Seconds 2
$left = @(Get-NetTCPConnection -State Listen -LocalPort $targets -ErrorAction SilentlyContinue).Count
if ($left -eq 0) { Write-Host 'Stopped.' } else { Write-Host "Warning: $left port(s) still listening." }
