@echo off
setlocal enabledelayedexpansion


if exist .env (
    echo [1/5] Cargando variables desde .env...
    for /f "tokens=*" %%i in (.env) do set %%i
) else (
    echo [ERROR] No se encontro el archivo .env
    pause
    exit /b 1
)


echo [2/5] Levantando PostgreSQL...
docker compose down -v
docker compose up -d


echo [3/5] Esperando a que PostgreSQL acepte conexiones...
:wait_loop
docker exec %PG_CONTAINER% pg_isready -U %DB_USER% -d %DB_NAME% >nul 2>&1
if !errorlevel! neq 0 (
    timeout /t 1 >nul
    goto wait_loop
)
echo [OK] PostgreSQL esta listo.


echo [4/5] Verificando dependencias y compilando con Maven...
if exist mvnw (
    call .\mvnw clean install -DskipTests
) else (
    echo [ERROR] No se encontro el archivo mvnw en la raiz.
    exit /b 1
)


echo [5/5] Ejecutando pruebas de salud del contenedor...
docker exec %PG_CONTAINER% psql -U %DB_USER% -d %DB_NAME% -c "SELECT 1;"

echo ===========================================================
echo [EXITO] Entorno Java/Spring listo para usar.
pause