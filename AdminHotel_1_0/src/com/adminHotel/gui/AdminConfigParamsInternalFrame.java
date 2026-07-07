package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.adminHotel.dao.HabitacionDAO;
import com.adminHotel.dao.ParametroDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.GUIToolkit;
import com.adminHotel.vo.HabitacionVO;

public class AdminConfigParamsInternalFrame extends JInternalFrame {
	
    private static final long serialVersionUID = 1L;
    // ----- Fuentes estándar del sistema -----
    private Font fuenteTitulo  = new Font("Arial", Font.BOLD, 20);
    private Font fuenteLabel   = new Font("Arial", Font.BOLD, 20);
    private Font fuenteBoton   = new Font("Arial", Font.BOLD, 20);
    // ----- Pestaña 1: Parámetros -----
    private JTable tablaParametros;
    private DefaultTableModel modeloParametros;
    // ----- Pestaña 2: Habitaciones -----
    private JTable tablaHabitaciones;
    private DefaultTableModel modeloHabitaciones;
    
    // ================================================
    // CONSTRUCTOR
    // ================================================
    public AdminConfigParamsInternalFrame() {
        super("HOTEL LAS TERRAZAS II - Configuración y Parámetros", true, true, true, true);
        setLayout(new BorderLayout());
        SwingUtilities.invokeLater(() -> {
            JDesktopPane desktop = getDesktopPane();
            if (desktop != null) {
                setSize(desktop.getSize().width, desktop.getSize().height);
                setLocation(0, 0);
            }
        });
        initComponentes();
        cargarParametros();
        cargarHabitaciones();
    }
    
