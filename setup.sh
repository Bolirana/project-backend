#!/bin/bash
# =============================================================
# setup.sh — Bolirana Backend (Linux / macOS)
# Prepara el entorno por primera vez y levanta la aplicación.
# Requisitos: Docker, Java 17+, Maven
# =============================================================

set -e

# -------------------------------------------------------------
# BLOQUE 1: Cargar variables de entorno desde el archivo .env
# -------------------------------------------------------------
echo ">>> Cargando variables de entorno..."

if [ ! -f ".env" ]; then
  echo "ERROR: No se encontró el archivo .env en la raíz del proyecto."
  echo "       Copia env.example a .env y completa los valores."
  exit 1
fi

set -a
source .env
set +a

echo "    Variables cargadas correctamente."

# -------------------------------------------------------------
# BLOQUE 2: Levantar la base de datos con Docker Compose
# -------------------------------------------------------------
echo ">>> Levantando base de datos PostgreSQL en Docker..."

docker compose down --remove-orphans
docker compose up -d

echo "    Contenedor iniciado. Spring Boot reintentará la conexión automáticamente."

# -------------------------------------------------------------
# BLOQUE 3: Instalar dependencias y compilar con Maven
# -------------------------------------------------------------
echo ">>> Compilando proyecto con Maven (sin ejecutar tests)..."

chmod +x mvnw
./mvnw clean install -DskipTests

echo "    Compilación exitosa."

# -------------------------------------------------------------
# BLOQUE 4: Ejecutar pruebas básicas
# -------------------------------------------------------------
echo ">>> Ejecutando pruebas básicas..."

./mvnw test

echo "    Pruebas completadas."

# -------------------------------------------------------------
# BLOQUE 5: Levantar la aplicación
# -------------------------------------------------------------
echo ">>> Iniciando el servidor en http://localhost:${PORT:-8080}"

./mvnw spring-boot:run
