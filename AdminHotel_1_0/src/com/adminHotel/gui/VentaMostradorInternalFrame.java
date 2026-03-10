package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.adminHotel.dao.ProductoDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.DetalleTemporal;
import com.adminHotel.vo.ProductoVO;

public class VentaMostradorInternalFrame extends JInternalFrame {
	
	private static final long serialVersionUID = -3130905198866840659L;
	private JPanel pnlProductos;
	private List<DetalleTemporal> carrito = new ArrayList<>();
	private Font fuenteTituloBordes = new Font("Arial", Font.BOLD, 20);
	private Font fuenteBotones = new Font("Arial", Font.BOLD, 20);
	private Font fuenteTiquetas = new Font("Arial", Font.BOLD, 20);
	private JTextField txtCodBarras;
	private JPanel pnlBusqueda; 
	
	public VentaMostradorInternalFrame() {
		super("HOTEL LAS TERRAZAS II - Venta en vitrina", true, true, true, true);
		
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
		
		// Panel superior de información
		pnlBusqueda = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 10));
		pnlBusqueda.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "BUSQUEDA RAPIDA (PISTOLA)", 0, 0, new Font("Arial", Font.BOLD, 14), Color.BLUE));
		JLabel lblIcono = new JLabel("CÓDIGO:");
		lblIcono.setFont(fuenteTiquetas);
		txtCodBarras = new JTextField(20);
		txtCodBarras.setFont(new Font("Arial", Font.BOLD, 25));
		txtCodBarras.setBackground(new Color(255, 255, 200));
		pnlBusqueda.add(lblIcono);
		pnlBusqueda.add(txtCodBarras);
		txtCodBarras.addActionListener(e -> buscarProductoPorCodigo());
		add(pnlBusqueda, BorderLayout.NORTH);

        pnlProductos = new JPanel(new GridLayout(0, 4, 15, 15));
        pnlProductos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JScrollPane scroll = new JScrollPane(pnlProductos);
        add(scroll, BorderLayout.CENTER);

        // Panel inferior con el botón de pagar
        JButton btnVerCarrito = new JButton("VER CARRITO Y PAGAR");
        btnVerCarrito.setFont(fuenteBotones);
        btnVerCarrito.setBackground(new Color(46, 204, 113));
        btnVerCarrito.setForeground(Color.WHITE);
        btnVerCarrito.addActionListener(e -> abrirCarrito());
        add(btnVerCarrito, BorderLayout.SOUTH);

        cargarBotonesProductos();
	}
	
	private void cargarBotonesProductos() {
        try (Connection cn = DBConnection.getConnection()) {
            ProductoDAO dao = new ProductoDAO(cn);
            List<ProductoVO> productos = dao.listarActivos();

            for (ProductoVO p : productos) {
                JButton btn = new JButton("<html><center>" + p.getNombre() + "<br>$" + p.getPrecioVenta() + "<br>Stock: " + p.getStock() + "</center></html>");
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
        String input = JOptionPane.showInputDialog(this, "Producto: " + p.getNombre() + "\nCantidad a llevar:", "1");
        if (input != null && !input.isEmpty()) {
            try {
                int cant = Integer.parseInt(input);
                if (cant > p.getStock()) {
                    JOptionPane.showMessageDialog(this, "No hay stock suficiente. Máximo: " + p.getStock());
                    return;
                }
                // Añadir al carrito temporal
                carrito.add(new DetalleTemporal(p, cant));
                JOptionPane.showMessageDialog(this, "Añadido: " + p.getNombre() + " x" + cant);
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
        // Llamaremos a un JDialog que crearemos a continuación
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
