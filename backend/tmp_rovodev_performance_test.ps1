# Performance Validation Script for Search Service
# Simulates 20-30 concurrent requests and autocomplete bursts

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SEARCH PERFORMANCE VALIDATION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$gatewayUrl = "http://localhost:8080"

# Get token
Write-Host "Getting authentication token..." -ForegroundColor Yellow
try {
    $loginBody = '{"username":"admin","password":"admin123"}'
    $loginResp = Invoke-RestMethod -Uri "$gatewayUrl/api/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
    $token = $loginResp.token
    Write-Host "✓ Token obtained" -ForegroundColor Green
} catch {
    Write-Host "✗ Cannot obtain token. Ensure auth-service is running." -ForegroundColor Red
    exit
}

$headers = @{
    "Authorization" = "Bearer $token"
    "X-Tenant-Id" = "demo"
}

Write-Host ""
Write-Host "Test 1: Sequential Search Requests (20 requests)" -ForegroundColor Yellow
Write-Host "Target: All complete, latency reasonable" -ForegroundColor Gray

$searchQueries = @("laptop", "mouse", "chair", "desk", "computer", "phone", "tablet", "monitor", "keyboard", "printer")
$successCount = 0
$errorCount = 0
$totalTime = 0
$maxTime = 0

for ($i = 0; $i -lt 20; $i++) {
    $query = $searchQueries[$i % $searchQueries.Length]
    try {
        $startTime = Get-Date
        $response = Invoke-RestMethod -Uri "$gatewayUrl/api/search?q=$query" -Method GET -Headers $headers
        $elapsed = ((Get-Date) - $startTime).TotalMilliseconds
        
        $successCount++
        $totalTime += $elapsed
        if ($elapsed -gt $maxTime) { $maxTime = $elapsed }
        
        if ($i -eq 0) {
            Write-Host "  First response time: $([math]::Round($elapsed, 2))ms" -ForegroundColor Gray
        }
    } catch {
        $errorCount++
        if ($_.Exception.Response.StatusCode.value__ -eq 429) {
            Write-Host "  Rate limited at request $($i+1)" -ForegroundColor Yellow
            break
        }
    }
}

$avgTime = if ($successCount -gt 0) { [math]::Round($totalTime / $successCount, 2) } else { 0 }

Write-Host ""
Write-Host "Results:" -ForegroundColor Cyan
Write-Host "  Success: $successCount / 20" -ForegroundColor White
Write-Host "  Errors: $errorCount" -ForegroundColor White
Write-Host "  Avg latency: ${avgTime}ms" -ForegroundColor White
Write-Host "  Max latency: $([math]::Round($maxTime, 2))ms" -ForegroundColor White

if ($avgTime -lt 300) {
    Write-Host "  ✓ Performance: Under 300ms target" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Performance: Over 300ms target" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Test 2: Autocomplete Burst (30 rapid requests)" -ForegroundColor Yellow
Write-Host "Target: Simulate fast typing, check for errors/lockups" -ForegroundColor Gray

$burstSuccess = 0
$burstErrors = 0
$burstRateLimited = 0

for ($i = 0; $i -lt 30; $i++) {
    $query = "lap".Substring(0, [Math]::Min($i % 5 + 1, 3))
    try {
        $response = Invoke-RestMethod -Uri "$gatewayUrl/api/search/suggestions?q=$query" -Method GET -Headers $headers
        $burstSuccess++
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 429) {
            $burstRateLimited++
        } else {
            $burstErrors++
        }
    }
    Start-Sleep -Milliseconds 50 # Fast typing simulation
}

Write-Host ""
Write-Host "Results:" -ForegroundColor Cyan
Write-Host "  Success: $burstSuccess / 30" -ForegroundColor White
Write-Host "  Rate limited (429): $burstRateLimited" -ForegroundColor White
Write-Host "  Errors: $burstErrors" -ForegroundColor White

