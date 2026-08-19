-- V1__create_usuarios_table.sql
-- Tipos ENUM nativos de PostgreSQL (los espera Hibernate con @JdbcTypeCode(NAMED_ENUM))
CREATE TYPE rol_usuario AS ENUM ('administrador', 'aprendiz', 'instructor', 'bibliotecario', 'almacenista');
CREATE TYPE estado_usuario AS ENUM ('activo', 'inactivo', 'bloqueado');

CREATE TABLE usuarios (
    id_usuario BIGSERIAL PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol rol_usuario,
    estado estado_usuario,
    fecha_registro TIMESTAMP,
    email_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_verificacion TIMESTAMP
);

CREATE INDEX idx_usuario_correo ON usuarios (correo);
