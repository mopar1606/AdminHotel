package com.adminHotel.printer;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.util.List;

import com.adminHotel.util.DetalleTemporal;

public class TicketVentaMostrador implements Printable {
	
    private Integer idVenta;
    private List<DetalleTemporal> items;
    private BigDecimal total;
    private java.sql.Timestamp fecha;
    private String metodoPago;
    
    public TicketVentaMostrador(
    		Integer idVenta,
    		List<DetalleTemporal> items,
    		BigDecimal total,
    		java.sql.Timestamp fecha,
            String metodoPago) {
        this.idVenta    = idVenta;
        this.items      = items;
        this.total      = total;
        this.fecha      = fecha;
        this.metodoPago = metodoPago;
    }
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
    	
        if (pageIndex > 0) return NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D) graphics;
        
        int ajusteIzquierda = -2; // igual que TicketCheckIn
        g2d.translate(ajusteIzquierda, pageFormat.getImageableY());
        int papelAncho = 160;
        int xInicio    = 5;
        int y          = 20;
        
        Font fontTitulo = new Font("Monospaced", Font.BOLD, 10);
        Font fontCuerpo = new Font("Monospaced", Font.PLAIN, 8);
        FontMetrics metrics;
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("$ #,##0");
        
        // ---- CABECERA ----
        g2d.setFont(fontTitulo);
        metrics = g2d.getFontMetrics(fontTitulo);
        String titulo = "HOTEL LAS TERRAZAS II";
        g2d.drawString(titulo, Math.max(xInicio, ((papelAncho - metrics.stringWidth(titulo)) / 2) - 15), y);
        y += 15;
        g2d.setFont(fontCuerpo);
        metrics = g2d.getFontMetrics(fontCuerpo);
        String[] infoHotel = {
            "NIT: 52180221 - 1",
            "Tel: 601 374 1072",
            "Cra 80A # 2-21",
            "WhatsApp: 3102203535"
        };
        
        for (String linea : infoHotel) {
            g2d.drawString(linea, Math.max(xInicio, ((papelAncho - metrics.stringWidth(linea)) / 2) - 15), y);
            y += 12;
        }
        // ---- SEPARADOR ----
        String sep = "---------------------------";
        y += 5;
        g2d.drawString(sep, xInicio, y); y += 15;
        
        // ---- DATOS VENTA ----
        g2d.setFont(fontTitulo);
        g2d.drawString("TIQUETE No. " + idVenta, xInicio, y); y += 15;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        g2d.setFont(fontCuerpo);
        g2d.drawString("FECHA:  " + (fecha != null ? sdf.format(fecha) : "N/A"), xInicio, y); y += 12;
        g2d.drawString("PAGO:   " + metodoPago, xInicio, y); y += 12;
        g2d.drawString(sep, xInicio, y); y += 12;
        
        // ---- ITEMS ----
        g2d.setFont(fontTitulo);
        g2d.drawString("DESCRIPCION", xInicio, y);
        g2d.drawString("CANT VALUE", 100, y);
        y += 12;
        
        g2d.setFont(fontCuerpo);
        g2d.drawString(sep, xInicio, y); y += 10;
        
        metrics = g2d.getFontMetrics(fontCuerpo);
        
        for (DetalleTemporal det : items) {
            String nombre = det.getProducto().getNombre();
            if (nombre.length() > 15) {
                nombre = nombre.substring(0, 14) + "."; // Truncar si es muy largo
            }
            
            // Dibujar Nombre a la izquierda
            g2d.drawString(nombre, xInicio, y);
            
            // Cantidad en medio
            g2d.drawString(det.getCantidad() + "x", 100, y);
            
            // Precio alineado a la derecha
            y += 12;
            String subTotalStr = df.format(det.getSubtotal());
            int widthSubTotal = metrics.stringWidth(subTotalStr);
            // El limite derecho del ticket es papelAncho aprox. Ajustamos a la izq.
            int rightAlignX = papelAncho - widthSubTotal; 
            
            g2d.drawString(subTotalStr, 120, y);
            y += 12;
        }
        
        // ---- TOTAL ----
        g2d.drawString(sep, xInicio, y); y += 12;
        g2d.setFont(fontTitulo);
        metrics = g2d.getFontMetrics(fontTitulo);
        String strTotal = "TOTAL: " + df.format(total);
        g2d.drawString(strTotal, Math.max(xInicio, ((papelAncho - metrics.stringWidth(strTotal)) / 2) - 15), y);
        y += 25;
        
        // ---- PIE ----
        g2d.setFont(fontCuerpo);
        metrics = g2d.getFontMetrics(fontCuerpo);
        String pie = "Gracias por su compra!";
        g2d.drawString(pie, Math.max(xInicio, ((papelAncho - metrics.stringWidth(pie)) / 2) - 15), y);
        y += 12;
        String pie2 = "Hotel Las Terrazas II";
        g2d.drawString(pie2, Math.max(xInicio, ((papelAncho - metrics.stringWidth(pie2)) / 2) - 15), y);
        
        return PAGE_EXISTS;
    }
}