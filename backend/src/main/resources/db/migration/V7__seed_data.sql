-- V7__seed_data.sql
-- Seed data de ejemplo para el dominio de biblioteca/almacén.
-- NOTA: solo inserta si las tablas están vacías. Las contraseñas están hasheadas con BCrypt.
-- Usuarios de prueba (contraseñas):
--   admin@email.com    / Admin1234   (administrador)
--   carlos@email.com   / 123456      (bibliotecario)
--   almacen@email.com  / Clave1234   (almacenista)
--   pedro@email.com    / Clave1234   (instructor)
--   maria@email.com    / 654321      (aprendiz)

INSERT INTO usuarios (nombres, apellidos, correo, password, rol, estado, fecha_registro, email_verificado)
SELECT 'Adriana', 'Vargas', 'admin@email.com', '$2a$10$rSRBlnEQpyOfIW9QTDKxD.xiNZJrYrLDqdFQYz6BmYV2neV9ysLzO', 'administrador', 'activo', CURRENT_TIMESTAMP, TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuarios LIMIT 1);

INSERT INTO usuarios (nombres, apellidos, correo, password, rol, estado, fecha_registro, email_verificado)
SELECT 'Carlos', 'Rueda', 'carlos@email.com', '$2a$10$H/lAZid0S5W/SGBlq0/vC.09yyb75mIz5ceelsPg2UBZIcYNAuOba', 'bibliotecario', 'activo', CURRENT_TIMESTAMP, TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'carlos@email.com');

INSERT INTO usuarios (nombres, apellidos, correo, password, rol, estado, fecha_registro, email_verificado)
SELECT 'Luis', 'Mora', 'almacen@email.com', '$2a$10$eGi3qYVCl5XhHtNYwVKKWO9BtJjvnF/Kz95GCV2dofwl5jT7LAwU6', 'almacenista', 'activo', CURRENT_TIMESTAMP, TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'almacen@email.com');

INSERT INTO usuarios (nombres, apellidos, correo, password, rol, estado, fecha_registro, email_verificado)
SELECT 'Pedro', 'Díaz', 'pedro@email.com', '$2a$10$eGi3qYVCl5XhHtNYwVKKWO9BtJjvnF/Kz95GCV2dofwl5jT7LAwU6', 'instructor', 'activo', CURRENT_TIMESTAMP, TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'pedro@email.com');

INSERT INTO usuarios (nombres, apellidos, correo, password, rol, estado, fecha_registro, email_verificado)
SELECT 'María', 'Gómez', 'maria@email.com', '$2a$10$tyWpXvsGhkuJFD.YIR8lhOIKGKW6vNJhzsrhaU5DENlO4hOTcpmsS', 'aprendiz', 'activo', CURRENT_TIMESTAMP, TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'maria@email.com');

INSERT INTO libros (titulo, autor, genero, codigo_unico, estado, ubicacion, fecha_registro, disponible_prestamo, tiempo_max_prestamo)
SELECT 'Cien años de soledad', 'Gabriel García Márquez', 'Novela', 'LIB-001', 'disponible', 'Estante A1', CURRENT_TIMESTAMP, TRUE, 15
WHERE NOT EXISTS (SELECT 1 FROM libros LIMIT 1);

INSERT INTO libros (titulo, autor, genero, codigo_unico, estado, ubicacion, fecha_registro, disponible_prestamo, tiempo_max_prestamo)
SELECT 'Clean Code', 'Robert C. Martin', 'Técnico', 'LIB-002', 'disponible', 'Estante B2', CURRENT_TIMESTAMP, TRUE, 10
WHERE NOT EXISTS (SELECT 1 FROM libros WHERE codigo_unico = 'LIB-002');

INSERT INTO equipos (nombre, tipo_equipo, marca, modelo, numero_serie, estado, ubicacion, fecha_registro, disponible_prestamo, tiempo_max_prestamo)
SELECT 'Portátil HP ProBook', 'Computador', 'HP', 'ProBook 450', 'SN-HP-001', 'disponible', 'Sala de sistemas', CURRENT_TIMESTAMP, TRUE, 7
WHERE NOT EXISTS (SELECT 1 FROM equipos LIMIT 1);

INSERT INTO equipos (nombre, tipo_equipo, marca, modelo, numero_serie, estado, ubicacion, fecha_registro)
SELECT 'Proyector Epson', 'Audiovisual', 'Epson', 'EB-X51', 'SN-EPS-002', 'mantenimiento', 'Sala 3', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM equipos WHERE numero_serie = 'SN-EPS-002');
