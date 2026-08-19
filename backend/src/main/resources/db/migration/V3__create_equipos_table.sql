-- V3__create_equipos_table.sql
CREATE TYPE estado_equipo AS ENUM ('disponible', 'prestado', 'mantenimiento', 'dañado');

CREATE TABLE equipos (
    id_equipo BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    tipo_equipo VARCHAR(50) NOT NULL,
    marca VARCHAR(100),
    modelo VARCHAR(100),
    numero_serie VARCHAR(100) NOT NULL UNIQUE,
    estado estado_equipo,
    ubicacion VARCHAR(150),
    fecha_registro TIMESTAMP,
    fecha_compra DATE,
    proveedor VARCHAR(150),
    responsable VARCHAR(150),
    disponible_prestamo BOOLEAN,
    tiempo_max_prestamo INTEGER,
    descripcion TEXT,
    eliminado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_equipo_numero_serie ON equipos (numero_serie);
CREATE INDEX idx_equipo_tipo ON equipos (tipo_equipo);
