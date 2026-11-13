param(
    [switch]$Install,
    [switch]$Preflight,
    [string]$ModsDir = (Join-Path $env:APPDATA ".minecraft\mods")
)
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
$version = "9.5.0"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $root
$toolDir = Join-Path $root ".build-tools"
$gradleInstallDir = Join-Path $toolDir "gradle-$version"
$launcher = Join-Path $gradleInstallDir "lib\gradle-launcher-$version.jar"
$zip = Join-Path $toolDir "gradle-$version-bin.zip"
$shaFile = "$zip.sha256"

# PowerShell variable names are case-insensitive. A separate Gradle install
# variable avoids assignment to the read-only automatic home-directory variable.
if ($Preflight) {
    if (-not $gradleInstallDir -or -not $launcher -or -not $root) {
        throw "Build paths were not initialized"
    }
    if ((Split-Path -Parent $launcher) -ne (Join-Path $gradleInstallDir "lib")) {
        throw "Gradle launcher path is inconsistent"
    }
    Write-Host "PREFLIGHT PASS: PowerShell startup and Gradle paths" -ForegroundColor Green
    exit 0
}

try {
    Write-Host "[1/4] Java 25 check" -ForegroundColor Cyan
    $java = (Get-Command java.exe -ErrorAction Stop).Source
    $start = New-Object System.Diagnostics.ProcessStartInfo
    $start.FileName = $java
    $start.Arguments = "-version"
    $start.UseShellExecute = $false
    $start.RedirectStandardError = $true
    $start.RedirectStandardOutput = $true
    $process = [System.Diagnostics.Process]::Start($start)
    $javaText = $process.StandardError.ReadToEnd() + $process.StandardOutput.ReadToEnd()
    $process.WaitForExit()
    Write-Host $javaText
    if ($process.ExitCode -ne 0) { throw "Java failed to start." }
    if ($javaText -notmatch 'version\s+"25(?:\.|")') {
        throw "Java JDK 25 is required. Set JAVA_HOME and PATH before building."
    }

    Write-Host "[2/4] Gradle $version" -ForegroundColor Cyan
    if (-not (Test-Path $launcher)) {
        New-Item -Path $toolDir -ItemType Directory -Force | Out-Null
        $url = "https://services.gradle.org/distributions/gradle-$version-bin.zip"
        Write-Host "Downloading official Gradle distribution and published SHA-256..."
        Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $zip -MaximumRedirection 10
        Invoke-WebRequest -UseBasicParsing -Uri "$url.sha256" -OutFile $shaFile -MaximumRedirection 10
        $expected = ([IO.File]::ReadAllText($shaFile,[Text.Encoding]::UTF8).Trim() -split '\s+')[0].ToLowerInvariant()
        if ($expected -notmatch '^[0-9a-f]{64}$') { throw "Invalid official SHA-256 file." }
        $actual = (Get-FileHash -Algorithm SHA256 -Path $zip).Hash.ToLowerInvariant()
        if ($actual -ne $expected) { throw "Gradle checksum mismatch. Build aborted." }
        Expand-Archive -Path $zip -DestinationPath $toolDir -Force
        if (-not (Test-Path $launcher)) { throw "Gradle launcher missing after extraction." }
        Remove-Item $zip,$shaFile -ErrorAction SilentlyContinue
    }

    Write-Host "[3/4] Building source with Gradle..." -ForegroundColor Cyan
    # Launch Gradle via Java: avoids Windows nested gradle.bat access-denied issues.
    & java '-Dorg.gradle.appname=gradle' '-classpath' $launcher 'org.gradle.launcher.GradleMain' '--no-daemon' 'clean' 'build'
    if ($LASTEXITCODE -ne 0) { throw "Gradle compilation failed. Installed mods were left untouched." }

    $jars = @(Get-ChildItem (Join-Path $root "build\libs\*.jar") -ErrorAction Stop |
        Where-Object { $_.Name -notmatch '(-sources|-dev)\.jar$' })
    if ($jars.Count -ne 1) { throw "Expected one distributable mod JAR, got $($jars.Count)." }
    $jar = $jars[0]
    Write-Host "Build completed: $($jar.FullName)" -ForegroundColor Green

    if ($Install) {
        Write-Host "[4/4] Install to: $ModsDir" -ForegroundColor Cyan
        if (-not (Test-Path $ModsDir)) { New-Item -Path $ModsDir -ItemType Directory -Force | Out-Null }
        # Only our own previous generators are disabled; no unrelated mod is changed.
        $previous = @(Get-ChildItem -LiteralPath $ModsDir -Filter "*.jar" -File -ErrorAction Stop |
            Where-Object { $_.Name -match '^(Sofron-Terra-Lab-|Sofron-Pack-Gen|sofron-pack|sofron_pack|26\.3-Sofron-Pack-Gen)' })
        if ($previous.Count -gt 0) {
            $backup = Join-Path $ModsDir ("sofron_terra_lab_backup_" + (Get-Date -Format "yyyyMMdd_HHmmss_fff"))
            New-Item -Path $backup -ItemType Directory -Force | Out-Null
            foreach ($previousJar in $previous) {
                Write-Host ("Backing up older Sofron generator: " + $previousJar.Name)
                Move-Item -LiteralPath $previousJar.FullName -Destination $backup -ErrorAction Stop
            }
        }
        Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $ModsDir $jar.Name) -Force -ErrorAction Stop
        Write-Host "Installed successfully. Fabric API must also be installed in this game profile." -ForegroundColor Green
    }
    else { Write-Host "[4/4] Install skipped (BUILD.bat). Run BUILD_AND_INSTALL.bat if desired." }
    Write-Host "Active custom 26.3 worldgen is included. Create a NEW world and back up all old saves." -ForegroundColor Yellow
    exit 0
}
catch {
    Write-Host ("ERROR: " + $_.Exception.Message) -ForegroundColor Red
    exit 1
}
