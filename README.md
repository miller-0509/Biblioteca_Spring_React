# Biblioteca API — Spring Boot 3

API REST de **gestión de biblioteca/almacén** (SENA ADSO): libros, equipos, usuarios, préstamos de libros y equipos, renovaciones, multas/suspensiones, historial de estados, reportes con exportación Excel y autenticación JWT con verificación de email. Incluye un frontend React.

> Nota: el proyecto se llama **Biblioteca_Spring_React**. Antes conservaba el nombre histórico `fincas-api` (carpeta, artifactId y paquete `co.sena.adso.fincasapi`), ya renombrado. La base de datos de desarrollo en Coolify es `master_db`. Se replica el sistema Flask de referencia (`Proyecto_gestion_biblioteca`).

Repositorio: <https://github.com/miller-0509/Biblioteca_Spring_React>

## Requisitos

- JDK 21 o superior.
- PostgreSQL 16 o superior en `localhost:5434`.
- API Spring Boot en `http://localhost:31026`.
- Node 18+ si vas a levantar el frontend React.

## Estructura del proyecto

- `backend/` — API Spring Boot (Maven, `pom.xml`, `mvnw`, código en `src/`, `requests.http`, `run-dev.ps1`, `.env`).
- `frontend/` — Aplicación React + Vite (código en `src/`, `package.json`, `vite.config.js`).

## Arranque reproducible

### Backend

1. En `backend/`, copia `.env.example` como `.env` y revisa usuario, contraseña y base.
2. Abre PowerShell en `backend/`.
3. Ejecuta:

```powershell
.\mvnw spring-boot:run
```

El proyecto usa `spring-boot-devtools`. En desarrollo se activa `app.security.enabled=true` (JWT obligatorio); puedes desactivarla solo para practicar CRUD sin token. El token se obtiene en `POST /api/auth/login`.

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

## Recorrido recomendado

1. `POST /api/auth/login` con `admin@email.com / Admin1234` (usuario de seed) y usa el token para el resto.
2. Revisa `controller/PrestamoController.java` y `PrestamoLibroController.java` (flujos de préstamo).
3. Revisa `MultaService`, `MultasScheduler` y `RecordatoriosScheduler` (multas y recordatorios automáticos).
4. Prueba los reportes JSON y la exportación Excel en `ReporteController`.
5. Revisa `SecurityConfig` para el RBAC de 5 roles.
6. Prueba los endpoints de `backend/requests.http`.

## Contrato de la API

### Autenticación — `/api/auth`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Login con correo/contraseña → JWT. Staff (admin/bibliotecario/almacenista) no requiere verificar email; aprendiz/instructor sí |
| POST | `/api/auth/registro` | Registro de usuario (rol aprendiz por defecto), genera token de verificación |
| GET | `/api/auth/verificar/{token}` | Verifica el correo (token one-time de 60 min) |
| POST | `/api/auth/reenviar-verificacion` | Reenvía el enlace de verificación |
| POST | `/api/auth/recuperar-password` | Solicita recuperación de contraseña |
| POST | `/api/auth/restablecer-password/{token}` | Restablece la contraseña con token |
| GET | `/api/auth/me` | Datos del usuario autenticado |

En desarrollo el "correo" se imprime en consola (no se envía SMTP real). En producción se puede conectar `spring.mail.*`.

### Libros — `/api/libros`

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/libros` | Lista de libros no eliminados y 200 |
| GET | `/api/libros/disponibles` | Solo libros disponibles |
| GET | `/api/libros/{id}` | DTO y 200; si no existe, 404 |
| POST | `/api/libros` | DTO creado y 201 (código único duplicado → 422) |
| PUT | `/api/libros/{id}` | DTO actualizado y 200 |
| PUT | `/api/libros/{id}/estado` | DTO con nuevo `estado` y 200 |
| DELETE | `/api/libros/{id}` | Sin cuerpo y 204 (borrado lógico) |

### Equipos — `/api/equipos`

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/equipos` | Lista de equipos no eliminados y 200 |
| GET | `/api/equipos/disponibles` | Solo equipos disponibles |
| GET | `/api/equipos/{id}` | DTO y 200; si no existe, 404 |
| POST | `/api/equipos` | DTO creado y 201 (número de serie duplicado → 422) |
| PUT | `/api/equipos/{id}` | DTO actualizado y 200 |
| PUT | `/api/equipos/{id}/estado` | DTO con nuevo `estado` y 200 |
| DELETE | `/api/equipos/{id}` | Sin cuerpo y 204 (borrado lógico) |

