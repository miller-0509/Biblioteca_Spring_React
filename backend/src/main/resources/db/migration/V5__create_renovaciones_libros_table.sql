-- V5__create_renovaciones_libros_table.sql
CREATE TABLE renovaciones_libros (
    id_renovacion BIGSERIAL PRIMARY KEY,
    id_prestamo_libro BIGINT NOT NULL REFERENCES prestamos_libros (id_prestamo_libro),
    id_usuario BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    id_administrador BIGINT REFERENCES usuarios (id_usuario),
    fecha_solicitud TIMESTAMP,
    fecha_respuesta TIMESTAMP,
    fecha_esperada_original TIMESTAMP NOT NULL,
    fecha_esperada_nueva TIMESTAMP,
    estado VARCHAR(20),
    motivo_solicitud TEXT NOT NULL,
    motivo_rechazo VARCHAR(255)
);

CREATE INDEX idx_renovacion_prestamo ON renovaciones_libros (id_prestamo_libro);
CREATE INDEX idx_renovacion_usuario ON renovaciones_libros (id_usuario);
