package co.sena.adso.biblioteca.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "libros", indexes = {
    @Index(name = "idx_libro_codigo", columnList = "codigo_unico"),
    @Index(name = "idx_libro_estado", columnList = "estado_libro"),
    @Index(name = "idx_libro_titulo", columnList = "titulo")
})
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Long id;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "autor", nullable = false, length = 150)
    private String autor;

    @Column(name = "genero", nullable = false, length = 100)
    private String genero;

    @Column(name = "codigo_unico", nullable = false, length = 100)
    private String codigoUnico;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "estado_libro")
    private EstadoLibro estado;

    @Column(name = "ubicacion", length = 150)
    private String ubicacion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "disponible_prestamo")
    private Boolean disponiblePrestamo;

    @Column(name = "tiempo_max_prestamo")
    private Integer tiempoMaxPrestamo;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "eliminado")
    private Boolean eliminado = false;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "proveedor", length = 150)
    private String proveedor;

    @Column(name = "responsable", length = 150)
    private String responsable;

    public Libro() {}

    public Libro(String titulo, String autor, String genero, String codigoUnico, EstadoLibro estado, String ubicacion, Integer tiempoMaxPrestamo, String descripcion, LocalDate fechaCompra, String proveedor, String responsable) {
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        this.codigoUnico = codigoUnico;
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.tiempoMaxPrestamo = tiempoMaxPrestamo;
        this.descripcion = descripcion;
        this.fechaCompra = fechaCompra;
        this.proveedor = proveedor;
        this.responsable = responsable;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getCodigoUnico() { return codigoUnico; }
    public void setCodigoUnico(String codigoUnico) { this.codigoUnico = codigoUnico; }
    public EstadoLibro getEstado() { return estado; }
    public void setEstado(EstadoLibro estado) { this.estado = estado; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public Boolean getDisponiblePrestamo() { return disponiblePrestamo; }
    public void setDisponiblePrestamo(Boolean disponiblePrestamo) { this.disponiblePrestamo = disponiblePrestamo; }
    public Integer getTiempoMaxPrestamo() { return tiempoMaxPrestamo; }
    public void setTiempoMaxPrestamo(Integer tiempoMaxPrestamo) { this.tiempoMaxPrestamo = tiempoMaxPrestamo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
    public LocalDate getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDate fechaCompra) { this.fechaCompra = fechaCompra; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
}
