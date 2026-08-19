package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.entity.*;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.repository.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    private final EquipoRepository equipoRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final PrestamoLibroRepository prestamoLibroRepository;

    public PdfExportService(EquipoRepository equipoRepository, LibroRepository libroRepository,
                            PrestamoRepository prestamoRepository, PrestamoLibroRepository prestamoLibroRepository) {
        this.equipoRepository = equipoRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.prestamoLibroRepository = prestamoLibroRepository;
    }

    private boolean verEquipos(RolUsuario rol) {
        return rol == RolUsuario.administrador || rol == RolUsuario.almacenista;
    }

    private boolean verLibros(RolUsuario rol) {
        return rol == RolUsuario.administrador || rol == RolUsuario.bibliotecario;
    }

    private boolean esBasico(RolUsuario rol) {
        return rol == RolUsuario.aprendiz || rol == RolUsuario.instructor;
    }

    @Transactional(readOnly = true)
    public byte[] exportarPdf(String tipoReporte, RolUsuario rol, Long idUsuario) {
        if (esBasico(rol) && !"mis_prestamos".equals(tipoReporte)) {
            throw new BusinessException("No tienes permisos para exportar este reporte.");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 25, 25, 30, 30);
            PdfWriter.getInstance(document, out);
            document.open();

            // Tipografías
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(15, 23, 42));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(71, 85, 105));
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(100, 116, 139));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(30, 41, 59));
            Font cellBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(15, 23, 42));

            String tituloReporte = switch (tipoReporte) {
                case "inventario_equipos" -> "Reporte de Inventario de Equipos Tecnológicos";
                case "inventario_libros" -> "Reporte de Catálogo e Inventario de Libros";
                case "prestamos_equipos" -> "Reporte Histórico de Préstamos de Equipos";
                case "prestamos_libros" -> "Reporte Histórico de Préstamos de Libros";
                case "mis_prestamos" -> "Reporte de Mis Préstamos Personales";
                default -> throw new BusinessException("Tipo de reporte no válido: " + tipoReporte);
            };

            // Encabezado institucional
            Paragraph pHeader = new Paragraph("SENA ADSO — Sistema de Biblioteca & Almacén", titleFont);
            pHeader.setAlignment(Element.ALIGN_LEFT);
            document.add(pHeader);

            Paragraph pSub = new Paragraph(tituloReporte, subtitleFont);
            pSub.setSpacingAfter(4);
            document.add(pSub);

            String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            Paragraph pMeta = new Paragraph("Generado el: " + fechaActual + " | Rol: " + rol.name(), metaFont);
            pMeta.setSpacingAfter(16);
            document.add(pMeta);

            Color headerColor = new Color(79, 70, 229);
            Color altRowColor = new Color(248, 250, 252);

            switch (tipoReporte) {
                case "inventario_equipos" -> {
                    if (!verEquipos(rol)) throw new BusinessException("Sin permisos para este reporte.");
                    List<String> headers = List.of("ID", "Nombre", "Tipo", "Marca", "Modelo", "N° Serie", "Estado", "Ubicación", "Registro");
                    float[] widths = {5f, 22f, 12f, 10f, 10f, 13f, 10f, 10f, 8f};
                    PdfPTable table = createTable(headers, widths, headerFont, headerColor);

                    boolean alt = false;
                    for (Equipo e : equipoRepository.findByEliminadoFalse()) {
                        Color bg = alt ? altRowColor : Color.WHITE;
                        addCell(table, "#" + e.getId(), cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, e.getNombre() != null ? e.getNombre() : "", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, e.getTipoEquipo() != null ? e.getTipoEquipo() : "", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, e.getMarca() != null ? e.getMarca() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, e.getModelo() != null ? e.getModelo() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, e.getNumeroSerie() != null ? e.getNumeroSerie() : "", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, e.getEstado() != null ? e.getEstado().name() : "", cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, e.getUbicacion() != null ? e.getUbicacion() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, e.getFechaRegistro() != null ? e.getFechaRegistro().toLocalDate().toString() : "—", cellFont, bg, Element.ALIGN_CENTER);
                        alt = !alt;
                    }
                    document.add(table);
                }
                case "inventario_libros" -> {
                    if (!verLibros(rol)) throw new BusinessException("Sin permisos para este reporte.");
                    List<String> headers = List.of("ID", "Título", "Autor", "Género", "Código Único", "Estado", "Disponible", "Ubicación");
                    float[] widths = {5f, 25f, 18f, 12f, 12f, 10f, 8f, 10f};
                    PdfPTable table = createTable(headers, widths, headerFont, headerColor);

                    boolean alt = false;
                    for (Libro l : libroRepository.findByEliminadoFalse()) {
                        Color bg = alt ? altRowColor : Color.WHITE;
                        addCell(table, "#" + l.getId(), cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, l.getTitulo() != null ? l.getTitulo() : "", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, l.getAutor() != null ? l.getAutor() : "", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, l.getGenero() != null ? l.getGenero() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, l.getCodigoUnico() != null ? l.getCodigoUnico() : "", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, l.getEstado() != null ? l.getEstado().name() : "", cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, Boolean.TRUE.equals(l.getDisponiblePrestamo()) ? "Sí" : "No", cellFont, bg, Element.ALIGN_CENTER);
                        addCell(table, l.getUbicacion() != null ? l.getUbicacion() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        alt = !alt;
                    }
                    document.add(table);
                }
                case "prestamos_equipos" -> {
                    if (!verEquipos(rol)) throw new BusinessException("Sin permisos para este reporte.");
                    List<String> headers = List.of("ID", "Usuario Solicitante", "Equipo", "Estado", "Fecha Solicitud", "Devolución Esperada", "Devolución Real");
                    float[] widths = {5f, 22f, 23f, 12f, 13f, 13f, 12f};
                    PdfPTable table = createTable(headers, widths, headerFont, headerColor);

                    boolean alt = false;
                    for (Prestamo p : prestamoRepository.findAll()) {
                        Color bg = alt ? altRowColor : Color.WHITE;
                        addCell(table, "#" + p.getId(), cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getUsuario() != null ? p.getUsuario().getNombres() + " " + p.getUsuario().getApellidos() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, p.getEquipo() != null ? p.getEquipo().getNombre() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, p.getEstado() != null ? p.getEstado().name() : "", cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaSolicitud() != null ? p.getFechaSolicitud().toLocalDate().toString() : "—", cellFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaDevolucionEsperada() != null ? p.getFechaDevolucionEsperada().toLocalDate().toString() : "—", cellFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaDevolucionReal() != null ? p.getFechaDevolucionReal().toLocalDate().toString() : "Pendiente", cellFont, bg, Element.ALIGN_CENTER);
                        alt = !alt;
                    }
                    document.add(table);
                }
                case "prestamos_libros" -> {
                    if (!verLibros(rol)) throw new BusinessException("Sin permisos para este reporte.");
                    List<String> headers = List.of("ID", "Usuario Solicitante", "Libro", "Estado", "Fecha Solicitud", "Devolución Esperada", "Devolución Real");
                    float[] widths = {5f, 22f, 23f, 12f, 13f, 13f, 12f};
                    PdfPTable table = createTable(headers, widths, headerFont, headerColor);

                    boolean alt = false;
                    for (PrestamoLibro p : prestamoLibroRepository.findAll()) {
                        Color bg = alt ? altRowColor : Color.WHITE;
                        addCell(table, "#" + p.getId(), cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getUsuario() != null ? p.getUsuario().getNombres() + " " + p.getUsuario().getApellidos() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, p.getLibro() != null ? p.getLibro().getTitulo() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, p.getEstado() != null ? p.getEstado().name() : "", cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaSolicitud() != null ? p.getFechaSolicitud().toLocalDate().toString() : "—", cellFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaDevolucionEsperada() != null ? p.getFechaDevolucionEsperada().toLocalDate().toString() : "—", cellFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaDevolucionReal() != null ? p.getFechaDevolucionReal().toLocalDate().toString() : "Pendiente", cellFont, bg, Element.ALIGN_CENTER);
                        alt = !alt;
                    }
                    document.add(table);
                }
                case "mis_prestamos" -> {
                    List<String> headers = List.of("Tipo", "Recurso", "Estado", "Fecha Solicitud", "Devolución Esperada", "Devolución Real");
                    float[] widths = {10f, 32f, 12f, 15f, 15f, 16f};
                    PdfPTable table = createTable(headers, widths, headerFont, headerColor);

                    boolean alt = false;
                    for (Prestamo p : prestamoRepository.findByUsuarioId(idUsuario)) {
                        Color bg = alt ? altRowColor : Color.WHITE;
                        addCell(table, "Equipo", cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getEquipo() != null ? p.getEquipo().getNombre() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, p.getEstado() != null ? p.getEstado().name() : "", cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaSolicitud() != null ? p.getFechaSolicitud().toLocalDate().toString() : "—", cellFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaDevolucionEsperada() != null ? p.getFechaDevolucionEsperada().toLocalDate().toString() : "—", cellFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaDevolucionReal() != null ? p.getFechaDevolucionReal().toLocalDate().toString() : "Pendiente", cellFont, bg, Element.ALIGN_CENTER);
                        alt = !alt;
                    }
                    for (PrestamoLibro p : prestamoLibroRepository.findByUsuarioId(idUsuario)) {
                        Color bg = alt ? altRowColor : Color.WHITE;
                        addCell(table, "Libro", cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getLibro() != null ? p.getLibro().getTitulo() : "—", cellFont, bg, Element.ALIGN_LEFT);
                        addCell(table, p.getEstado() != null ? p.getEstado().name() : "", cellBoldFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaSolicitud() != null ? p.getFechaSolicitud().toLocalDate().toString() : "—", cellFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaDevolucionEsperada() != null ? p.getFechaDevolucionEsperada().toLocalDate().toString() : "—", cellFont, bg, Element.ALIGN_CENTER);
                        addCell(table, p.getFechaDevolucionReal() != null ? p.getFechaDevolucionReal().toLocalDate().toString() : "Pendiente", cellFont, bg, Element.ALIGN_CENTER);
                        alt = !alt;
                    }
                    document.add(table);
                }
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando el archivo PDF: " + e.getMessage(), e);
        }
    }

    private PdfPTable createTable(List<String> headers, float[] widths, Font headerFont, Color headerColor) throws DocumentException {
        PdfPTable table = new PdfPTable(headers.size());
        table.setWidthPercentage(100);
        table.setWidths(widths);
        table.setHeaderRows(1);
        table.setSpacingBefore(8);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setPaddingTop(7);
            cell.setPaddingBottom(7);
            cell.setPaddingLeft(6);
            cell.setPaddingRight(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorderColor(new Color(99, 102, 241));
            table.addCell(cell);
        }
        return table;
    }

    private void addCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(bgColor);
        cell.setPaddingTop(5);
        cell.setPaddingBottom(5);
        cell.setPaddingLeft(6);
        cell.setPaddingRight(6);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }
}
