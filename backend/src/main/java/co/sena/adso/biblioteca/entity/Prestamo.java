package co.sena.adso.biblioteca.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipo", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador")
    private Usuario administrador;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "fecha_devolucion_esperada")
    private LocalDateTime fechaDevolucionEsperada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "estado_prestamo")
    private EstadoPrestamo estado;

    @Column(name = "razon_rechazo", length = 255)
    private String razonRechazo;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "notificacion_vencimiento_enviada")
    private Boolean notificacionVencimientoEnviada = false;

    @Column(name = "notificacion_vencido_enviada")
    private Boolean notificacionVencidoEnviada = false;

    @Column(name = "observacion_devolucion", columnDefinition = "text")
    private String observacionDevolucion;

    @Column(name = "estado_fisico_devolucion", length = 20)
    private String estadoFisicoDevolucion;

    @Column(name = "renovaciones_aplicadas")
    private Integer renovacionesAplicadas = 0;

    @Column(name = "estado_renovacion", length = 20)
    private String estadoRenovacion;

    public Prestamo() {}

    public Prestamo(Usuario usuario, Equipo equipo, Usuario administrador, LocalDateTime fechaSolicitud, LocalDateTime fechaAprobacion, LocalDateTime fechaDevolucionEsperada, LocalDateTime fechaDevolucionReal, EstadoPrestamo estado, String razonRechazo, String observaciones, String observacionDevolucion, String estadoFisicoDevolucion, Integer renovacionesAplicadas, String estadoRenovacion) {
        this.usuario = usuario;
        this.equipo = equipo;
        this.administrador = administrador;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaAprobacion = fechaAprobacion;
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
        this.fechaDevolucionReal = fechaDevolucionReal;
        this.estado = estado;
        this.razonRechazo = razonRechazo;
        this.observaciones = observaciones;
        this.observacionDevolucion = observacionDevolucion;
        this.estadoFisicoDevolucion = estadoFisicoDevolucion;
        this.renovacionesAplicadas = renovacionesAplicadas;
        this.estadoRenovacion = estadoRenovacion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
    public Usuario getAdministrador() { return administrador; }
    public void setAdministrador(Usuario administrador) { this.administrador = administrador; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public LocalDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDateTime fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }
    public LocalDateTime getFechaDevolucionEsperada() { return fechaDevolucionEsperada; }
    public void setFechaDevolucionEsperada(LocalDateTime fechaDevolucionEsperada) { this.fechaDevolucionEsperada = fechaDevolucionEsperada; }
    public LocalDateTime getFechaDevolucionReal() { return fechaDevolucionReal; }
    public void setFechaDevolucionReal(LocalDateTime fechaDevolucionReal) { this.fechaDevolucionReal = fechaDevolucionReal; }
    public EstadoPrestamo getEstado() { return estado; }
    public void setEstado(EstadoPrestamo estado) { this.estado = estado; }
    public String getRazonRechazo() { return razonRechazo; }
    public void setRazonRechazo(String razonRechazo) { this.razonRechazo = razonRechazo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Boolean getNotificacionVencimientoEnviada() { return notificacionVencimientoEnviada; }
    public void setNotificacionVencimientoEnviada(Boolean notificacionVencimientoEnviada) { this.notificacionVencimientoEnviada = notificacionVencimientoEnviada; }
    public Boolean getNotificacionVencidoEnviada() { return notificacionVencidoEnviada; }
    public void setNotificacionVencidoEnviada(Boolean notificacionVencidoEnviada) { this.notificacionVencidoEnviada = notificacionVencidoEnviada; }
    public String getObservacionDevolucion() { return observacionDevolucion; }
    public void setObservacionDevolucion(String observacionDevolucion) { this.observacionDevolucion = observacionDevolucion; }
    public String getEstadoFisicoDevolucion() { return estadoFisicoDevolucion; }
    public void setEstadoFisicoDevolucion(String estadoFisicoDevolucion) { this.estadoFisicoDevolucion = estadoFisicoDevolucion; }
    public Integer getRenovacionesAplicadas() { return renovacionesAplicadas; }
    public void setRenovacionesAplicadas(Integer renovacionesAplicadas) { this.renovacionesAplicadas = renovacionesAplicadas; }
    public String getEstadoRenovacion() { return estadoRenovacion; }
    public void setEstadoRenovacion(String estadoRenovacion) { this.estadoRenovacion = estadoRenovacion; }
}
