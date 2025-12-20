param(
    [Parameter(ValueFromRemainingArguments=$true)]
    [string[]]$ComposeArgs
)

$ErrorActionPreference = 'Stop'

# Log file at repo root
$logPath = Join-Path -Path (Get-Location) -ChildPath "docker-compose.log"

# Start header
$header = "`n==== $(Get-Date -Format o) :: docker compose up $($ComposeArgs -join ' ') ===="
Add-Content -Path $logPath -Value $header

# Run docker compose up and tee output to log (append)
# Note: Using -NoNewWindow loses coloring in some terminals, but logs remain readable
try {
    docker compose up @ComposeArgs 2>&1 | Tee-Object -FilePath $logPath -Append
}
catch {
    Write-Error $_
    throw
}
