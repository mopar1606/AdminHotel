package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class HabitacionInternalFrame extends JInternalFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6216033766678853720L;

	private JPanel panelHabitaciones;
    private JPanel panelCentro;

    public HabitacionInternalFrame() {

        super("Mapa de Habitaciones",
                true, true, true, true);

        setSize(1000, 600);
        setLocation(30, 30);
        setLayout(new BorderLayout());

        inicializarPanelHabitaciones();
        inicializarPanelCentro();
    }

 // ------------------------------------------------
    // PANEL NORTE - HABITACIONES + BOTÓN AGREGAR
    // ------------------------------------------------
    private void inicializarPanelHabitaciones() {

        JPanel panelSuperior = new JPanel(new BorderLayout());

        // Botón agregar habitación
        JButton btnAgregarHabitacion = new JButton(" + Agregar Habitación");
        btnAgregarHabitacion.setFocusPainted(false);

        btnAgregarHabitacion.addActionListener(e -> {
            DialogAgregarHabitacion dialog =
                    new DialogAgregarHabitacion();
            dialog.setVisible(true);
        });

        panelSuperior.add(btnAgregarHabitacion, BorderLayout.WEST);

        // Panel de habitaciones
        panelHabitaciones = new JPanel();
        panelHabitaciones.setLayout(
                new BoxLayout(panelHabitaciones, BoxLayout.Y_AXIS)
        );

        panelHabitaciones.setPreferredSize(null);

        panelHabitaciones.setBorder(
                BorderFactory.createTitledBorder("Habitaciones")
        );

        // Simulación de pisos y habitaciones
        crearPiso(panelHabitaciones, "1", 101);
        crearPiso(panelHabitaciones, "2", 201);
        crearPiso(panelHabitaciones, "3", 301);
        crearPiso(panelHabitaciones, "4", 401);
        crearPiso(panelHabitaciones, "5", 501);

        JScrollPane scroll = new JScrollPane(panelHabitaciones);
        scroll.setBorder(null);

        panelSuperior.add(scroll, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
    }

    
    // -------------------------------
    // CREA UN BLOQUE COMPLETO DE PISO
    // -------------------------------
    private void crearPiso(JPanel contenedor, String nombrePiso, int inicio) {

    	// Panel principal del piso
        JPanel panelPiso = new JPanel(new BorderLayout());
        panelPiso.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // =========================
        // ICONO
        // =========================
        String numero = nombrePiso.replaceAll("[^0-9]", "");
        String rutaIcono = "/com/adminHotel/gui/complements/P" + numero + ".png";

        JLabel lblIcono = new JLabel();
        lblIcono.setIcon(new ImageIcon(getClass().getResource(rutaIcono)));
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcono.setVerticalAlignment(SwingConstants.CENTER);
        
        JButton botonTemp = crearBotonHabitacion("000", "DISPONIBLE");
        int altoBoton = botonTemp.getPreferredSize().height;

        lblIcono.setPreferredSize(new Dimension(50, altoBoton + 20));

        panelPiso.add(lblIcono, BorderLayout.WEST);

        // =========================
        // BOTONES
        // =========================
        JPanel panelBotones = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 15, 10)
        );

        for (int i = 0; i < 8; i++) {
            int numeroHab = inicio + i;
            panelBotones.add(
                    crearBotonHabitacion(String.valueOf(numeroHab), "DISPONIBLE")
            );
        }

        panelPiso.add(panelBotones, BorderLayout.CENTER);

        panelPiso.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedor.add(panelPiso);
    }


    // ----------------------------------
    // CREACIÓN DEL BOTÓN DE HABITACIÓN
    // ----------------------------------
    private JButton crearBotonHabitacion(String numero, String estado) {

        JButton boton = new JButton(numero);

        boton.setPreferredSize(new Dimension(90, 70));
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);

        String rutaIcono;

        switch (estado) {

            case "OCUPADA":
                boton.setBackground(new Color(231, 76, 60));
                rutaIcono = "/com/adminHotel/gui/complements/ocupada.png";
                break;

            case "POR_LIMPIAR":
                boton.setBackground(new Color(243, 156, 18));
                rutaIcono = "/com/adminHotel/gui/complements/limpiar.png";
                break;

            default:
                boton.setBackground(new Color(46, 204, 113));
                rutaIcono = "/com/adminHotel/gui/complements/disponible.png";
                break;
        }

        boton.setIcon(new ImageIcon(getClass().getResource("")));

        // texto arriba, icono abajo
        boton.setHorizontalTextPosition(SwingConstants.CENTER);
        boton.setVerticalTextPosition(SwingConstants.BOTTOM);

        boton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Habitación seleccionada: " + numero,
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        return boton;
    }

    // -------------------------------
    // PANEL CENTRAL (RESERVADO)
    // -------------------------------
    private void inicializarPanelCentro() {

        panelCentro = new JPanel();
        panelCentro.setLayout(new BorderLayout());

        panelCentro.setBorder(
                BorderFactory.createTitledBorder("Detalle de Habitación")
        );

        JLabel lbl = new JLabel(
                "Seleccione una habitación para ver el detalle",
                SwingConstants.CENTER
        );

        lbl.setFont(new Font("Arial", Font.PLAIN, 16));

        panelCentro.add(lbl, BorderLayout.CENTER);

        add(panelCentro, BorderLayout.CENTER);
    }
}