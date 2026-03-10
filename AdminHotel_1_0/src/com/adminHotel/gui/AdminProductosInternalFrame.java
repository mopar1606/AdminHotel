package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.adminHotel.dao.ProductoDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.vo.ProductoVO;

public class AdminProductosInternalFrame extends JInternalFrame {
	
	private static final long serialVersionUID = 4461285326950607457L;
	
	private JTextField txtNombre, txtPrecioCompra, txtPrecioVenta, txtStock, txtCodigoBarras;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JButton btnGuardar, btnActualizar, btnLimpiar, btnSurtir;
    private Integer idSeleccionado = null;
    private Font fuenteLabel = new Font("Arial", Font.BOLD, 20);
    private Font fuenteInput = new Font("Arial", Font.PLAIN, 20);
    
    public AdminProductosInternalFrame() {
    	super("HOTEL LAS TERRAZAS II - Administración de Productos - Inventario", true, true, true, true);    	
    	
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

		initSeccionSuperior();
        initSeccionTabla();
        cargarDatos();
    }

    private void initSeccionSuperior() {
    	// 1. PRIMERO: Inicializar todos los objetos (Para evitar el NullPointerException)
        txtNombre = new JTextField();
        txtPrecioCompra = new JTextField();
        txtPrecioVenta = new JTextField();
        txtStock = new JTextField();
        txtCodigoBarras = new JTextField();
        
        // Aplicar fuente a los inputs
        txtNombre.setFont(fuenteInput);
        txtPrecioCompra.setFont(fuenteInput);
        txtPrecioVenta.setFont(fuenteInput);
        txtStock.setFont(fuenteInput);
        txtCodigoBarras.setFont(fuenteInput);
        // Inicializar botones con texto y estilo
        btnGuardar = new JButton("GUARDAR");
        btnActualizar = new JButton("ACTUALIZAR");
        btnLimpiar = new JButton("LIMPIAR");
        btnSurtir = new JButton("SURTIR STOCK");
        btnGuardar.setFont(fuenteLabel);
        btnGuardar.setBackground(new java.awt.Color(39, 174, 96));
        btnGuardar.setForeground(java.awt.Color.WHITE);
        btnActualizar.setFont(fuenteLabel);
        btnActualizar.setBackground(new java.awt.Color(41, 128, 185));
        btnActualizar.setForeground(java.awt.Color.WHITE);
        btnLimpiar.setFont(fuenteLabel);
        btnLimpiar.setBackground(new java.awt.Color(149, 165, 166));
        btnLimpiar.setForeground(java.awt.Color.WHITE);
        btnSurtir.setFont(fuenteLabel);
        btnSurtir.setBackground(new java.awt.Color(211, 84, 0));
        btnSurtir.setForeground(java.awt.Color.WHITE);
        // 2. SEGUNDO: Armar el diseño
        JPanel pnlNorte = new JPanel(new BorderLayout());
        JPanel pnlCampos = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        
        pnlCampos.setBorder(BorderFactory.createTitledBorder(
            new javax.swing.border.LineBorder(Color.BLUE, 1), "Datos del Producto", 
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, fuenteLabel));
        pnlCampos.setBackground(Color.WHITE);
        gbc.insets = new java.awt.Insets(10, 15, 10, 15);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        // FILA 1: CÓDIGO Y NOMBRE
        gbc.gridy = 0; 
        gbc.gridx = 0; gbc.weightx = 0; pnlCampos.add(crearLabel("CÓDIGO:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3; txtCodigoBarras.setPreferredSize(new Dimension(150, 35)); pnlCampos.add(txtCodigoBarras, gbc);
        gbc.gridx = 2; gbc.weightx = 0; pnlCampos.add(crearLabel("NOMBRE:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.7; pnlCampos.add(txtNombre, gbc);
        // FILA 2: PRECIOS
        gbc.gridy = 1;
        gbc.gridx = 0; gbc.weightx = 0; pnlCampos.add(crearLabel("P. COMPRA:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3; pnlCampos.add(txtPrecioCompra, gbc);
        gbc.gridx = 2; gbc.weightx = 0; pnlCampos.add(crearLabel("P. VENTA:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.7; pnlCampos.add(txtPrecioVenta, gbc);
        // FILA 3: STOCK
        gbc.gridy = 2;
        gbc.gridx = 0; gbc.weightx = 0; pnlCampos.add(crearLabel("STOCK:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3; pnlCampos.add(txtStock, gbc);
        // Panel de botones
        JPanel pnlAcciones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 10));
        pnlAcciones.add(btnGuardar);
        pnlAcciones.add(btnActualizar);
        pnlAcciones.add(btnLimpiar);
        pnlAcciones.add(btnSurtir);
        pnlNorte.add(pnlCampos, BorderLayout.CENTER);
        pnlNorte.add(pnlAcciones, BorderLayout.SOUTH);
        add(pnlNorte, BorderLayout.NORTH);
        // 3. EVENTOS
        btnGuardar.addActionListener(e -> guardarProducto());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnActualizar.addActionListener(e -> actualizarProducto());
        btnSurtir.addActionListener(e -> sumarStockRapido());
    }

    private void initSeccionTabla() {
        // Panel Contenedor de la Tabla con Borde de Línea y Título
        JPanel pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(Color.BLUE, 1), "Listado de Productos en Inventario", TitledBorder.LEFT, TitledBorder.TOP, fuenteLabel));

        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre", "P. Compra", "P. Venta", "Stock", "Cod. Barras"}, 0);
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setFont(new Font("Arial", Font.PLAIN, 16));
        tablaProductos.setRowHeight(30); // Filas más altas para tocar fácil
        
        // Selección de fila
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila != -1) {
                idSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
                txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
                txtPrecioCompra.setText(modeloTabla.getValueAt(fila, 2).toString());
                txtPrecioVenta.setText(modeloTabla.getValueAt(fila, 3).toString());
                txtStock.setText(modeloTabla.getValueAt(fila, 4).toString());
                txtCodigoBarras.setText(modeloTabla.getValueAt(fila, 5) != null ? modeloTabla.getValueAt(fila, 5).toString() : "");
                txtStock.setEditable(false); // Bloqueado en edición por auditoría
            }
        });

        JScrollPane scroll = new JScrollPane(tablaProductos);
        pnlTabla.add(scroll, BorderLayout.CENTER);

        add(pnlTabla, BorderLayout.CENTER);
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(fuenteLabel);
        return lbl;
    }

    // Lógica para conectar con el DAO
    private void cargarDatos() {
        modeloTabla.setRowCount(0);
        try (Connection cn = DBConnection.getConnection()) {
            ProductoDAO dao = new ProductoDAO(cn);
            List<ProductoVO> lista = dao.listarTodo();
            for (ProductoVO p : lista) {
                modeloTabla.addRow(new Object[]{
                    p.getIdProducto(), p.getNombre(), p.getPrecioCompra(), p.getPrecioVenta(), p.getStock(), p.getCodigoBarras()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage());
        }
    }

    private void guardarProducto() {
    	
    	if(validarCampos())
    	{
    		try (Connection cn = DBConnection.getConnection()) {
                ProductoDAO dao = new ProductoDAO(cn);
                ProductoVO p = new ProductoVO();
                p.setNombre(txtNombre.getText());
                p.setPrecioCompra(new BigDecimal(txtPrecioCompra.getText()));
                p.setPrecioVenta(new BigDecimal(txtPrecioVenta.getText()));
                p.setCodigoBarras(txtCodigoBarras.getText().trim().isEmpty() ? null : txtCodigoBarras.getText().trim());
                p.setStock(Integer.parseInt(txtStock.getText()));
                p.setIdEstadoRegistro(1);

                dao.insertar(p);
                JOptionPane.showMessageDialog(this, "Producto guardado con éxito");
                limpiarFormulario();
                cargarDatos();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
    	}
    }
    
    private void actualizarProducto() {
        if (idSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un producto de la tabla primero.");
            return;
        }
        
        // Validar campos antes de intentar actualizar
        String nombre     = txtNombre.getText().trim();
        String sCompra    = txtPrecioCompra.getText().trim();
        String sVenta     = txtPrecioVenta.getText().trim();
        
        if (nombre.isEmpty() || sVenta.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Nombre y Precio de Venta son obligatorios.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ProductoVO p = new ProductoVO();
            p.setIdProducto(idSeleccionado);
            p.setNombre(nombre);
            p.setPrecioCompra(sCompra.isEmpty() ? BigDecimal.ZERO : new BigDecimal(sCompra));
            p.setPrecioVenta(new BigDecimal(sVenta));
            p.setCodigoBarras(txtCodigoBarras.getText().trim().isEmpty() ? null : txtCodigoBarras.getText().trim());
            p.setIdEstadoRegistro(1);
            try (Connection cn = DBConnection.getConnection()) {
                ProductoDAO dao = new ProductoDAO(cn);
                dao.actualizar(p);
            }
            JOptionPane.showMessageDialog(this,
                "Producto actualizado correctamente.",
                "Exito", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
            cargarDatos(); // Siempre se ejecuta si no hubo excepcion
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Los precios deben ser numeros validos (use punto para decimales).",
                "Error de formato", JOptionPane.ERROR_MESSAGE);
            // NO limpiar para que el usuario corrija
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al actualizar: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminarProducto() {
        if (idSeleccionado == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de eliminar este producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection cn = DBConnection.getConnection()) {
                // Podrías crear un método en el DAO llamado cambiarEstado(id, estado)
                // O simplemente usar el actualizar mandando el estado 3 (ELIMINADO)
                String sql = "UPDATE producto SET id_estado_registro = 3 WHERE id_producto = ?";
                try (java.sql.PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, idSeleccionado);
                    ps.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "Producto eliminado.");
                limpiarFormulario();
                cargarDatos();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private void sumarStockRapido() {
    	
    	if (idSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla primero.");
            return;
        }
    	
    	String cant = JOptionPane.showInputDialog(this, "Cantidad que ingresa al inventario:");
        
        if (cant != null && !cant.isEmpty()) {
            try {
                int cantidad = Integer.parseInt(cant);
                
                try (Connection cn = DBConnection.getConnection()) {
                    ProductoDAO dao = new ProductoDAO(cn);
                    boolean exito = dao.sumarStock(idSeleccionado, cantidad);
                    
                    if (exito) {
                        JOptionPane.showMessageDialog(this, "Stock actualizado con éxito.");
                        
                        // --- EL AJUSTE CLAVE ---
                        limpiarFormulario(); // Limpia los campos de texto e idSeleccionado
                        cargarDatos();      // Recarga la tabla con los nuevos valores de la BD
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un número entero válido.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }    	
    }
    
    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() || txtPrecioVenta.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y Precio de Venta son obligatorios.");
            return false;
        }
        try {
            new BigDecimal(txtPrecioCompra.getText());
            new BigDecimal(txtPrecioVenta.getText());
            if (idSeleccionado == null) Integer.parseInt(txtStock.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Los precios y el stock deben ser números válidos (use punto para decimales).");
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtPrecioCompra.setText("");
        txtPrecioVenta.setText("");
        txtStock.setText("");
        txtStock.setEditable(true);
        idSeleccionado = null;
        tablaProductos.clearSelection();
        txtCodigoBarras.setText("");
    }
}