### Usuarios — `/api/usuarios`

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/usuarios` | Lista y 200 |
| GET | `/api/usuarios/{id}` | DTO y 200; si no existe, 404 |
| GET | `/api/usuarios/correo/{correo}` | Búsqueda por correo y 200; si no existe, 404 |
| GET | `/api/usuarios/{id}/historial` | Historial unificado de préstamos (libros + equipos) con estado `atrasado` y estadísticas; admin o el propio usuario |
| POST | `/api/usuarios` | DTO creado y 201 (correo duplicado → 422) |
| PUT | `/api/usuarios/{id}` | DTO actualizado y 200 (correo en uso por otro usuario → 422) |
| DELETE | `/api/usuarios/{id}` | 204; elimina en cascada sus renovaciones, multas, préstamos e historial. No se puede eliminar la propia cuenta |

### Préstamos de equipos — `/api/prestamos`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/prestamos` | Lista (admin/almacenista) |
| GET | `/api/prestamos/usuario/{idUsuario}` | Préstamos de un usuario |
| GET | `/api/prestamos/equipo/{idEquipo}` | Préstamos de un equipo |
| GET | `/api/prestamos/{id}` | Detalle (con verificación de acceso) |
| POST | `/api/prestamos` | Solicitar préstamo. Usuario → `pendiente`; admin/almacenista → `aceptado` directo |
| PUT | `/api/prestamos/{id}/aceptar` | Aprobar solicitud |
| PUT | `/api/prestamos/{id}/rechazar` | Rechazar con razón |
| PUT | `/api/prestamos/{id}/devolver` | Devolver (cambia estado del equipo + historial + multa si hay retraso) |
| PUT | `/api/prestamos/{id}/renovar` | Solicitar renovación |
| PUT | `/api/prestamos/{id}/procesar-renovacion` | Aprobar/rechazar renovación |

### Préstamos de libros — `/api/prestamos-libros`

Mismo CRUD que antes (`GET`, `GET /{id}`, `GET /usuario/{id}`, `GET /libro/{id}`, `POST`, `PUT /{id}`, `DELETE`) más los flujos de negocio idénticos a equipos: `PUT /{id}/aceptar`, `PUT /{id}/rechazar`, `PUT /{id}/devolver`, `PUT /{id}/renovar`, `PUT /{id}/procesar-renovacion`.

### Renovaciones — `/api/renovaciones-libros` y `/api/renovaciones-equipos`

Historial de solicitudes de renovación: `GET`, `GET /{id}`, `GET /prestamo/{id}`, `GET /usuario/{id}` (+ CRUD genérico en libros).

### Multas / suspensiones — `/api/multas`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/multas?estado=activa` | Lista filtrada por rol (admin: todas; bibliotecario: libros; almacenista: equipos; aprendiz/instructor: las propias) |
| POST | `/api/multas/{id}/condonar` | Condonar sanción con observación (admin/bibliotecario/almacenista según tipo) |

### Reportes — `/api/reportes`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/reportes/dashboard` | Estadísticas según el rol |
| GET | `/api/reportes/inventario?estado=&tipo=&fechaInicio=&fechaFin=` | Inventario de equipos y libros |
| GET | `/api/reportes/prestamos?estado=&tipoRecurso=&fechaInicio=&fechaFin=` | Reporte de préstamos |
| GET | `/api/reportes/mis-prestamos?estado=` | Historial del usuario autenticado |
| GET | `/api/reportes/usuarios-activos` | Usuarios activos con préstamos (solo admin) |
| GET | `/api/reportes/exportar/excel/{tipo}` | Exportar Excel (`inventario_equipos`, `inventario_libros`, `prestamos_equipos`, `prestamos_libros`, `mis_prestamos`) |

### Historial de estados — `/api/historial-estado-libros` y `/api/historial-estado-equipos`

