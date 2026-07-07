package com.adminHotel.printer;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class TicketCheckIn implements Printable {
	
    private Integer idBill, numberNoches;
    private String idClient, numberBed;
    private BigDecimal price, total;
    private Timestamp datePrint;
    
    public TicketCheckIn(
            Integer idRecibo,
            String idCliente,
            String numeroHab,
            Integer noches,
            BigDecimal total,
            Timestamp fecha,
            BigDecimal hPrecio) {
        
        this.idBill = idRecibo;
        this.idClient = idCliente;
        this.numberBed = numeroHab;
        this.numberNoches = noches;
        this.datePrint = fecha;
        this.price = hPrecio;
        this.total = total;
    }
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        
        if (pageIndex > 0) return NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D) graphics;
        
        // --------------------------------------------------------------------------
        // AJUSTE DE MARGEN IZQUIERDO FANTASMA
        // Las impresoras de 58mm a veces fuerzan un margen a la izquierda por driver.
        // Un valor negativo aquí (-15 a -25) empuja todo el contenido hacia la izquierda.
        int ajusteIzquierda = -2;  // <-- Cambia este número si necesitas correrlo más o menos
        // --------------------------------------------------------------------------
        g2d.translate(ajusteIzquierda, pageFormat.getImageableY());
        
        // Ancho máximo utilizado para calcular los centros de las líneas de texto
        int papelAncho = 160; 
        int xInicio = 5; 
        int y = 20;
        
        // Fuentes
        Font fontTitulo = new Font("Monospaced", Font.BOLD, 10);
        Font fontCuerpo = new Font("Monospaced", Font.PLAIN, 8);
        FontMetrics metrics;
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("$ #,##0");
        
        // Cabecera
        g2d.setFont(fontTitulo);
        metrics = g2d.getFontMetrics(fontTitulo);
        
        String textoAux = "HOTEL LAS TERRAZAS II";
        g2d.drawString(textoAux, Math.max(xInicio, ((papelAncho - metrics.stringWidth(textoAux)) / 2) - 15), y);
        y += 15;
        
        g2d.setFont(fontCuerpo);
        metrics = g2d.getFontMetrics(fontCuerpo);
        
        String[] infoHotel = {
                "NIT: 52180221 - 1",
        		"Tel: 601 374 1072",
                "hotelterrazas@hotmail.com",
                "Cra 80A # 2-21",
                "WhatsApp: 3102203535"
        };
        
        for (String linea : infoHotel) {
        	g2d.drawString(linea, Math.max(xInicio, ((papelAncho - metrics.stringWidth(linea)) / 2) - 15), y);
            y += 12;
        }
        
        y += 5;
        String lineaRecorte = "---------------------------";
        g2d.drawString(lineaRecorte, xInicio, y);
        y += 15;
        
        g2d.setFont(fontTitulo);
        g2d.drawString("No. Recibo: " + (idBill != null ? idBill : "---"), xInicio, y);
        y += 15;
        
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        g2d.drawString("FECHA: ", xInicio, y); y += 15;
        g2d.drawString(((datePrint != null) ? sdf1.format(datePrint) : "N/A"), xInicio, y);
        y += 15;
        
        g2d.setFont(fontCuerpo);
        g2d.drawString(lineaRecorte, xInicio, y); y += 15;
        
        g2d.drawString("HABITACION: " + numberBed, xInicio, y);
        y += 15;
        g2d.drawString("CLIENTE:    " + idClient, xInicio, y);
        y += 15;
        g2d.drawString("NOCHES:     " + numberNoches, xInicio, y);
        y += 15;
        
        java.text.DecimalFormat df1 = new java.text.DecimalFormat("$ #,##0");
        g2d.drawString("PRECIO/N:   " + ((price != null) ? df1.format(price) : "$ 0"), xInicio, y);
        y += 15;
        
        g2d.drawString(lineaRecorte, xInicio, y);
        y += 15;
        
        g2d.setFont(fontTitulo);
        metrics = g2d.getFontMetrics(fontTitulo);
        String totalStr = "TOTAL: " + df.format(total);
        g2d.drawString(totalStr, Math.max(xInicio, ((papelAncho - metrics.stringWidth(totalStr)) / 2) - 15), y);
        y += 25;
        
        g2d.setFont(fontCuerpo);
        metrics = g2d.getFontMetrics(fontCuerpo);
        String pie1 = "Gracias por su visita!";
        String pie2 = "Hotel Las Terrazas II";
        g2d.drawString(pie1, Math.max(xInicio, ((papelAncho - metrics.stringWidth(pie1)) / 2) - 15), y);
        y += 12;
        g2d.drawString(pie2, Math.max(xInicio, ((papelAncho - metrics.stringWidth(pie2)) / 2) - 15), y);
        return PAGE_EXISTS;
    }    
}
