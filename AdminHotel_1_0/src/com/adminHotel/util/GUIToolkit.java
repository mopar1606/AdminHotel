package com.adminHotel.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * Clase utilitaria centralizada para la interfaz gráfica de AdminHotel.
 * Centraliza fuentes, colores, hardware y helpers de Swing.
 */
public class GUIToolkit {
    // ==========================================
    // 1. CONSTANTES DE DISEÑO (FUENTES)
    // ==========================================
    public static final Font FONT_TITLE    = new Font("Arial", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Arial", Font.BOLD, 18);
    public static final Font FONT_LABEL    = new Font("Arial", Font.BOLD, 16);
    public static final Font FONT_VALUE    = new Font("Arial", Font.BOLD, 16);
    public static final Font FONT_BUTTON   = new Font("Arial", Font.BOLD, 16);
    public static final Font FONT_NORMAL   = new Font("Arial", Font.PLAIN, 14);
    public static final Font FONT_HEADER_CELDAS   = new Font("Arial", Font.BOLD, 20);
    public static final Font FONT_CELDAS   = new Font("Arial", Font.PLAIN, 17);
    // ==========================================
    // 2. CONSTANTES DE DISEÑO (COLORES)
    // ==========================================
    public static final Color COLOR_PRIMARY   = new Color(52, 73, 94);   // Azul Oscuro
    public static final Color COLOR_SUCCESS   = new Color(46, 204, 113); // Verde (Disponible)
    public static final Color COLOR_DANGER    = new Color(231, 76, 60);  // Rojo (Ocupado)
    public static final Color COLOR_WARNING   = new Color(243, 156, 18); // Naranja (Aseo)
    public static final Color COLOR_INFO      = new Color(52, 152, 219); // Azul Claro
    public static final Color COLOR_DISABLED  = new Color(181, 176, 176); // Gris (Fuera Servicio)
    // ==========================================
    // 3. MÉTODOS DE FORMATEO Y ESTILO
    // ==========================================
    /**
     * Formatea un BigDecimal a formato moneda Colombia.
     */
    public static String formatCurrency(BigDecimal value) {
        if (value == null) return "$ 0";
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        return format.format(value);
    }
    /**
     * Aplica un borde con título estandarizado (usado en paneles de detalle).
     */
    public static void applyStandardBorder(JComponent component, String title) {
        component.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FONT_SUBTITLE));
    }
    /**
     * Aplica estilo estándar a un botón.
     */
    public static void styleButton(JButton btn, Color bgColor) {
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }
    // ==========================================
    // 4. GESTIÓN DE FORMULARIOS
    // ==========================================
    /**
     * Limpia de forma recursiva todos los campos de texto dentro de un contenedor.
     */
    public static void clearForm(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTextField) {
                ((JTextField) c).setText("");
            } else if (c instanceof JTextArea) {
                ((JTextArea) c).setText("");
            } else if (c instanceof Container) {
                clearForm((Container) c);
            }
        }
    }
    // ==========================================
    // 5. HARDWARE (CAJÓN MONEDERO)
    // ==========================================
    /**
     * Abre el cajón monedero enviando comandos ESC/POS a la impresora por defecto.
     */
    public static void abrirCajon(Component parent) {
        try {
            byte[] comandoAbrirCajon = { 27, 112, 0, 25, (byte) 250 };
            javax.print.PrintService impresora = javax.print.PrintServiceLookup.lookupDefaultPrintService();
            if (impresora == null) {
                showInfo(parent, "No hay impresora predeterminada para abrir el cajón.");
                return;
            }
            javax.print.DocPrintJob job = impresora.createPrintJob();
            javax.print.Doc doc = new javax.print.SimpleDoc(
                    comandoAbrirCajon,
                    javax.print.DocFlavor.BYTE_ARRAY.AUTOSENSE,
                    null);
            job.print(doc, null);
        } catch (Exception e) {
            showError(parent, "abrir el cajon monedero", e);
        }
    }
    
    // ==========================================
    // 6. DIÁLOGOS DE MENSAJES (REDUCCIÓN DE CÓDIGO)
    // ==========================================
    /**
     * Muestra un error formateado con detalles técnicos.
     */
    public static void showError(Component parent, String accion, Exception e) {
        String mensaje = String.format("Error al %s:\n\nDetalle: %s\nMotivo: %s", 
                accion, e.getClass().getSimpleName(), e.getMessage());
        
        JOptionPane.showMessageDialog(
        		parent, 
        		mensaje, 
        		"HOTEL LAS TERRAZAS II - Error",
        		JOptionPane.ERROR_MESSAGE);
    }
    /**
     * Muestra un mensaje informativo rápido.
     */
    public static void showInfo(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(
        		parent, 
        		mensaje, 
        		"HOTEL LAS TERRAZAS II - Info",
        		JOptionPane.INFORMATION_MESSAGE);
    }
    /**
     * Muestra un diálogo de confirmación YES/NO.
     */
    public static boolean confirm(Component parent, String mensaje) {
        int resp = JOptionPane.showConfirmDialog(
        		parent, 
        		mensaje, 
        		"HOTEL LAS TERRAZAS II - Confirmación", 
        		JOptionPane.YES_NO_OPTION);
        return resp == JOptionPane.YES_OPTION;
    }
}