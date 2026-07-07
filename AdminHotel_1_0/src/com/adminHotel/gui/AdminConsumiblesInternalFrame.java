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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.adminHotel.dao.ConsumibleDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.GUIToolkit;
import com.adminHotel.vo.ConsumibleVO;

public class AdminConsumiblesInternalFrame extends JInternalFrame {

    private static final long serialVersionUID = -8142187112486979339L;
	private JTextField txtNombre, txtPrecioCompra, txtStock, txtFechaCompra, txtPrecioMulta;
	private javax.swing.JCheckBox chkPrestable;
    private JTable tablaConsumibles;
    private DefaultTableModel modeloTabla;
    private JButton btnGuardar, btnActualizar, btnLimpiar, btnStock;
    private Integer idSeleccionado = null;
    private Font fuenteLabel = new Font("Arial", Font.BOLD, 20);
    private Font fuenteInput = new Font("Arial", Font.PLAIN, 20);

    public AdminConsumiblesInternalFrame() {
        super("HOTEL LAS TERRAZAS II - Administraci�n de Consumibles - Inventario", true, true, true, true);
        
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
        cargarDatos();        
    }
    
    // ------------------------------------------------
  	// PANEL SUPERIOR - CONTROLES DE INVENTARIO
  	// ------------------------------------------------
     @SuppressWarnings("serial")
     private void initSeccionSuperior() {
         
         txtNombre = new JTextField();
         txtPrecioCompra = new JTextField();
         txtPrecioMulta = new JTextField();
         txtFechaCompra = new JTextField();
         txtStock = new JTextField();
         
         chkPrestable = new javax.swing.JCheckBox("Es Prestable en Check-In");
         chkPrestable.setFont(fuenteInput);
         chkPrestable.setBackground(Color.WHITE);
         chkPrestable.setSelected(false);
         
         txtNombre.setFont(fuenteInput);
         txtPrecioCompra.setFont(fuenteInput);
         txtPrecioMulta.setFont(fuenteInput);
         txtFechaCompra.setFont(fuenteInput);
         txtStock.setFont(fuenteInput);
         
         JPanel pnlNorte = new JPanel(new BorderLayout());
         JPanel pnlCampos = new JPanel(new java.awt.GridBagLayout());
         java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
         
         pnlCampos.setBorder(BorderFactory.createTitledBorder(
             new javax.swing.border.LineBorder(Color.BLUE, 1), "Datos del Producto", 
             javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, fuenteLabel));
         pnlCampos.setBackground(Color.WHITE);
         gbc.insets = new java.awt.Insets(10, 10, 10, 10);
         gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
         
         // FILA 1
         gbc.gridy = 0; 
         gbc.gridx = 0; gbc.weightx = 0; pnlCampos.add(crearLabel("NOMBRE:"), gbc);
         gbc.gridx = 1; gbc.weightx = 0.4; pnlCampos.add(txtNombre, gbc);
         
         gbc.gridx = 2; gbc.weightx = 0; pnlCampos.add(crearLabel("P. COMPRA:"), gbc);
         gbc.gridx = 3; gbc.weightx = 0.2; pnlCampos.add(txtPrecioCompra, gbc);
         
         gbc.gridx = 4; gbc.weightx = 0; pnlCampos.add(crearLabel("P. MULTA:"), gbc);
         gbc.gridx = 5; gbc.weightx = 0.2; pnlCampos.add(txtPrecioMulta, gbc);
         
         // FILA 2
         gbc.gridy = 1;
         gbc.gridx = 0; gbc.weightx = 0; pnlCampos.add(crearLabel("FECHA COMPRA (AAAA-MM-DD):"), gbc);
         gbc.gridx = 1; gbc.weightx = 0.4; pnlCampos.add(txtFechaCompra, gbc);
         
         gbc.gridx = 2; gbc.weightx = 0; pnlCampos.add(crearLabel("CANTIDAD (Libres):"), gbc);
         gbc.gridx = 3; gbc.weightx = 0.2; pnlCampos.add(txtStock, gbc); 
         
         gbc.gridx = 4; gbc.weightx = 0; pnlCampos.add(crearLabel("TIPO:"), gbc);
         gbc.gridx = 5; gbc.weightx = 0.2; pnlCampos.add(chkPrestable, gbc);
         
         JPanel panelBotones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 10));
         
         btnGuardar = new JButton("GUARDAR");
         btnGuardar.setFont(fuenteLabel);
         btnGuardar.setBackground(new java.awt.Color(39, 174, 96));
         btnGuardar.setForeground(java.awt.Color.WHITE);
         btnGuardar.addActionListener(e -> guardarConsumible());
         panelBotones.add(btnGuardar);
         btnActualizar = new JButton("ACTUALIZAR");
         btnActualizar.setFont(fuenteLabel);
         btnActualizar.setBackground(new java.awt.Color(41, 128, 185));
         btnActualizar.setForeground(java.awt.Color.WHITE);
         btnActualizar.addActionListener(e -> actualizarConsumible());
         panelBotones.add(btnActualizar);
         btnStock = new JButton("SURTIR BOD.");
         btnStock.setFont(fuenteLabel);
         btnStock.setBackground(new java.awt.Color(211, 84, 0));
         btnStock.setForeground(java.awt.Color.WHITE);
         btnStock.addActionListener(e -> ajustarStock());
         panelBotones.add(btnStock);
         JButton btnEliminar = new JButton("ELIMINAR");
         btnEliminar.setFont(fuenteLabel);
         btnEliminar.addActionListener(e -> eliminarConsumible());
         panelBotones.add(btnEliminar);
         btnLimpiar = new JButton("LIMPIAR");
         btnLimpiar.setFont(fuenteLabel);
         btnLimpiar.setBackground(new java.awt.Color(149, 165, 166));
         btnLimpiar.setForeground(java.awt.Color.WHITE);
         btnLimpiar.addActionListener(e -> limpiarFormulario());
         panelBotones.add(btnLimpiar);         
         
         pnlNorte.add(pnlCampos, BorderLayout.CENTER);
         pnlNorte.add(panelBotones, BorderLayout.SOUTH);
         add(pnlNorte, BorderLayout.NORTH);
         
         // --- PANEL DE TABLA ---
         modeloTabla = new DefaultTableModel(
             new Object[]{"ID", "Nombre", "P. Compra", "P. Multa", "Fecha Compra", "Cantidad Bodega", "En Habitaci�n", "Perdidos", "�Prestable?"}, 0) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false;
             }
         };
         tablaConsumibles = new JTable(modeloTabla);
         tablaConsumibles.setRowHeight(25);
         tablaConsumibles.setFont(GUIToolkit.FONT_CELDAS);
         tablaConsumibles.getTableHeader().setFont(GUIToolkit.FONT_HEADER_CELDAS);
         
         tablaConsumibles.getSelectionModel().addListSelectionListener(e -> {
             int fila = tablaConsumibles.getSelectedRow();
             if (fila != -1) {
                 idSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
                 txtNombre.setText((String) modeloTabla.getValueAt(fila, 1));
                 txtPrecioCompra.setText(modeloTabla.getValueAt(fila, 2).toString());
                 txtPrecioMulta.setText(modeloTabla.getValueAt(fila, 3).toString());
                 txtFechaCompra.setText(modeloTabla.getValueAt(fila, 4) != null ? modeloTabla.getValueAt(fila, 4).toString() : "");
                 txtStock.setText(modeloTabla.getValueAt(fila, 5).toString());
                 txtStock.setEditable(false);

                 String prestable = modeloTabla.getValueAt(fila, 8).toString();
                 chkPrestable.setSelected(prestable.equals("S�"));
             }
         });
         add(new JScrollPane(tablaConsumibles), BorderLayout.CENTER);                
     }
     
     // ------------------------------------------------
	 // CONSTRUCTOR ETIQUETAS
	 // ------------------------------------------------
     private JLabel crearLabel(String texto) {
         JLabel lbl = new JLabel(texto);
         lbl.setFont(fuenteLabel);
         return lbl;
     }

     private void cargarDatos() {
         modeloTabla.setRowCount(0);
         try (Connection cn = DBConnection.getConnection()) {
             ConsumibleDAO dao = new ConsumibleDAO(cn);
             List<ConsumibleVO> lista = dao.listarTodo();
             for (ConsumibleVO c : lista) {
                 modeloTabla.addRow(new Object[]{
                     c.getIdConsumible(),
                     c.getNombre(),
                     c.getPrecioCompra(),
                     c.getPrecioMulta(),
                     c.getFechaCompra(),
                     c.getStock(),
                     c.getEnHabitaciones(),
                     c.getPerdidosCobrados(),
                     c.getEsPrestable() != null && c.getEsPrestable() ? "S�" : "NO"
                 });
             }
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage());
         }
     }

     private void guardarConsumible() {
         if (validarCampos()) {
             try (Connection cn = DBConnection.getConnection()) {
                 ConsumibleDAO dao = new ConsumibleDAO(cn);
                 ConsumibleVO c = new ConsumibleVO();
                 c.setNombre(txtNombre.getText().trim());
                 c.setPrecioCompra(!txtPrecioCompra.getText().trim().isEmpty()
                		    ? new BigDecimal(txtPrecioCompra.getText().trim())
                		    : BigDecimal.ZERO);
                		c.setPrecioMulta(!txtPrecioMulta.getText().trim().isEmpty()
                		    ? new BigDecimal(txtPrecioMulta.getText().trim())
                		    : BigDecimal.ZERO);
                 c.setStock(Integer.parseInt(txtStock.getText().trim()));
                 c.setEsPrestable(chkPrestable.isSelected());
                 
                 if (!txtFechaCompra.getText().trim().isEmpty()) {
                     c.setFechaCompra(java.sql.Date.valueOf(txtFechaCompra.getText().trim()));
                 }
                 c.setIdEstadoRegistro(1);
                 dao.insertar(c);
                 JOptionPane.showMessageDialog(this, "Consumible guardado con �xito");
                 limpiarFormulario();
                 cargarDatos();
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
             }
         }
     }
     
     private void actualizarConsumible() {
         if (idSeleccionado == null) {
             JOptionPane.showMessageDialog(this, "Seleccione un consumible de la tabla.");
             return;
         }
         if (validarCampos()) {
             try (Connection cn = DBConnection.getConnection()) {
                 ConsumibleDAO dao = new ConsumibleDAO(cn);
                 ConsumibleVO c = new ConsumibleVO();
                 c.setIdConsumible(idSeleccionado);
                 c.setNombre(txtNombre.getText().trim());
                 c.setPrecioCompra(!txtPrecioCompra.getText().trim().isEmpty()
                		    ? new BigDecimal(txtPrecioCompra.getText().trim())
                		    : BigDecimal.ZERO);
                		c.setPrecioMulta(!txtPrecioMulta.getText().trim().isEmpty()
                		    ? new BigDecimal(txtPrecioMulta.getText().trim())
                		    : BigDecimal.ZERO);
                 c.setEsPrestable(chkPrestable.isSelected());
                 
                 if (!txtFechaCompra.getText().trim().isEmpty()) {
                     c.setFechaCompra(java.sql.Date.valueOf(txtFechaCompra.getText().trim()));
                 }
                 
                 dao.actualizar(c);
                 JOptionPane.showMessageDialog(this, "Consumible actualizado con �xito");
                 limpiarFormulario();
                 cargarDatos();
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
             }
         }
     }
     
     private void ajustarStock() {
         if (idSeleccionado == null) {
             JOptionPane.showMessageDialog(this, "Seleccione un consumible de la tabla.");
             return;
         }
         String mensaje = "Ajuste de Inventario de Bodega:\n\n" +
                          "+ Positivo para sumar (Compr� m�s)\n" +
                          "- Negativo para restar (Desechado/Roto)\n\n" +
                          "Cantidad:";
         String cantStr = JOptionPane.showInputDialog(this, mensaje, "0");
         if (cantStr != null && !cantStr.isEmpty()) {
             try {
                 int cantidad = Integer.parseInt(cantStr);
                 try (Connection cn = DBConnection.getConnection()) {
                     ConsumibleDAO dao = new ConsumibleDAO(cn);
                     dao.ajustarStock(idSeleccionado, cantidad);
                     JOptionPane.showMessageDialog(this, "Cantidad ajustada correctamente.");
                     limpiarFormulario();
                     cargarDatos();
                 }
             } catch (NumberFormatException e) {
                 JOptionPane.showMessageDialog(this, "Ingrese un n�mero v�lido.");
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(this, "Error al ajustar: " + e.getMessage());
             }
         }
     }
     
     private void eliminarConsumible() {
         if (idSeleccionado == null) return;
         int confirm = JOptionPane.showConfirmDialog(this, "�Seguro de eliminar este consumible?", "Confirmar", JOptionPane.YES_NO_OPTION);
         if (confirm == JOptionPane.YES_OPTION) {
             try (Connection cn = DBConnection.getConnection()) {
                 ConsumibleDAO dao = new ConsumibleDAO(cn);
                 dao.eliminar(idSeleccionado);
                 JOptionPane.showMessageDialog(this, "Consumible eliminado.");
                 limpiarFormulario();
                 cargarDatos();
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
             }
         }
     }
     
     private boolean validarCampos() {
    	    // NOMBRE: siempre obligatorio
    	    if (txtNombre.getText().trim().isEmpty()) {
    	        JOptionPane.showMessageDialog(this, "El NOMBRE del consumible es obligatorio.");
    	        return false;
    	    }
    	    // PRECIO COMPRA y PRECIO MULTA: solo obligatorios si es prestable
    	    boolean esPrestable = chkPrestable.isSelected();
    	    if (esPrestable) {
    	        if (txtPrecioCompra.getText().trim().isEmpty()) {
    	            JOptionPane.showMessageDialog(this,
    	                "El PRECIO DE COMPRA es obligatorio cuando el ítem es prestable.");
    	            return false;
    	        }
    	        if (txtPrecioMulta.getText().trim().isEmpty()) {
    	            JOptionPane.showMessageDialog(this,
    	                "El PRECIO DE MULTA es obligatorio cuando el ítem es prestable.");
    	            return false;
    	        }
    	    }
    	    // Validar formato numérico solo si tienen contenido
    	    try {
    	        if (!txtPrecioCompra.getText().trim().isEmpty()) {
    	            new BigDecimal(txtPrecioCompra.getText().trim());
    	        }
    	        if (!txtPrecioMulta.getText().trim().isEmpty()) {
    	            new BigDecimal(txtPrecioMulta.getText().trim());
    	        }
    	        if (idSeleccionado == null) {
    	            Integer.parseInt(txtStock.getText().trim());
    	        }
    	        if (!txtFechaCompra.getText().trim().isEmpty()) {
    	            java.sql.Date.valueOf(txtFechaCompra.getText().trim());
    	        }
    	    } catch (NumberFormatException e) {
    	        JOptionPane.showMessageDialog(this, "Precios y Cantidad deben ser números válidos.");
    	        return false;
    	    } catch (IllegalArgumentException e) {
    	        JOptionPane.showMessageDialog(this, "La fecha debe tener el formato YYYY-MM-DD.");
    	        return false;
    	    }
    	    return true;
    	}
     
     private void limpiarFormulario() {
         txtNombre.setText("");
         txtPrecioCompra.setText("");
         txtPrecioMulta.setText("");
         txtFechaCompra.setText("");
         txtStock.setText("");
         chkPrestable.setSelected(false);
         txtStock.setEditable(true);
         idSeleccionado = null;
         tablaConsumibles.clearSelection();
     }
}
