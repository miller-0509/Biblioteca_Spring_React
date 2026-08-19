-- V2__create_libros_table.sql
CREATE TYPE estado_libro AS ENUM ('disponible', 'prestado', 'mantenimiento', 'dañado');

CREATE TABLE libros (
    id_libro BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(150) NOT NULL,
    genero VARCHAR(100) NOT NULL,
    codigo_unico VARCHAR(100) NOT NULL,
    estado estado_libro,
    ubicacion VARCHAR(150),
    fecha_registro TIMESTAMP,
    disponible_prestamo BOOLEAN,
    tiempo_max_prestamo INTEGER,
    descripcion TEXT,
    eliminado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_compra DATE,
    proveedor VARCHAR(150),
    responsable VARCHAR(150)
);

CREATE INDEX idx_libro_codigo_unico ON libros (codigo_unico);
CREATE INDEX idx_libro_titulo ON libros (titulo);
