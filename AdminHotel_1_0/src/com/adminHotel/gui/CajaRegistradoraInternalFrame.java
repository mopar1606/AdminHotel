package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.adminHotel.printer.TicketCashRegister;
import com.adminHotel.service.OperationBoxService;
import com.adminHotel.service.TurnoSchedulerService;
import com.adminHotel.util.GUIToolkit;
import com.adminHotel.util.SesionUsuario;
import com.adminHotel.vo.TurnoCajaDetalleVO;
import com.adminHotel.vo.TurnoCajaVO;

public class CajaRegistradoraInternalFrame extends JInternalFrame {
	
	private static final long serialVersionUID = -3586231146165190389L;
	
	// -------------------------------------------------------
    // CONSTANTES DE CAJA (deben coincidir con IDs de tabla caja)
    // -------------------------------------------------------
    private static final int ID_CAJA_HOTEL     = 1;
    private static final int ID_CAJA_MOSTRADOR = 2;
    private static final int ID_METODO_NEQUI   = 2;
    private static final int ID_METODO_EFECTIVO = 1;
    // -------------------------------------------------------
    // FUENTES Y COLORES
    // -------------------------------------------------------
    private static final Font FUENTE_TITULO   = new Font("Arial", Font.BOLD, 18);
    private static final Font FUENTE_MONTO    = new Font("Arial", Font.BOLD, 28);
    private static final Font FUENTE_LABEL    = new Font("Arial", Font.BOLD, 18);
    private static final Font FUENTE_BOTON    = new Font("Arial", Font.BOLD, 18);
    private static final Color COLOR_HEADER   = new Color(41, 128, 185);
    private static final Color COLOR_EFECTIVO = new Color(39, 174, 96);
    private static final Color COLOR_NEQUI    = new Color(142, 68, 173);
    private static final Color COLOR_FONDO    = new Color(236, 240, 241);
    // -------------------------------------------------------
    // COMPONENTES DE PANTALLA
    // -------------------------------------------------------
    // Tablero Hotel - Nequi
    private JLabel lblHotelNequiMonto;
    private JTable tblHotelNequi;
    private DefaultTableModel modelHotelNequi;
    // Tablero Mostrador - Nequi
    private JLabel lblMostradorNequiMonto;
    private JTable tblMostradorNequi;
    private DefaultTableModel modelMostradorNequi;
    // Tablero Hotel - Efectivo
    private JLabel lblHotelEfectivoMonto;
    private JTable tblHotelEfectivo;
    private DefaultTableModel modelHotelEfectivo;
    // Tablero Mostrador - Efectivo
    private JLabel lblMostradorEfectivoMonto;
    private JTable tblMostradorEfectivo;
    private DefaultTableModel modelMostradorEfectivo;
    // Panel de préstamos
    private JTable tblPrestamos;
    private DefaultTableModel modelPrestamos;
    private JPanel pnlContenedorTarjetas;
    // Botones
    private JButton btnAbrirTurno;
    private JButton btnCerrarTurno;
    private JButton btnRefrescar;
    private JButton btnPrintTurno;
    // Estado del turno activo
    private TurnoCajaVO turnoHotelActivo;
    private TurnoCajaVO turnoMostradorActivo;
    private OperationBoxService boxService = new OperationBoxService();

	public CajaRegistradoraInternalFrame() {
		super("HOTEL LAS TERRAZAS II - Administrar Caja Registradora", true, true, true, true);

		setLayout(new BorderLayout());
		
		getContentPane().setBackground(COLOR_FONDO);

		SwingUtilities.invokeLater(() -> {
    	    JDesktopPane desktop = getDesktopPane();
    	    if (desktop != null) {
    	        
    	    	Dimension size = desktop.getSize();    	        
    	        setSize(size.width, size.height);    	        
    	        setLocation(0, 0);
    	    }
    	});
		
		// Construcción de paneles
        add(buildPanelPrestamos(),  BorderLayout.NORTH);
        add(buildPanelTableros(),   BorderLayout.CENTER);
        add(buildPanelBotones(),    BorderLayout.SOUTH);
        // Cargar estado inicial
        verificarTurnosActivos();
        refrescarTablero();
        refrescarPrestamos();
	}
	
