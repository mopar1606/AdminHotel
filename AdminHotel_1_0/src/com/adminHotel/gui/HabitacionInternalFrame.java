package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import com.adminHotel.dao.ConsumibleDAO;
import com.adminHotel.dao.HabitacionDAO;
import com.adminHotel.dao.HabitacionMovimientoDAO;
import com.adminHotel.dao.PrestamoConsumibleDAO;
import com.adminHotel.service.OperationService;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.EstadoHabitacionEnum;
import com.adminHotel.util.GUIToolkit;
import com.adminHotel.util.MetodoPagoEnum;
import com.adminHotel.vo.ConsumibleVO;
import com.adminHotel.vo.HabitacionVO;
import com.adminHotel.vo.PrestamoConsumibleVO;

public class HabitacionInternalFrame extends JInternalFrame {

    private static final long serialVersionUID = -6216033766678853720L;

    private JPanel panelHabitaciones;
    private JPanel panelCentro;
    private OperationService service;
    private Font fuenteTituloBordes = new Font("Arial", Font.BOLD, 20);
    private Font fuenteBotones = new Font("Arial", Font.BOLD, 20);
    private Integer grosorBorde = 3;

    // ------------------------------------------------
    // INSTANCIA INTERNAL FRAME HABITACION
    // ------------------------------------------------
    public HabitacionInternalFrame() {
        super("HOTEL LAS TERRAZAS II - Administracion de Habitaciones", true, true, true, true);

        setLayout(new BorderLayout());

        SwingUtilities.invokeLater(() -> {
            JDesktopPane desktop = getDesktopPane();
            if (desktop != null) {

                Dimension size = desktop.getSize();
                setSize(size.width, size.height);
                setLocation(0, 0);
            }
        });

        service = new OperationService();
        inicializarPanelHabitaciones();
        inicializarPanelCentro();
    }