CRUD con filtros `GET /libro/{idLibro}` y `GET /equipo/{idEquipo}`. Guarda `estadoAnterior` → `estadoNuevo`, observación y administrador.

## Reglas de negocio

- **Límites de préstamos activos** (libros + equipos): aprendiz 3, instructor 8, bibliotecario 5, almacenista 5, admin sin límite (no solicita préstamos).
- **Bloqueo de recursos con préstamo activo**: un libro o equipo con préstamos en estado `pendiente`/`aceptado` no puede eliminarse ni cambiar de estado.
- **Renovaciones**: aprendiz 1, instructor 2; no se renueva un préstamo vencido ni con multas pendientes.
- **Multas**: `dias_gracia=1`, `factor_libro=1`, `factor_equipo=1` (configurables en properties). Estados `acumulando → activa → cumplida | condonada`. Una multa por préstamo (evita duplicados).
- **RBAC**: admin todo; bibliotecario libros y multas de libros; almacenista equipos y multas de equipos; aprendiz/instructor solo sus préstamos y multas.

## Tareas programadas

- `MultasScheduler` (diario): crea/actualiza multas `acumulando`, marca como `cumplida` las suspensiones vencidas.
- `RecordatoriosScheduler` (diario): avisos de "próximo a vencer" y "vencido" por email (consola en dev).

## Modelo de dominio

- `Libro` — título, autor, género, código único, `estado`, ubicación, disponibilidad, soft delete.
- `Equipo` — nombre, tipo, marca/modelo, número de serie único, `estado`, ubicación, soft delete.
- `Usuario` — nombres, apellidos, correo único, `rol`, `estado`, password (BCrypt), verificación de email.
- `Prestamo` / `PrestamoLibro` — préstamos de equipos y libros con flujo `pendiente → aceptado → devuelto` (o `rechazado`), renovaciones y notificaciones.
- `RenovacionEquipo` / `RenovacionLibro` — solicitudes de renovación.
- `Multa` — sanción por retraso (días de suspensión, observación, condonación).
- `TokenVerificacion` — tokens one-time para verificación y recuperación.
- `HistorialEstadoLibro` / `HistorialEstadoEquipo` — auditoría de cambios de estado.

Enums:

| Enum | Valores |
|---|---|
| `EstadoLibro` | `disponible`, `prestado`, `mantenimiento`, `dañado`, `perdido`, `eliminado` |
| `EstadoEquipo` | `disponible`, `prestado`, `mantenimiento`, `dañado`, `perdido`, `eliminado`, `fuera_de_servicio` |
| `EstadoPrestamoLibro` / `EstadoPrestamo` | `pendiente`, `aceptado`, `rechazado`, `devuelto` |
| `EstadoMulta` | `acumulando`, `activa`, `cumplida`, `condonada` |
| `RolUsuario` | `administrador`, `aprendiz`, `instructor`, `bibliotecario`, `almacenista` |
| `EstadoUsuario` | `activo`, `inactivo`, `bloqueado` |
| `TipoRecurso` | `libro`, `equipo` |
| `TipoToken` | `verificacion`, `recuperacion` |

## Errores

`GlobalExceptionHandler` centraliza las respuestas de error:

- **400** — errores de validación (Bean Validation).
- **401** — credenciales inválidas / token inválido.
- **403** — sin permisos para el recurso (RBAC).
- **404** — `ResourceNotFoundException`.
- **422** — `BusinessException` (reglas de negocio).

## Seguridad

- `JwtService` firma y valida tokens JWT (JJWT 0.12.6); `JwtAuthenticationFilter` carga el contexto de seguridad con `CurrentUser`.
- `SecurityConfig`: RBAC por patrón/método con `ROLE_<rol>`, handlers JSON 401/403, BCrypt para passwords.
- `POST /api/auth/**`, Swagger y health son públicos.

## Calidad

```powershell
cd backend
.\mvnw test
```

Tests unitarios de Service (Mockito) y de Controller (MockMvc). Cobertura con JaCoCo en `backend/target/site/jacoco`.

## Documentación de la API

- Swagger UI: `http://localhost:31026/swagger-ui.html`
- OpenAPI JSON: `http://localhost:31026/v3/api-docs`
