package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.adminHotel.dao.ProductoDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.DetalleTemporal;
import com.adminHotel.vo.ProductoVO;

public class VentaMostradorInternalFrame extends JInternalFrame {
	
	private static final long serialVersionUID = -3130905198866840659L;
	private JPanel pnlProductos;
	private List<DetalleTemporal> carrito = new ArrayList<>();
	private Font fuenteBotones = new Font("Arial", Font.BOLD, 20);
	private Font fuenteTiquetas = new Font("Arial", Font.BOLD, 20);
	private JTextField txtCodBarras;
	private JPanel pnlBusqueda;
	
	public VentaMostradorInternalFrame() {
		super("HOTEL LAS TERRAZAS II - Venta de Vitrina", true, true, true, true);
		
		setLayout(new BorderLayout());

		SwingUtilities.invokeLater(() -> {
    	    JDesktopPane desktop = getDesktopPane();
    	    if (desktop != null) {
    	        
    	    	Dimension size = desktop.getSize();    	        
    	        setSize(size.width, size.height);    	        
    	        setLocation(0, 0);
    	    }
    	});
		
		// Panel superior de informacion
		pnlBusqueda = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 10));
		pnlBusqueda.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "BUSQUEDA RAPIDA (PISTOLA)", 0, 0, new Font("Arial", Font.BOLD, 14), Color.BLUE));
		JLabel lblIcono = new JLabel("CODIGO:");
		lblIcono.setFont(fuenteTiquetas);
		txtCodBarras = new JTextField(20);
		txtCodBarras.setFont(new Font("Arial", Font.BOLD, 25));
		txtCodBarras.setBackground(new Color(255, 255, 200));
		pnlBusqueda.add(lblIcono);
		pnlBusqueda.add(txtCodBarras);
		txtCodBarras.addActionListener(e -> buscarProductoPorCodigo());
		// =============================================
        // CAMPO BÚSQUEDA POR NOMBRE CON AUTOCOMPLETADO
        // =============================================
        JLabel lblNombre = new JLabel("NOMBRE:");
        lblNombre.setFont(fuenteTiquetas);
        pnlBusqueda.add(lblNombre);
        JTextField txtBusquedaNombre = new JTextField(25);
        txtBusquedaNombre.setFont(new Font("Arial", Font.BOLD, 25));
        txtBusquedaNombre.setBackground(new Color(200, 255, 200));
        pnlBusqueda.add(txtBusquedaNombre);
        // Popup que mostrará las coincidencias
        JPopupMenu popupSugerencias = new JPopupMenu();
        // Lista de todos los productos cargada una sola vez para no ir a BD en cada keystroke
        final List<ProductoVO>[] todosProductos = new List[]{new ArrayList<>()};
        try (Connection cnTemp = DBConnection.getConnection()) {
            todosProductos[0] = new ProductoDAO(cnTemp).listarActivos();
        } catch (Exception ex) {
            // Si falla al precargar, el campo simplemente no sugiere nada
        }
        txtBusquedaNombre.getDocument().addDocumentListener(new DocumentListener() {
            private void actualizarSugerencias() {
                // Forzar UPPERCASE mientras escribe
                String textoOriginal = txtBusquedaNombre.getText();
                String textoMayus = textoOriginal.toUpperCase();
                if (!textoOriginal.equals(textoMayus)) {
                    // Evitar recursión infinita
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        int pos = txtBusquedaNombre.getCaretPosition();
                        txtBusquedaNombre.getDocument().removeDocumentListener(this);
                        txtBusquedaNombre.setText(textoMayus);
                        txtBusquedaNombre.setCaretPosition(Math.min(pos, textoMayus.length()));
                        txtBusquedaNombre.getDocument().addDocumentListener(this);
                    });
                    return;
                }
                String filtro = textoMayus.trim();
                popupSugerencias.setVisible(false);
                popupSugerencias.removeAll();
                if (filtro.isEmpty()) return;
                List<ProductoVO> coincidencias = new ArrayList<>();
                for (ProductoVO p : todosProductos[0]) {
                    if (p.getNombre().toUpperCase().contains(filtro)) {
                        coincidencias.add(p);
                    }
                }
                if (coincidencias.isEmpty()) return;
                for (ProductoVO p : coincidencias) {
                    String etiqueta = p.getNombre().toUpperCase() + "  |  $" + p.getPrecioVenta() + "  |  Stock: " + p.getStock();
                    JMenuItem item = new JMenuItem(etiqueta);
                    item.setFont(new Font("Arial", Font.BOLD, 20));
                    item.addActionListener(ev -> {
                        popupSugerencias.setVisible(false);
                        txtBusquedaNombre.setText("");
                        pedirCantidad(p); // Abre el popup ya existente de cantidad
                    });
                    popupSugerencias.add(item);
                }
                // Mostrar el popup debajo del campo de texto
                popupSugerencias.show(txtBusquedaNombre, 0, txtBusquedaNombre.getHeight());
                txtBusquedaNombre.requestFocus();
            }
            @Override public void insertUpdate(DocumentEvent e) { actualizarSugerencias(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizarSugerencias(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizarSugerencias(); }
        });
        // Cerrar popup con ESC
        txtBusquedaNombre.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupSugerencias.setVisible(false);
                    txtBusquedaNombre.setText("");
                }
            }
        });
        add(pnlBusqueda, BorderLayout.NORTH);

        pnlProductos = new JPanel(new GridLayout(0, 4, 15, 15));
        pnlProductos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JScrollPane scroll = new JScrollPane(pnlProductos);
        add(scroll, BorderLayout.CENTER);

        // Panel inferior con el boton de pagar
        JButton btnVerCarrito = new JButton("*** PAGAR ***");
        btnVerCarrito.setFont(new Font("Arial", Font.BOLD, 26)); // Letra más grande también
        btnVerCarrito.setBackground(new Color(46, 204, 113));
        btnVerCarrito.setForeground(Color.BLACK);
        btnVerCarrito.setPreferredSize(new Dimension(Integer.MAX_VALUE, 65)); // <-- AQUI el grosor
        btnVerCarrito.setFocusPainted(false);
        btnVerCarrito.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)); // Cursor de mano al pasar
        btnVerCarrito.addActionListener(e -> abrirCarrito());
        add(btnVerCarrito, BorderLayout.SOUTH);

        cargarBotonesProductos();
	}
	
	private void cargarBotonesProductos() {
        try (Connection cn = DBConnection.getConnection()) {
            ProductoDAO dao = new ProductoDAO(cn);
            List<ProductoVO> productos = dao.listarActivos();

            for (ProductoVO p : productos) {
                JButton btn = new JButton("<html><center>" + p.getNombre() + "<br>$" + p.getPrecioVenta() + "<br>Cantidad: " + p.getStock() + "</center></html>");
                btn.setFont(fuenteBotones);
                btn.setPreferredSize(new Dimension(150, 100));
                
                btn.addActionListener(e -> pedirCantidad(p));
                pnlProductos.add(btn);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos: " + e.getMessage());
        }
    }
	
	private void pedirCantidad(ProductoVO p) {
        String input = JOptionPane.showInputDialog(this, "PRODUCTO: " + p.getNombre() + "\nCANTIDAD:", "1");
        if (input != null && !input.isEmpty()) {
            try {
                int cant = Integer.parseInt(input);
                if (cant > p.getStock()) {
                    JOptionPane.showMessageDialog(this, "No hay cantidad suficiente. Máximo: " + p.getStock());
                    return;
                }
                // A�adir al carrito temporal
                carrito.add(new DetalleTemporal(p, cant));
                JOptionPane.showMessageDialog(this, "AGREGADO: " + p.getNombre() + " x" + cant);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Ingrese un número válido.");
            }
        }
    }
	
	private void abrirCarrito() {
        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }
        // Llamaremos a un JDialog que crearemos a continuaci�n
        CarritoDialog dialog = new CarritoDialog(null, true, carrito, this);
        dialog.setVisible(true);
    }
	
	public void vaciarCarrito() {
        this.carrito.clear();
    }
	
	private void buscarProductoPorCodigo() {
	    String codigo = txtCodBarras.getText().trim();
	    if (codigo.isEmpty()) return;
	    try (Connection cn = DBConnection.getConnection()) {
	        ProductoDAO dao = new ProductoDAO(cn);
	        ProductoVO p = dao.buscarPorCodigo(codigo);
	        if (p != null) {
	            pedirCantidad(p);
	        } else {
	            JOptionPane.showMessageDialog(this, "Producto no encontrado con el código: " + codigo, 
	                "Error", JOptionPane.ERROR_MESSAGE);
	        }
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, "Error de búsqueda: " + e.getMessage());
	    } finally {
	        txtCodBarras.setText("");
	        txtCodBarras.requestFocus();
	    }
	}
}
