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
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.adminHotel.dao.TurnoCajaDAO;
import com.adminHotel.dao.TurnoCajaDetalleDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.GUIToolkit;
import com.adminHotel.vo.TurnoCajaDetalleVO;

public class AdminInformesInternalFrame extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    private Font fuenteLabel  = new Font("Arial", Font.BOLD, 20);
    private Font fuenteNormal = new Font("Arial", Font.PLAIN, 18);
    private Font fuenteTitulo = new Font("Arial", Font.BOLD, 18);
    // Combo de fechas
    private JComboBox<String> comboCierres;
    // Tabla CAJA HOTEL
    private JTable tablaHotel;
    private DefaultTableModel modeloHotel;
    private JLabel lblSubtotalHotel;
    // Tabla CAJA MOSTRADOR
    private JTable tablaMostrador;
    private DefaultTableModel modeloMostrador;
    private JLabel lblSubtotalMostrador;
    // Total general
    private JLabel lblTotalGeneral;
    // ------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------
    public AdminInformesInternalFrame() {
        super("HOTEL LAS TERRAZAS II - Informes y Reportes", true, true, true, true);
        setLayout(new BorderLayout());
        SwingUtilities.invokeLater(() -> {
            JDesktopPane desktop = getDesktopPane();
            if (desktop != null) {
                setSize(desktop.getSize().width, desktop.getSize().height);
                setLocation(0, 0);
            }
        });
        initComponentes();
        cargarCierres();
    }
    // ------------------------------------------------
    // INICIALIZAR COMPONENTES
    // ------------------------------------------------
    private void initComponentes() {
        // ---- PANEL NORTE: Selector de Fecha ----
        JPanel pnlNorte = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
        pnlNorte.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                "Seleccionar Cierre de Caja",
                TitledBorder.LEFT, TitledBorder.TOP, fuenteTitulo));
        pnlNorte.setBackground(new Color(245, 245, 245));
        JLabel lblSelector = new JLabel("Fecha de Cierre:");
        lblSelector.setFont(fuenteLabel);
        comboCierres = new JComboBox<>();
        comboCierres.setFont(fuenteNormal);
        comboCierres.setPreferredSize(new Dimension(420, 38));
        comboCierres.addActionListener(e -> consultarCierre());
        JButton btnConsultar = new JButton("CONSULTAR");
        btnConsultar.setFont(fuenteLabel);
        btnConsultar.setBackground(new Color(41, 128, 185));
        btnConsultar.setForeground(Color.WHITE);
        btnConsultar.setFocusPainted(false);
        btnConsultar.addActionListener(e -> consultarCierre());
        pnlNorte.add(lblSelector);
        pnlNorte.add(comboCierres);
        pnlNorte.add(btnConsultar);
        add(pnlNorte, BorderLayout.NORTH);
        // ---- PANEL CAJA HOTEL ----
        JPanel pnlHotel = new JPanel(new BorderLayout());
        pnlHotel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
                "CAJA HOTEL",
                TitledBorder.LEFT, TitledBorder.TOP, fuenteTitulo));
        modeloHotel = crearModelo();
        tablaHotel  = crearTabla(modeloHotel);
        pnlHotel.add(new JScrollPane(tablaHotel), BorderLayout.CENTER);
        lblSubtotalHotel = new JLabel("  Subtotal Caja Hotel: --", JLabel.RIGHT);
        lblSubtotalHotel.setFont(new Font("Arial", Font.BOLD, 18));
        lblSubtotalHotel.setForeground(new Color(41, 128, 185));
        lblSubtotalHotel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 15));
        pnlHotel.add(lblSubtotalHotel, BorderLayout.SOUTH);
        // ---- PANEL CAJA MOSTRADOR ----
        JPanel pnlMostrador = new JPanel(new BorderLayout());
        pnlMostrador.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(39, 174, 96), 2),
                "CAJA MOSTRADOR",
                TitledBorder.LEFT, TitledBorder.TOP, fuenteTitulo));
        modeloMostrador = crearModelo();
        tablaMostrador  = crearTabla(modeloMostrador);
        pnlMostrador.add(new JScrollPane(tablaMostrador), BorderLayout.CENTER);
        lblSubtotalMostrador = new JLabel("  Subtotal Caja Mostrador: --", JLabel.RIGHT);
        lblSubtotalMostrador.setFont(new Font("Arial", Font.BOLD, 18));
        lblSubtotalMostrador.setForeground(new Color(39, 174, 96));
        lblSubtotalMostrador.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 15));
        pnlMostrador.add(lblSubtotalMostrador, BorderLayout.SOUTH);
        // ---- SPLIT PANE: Las dos tablas juntas ----
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlHotel, pnlMostrador);
        split.setResizeWeight(0.5); // Mitad para cada tabla
        split.setDividerSize(8);
        add(split, BorderLayout.CENTER);
        // ---- TOTAL GENERAL ----
        lblTotalGeneral = new JLabel("  TOTAL GENERAL AMBAS CAJAS: --", JLabel.RIGHT);
        lblTotalGeneral.setFont(new Font("Arial", Font.BOLD, 22));
        lblTotalGeneral.setForeground(new Color(192, 57, 43));
        lblTotalGeneral.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 20));
        add(lblTotalGeneral, BorderLayout.SOUTH);
    }
    // ------------------------------------------------
    // HELPER: crea el modelo de tabla estándar
    // ------------------------------------------------
    private DefaultTableModel crearModelo() {
        return new DefaultTableModel(
                new Object[]{"#", "CONCEPTO / DESCRIPCIÓN", "MÉTODO PAGO", "VALOR"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }
    // ------------------------------------------------
    // HELPER: crea la JTable estándar con estilos
    // ------------------------------------------------
    private JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(GUIToolkit.FONT_CELDAS);
        tabla.setRowHeight(30);
        tabla.getTableHeader().setFont(GUIToolkit.FONT_HEADER_CELDAS);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(420);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(110);
        return tabla;
    }
    // ------------------------------------------------
    // CARGAR FECHAS ÚNICAS EN EL COMBO
    // ------------------------------------------------
    private void cargarCierres() {
        comboCierres.removeAllItems();
        comboCierres.addItem("--- SELECCIONE FECHA ---");
        try (Connection cn = DBConnection.getConnection()) {
            TurnoCajaDAO dao = new TurnoCajaDAO(cn);
            List<String> fechas = dao.listarFechasCierreUnicas(); // "2026-04-16"
            for (String fechaConHora : fechas) {
                // fechaConHora trae "2026-04-16 14:30"
                String[] partes = fechaConHora.split(" ");
                String[] dp = partes[0].split("-");
                String fechaVisible = dp[2] + "/" + dp[1] + "/" + dp[0] + "  " + partes[1];
                // Visible: "16/04/2026  14:30"   |   Raw interno para la consulta: "2026-04-16"
                comboCierres.addItem(fechaVisible + " || " + partes[0]);
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Error al cargar los cierres:\n" + e.getMessage(),
                    "HOTEL LAS TERRAZAS II - Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
    // ------------------------------------------------
    // CONSULTAR LAS DOS CAJAS PARA LA FECHA SELECCIONADA
    // ------------------------------------------------
    private void consultarCierre() {
        String item = (String) comboCierres.getSelectedItem();
        if (item == null || !item.contains("||")) return;
        // Extraemos la fecha raw "yyyy-MM-dd" que va después del ||
        String fechaDia = item.contains("||") ? item.split("\\|\\|")[1].trim() : item;
        modeloHotel.setRowCount(0);
        modeloMostrador.setRowCount(0);
        BigDecimal totalHotel     = BigDecimal.ZERO;
        BigDecimal totalMostrador = BigDecimal.ZERO;
        try (Connection cn = DBConnection.getConnection()) {
            TurnoCajaDetalleDAO dao = new TurnoCajaDetalleDAO(cn);
            // --- CAJA HOTEL (id_caja = 1) ---
            List<TurnoCajaDetalleVO> ventasHotel = dao.listarVentasPorFechaYCaja(fechaDia, 1);
            int row = 1;
            for (TurnoCajaDetalleVO v : ventasHotel) {
                modeloHotel.addRow(new Object[]{
                    row++,
                    v.getNombreCaja(),         // CONCEPTO - Observación
                    v.getDescripcionMetodo(),  // EFECTIVO / NEQUI
                    "$ " + v.getTotalIngresos()
                });
                totalHotel = totalHotel.add(v.getTotalIngresos());
            }
            // --- CAJA MOSTRADOR (id_caja = 2) ---
            List<TurnoCajaDetalleVO> ventasMostrador = dao.listarVentasPorFechaYCaja(fechaDia, 2);
            row = 1;
            for (TurnoCajaDetalleVO v : ventasMostrador) {
                modeloMostrador.addRow(new Object[]{
                    row++,
                    v.getNombreCaja(),
                    v.getDescripcionMetodo(),
                    "$ " + v.getTotalIngresos()
                });
                totalMostrador = totalMostrador.add(v.getTotalIngresos());
            }
            // --- ACTUALIZAR SUBTOTALES Y TOTAL ---
            lblSubtotalHotel.setText("  Subtotal Caja Hotel: $ " + totalHotel + "   ");
            lblSubtotalMostrador.setText("  Subtotal Caja Mostrador: $ " + totalMostrador + "   ");
            lblTotalGeneral.setText("  TOTAL GENERAL AMBAS CAJAS: $ " +
                    totalHotel.add(totalMostrador) + "   ");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Error al consultar el cierre:\n" + e.getMessage(),
                    "HOTEL LAS TERRAZAS II - Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }
}