package co.sena.adso.biblioteca.config;

import co.sena.adso.biblioteca.entity.*;
import co.sena.adso.biblioteca.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final EquipoRepository equipoRepository;
    private final PrestamoLibroRepository prestamoLibroRepository;
    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(@Lazy UsuarioRepository usuarioRepository,
                      @Lazy LibroRepository libroRepository,
                      @Lazy EquipoRepository equipoRepository,
                      @Lazy PrestamoLibroRepository prestamoLibroRepository,
                      @Lazy PrestamoRepository prestamoRepository,
                      @Lazy MultaRepository multaRepository,
                      @Lazy PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.equipoRepository = equipoRepository;
        this.prestamoLibroRepository = prestamoLibroRepository;
        this.prestamoRepository = prestamoRepository;
        this.multaRepository = multaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        try {
            log.info("Iniciando DataSeeder (ApplicationReadyEvent): Creación de datos ficticios...");

            // 1. Usuarios con sus roles
            Usuario admin = crearOActualizarUsuario("admin@email.com", "Admin1234", "Administrador", "Principal", RolUsuario.administrador);
            Usuario bibliotecario = crearOActualizarUsuario("carlos@email.com", "123456", "Carlos Andrés", "Mendoza Gómez", RolUsuario.bibliotecario);
            Usuario almacenista = crearOActualizarUsuario("almacen@email.com", "Clave1234", "Jorge Iván", "Almacén Ríos", RolUsuario.almacenista);
            Usuario maria = crearOActualizarUsuario("maria@email.com", "654321", "María Camila", "Pérez Restrepo", RolUsuario.aprendiz);
            Usuario pedro = crearOActualizarUsuario("pedro@email.com", "Clave1234", "Pedro Antonio", "Giraldo López", RolUsuario.instructor);
            Usuario diego = crearOActualizarUsuario("diego@email.com", "Clave1234", "Diego Alejandro", "Ramírez Ortiz", RolUsuario.aprendiz);
            Usuario laura = crearOActualizarUsuario("laura@email.com", "Clave1234", "Laura Sofía", "Valencia Díaz", RolUsuario.aprendiz);

            // Asegurar que cualquier usuario preexistente en base de datos esté verificado y activo
            usuarioRepository.findAll().forEach(u -> {
                boolean cambiado = false;
                if (!Boolean.TRUE.equals(u.getEmailVerificado())) {
                    u.setEmailVerificado(true);
                    cambiado = true;
                }
                if (u.getEstado() == null) {
                    u.setEstado(EstadoUsuario.activo);
                    cambiado = true;
                }
                if (cambiado) {
                    usuarioRepository.save(u);
                }
            });

            // 2. Libros Ficticios
            Libro l1 = crearLibroSiNoExiste("LIB-001", "Cien años de soledad", "Gabriel García Márquez", "Novela / Realismo Mágico", EstadoLibro.disponible, "Estante A-12", 15, "Obra cumbre de la literatura hispanoamericana.");
            Libro l2 = crearLibroSiNoExiste("LIB-002", "Clean Code: Manual de estilo para desarrollo ágil", "Robert C. Martin", "Ingeniería de Software", EstadoLibro.prestado, "Estante B-04", 10, "Principios y patrones para escribir código limpio y mantenible.");
            Libro l3 = crearLibroSiNoExiste("LIB-003", "El ingenioso hidalgo Don Quijote de la Mancha", "Miguel de Cervantes", "Clásico Universal", EstadoLibro.disponible, "Estante A-01", 20, "Edición conmemorativa de la Real Academia Española.");
            Libro l4 = crearLibroSiNoExiste("LIB-004", "Design Patterns: Elements of Reusable Object-Oriented Software", "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", "Arquitectura de Software", EstadoLibro.prestado, "Estante B-05", 15, "Catálogo de 23 patrones de diseño orientado a objetos.");
            Libro l5 = crearLibroSiNoExiste("LIB-005", "Spring in Action (6th Edition)", "Craig Walls", "Desarrollo Web / Frameworks", EstadoLibro.disponible, "Estante B-08", 12, "Guía completa de Spring Boot 3, Spring Security y Reactive Streams.");
            Libro l6 = crearLibroSiNoExiste("LIB-006", "Introduction to Algorithms (CLRS 4th Ed)", "Thomas H. Cormen", "Ciencias de la Computación", EstadoLibro.mantenimiento, "Estante C-02", 7, "Texto de referencia global en diseño y análisis de algoritmos.");
            Libro l7 = crearLibroSiNoExiste("LIB-007", "Database System Concepts (7th Edition)", "Abraham Silberschatz", "Bases de Datos", EstadoLibro.disponible, "Estante C-09", 14, "Modelado relacional, normalización y motores de base de datos distribuidas.");

            // 3. Equipos Ficticios
            Equipo eq1 = crearEquipoSiNoExiste("SN-LEN-88219", "Portátil Lenovo ThinkPad E14 Gen 4", "Portátil", "Lenovo", "ThinkPad E14", EstadoEquipo.prestado, "Laboratorio TIC 1", 3, "Intel Core i7-1255U, 16GB RAM, SSD 512GB NVMe");
            Equipo eq2 = crearEquipoSiNoExiste("SN-EPS-10492", "Video Proyector Epson PowerLite E20", "Audiovisual", "Epson", "PowerLite E20", EstadoEquipo.prestado, "Auditorio Principal", 1, "Proyector 3400 lúmenes, HDMI/VGA, resolución XGA");
            Equipo eq3 = crearEquipoSiNoExiste("SN-DEL-55912", "Portátil Dell Latitude 5420", "Portátil", "Dell", "Latitude 5420", EstadoEquipo.disponible, "Laboratorio TIC 2", 3, "Core i5-1145G7, 16GB RAM, Windows 11 Pro");
            Equipo eq4 = crearEquipoSiNoExiste("SN-ARD-33901", "Kit Arduino Mega 2560 Starter Pro", "Electrónica", "Arduino", "Mega 2560 R3", EstadoEquipo.disponible, "Mesa de Electrónica 4", 5, "Incluye sensores ultrasonido, servomotores y pantalla LCD I2C");
            Equipo eq5 = crearEquipoSiNoExiste("SN-SAM-77123", "Tablet Samsung Galaxy Tab S7 FE", "Tablet", "Samsung", "Galaxy Tab S7 FE", EstadoEquipo.mantenimiento, "Almacén General", 2, "Pantalla 12.4 pulgadas con S-Pen para diseño y diagramación");
            Equipo eq6 = crearEquipoSiNoExiste("SN-RIG-44018", "Osciloscopio Digital Rigol DS1054Z", "Instrumentación", "Rigol", "DS1054Z", EstadoEquipo.fuera_de_servicio, "Taller de Mantenimiento", 1, "50 MHz, 4 canales digitales de alta precisión");

            // 4. Préstamos de Libros
            LocalDateTime ahora = LocalDateTime.now();
            crearPrestamoLibroSiNoExiste(maria, l2, bibliotecario, ahora.minusDays(5), ahora.minusDays(5), ahora.plusDays(5), EstadoPrestamoLibro.aceptado, "Préstamo activo para proyecto de trimestre ADSO");
            crearPrestamoLibroSiNoExiste(pedro, l4, bibliotecario, ahora.minusDays(8), ahora.minusDays(8), ahora.plusDays(7), EstadoPrestamoLibro.aceptado, "Préstamo para preparación de clase de Patrones de Diseño");
            crearPrestamoLibroSiNoExiste(diego, l6, bibliotecario, ahora.minusDays(15), ahora.minusDays(15), ahora.minusDays(5), EstadoPrestamoLibro.aceptado, "Préstamo vencido con retraso de 5 días");
            crearPrestamoLibroSiNoExiste(laura, l1, bibliotecario, ahora.minusDays(20), ahora.minusDays(20), ahora.minusDays(5), EstadoPrestamoLibro.devuelto, "Préstamo devuelto satisfactoriamente");

            // 5. Préstamos de Equipos
            crearPrestamoEquipoSiNoExiste(maria, eq1, almacenista, ahora.minusDays(2), ahora.minusDays(2), ahora.plusDays(1), EstadoPrestamo.aceptado, "Préstamo de portátil para desarrollo de backend Spring Boot");
            crearPrestamoEquipoSiNoExiste(pedro, eq2, almacenista, ahora.minusDays(1), ahora.minusDays(1), ahora.plusDays(1), EstadoPrestamo.aceptado, "Préstamo de proyector para sustentación de fichas");
            crearPrestamoEquipoSiNoExiste(laura, eq4, almacenista, ahora.minusDays(10), ahora.minusDays(10), ahora.minusDays(5), EstadoPrestamo.devuelto, "Kit devuelto completo en buen estado");

            // 6. Multas y Sanciones Ficticias
            crearMultaSiNoExiste(diego, TipoRecurso.libro, 5, 5, ahora.minusDays(5), ahora.minusDays(5), ahora, EstadoMulta.activa, "Suspensión activa por 5 días de retraso en devolución de libro CLRS.");
            crearMultaSiNoExiste(laura, TipoRecurso.equipo, 2, 2, ahora.minusDays(25), ahora.minusDays(25), ahora.minusDays(23), EstadoMulta.cumplida, "Sanción cumplida satisfactoriamente tras devolución.");
            crearMultaSiNoExiste(maria, TipoRecurso.libro, 1, 1, ahora.minusDays(30), ahora.minusDays(30), ahora.minusDays(29), EstadoMulta.condonada, "Condonada por justificación médica presentada oportunamente.");

            log.info("DataSeeder finalizado con éxito: Base de datos poblada.");
        } catch (Exception e) {
            log.error("Error durante la inicialización de datos en DataSeeder: {}", e.getMessage(), e);
        }
    }

    private Usuario crearOActualizarUsuario(String correo, String pass, String nombres, String apellidos, RolUsuario rol) {
        return usuarioRepository.findByCorreoIgnoreCase(correo).map(u -> {
            u.setNombres(nombres);
            u.setApellidos(apellidos);
            u.setRol(rol);
            u.setEstado(EstadoUsuario.activo);
            u.setEmailVerificado(true);
            u.setPassword(passwordEncoder.encode(pass));
            return usuarioRepository.save(u);
        }).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setCorreo(correo.toLowerCase());
            u.setPassword(passwordEncoder.encode(pass));
            u.setNombres(nombres);
            u.setApellidos(apellidos);
            u.setRol(rol);
            u.setEstado(EstadoUsuario.activo);
            u.setEmailVerificado(true);
            u.setFechaRegistro(LocalDateTime.now());
            return usuarioRepository.save(u);
        });
    }

    private Libro crearLibroSiNoExiste(String codigo, String titulo, String autor, String genero, EstadoLibro estado, String ubicacion, int tiempoMax, String descripcion) {
        return libroRepository.findByCodigoUnico(codigo).orElseGet(() -> {
            Libro l = new Libro();
            l.setCodigoUnico(codigo);
            l.setTitulo(titulo);
            l.setAutor(autor);
            l.setGenero(genero);
            l.setEstado(estado);
            l.setUbicacion(ubicacion);
            l.setTiempoMaxPrestamo(tiempoMax);
            l.setDescripcion(descripcion);
            l.setDisponiblePrestamo(estado == EstadoLibro.disponible);
            l.setFechaRegistro(LocalDateTime.now());
            l.setEliminado(false);
            return libroRepository.save(l);
        });
    }

    private Equipo crearEquipoSiNoExiste(String serie, String nombre, String tipo, String marca, String modelo, EstadoEquipo estado, String ubicacion, int tiempoMax, String descripcion) {
        return equipoRepository.findByNumeroSerie(serie).orElseGet(() -> {
            Equipo eq = new Equipo();
            eq.setNumeroSerie(serie);
            eq.setNombre(nombre);
            eq.setTipoEquipo(tipo);
            eq.setMarca(marca);
            eq.setModelo(modelo);
            eq.setEstado(estado);
            eq.setUbicacion(ubicacion);
            eq.setTiempoMaxPrestamo(tiempoMax);
            eq.setDescripcion(descripcion);
            eq.setDisponiblePrestamo(estado == EstadoEquipo.disponible);
            eq.setFechaRegistro(LocalDateTime.now());
            eq.setEliminado(false);
            return equipoRepository.save(eq);
        });
    }

    private void crearPrestamoLibroSiNoExiste(Usuario u, Libro l, Usuario admin, LocalDateTime fSol, LocalDateTime fApr, LocalDateTime fDevEsp, EstadoPrestamoLibro estado, String obs) {
        List<PrestamoLibro> existentes = prestamoLibroRepository.findByUsuarioId(u.getId());
        boolean yaExiste = existentes.stream().anyMatch(p -> p.getLibro() != null && p.getLibro().getId().equals(l.getId()) && p.getEstado() == estado);
        if (!yaExiste) {
            PrestamoLibro p = new PrestamoLibro();
            p.setUsuario(u);
            p.setLibro(l);
            p.setAdministrador(admin);
            p.setFechaSolicitud(fSol);
            p.setFechaAprobacion(fApr);
            p.setFechaDevolucionEsperada(fDevEsp);
            if (estado == EstadoPrestamoLibro.devuelto) {
                p.setFechaDevolucionReal(fDevEsp);
            }
            p.setEstado(estado);
            p.setObservaciones(obs);
            prestamoLibroRepository.save(p);
        }
    }

    private void crearPrestamoEquipoSiNoExiste(Usuario u, Equipo eq, Usuario admin, LocalDateTime fSol, LocalDateTime fApr, LocalDateTime fDevEsp, EstadoPrestamo estado, String obs) {
        List<Prestamo> existentes = prestamoRepository.findByUsuarioId(u.getId());
        boolean yaExiste = existentes.stream().anyMatch(p -> p.getEquipo() != null && p.getEquipo().getId().equals(eq.getId()) && p.getEstado() == estado);
        if (!yaExiste) {
            Prestamo p = new Prestamo();
            p.setUsuario(u);
            p.setEquipo(eq);
            p.setAdministrador(admin);
            p.setFechaSolicitud(fSol);
            p.setFechaAprobacion(fApr);
            p.setFechaDevolucionEsperada(fDevEsp);
            if (estado == EstadoPrestamo.devuelto) {
                p.setFechaDevolucionReal(fDevEsp);
            }
            p.setEstado(estado);
            p.setObservaciones(obs);
            prestamoRepository.save(p);
        }
    }

    private void crearMultaSiNoExiste(Usuario u, TipoRecurso tipo, int retraso, int suspension, LocalDateTime fGen, LocalDateTime fIni, LocalDateTime fFin, EstadoMulta estado, String obs) {
        List<Multa> existentes = multaRepository.findByUsuarioId(u.getId());
        boolean yaExiste = existentes.stream().anyMatch(m -> m.getEstado() == estado && m.getTipoRecurso() == tipo);
        if (!yaExiste) {
            Multa m = new Multa();
            m.setUsuario(u);
            m.setTipoRecurso(tipo);
            m.setDiasRetraso(retraso);
            m.setDiasSuspension(suspension);
            m.setFechaGeneracion(fGen);
            m.setFechaInicioSuspension(fIni);
            m.setFechaFinSuspension(fFin);
            m.setEstado(estado);
            m.setObservacion(obs);
            m.setCreatedAt(fGen != null ? fGen : LocalDateTime.now());
            m.setUpdatedAt(fGen != null ? fGen : LocalDateTime.now());
            multaRepository.save(m);
        }
    }
}
