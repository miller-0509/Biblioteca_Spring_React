package co.sena.adso.biblioteca.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipos", indexes = {
    @Index(name = "idx_equipo_serie", columnList = "numero_serie"),
    @Index(name = "idx_equipo_estado", columnList = "estado_equipo"),
    @Index(name = "idx_equipo_nombre", columnList = "nombre")
})
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "tipo_equipo", nullable = false, length = 50)
    private String tipoEquipo;

    @Column(name = "marca", length = 100)
    private String marca;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "numero_serie", nullable = false, unique = true, length = 100)
    private String numeroSerie;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "estado_equipo")
    private EstadoEquipo estado;

    @Column(name = "ubicacion", length = 150)
    private String ubicacion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "proveedor", length = 150)
    private String proveedor;

    @Column(name = "responsable", length = 150)
    private String responsable;

    @Column(name = "disponible_prestamo")
    private Boolean disponiblePrestamo;

    @Column(name = "tiempo_max_prestamo")
    private Integer tiempoMaxPrestamo;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "eliminado")
    private Boolean eliminado = false;

    public Equipo() {}

    public Equipo(String nombre, String tipoEquipo, String marca, String modelo, String numeroSerie,
                  EstadoEquipo estado, String ubicacion, String proveedor, String responsable,
                  Boolean disponiblePrestamo, Integer tiempoMaxPrestamo, String descripcion,
                  LocalDate fechaCompra) {
        this.nombre = nombre;
        this.tipoEquipo = tipoEquipo;
        this.marca = marca;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.proveedor = proveedor;
        this.responsable = responsable;
        this.disponiblePrestamo = disponiblePrestamo;
        this.tiempoMaxPrestamo = tiempoMaxPrestamo;
        this.descripcion = descripcion;
        this.fechaCompra = fechaCompra;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipoEquipo() { return tipoEquipo; }
    public void setTipoEquipo(String tipoEquipo) { this.tipoEquipo = tipoEquipo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
    public EstadoEquipo getEstado() { return estado; }
    public void setEstado(EstadoEquipo estado) { this.estado = estado; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDate getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDate fechaCompra) { this.fechaCompra = fechaCompra; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public Boolean getDisponiblePrestamo() { return disponiblePrestamo; }
    public void setDisponiblePrestamo(Boolean disponiblePrestamo) { this.disponiblePrestamo = disponiblePrestamo; }
    public Integer getTiempoMaxPrestamo() { return tiempoMaxPrestamo; }
    public void setTiempoMaxPrestamo(Integer tiempoMaxPrestamo) { this.tiempoMaxPrestamo = tiempoMaxPrestamo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
}
