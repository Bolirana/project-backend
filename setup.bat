@echo off
setlocal EnableDelayedExpansion

REM =============================================================
REM setup.bat — Bolirana Backend (Windows)
REM Prepara el entorno por primera vez y levanta la aplicación.
REM Requisitos: Docker Desktop, Java 17+, Maven
REM =============================================================

REM -------------------------------------------------------------
REM BLOQUE 1: Cargar variables de entorno desde el archivo .env
REM -------------------------------------------------------------
echo ^>^>^> Cargando variables de entorno...

if not exist ".env" (
    echo ERROR: No se encontro el archivo .env en la raiz del proyecto.
    echo        Copia env.example a .env y completa los valores.
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    set "line=%%A"
    if not "!line!"=="" (
        echo !line! | findstr /r "^[^#]" >nul 2>&1
        if !errorlevel!==0 set "%%A=%%B"
    )
)

echo     Variables cargadas correctamente.

REM -------------------------------------------------------------
REM BLOQUE 2: Levantar la base de datos con Docker Compose
REM -------------------------------------------------------------
echo ^>^>^> Levantando base de datos PostgreSQL en Docker...

docker compose down --remove-orphans
docker compose up -d

if !errorlevel! neq 0 (
    echo ERROR: Fallo al iniciar docker compose.
    exit /b 1
)

echo     Contenedor iniciado. Spring Boot reintentara la conexion automaticamente.

REM -------------------------------------------------------------
REM BLOQUE 3: Instalar dependencias y compilar con Maven
REM -------------------------------------------------------------
echo ^>^>^> Compilando proyecto con Maven (sin ejecutar tests)...

call mvnw.cmd clean install -DskipTests

if !errorlevel! neq 0 (
    echo ERROR: Fallo la compilacion con Maven.
    exit /b 1
)

echo     Compilacion exitosa.

REM -------------------------------------------------------------
REM BLOQUE 4: Ejecutar pruebas básicas
REM -------------------------------------------------------------
echo ^>^>^> Ejecutando pruebas basicas...

call mvnw.cmd test

if !errorlevel! neq 0 (
    echo ADVERTENCIA: Algunas pruebas fallaron. Revisa el reporte en target/surefire-reports
)

echo     Pruebas completadas.

REM -------------------------------------------------------------
REM BLOQUE 5: Levantar la aplicación
REM -------------------------------------------------------------
echo ^>^>^> Iniciando el servidor en http://localhost:8080

call mvnw.cmd spring-boot:run

endlocal
