package com.adminHotel.printer;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.adminHotel.vo.TurnoCajaDetalleVO;

public class TicketCashRegister implements Printable {
    
    private String nombreCaja;
    private List<TurnoCajaDetalleVO> detalles;
    private BigDecimal totalRecaudado;
    
    public TicketCashRegister(
            String nombreCaja, 
            List<TurnoCajaDetalleVO> detalles, 
            BigDecimal totalRecaudado) {
        
        this.nombreCaja = nombreCaja;
        this.detalles = detalles;
        this.totalRecaudado = (totalRecaudado != null) ? totalRecaudado : BigDecimal.ZERO;
    }
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D) graphics;
        
        int ajusteIzquierda = -2;
        g2d.translate(ajusteIzquierda, pageFormat.getImageableY());
        
        int papelAncho = 160; 
        int xInicio = 5; 
        int y = 20;
        
        Font fontTitulo  = new Font("Monospaced", Font.BOLD, 10);
        Font fontSeccion = new Font("Monospaced", Font.BOLD, 9);
        Font fontCuerpo  = new Font("Monospaced", Font.PLAIN, 8);
        FontMetrics metrics;
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("$ #,##0");
        String lineaRecorte = "---------------------------";
        java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        // ==========================================
        // ENCABEZADO
        // ==========================================
        g2d.setFont(fontTitulo);
        metrics = g2d.getFontMetrics(fontTitulo);
        String textoAux = "HOTEL LAS TERRAZAS II";
        g2d.drawString(textoAux, Math.max(xInicio, ((papelAncho - metrics.stringWidth(textoAux)) / 2) - 15), y);
        y += 15;
        
        g2d.setFont(fontCuerpo);
        metrics = g2d.getFontMetrics(fontCuerpo);
        String sub = "REPORTE DE CIERRE / TURNO";
        g2d.drawString(sub, Math.max(xInicio, ((papelAncho - metrics.stringWidth(sub)) / 2) - 15), y);
        y += 12;
        
        g2d.drawString(lineaRecorte, xInicio, y);
        y += 12;
        
        g2d.setFont(fontTitulo);
        g2d.drawString("FECHA:", xInicio, y);
        y += 12;
        g2d.setFont(fontCuerpo);
        g2d.drawString(sdf1.format(new Date()), xInicio, y);
        y += 15;
        
        g2d.drawString(lineaRecorte, xInicio, y);
        y += 12;
        // ==========================================
        // IMPRIMIMOS LAS 4 SECCIONES FIJAS
        // ==========================================
        y = imprimirSeccion(g2d, fontSeccion, fontCuerpo, df, lineaRecorte,
                ">> HOTEL - EFECTIVO", 1, 1, papelAncho, xInicio, y);
        y = imprimirSeccion(g2d, fontSeccion, fontCuerpo, df, lineaRecorte,
                ">> HOTEL - NEQUI", 1, 2, papelAncho, xInicio, y);
        y = imprimirSeccion(g2d, fontSeccion, fontCuerpo, df, lineaRecorte,
                ">> MOSTRADOR - EFECTIVO", 2, 1, papelAncho, xInicio, y);
        y = imprimirSeccion(g2d, fontSeccion, fontCuerpo, df, lineaRecorte,
                ">> MOSTRADOR - NEQUI", 2, 2, papelAncho, xInicio, y);
        // ==========================================
        // GRAN TOTAL GENERAL
        // ==========================================
        g2d.setFont(fontTitulo);
        metrics = g2d.getFontMetrics(fontTitulo);
        String totalStr = "GRAN TOTAL: " + df.format(totalRecaudado);
        g2d.drawString(totalStr, Math.max(xInicio, ((papelAncho - metrics.stringWidth(totalStr)) / 2) - 15), y);
        y += 20;
        g2d.setFont(fontCuerpo);
        metrics = g2d.getFontMetrics(fontCuerpo);
        String pie1 = "Verificado por el Administrador";
        g2d.drawString(pie1, Math.max(xInicio, ((papelAncho - metrics.stringWidth(pie1)) / 2) - 15), y);
        return PAGE_EXISTS;
    }
    
    // ==========================================
    // HELPER ACTUALIZADO: Filtra por IDs, no por texto
    // ==========================================
    private int imprimirSeccion(
            Graphics2D g2d,
            Font fontSeccion,
            Font fontCuerpo,
            java.text.DecimalFormat df,
            String lineaRecorte,
            String titulo,
            Integer idCajaFiltro,    // 1=Hotel, 2=Mostrador
            Integer idMetodoFiltro,  // 1=Efectivo, 2=Nequi
            int papelAncho,
            int xInicio,
            int y) {

        // Filtrar la lista por idCaja e idMetodo (100% fiable, no depende del texto)
        List<TurnoCajaDetalleVO> filtrados = detalles == null ? 
                java.util.Collections.emptyList() : 
                detalles.stream()
                    .filter(d -> idCajaFiltro.equals(d.getIdCaja()))
                    .filter(d -> idMetodoFiltro.equals(d.getIdMetodo()))
                    .collect(java.util.stream.Collectors.toList());

        FontMetrics metrics = g2d.getFontMetrics(fontCuerpo);

        g2d.setFont(fontSeccion);
        g2d.drawString(titulo, xInicio, y);
        y += 13;

        g2d.setFont(fontCuerpo);
        metrics = g2d.getFontMetrics(fontCuerpo);

        BigDecimal subtotal = BigDecimal.ZERO;

        if (filtrados.isEmpty()) {
            g2d.drawString("  (sin movimientos)", xInicio, y);
            y += 12;
        } else {
            for (TurnoCajaDetalleVO d : filtrados) {
                String desc = d.getDescripcionMetodo();
                if (desc == null) desc = "Item";
                if (desc.length() > 27) desc = desc.substring(0, 24) + "...";
                
                g2d.drawString(desc, xInicio, y);
                y += 11;
                
                BigDecimal valor = d.getTotalIngresos() != null ? d.getTotalIngresos() : BigDecimal.ZERO;
                subtotal = subtotal.add(valor);
                String precioStr = df.format(valor);
                int anchoStr = metrics.stringWidth(precioStr);
                int xDerecha = Math.max(xInicio, (papelAncho - anchoStr) - 15);
                g2d.drawString(precioStr, xDerecha, y);
                y += 13;
            }
        }

        String subTotalStr = "Subtotal: " + df.format(subtotal);
        metrics = g2d.getFontMetrics(fontCuerpo);
        int xSub = Math.max(xInicio, (papelAncho - metrics.stringWidth(subTotalStr)) - 15);
        g2d.drawString(subTotalStr, xSub, y);
        y += 12;

        g2d.drawString(lineaRecorte, xInicio, y);
        y += 13;

        return y;
    }

}