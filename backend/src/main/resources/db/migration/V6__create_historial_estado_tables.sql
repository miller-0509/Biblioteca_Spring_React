-- V6__create_historial_estado_tables.sql
-- Auditoría de cambios de estado de libros y equipos
CREATE TABLE historial_estado_libros (
    id BIGSERIAL PRIMARY KEY,
    id_libro BIGINT NOT NULL REFERENCES libros (id_libro),
    estado_anterior VARCHAR(50) NOT NULL,
    estado_nuevo VARCHAR(50) NOT NULL,
    observacion TEXT NOT NULL,
    id_administrador BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    fecha TIMESTAMP NOT NULL
);

CREATE TABLE historial_estado_equipos (
    id BIGSERIAL PRIMARY KEY,
    id_equipo BIGINT NOT NULL REFERENCES equipos (id_equipo),
    estado_anterior VARCHAR(50) NOT NULL,
    estado_nuevo VARCHAR(50) NOT NULL,
    observacion TEXT NOT NULL,
    id_administrador BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    fecha TIMESTAMP NOT NULL
);

CREATE INDEX idx_historial_libro ON historial_estado_libros (id_libro);
CREATE INDEX idx_historial_equipo ON historial_estado_equipos (id_equipo);
