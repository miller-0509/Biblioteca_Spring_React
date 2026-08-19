# Despliegue en Coolify — Biblioteca_Spring_React

Guía paso a paso para desplegar el proyecto completo (backend Spring Boot + frontend React) en Coolify del servidor `144.91.74.225`, usando la base de datos `master_db` existente.

## Arquitectura

| Servicio | Imagen | Puerto interno | Dominio |
|---|---|---|---|
| Backend (Spring Boot, perfil `prod`) | multi-stage JDK 21 | 8080 | `https://api.biblioteca.spring.softlane.click` |
| Frontend (React + Vite → nginx) | multi-stage Node 22 / nginx | 80 | `https://biblioteca.spring.softlane.click` |

- El frontend sirve la SPA y redirige `/api/*` al backend (`nginx.conf` usa `BACKEND_URL`).
- La base de datos es la misma `master_db` de siempre: `144.91.74.225:5434` (usuario `admin`).
- Los secretos NO van en el repo: se definen como variables de entorno en Coolify.

## 1. Requisitos previos

1. Subir los cambios a GitHub (Dockerfiles, nginx.conf, `application-prod.properties`).
2. Tener el servidor de Coolify conectado (el mismo VPS `144.91.74.225`).
3. DNS: registrar apuntando al VPS:
   - `biblioteca.spring.softlane.click` → A `144.91.74.225`
   - `api.biblioteca.spring.softlane.click` → A `144.91.74.225`
   (si los dominios ya existen en tu proveedor DNS, solo agrega/actualiza los registros A)

## 2. Backend — crear recurso en Coolify

1. Proyecto → **+ New** → **Public Repository** (el repo `Biblioteca_Spring_React` ya es público).
2. Pegar `https://github.com/miller-0509/Biblioteca_Spring_React` → **Check Repository** → rama `master`.
3. **Build Pack**: `Dockerfile`.
4. **Base Directory**: `/backend` (contexto de build).
5. **Dockerfile Location**: `Dockerfile`.
6. **Continue**.
7. En **Configuration**:
   - **Ports Exposes**: `8080`
   - **Domains**: `api.biblioteca.spring.softlane.click` (activa HTTPS / Let's Encrypt).
   - **Environment Variables** (tab **Environment Variables**):

| Variable | Valor |
|---|---|
| `DB_HOST` | `144.91.74.225` |
| `DB_PORT` | `5434` |
| `DB_NAME` | `master_db` |
| `DB_USER` | `admin` |
| `DB_PASSWORD` | (tu contraseña de master_db) |
| `JWT_SECRET` | (genera una clave larga, ej. `openssl rand -base64 64`) |
| `JWT_EXPIRATION` | `86400000` |
| `CORS_ALLOWED_ORIGINS` | `https://biblioteca.spring.softlane.click` |
| `FRONTEND_URL` | `https://biblioteca.spring.softlane.click` |
| `MAIL_HOST` | `smtp.gmail.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | `caperamiller5@gmail.com` |
| `MAIL_PASSWORD` | (contraseña de aplicación de Gmail) |
| `MAIL_FROM` | `caperamiller5@gmail.com` |
| `MAIL_SENDER_NAME` | `Biblioteca SENA ADSO` |

8. **Deploy** y revisa el log de build (primera vez tarda varios minutos: descarga Maven + dependencias).
9. Verificación: `https://api.biblioteca.spring.softlane.click/actuator/health` → `{"status":"UP"}`.

> Notas:
> - El contenedor ya fija `SERVER_PORT=8080` y `SPRING_PROFILES_ACTIVE=prod`.
> - Flyway arranca con `baseline-version=8`: como `master_db` ya tiene el esquema, las migraciones V1–V8 se omiten y solo corren las futuras (V9+). En una BD nueva vacía, se aplican todas desde cero.
> - `ddl-auto=validate` verifica que las entidades coincidan con el esquema existente; si falla el arranque por validación, revisa el log (posible desajuste entre entidades y BD).

## 3. Frontend — crear recurso en Coolify

1. Proyecto → **+ New** → **Public Repository**, mismo repo y rama `master`.
2. **Build Pack**: `Dockerfile`.
3. **Base Directory**: `/frontend`.
4. **Dockerfile Location**: `Dockerfile`.
5. **Continue**.
6. En **Configuration**:
   - **Ports Exposes**: `80`
   - **Domains**: `biblioteca.spring.softlane.click` (HTTPS / Let's Encrypt).
   - **Environment Variables**:

| Variable | Valor |
|---|---|
| `BACKEND_URL` | `https://api.biblioteca.spring.softlane.click` |

> El nginx del contenedor sustituye `${BACKEND_URL}` al arrancar y redirige `/api/*` ahí. No debe terminar en `/`.

7. **Deploy**.
8. Verificación: abrir `https://biblioteca.spring.softlane.click`, iniciar sesión con un usuario seed y probar listar libros/equipos.

## 4. Verificación final

- [ ] `/actuator/health` del backend responde `UP`.
- [ ] Login por `https://biblioteca.spring.softlane.click` funciona (usuario BCrypt y usuario scrypt).
- [ ] Un registro nuevo recibe el correo de verificación con enlaces apuntando al dominio real.
- [ ] Swagger: `https://api.biblioteca.spring.softlane.click/swagger-ui.html`.

## 5. Actualizaciones futuras

Tras un commit a `master` del repo, abre el recurso en Coolify y pulsa **Deploy** (o configura *Auto Deploy* en la pestaña General). La base de datos no se toca: `validate` + Flyway solo aplican migraciones nuevas.

## Troubleshooting

| Problema | Causa probable / solución |
|---|---|
| Backend no inicia, `BeanCreationException` con datasource | `DB_*` mal configurados o `master_db` inalcanzable desde el contenedor (revisa `DB_HOST`/`DB_PORT`) |
| Fallo de validación de Hibernate al arrancar | La BD no coincide con las entidades; revisa el log y si hace falta `ddl-auto=update` temporal |
| Frontend carga pero `/api` da 502 | `BACKEND_URL` incorrecto o backend caído; verifica el dominio de la API |
| Correos no salen | `MAIL_*` vacíos → el servicio registra el enlace en consola como respaldo; revisa los logs del backend |
| `400` en login de usuarios creados por Flask | Ya resuelto con `CompatiblePasswordEncoder` (BCrypt + scrypt werkzeug) |