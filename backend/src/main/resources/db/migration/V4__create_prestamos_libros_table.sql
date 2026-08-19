-- V4__create_prestamos_libros_table.sql
CREATE TYPE estado_prestamo_libro AS ENUM ('pendiente', 'aceptado', 'rechazado', 'devuelto');

CREATE TABLE prestamos_libros (
    id_prestamo_libro BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    id_libro BIGINT NOT NULL REFERENCES libros (id_libro),
    id_administrador BIGINT REFERENCES usuarios (id_usuario),
    fecha_solicitud TIMESTAMP,
    fecha_aprobacion TIMESTAMP,
    fecha_devolucion_esperada TIMESTAMP,
    fecha_devolucion_real TIMESTAMP,
    estado estado_prestamo_libro,
    razon_rechazo VARCHAR(255),
    observaciones TEXT,
    notificacion_vencimiento_enviada BOOLEAN NOT NULL DEFAULT FALSE,
    notificacion_vencido_enviada BOOLEAN NOT NULL DEFAULT FALSE,
    observacion_devolucion TEXT,
    estado_fisico_devolucion VARCHAR(20),
    renovaciones_aplicadas INTEGER NOT NULL DEFAULT 0,
    estado_renovacion VARCHAR(20)
);

CREATE INDEX idx_prestamo_usuario ON prestamos_libros (id_usuario);
CREATE INDEX idx_prestamo_libro ON prestamos_libros (id_libro);
CREATE INDEX idx_prestamo_estado ON prestamos_libros (estado);