	// -------------------------------------------------------
    // PANEL NORTE: PRÉSTAMOS EN CURSO
    // -------------------------------------------------------    
    private JPanel buildPanelPrestamos() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_HEADER, 2),
                "RESUMEN DE OBJETOS PRESTABLES",
                0, 0, FUENTE_TITULO, COLOR_HEADER));
        pnl.setBackground(Color.WHITE);
        pnl.setPreferredSize(new Dimension(0, 140)); // Un alto fijo amigable para las tarjetas
        
        // FlowLayout.LEFT asegura que se alineen uno tras otro y aplique scroll si no caben
        pnlContenedorTarjetas = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
        pnlContenedorTarjetas.setBackground(Color.WHITE);
        
        // Scroll horizontal
        JScrollPane scroll = new JScrollPane(pnlContenedorTarjetas);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setBorder(null); // Sin borde para que se vea limpio
        
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }
    
    // -------------------------------------------------------
    // PANEL CENTRO: 4 TABLEROS DE CAJA
    // -------------------------------------------------------
    private JPanel buildPanelTableros() {
        JPanel pnl = new JPanel(new GridLayout(2, 2, 10, 10));
        pnl.setBackground(COLOR_FONDO);
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Fila 1: Nequi Hotel | Nequi Mostrador
        pnl.add(buildCuadrante("VENTAS HOTEL — NEQUI",      COLOR_NEQUI,    "Hotel",     "NEQUI",    true));
        pnl.add(buildCuadrante("VENTAS MOSTRADOR — NEQUI",   COLOR_NEQUI,    "Mostrador", "NEQUI",    false));
        // Fila 2: Efectivo Hotel | Efectivo Mostrador
        pnl.add(buildCuadrante("VENTAS HOTEL — EFECTIVO",    COLOR_EFECTIVO, "Hotel",     "EFECTIVO", true));
        pnl.add(buildCuadrante("VENTAS MOSTRADOR — EFECTIVO",COLOR_EFECTIVO, "Mostrador", "EFECTIVO", false));
        return pnl;
    }
    
    /**
     * Construye un cuadrante del tablero.
     * @param titulo   Título del panel
     * @param color    Color del encabezado
     * @param caja     "Hotel" o "Mostrador"
     * @param metodo   "NEQUI" o "EFECTIVO"
     * @param esHotel  true = caja hotel, false = caja mostrador
     */
    private JPanel buildCuadrante(String titulo, Color color, String caja, String metodo, boolean esHotel) {
        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(color, 2),
                titulo, 0, 0, FUENTE_TITULO, color));
        // Monto acumulado grande en el NORTE del cuadrante
        JLabel lblMonto = new JLabel("$ 0.00", SwingConstants.CENTER);
        lblMonto.setFont(FUENTE_MONTO);
        lblMonto.setForeground(color);
        lblMonto.setOpaque(true);
        lblMonto.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
        lblMonto.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        pnl.add(lblMonto, BorderLayout.NORTH);
        // Tabla de detalle en el CENTRO
        String[] cols = { "CONCEPTO", "VALOR" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tabla = new JTable(model);
        tabla.setFont(FUENTE_LABEL);
        tabla.setRowHeight(26);
        tabla.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(color);
        tabla.getTableHeader().setForeground(Color.BLACK);
        pnl.add(new JScrollPane(tabla), BorderLayout.CENTER);
        // Asignar referencias según caja y método
        if (esHotel && "NEQUI".equals(metodo)) {
            lblHotelNequiMonto = lblMonto;
            tblHotelNequi      = tabla;
            modelHotelNequi    = model;
        } else if (!esHotel && "NEQUI".equals(metodo)) {
            lblMostradorNequiMonto = lblMonto;
            tblMostradorNequi      = tabla;
            modelMostradorNequi    = model;
        } else if (esHotel && "EFECTIVO".equals(metodo)) {
            lblHotelEfectivoMonto = lblMonto;
            tblHotelEfectivo      = tabla;
            modelHotelEfectivo    = model;
        } else {
            lblMostradorEfectivoMonto = lblMonto;
            tblMostradorEfectivo      = tabla;
            modelMostradorEfectivo    = model;
        }
        return pnl;
    }
    
    // -------------------------------------------------------
    // PANEL SUR: BOTONES DE ACCIÓN
    // -------------------------------------------------------
    private JPanel buildPanelBotones() {
        JPanel pnl = new JPanel();
        pnl.setBackground(new Color(44, 62, 80));
        pnl.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        btnAbrirTurno = new JButton("ABRIR TURNO");
        btnAbrirTurno.setFont(FUENTE_BOTON);
        btnAbrirTurno.setBackground(new Color(46, 204, 113));
        btnAbrirTurno.setForeground(Color.BLACK);
        btnAbrirTurno.setPreferredSize(new Dimension(220, 45));
        btnAbrirTurno.addActionListener(e -> accionAbrirTurno());
        
        btnCerrarTurno = new JButton("CERRAR TURNO");
        btnCerrarTurno.setFont(FUENTE_BOTON);
        btnCerrarTurno.setBackground(new Color(231, 76, 60));
        btnCerrarTurno.setForeground(Color.BLACK);
        btnCerrarTurno.setPreferredSize(new Dimension(220, 45));
        btnCerrarTurno.addActionListener(e -> accionCerrarTurno());
        
        btnRefrescar = new JButton("REFRESCAR");
        btnRefrescar.setFont(FUENTE_BOTON);
        btnRefrescar.setBackground(new Color(41, 128, 185));
        btnRefrescar.setForeground(Color.BLACK);
        btnRefrescar.setPreferredSize(new Dimension(180, 45));
        btnRefrescar.addActionListener(e -> refrescarTablero());
        
        btnPrintTurno = new JButton("IMPRIMIR CIERRE");
        btnPrintTurno.setFont(FUENTE_BOTON);
        btnPrintTurno.setBackground(new java.awt.Color(149, 165, 166));
        btnPrintTurno.setForeground(Color.BLACK);
        btnPrintTurno.setPreferredSize(new Dimension(220, 45));
        btnPrintTurno.addActionListener(e -> accionPrintTurno());
        
        pnl.add(btnAbrirTurno);
        pnl.add(btnCerrarTurno);
        pnl.add(btnRefrescar);
        pnl.add(btnPrintTurno);
        
        return pnl;
    }
    
 // -------------------------------------------------------
 // ACCIÓN: ABRIR TURNO (VERSIÓN MEJORADA CON VALIDACIÓN)
 // -------------------------------------------------------
 private void accionAbrirTurno() {
     // =============================
     // 1. VERIFICAR CIERRES FORZADOS PENDIENTES
     // =============================
     if (!procesarCierresForzadosPendientes()) {
         return; // No puede continuar si hay cierres pendientes no resueltos
     }
     
     // =============================
     // 2. VERIFICAR SI YA HAY TURNOS ABIERTOS
     // =============================
     if (turnoHotelActivo != null && turnoMostradorActivo != null) {
         JOptionPane.showMessageDialog(this,
             "Ambas cajas ya tienen un turno ABIERTO.\n\n" +
             "• Caja Hotel: ABIERTA\n" +
             "• Caja Mostrador: ABIERTA\n\n" +
             "Debe cerrar los turnos actuales antes de abrir nuevos.",
             "HOTEL LAS TERRAZAS II - CAJAS ABIERTAS",
             JOptionPane.WARNING_MESSAGE);
         return;
     }
     
     // =============================
     // 3. CONFIRMAR APERTURA
     // =============================
     StringBuilder mensajeConfirmacion = new StringBuilder();
     mensajeConfirmacion.append("¿Desea abrir el turno de AMBAS CAJAS?\n\n");
     
     if (turnoHotelActivo == null) {
         mensajeConfirmacion.append("✅ Caja Hotel: SE ABRIRÁ\n");
     } else {
         mensajeConfirmacion.append("⏸️ Caja Hotel: YA ABIERTA (se mantendrá)\n");
     }
     
     if (turnoMostradorActivo == null) {
         mensajeConfirmacion.append("✅ Caja Mostrador: SE ABRIRÁ\n");
     } else {
         mensajeConfirmacion.append("⏸️ Caja Mostrador: YA ABIERTA (se mantendrá)\n");
     }
     
     mensajeConfirmacion.append("\nEsta acción registrará:\n");
     mensajeConfirmacion.append("• Usuario responsable: ").append(SesionUsuario.getNombreUsuario()).append("\n");
     mensajeConfirmacion.append("• Hora de apertura: Actual\n");
     mensajeConfirmacion.append("• Base inicial: Desde parámetros del sistema\n");
     
     int confirm = JOptionPane.showConfirmDialog(this,
         mensajeConfirmacion.toString(),
         "HOTEL LAS TERRAZAS II - CONFIRMAR APERTURA DE TURNO",
         JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE);
     
     if (confirm != JOptionPane.YES_OPTION) {
         return; // Usuario canceló
     }
     
     // =============================
     // 4. EJECUTAR APERTURA
     // =============================
     try {
         boolean algunaCajaAbierta = false;
         StringBuilder resultado = new StringBuilder();
         resultado.append("Resultado de apertura de turnos:\n\n");
         
         // Abrir Caja Hotel si no está activa
         if (turnoHotelActivo == null) {
             try {
                 turnoHotelActivo = boxService.doOpenBox(ID_CAJA_HOTEL);
                 resultado.append("✅ Caja Hotel: ABIERTA exitosamente\n");
                 algunaCajaAbierta = true;
             } catch (Exception e) {
                 resultado.append("❌ Caja Hotel: ERROR - ").append(e.getMessage()).append("\n");
             }
         } else {
             resultado.append("⏸️ Caja Hotel: YA ESTABA ABIERTA\n");
         }
         
         // Abrir Caja Mostrador si no está activa
         if (turnoMostradorActivo == null) {
             try {
                 turnoMostradorActivo = boxService.doOpenBox(ID_CAJA_MOSTRADOR);
                 resultado.append("✅ Caja Mostrador: ABIERTA exitosamente\n");
                 algunaCajaAbierta = true;
             } catch (Exception e) {
                 resultado.append("❌ Caja Mostrador: ERROR - ").append(e.getMessage()).append("\n");
             }
         } else {
             resultado.append("⏸️ Caja Mostrador: YA ESTABA ABIERTA\n");
         }
         
         // =============================
         // 5. MOSTRAR RESULTADO
         // =============================
         if (algunaCajaAbierta) {
             resultado.append("\n————————————————————————\n");
             resultado.append("Usuario: ").append(SesionUsuario.getNombreUsuario()).append("\n");
             resultado.append("Hora: ").append(new java.util.Date()).append("\n");
             resultado.append("————————————————————————\n\n");
             resultado.append("✅ Turno(s) abierto(s) correctamente.");
             
             JOptionPane.showMessageDialog(this,
                 resultado.toString(),
                 "HOTEL LAS TERRAZAS II - TURNO ABIERTO",
                 JOptionPane.INFORMATION_MESSAGE);
             
             // Actualizar interfaz
             actualizarEstadoBotones();
             refrescarTablero();
             
         } else {
             JOptionPane.showMessageDialog(this,
                 "No se pudo abrir ninguna caja nueva.\n" +
                 "Ambas cajas ya estaban abiertas o hubo errores.",
                 "HOTEL LAS TERRAZAS II - SIN CAMBIOS",
                 JOptionPane.WARNING_MESSAGE);
         }
         
     } catch (Exception e) {
         JOptionPane.showMessageDialog(this,
             "Error crítico al abrir turno:\n\n" + e.getMessage() + "\n\n" +
             "Contacte al administrador del sistema.",
             "HOTEL LAS TERRAZAS II - ERROR CRÍTICO",
             JOptionPane.ERROR_MESSAGE);
     }
 }
    
    // -------------------------------------------------------
    // ACCIÓN: CERRAR TURNO
    // Pide confirmación y cierra ambas cajas
    // -------------------------------------------------------
    private void accionCerrarTurno() {
        if (turnoHotelActivo == null && turnoMostradorActivo == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay turnos abiertos para cerrar.",
                    "SIN TURNO ACTIVO", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Confirma el CIERRE de ambas cajas?\n"
                + "Esta acción consolidará los totales del turno.",
                "HOTEL LAS TERRAZAS II - CERRAR TURNO", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        String observacion = JOptionPane.showInputDialog(this,
                "Observación de cierre (opcional):",
                "HOTEL LAS TERRAZAS II - CIERRE DE CAJA", JOptionPane.QUESTION_MESSAGE);
                
        if (observacion == null) return; // Si cancela la ventana, abortamos el cierre
        
        try {
            // Obtener los acumulados actuales para armar el detalle de cierre
            List<com.adminHotel.vo.TurnoCajaDetalleVO> acumulados = boxService.doGetLiveAccumulados();
            
            // Pedir al recepcionista que declare lo que contó físicamente
            List<com.adminHotel.vo.TurnoCajaDetalleVO> detallesConMontosReales = pedirMontosRealesAlUsuario(acumulados);
            if (detallesConMontosReales == null) return; // Canceló
            
            // --- NUEVO: REVISIÓN DE DESCUADRE ---
            boolean huboDescuadre = false;
            StringBuilder msgDescuadre = new StringBuilder("¡ATENCIÓN! Se encontraron las siguientes diferencias:\n\n");
            
            for (com.adminHotel.vo.TurnoCajaDetalleVO det : detallesConMontosReales) {
                // Comparamos el sistema vs lo reportado por el recepcionista
                java.math.BigDecimal esperado = det.getTotalIngresos() != null ? det.getTotalIngresos() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal real = det.getMontoReal() != null ? det.getMontoReal() : java.math.BigDecimal.ZERO;
                
                if (esperado.compareTo(real) != 0) {
                    huboDescuadre = true;
                    java.math.BigDecimal dif = real.subtract(esperado);
                    String tipo = (dif.compareTo(java.math.BigDecimal.ZERO) > 0) ? "SOBRAN" : "FALTAN";
                    
                    msgDescuadre.append("- ").append(det.getNombreCaja()).append(" (").append(det.getDescripcionMetodo()).append("):\n")
                                .append("  Sistema: $").append(esperado)
                                .append(" | Cajón: $").append(real)
                                .append(" | ").append(tipo).append(": $").append(dif.abs()).append("\n\n");
                }
            }
            
            // Si hubo descuadre se muestra el panel de alerta antes de cerrar
            if (huboDescuadre) {
                msgDescuadre.append("¿Desea CERRAR LA CAJA DESCUADRADA de todas formas?");
                int confirmDescuadre = JOptionPane.showConfirmDialog(this, 
                        msgDescuadre.toString(), 
                        "ALERTA: CAJA DESCUADRADA", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        
                if (confirmDescuadre != JOptionPane.YES_OPTION) {
                    return; // Si dice NO, se cancela todo el proceso
                }
                
                // Si acepta cerrarla descuadrada, estampamos el registro
                observacion = "¡DESCUADRE REPORTADO! " + observacion;
            }
            // --- FIN REVISIÓN DESCUADRE ---

            // Cerrar Caja Hotel
            if (turnoHotelActivo != null) {
                List<com.adminHotel.vo.TurnoCajaDetalleVO> detalleHotel = filtrarPorTurno(
                        detallesConMontosReales, turnoHotelActivo.getIdTurnoCaja());
                boxService.doCloseBox(turnoHotelActivo.getIdTurnoCaja(), detalleHotel, observacion);
                turnoHotelActivo = null;
            }
            
            // Cerrar Caja Mostrador
            if (turnoMostradorActivo != null) {
                List<com.adminHotel.vo.TurnoCajaDetalleVO> detalleMostrador = filtrarPorTurno(
                        detallesConMontosReales, turnoMostradorActivo.getIdTurnoCaja());
                boxService.doCloseBox(turnoMostradorActivo.getIdTurnoCaja(), detalleMostrador, observacion);
                turnoMostradorActivo = null;
            }
            
            // Mensaje final personalizado
            if (huboDescuadre) {
                JOptionPane.showMessageDialog(this,
                    "Turno cerrado. Se ha registrado el descuadre y las diferencias en el sistema.",
                    "TURNO CERRADO CON DESCUADRE", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Turno cerrado correctamente (Caja Cuadrada a la perfección).",
                    "TURNO CERRADO", JOptionPane.INFORMATION_MESSAGE);
            }
            
            actualizarEstadoBotones();
            limpiarTablero();
            if(pnlContenedorTarjetas != null) pnlContenedorTarjetas.removeAll(); 
            repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cerrar turno: " + e.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    // -------------------------------------------------------
    // DIÁLOGO: Pedir montos reales al recepcionista al cerrar
    // -------------------------------------------------------
    private List<TurnoCajaDetalleVO> pedirMontosRealesAlUsuario(List<TurnoCajaDetalleVO> acumulados) {
        List<TurnoCajaDetalleVO> resultado = new ArrayList<>();
        for (TurnoCajaDetalleVO acum : acumulados) {
            String titulo = acum.getNombreCaja() + " — " + acum.getDescripcionMetodo();
            String mensaje = "¿Cuánto dinero contó / verificó en:\n"
                    + titulo + "?\n\n"
                    + "Sistema espera: $ " + acum.getTotalIngresos();
            String input = JOptionPane.showInputDialog(this, mensaje, titulo, JOptionPane.QUESTION_MESSAGE);
            if (input == null) return null; // El usuario canceló
            try {
                BigDecimal montoReal = new BigDecimal(input.trim().replace(",", "."));
                acum.setMontoReal(montoReal);
                resultado.add(acum);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor ingresado no válido: " + input);
                return null;
            }
        }
        return resultado;
    }
    
    // -------------------------------------------------------
    // REFRESCAR TABLERO EN TIEMPO REAL
    // -------------------------------------------------------
    private void refrescarTablero() {
        try {
            // 1. Obtener los totales acumulados (CROSS JOIN) para actualizar las ETIQUETAS GRANDES sin fallar si hay ceros
            List<TurnoCajaDetalleVO> acumulados = boxService.doGetLiveAccumulados();
            limpiarTablero(); // Limpia los 4 modelos de las tablas
            
            BigDecimal totalHotelNequi        = BigDecimal.ZERO;
            BigDecimal totalMostradorNequi    = BigDecimal.ZERO;
            BigDecimal totalHotelEfectivo     = BigDecimal.ZERO;
            BigDecimal totalMostradorEfectivo = BigDecimal.ZERO;
            
            for (TurnoCajaDetalleVO d : acumulados) {
                boolean esHotel     = "CAJA HOTEL".equals(d.getNombreCaja());
                boolean esMostrador = "CAJA MOSTRADOR".equals(d.getNombreCaja());
                boolean esNequi     = d.getIdMetodo() != null && d.getIdMetodo() == ID_METODO_NEQUI;
                boolean esEfectivo  = d.getIdMetodo() != null && d.getIdMetodo() == ID_METODO_EFECTIVO;
                
                if (esHotel && esNequi) {
                    totalHotelNequi = totalHotelNequi.add(d.getTotalIngresos());
                } else if (esMostrador && esNequi) {
                    totalMostradorNequi = totalMostradorNequi.add(d.getTotalIngresos());
                } else if (esHotel && esEfectivo) {
                    totalHotelEfectivo = totalHotelEfectivo.add(d.getTotalIngresos());
                } else if (esMostrador && esEfectivo) {
                    totalMostradorEfectivo = totalMostradorEfectivo.add(d.getTotalIngresos());
                }
            }
            
            lblHotelNequiMonto.setText("$ " + totalHotelNequi.toPlainString());
            lblMostradorNequiMonto.setText("$ " + totalMostradorNequi.toPlainString());
            lblHotelEfectivoMonto.setText("$ " + totalHotelEfectivo.toPlainString());
            lblMostradorEfectivoMonto.setText("$ " + totalMostradorEfectivo.toPlainString());

            // 2. Obtener TODAS las ventas individuales para LLENAR LAS TABLAS con el detalle línea a línea
            List<TurnoCajaDetalleVO> ventasDetalladas = boxService.doGetLiveVentasDetalle();
            for (TurnoCajaDetalleVO v : ventasDetalladas) {
                boolean esHotel     = "CAJA HOTEL".equals(v.getNombreCaja());
                boolean esMostrador = "CAJA MOSTRADOR".equals(v.getNombreCaja());
                boolean esNequi     = v.getIdMetodo() != null && v.getIdMetodo() == ID_METODO_NEQUI;
                boolean esEfectivo  = v.getIdMetodo() != null && v.getIdMetodo() == ID_METODO_EFECTIVO;
                
                String concepto = v.getDescripcionMetodo(); // Aquí viene la observación (Check in, Venta rápida, etc...)
                String valor    = "$ " + v.getTotalIngresos().toPlainString();
                
                if (esHotel && esNequi) {
                    modelHotelNequi.addRow(new Object[]{ concepto, valor });
                } else if (esMostrador && esNequi) {
                    modelMostradorNequi.addRow(new Object[]{ concepto, valor });
                } else if (esHotel && esEfectivo) {
                    modelHotelEfectivo.addRow(new Object[]{ concepto, valor });
                } else if (esMostrador && esEfectivo) {
                    modelMostradorEfectivo.addRow(new Object[]{ concepto, valor });
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al refrescar tablero: " + e.getMessage(),
                    "HOTEL LAS TERRAZAS II - ERROR", JOptionPane.ERROR_MESSAGE);
        }
        refrescarPrestamos(); 
    }
    
    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------
    private void verificarTurnosActivos() {
        try {
            turnoHotelActivo     = boxService.doGetTurnoActivo(ID_CAJA_HOTEL);
            turnoMostradorActivo = boxService.doGetTurnoActivo(ID_CAJA_MOSTRADOR);
            actualizarEstadoBotones();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al verificar turnos: " + e.getMessage(),
                    "HOTEL LAS TERRAZAS II - ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarEstadoBotones() {
        boolean hayTurnosAbiertos = (turnoHotelActivo != null || turnoMostradorActivo != null);
        btnAbrirTurno.setEnabled(!hayTurnosAbiertos);
        btnCerrarTurno.setEnabled(hayTurnosAbiertos);
    }
    
    private void limpiarTablero() {
        modelHotelNequi.setRowCount(0);
        modelMostradorNequi.setRowCount(0);
        modelHotelEfectivo.setRowCount(0);
        modelMostradorEfectivo.setRowCount(0);
        lblHotelNequiMonto.setText("$ 0.00");
        lblMostradorNequiMonto.setText("$ 0.00");
        lblHotelEfectivoMonto.setText("$ 0.00");
        lblMostradorEfectivoMonto.setText("$ 0.00");
    }
    
    private List<TurnoCajaDetalleVO> filtrarPorTurno(List<TurnoCajaDetalleVO> lista, Integer idTurno) {
        List<TurnoCajaDetalleVO> resultado = new ArrayList<>();
        for (TurnoCajaDetalleVO d : lista) {
            if (idTurno != null && idTurno.equals(d.getIdTurnoCaja())) {
                resultado.add(d);
            }
        }
        return resultado;
    }
    
    // -------------------------------------------------------
    // CARGAR TARJETAS DE PRÉSTAMOS
    // -------------------------------------------------------
    private void refrescarPrestamos() {
        if (pnlContenedorTarjetas == null) return;
        
        pnlContenedorTarjetas.removeAll();
        try {
            List<com.adminHotel.vo.ConsumibleVO> prestables = boxService.doGetPrestablesConEstado();
            
            for (com.adminHotel.vo.ConsumibleVO c : prestables) {
                // Crear la tarjeta individual
                JPanel card = new JPanel(new BorderLayout());
                card.setBorder(BorderFactory.createLineBorder(COLOR_HEADER, 1, true));
                card.setPreferredSize(new Dimension(250, 75)); // Tamaño similar a tu dibujo
                
                // Título (Nombre del Artículo)
                JLabel lblTitulo = new JLabel(c.getNombre(), SwingConstants.CENTER);
                lblTitulo.setOpaque(true);
                lblTitulo.setBackground(COLOR_HEADER);
                lblTitulo.setForeground(Color.BLACK);
                lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
                lblTitulo.setPreferredSize(new Dimension(200, 25));
                card.add(lblTitulo, BorderLayout.NORTH);
                
                // Mitades inferiores
                JPanel pnlDatos = new JPanel(new GridLayout(1, 2));
                pnlDatos.setBackground(Color.WHITE);
                
                // --- Mitad Izquierda (PRESTADOS) ---
                JPanel pnlPrestados = new JPanel(new BorderLayout());
                pnlPrestados.setBackground(new Color(245, 245, 245));
                pnlPrestados.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
                JLabel lblP = new JLabel("PRESTADOS", SwingConstants.CENTER);
                lblP.setFont(new Font("Arial", Font.BOLD, 20));
                lblP.setForeground(Color.GRAY);
                JLabel valP = new JLabel(String.valueOf(c.getEnHabitaciones()), SwingConstants.CENTER);
                valP.setFont(new Font("Arial", Font.BOLD, 16));
                valP.setForeground(new Color(41, 128, 185)); // Azul
                pnlPrestados.add(lblP, BorderLayout.NORTH);
                pnlPrestados.add(valP, BorderLayout.CENTER);
                
                // --- Mitad Derecha (BODEGA/STOCK) ---
                JPanel pnlBodega = new JPanel(new BorderLayout());
                pnlBodega.setBackground(new Color(245, 245, 245));
                JLabel lblB = new JLabel("BODEGA", SwingConstants.CENTER);
                lblB.setFont(new Font("Arial", Font.BOLD, 20));
                lblB.setForeground(Color.GRAY);
                JLabel valB = new JLabel(String.valueOf(c.getStock()), SwingConstants.CENTER);
                valB.setFont(new Font("Arial", Font.BOLD, 16));
                valB.setForeground(new Color(46, 204, 113)); // Verde
                pnlBodega.add(lblB, BorderLayout.NORTH);
                pnlBodega.add(valB, BorderLayout.CENTER);
                
                pnlDatos.add(pnlPrestados);
                pnlDatos.add(pnlBodega);
                
                card.add(pnlDatos, BorderLayout.CENTER);
                
                // Añadir al flujo horizontal
                pnlContenedorTarjetas.add(card);
            }
            
        } catch (Exception e) {
            System.err.println("Error llenando panel de préstamos: " + e.getMessage());
        }
        
        // Obliga a pintar de nuevo los componentes dinámicos
        pnlContenedorTarjetas.revalidate();
        pnlContenedorTarjetas.repaint();
    }
    
    private void accionPrintTurno() {
    	
    	try {
    		
    		List<TurnoCajaDetalleVO> ventasDetalladas = boxService.doGetLiveVentasDetalle();
            
            if (ventasDetalladas == null || ventasDetalladas.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "No hay movimientos registrados en el turno activo para imprimir.", 
                        "HOTEL LAS TERRAZAS II - Arqueo de Caja", 
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            BigDecimal granTotal = BigDecimal.ZERO;
            String nombreCaja = "CAJA GENERAL";
            
            for (TurnoCajaDetalleVO d : ventasDetalladas) {
                if (d.getTotalIngresos() != null) {
                    granTotal = granTotal.add(d.getTotalIngresos());
                }
                
                if (d.getNombreCaja() != null && !d.getNombreCaja().trim().isEmpty()) {
                    nombreCaja = d.getNombreCaja().toUpperCase();
                }
            }
            
            PrinterJob job = PrinterJob.getPrinterJob();
            
	         // Calcular alto dinámico según cantidad de items
	         int altoFinal = Math.max(500, 150 + (ventasDetalladas.size() * 25) + 80);
	         // Configuración del PageFormat (para el renderizado interno de Java)
	         PageFormat pf = job.defaultPage();
	         Paper paper = new Paper();
	         paper.setSize(164, altoFinal);
	         paper.setImageableArea(0, 0, 164, altoFinal);
	         pf.setPaper(paper);
	         pf.setOrientation(PageFormat.PORTRAIT);
	         TicketCashRegister ticket = new TicketCashRegister(nombreCaja, ventasDetalladas, granTotal);
	         job.setPrintable(ticket, pf);
	         // *** LA CLAVE: Forzar el tamaño físico al driver de la impresora térmica ***
	         // Conversión: 1 punto Java = 0.352778mm
	         float anchoMM  = 58f;                          // Ancho fijo de la térmica 58mm
	         float altoMM   = altoFinal * 0.352778f;        // Alto dinámico convertido a mm
	         javax.print.attribute.PrintRequestAttributeSet attrs =
	             new javax.print.attribute.HashPrintRequestAttributeSet();
	             
	         attrs.add(javax.print.attribute.standard.OrientationRequested.PORTRAIT);
	         attrs.add(new javax.print.attribute.standard.MediaPrintableArea(
	             0, 0, anchoMM, altoMM,
	             javax.print.attribute.standard.MediaPrintableArea.MM));
	         job.print(attrs); // <-- Aquí el cambio crítico: pasar los atributos
            
            GUIToolkit.abrirCajon(this);
    		
    	} catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                    "Se presentó un error intentando enviar la información a la impresora.\nDetalle: " + ex.getMessage(), 
                    "HOTEL LAS TERRAZAS II - Error de Impresión", 
                    JOptionPane.ERROR_MESSAGE);
        }
    	
    }
    
 // -------------------------------------------------------
 // VALIDAR Y PROCESAR CIERRES FORZADOS PENDIENTES
 // -------------------------------------------------------
 private boolean procesarCierresForzadosPendientes() {
     try {
         TurnoSchedulerService scheduler = new TurnoSchedulerService();
         
         // Verificar si hay turnos vencidos
         if (scheduler.hayTurnosVencidos()) {
             
             // Mostrar mensaje de alerta
             StringBuilder mensaje = new StringBuilder();
             mensaje.append("¡ATENCIÓN! Hay turnos vencidos que requieren cierre forzado.\n\n");
             mensaje.append("Motivo: Ha pasado la hora de cierre (7AM/7PM) y los turnos no se han cerrado.\n\n");
             mensaje.append("Opciones:\n");
             mensaje.append("1. CERRAR FORZADAMENTE: El sistema cerrará los turnos automáticamente\n");
             mensaje.append("2. CANCELAR: No se podrá abrir nuevo turno hasta cerrar los existentes\n\n");
             mensaje.append("¿Desea proceder con el cierre forzado antes de abrir nuevo turno?");
             
             int respuesta = JOptionPane.showConfirmDialog(this,
                 mensaje.toString(),
                 "CIERRE FORZADO REQUERIDO - HOTEL LAS TERRAZAS II", 
                 JOptionPane.YES_NO_OPTION, 
                 JOptionPane.WARNING_MESSAGE);
             
             if (respuesta == JOptionPane.YES_OPTION) {
                 try {
                     // Ejecutar cierre forzado
                     scheduler.cerrarTurnosVencidos();
                     
                     // Mostrar confirmación
                     JOptionPane.showMessageDialog(this,
                         "✅ Cierre forzado realizado exitosamente.\n\n" +
                         "Los turnos vencidos han sido cerrados automáticamente.\n" +
                         "Ahora puede proceder a abrir un nuevo turno.",
                         "CIERRE FORZADO COMPLETADO - HOTEL LAS TERRAZAS II",
                         JOptionPane.INFORMATION_MESSAGE);
                     
                     // Actualizar estado de la interfaz
                     verificarTurnosActivos();
                     refrescarTablero();
                     
                     return true; // Cierre exitoso, puede continuar
                     
                 } catch (Exception ex) {
                     JOptionPane.showMessageDialog(this,
                         "❌ Error en cierre forzado:\n" + ex.getMessage() + "\n\n" +
                         "No se pudo cerrar los turnos vencidos.\n" +
                         "Contacte al administrador del sistema.",
                         "ERROR EN CIERRE FORZADO - HOTEL LAS TERRAZAS II",
                         JOptionPane.ERROR_MESSAGE);
                     return false; // Error, no puede continuar
                 }
             } else {
                 // Usuario canceló
                 JOptionPane.showMessageDialog(this,
                     "⏸️ Operación cancelada.\n\n" +
                     "No se abrirá nuevo turno.\n" +
                     "Debe cerrar manualmente los turnos vencidos en el módulo de caja.",
                     "APERTURA CANCELADA - HOTEL LAS TERRAZAS II",
                     JOptionPane.INFORMATION_MESSAGE);
                 return false; // Cancelado, no puede continuar
             }
         }
         
         return true; // No hay cierres pendientes, puede continuar
         
     } catch (Exception e) {
         JOptionPane.showMessageDialog(this,
             "Error verificando cierres forzados: " + e.getMessage(),
             "ERROR - HOTEL LAS TERRAZAS II",
             JOptionPane.ERROR_MESSAGE);
         return false; // Error, no puede continuar
     }
 }
}