if ($burstErrors -eq 0) {
    Write-Host "  ✓ No errors: DB handled burst correctly" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Errors detected: Check DB configuration" -ForegroundColor Yellow
}

if ($burstRateLimited -gt 0) {
    Write-Host "  ✓ Rate limiting active" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Rate limiting not triggered" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Test 3: Concurrent Requests (10 parallel)" -ForegroundColor Yellow
Write-Host "Target: No DB lockups, all complete" -ForegroundColor Gray

$jobs = @()
for ($i = 0; $i -lt 10; $i++) {
    $query = "test$i"
    $job = Start-Job -ScriptBlock {
        param($url, $hdr, $q)
        try {
            $startTime = Get-Date
            $resp = Invoke-RestMethod -Uri "$url/api/search?q=$q" -Method GET -Headers $hdr
            $elapsed = ((Get-Date) - $startTime).TotalMilliseconds
            return @{ success = $true; time = $elapsed }
        } catch {
            return @{ success = $false; error = $_.Exception.Message }
        }
    } -ArgumentList $gatewayUrl, $headers, $query
    
    $jobs += $job
}

# Wait for all jobs
$results = $jobs | Wait-Job | Receive-Job
$jobs | Remove-Job

$concurrentSuccess = ($results | Where-Object { $_.success }).Count
$concurrentErrors = ($results | Where-Object { -not $_.success }).Count
$concurrentTimes = $results | Where-Object { $_.success } | ForEach-Object { $_.time }
$concurrentAvg = if ($concurrentTimes.Count -gt 0) { 
    [math]::Round(($concurrentTimes | Measure-Object -Average).Average, 2) 
} else { 0 }

Write-Host ""
Write-Host "Results:" -ForegroundColor Cyan
Write-Host "  Success: $concurrentSuccess / 10" -ForegroundColor White
Write-Host "  Errors: $concurrentErrors" -ForegroundColor White
Write-Host "  Avg latency: ${concurrentAvg}ms" -ForegroundColor White

if ($concurrentErrors -eq 0) {
    Write-Host "  ✓ No DB lockups" -ForegroundColor Green
} else {
    Write-Host "  ⚠ Some requests failed" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PERFORMANCE SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Sequential Search:" -ForegroundColor White
Write-Host "  Success Rate: $(if ($successCount -gt 0) { [math]::Round($successCount/20*100, 1) } else { 0 })%" -ForegroundColor White
Write-Host "  Avg Latency: ${avgTime}ms (target: <300ms)" -ForegroundColor $(if ($avgTime -lt 300) { "Green" } else { "Yellow" })
Write-Host ""
Write-Host "Autocomplete Burst:" -ForegroundColor White
Write-Host "  Success Rate: $(if ($burstSuccess -gt 0) { [math]::Round($burstSuccess/30*100, 1) } else { 0 })%" -ForegroundColor White
Write-Host "  Rate Limiting: $(if ($burstRateLimited -gt 0) { 'Active' } else { 'Not Triggered' })" -ForegroundColor $(if ($burstRateLimited -gt 0) { "Green" } else { "Yellow" })
Write-Host ""
Write-Host "Concurrent Requests:" -ForegroundColor White
Write-Host "  Success Rate: $(if ($concurrentSuccess -gt 0) { [math]::Round($concurrentSuccess/10*100, 1) } else { 0 })%" -ForegroundColor White
Write-Host "  No Lockups: $(if ($concurrentErrors -eq 0) { 'Yes' } else { 'No' })" -ForegroundColor $(if ($concurrentErrors -eq 0) { "Green" } else { "Red" })
Write-Host ""

$overall = $successCount -gt 15 -and $burstErrors -eq 0 -and $concurrentErrors -eq 0
if ($overall) {
    Write-Host "✓ PERFORMANCE VALIDATION PASSED" -ForegroundColor Green
} else {
    Write-Host "⚠ PERFORMANCE VALIDATION: Review issues above" -ForegroundColor Yellow
}

Write-Host ""
