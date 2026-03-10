package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.adminHotel.dao.ProductoDAO;
import com.adminHotel.dao.VentaDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.DetalleTemporal;
import com.adminHotel.vo.VentaVO;

public class CarritoDialog extends JDialog {
	
	private static final long serialVersionUID = 3062071086435829003L;
	private JTable tablaCarrito;
    private DefaultTableModel modelo;
    private JLabel lblTotal;
    private List<DetalleTemporal> carrito;
    private VentaMostradorInternalFrame padre;
    private BigDecimal totalVenta = BigDecimal.ZERO;
    private Font fuenteTiquetas = new Font("Arial", Font.BOLD, 20);
    
    public CarritoDialog(Frame owner, boolean modal, List<DetalleTemporal> carrito, VentaMostradorInternalFrame padre) {
        super(owner, "HOTEL LAS TERRAZAS II - Resumen de Venta...", modal);
        this.carrito = carrito;
        this.padre = padre;
        
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponentes();
        calcularTotal();
    }
    
    private void initComponentes() {
        // 1. Tabla del Carrito
        modelo = new DefaultTableModel(new Object[]{"Producto", "Cant.", "P. Unit", "Subtotal"}, 0);
        tablaCarrito = new JTable(modelo);
        tablaCarrito.setFont(new Font("Arial", Font.PLAIN, 16));
        tablaCarrito.setRowHeight(25);

        for (DetalleTemporal det : carrito) {
            modelo.addRow(new Object[]{
                det.getProducto().getNombre(),
                det.getCantidad(),
                det.getProducto().getPrecioVenta(),
                det.getSubtotal()
            });
        }
        add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        // 2. Panel Inferior (Total y Botones)
        JPanel pnlInferior = new JPanel(new GridLayout(2, 1));
        
        lblTotal = new JLabel("TOTAL A PAGAR: $0.00  ", JLabel.RIGHT);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 25));
        lblTotal.setForeground(new Color(192, 57, 43)); // Rojo oscuro
        
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        JButton btnCancelar = new JButton("CANCELAR VENTA");
        JButton btnPagar = new JButton("CONFIRMAR Y PAGAR");

        btnCancelar.setFont(fuenteTiquetas);
        btnPagar.setFont(fuenteTiquetas);
        btnPagar.setBackground(new Color(46, 204, 113));
        btnPagar.setForeground(Color.WHITE);

        btnCancelar.addActionListener(e -> {
            padre.vaciarCarrito();
            dispose();
        });

        btnPagar.addActionListener(e -> procesarVenta());

        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnPagar);
        
        pnlInferior.add(lblTotal);
        pnlInferior.add(pnlBotones);
        add(pnlInferior, BorderLayout.SOUTH);
    }
    
    private void calcularTotal() {
        totalVenta = BigDecimal.ZERO;
        for (DetalleTemporal det : carrito) {
            totalVenta = totalVenta.add(det.getSubtotal());
        }
        lblTotal.setText("TOTAL A PAGAR: $" + totalVenta + "  ");
    }
    
    private void procesarVenta() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Desea registrar la venta por $" + totalVenta + "?", "Confirmar Pago", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Connection cn = null;
            try {
                cn = DBConnection.getConnection();
                cn.setAutoCommit(false); // IMPORTANTE: Transacción iniciada

                // 1. Crear Cabecera de Venta
                VentaDAO vDao = new VentaDAO(cn);
                VentaVO vVo = new VentaVO();
                vVo.setIdCliente(null); // Venta mostrador usualmente no requiere cliente
                vVo.setIdConceptoVenta(2); // 2 = MOSTRADOR
                vVo.setTotal(totalVenta);
                vVo.setObservacion("Venta rápida mostrador");
                
                int idVenta = vDao.insertar(vVo);

                // 2. Crear Detalles y Descontar Stock
                ProductoDAO pDao = new ProductoDAO(cn);
                // Aquí necesitarías un VentaDetalleDAO para insertar los items
                // Pero por ahora, simulamos el descuento de stock:
                for (DetalleTemporal det : carrito) {
                    pDao.descontarStock(det.getProducto().getIdProducto(), det.getCantidad());
                    // Aquí iría: detalleDao.insertar(idVenta, det);
                }

                cn.commit(); // Todo salió bien, guardamos cambios
                JOptionPane.showMessageDialog(this, "¡VENTA EXITOSA!\nInventario actualizado.");
                padre.vaciarCarrito();
                dispose();

            } catch (Exception e) {
                try { if (cn != null) cn.rollback(); } catch (Exception ex) {} // Error, deshacemos todo
                JOptionPane.showMessageDialog(this, "Error en la venta: " + e.getMessage());
            } finally {
                try { if (cn != null) cn.close(); } catch (Exception ex) {}
            }
        }
    }
}
