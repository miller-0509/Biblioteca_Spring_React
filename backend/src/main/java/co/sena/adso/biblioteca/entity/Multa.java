package co.sena.adso.biblioteca.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "multas")
public class Multa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_multa")
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo_recurso", nullable = false, length = 20)
    private TipoRecurso tipoRecurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestamo_equipo")
    private Prestamo prestamoEquipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestamo_libro")
    private PrestamoLibro prestamoLibro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador_resolucion")
    private Usuario administradorResolucion;

    @Column(name = "dias_retraso", nullable = false)
    private Integer diasRetraso = 0;

    @Column(name = "dias_suspension", nullable = false)
    private Integer diasSuspension = 0;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_inicio_suspension")
    private LocalDateTime fechaInicioSuspension;

    @Column(name = "fecha_fin_suspension")
    private LocalDateTime fechaFinSuspension;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "estado_multa")
    private EstadoMulta estado;

    @Column(name = "observacion", columnDefinition = "text")
    private String observacion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Multa() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TipoRecurso getTipoRecurso() { return tipoRecurso; }
    public void setTipoRecurso(TipoRecurso tipoRecurso) { this.tipoRecurso = tipoRecurso; }
    public Prestamo getPrestamoEquipo() { return prestamoEquipo; }
    public void setPrestamoEquipo(Prestamo prestamoEquipo) { this.prestamoEquipo = prestamoEquipo; }
    public PrestamoLibro getPrestamoLibro() { return prestamoLibro; }
    public void setPrestamoLibro(PrestamoLibro prestamoLibro) { this.prestamoLibro = prestamoLibro; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Usuario getAdministradorResolucion() { return administradorResolucion; }
    public void setAdministradorResolucion(Usuario administradorResolucion) { this.administradorResolucion = administradorResolucion; }
    public Integer getDiasRetraso() { return diasRetraso; }
    public void setDiasRetraso(Integer diasRetraso) { this.diasRetraso = diasRetraso; }
    public Integer getDiasSuspension() { return diasSuspension; }
    public void setDiasSuspension(Integer diasSuspension) { this.diasSuspension = diasSuspension; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public LocalDateTime getFechaInicioSuspension() { return fechaInicioSuspension; }
    public void setFechaInicioSuspension(LocalDateTime fechaInicioSuspension) { this.fechaInicioSuspension = fechaInicioSuspension; }
    public LocalDateTime getFechaFinSuspension() { return fechaFinSuspension; }
    public void setFechaFinSuspension(LocalDateTime fechaFinSuspension) { this.fechaFinSuspension = fechaFinSuspension; }
    public EstadoMulta getEstado() { return estado; }
    public void setEstado(EstadoMulta estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
