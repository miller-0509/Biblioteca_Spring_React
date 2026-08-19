-- V8__create_prestamos_equipos_multas_tokens.sql
-- Préstamos de equipos (análogo a prestamos_libros)
CREATE TYPE estado_prestamo AS ENUM ('pendiente', 'aceptado', 'rechazado', 'devuelto');
CREATE TYPE estado_multa AS ENUM ('acumulando', 'activa', 'cumplida', 'condonada');

CREATE TABLE prestamos (
    id_prestamo BIGSERIAL PRIMARY KEY,
    id_usuario BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    id_equipo BIGINT NOT NULL REFERENCES equipos (id_equipo),
    id_administrador BIGINT REFERENCES usuarios (id_usuario),
    fecha_solicitud TIMESTAMP,
    fecha_aprobacion TIMESTAMP,
    fecha_devolucion_esperada TIMESTAMP,
    fecha_devolucion_real TIMESTAMP,
    estado estado_prestamo,
    razon_rechazo VARCHAR(255),
    observaciones TEXT,
    notificacion_vencimiento_enviada BOOLEAN NOT NULL DEFAULT FALSE,
    notificacion_vencido_enviada BOOLEAN NOT NULL DEFAULT FALSE,
    observacion_devolucion TEXT,
    estado_fisico_devolucion VARCHAR(20),
    renovaciones_aplicadas INTEGER NOT NULL DEFAULT 0,
    estado_renovacion VARCHAR(20)
);

CREATE INDEX idx_prestamo_equipo_usuario ON prestamos (id_usuario);
CREATE INDEX idx_prestamo_equipo ON prestamos (id_equipo);
CREATE INDEX idx_prestamo_equipo_estado ON prestamos (estado);

CREATE TABLE renovaciones_equipos (
    id_renovacion BIGSERIAL PRIMARY KEY,
    id_prestamo BIGINT NOT NULL REFERENCES prestamos (id_prestamo),
    id_usuario BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    id_administrador BIGINT REFERENCES usuarios (id_usuario),
    fecha_solicitud TIMESTAMP,
    fecha_respuesta TIMESTAMP,
    fecha_esperada_original TIMESTAMP NOT NULL,
    fecha_esperada_nueva TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'pendiente',
    motivo_solicitud TEXT NOT NULL,
    motivo_rechazo VARCHAR(255)
);

CREATE INDEX idx_renovacion_equipo_prestamo ON renovaciones_equipos (id_prestamo);

CREATE TABLE multas (
    id_multa BIGSERIAL PRIMARY KEY,
    tipo_recurso VARCHAR(20) NOT NULL,
    id_prestamo_equipo BIGINT REFERENCES prestamos (id_prestamo),
    id_prestamo_libro BIGINT REFERENCES prestamos_libros (id_prestamo_libro),
    id_usuario BIGINT NOT NULL REFERENCES usuarios (id_usuario),
    dias_retraso INTEGER NOT NULL DEFAULT 0,
    dias_suspension INTEGER NOT NULL DEFAULT 0,
    fecha_generacion TIMESTAMP NOT NULL,
    fecha_inicio_suspension TIMESTAMP,
    fecha_fin_suspension TIMESTAMP,
    estado estado_multa,
    observacion TEXT,
    id_administrador_resolucion BIGINT REFERENCES usuarios (id_usuario),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_multa_usuario ON multas (id_usuario);
CREATE INDEX idx_multa_prestamo_equipo ON multas (id_prestamo_equipo);
CREATE INDEX idx_multa_prestamo_libro ON multas (id_prestamo_libro);
CREATE INDEX idx_multa_estado ON multas (estado);

CREATE TABLE tokens_verificacion (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    correo VARCHAR(150) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_token_correo ON tokens_verificacion (correo);
CREATE INDEX idx_token_token ON tokens_verificacion (token);
