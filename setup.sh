set -e

echo "Iniciando setup profesional (Java/Spring Boot)..."

if [ -f .env ]; then
  echo "Cargando variables desde .env..."
  set -a
  source .env
  set +a
else
  echo "No se encontró el archivo .env."
  exit 1
fi

echo "Levantando PostgreSQL..."
docker compose down -v
docker compose up -d


echo "Esperando a que PostgreSQL acepte conexiones..."
until docker exec "$PG_CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; do
  sleep 1
done
echo "PostgreSQL está listo."


echo "Verificando dependencias de Maven..."
if [ -f "mvnw" ]; then
  chmod +x mvnw
  ./mvnw clean install -DskipTests
else
  echo "No se encontró mvnw. Asegúrate de estar en la raíz del proyecto Java."
  exit 1
fi


echo "Ejecutando verificación de base de datos..."
docker exec -i "$PG_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();"

echo "Setup finalizado correctamente. Entorno listo."