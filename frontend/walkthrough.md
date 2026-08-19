# Renovación Completa del Frontend — Biblioteca & Almacén SENA ADSO

Se ha rediseñado y modernizado toda la experiencia visual y funcional del frontend de la aplicación.

---

## Mejoras Principales Implementadas

### 1. Sistema de Diseño y Estilos Modernos (`index.css` & Google Fonts)
- **Tipografía:** Integración de **Plus Jakarta Sans** e **Inter** con jerarquías claras.
- **Paleta de Colores Curada:** Acentos en índigo vibrante (`#4f46e5`), esmeralda (`#10b981`), ámbar (`#f59e0b`), púrpura (`#8b5cf6`) y rosa (`#ef4444`).
- **Sidebar & Topbar:** Barra lateral en tono azul marino profundo (`#0f172a` a `#111c35`) con efectos de iluminación (*glow*), píldoras de navegación activas, avatar de usuario con iniciales y barra superior (*topbar*) fija con efecto de cristal translúcido (*backdrop blur*).
- **Micro-interacciones y Animaciones:** Transiciones suaves en botones, elevación (*lift*) en tarjetas al hacer *hover*, animaciones de entrada `fadeInUp` y modales emergentes con desenfoque de fondo.

### 2. Inicio de Sesión (`Login.jsx`)
- Fondo degradado con orbes de luz ambiental.
- **Selector Rápido de Roles (1-Clic):** Botones con credenciales instantáneas de demostración para ingresar como *Administrador*, *Bibliotecario*, *Almacenista*, *Aprendiz* o *Instructor* sin necesidad de escribir contraseñas manualmente.
- Campos de formulario con iconos (*Mail*, *Lock*) y estados de carga.

### 3. Panel de Control / Dashboard (`Home.jsx`)
- **Banner Hero:** Saludo personalizado según rol, resumen institucional e indicadores en tiempo real.
- **Tarjetas KPI Interactivas:** Métricas de inventario y préstamos con contenedores de iconos e insignias de color.
- **Accesos Rápidos:** Enlaces directos a los módulos autorizados para el usuario.

### 4. Catálogos y Formularios Modales (`Libros.jsx` & `Equipos.jsx`)
- **Barra de Búsqueda y Filtros:** Búsqueda en vivo y filtrado dinámico por estado y tipo.
- **Modales Flotantes:** Reemplazo de formularios expansivos por diálogos modales limpios con validaciones.
- **Gestión de Equipos:** Eliminación de `window.prompt()` para cambio de estado, reemplazado por un modal dedicado con selección de administrador y motivo obligatorio. Modal de historial de estados.
- **Badges:** Insignias de estado con punto indicador brillante (*glowing dot*).

### 5. Préstamos y Sanciones (`Prestamos.jsx`, `PrestamosEquipos.jsx`, `Multas.jsx`)
- **Navegación por Pestañas:** Filtros por estado (*Todos*, *Pendiente*, *Aceptado*, *Devolver*, *Rechazado*).
- **Modales de Flujo Completo:** Ventanas emergentes para solicitud, entrega/devolución con estado físico, solicitud de renovación y rechazo justificado.
- **Gestión de Sanciones:** Tarjetas estadísticas de multas activas/acumulando y modal de condonación de faltas.

### 6. Reportes y Exportación (`Reportes.jsx`)
- Vistas segmentadas para Inventario, Préstamos, Mis Préstamos y Usuarios Activos.
- Botones de descarga directa en formato **Excel XLSX**.

---

## Verificación
- Compilación de producción con **Vite** ejecutada con éxito (`npm run build` en 845ms).
- Servidor de desarrollo Vite activo en [http://localhost:5173](http://localhost:5173).