    // ------------------------------------------------
    // PANEL NORTE - HABITACIONES
    // ------------------------------------------------
    private void inicializarPanelHabitaciones() {
        JPanel franjaSuperior = new JPanel(new BorderLayout(10, 0));
        // 1. Ya NO le ponemos el borde al panel de habitaciones para evitar que el título se mueva si scrolleamos
        panelHabitaciones = new JPanel();
        panelHabitaciones.setLayout(new BoxLayout(panelHabitaciones, BoxLayout.Y_AXIS));
        cargarHabitaciones();
        JPanel panelOpciones = new JPanel();
        
        // Lo dejamos a 2 filas para que se alinee estéticamente
        panelOpciones.setLayout(new java.awt.GridLayout(3, 1, 5, 5));
        panelOpciones.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, grosorBorde),
            "EXTRAS",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            fuenteTituloBordes));
        JButton duchas = new JButton("DUCHAS");
        duchas.setPreferredSize(new Dimension(260, 40));
        duchas.setFont(fuenteBotones);
        duchas.addActionListener(e -> doShower());
        JButton bano = new JButton("BAÑO");
        bano.setPreferredSize(new Dimension(260, 40));
        bano.setFont(fuenteBotones);
        bano.addActionListener(e -> doBathroom());
        JButton lavanderia = new JButton("LAVANDERIA");
        lavanderia.setPreferredSize(new Dimension(260, 40));
        lavanderia.setFont(fuenteBotones);
        lavanderia.addActionListener(e -> doLaundry());
        panelOpciones.add(duchas);
        panelOpciones.add(bano);
        panelOpciones.add(lavanderia);
        
        // 2. LA MAGIA: Envolvemos los pisos en el Scroll y le ponemos el Título de Marco a él. 
        JScrollPane scrollHabitaciones = new JScrollPane(panelHabitaciones);
        scrollHabitaciones.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, grosorBorde),
            "HABITACIONES",
            TitledBorder.LEFT, TitledBorder.TOP, fuenteTituloBordes));
        scrollHabitaciones.getVerticalScrollBar().setUnitIncrement(16); // Un scroll suave mediante la rueda del raton
        franjaSuperior.add(scrollHabitaciones, BorderLayout.CENTER);
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.add(panelOpciones, BorderLayout.NORTH);
        franjaSuperior.add(envoltorio, BorderLayout.EAST);
        // 3. ¡VITAL! Lo marcamos como CENTER general. Esto forzará al sistema a darle todo el espacio posible de diseño al panel de Operaciones primero, evitando el "aplastamiento" del recuadro rojo.
        add(franjaSuperior, BorderLayout.CENTER);
    }

    // ------------------------------------------------
    // CARGA DESDE BD Y AGRUPA POR PISO
    // ------------------------------------------------
    private void cargarHabitaciones() {

        Connection cn = null;
        try {

            cn = DBConnection.getConnection();
            cn.setAutoCommit(false);

            HabitacionDAO habitacionDAO = new HabitacionDAO(cn);

            List < HabitacionVO > lista = habitacionDAO.listarHabitaciones();

            // Agrupar por piso
            Map < Integer, List < HabitacionVO >> pisos = new TreeMap < > ();

            for (HabitacionVO h: lista) {
                pisos.computeIfAbsent(h.getPiso(), k -> new ArrayList < > ()).add(h);
            }

            // Crear UI por cada piso
            for (Integer piso: pisos.keySet()) {
                crearPiso(panelHabitaciones, piso, pisos.get(piso));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error cargando habitaciones\n" + e);
        }
    }

    // -------------------------------
    // CREA BLOQUE DE PISO DINAMICO
    // -------------------------------
    private void crearPiso(JPanel contenedor, Integer numeroPiso, List < HabitacionVO > habitaciones) {

        JPanel panelPiso = new JPanel(new BorderLayout());
        panelPiso.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        // =========================
        // ICONO DEL PISO
        // =========================
        String rutaIcono = "/com/adminHotel/gui/complements/P" + numeroPiso + ".png";

        JLabel lblIcono = new JLabel();
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);

        try {
            lblIcono.setIcon(new ImageIcon(getClass().getResource(rutaIcono)));
        } catch (Exception e) {
            lblIcono.setText("P" + numeroPiso);
        }

        JButton botonTemp = new JButton();
        botonTemp.setPreferredSize(new Dimension(90, 70));

        lblIcono.setPreferredSize(new Dimension(50, botonTemp.getPreferredSize().height + 10));

        panelPiso.add(lblIcono, BorderLayout.WEST);

        // =========================
        // HABITACIONES
        // =========================
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 4));

        for (HabitacionVO h: habitaciones) {
            panelBotones.add(crearBotonHabitacion(h));
        }

        panelPiso.add(panelBotones, BorderLayout.CENTER);

        panelPiso.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedor.add(panelPiso);
    }

    // ----------------------------------
    // BOTON HABITACION
    // ----------------------------------
    private JButton crearBotonHabitacion(HabitacionVO h) {

        JButton boton = new JButton(String.valueOf(h.getNumeroHabitacion()));

        boton.setPreferredSize(new Dimension(85, 60));
        boton.setFocusPainted(false);
        boton.setFont(fuenteBotones);
        boton.setForeground(Color.BLACK);

        // =========================
        // ESTADO
        // =========================
        String estado = h.getEstadoDescripcion();

        if (estado == null)
            estado = "DISPONIBLE";

        String rutaIcono;

        switch (estado) {

        case "OCUPADA":
            boton.setBackground(new Color(231, 76, 60));
            rutaIcono = "/com/adminHotel/gui/complements/ocupada.png";
            break;

        case "POR ASEO":
            boton.setBackground(new Color(243, 156, 18));
            rutaIcono = "/com/adminHotel/gui/complements/limpiar.png";
            break;

        case "NO DISPONIBLE":
            boton.setBackground(new Color(181, 176, 176));
            rutaIcono = "/com/adminHotel/gui/complements/nosirve.png";
            break;
            
        case "RESERVADA":
            boton.setBackground(new Color(155, 89, 182));
            rutaIcono = "/com/adminHotel/gui/complements/booking.png";
            break;

        default:
            boton.setBackground(new Color(46, 204, 113));
            rutaIcono = "/com/adminHotel/gui/complements/disponible.png";
            break;
        }

        // =========================
        // ICONO SEGURO
        // =========================
        java.net.URL url = getClass().getResource(rutaIcono);
        if (url != null) {
            boton.setIcon(new ImageIcon(url));
        }

        boton.setHorizontalTextPosition(SwingConstants.CENTER);
        boton.setVerticalTextPosition(SwingConstants.BOTTOM);

        boton.addActionListener(e -> mostrarDetalleHabitacion(h));

        return boton;
    }

    // -------------------------------
    // PANEL CENTRAL
    // -------------------------------
    private void inicializarPanelCentro() {

        panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLACK, grosorBorde),
            "DETALLE Y OPERACIONES",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            fuenteTituloBordes));

        JLabel lbl = new JLabel("Seleccione una habitación para ver el detalle!", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        panelCentro.add(lbl, BorderLayout.CENTER);

        add(panelCentro, BorderLayout.SOUTH);
    }

    // -------------------------------
    // PINTAR DETALLE HABITACION
    // -------------------------------
    private void mostrarDetalleHabitacion(HabitacionVO habitacion) {

        panelCentro.removeAll();

        JPanel contenedor = new JPanel();
        contenedor.setLayout(new GridBagLayout());
        contenedor.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.65;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 10);
        contenedor.add(crearPanelInfo(habitacion), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 0, 0, 0);
        contenedor.add(crearPanelOperaciones(habitacion), gbc);

        panelCentro.add(contenedor, BorderLayout.CENTER);

        panelCentro.revalidate();
        panelCentro.repaint();
    }

    // -------------------------------
    // MOSTRAR DETALLE HABITACION (ESTILIZADO 2 COLUMNAS)
    // -------------------------------
    private JPanel crearPanelInfo(HabitacionVO h) {

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, grosorBorde),
                "Informacion de la Habitacion",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                fuenteTituloBordes),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 20);
        Font valueFont = new Font("Arial", Font.BOLD, 20);

        int row = 0;

        // Fila 0
        agregarColumnaDoble(panel, gbc, 0, row, "NUMERO:", String.valueOf(h.getNumeroHabitacion()), labelFont,
            valueFont);
        agregarColumnaDoble(panel, gbc, 2, row, "PISO:", String.valueOf(h.getPiso()), labelFont, valueFont);
        row++;

        // Fila 1
        String tipoStr = h.getTipoDescripcion() != null ? h.getTipoDescripcion() : "-";
        agregarColumnaDoble(panel, gbc, 0, row, "TIPO:",
            "<html><div style='width: 120px;'>" + tipoStr + "</div></html>", labelFont, valueFont);

        String estadoStr = h.getEstadoDescripcion() != null ? h.getEstadoDescripcion() : "-";
        agregarColumnaDoble(panel, gbc, 2, row, "ESTADO:", estadoStr, labelFont, valueFont);
        row++;

        // Fila 2
        int colActual = 0;
        if (h.getPrecioSencilla() != null && h.getPrecioSencilla().compareTo(BigDecimal.ZERO) > 0) {
            agregarColumnaDoble(panel, gbc, colActual, row, "PRECIO 1:", formatearMoneda(h.getPrecioSencilla()),
                labelFont, valueFont);
            colActual += 2;
        }
        if (h.getPrecioDoble() != null && h.getPrecioDoble().compareTo(BigDecimal.ZERO) > 0) {
            agregarColumnaDoble(panel, gbc, colActual, row, "PRECIO 2:", formatearMoneda(h.getPrecioDoble()), labelFont,
                valueFont);
            colActual += 2;
        }
        if (h.getPrecioTres() != null && h.getPrecioTres().compareTo(BigDecimal.ZERO) > 0) {
            agregarColumnaDoble(panel, gbc, colActual, row, "PRECIO 3:", formatearMoneda(h.getPrecioTres()), labelFont,
                valueFont);
            colActual += 2;
        }
        if (colActual > 0) {
            row++;
        }

        // Fila 3: Descripcion (Ocupa todo el ancho si queremos, o solo una celda)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        JLabel lblDesc = new JLabel("DESC:");
        lblDesc.setFont(labelFont);
        panel.add(lblDesc, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3; // Ocupa las 3 celdas restantes de las columnas
        gbc.weightx = 1.0;
        String desc = h.getDescripcion() != null ? h.getDescripcion() : "-";
        JLabel valDesc = new JLabel("<html><div style='width: 300px;'>" + desc + "</div></html>");
        valDesc.setFont(valueFont);
        valDesc.setForeground(new Color(40, 40, 40));
        panel.add(valDesc, gbc);

        return panel;
    }

    // -------------------------------
    // AGREGAR BIFURCADO EN 2 COLUMNAS
    // -------------------------------
    private void agregarColumnaDoble(JPanel panel, GridBagConstraints gbc, int startCol, int row, String etiqueta,
        String valor,
        Font labelFont, Font valueFont) {

        gbc.gridy = row;
        gbc.gridwidth = 1;

        gbc.gridx = startCol;
        gbc.weightx = 0.0;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(labelFont);
        panel.add(lbl, gbc);

        gbc.gridx = startCol + 1;
        gbc.weightx = 1.0;
        JLabel val = new JLabel(valor);
        val.setFont(valueFont);
        val.setForeground(new Color(40, 40, 40));
        panel.add(val, gbc);
    }

    // -------------------------------
    // FORMATO MONEDA
    // -------------------------------
    private String formatearMoneda(BigDecimal valor) {

        if (valor == null)
            return "-";

        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        return formato.format(valor);
    }

    // -------------------------------
    // OPERACIONES
    // -------------------------------
    private JPanel crearPanelOperaciones(HabitacionVO h) {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, grosorBorde),
                "OPERACIONES",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                fuenteTituloBordes),
            BorderFactory.createEmptyBorder(15, 10, 15, 10)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton btnAccion = new JButton();
        btnAccion.setPreferredSize(new Dimension(260, 40));
        btnAccion.setFont(fuenteBotones);

        String estado = h.getEstadoDescripcion();

        if (estado == null)
            estado = "DISPONIBLE";

        switch (estado) {

        case "DISPONIBLE":
            btnAccion.setText("HOSPEDAR");
            btnAccion.addActionListener(e -> doCheckIn(h));
            panel.add(btnAccion, gbc);

            JButton btnBitTime = new JButton("RATO");
            btnBitTime.setPreferredSize(new Dimension(260, 40));
            btnBitTime.setFont(fuenteBotones);
            btnBitTime.addActionListener(e -> doBitTime(h));
            panel.add(btnBitTime, gbc);
            
            JButton btnReservar = new JButton("RESERVAR");
            btnReservar.setPreferredSize(new Dimension(260, 40));
            btnReservar.setFont(fuenteBotones);
            btnReservar.addActionListener(e -> doReservar(h));
            panel.add(btnReservar, gbc);

            break;

        case "OCUPADA":
            JButton btnAdicional = new JButton("ADICIONAL");
            btnAdicional.setPreferredSize(new Dimension(260, 40));
            btnAdicional.setFont(fuenteBotones);
            btnAdicional.addActionListener(e -> doAdicionarPersona(h));
            panel.add(btnAdicional, gbc);

            JButton btnPrint = new JButton("IMPRIMIR TICKET");
            btnPrint.setPreferredSize(new Dimension(260, 40));
            btnPrint.setFont(fuenteBotones);
            btnPrint.addActionListener(e -> doPrint(h));
            panel.add(btnPrint, gbc);
            
            JButton btnCambio = new JButton("CAMBIAR HABITACIÓN");
            btnCambio.setPreferredSize(new Dimension(260, 40));
            btnCambio.setFont(fuenteBotones);
            btnCambio.addActionListener(e -> doCambioMoverHabitacion(h));
            panel.add(btnCambio, gbc);

            btnAccion.setText("REGISTRAR SALIDA");
            btnAccion.addActionListener(e -> doCheckOut(h));
            panel.add(btnAccion, gbc);
            break;

        case "POR ASEO":
            btnAccion.setText("MARCAR DISPONIBLE");
            btnAccion.addActionListener(e -> doChangeState(h, EstadoHabitacionEnum.DISPONIBLE.getCodigo()));
            panel.add(btnAccion, gbc);

            JButton btnReportar = new JButton("FUERA DE SERVICIO");
            btnReportar.setPreferredSize(new Dimension(260, 40));
            btnReportar.setFont(fuenteBotones);
            btnReportar.addActionListener(e -> doChangeState(h, EstadoHabitacionEnum.NO_DISPONIBLE.getCodigo()));
            panel.add(btnReportar, gbc);
            break;

        case "NO DISPONIBLE":
            btnAccion.setText("MARCAR DISPONIBLE");
            btnAccion.addActionListener(e -> doChangeState(h, EstadoHabitacionEnum.DISPONIBLE.getCodigo()));
            panel.add(btnAccion, gbc);

            JButton btnAseo = new JButton("LIMPIEZA");
            btnAseo.setPreferredSize(new Dimension(260, 40));
            btnAseo.setFont(fuenteBotones);
            btnAseo.addActionListener(e -> doChangeState(h, EstadoHabitacionEnum.POR_ASEO.getCodigo()));
            panel.add(btnAseo, gbc);
            break;
            
        case "RESERVADA":
            JButton btnHospedarReserva = new JButton("HOSPEDAR");
            btnHospedarReserva.setPreferredSize(new Dimension(260, 40));
            btnHospedarReserva.setFont(fuenteBotones);
            btnHospedarReserva.addActionListener(e -> doCheckIn(h));
            panel.add(btnHospedarReserva, gbc);
            
            JButton btnCancelarReserva = new JButton("CANCELAR RESERVA");
            btnCancelarReserva.setPreferredSize(new Dimension(260, 40));
            btnCancelarReserva.setFont(fuenteBotones);
            btnCancelarReserva.addActionListener(e -> doCancelarReserva(h));
            panel.add(btnCancelarReserva, gbc);
            break;

        default:
            btnAccion.setVisible(false);
            break;
        }

        return panel;
    }

    // -------------------------------
    // TAKE SHOWER
    // -------------------------------
    private void doShower() {
        service = new OperationService();
        try {
            Integer idRecibo = service.doTransactionShower();
            if (idRecibo != null) {
                imprimirTicketServicio(idRecibo, "DUCHA");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al procesar Una Ducha: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " + e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------
    // BATHROOM
    // -------------------------------
    private void doBathroom() {
        service = new OperationService();
        try {
            Integer idRecibo = service.doTransactionBathroom();
            if (idRecibo != null) {
                imprimirTicketServicio(idRecibo, "BAÑO");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al procesar Baño: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " + e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
	// -------------------------------
	// LAVANDERIA
	// -------------------------------
	private void doLaundry() {
		service = new OperationService();
		try {
			Integer idRecibo = service.doTransactionLaundry();
			if (idRecibo != null) {
				imprimirTicketServicio(idRecibo, "LAVANDERIA");
			}
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(this,
							"Error al procesar Lavandería: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: "
									+ e.getMessage(),
							"HOTEL LAS TERRAZAS II - Error del Sistema", JOptionPane.ERROR_MESSAGE);
		}
	}

    // -------------------------------
    // BIT TIME - CHECK IN
    // -------------------------------
    private void doBitTime(HabitacionVO habitacion) {
        service = new OperationService();
        try {

            service.doTransactionBitTime(habitacion);

            refrescarMapaHabitaciones();
            JOptionPane.showMessageDialog(
                this,
                "Check-in registrado con Exito, Habitacion: " + habitacion.getNumeroHabitacion(),
                "HOTEL LAS TERRAZAS II - Registrado...",
                JOptionPane.INFORMATION_MESSAGE);

            int printOpt = JOptionPane.showConfirmDialog(
                this,
                "¿QUIERE IMPRIMIR RECIBO " + habitacion.getNumeroHabitacion() + "?",
                "HOTEL LAS TERRAZAS II - Imprimir Recibo...",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (printOpt == JOptionPane.YES_OPTION) {
                service.doPrintTicket(habitacion.getIdHabitacion());
            } else {
                //abrirCajon();
                GUIToolkit.abrirCajon(this);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al procesar el Check-In Rato: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " +
                e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------
    // CHECK IN
    // -------------------------------
    private void doCheckIn(HabitacionVO habitacion) {

        // =============================
        // PEDIR CANTIDAD NOCHES
        // =============================
        Object[] opciones = {
            "1",
            "2",
            "3",
            "4",
            "5"
        };
        int noches = 1 + JOptionPane.showOptionDialog(
            this,
            "Cuantas Noches Se Hospedara?",
            "HOTEL LAS TERRAZAS II - Numero de Noches...",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opciones, opciones[0]);
        if (noches == 0)
            return;

        // =============================
        // NUMERO DE PERSONAS
        // =============================
        java.util.List < String > listOpciones = new java.util.ArrayList < > ();
        java.util.List < Integer > listCapacidades = new java.util.ArrayList < > ();
        if (habitacion.getPrecioSencilla() != null && habitacion.getPrecioSencilla().compareTo(BigDecimal.ZERO) > 0) {
            listOpciones.add("1 PERSONA");
            listCapacidades.add(1);
        }
        if (habitacion.getPrecioDoble() != null && habitacion.getPrecioDoble().compareTo(BigDecimal.ZERO) > 0) {
            listOpciones.add("2 PERSONAS");
            listCapacidades.add(2);
        }
        if (habitacion.getPrecioTres() != null && habitacion.getPrecioTres().compareTo(BigDecimal.ZERO) > 0) {
            listOpciones.add("3 PERSONAS");
            listCapacidades.add(3);
        }

        if (listOpciones.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Esta habitación no tiene precios de renta configurados.", "Alerta",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] opcionesPersonas = listOpciones.toArray();
        int personasSel = JOptionPane.showOptionDialog(
            this,
            "Para Cuantas Personas?",
            "HOTEL LAS TERRAZAS II - Numero Ocupantes...",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            opcionesPersonas, opcionesPersonas[0]);
        if (personasSel == -1)
            return;
        int personas = listCapacidades.get(personasSel);

        // =============================
        // SOLICITAR PAGO
        // =============================
        BigDecimal auxprecio = new BigDecimal("0.0");
        if (personas == 1)
            auxprecio = habitacion.getPrecioSencilla();
        if (personas == 2)
            auxprecio = habitacion.getPrecioDoble();
        if (personas == 3)
            auxprecio = habitacion.getPrecioTres();
        if (personas < 1)
            return;

        int opcion = JOptionPane.showConfirmDialog(
            this,
            "CLIENTE DEBE PAGAR ".concat(new BigDecimal(noches).multiply(auxprecio).toString()),
            "HOTEL LAS TERRAZAS II - Pedir Pago...",
            JOptionPane.YES_NO_OPTION);

        if (opcion != JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(
                this,
                "SI EL CLIENTE NO PAGA, NO SE ASIGNA HABITACION!!!",
                "HOTEL LAS TERRAZAS II - No Asigna Habitaci�n...",
                JOptionPane.WARNING_MESSAGE);

            return;
        }

        // =============================
        // METODO DE PAGO
        // =============================
        Object[] opcionesIconos = {
            new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/efectivo.png")),
            new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/nequi.png"))
        };
        int seleccion = JOptionPane.showOptionDialog(
            this,
            "CUAL ES EL METODO DE PAGO?",
            "HOTEL LAS TERRAZAS II - Metodo de Pago",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            opcionesIconos,
            opcionesIconos[0]);

        if (seleccion == JOptionPane.CLOSED_OPTION)
            return;

        if (seleccion != JOptionPane.CLOSED_OPTION) {
            // Puedes usar un switch para ejecutar la l�gica seg�n el n�mero
            switch (seleccion) {
            case 0:
                seleccion = MetodoPagoEnum.EFECTIVO.getCodigo();
                break;
            case 1:
                seleccion = MetodoPagoEnum.NEQUI.getCodigo();
                break;
            }
        } else {
            if (opcion != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(
                    this,
                    "DEBE SELECCIONAR METODO DE PAGO!!!",
                    "HOTEL LAS TERRAZAS II - No Asigna Habitaci�n...",
                    JOptionPane.WARNING_MESSAGE);
            }
        }

        // =============================
        // PRESTAMO DE CONSUMIBLES (Toallas, etc)
        // =============================
        List < PrestamoConsumibleVO > prestamos = new ArrayList < > ();
        try {
            Connection cn = DBConnection.getConnection();
            ConsumibleDAO consumibleDAO = new ConsumibleDAO(cn);
            List < ConsumibleVO > disponibles = consumibleDAO.listarActivosParaPrestamo();
            cn.close();
            if (!disponibles.isEmpty()) {

                // Columnas: consumible | [ - ] [ N ] [ + ]
                JPanel pnlConsumibles = new JPanel(new GridLayout(disponibles.size() + 1, 2, 10, 10));
                java.awt.Font fuenteGrande = new java.awt.Font("Arial", java.awt.Font.BOLD, 20);
                JLabel lblTitulo1 = new JLabel("Consumible");
                lblTitulo1.setFont(fuenteGrande);
                JLabel lblTitulo2 = new JLabel("Cantidad a prestar");
                lblTitulo2.setFont(fuenteGrande);
                pnlConsumibles.add(lblTitulo1);
                pnlConsumibles.add(lblTitulo2);

                // En vez de spinners: usamos un int[] para guardar el valor de cada consumible
                int[] cantidades = new int[disponibles.size()];
                for (int idx = 0; idx < disponibles.size(); idx++) {
                    ConsumibleVO cons = disponibles.get(idx);
                    int stockMax = cons.getStock().intValue();
                    // Límite real: máximo 5 o el stock disponible, lo que sea menor
                    int limite = Math.min(5, stockMax);
                    JLabel lblItem = new JLabel(cons.getNombre() + " (Disp: " + stockMax + ")");
                    lblItem.setFont(fuenteGrande);
                    pnlConsumibles.add(lblItem);
                    // Panel de botones [ - ] [ lblCantidad ] [ + ]
                    JPanel pnlBotones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 8, 0));
                    JButton btnMenos = new JButton("-");
                    JLabel lblCant = new JLabel("0", JLabel.CENTER);
                    JButton btnMas = new JButton("+");
                    btnMenos.setFont(fuenteGrande);
                    lblCant.setFont(fuenteGrande);
                    btnMas.setFont(fuenteGrande);
                    btnMenos.setPreferredSize(new java.awt.Dimension(50, 40));
                    lblCant.setPreferredSize(new java.awt.Dimension(40, 40));
                    btnMas.setPreferredSize(new java.awt.Dimension(50, 40));
                    final int i = idx;
                    final int max = limite;
                    btnMas.addActionListener(e -> {
                        if (cantidades[i] < max) {
                            cantidades[i]++;
                            lblCant.setText(String.valueOf(cantidades[i]));
                        }
                    });
                    btnMenos.addActionListener(e -> {
                        if (cantidades[i] > 0) {
                            cantidades[i]--;
                            lblCant.setText(String.valueOf(cantidades[i]));
                        }
                    });
                    pnlBotones.add(btnMenos);
                    pnlBotones.add(lblCant);
                    pnlBotones.add(btnMas);
                    pnlConsumibles.add(pnlBotones);
                }

                int resultPrestamo = JOptionPane.showConfirmDialog(this,
                    pnlConsumibles,
                    "Desea prestar consumibles a la habitacion?", JOptionPane.OK_CANCEL_OPTION);

                if (resultPrestamo != JOptionPane.OK_OPTION) {
                    JOptionPane.showMessageDialog(this,
                        "Check-In cancelado por el usuario.",
                        "HOTEL LAS TERRAZAS II - Cancelado",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (resultPrestamo == JOptionPane.OK_OPTION) {
                    for (int i = 0; i < disponibles.size(); i++) {
                        int cantidad = cantidades[i]; // ← ya no es spinner.getValue()
                        if (cantidad > 0) {
                            PrestamoConsumibleVO p = new PrestamoConsumibleVO();
                            p.setIdConsumible(disponibles.get(i).getIdConsumible());
                            p.setCantidadEntregada(cantidad);
                            prestamos.add(p);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo cargar inventario de consumibles para checkin: " + e.getMessage());
        }

        // =============================
        // TRANSACCION CHECK IN
        // =============================
        service = new OperationService();
        try {
            service.doTransactionCheckIn(habitacion, noches, personas, seleccion, prestamos, auxprecio);

            refrescarMapaHabitaciones();
            JOptionPane.showMessageDialog(
                this,
                "Check-in registrado con Exito, Habitacion: " + habitacion.getNumeroHabitacion(),
                "HOTEL LAS TERRAZAS II - Registrado...",
                JOptionPane.INFORMATION_MESSAGE);

            int printOpt = JOptionPane.showConfirmDialog(
                this,
                "¿QUIERE IMPRIMIR RECIBO " + habitacion.getNumeroHabitacion() + "?",
                "HOTEL LAS TERRAZAS II - Imprimir Recibo...",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (printOpt == JOptionPane.YES_OPTION) {
                service.doPrintTicket(habitacion.getIdHabitacion());
            } else {
                //abrirCajon();
                GUIToolkit.abrirCajon(this);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al procesar el Check-In: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " +
                e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------
    // CHECK OUT
    // -------------------------------
    private void doCheckOut(HabitacionVO habitacion) {

        List < PrestamoConsumibleVO > devolucionesReales = new ArrayList < > ();

        try {
            // 1. OBTENER PRÉSTAMOS PENDIENTES DE LA HABITACIÓN
            Connection cn = DBConnection.getConnection();
            HabitacionMovimientoDAO movDAO = new HabitacionMovimientoDAO(cn);
            PrestamoConsumibleDAO prestamoDAO = new PrestamoConsumibleDAO(cn);

            Integer idMovimientoActivo = movDAO.obtenerMovimientoActivo(habitacion.getIdHabitacion());

            if (idMovimientoActivo == null) {
                JOptionPane.showMessageDialog(this,
                    "No se encontró un movimiento de entrada activo para esta habitación.",
                    "HOTEL LAS TERRAZAS II - Error Check-Out",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Buscar préstamos pendientes
            List < PrestamoConsumibleVO > prestamosPendientes = prestamoDAO.buscarPrestamosPendientesPorMovimiento(idMovimientoActivo);
            cn.close();

            // 2. SI HAY PRÉSTAMOS PENDIENTES, SOLICITAR DEVOLUCIÓN
            if (prestamosPendientes != null && !prestamosPendientes.isEmpty()) {

                // Contenedor principal que envuelve el titulo y la tabla
                JPanel contenedorCentral = new JPanel(new BorderLayout(0, 15));
                JLabel titulo = new JLabel("REGISTRAR DEVOLUCIÓN - Habitación " + habitacion.getNumeroHabitacion(), SwingConstants.CENTER);
                titulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 22));
                contenedorCentral.add(titulo, BorderLayout.NORTH);

                // Panel Estilo Check-In (Dos columnas grandes)
                JPanel panelPrincipal = new JPanel(new GridLayout(prestamosPendientes.size() + 1, 2, 10, 10));
                java.awt.Font fuenteGrande = new java.awt.Font("Arial", java.awt.Font.BOLD, 20);

                // Títulos de Encabezado
                JLabel lblTitulo1 = new JLabel("Objeto Prestado");
                lblTitulo1.setFont(fuenteGrande);
                JLabel lblTitulo2 = new JLabel("Cantidad a Devolver");
                lblTitulo2.setFont(fuenteGrande);
                panelPrincipal.add(lblTitulo1);
                panelPrincipal.add(lblTitulo2);

                // Arreglo para mantener seguimiento numérico y etiquetas de interfaz
                int[] cantDevueltas = new int[prestamosPendientes.size()];
                List < JLabel > contadores = new ArrayList < > ();

                for (int idx = 0; idx < prestamosPendientes.size(); idx++) {
                    PrestamoConsumibleVO prestamo = prestamosPendientes.get(idx);
                    int maxCantidad = prestamo.getCantidadEntregada();

                    cantDevueltas[idx] = maxCantidad; // Por defecto el sistema asume que devuelven todo

                    JLabel lblItem = new JLabel(prestamo.getNombreConsumible() + " (Prestó: " + maxCantidad + ")");
                    lblItem.setFont(fuenteGrande);
                    panelPrincipal.add(lblItem);

                    JPanel pnlBotones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 8, 0));
                    JButton btnMenos = new JButton("-");
                    JLabel lblCant = new JLabel(String.valueOf(cantDevueltas[idx]), SwingConstants.CENTER);
                    JButton btnMas = new JButton("+");

                    btnMenos.setFont(fuenteGrande);
                    lblCant.setFont(fuenteGrande);
                    btnMas.setFont(fuenteGrande);

                    btnMenos.setPreferredSize(new java.awt.Dimension(50, 40));
                    lblCant.setPreferredSize(new java.awt.Dimension(40, 40));
                    btnMas.setPreferredSize(new java.awt.Dimension(50, 40));

                    contadores.add(lblCant);

                    final int i = idx;
                    final int max = maxCantidad;

                    btnMas.addActionListener(e -> {
                        if (cantDevueltas[i] < max) {
                            cantDevueltas[i]++;
                            lblCant.setText(String.valueOf(cantDevueltas[i]));
                        }
                    });

                    btnMenos.addActionListener(e -> {
                        if (cantDevueltas[i] > 0) {
                            cantDevueltas[i]--;
                            lblCant.setText(String.valueOf(cantDevueltas[i]));
                        }
                    });

                    pnlBotones.add(btnMenos);
                    pnlBotones.add(lblCant);
                    pnlBotones.add(btnMas);

                    panelPrincipal.add(pnlBotones);
                }

                // Integramos la tabla al centro
                contenedorCentral.add(panelPrincipal, BorderLayout.CENTER);

                // Imprimimos la alerta con el contenedor combinado
                int opcion = JOptionPane.showConfirmDialog(this,
                    contenedorCentral,
                    "HOTEL LAS TERRAZAS II - Devolución de Objetos",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

                // 3. PROCESAR LAS DEVOLUCIONES REGISTRADAS
                for (int i = 0; i < prestamosPendientes.size(); i++) {
                    PrestamoConsumibleVO prestamo = prestamosPendientes.get(i);
                    int cantidadDevuelta = Integer.parseInt(contadores.get(i).getText());

                    prestamo.setCantidadDevuelta(cantidadDevuelta);
                    devolucionesReales.add(prestamo);
                }
            }

            // 4. VERIFICAR SI HAY OBJETOS NO DEVUELTOS (PARA MULTAS)
            boolean hayMultas = false;
            List < String > objetosNoDevueltos = new ArrayList < > ();

            for (PrestamoConsumibleVO devolucion: devolucionesReales) {
                if (devolucion.getCantidadDevuelta() < devolucion.getCantidadEntregada()) {
                    hayMultas = true;
                    int faltantes = devolucion.getCantidadEntregada() - devolucion.getCantidadDevuelta();
                    objetosNoDevueltos.add(devolucion.getNombreConsumible() + " (" + faltantes + " unidades)");
                }
            }

            Integer metodoPagoMulta = null;

            // 5. SI HAY MULTAS, SOLICITAR MÉTODO DE PAGO
            if (hayMultas) {
                // Mostrar resumen de objetos no devueltos
                StringBuilder mensajeMulta = new StringBuilder();
                mensajeMulta.append("Hay objetos no devueltos:\n\n");
                for (String objeto: objetosNoDevueltos) {
                    mensajeMulta.append("• ").append(objeto).append("\n");
                }
                mensajeMulta.append("\nSeleccione método de pago para la multa:");

                Object[] opcionesIconos = {
                    new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/efectivo.png")),
                    new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/nequi.png"))
                };

                int seleccion = JOptionPane.showOptionDialog(this,
                    mensajeMulta.toString(),
                    "HOTEL LAS TERRAZAS II - Método de Pago Multa",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opcionesIconos,
                    opcionesIconos[0]);

                if (seleccion == JOptionPane.CLOSED_OPTION) {
                    JOptionPane.showMessageDialog(this,
                        "Check-out cancelado. Debe seleccionar método de pago para las multas.",
                        "HOTEL LAS TERRAZAS II - Check-Out Cancelado",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                switch (seleccion) {
                case 0:
                    metodoPagoMulta = MetodoPagoEnum.EFECTIVO.getCodigo();
                    break;
                case 1:
                    metodoPagoMulta = MetodoPagoEnum.NEQUI.getCodigo();
                    break;
                default:
                    metodoPagoMulta = MetodoPagoEnum.EFECTIVO.getCodigo();
                    break;
                }
            }

            // 6. EJECUTAR EL CHECK-OUT
            OperationService service = new OperationService();
            try {
                service.doTransactionCheckOut(habitacion, devolucionesReales, metodoPagoMulta);

                JOptionPane.showMessageDialog(this,
                    "Check-out realizado exitosamente para la habitación " + habitacion.getNumeroHabitacion(),
                    "HOTEL LAS TERRAZAS II - Check-Out Exitoso",
                    JOptionPane.INFORMATION_MESSAGE);

                refrescarMapaHabitaciones();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al procesar el Check-Out:\n" + e.getMessage(),
                    "HOTEL LAS TERRAZAS II - Error Check-Out",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error en el proceso de Check-Out:\n" + e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error Check-Out",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------
    // CAMBIO DE ESTADO
    // -------------------------------
    private void doChangeState(HabitacionVO habitacion, Integer state) {

        int confirmar = Integer.MIN_VALUE;

        if (state == EstadoHabitacionEnum.DISPONIBLE.getCodigo()) {
            confirmar = JOptionPane.showConfirmDialog(
                this,
                "Desea Dejar Disponible la Habitacion " + habitacion.getNumeroHabitacion() + "?",
                "HOTEL LAS TERRAZAS II - Confirmar Estado...",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        }

        if (state == EstadoHabitacionEnum.NO_DISPONIBLE.getCodigo()) {
            confirmar = JOptionPane.showConfirmDialog(
                this,
                "Desea Bloquear la Habitacion " + habitacion.getNumeroHabitacion() + "?",
                "HOTEL LAS TERRAZAS II - Confirmar Estado...",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        }

        if (state == EstadoHabitacionEnum.POR_ASEO.getCodigo()) {
            confirmar = JOptionPane.showConfirmDialog(
                this,
                "Desea Asear la Habitacion " + habitacion.getNumeroHabitacion() + "?",
                "HOTEL LAS TERRAZAS II - Confirmar Estado...",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        }

        if (confirmar != JOptionPane.YES_OPTION)
            return;

        // =============================
        // TRANSACCION CHECK OUT
        // =============================
        service = new OperationService();
        try {
            service.doTransactionChangeState(habitacion, state);

            refrescarMapaHabitaciones();

            JOptionPane.showMessageDialog(
                this,
                "Actualizacion Entrega Habitacion: " + habitacion.getNumeroHabitacion(),
                "HOTEL LAS TERRAZAS II - Actualizado...",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al procesar Actualizacion: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " +
                e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------
    // ADICIONAR PERSONA
    // -------------------------------
    private void doAdicionarPersona(HabitacionVO habitacion) {

        // 1. OBTENER LA TARIFA DESDE LA BASE DE DATOS
        java.math.BigDecimal tarifaAdicional = null;
        try {
            java.sql.Connection cn = DBConnection.getConnection();
            com.adminHotel.dao.ParametroDAO parDAO = new com.adminHotel.dao.ParametroDAO(cn);
            tarifaAdicional = parDAO.obtenerValor("TARIFA_PERSONA_ADICIONAL");
            cn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Aviso: No se pudo cargar TARIFA_PERSONA_ADICIONAL: " + e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }

        // 2. FORMATEAR EL PRECIO 
        String textoPrecio = (tarifaAdicional != null) ? formatearMoneda(tarifaAdicional) : "$0.00";

        // 3. MOSTRAR PRECIO EN PRIMER AVISO
        int confirmar = JOptionPane.showConfirmDialog(
            this,
            "¿Desea adicionar una persona a la habitación " + habitacion.getNumeroHabitacion() + "?\n\n" +
            "Se aplicará la tarifa de persona adicional por valor de: " + textoPrecio,
            "HOTEL LAS TERRAZAS II - Adicionar Persona",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION)
            return;
        // Metodo de pago
        Object[] opcionesIconos = {
            new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/efectivo.png")),
            new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/nequi.png"))
        };
        int seleccion = JOptionPane.showOptionDialog(
            this,
            "EL CLIENTE DEBE PAGAR: " + textoPrecio + "\n\n¿CUÁL ES EL MÉTODO DE PAGO?",
            "HOTEL LAS TERRAZAS II - Método de Pago",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            opcionesIconos,
            opcionesIconos[0]);
        if (seleccion == JOptionPane.CLOSED_OPTION)
            return;
        switch (seleccion) {
        case 0:
            seleccion = MetodoPagoEnum.EFECTIVO.getCodigo();
            break;
        case 1:
            seleccion = MetodoPagoEnum.NEQUI.getCodigo();
            break;
        }
        service = new OperationService();
        try {
            service.doTransactionAdicionarPersona(habitacion, seleccion);
            JOptionPane.showMessageDialog(
                this,
                "Persona adicional registrada con Habitacion Numero: " + habitacion.getNumeroHabitacion(),
                "HOTEL LAS TERRAZAS II - Registrado...",
                JOptionPane.INFORMATION_MESSAGE);

            int print = JOptionPane.showConfirmDialog(
                this,
                "Registro exitoso.\n¿Desea imprimir el recibo de esta Persona Adicional?",
                "HOTEL LAS TERRAZAS II - Imprimir Recibo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (print == JOptionPane.YES_OPTION) {
                service.doPrintTicketAdicional(habitacion.getIdHabitacion());
            } else {
                GUIToolkit.abrirCajon(this);
                //abrirCajon();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al adicionar persona: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " +
                e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------
    // IMPRIMIR TICKET
    // -------------------------------
    private void doPrint(HabitacionVO habitacion) {

        service = new OperationService();
        try {

            int printOpt = JOptionPane.showConfirmDialog(
                this,
                "¿QUIERE IMPRIMIR RECIBO?" + habitacion.getNumeroHabitacion() + "?",
                "HOTEL LAS TERRAZAS II - Imprimir Recibo...",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (printOpt == JOptionPane.YES_OPTION) {
                service.doPrintTicket(habitacion.getIdHabitacion());
            } else {
                //abrirCajon();
                GUIToolkit.abrirCajon(this);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error al Imprimir Ticket: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " +
                e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error del Sistema",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------
    // IMPRIMIR TICKET DUCHA / BAÑO
    // Reutiliza TicketVentaMostrador con un item sintético
    // -------------------------------
    private void imprimirTicketServicio(Integer idRecibo, String tipoServicio) {
        int printOpt = JOptionPane.showConfirmDialog(
            this,
            "¿DESEA IMPRIMIR EL RECIBO No. " + idRecibo + "?",
            "HOTEL LAS TERRAZAS II - Imprimir",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (printOpt != JOptionPane.YES_OPTION) {
            GUIToolkit.abrirCajon(this);
            return;
        }

        try {
            // Crear un item sintético para pasar a TicketVentaMostrador
            // usando DetalleTemporal con un ProductoVO genérico
            com.adminHotel.vo.ProductoVO productoFake = new com.adminHotel.vo.ProductoVO();
            productoFake.setNombre(tipoServicio);
            // La tarifa la tomamos del servicio ya registrado (la mostramos como 0 si no aplica)
            // Alternativa: pasar la tarifa como parámetro adicional
            productoFake.setPrecioVenta(java.math.BigDecimal.ZERO);
            com.adminHotel.util.DetalleTemporal itemFake = new com.adminHotel.util.DetalleTemporal();
            itemFake.setProducto(productoFake);
            itemFake.setCantidad(1);
            // El subtotal lo pondrá DetalleTemporal automáticamente si tiene el método
            java.util.List < com.adminHotel.util.DetalleTemporal > items = new java.util.ArrayList < > ();
            items.add(itemFake);
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            java.awt.print.PageFormat pf = job.defaultPage();
            java.awt.print.Paper papel = pf.getPaper();
            double anchoMM = 58 * 2.8346;
            papel.setSize(anchoMM, 400);
            papel.setImageableArea(0, 0, anchoMM, 400);
            pf.setPaper(papel);
            com.adminHotel.printer.TicketVentaMostrador ticket =
                new com.adminHotel.printer.TicketVentaMostrador(
                    idRecibo,
                    items,
                    java.math.BigDecimal.ZERO, // El total real se obtendría del DTO
                    new java.sql.Timestamp(System.currentTimeMillis()),
                    "EFECTIVO");
            job.setPrintable(ticket, pf);
            job.print();
            GUIToolkit.abrirCajon(this);
        } catch (java.awt.print.PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al imprimir:\n" + ex.getMessage(),
                "HOTEL LAS TERRAZAS II - Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
 // ==========================================================
    // MÉTODO INTERFAZ: TRASLADAR Y COBRAR (Nivel Seguro)
    // ==========================================================
    private void doCambioMoverHabitacion(HabitacionVO habVieja) {
        try {
            // 1. Obtener Noches originales y Dinero previamente pagado solo en ALOJAMIENTOS de esta pieza
        	// 1. Obtener Noches, Cliente y Fecha del check-in actual
            int nochesOriginales = 0;
            Integer clienteId = null;
            java.sql.Timestamp fechaEnt = null;
            java.math.BigDecimal totalPagado = java.math.BigDecimal.ZERO;
            
            try (java.sql.Connection cn = DBConnection.getConnection()) {
                // Sacamos quién es, cuántas noches y cuándo entró
                String sql1 = "SELECT noches, id_cliente, fecha_entrada FROM habitacion_movimiento WHERE id_habitacion = ? AND id_estado_registro = 1";
                try(java.sql.PreparedStatement ps = cn.prepareStatement(sql1)) {
                    ps.setInt(1, habVieja.getIdHabitacion());
                    try(java.sql.ResultSet rs = ps.executeQuery()) {
                        if(rs.next()) {
                            nochesOriginales = rs.getInt("noches");
                            clienteId = rs.getInt("id_cliente");
                            fechaEnt = rs.getTimestamp("fecha_entrada");
                        }
                    }
                }
                
             // Solo lo pagado en ESA habitación por ALOJAMIENTO desde que entró el huésped actual
                if(fechaEnt != null) {
                    String sql2 = "SELECT SUM(p.valor) as total " +
                                  "FROM pago p " +
                                  "INNER JOIN venta v ON p.id_venta = v.id_venta " +
                                  "WHERE v.id_habitacion = ? " +
                                  "AND v.id_concepto_venta = 1 " +
                                  "AND p.id_estado_registro = 1 " +
                                  "AND v.fecha >= DATE_SUB(?, INTERVAL 1 HOUR)";
                    try(java.sql.PreparedStatement ps = cn.prepareStatement(sql2)) {
                        ps.setInt(1, habVieja.getIdHabitacion());
                        ps.setTimestamp(2, fechaEnt);
                        try(java.sql.ResultSet rs = ps.executeQuery()) {
                            if(rs.next() && rs.getBigDecimal("total") != null) {
                                totalPagado = rs.getBigDecimal("total");
                            }
                        }
                    }
                }                
            }
            if (nochesOriginales == 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "La habitación de origen no posee huéspedes vigentes."); 
                return;
            }
            // 2. Buscar libres
            java.util.List<HabitacionVO> libres = new java.util.ArrayList<>();
            try (java.sql.Connection cn = DBConnection.getConnection()) {
                com.adminHotel.dao.HabitacionDAO dao = new com.adminHotel.dao.HabitacionDAO(cn);
                for (HabitacionVO h : dao.listarHabitaciones()) if (h.getIdEstadoHabitacion() == 1) libres.add(h);
            }
            if (libres.isEmpty()) { javax.swing.JOptionPane.showMessageDialog(this, "No hay habitaciones libres para cambiar."); return; }
            // 3. Dropdown para elegir la Habitación de Destino
            String[] arrayLibres = new String[libres.size()];
            for (int i=0; i<libres.size(); i++) arrayLibres[i] = "Hab. " + libres.get(i).getNumeroHabitacion() + " (" + libres.get(i).getTipoDescripcion() + ")";
            javax.swing.JComboBox<String> combo = new javax.swing.JComboBox<>(arrayLibres);
            combo.setFont(new Font("Arial", Font.BOLD, 18));
            
            Object[] msg = { "Mover a habitación:", combo };
            int op1 = javax.swing.JOptionPane.showConfirmDialog(this, msg, "Selección de Cuarto", javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
            if (op1 != javax.swing.JOptionPane.OK_OPTION) return; // Cancela el asistente
            
            HabitacionVO habNueva = libres.get(combo.getSelectedIndex());
            // 4. Exigir selección de tarifa de Capacidad en el cuarto nuevo
            java.util.List<String> opcPers = new java.util.ArrayList<>();
            java.util.List<java.math.BigDecimal> opcPrec = new java.util.ArrayList<>();
            if (habNueva.getPrecioSencilla() != null && habNueva.getPrecioSencilla().compareTo(java.math.BigDecimal.ZERO) > 0) { opcPers.add("1 PERSONA - " + formatearMoneda(habNueva.getPrecioSencilla())); opcPrec.add(habNueva.getPrecioSencilla()); }
            if (habNueva.getPrecioDoble() != null && habNueva.getPrecioDoble().compareTo(java.math.BigDecimal.ZERO) > 0) { opcPers.add("2 PERSONAS - " + formatearMoneda(habNueva.getPrecioDoble())); opcPrec.add(habNueva.getPrecioDoble()); }
            if (habNueva.getPrecioTres() != null && habNueva.getPrecioTres().compareTo(java.math.BigDecimal.ZERO) > 0) { opcPers.add("3 PERSONAS - " + formatearMoneda(habNueva.getPrecioTres())); opcPrec.add(habNueva.getPrecioTres()); }
            int selTarifa = javax.swing.JOptionPane.showOptionDialog(this, "¿Bajo qué ocupación se re-ubicará al cliente en la Hab " + habNueva.getNumeroHabitacion() + "?", "Ajuste de Precio - Paso 1", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, opcPers.toArray(), opcPers.get(0));
            if (selTarifa < 0) return; // Cancela
            
            // 5. Motor Matemático
            java.math.BigDecimal tarifaElegidaXVnoche = opcPrec.get(selTarifa);
            java.math.BigDecimal totalNuevoExigido = tarifaElegidaXVnoche.multiply(new java.math.BigDecimal(nochesOriginales));
            java.math.BigDecimal excedente = totalNuevoExigido.subtract(totalPagado);
            // 6. Análisis Final: O Pasa Normal, O Se Cobra.
            if (excedente.compareTo(java.math.BigDecimal.ZERO) > 0) {
                // El cuarto nuevo es MÁS caro. ¡HAY QUE COBRAR!
                Object[] iconosPago = {
                    new javax.swing.ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/efectivo.png")),
                    new javax.swing.ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/nequi.png"))
                };
                
                int selPago = javax.swing.JOptionPane.showOptionDialog(this, 
                    "¡ATENCIÓN! La nueva habitación genera un costo excedente total de " + formatearMoneda(excedente) + "\n\n" +
                    "Pagó Antes: " + formatearMoneda(totalPagado) + "\n" +
                    "Valor Nuevo (" + nochesOriginales + " Noches): " + formatearMoneda(totalNuevoExigido) + "\n\n" +
                    "MÉTODO DE PAGO para procesar el Cambio de Habitación:",
                    "HOTEL LAS TERRAZAS II - CUADRE FINANCIERO",
                    javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.WARNING_MESSAGE, null, iconosPago, iconosPago[0]);
                
                if (selPago < 0) return; // Si cierran la ventana aquí sin elegir método, el check-in falla y no jode tu caja
                int metodoId = (selPago == 0) ? 1 : 2; // 1=Efect, 2=Nequi
                
                com.adminHotel.service.OperationService srv = new com.adminHotel.service.OperationService();
                srv.doTransactionCambioHabitacion(habVieja.getIdHabitacion(), habNueva.getIdHabitacion(), excedente, metodoId);
                
            } else {
                // Es del mismo precio o menor valor (Excedente devuelto cero o menor)
                int ok = javax.swing.JOptionPane.showConfirmDialog(this, 
                    "No se registran excedentes a cobrar (Habitación de menor o igual rango).\n¿Confirma continuar para ubicar el huésped en la Hab " + habNueva.getNumeroHabitacion() + "?", 
                    "HOTEL LAS TERRAZAS II", javax.swing.JOptionPane.YES_NO_OPTION);
                if (ok != javax.swing.JOptionPane.YES_OPTION) return;
                
                com.adminHotel.service.OperationService srv = new com.adminHotel.service.OperationService();
                srv.doTransactionCambioHabitacion(habVieja.getIdHabitacion(), habNueva.getIdHabitacion(), java.math.BigDecimal.ZERO, null);
            }
            javax.swing.JOptionPane.showMessageDialog(this, "TRASLADO HABITACION OK...", "HOTEL LAS TERRAZAS II", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            refrescarMapaHabitaciones();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Se cayó la operación:\n" + e.getMessage(), "ERROR INTERNO", javax.swing.JOptionPane.ERROR_MESSAGE); e.printStackTrace();
        }
    }

	// -------------------------------
	// RESERVAR HABITACION
	// -------------------------------
	private void doReservar(HabitacionVO habitacion) {
		String nombre = JOptionPane.showInputDialog(this, "Ingrese el nombre para la reserva:",
				"HOTEL LAS TERRAZAS II - Reservar Habitación " + habitacion.getNumeroHabitacion(),
				JOptionPane.QUESTION_MESSAGE);
		if (nombre == null || nombre.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Debe ingresar un nombre para realizar la reserva.",
					"HOTEL LAS TERRAZAS II - Reserva Cancelada", JOptionPane.WARNING_MESSAGE);
			return;
		}
		service = new OperationService();
		try {
			service.doTransactionReservar(habitacion, nombre.trim().toUpperCase());
			refrescarMapaHabitaciones();
			JOptionPane.showMessageDialog(this,
					"Habitación " + habitacion.getNumeroHabitacion() + " reservada a nombre de: "
							+ nombre.trim().toUpperCase(),
					"HOTEL LAS TERRAZAS II - Reserva Exitosa", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(this,
							"Error al reservar la habitación:\nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: "
									+ e.getMessage(),
							"HOTEL LAS TERRAZAS II - Error del Sistema", JOptionPane.ERROR_MESSAGE);
		}
	}

	// -------------------------------
	// CANCELAR RESERVA
	// -------------------------------
	private void doCancelarReserva(HabitacionVO habitacion) {
		int confirmar = JOptionPane.showConfirmDialog(this,
				"¿Desea CANCELAR la reserva de la habitación " + habitacion.getNumeroHabitacion() + "?",
				"HOTEL LAS TERRAZAS II - Cancelar Reserva", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (confirmar != JOptionPane.YES_OPTION)
			return;
		service = new OperationService();
		try {
			service.doTransactionCancelarReserva(habitacion);
			refrescarMapaHabitaciones();
			JOptionPane.showMessageDialog(this,
					"Reserva cancelada. Habitación " + habitacion.getNumeroHabitacion() + " disponible nuevamente.",
					"HOTEL LAS TERRAZAS II - Reserva Cancelada", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(this,
							"Error al cancelar la reserva:\nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: "
									+ e.getMessage(),
							"HOTEL LAS TERRAZAS II - Error del Sistema", JOptionPane.ERROR_MESSAGE);
		}
	}

    // =============================
    // REFRESCAR MAPA
    // =============================
    private void refrescarMapaHabitaciones() {
        panelHabitaciones.removeAll();
        cargarHabitaciones();
        panelHabitaciones.revalidate();
        panelHabitaciones.repaint();

        if (panelCentro != null) {
            panelCentro.removeAll();
            panelCentro.revalidate();
            panelCentro.repaint();
        }
    }
}