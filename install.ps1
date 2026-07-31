$ErrorActionPreference = "Stop"

# Enable TLS 1.2
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$downloadUrl = "https://github.com/Rajesh-bandi/TunnelFlow/releases/download/v1.0.0-beta.1/TunnelFlow-1.0.0.msi"

$tempFile = Join-Path $env:TEMP "TunnelFlow-Setup.msi"
$logFile = Join-Path $env:TEMP "TunnelFlow-Install.log"

Clear-Host

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "          TunnelFlow Installer" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

try {

    ############################################################
    # Download
    ############################################################

    Write-Host "[1/3] Downloading TunnelFlow..." -ForegroundColor Cyan

    Invoke-WebRequest `
        -Uri $downloadUrl `
        -OutFile $tempFile `
        -UseBasicParsing

    if (!(Test-Path $tempFile)) {
        throw "Download failed."
    }

    Write-Host "Download completed." -ForegroundColor Green
    Write-Host ""

    ############################################################
    # Install
    ############################################################

    Write-Host "[2/3] Installing TunnelFlow..." -ForegroundColor Cyan

    $arguments = "/i `"$tempFile`" /quiet /norestart /L*v `"$logFile`""

    $process = Start-Process `
        -FilePath "msiexec.exe" `
        -ArgumentList $arguments `
        -Wait `
        -PassThru

    if ($process.ExitCode -ne 0) {

        Write-Host ""
        Write-Host "Installation failed!" -ForegroundColor Red
        Write-Host ""
        Write-Host "MSI Exit Code : $($process.ExitCode)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Installer Log :" -ForegroundColor Yellow
        Write-Host "$logFile" -ForegroundColor Cyan
        Write-Host ""

        Write-Host "Open the log file and search for:" -ForegroundColor Yellow
        Write-Host "Return value 3" -ForegroundColor Green
        Write-Host ""

        return
    }

    ############################################################
    # Verify
    ############################################################

    Write-Host ""
    Write-Host "[3/3] Verifying installation..." -ForegroundColor Cyan

    $machinePath = [Environment]::GetEnvironmentVariable("Path","Machine")
    $userPath    = [Environment]::GetEnvironmentVariable("Path","User")

    $env:Path = "$machinePath;$userPath"

    $command = Get-Command tunnelflow -ErrorAction SilentlyContinue

    Write-Host ""

    if ($command) {

        Write-Host "SUCCESS! TunnelFlow installed successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Open a NEW terminal and run:" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "    tunnelflow start" -ForegroundColor Green
    }
    else {

        Write-Host "Installation completed." -ForegroundColor Green
        Write-Host ""
        Write-Host "Open a NEW terminal and run:" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "    tunnelflow start" -ForegroundColor Green
    }

}
catch {

    Write-Host ""
    Write-Host "ERROR" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Yellow
}

finally {

    if (Test-Path $tempFile) {
        Remove-Item $tempFile -Force -ErrorAction SilentlyContinue
    }

    Write-Host ""
    Write-Host "Press ENTER to exit..."
    Read-Host | Out-Null
}