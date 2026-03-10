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

import com.adminHotel.dao.HabitacionDAO;
import com.adminHotel.service.OperationService;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.EstadoHabitacionEnum;
import com.adminHotel.util.MetodoPagoEnum;
import com.adminHotel.vo.HabitacionVO;

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
		super("HOTEL LAS TERRAZAS II - Mapa de Habitaciones", true, true, true, true);

		setLayout(new BorderLayout());

		SwingUtilities.invokeLater(() -> {

			JDesktopPane desktop = getDesktopPane();

			if (desktop != null) {
				Dimension size = desktop.getSize();

				int width = (int) (size.width * 0.90);
				int height = (int) (size.height * 0.90);

				setSize(width, height);
				setLocation((size.width - width) / 2, (size.height - height) / 2);
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

		panelHabitaciones = new JPanel();
		panelHabitaciones.setLayout(new BoxLayout(panelHabitaciones, BoxLayout.Y_AXIS));
		panelHabitaciones.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK, grosorBorde),
			    "HABITACIONES",
			    TitledBorder.LEFT,
			    TitledBorder.TOP,
			    fuenteTituloBordes
		));

		cargarHabitaciones();

		JScrollPane scroll = new JScrollPane(panelHabitaciones);
		scroll.setBorder(null);

		add(scroll, BorderLayout.NORTH);
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

			List<HabitacionVO> lista = habitacionDAO.listarHabitaciones();

			// Agrupar por piso
			Map<Integer, List<HabitacionVO>> pisos = new TreeMap<>();

			for (HabitacionVO h : lista) {
				pisos.computeIfAbsent(h.getPiso(), k -> new ArrayList<>()).add(h);
			}

			// Crear UI por cada piso
			for (Integer piso : pisos.keySet()) {
				crearPiso(panelHabitaciones, piso, pisos.get(piso));
			}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error cargando habitaciones\n" + e);
		}
	}

	// -------------------------------
	// CREA BLOQUE DE PISO DINÁMICO
	// -------------------------------
	private void crearPiso(JPanel contenedor, Integer numeroPiso, List<HabitacionVO> habitaciones) {

		JPanel panelPiso = new JPanel(new BorderLayout());
		panelPiso.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

		lblIcono.setPreferredSize(new Dimension(50, botonTemp.getPreferredSize().height + 20));

		panelPiso.add(lblIcono, BorderLayout.WEST);

		// =========================
		// HABITACIONES
		// =========================
		JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

		for (HabitacionVO h : habitaciones) {
			panelBotones.add(crearBotonHabitacion(h));
		}

		panelPiso.add(panelBotones, BorderLayout.CENTER);

		panelPiso.setAlignmentX(Component.LEFT_ALIGNMENT);
		contenedor.add(panelPiso);
	}

	// ----------------------------------
	// BOTÓN HABITACIÓN
	// ----------------------------------
	private JButton crearBotonHabitacion(HabitacionVO h) {

		JButton boton = new JButton(String.valueOf(h.getNumeroHabitacion()));

		boton.setPreferredSize(new Dimension(90, 70));
		boton.setFocusPainted(false);
		boton.setFont(fuenteBotones);
		boton.setForeground(Color.WHITE);

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
			    fuenteTituloBordes
		));

		JLabel lbl = new JLabel("Seleccione una habitación para ver el detalle", SwingConstants.CENTER);
		lbl.setFont(new Font("Arial", Font.PLAIN, 16));
		panelCentro.add(lbl, BorderLayout.CENTER);

		add(panelCentro, BorderLayout.CENTER);
	}

	// -------------------------------
	// PINTAR DETALLE HABITACION
	// -------------------------------
	private void mostrarDetalleHabitacion(HabitacionVO habitacion) {

		panelCentro.removeAll();

		JPanel contenedor = new JPanel();
		contenedor.setLayout(new GridLayout(1, 2, 10, 10));
		contenedor.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		contenedor.add(crearPanelInfo(habitacion));
		contenedor.add(crearPanelOperaciones(habitacion));

		panelCentro.add(contenedor, BorderLayout.CENTER);

		panelCentro.revalidate();
		panelCentro.repaint();
	}

	// -------------------------------
	// MOSTRAR DETALLE HABITACION (ESTILIZADO)
	// -------------------------------
	private JPanel crearPanelInfo(HabitacionVO h) {

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(Color.BLACK, grosorBorde),
			    "Información de la Habitación",
			    TitledBorder.LEFT,
			    TitledBorder.TOP,
			    fuenteTituloBordes
		));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 10, 8, 10);
		gbc.anchor = GridBagConstraints.WEST;

		Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
		Font valueFont = new Font("Segoe UI", Font.PLAIN, 16);

		int row = 0;

		row = agregarFila(panel, gbc, row, "Número:", String.valueOf(h.getNumeroHabitacion()), labelFont, valueFont);

		row = agregarFila(panel, gbc, row, "Piso:", String.valueOf(h.getPiso()), labelFont, valueFont);

		row = agregarFila(panel, gbc, row, "Tipo:", h.getTipoDescripcion() != null ? h.getTipoDescripcion() : "-",
				labelFont, valueFont);

		row = agregarFila(panel, gbc, row, "Precio por noche:", formatearMoneda(h.getPrecio()), labelFont, valueFont);

		row = agregarFila(panel, gbc, row, "Estado:", h.getEstadoDescripcion() != null ? h.getEstadoDescripcion() : "-",
				labelFont, valueFont);

		row = agregarFila(panel, gbc, row, "Descripción:", h.getDescripcion() != null ? h.getDescripcion() : "-",
				labelFont, valueFont);

		return panel;
	}

	// -------------------------------
	// MOSTRAR DETALLE HABITACION (ESTILIZADO)
	// -------------------------------
	private int agregarFila(JPanel panel, GridBagConstraints gbc, int row, String etiqueta, String valor,
			Font labelFont, Font valueFont) {

		gbc.gridx = 0;
		gbc.gridy = row;
		JLabel lbl = new JLabel(etiqueta);
		lbl.setFont(labelFont);
		panel.add(lbl, gbc);

		gbc.gridx = 1;
		JLabel val = new JLabel(valor);
		val.setFont(valueFont);
		val.setForeground(new Color(40, 40, 40));
		panel.add(val, gbc);

		return row + 1;
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
		panel.setBorder(BorderFactory.createTitledBorder(
	            BorderFactory.createLineBorder(Color.BLACK, grosorBorde),
	            "OPERACIONES",
	            TitledBorder.LEFT,
	            TitledBorder.TOP,
	            fuenteTituloBordes
	    ));
	    
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
			btnAccion.setText("ASIGNAR");
			btnAccion.addActionListener(e -> doCheckIn(h));
			panel.add(btnAccion, gbc);
			break;

		case "OCUPADA":
			btnAccion.setText("REGISTRAR SALIDA");
			btnAccion.addActionListener(e -> doCheckOut(h));
			panel.add(btnAccion, gbc);
			
			JButton btnPrint = new JButton("IMPRIMIR TICKET");
			btnPrint.setPreferredSize(new Dimension(260, 40));
			btnPrint.setFont(fuenteBotones);
			btnPrint.addActionListener(e -> doPrint(h));
			panel.add(btnPrint, gbc);
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

		default:
			btnAccion.setVisible(false);
			break;
		}

		return panel;
	}

	// -------------------------------
	// CHECK IN
	// -------------------------------
	private void doCheckIn(HabitacionVO habitacion) {

		// =============================
		// PEDIR CANTIDAD NOCHES ?
		// =============================
		Object[] opciones = { "1", "2", "3", "4", "5" };
		int noches = 1 + JOptionPane.showOptionDialog(
				this, 
				"¿Cuantos Noches Se Hospedara?",
				"HOTEL LAS TERRAZAS II - Número de Noches...",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				opciones, opciones[0]);

		if (noches == 0)
			return;

		// =============================
		// SOLICITAR PAGO
		// =============================
		int opcion = JOptionPane.showConfirmDialog(
				this,
				"CLIENTE DEBE PAGAR ".concat(new BigDecimal(noches).multiply(habitacion.getPrecio()).toString()),
				"HOTEL LAS TERRAZAS II - Pedir Pago...",
				JOptionPane.YES_NO_OPTION);

		if (opcion != JOptionPane.YES_OPTION) {
			JOptionPane.showMessageDialog(
					this,
					"SI EL CLIENTE NO PAGA, NO SE ASIGNA HABITACION!!!",
					"HOTEL LAS TERRAZAS II - No Asigna Habitación...",
					JOptionPane.WARNING_MESSAGE);

			return;
		}

		// =============================
		// METODO DE PAGO
		// =============================
		Object[] opcionesIconos = {
				new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/efectivo.png")),
				new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/nequi.png")),
				new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/daviplata.png")) };
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
			// Puedes usar un switch para ejecutar la lógica según el número
			switch (seleccion) {
			case 0:
				seleccion = MetodoPagoEnum.EFECTIVO.getCodigo();
				break;
			case 1:
				seleccion = MetodoPagoEnum.NEQUI.getCodigo();
				break;
			case 2:
				seleccion = MetodoPagoEnum.DAVIPLATA.getCodigo();
				break;
			}
		} else {
			if (opcion != JOptionPane.YES_OPTION) {
				JOptionPane.showMessageDialog(
						this,
						"DEBE SELECCIONAR METODO DE PAGO!!!",
						"HOTEL LAS TERRAZAS II - No Asigna Habitación...",
						JOptionPane.WARNING_MESSAGE);
			}
		}

		// =============================
		// PEDIR DOCUMENTO
		// =============================
		String documento = "";
		boolean validar = true;
		while (validar) {

			validar = false;

			documento = JOptionPane.showInputDialog(
					this,
					"Número Identificación del cliente:",
					"HOTEL LAS TERRAZAS II - Identificación Cliente...",
					JOptionPane.QUESTION_MESSAGE);

			if (documento != null) {
				documento = documento.trim();
				if (!documento.isEmpty()) {

					if (!documento.matches("\\d+")) {
						JOptionPane.showMessageDialog(
								this,
								"El documento solo debe contener números...",
								"HOTEL LAS TERRAZAS II - Número Documento Invalido...",
								JOptionPane.WARNING_MESSAGE);
						validar = true;
					}

				} else
					validar = true;
			} else
				return;
		}

		// =============================
		// TRANSACCION CHECK IN
		// =============================
		service = new OperationService();
		try {
			service.doTransactionCheckIn(habitacion, documento, noches, seleccion);

			refrescarMapaHabitaciones();

			JOptionPane.showMessageDialog(
					this,
					"¡Check-In realizado con éxito!\nHabitación: " + habitacion.getNumeroHabitacion(),
					"HOTEL LAS TERRAZAS II - Registrado...",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
	                this,
	                "Error al procesar el Check-In: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " + e.getMessage(),
	                "HOTEL LAS TERRAZAS II - Error del Sistema",
	                JOptionPane.ERROR_MESSAGE
	        );
		}

	}

	// -------------------------------
	// CHECK OUT
	// -------------------------------
	private void doCheckOut(HabitacionVO habitacion) {
		
		int confirmar = JOptionPane.showConfirmDialog(
	            this,
	            "¿Desea realizar el Check-Out de la habitación " + habitacion.getNumeroHabitacion() + "?\n" +
	            "Esta acción liberará la habitación y registrará la fecha de salida.",
	            "HOTEL LAS TERRAZAS II - Confirmar Salida",
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.QUESTION_MESSAGE
	    );
		
		if (confirmar != JOptionPane.YES_OPTION) return;
		
		// =============================
		// TRANSACCION CHECK OUT
		// =============================
		service = new OperationService();
		try {
			service.doTransactionCheckOut(habitacion);
			
			refrescarMapaHabitaciones();

			JOptionPane.showMessageDialog(
					this,
					"¡Check-Out realizado con éxito!\nHabitación: " + habitacion.getNumeroHabitacion(),
					"HOTEL LAS TERRAZAS II - Registrado...",
					JOptionPane.INFORMATION_MESSAGE);
			
		} catch (Exception e) {
	        JOptionPane.showMessageDialog(
	                this,
	                "Error al procesar el Check-Out: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " + e.getMessage(),
	                "HOTEL LAS TERRAZAS II - Error del Sistema",
	                JOptionPane.ERROR_MESSAGE
	        );
	    }
	}

	// -------------------------------
	// CAMBIO DE ESTADO
	// -------------------------------
	private void doChangeState(HabitacionVO habitacion, Integer state) {
		
		int confirmar = Integer.MIN_VALUE;
		
		if(state == EstadoHabitacionEnum.DISPONIBLE.getCodigo()) {
			confirmar = JOptionPane.showConfirmDialog(
		            this,
		            "¿Desea Dejar Disponible la Habitacion " + habitacion.getNumeroHabitacion() + "?",
		            "HOTEL LAS TERRAZAS II - Confirmar Estado...",
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE
		    );
		}
		
		if(state == EstadoHabitacionEnum.NO_DISPONIBLE.getCodigo()) {
			confirmar = JOptionPane.showConfirmDialog(
		            this,
		            "¿Desea Bloquear la Habitacion " + habitacion.getNumeroHabitacion() + "?",
		            "HOTEL LAS TERRAZAS II - Confirmar Estado...",
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE
		    );
		}
		
		if(state == EstadoHabitacionEnum.POR_ASEO.getCodigo()) {
			confirmar = JOptionPane.showConfirmDialog(
		            this,
		            "¿Desea Asear la Habitacion " + habitacion.getNumeroHabitacion() + "?",
		            "HOTEL LAS TERRAZAS II - Confirmar Estado...",
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE
		    );
		}
		
		if (confirmar != JOptionPane.YES_OPTION) return;
		
		// =============================
		// TRANSACCION CHECK OUT
		// =============================
		service = new OperationService();
		try {
			service.doTransactionChangeState(habitacion, state);
			
			refrescarMapaHabitaciones();

			JOptionPane.showMessageDialog(
					this,
					"Actualización con éxito!\nHabitación: " + habitacion.getNumeroHabitacion(),
					"HOTEL LAS TERRAZAS II - Actualizado...",
					JOptionPane.INFORMATION_MESSAGE);
			
		} catch (Exception e) {
	        JOptionPane.showMessageDialog(
	                this,
	                "Error al procesar Actualización: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " + e.getMessage(),
	                "HOTEL LAS TERRAZAS II - Error del Sistema",
	                JOptionPane.ERROR_MESSAGE
	        );
	    }
	}

	// -------------------------------
	// IMPRIMIR TICKET
	// -------------------------------
	private void doPrint(HabitacionVO habitacion) {
		
		service = new OperationService();
		try {
			
			service.doPrintTicket(habitacion.getIdHabitacion());
			
		} catch (Exception e) {
	        JOptionPane.showMessageDialog(
	                this,
	                "Error al Imprimir Ticket: \nDetalle: " + e.getClass().getSimpleName() + "\nMotivo: " + e.getMessage(),
	                "HOTEL LAS TERRAZAS II - Error del Sistema",
	                JOptionPane.ERROR_MESSAGE
	        );
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
	}	
}