package com.regularizacion.topicosprogramacion.service;

import com.regularizacion.topicosprogramacion.model.Asesor;
import com.regularizacion.topicosprogramacion.model.Automovil;
import com.regularizacion.topicosprogramacion.model.Cliente;
import com.regularizacion.topicosprogramacion.model.OrdenItem;
import com.regularizacion.topicosprogramacion.model.OrdenServicio;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Locale;

public class PdfReportService {
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.of("es", "MX"));
    private static final float LEFT = 52;
    private static final float RIGHT = 560;
    private static final float BOTTOM = 76;

    private final PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public Path generarOrdenPdf(OrdenServicio orden, Path outputDir) throws IOException {
        return generarOrdenPdf(orden, null, null, null, outputDir);
    }

    public Path generarOrdenPdf(OrdenServicio orden, Cliente cliente, Automovil automovil, Asesor asesor, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        String filename = "orden_" + safe(orden.getNumeroCita()) + "_" + System.currentTimeMillis() + ".pdf";
        Path target = outputDir.resolve(filename);

        try (PDDocument document = new PDDocument()) {
            PageContext page = newPage(document, 1);
            page.y = title(page.content, "Orden de reparacion / mantenimiento", page.y);
            page.y = lineText(page.content, "Numero de cita: " + value(orden.getNumeroCita(), "Sin cita"), LEFT, page.y, true);
            text(page.content, "Fecha captura: " + value(orden.getFechaCaptura(), "Sin fecha"), 330, page.y + 14, false);

            page.y -= 8;
            page.y = section(page.content, "Cliente", page.y);
            page.y = lineText(page.content, "Nombre: " + value(first(cliente == null ? null : cliente.getNombreCompleto(), orden.getClienteNombre()), "Sin cliente"), 60, page.y, true);
            if (cliente != null) {
                page.y = lineText(page.content, "Direccion: " + value(cliente.getDireccion().asSingleLine(), "Sin direccion"), 60, page.y, false);
                page.y = lineText(page.content, "Telefono: " + value(cliente.getTelefono(), "N/D") + "    RFC: " + value(cliente.getRfc(), "N/D") + "    Mail: " + value(cliente.getMail(), "N/D"), 60, page.y, false);
            }

            page.y -= 4;
            page.y = section(page.content, "Automovil", page.y);
            page.y = lineText(page.content, "Vehiculo: " + vehicleLabel(orden, automovil), 60, page.y, true);
            if (automovil != null) {
                page.y = lineText(page.content, "Color: " + value(automovil.getColor(), "N/D") + "    Año-modelo: " + automovil.getAnioModelo() + "    Placas: " + value(automovil.getPlacas(), "N/D"), 60, page.y, false);
                page.y = lineText(page.content, "Numero de serie: " + value(automovil.getNumeroSerie(), "N/D") + "    Kilometraje: " + orden.getKilometraje() + " km", 60, page.y, false);
            } else {
                page.y = lineText(page.content, "Kilometraje: " + orden.getKilometraje() + " km", 60, page.y, false);
            }

            page.y -= 4;
            page.y = section(page.content, "Asesor", page.y);
            page.y = lineText(page.content, "Nombre: " + value(first(asesor == null ? null : asesor.getNombreCompleto(), orden.getAsesorNombre()), "Sin asesor"), 60, page.y, true);
            if (asesor != null) {
                page.y = lineText(page.content, "Departamento: " + value(asesor.getDepartamento(), "N/D") + "    Telefono: " + value(asesor.getTelefono(), "N/D") + "    Mail: " + value(asesor.getMail(), "N/D"), 60, page.y, false);
            }

            page.y -= 4;
            page.y = section(page.content, "Fechas de servicio", page.y);
            page.y = lineText(page.content, "Ingreso: " + value(orden.getFechaIngreso(), "Sin fecha") + "    Entrega: " + value(orden.getFechaEntrega(), "Sin fecha"), 60, page.y, false);

            page.y -= 4;
            page.y = section(page.content, "Observaciones", page.y);
            page.y = paragraph(page.content, value(orden.getObservaciones(), "Sin observaciones"), 60, page.y, 88, 12);

            page = ensureSpace(document, page, 165);
            page.y -= 4;
            page.y = section(page.content, "Operaciones y refacciones", page.y);
            page.y = tableHeader(page.content, page.y);
            for (OrdenItem item : orden.getItems()) {
                page = ensureSpace(document, page, 118);
                text(page.content, value(item.getTipo(), "Partida"), 58, page.y, false);
                text(page.content, crop(item.getDescripcion(), 40), 140, page.y, false);
                text(page.content, String.valueOf(item.getCantidad()), 380, page.y, false);
                text(page.content, MONEY.format(item.getPrecioUnitario()), 425, page.y, false);
                text(page.content, MONEY.format(item.getTotal()), 505, page.y, false);
                page.y -= 17;
            }

            page = ensureSpace(document, page, 135);
            page.y -= 8;
            line(page.content, 380, page.y, RIGHT, page.y);
            page.y -= 18;
            text(page.content, "Subtotal operaciones:", 380, page.y, true);
            text(page.content, MONEY.format(subtotal(orden, "operacion")), 505, page.y, false);
            page.y -= 17;
            text(page.content, "Subtotal refacciones:", 380, page.y, true);
            text(page.content, MONEY.format(subtotal(orden, "refaccion")), 505, page.y, false);
            page.y -= 19;
            text(page.content, "Total de la orden:", 380, page.y, true);
            text(page.content, MONEY.format(orden.getCostoTotal()), 505, page.y, true);

            page.close();
            document.save(target.toFile());
        }
        return target;
    }

    private PageContext newPage(PDDocument document, int pageNumber) throws IOException {
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);
        PDPageContentStream content = new PDPageContentStream(document, page);
        drawHeader(document, content);
        return new PageContext(content, 690, pageNumber);
    }

    private PageContext ensureSpace(PDDocument document, PageContext page, float minimumY) throws IOException {
        if (page.y >= minimumY) {
            return page;
        }
        page.close();
        return newPage(document, page.pageNumber + 1);
    }

    private void drawHeader(PDDocument document, PDPageContentStream content) throws IOException {
        content.setNonStrokingColor(new Color(31, 41, 55));
        content.addRect(0, 735, 612, 57);
        content.fill();
        PDImageXObject logo = LosslessFactory.createFromImage(document, logoImage());
        content.drawImage(logo, LEFT, 746, 145, 34);
        content.setNonStrokingColor(Color.WHITE);
        content.beginText();
        content.setFont(bold, 15);
        content.newLineAtOffset(390, 766);
        content.showText("AutoPrime Servicio");
        content.endText();
        content.beginText();
        content.setFont(regular, 9);
        content.newLineAtOffset(390, 751);
        content.showText("Agencia automotriz - Taller certificado");
        content.endText();
        content.setNonStrokingColor(Color.BLACK);
    }

    private BufferedImage logoImage() {
        try (InputStream input = getClass().getResourceAsStream("/images/LogoAgencia.png")) {
            if (input != null) {
                BufferedImage image = ImageIO.read(input);
                if (image != null) {
                    return image;
                }
            }
        } catch (IOException ignored) {
            // Si la imagen no puede cargarse, se usa el logo generado como respaldo.
        }

        BufferedImage image = new BufferedImage(460, 110, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(248, 250, 252));
        g.fillRect(0, 0, 460, 110);
        g.setColor(new Color(37, 99, 235));
        g.fillRoundRect(18, 30, 115, 43, 18, 18);
        g.setColor(new Color(15, 23, 42));
        g.fillOval(35, 65, 27, 27);
        g.fillOval(90, 65, 27, 27);
        g.setColor(new Color(15, 23, 42));
        g.setFont(new Font("Arial", Font.BOLD, 34));
        g.drawString("AutoPrime", 155, 55);
        g.setFont(new Font("Arial", Font.PLAIN, 17));
        g.drawString("Servicio automotriz", 158, 82);
        g.dispose();
        return image;
    }

    private float title(PDPageContentStream content, String value, float y) throws IOException {
        content.beginText();
        content.setFont(bold, 20);
        content.newLineAtOffset(LEFT, y);
        content.showText(clean(value));
        content.endText();
        return y - 30;
    }

    private float section(PDPageContentStream content, String value, float y) throws IOException {
        content.setNonStrokingColor(new Color(229, 231, 235));
        content.addRect(LEFT, y - 4, RIGHT - LEFT, 17);
        content.fill();
        content.setNonStrokingColor(Color.BLACK);
        text(content, value, LEFT + 8, y, true);
        return y - 19;
    }

    private float tableHeader(PDPageContentStream content, float y) throws IOException {
        content.setNonStrokingColor(new Color(243, 244, 246));
        content.addRect(LEFT, y - 5, RIGHT - LEFT, 18);
        content.fill();
        content.setNonStrokingColor(Color.BLACK);
        text(content, "Tipo", 58, y, true);
        text(content, "Descripcion", 140, y, true);
        text(content, "Cant.", 380, y, true);
        text(content, "Precio", 425, y, true);
        text(content, "Total", 505, y, true);
        return y - 18;
    }

    private float lineText(PDPageContentStream content, String value, float x, float y, boolean strong) throws IOException {
        text(content, value, x, y, strong);
        return y - 15;
    }

    private void text(PDPageContentStream content, String value, float x, float y, boolean strong) throws IOException {
        content.beginText();
        content.setFont(strong ? bold : regular, 10);
        content.newLineAtOffset(x, y);
        content.showText(clean(value));
        content.endText();
    }

    private float paragraph(PDPageContentStream content, String value, float x, float y, int chars, int lineHeight) throws IOException {
        String clean = clean(value);
        if (clean.isBlank()) {
            return lineText(content, "Sin observaciones", x, y, false);
        }
        for (int i = 0; i < clean.length(); i += chars) {
            text(content, clean.substring(i, Math.min(i + chars, clean.length())), x, y, false);
            y -= lineHeight;
        }
        return y;
    }

    private void footer(PDPageContentStream content, int pageNumber) throws IOException {
        line(content, LEFT, 55, RIGHT, 55);
        text(content, "Documento generado por el Sistema de Ordenes de Reparacion AutoPrime.", LEFT, 39, false);
        text(content, "Pagina " + pageNumber, 520, 39, false);
    }

    private void line(PDPageContentStream content, float x1, float y1, float x2, float y2) throws IOException {
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
    }

    private double subtotal(OrdenServicio orden, String tipo) {
        return orden.getItems().stream()
                .filter(item -> item.getTipo() != null && item.getTipo().toLowerCase(Locale.ROOT).contains(tipo))
                .mapToDouble(OrdenItem::getTotal)
                .sum();
    }

    private String vehicleLabel(OrdenServicio orden, Automovil automovil) {
        if (automovil == null) {
            return value(orden.getAutomovilDescripcion(), "Sin automovil");
        }
        return String.format("%s %s %d", automovil.getMarca(), automovil.getSubmarca(), automovil.getAnioModelo()).trim();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "sin_cita" : value.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private String crop(String value, int max) {
        String clean = clean(value);
        return clean.length() <= max ? clean : clean.substring(0, max - 3) + "...";
    }

    private String clean(String value) {
        return value == null ? "" : value.replace("\n", " ").replace("\r", " ");
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String first(String primary, String secondary) {
        return primary == null || primary.isBlank() ? secondary : primary;
    }

    private final class PageContext {
        private final PDPageContentStream content;
        private final int pageNumber;
        private float y;

        private PageContext(PDPageContentStream content, float y, int pageNumber) {
            this.content = content;
            this.y = y;
            this.pageNumber = pageNumber;
        }

        private void close() throws IOException {
            footer(content, pageNumber);
            content.close();
        }
    }
}
