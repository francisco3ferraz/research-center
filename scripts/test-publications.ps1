Param(
    [string]$BaseUrl = "http://localhost:8080/research-center/api",
    [string]$AuthUser = "admin",
    [string]$AuthPass = "admin"
)

Write-Host "BASE=$BaseUrl"

# UTF8 without BOM writer for files used by curl
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

# Login and capture token
try {
    $loginBody = @{ username = $AuthUser; password = $AuthPass } | ConvertTo-Json
    $token = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method Post -ContentType 'application/json' -Body $loginBody -ErrorAction Stop
    if ($token) { Write-Host "Obtained token (len=$($token.Length))" }
} catch {
    Write-Host "No token obtained; continuing without Authorization header"
    $token = $null
}

Write-Host "`n1) GET /publications/ (list)"
& curl.exe -i -sS "$BaseUrl/publications/"

Write-Host "`n2) GET /publications/{id} (details) - example id=1"
& curl.exe -i -sS "$BaseUrl/publications/1"

Write-Host "`n3) POST /publications/ (create JSON)"
$meta = @'
{
  "title": "Test Publication from curl",
  "authors": ["Tester"],
  "type": "ARTICLE",
  "areaScientific": "Ciência de Dados",
  "year": 2026,
  "publisher": "ACME",
  "doi": "10.0000/example",
  "abstract": "Sample abstract",
  "confidential": false,
    "uploadedById": 3
}
'@
$meta | Out-String > $null
[System.IO.File]::WriteAllText("$env:TEMP\pub_metadata.json", $meta, $utf8NoBom)
$body = Get-Content -Raw -Path "$env:TEMP\pub_metadata.json"
try {
    $headers = @{}
    if ($token) { $headers['Authorization'] = "Bearer $token" }
    $resp = Invoke-RestMethod -Uri "$BaseUrl/publications/" -Method Post -ContentType 'application/json' -Body $body -Headers $headers
    $resp | ConvertTo-Json -Depth 5 | Write-Host
} catch {
    Write-Host "Request failed:" $_.Exception.Message
    if ($_.Exception.Response -ne $null) {
        try { $body = $_.Exception.Response.Content; Write-Host "Response body:`n$body" } catch {}
    }
}

Write-Host "`n4) POST /publications/ (create multipart: metadata + file)"
if (Test-Path -Path "sample.pdf") {
    $authHeader = if ($token) { "-H 'Authorization: Bearer $token'" } else { "" }
    & curl.exe -i -sS $authHeader -F "file=@sample.pdf" -F "metadata=@$env:TEMP\pub_metadata.json;type=application/json" "$BaseUrl/publications/"
} else {
    Write-Host "skipping multipart test (sample.pdf not found)"
}

Write-Host "`n5) PUT /publications/{id} (update) - example id=1"
$update = @'
{
  "title": "Updated Title",
  "authors": ["Tester", "Coauthor"],
  "abstract": "Updated abstract",
  "aiGeneratedSummary": "AI summary",
  "year": 2026,
  "publisher": "ACME",
  "doi": "10.0000/example"
}
'@
$update | Out-String > $null
[System.IO.File]::WriteAllText("$env:TEMP\pub_update.json", $update, $utf8NoBom)
$body = Get-Content -Raw -Path "$env:TEMP\pub_update.json"
try {
    $curlArgs = @('-i','-sS','-H','Content-Type: application/json')
    if ($token) { $curlArgs += '-H'; $curlArgs += "Authorization: Bearer $token" }
    $curlArgs += '-X'; $curlArgs += 'PUT'; $curlArgs += '-d'; $curlArgs += "@$env:TEMP\pub_update.json"; $curlArgs += "$BaseUrl/publications/1"
    & curl.exe @curlArgs
} catch {
    Write-Host "Request failed:" $_.Exception.Message
    if ($_.Exception.Response -ne $null) {
        try { $body = $_.Exception.Response.Content; Write-Host "Response body:`n$body" } catch {}
    }
}

Write-Host "`n6) DELETE /publications/{id} (delete) - example id=2"
if ($token) { & curl.exe -i -sS -H "Authorization: Bearer $token" -X DELETE "$BaseUrl/publications/2" } else { & curl.exe -i -sS -X DELETE "$BaseUrl/publications/2" }

Write-Host "`n7) POST /publications/{id}/tags (add tag by body)"
$headers = @{}
if ($token) { $headers['Authorization'] = "Bearer $token" }
$tagBody = @{ tagId = 5 } | ConvertTo-Json
try {
    $resp = Invoke-RestMethod -Uri "$BaseUrl/publications/1/tags" -Method Post -ContentType 'application/json' -Body $tagBody -Headers $headers -ErrorAction Stop
    $resp | ConvertTo-Json -Depth 5 | Write-Host
} catch {
    Write-Host "Request failed:" $_.Exception.Message
    if ($_.Exception.Response -ne $null) {
        try { $body = $_.Exception.Response.Content; Write-Host "Response body:`n$body" } catch {}
    }
}

Write-Host "`n8) POST /publications/{id}/tags/{tagId} (add tag by path)"
if ($token) { & curl.exe -i -sS -H "Authorization: Bearer $token" -X POST "$BaseUrl/publications/1/tags/5" } else { & curl.exe -i -sS -X POST "$BaseUrl/publications/1/tags/5" }

Write-Host "`n9) DELETE /publications/{id}/tags/{tagId} (remove tag)"
if ($token) { & curl.exe -i -sS -H "Authorization: Bearer $token" -X DELETE "$BaseUrl/publications/1/tags/5" } else { & curl.exe -i -sS -X DELETE "$BaseUrl/publications/1/tags/5" }

Write-Host "`n10) POST /publications/{id}/visiblity (set visibility - POST fallback)"
$headers = @{}
if ($token) { $headers['Authorization'] = "Bearer $token" }
$vis = @{ visible = $false } | ConvertTo-Json
try {
    $vis | Out-String > $null
    [System.IO.File]::WriteAllText("$env:TEMP\pub_vis.json", $vis, $utf8NoBom)
    $curlArgs = @('-i','-sS','-H','Content-Type: application/json')
    if ($token) { $curlArgs += '-H'; $curlArgs += "Authorization: Bearer $token" }
    $curlArgs += '-X'; $curlArgs += 'POST'; $curlArgs += '-d'; $curlArgs += "@$env:TEMP\pub_vis.json"; $curlArgs += "$BaseUrl/publications/1/visiblity"
    & curl.exe @curlArgs
} catch {
    Write-Host "Request failed:" $_.Exception.Message
    if ($_.Exception.Response -ne $null) {
        try { $body = $_.Exception.Response.Content; Write-Host "Response body:`n$body" } catch {}
    }
}

Write-Host "`n11) GET /publications/{id}/file (download) - example id=1"
$downloadPath = "$env:TEMP\pub_1_file"
& curl.exe -sS "$BaseUrl/publications/1/file" -o $downloadPath
if (Test-Path $downloadPath) {
    $len = (Get-Item $downloadPath).Length
    Write-Host "file downloaded to $downloadPath (size=$len bytes)"
} else {
    Write-Host "no file returned or empty"
}

Write-Host "`nDone."
