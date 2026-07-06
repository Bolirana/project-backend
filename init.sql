-- Script de inicialización de la base de datos
-- Proyecto: Sistema de Apuestas
-- Curso: Ingeniería de Software 1 (2016701)

CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nombre_completo VARCHAR,
    correo VARCHAR UNIQUE NOT NULL,
    contrasena_hash VARCHAR NOT NULL,
    fecha_nacimiento DATE,
    rol VARCHAR,
    estado VARCHAR,
    saldo FLOAT DEFAULT 0.0,
    creado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE evento (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR,
    deporte VARCHAR,
    fecha_evento DATE,
    equipo_local VARCHAR,
    equipo_visitante VARCHAR,
    estado VARCHAR,
    creado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE mercado (
    id SERIAL PRIMARY KEY,
    evento_id INTEGER NOT NULL REFERENCES evento(id),
    nombre VARCHAR,
    creado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE opcion_apuesta (
    id SERIAL PRIMARY KEY,
    mercado_id INTEGER NOT NULL REFERENCES mercado(id),
    nombre VARCHAR,
    cuota_actual NUMERIC(10,2)
);

CREATE TABLE apuesta (
    id SERIAL PRIMARY KEY,
    apostador_id INTEGER NOT NULL REFERENCES usuario(id),
    opcion_id INTEGER NOT NULL REFERENCES opcion_apuesta(id),
    monto FLOAT,
    cuota_congelada FLOAT,
    estado VARCHAR,
    creado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE movimiento_saldo (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL REFERENCES usuario(id),
    tipo VARCHAR,
    monto FLOAT,
    metodo_pago VARCHAR,
    creado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE historial_cuota (
    id SERIAL PRIMARY KEY,
    opcion_id INTEGER NOT NULL REFERENCES opcion_apuesta(id),
    cuota_anterior FLOAT,
    cuota_nueva FLOAT,
    origen VARCHAR,
    cambiado_en TIMESTAMP DEFAULT NOW()
);

CREATE TABLE configuracion_riesgo (
    id SERIAL PRIMARY KEY,
    mercado_id INTEGER UNIQUE NOT NULL REFERENCES mercado(id),
    limite_alerta FLOAT DEFAULT 500000,
    actualizado_en TIMESTAMP DEFAULT NOW()
);
