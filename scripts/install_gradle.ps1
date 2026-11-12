param(
    [Parameter(Mandatory = $true)][string]$Version,
    [Parameter(Mandatory = $true)][string]$Destination
)
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$zipName = "gradle-$Version-bin.zip"
$url = "https://services.gradle.org/distributions/$zipName"
$hashUrl = "$url.sha256"
$installDir = Join-Path $Destination "gradle-$Version"
$exe = Join-Path $installDir "bin\gradle.bat"
if (Test-Path $exe) {
    Write-Host "Gradle $Version already installed."
    exit 0
}
New-Item -ItemType Directory -Force -Path $Destination | Out-Null
$zip = Join-Path $Destination $zipName
$checksumFile = Join-Path $Destination "$zipName.sha256"
try {
    Write-Host "Downloading $url"
    Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $zip -MaximumRedirection 10
    Write-Host "Downloading SHA-256 checksum..."
    Invoke-WebRequest -UseBasicParsing -Uri $hashUrl -OutFile $checksumFile -MaximumRedirection 10
    $checksumText = [System.IO.File]::ReadAllText($checksumFile, [System.Text.Encoding]::UTF8).Trim()
    $expected = ($checksumText -split '\s+')[0].ToLowerInvariant()
    if ($expected -notmatch '^[a-f0-9]{64}$') {
        throw "Invalid SHA-256 checksum received from Gradle distribution service."
    }
    $actual = (Get-FileHash -Algorithm SHA256 -Path $zip).Hash.ToLowerInvariant()
    if ($actual -ne $expected) {
        throw "SHA-256 mismatch: expected $expected; got $actual"
    }
    Write-Host "SHA-256 verified. Extracting Gradle..."
    Expand-Archive -Path $zip -DestinationPath $Destination -Force
    if (-not (Test-Path $exe)) {
        throw "Gradle executable missing after extraction: $exe"
    }
    Write-Host "Gradle $Version ready."
} catch {
    Write-Host ("ERROR: " + $_.Exception.Message) -ForegroundColor Red
    exit 1
} finally {
    Remove-Item -Path $zip -Force -ErrorAction SilentlyContinue
    Remove-Item -Path $checksumFile -Force -ErrorAction SilentlyContinue
}
