param()
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $root
$exitCode = 0
Get-ChildItem -Recurse -Filter *.java | ForEach-Object {
  $path = $_.FullName
  $fs = [System.IO.File]::OpenRead($path)
  $bytes = New-Object byte[] 3
  $read = $fs.Read($bytes,0,3)
  $fs.Close()
  if ($read -eq 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    Write-Host "BOM found: $path"
    $exitCode = 1
  }
}
$matches = Select-String -Path (Get-ChildItem -Recurse -Filter *.java).FullName -Pattern '\\"' -SimpleMatch
if ($matches) { Write-Host "Found suspicious escaped quotes in Java sources (\\\"). Please fix."; $exitCode = 1 }
exit $exitCode
