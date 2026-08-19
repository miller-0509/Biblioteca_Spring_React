package co.sena.adso.biblioteca.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "renovaciones_libros")
public class RenovacionLibro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_renovacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestamo_libro", nullable = false)
    private PrestamoLibro prestamoLibro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador")
    private Usuario administrador;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(name = "fecha_esperada_original", nullable = false)
    private LocalDateTime fechaEsperadaOriginal;

    @Column(name = "fecha_esperada_nueva")
    private LocalDateTime fechaEsperadaNueva;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "motivo_solicitud", nullable = false, columnDefinition = "text")
    private String motivoSolicitud;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    public RenovacionLibro() {}

    public RenovacionLibro(PrestamoLibro prestamoLibro, Usuario usuario, Usuario administrador, LocalDateTime fechaSolicitud, LocalDateTime fechaRespuesta, LocalDateTime fechaEsperadaOriginal, LocalDateTime fechaEsperadaNueva, String estado, String motivoSolicitud, String motivoRechazo) {
        this.prestamoLibro = prestamoLibro;
        this.usuario = usuario;
        this.administrador = administrador;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaRespuesta = fechaRespuesta;
        this.fechaEsperadaOriginal = fechaEsperadaOriginal;
        this.fechaEsperadaNueva = fechaEsperadaNueva;
        this.estado = estado;
        this.motivoSolicitud = motivoSolicitud;
        this.motivoRechazo = motivoRechazo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PrestamoLibro getPrestamoLibro() { return prestamoLibro; }
    public void setPrestamoLibro(PrestamoLibro prestamoLibro) { this.prestamoLibro = prestamoLibro; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Usuario getAdministrador() { return administrador; }
    public void setAdministrador(Usuario administrador) { this.administrador = administrador; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }
    public LocalDateTime getFechaEsperadaOriginal() { return fechaEsperadaOriginal; }
    public void setFechaEsperadaOriginal(LocalDateTime fechaEsperadaOriginal) { this.fechaEsperadaOriginal = fechaEsperadaOriginal; }
    public LocalDateTime getFechaEsperadaNueva() { return fechaEsperadaNueva; }
    public void setFechaEsperadaNueva(LocalDateTime fechaEsperadaNueva) { this.fechaEsperadaNueva = fechaEsperadaNueva; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivoSolicitud() { return motivoSolicitud; }
    public void setMotivoSolicitud(String motivoSolicitud) { this.motivoSolicitud = motivoSolicitud; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}
