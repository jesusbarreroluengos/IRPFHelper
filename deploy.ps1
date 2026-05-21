# Despliegue Angular + Spring Boot (WAR) en Tomcat 9.0.105 sin servicio
# Usa shutdown.bat / startup.bat. Ejecutar como Administrador.

# --- Configuracion ---
$TomcatHome    = "C:\apache-tomcat-10.1.50"              # Ruta Tomcat
$AngularDir    = "C:\Users\jbarl\Documents\UNIR\TFG\irpfHelper\frontend"                    # Carpeta del proyecto Angular
$Npmci         = "npm ci" # npm ci
$NgBuildCmd    = "ng build --configuration production --base-href /irpfhelper/" # build front
$FrontendDist  = "dist\frontend"                            # Ruta de dist relativa a AngularDir
$BackendDir    = "C:\Users\jbarl\Documents\UNIR\TFG\irpfHelper\backend"                     # Carpeta del proyecto Spring Boot
$StaticTarget  = "src\main\resources\static"             # Donde se incrusta el front
$MavenCmd      = "mvn -DskipTests clean package"         # Empaquetar WAR
$WarRelPath    = "target\irpfhelper-0.0.1-SNAPSHOT.war"                      # WAR resultante relativo a backend
$Contexto      = "irpfhelper"                                 # Nombre de contexto (WAR/carpeta)
$BackupRoot    = "C:\backups\tomcat-deploys"             # Carpeta de backups
$TiempoParada  = 40                                      # Segundos para esperar parada
$TiempoArranque= 60                                      # Segundos para esperar arranque
$Puerto        = 8080                                    # Puerto HTTP de Tomcat

# --- Rutas derivadas ---
$Webapps     = Join-Path $TomcatHome "webapps"
$WarOrigen   = Join-Path $BackendDir $WarRelPath
$WarDestino  = Join-Path $Webapps "$Contexto.war"
$Despliegue  = Join-Path $Webapps $Contexto
$Stamp       = Get-Date -Format "yyyyMMdd-HHmmss"
$BackupDir   = Join-Path $BackupRoot $Stamp
New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null

Write-Host "=== Despliegue $Contexto iniciado ===" -ForegroundColor Cyan

# --- Paso 1: Validaciones basicas ---
if (-not (Test-Path $TomcatHome)) { throw "TomcatHome no existe: $TomcatHome" }
if (-not (Test-Path (Join-Path $TomcatHome "bin\startup.bat"))) { throw "No se encuentra startup.bat" }

# --- Paso 2: Build frontend Angular ---
Write-Host "Compilando frontend..." -ForegroundColor Yellow
Push-Location $AngularDir
powershell -NoProfile -Command $Npmci
powershell -NoProfile -Command $NgBuildCmd
if ($LASTEXITCODE -ne 0) { Pop-Location; throw "Build Angular fallo (codigo $LASTEXITCODE)" }
Pop-Location
$DistPath = Join-Path $AngularDir $FrontendDist
if (-not (Test-Path $DistPath)) { throw "No se encontro dist: $DistPath" }

# --- Paso 3: Copiar frontend al backend (static) ---
$StaticPath = Join-Path $BackendDir $StaticTarget
if (Test-Path $StaticPath) { Remove-Item $StaticPath -Recurse -Force }
New-Item -ItemType Directory -Force -Path $StaticPath | Out-Null
Copy-Item (Join-Path $DistPath "*") $StaticPath -Recurse -Force
Write-Host "Frontend copiado a recursos estaticos del backend."

# --- Paso 4: Build backend WAR ---
Write-Host "Construyendo WAR backend..." -ForegroundColor Yellow
Push-Location $BackendDir
powershell -NoProfile -Command $MavenCmd
if ($LASTEXITCODE -ne 0) { Pop-Location; throw "Build backend fallo (codigo $LASTEXITCODE)" }
Pop-Location
if (-not (Test-Path $WarOrigen)) { throw "WAR no encontrado: $WarOrigen" }


# --- Paso 5: Backups del despliegue previo ---
if (Test-Path $WarDestino)   { Copy-Item $WarDestino (Join-Path $BackupDir "$Contexto.war") -Force }
if (Test-Path $Despliegue)   { Copy-Item $Despliegue (Join-Path $BackupDir $Contexto) -Recurse -Force }
Write-Host "Backup guardado en $BackupDir"

# --- Paso 6: Limpiar despliegue previo ---
if (Test-Path $WarDestino) { Remove-Item $WarDestino -Force }
if (Test-Path $Despliegue) { Remove-Item $Despliegue -Recurse -Force }

# --- Paso 7: Copiar nuevo WAR a webapps ---
Copy-Item $WarOrigen $WarDestino -Force
Write-Host "Nuevo WAR copiado a $WarDestino"

# --- Paso 8: Arrancar Tomcat con startup.bat ---
Write-Host "Arrancando Tomcat..." -ForegroundColor Yellow
Start-Process -FilePath (Join-Path $TomcatHome "bin\startup.bat") -WorkingDirectory (Join-Path $TomcatHome "bin") -NoNewWindow -Wait
$espera = 0
while (-not (Test-NetConnection -ComputerName "127.0.0.1" -Port $Puerto -InformationLevel Quiet -ErrorAction SilentlyContinue -WarningAction SilentlyContinue) -and $espera -lt $TiempoArranque) {
    Start-Sleep -Seconds 1; $espera++
}
if (-not (Test-NetConnection -ComputerName "127.0.0.1" -Port $Puerto -InformationLevel Quiet -ErrorAction SilentlyContinue -WarningAction SilentlyContinue)) {
    Write-Warning "No se confirmo escucha en puerto $Puerto tras $TiempoArranque s. Revisa logs."
}

# --- Paso 9: Indicar logs ---
$LogDir = Join-Path $TomcatHome "logs"
Write-Host "Despliegue finalizado. Logs en $LogDir (catalina.YYYY-MM-DD.log)" -ForegroundColor Green
Write-Host "=== Despliegue $Contexto completado ===" -ForegroundColor Cyan