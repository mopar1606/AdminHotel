package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import com.adminHotel.dao.ProductoDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.GUIToolkit;
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
    
    // ------------------------------------------------
 	// INSTANCIA INTERNAL FRAME ADMINISTRACION INVENTARIO
 	// ------------------------------------------------
    public AdminProductosInternalFrame() {
    	super("HOTEL LAS TERRAZAS II - Administraci\\u00f3n de Productos - Inventario", true, true, true, true);    	
    	
    	setLayout(new BorderLayout());

    	SwingUtilities.invokeLater(() -> {
    	    JDesktopPane desktop = getDesktopPane();
    	    if (desktop != null) {
    	        
    	    	Dimension size = desktop.getSize();    	        
    	        setSize(size.width, size.height);    	        
    	        setLocation(0, 0);
    	    }
    	});

		initSeccionSuperior();
        initSeccionTabla();
        cargarDatos();
    }

    // ------------------------------------------------
 	// PANEL SUPERIOR - CONTROLES DE INVENTARIO
 	// ------------------------------------------------
    private void initSeccionSuperior() {
    	// 1. PRIMERO: Inicializar todos los objetos (Para evitar el NullPointerException)
        txtNombre = new JTextField();
        txtPrecioCompra = new JTextField();
        txtPrecioVenta = new JTextField();
        txtStock = new JTextField();
        txtCodigoBarras = new JTextField();
        txtCodigoBarras.addActionListener(e -> buscarPorCodigoBarras());
        
        // Aplicar fuente a los inputs
        txtNombre.setFont(fuenteInput);
        
        // =============================================
        // AUTOCOMPLETADO UPPERCASE EN txtNombre
        // =============================================
        JPopupMenu popupNombre = new JPopupMenu();
        txtNombre.getDocument().addDocumentListener(new DocumentListener() {
            private void actualizarSugerencias() {
                // Forzar UPPERCASE mientras escribe
                String original = txtNombre.getText();
                String mayus = original.toUpperCase();
                if (!original.equals(mayus)) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        int pos = txtNombre.getCaretPosition();
                        txtNombre.getDocument().removeDocumentListener(this);
                        txtNombre.setText(mayus);
                        txtNombre.setCaretPosition(Math.min(pos, mayus.length()));
                        txtNombre.getDocument().addDocumentListener(this);
                    });
                    return;
                }
                String filtro = mayus.trim();
                popupNombre.setVisible(false);
                popupNombre.removeAll();
                if (filtro.length() < 2) return; // Mínimo 2 letras para buscar
                // Si ya hay un producto seleccionado (idSeleccionado != null), no sugerir
                if (idSeleccionado != null) return;
                try (Connection cnAuto = DBConnection.getConnection()) {
                    ProductoDAO daoAuto = new ProductoDAO(cnAuto);
                    List<ProductoVO> todos = daoAuto.listarActivos();
                    List<ProductoVO> coincidencias = new ArrayList<>();
                    for (ProductoVO p : todos) {
                        if (p.getNombre().toUpperCase().contains(filtro)) {
                            coincidencias.add(p);
                        }
                    }
                    if (coincidencias.isEmpty()) return;
                    for (ProductoVO p : coincidencias) {
                        String etiqueta = p.getNombre().toUpperCase()
                            + "  |  Venta: $" + p.getPrecioVenta()
                            + "  |  Stock: " + p.getStock();
                        JMenuItem item = new JMenuItem(etiqueta);
                        item.setFont(new Font("Arial", Font.BOLD, 16));
                        item.addActionListener(ev -> {
                            popupNombre.setVisible(false);
                            // Rellenar formulario completo como si hubiera clic en la tabla
                            idSeleccionado = p.getIdProducto();
                            txtNombre.getDocument().removeDocumentListener(this);
                            txtNombre.setText(p.getNombre().toUpperCase());
                            txtNombre.getDocument().addDocumentListener(this);
                            txtPrecioCompra.setText(p.getPrecioCompra() != null ? p.getPrecioCompra().toPlainString() : "");
                            txtPrecioVenta.setText(p.getPrecioVenta() != null ? p.getPrecioVenta().toPlainString() : "");
                            txtStock.setText(String.valueOf(p.getStock()));
                            txtCodigoBarras.setText(p.getCodigoBarras() != null ? p.getCodigoBarras() : "");
                            txtStock.setEditable(false); // Bloqueado como al seleccionar de tabla
                            // Resaltar en la tabla también
                            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                                if (modeloTabla.getValueAt(i, 0).equals(idSeleccionado)) {
                                    tablaProductos.setRowSelectionInterval(i, i);
                                    tablaProductos.scrollRectToVisible(tablaProductos.getCellRect(i, 0, true));
                                    break;
                                }
                            }
                        });
                        popupNombre.add(item);
                    }
                    popupNombre.show(txtNombre, 0, txtNombre.getHeight());
                    txtNombre.requestFocus();
                } catch (Exception ex) {
                    // Si falla la BD durante la búsqueda, simplemente no muestra sugerencias
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { actualizarSugerencias(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizarSugerencias(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizarSugerencias(); }
        });
        txtNombre.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupNombre.setVisible(false);
                }
            }
        });
        
        txtPrecioCompra.setFont(fuenteInput);
        txtPrecioVenta.setFont(fuenteInput);
        txtStock.setFont(fuenteInput);
        txtCodigoBarras.setFont(fuenteInput);
        // Inicializar botones con texto y estilo
        btnGuardar = new JButton("GUARDAR");
        btnActualizar = new JButton("ACTUALIZAR");
        btnLimpiar = new JButton("LIMPIAR");
        btnSurtir = new JButton("SURTIR CANTIDAD");
        btnGuardar.setFont(fuenteLabel);
        btnGuardar.setBackground(new java.awt.Color(39, 174, 96));
        btnGuardar.setForeground(java.awt.Color.BLACK);
        btnActualizar.setFont(fuenteLabel);
        btnActualizar.setBackground(new java.awt.Color(41, 128, 185));
        btnActualizar.setForeground(java.awt.Color.BLACK);
        btnLimpiar.setFont(fuenteLabel);
        btnLimpiar.setBackground(new java.awt.Color(149, 165, 166));
        btnLimpiar.setForeground(java.awt.Color.BLACK);
        btnSurtir.setFont(fuenteLabel);
        btnSurtir.setBackground(new java.awt.Color(211, 84, 0));
        btnSurtir.setForeground(java.awt.Color.BLACK);
        // 2. SEGUNDO: Armar el dise�o
        JPanel pnlNorte = new JPanel(new BorderLayout());
        JPanel pnlCampos = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        
        pnlCampos.setBorder(BorderFactory.createTitledBorder(
            new javax.swing.border.LineBorder(Color.BLUE, 1), "Datos del Producto", 
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, fuenteLabel));
        pnlCampos.setBackground(Color.WHITE);
        gbc.insets = new java.awt.Insets(10, 15, 10, 15);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        // FILA 1: C�DIGO Y NOMBRE
        gbc.gridy = 0; 
        gbc.gridx = 0; gbc.weightx = 0; pnlCampos.add(crearLabel("CODIGO BAR.:"), gbc);
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
        gbc.gridx = 0; gbc.weightx = 0; pnlCampos.add(crearLabel("CANTIDAD:"), gbc);
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
    
    // ------------------------------------------------
   	// CONSTRUCTOR ETIQUETAS
   	// ------------------------------------------------
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(fuenteLabel);
        return lbl;
    }

    // ------------------------------------------------
  	// PANEL CENTRAL - LISTA DE INVENTARIO
  	// ------------------------------------------------
    @SuppressWarnings("serial")
	private void initSeccionTabla() {
        // Panel Contenedor de la Tabla con Borde de L�nea y Titulo
        JPanel pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(Color.BLUE, 1), "Listado de Productos en Inventario", TitledBorder.LEFT, TitledBorder.TOP, fuenteLabel));

        modeloTabla = new DefaultTableModel(new Object[]{"ID", "NOMBRE", "P. COMPRA", "P. VENTA", "CANTIDAD", "COD. BARRAS"}, 0) {
        	@Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setFont(GUIToolkit.FONT_CELDAS);
        tablaProductos.setRowHeight(30);
		tablaProductos.getTableHeader().setFont(GUIToolkit.FONT_HEADER_CELDAS);
        
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            int fila = tablaProductos.getSelectedRow();
            if (fila != -1) {
                idSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
                txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
                txtPrecioCompra.setText(modeloTabla.getValueAt(fila, 2).toString());
                txtPrecioVenta.setText(modeloTabla.getValueAt(fila, 3).toString());
                txtStock.setText(modeloTabla.getValueAt(fila, 4).toString());
                txtCodigoBarras.setText(modeloTabla.getValueAt(fila, 5) != null ? modeloTabla.getValueAt(fila, 5).toString() : "");
                txtStock.setEditable(false); // Bloqueado en edici�n por auditor�a
            }
        });

        JScrollPane scroll = new JScrollPane(tablaProductos);
        pnlTabla.add(scroll, BorderLayout.CENTER);

        add(pnlTabla, BorderLayout.CENTER);
    }
    
    // ------------------------------------------------
   	// CARGA LISTA DE INVENTARIO
   	// ------------------------------------------------
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

    // ------------------------------------------------
   	// GUARDAR NUEVO PRODUCTO
   	// ------------------------------------------------
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
                JOptionPane.showMessageDialog(this, "Producto guardado con �xito");
                limpiarFormulario();
                cargarDatos();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
    	}
    }
    
    // ------------------------------------------------
   	// ACTUALIZAR PRODUCTO
   	// ------------------------------------------------
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
    
    // ------------------------------------------------
   	// SUPRIMIR PRODUCTO
   	// ------------------------------------------------
    private void eliminarProducto() {
        if (idSeleccionado == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
            "�Est� seguro de eliminar este producto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection cn = DBConnection.getConnection()) {
                // Podr�as crear un m�todo en el DAO llamado cambiarEstado(id, estado)
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
    
    // ------------------------------------------------
   	// ACTUALIZAR CANTIDAD
   	// ------------------------------------------------
    private void sumarStockRapido() {
    	
    	if (idSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla primero.");
            return;
        }
        // Mensaje mejorado para explicar c�mo sumar o restar
        String mensaje = "Ajuste de Inventario:\n\n" +
                         "� Ingrese n�meros POSITIVOS para SURTIR (ej: 10)\n" +
                         "� Ingrese n�meros NEGATIVOS para CORREGIR (ej: -1)\n\n" +
                         "Cantidad a ajustar:";
        
        String cant = JOptionPane.showInputDialog(this, mensaje, "0");
        if (cant != null && !cant.isEmpty()) {
            try {
                int cantidad = Integer.parseInt(cant);
                try (Connection cn = DBConnection.getConnection()) {
                    ProductoDAO dao = new ProductoDAO(cn);
                    
                    // Intentamos realizar el ajuste
                    boolean exito = dao.sumarStock(idSeleccionado, cantidad);
                    if (exito) {
                        JOptionPane.showMessageDialog(this, "Stock ajustado correctamente.");
                        limpiarFormulario();
                        cargarDatos();
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un número entero válido.");
            } catch (Exception e) {
                // Aqu� atrapar� el error de la base de datos si el stock intentara quedar en negativo
                String errorMsg = e.getMessage();
                if (errorMsg.contains("chk_producto_stock")) {
                    JOptionPane.showMessageDialog(this, "Error: No puede restar más cantidad de la que existe en stock.");
                } else {
                    JOptionPane.showMessageDialog(this, "Error al ajustar: " + errorMsg);
                }
            }
        }
    }
    
    // ------------------------------------------------
   	// VALIDAR CAMPOS VACIOS
   	// ------------------------------------------------    
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

    // ------------------------------------------------
   	// CONTROL DE REFRESH
   	// ------------------------------------------------
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
    
	 // -----------------------------------------------
	 // BUSCAR PRODUCTO POR CÓDIGO DE BARRAS (PISTOLA)
	 // Se dispara cuando la pistola envía el ENTER
	 // -----------------------------------------------
	 private void buscarPorCodigoBarras() {
	     String codigo = txtCodigoBarras.getText().trim();
	     
	     if (codigo.isEmpty()) return;
	     try (Connection cn2 = DBConnection.getConnection()) {
	         ProductoDAO dao = new ProductoDAO(cn2);
	         ProductoVO producto = dao.buscarPorCodigo(codigo);
	         if (producto != null) {
	        	 
	             // Producto encontrado: llenar todos los campos del formulario
	             idSeleccionado = producto.getIdProducto();
	             txtNombre.setText(producto.getNombre());
	             txtPrecioCompra.setText(producto.getPrecioCompra() != null ? producto.getPrecioCompra().toPlainString() : "");
	             txtPrecioVenta.setText(producto.getPrecioVenta() != null ? producto.getPrecioVenta().toPlainString() : "");
	             txtStock.setText(String.valueOf(producto.getStock()));
	             txtStock.setEditable(false); // Bloqueado igual que al seleccionar desde la tabla
	             
	             // Resaltar la fila correspondiente en la tabla
	             for (int i = 0; i < modeloTabla.getRowCount(); i++) {
	                 if (modeloTabla.getValueAt(i, 0).equals(idSeleccionado)) {
	                     tablaProductos.setRowSelectionInterval(i, i);
	                     tablaProductos.scrollRectToVisible(tablaProductos.getCellRect(i, 0, true));
	                     break;
	                 }
	             }
	         }
	         // Si no existe: no hace nada (el código permanece en el campo para referencia visual)
	     } catch (Exception ex) {
	         JOptionPane.showMessageDialog(this,
	             "Error al buscar producto:\nMotivo: " + ex.getMessage(),
	             "HOTEL LAS TERRAZAS II - Error",
	             JOptionPane.ERROR_MESSAGE);
	     }
	 }
}