    // ================================================
    // INICIALIZAR COMPONENTES
    // ================================================
    private void initComponentes() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(fuenteLabel);
        // ---- PESTAÑA 1: PARÁMETROS DEL SISTEMA ----
        tabs.addTab("PRECIOS SISTEMA", crearPanelParametros());
        // ---- PESTAÑA 2: PRECIOS DE HABITACIONES ----
        tabs.addTab("PRECIOS HABITACIONES", crearPanelHabitaciones());
        add(tabs, BorderLayout.CENTER);
    }
    
    // ================================================
    // PANEL DE PARÁMETROS
    // ================================================
    private JPanel crearPanelParametros() {
    	
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // --- Título ---
        JLabel lblTitulo = new JLabel("Listado de Parámetros del Sistema");
        lblTitulo.setFont(fuenteTitulo);
        lblTitulo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                "Parámetros Configurables",
                TitledBorder.LEFT, TitledBorder.TOP, fuenteTitulo));
        panel.add(lblTitulo, BorderLayout.NORTH);
        // --- Tabla ---
        modeloParametros = new DefaultTableModel(
                new Object[]{"ID", "CLAVE", "VALOR ACTUAL", "DESCRIPCIÓN"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        tablaParametros = new JTable(modeloParametros);
        tablaParametros.setFont(GUIToolkit.FONT_CELDAS);
        tablaParametros.setRowHeight(30);
        tablaParametros.getTableHeader().setFont(GUIToolkit.FONT_HEADER_CELDAS);
        tablaParametros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaParametros.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaParametros.getColumnModel().getColumn(1).setPreferredWidth(260);
        tablaParametros.getColumnModel().getColumn(2).setPreferredWidth(140);
        tablaParametros.getColumnModel().getColumn(3).setPreferredWidth(360);
        JScrollPane scroll = new JScrollPane(tablaParametros);
        panel.add(scroll, BorderLayout.CENTER);
        
        // --- Botones ---
        JPanel pnlBotones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 5));
        pnlBotones.setOpaque(false);
        JButton btnEditar = new JButton("EDITAR VALOR");
        btnEditar.setFont(fuenteBoton);
        btnEditar.setBackground(new Color(41, 128, 185));
        btnEditar.setForeground(Color.BLACK);
        btnEditar.setFocusPainted(false);
        btnEditar.setPreferredSize(new Dimension(220, 40));
        btnEditar.addActionListener(e -> editarParametro());
        JButton btnRefrescar = new JButton("REFRESCAR");
        btnRefrescar.setFont(fuenteBoton);
        btnRefrescar.setBackground(new Color(100, 100, 100));
        btnRefrescar.setForeground(Color.BLACK);
        btnRefrescar.setFocusPainted(false);
        btnRefrescar.setPreferredSize(new Dimension(180, 40));
        btnRefrescar.addActionListener(e -> cargarParametros());
        pnlBotones.add(btnRefrescar);
        pnlBotones.add(btnEditar);
        panel.add(pnlBotones, BorderLayout.SOUTH);
        return panel;
    }
    
    // ================================================
    // PANEL DE HABITACIONES
    // ================================================
    private JPanel crearPanelHabitaciones() {
    	
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // --- Título ---
        JLabel lblTitulo = new JLabel("Configurar precios por habitación");
        lblTitulo.setFont(fuenteTitulo);
        lblTitulo.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                "Precios de Habitaciones",
                TitledBorder.LEFT, TitledBorder.TOP, fuenteTitulo));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // --- Tabla ---
        modeloHabitaciones = new DefaultTableModel(
                new Object[]{"ID", "HABITACION", "PISO", "TIPO", "PRECIO 1 PERSONA", "PRECIO 2 PERSONAS", "PRECIO 3 PERSONAS"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaHabitaciones = new JTable(modeloHabitaciones);
        tablaHabitaciones.setFont(GUIToolkit.FONT_CELDAS);
        tablaHabitaciones.setRowHeight(30);
        tablaHabitaciones.getTableHeader().setFont(GUIToolkit.FONT_HEADER_CELDAS);
        tablaHabitaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaHabitaciones.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaHabitaciones.getColumnModel().getColumn(1).setPreferredWidth(60);
        tablaHabitaciones.getColumnModel().getColumn(2).setPreferredWidth(60);
        tablaHabitaciones.getColumnModel().getColumn(3).setPreferredWidth(150);
        tablaHabitaciones.getColumnModel().getColumn(4).setPreferredWidth(160);
        tablaHabitaciones.getColumnModel().getColumn(5).setPreferredWidth(160);
        tablaHabitaciones.getColumnModel().getColumn(6).setPreferredWidth(160);
        JScrollPane scroll = new JScrollPane(tablaHabitaciones);
        panel.add(scroll, BorderLayout.CENTER);
        
        // --- Botones ---
        JPanel pnlBotones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 5));
        pnlBotones.setOpaque(false);
        JButton btnEditar = new JButton("EDITAR PRECIOS");
        btnEditar.setFont(fuenteBoton);
        btnEditar.setBackground(new Color(39, 174, 96));
        btnEditar.setForeground(Color.BLACK);
        btnEditar.setFocusPainted(false);
        btnEditar.setPreferredSize(new Dimension(220, 40));
        btnEditar.addActionListener(e -> editarPreciosHabitacion());
        JButton btnRefrescar = new JButton("REFRESCAR");
        btnRefrescar.setFont(fuenteBoton);
        btnRefrescar.setBackground(new Color(100, 100, 100));
        btnRefrescar.setForeground(Color.BLACK);
        btnRefrescar.setFocusPainted(false);
        btnRefrescar.setPreferredSize(new Dimension(180, 40));
        btnRefrescar.addActionListener(e -> cargarHabitaciones());
        pnlBotones.add(btnRefrescar);
        pnlBotones.add(btnEditar);
        panel.add(pnlBotones, BorderLayout.SOUTH);
        return panel;
    }
    
    // ================================================
    // CARGAR DATOS: PARÁMETROS
    // ================================================
    private void cargarParametros() {
    	
    	java.text.NumberFormat fmt = java.text.NumberFormat.getIntegerInstance(new java.util.Locale("es", "CO"));
    	
        modeloParametros.setRowCount(0);
        try (Connection cn = DBConnection.getConnection()) {
            ParametroDAO dao = new ParametroDAO(cn);
            List<String[]> lista = dao.listarTodos();
            for (String[] p : lista) {
            	modeloParametros.addRow(new Object[]{
            		    p[0],
            		    p[1],
            		    "$ " + fmt.format(new java.math.BigDecimal(p[2])),  // <-- con formato
            		    p[3]
            		});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar parámetros:\n" + e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ================================================
    // CARGAR DATOS: HABITACIONES
    // ================================================
    private void cargarHabitaciones() {
    	
    	java.text.NumberFormat fmt = java.text.NumberFormat.getIntegerInstance(new java.util.Locale("es", "CO"));
    	
        modeloHabitaciones.setRowCount(0);
        try (Connection cn = DBConnection.getConnection()) {
            HabitacionDAO dao = new HabitacionDAO(cn);
            List<HabitacionVO> lista = dao.listarHabitaciones();
            for (HabitacionVO h : lista) {
            	modeloHabitaciones.addRow(new Object[]{
            		    h.getIdHabitacion(),
            		    h.getNumeroHabitacion(),
            		    "Piso " + h.getPiso(),
            		    h.getTipoDescripcion(),
            		    "$ " + fmt.format(h.getPrecioSencilla()),
            		    "$ " + fmt.format(h.getPrecioDoble() != null ? h.getPrecioDoble() : java.math.BigDecimal.ZERO),
            		    "$ " + fmt.format(h.getPrecioTres()  != null ? h.getPrecioTres()  : java.math.BigDecimal.ZERO)
            		});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar habitaciones:\n" + e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ================================================
    // EDITAR: PARÁMETRO SELECCIONADO
    // ================================================
    private void editarParametro() {
        int fila = tablaParametros.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un parámetro de la lista.",
                "HOTEL LAS TERRAZAS II - Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int    id         = Integer.parseInt(modeloParametros.getValueAt(fila, 0).toString());
        String clave      = modeloParametros.getValueAt(fila, 1).toString();
        String valorActual = modeloParametros.getValueAt(fila, 2).toString().replace("$ ", "");
        javax.swing.JTextField txtNuevoValor = new javax.swing.JTextField(valorActual);
        //txtNuevoValor.setFont(fuenteLabel);
        JLabel lblClave     = new JLabel("Parámetro:  " + clave);
        JLabel lblNuevoValor = new JLabel("Nuevo valor ($):");
        lblClave.setFont(fuenteLabel);
        lblNuevoValor.setFont(fuenteLabel);
        Object[] campos = { lblClave, lblNuevoValor, txtNuevoValor };
        
        int op = JOptionPane.showConfirmDialog(this, campos,
            "HOTEL LAS TERRAZAS II - Editar Parámetro",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return;
        String nuevoValor = txtNuevoValor.getText().trim();
        if (nuevoValor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El valor no puede estar vacío.",
                "HOTEL LAS TERRAZAS II - Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            // Validar que sea número
            new BigDecimal(nuevoValor);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "El valor debe ser numérico (Ej: 35000 ó 35000.00).",
                "HOTEL LAS TERRAZAS II - Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try (Connection cn = DBConnection.getConnection()) {
            ParametroDAO dao = new ParametroDAO(cn);
            dao.actualizarValor(id, nuevoValor);
            JOptionPane.showMessageDialog(this,
                "Parámetro '" + clave + "' actualizado a: $ " + nuevoValor,
                "HOTEL LAS TERRAZAS II - Guardado", JOptionPane.INFORMATION_MESSAGE);
            cargarParametros();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar el parámetro:\n" + e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // ================================================
    // EDITAR: PRECIOS DE HABITACIÓN SELECCIONADA
    // ================================================
    private void editarPreciosHabitacion() {
        int fila = tablaHabitaciones.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione una habitación de la lista.",
                "HOTEL LAS TERRAZAS II - Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int    id     = Integer.parseInt(modeloHabitaciones.getValueAt(fila, 0).toString());
        int    nro    = Integer.parseInt(modeloHabitaciones.getValueAt(fila, 1).toString());
        String tipo   = modeloHabitaciones.getValueAt(fila, 3).toString();
        String pSenc  = modeloHabitaciones.getValueAt(fila, 4).toString().replace("$ ", "").replace(".", "");
        String pDoble = modeloHabitaciones.getValueAt(fila, 5).toString().replace("$ ", "").replace(".", "");
        String pTres  = modeloHabitaciones.getValueAt(fila, 6).toString().replace("$ ", "").replace(".", "");
        javax.swing.JTextField txtSencilla = new javax.swing.JTextField(pSenc);
        javax.swing.JTextField txtDoble    = new javax.swing.JTextField(pDoble);
        javax.swing.JTextField txtTres     = new javax.swing.JTextField(pTres);
        txtSencilla.setFont(fuenteLabel);
        txtDoble.setFont(fuenteLabel);
        txtTres.setFont(fuenteLabel);
        
        JLabel lblTitulo  = new JLabel("Habitación Nro. " + nro + "  |  Tipo: " + tipo);
        JLabel lblSenc    = new JLabel("Precio 1 PERSONA ($):");
        JLabel lblDoble   = new JLabel("Precio 2 PERSONAS ($):");
        JLabel lblTres    = new JLabel("Precio 3 PERSONAS ($):");
        lblTitulo.setFont(fuenteLabel);
        lblSenc.setFont(fuenteLabel);
        lblDoble.setFont(fuenteLabel);
        lblTres.setFont(fuenteLabel);
        Object[] campos = { lblTitulo, lblSenc, txtSencilla, lblDoble, txtDoble, lblTres, txtTres };
        
        int op = JOptionPane.showConfirmDialog(this, campos,
            "HOTEL LAS TERRAZAS II - Editar Precios",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return;
        try {
            // Si la recepcionista se le escapa un punto por costumbre (Ej: 45.000), se lo neutralizamos
            BigDecimal nuevaSencilla = new BigDecimal(txtSencilla.getText().trim().replace(".", ""));
            BigDecimal nuevaDoble    = new BigDecimal(txtDoble.getText().trim().replace(".", ""));
            BigDecimal nuevaTres     = new BigDecimal(txtTres.getText().trim().replace(".", ""));
            try (Connection cn = DBConnection.getConnection()) {
                HabitacionDAO dao = new HabitacionDAO(cn);
                
                // ¡Magia! Usamos el nuevo método quirúrgico en vez del genérico que fallaba
                dao.actualizarPrecios(id, nuevaSencilla, nuevaDoble, nuevaTres);
                
                JOptionPane.showMessageDialog(this,
                    "Precios de la habitación " + nro + " actualizados correctamente.",
                    "HOTEL LAS TERRAZAS II - Guardado", JOptionPane.INFORMATION_MESSAGE);
                cargarHabitaciones();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Todos los precios deben ser numéricos (Ej: 45000 ó 45000.00).",
                "HOTEL LAS TERRAZAS II - Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar los precios:\n" + e.getMessage(),
                "HOTEL LAS TERRAZAS II - Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}