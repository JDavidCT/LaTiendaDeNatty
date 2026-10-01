$ErrorActionPreference = 'Stop'

$port = 8080
$portInUse = @(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue)

if ($portInUse.Count -gt 0) {
    $listener = $portInUse[0]
    $listenerProcess = Get-CimInstance Win32_Process -Filter "ProcessId = $($listener.OwningProcess)" -ErrorAction SilentlyContinue

    if ($listenerProcess -and $listenerProcess.CommandLine -match 'org\.apache\.catalina\.startup\.Bootstrap') {
        Write-Host "Tomcat ya esta activo en http://localhost:$port/."

        try {
            $appResponse = Invoke-WebRequest -Uri "http://localhost:$port/LaTienda-copia/" -UseBasicParsing -TimeoutSec 5
            Write-Host "La aplicacion tambien esta disponible: http://localhost:$port/LaTienda-copia/ (HTTP $([int]$appResponse.StatusCode))."
        }
        catch {
            Write-Warning "Tomcat esta activo, pero la aplicacion no respondio en /LaTienda-copia/. Revisa el despliegue y los logs de Tomcat."
        }

        return
    }

    $processName = if ($listenerProcess) { $listenerProcess.Name } else { 'proceso desconocido' }
    throw "El puerto $port esta ocupado por $processName (PID $($listener.OwningProcess)); Tomcat no puede iniciar mientras ese proceso lo use."
}

$defaultTomcatHome = Join-Path $env:USERPROFILE 'Downloads\apache-tomcat-10.1.55-windows-x64\apache-tomcat-10.1.55'
$tomcatHome = Read-Host "Carpeta de Tomcat 10 [$defaultTomcatHome]"

if ([string]::IsNullOrWhiteSpace($tomcatHome)) {
    $tomcatHome = $defaultTomcatHome
}

$catalina = Join-Path $tomcatHome 'bin\catalina.bat'

if (-not (Test-Path $catalina)) {
    throw "No se encontro catalina.bat en '$tomcatHome'."
}

$adminUser = Read-Host 'Usuario unico del administrador'
$adminPasswordHash = Read-Host 'Hash BCrypt de la contrasena del administrador'

if ([string]::IsNullOrWhiteSpace($adminUser) -or [string]::IsNullOrWhiteSpace($adminPasswordHash)) {
    throw 'Debes configurar LATIENDA_ADMIN_USER y LATIENDA_ADMIN_PASSWORD_HASH.'
}

[Environment]::SetEnvironmentVariable('LATIENDA_ADMIN_USER', $adminUser, 'Process')
[Environment]::SetEnvironmentVariable('LATIENDA_ADMIN_PASSWORD_HASH', $adminPasswordHash, 'Process')

Write-Host 'Configura usuario y contrasena MySQL en src\main\webapp\META-INF\context.xml.'
Write-Host 'Iniciando Tomcat...'
& $catalina run