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
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        int papelAncho = 210;
        int y = 20;
        
        // Configuración de fuentes
        Font fontTitulo = new Font("Monospaced", Font.BOLD, 12);
        Font fontCuerpo = new Font("Monospaced", Font.PLAIN, 9);
        FontMetrics metrics;
        String textoAux = "";
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("$ #,##0.00");
        
        // Cabecera
        g2d.setFont(fontTitulo);
        metrics = g2d.getFontMetrics(fontTitulo);
        
        
        textoAux = "HOTEL LAS TERRAZAS II";
        g2d.drawString(textoAux, (papelAncho - metrics.stringWidth(textoAux)) / 2, y); y += 15;
        
        g2d.setFont(fontCuerpo);
        metrics = g2d.getFontMetrics(fontCuerpo);
        
        String[] infoHotel = {
                "Tel: 601 374 1072",
                "hotelterrazas@hotmail.com",
                "Carrera 80A # 2-21",
                "Whatsapp 3102203535"
            };
        
        for (String linea : infoHotel) {
            g2d.drawString(linea, (papelAncho - metrics.stringWidth(linea)) / 2, y);
            y += 12;
        }
        
        y += 5;
        g2d.drawString("--------------------------------", 5, y); y += 15;
        
        g2d.setFont(fontTitulo);
        g2d.drawString("No. Recibo: " + (idBill != null ? idBill : "---"), 10, y); y += 15;
        
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String fechaStr = (datePrint != null) ? sdf1.format(datePrint) : "N/A";
        g2d.drawString("FECHA: " + fechaStr, 10, y); y += 15;
        
        g2d.setFont(fontCuerpo);
        g2d.drawString("--------------------------------", 5, y); y += 15;
        
        g2d.drawString("HABITACIÓN: " + numberBed, 10, y); y += 15;
        g2d.drawString("CLIENTE:    " + idClient, 10, y); y += 15;
        g2d.drawString("NOCHES:     " + numberNoches, 10, y); y += 15;
        
        java.text.DecimalFormat df1 = new java.text.DecimalFormat("$ #,##0.00");
        String precioStr = (price != null) ? df1.format(price) : "$ 0.00";
        g2d.drawString("PRECIO/N:   " + precioStr, 10, y); y += 15;
        
        g2d.drawString("--------------------------------", 5, y); y += 15;
        
        g2d.setFont(fontTitulo);
        metrics = g2d.getFontMetrics(fontTitulo);
        String totalStr = "TOTAL: " + df.format(total);
        g2d.drawString(totalStr, (papelAncho - metrics.stringWidth(totalStr)) / 2, y); 
        y += 25;
        
        g2d.setFont(fontCuerpo);
        metrics = g2d.getFontMetrics(fontCuerpo);
        String pie1 = "¡Gracias por su visita!";
        String pie2 = "Hotel Las Terrazas II";
        g2d.drawString(pie1, (papelAncho - metrics.stringWidth(pie1)) / 2, y); y += 12;
        g2d.drawString(pie2, (papelAncho - metrics.stringWidth(pie2)) / 2, y);

        return PAGE_EXISTS;
	}    
    
}
