param(
    [switch]$Install,
    [string]$ModsDir = (Join-Path $env:APPDATA ".minecraft\mods")
)
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"
$version = "9.5.0"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $root
$toolDir = Join-Path $root ".build-tools"
$home = Join-Path $toolDir "gradle-$version"
$launcher = Join-Path $home "lib\gradle-launcher-$version.jar"
$zip = Join-Path $toolDir "gradle-$version-bin.zip"
$shaFile = "$zip.sha256"

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
        $previous = @(Get-ChildItem -Path $ModsDir -Filter 'Sofron-Terra-Lab-*.jar' -File -ErrorAction SilentlyContinue)
        if ($previous.Count -gt 0) {
            $backup = Join-Path $ModsDir ("sofron_terra_lab_backup_" + (Get-Date -Format "yyyyMMdd_HHmmss"))
            New-Item -Path $backup -ItemType Directory -Force | Out-Null
            foreach ($p in $previous) { Move-Item -LiteralPath $p.FullName -Destination $backup -ErrorAction Stop }
        }
        Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $ModsDir $jar.Name) -Force
        $legacy = @(Get-ChildItem -Path $ModsDir -Filter 'Sofron-Pack-Gen*.jar' -File -ErrorAction SilentlyContinue)
        if ($legacy.Count -gt 0) { Write-Warning "Old SofronPackGen JARs detected. Do not test both worldgen mods together." }
        Write-Host "Installed successfully. Fabric API must also be installed in this game profile." -ForegroundColor Green
    }
    else { Write-Host "[4/4] Install skipped (BUILD.bat). Run BUILD_AND_INSTALL.bat if desired." }
    Write-Warning "This is a DESIGN SCAFFOLD with vanilla world generation; custom generator starts after choices are approved."
    exit 0
}
catch {
    Write-Host ("ERROR: " + $_.Exception.Message) -ForegroundColor Red
    exit 1
}
