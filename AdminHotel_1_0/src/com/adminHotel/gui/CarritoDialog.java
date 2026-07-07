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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.adminHotel.dao.ReciboDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.DetalleTemporal;
import com.adminHotel.util.GUIToolkit;
import com.adminHotel.util.MetodoPagoEnum;
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

        JButton btnPagar = new JButton("PAGAR");
        btnPagar.setFont(fuenteTiquetas);
        btnPagar.setBackground(new Color(46, 204, 113));
        btnPagar.setForeground(Color.BLACK);
        btnPagar.addActionListener(e -> procesarVenta());

        JButton btnCancelar = new JButton("CANCELAR VENTA");
        btnCancelar.setFont(fuenteTiquetas);
        btnCancelar.setForeground(Color.BLACK);
        btnCancelar.addActionListener(e -> {
            padre.vaciarCarrito();
            dispose();
        });
        
        JButton btnQuitar = new JButton("QUITAR");
        btnQuitar.setFont(fuenteTiquetas);
        btnQuitar.setBackground(new Color(231, 76, 60)); // Rojo
        btnQuitar.setForeground(Color.BLACK);
        btnQuitar.setFocusPainted(false);
        btnQuitar.addActionListener(e -> {
            int fila = tablaCarrito.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(CarritoDialog.this,
                    "Seleccione un producto de la lista para quitarlo.",
                    "HOTEL LAS TERRAZAS II - Atención",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            String nombreProducto = modelo.getValueAt(fila, 0).toString();
            int confirmar = JOptionPane.showConfirmDialog(CarritoDialog.this,
                "¿Desea quitar del carrito: " + nombreProducto + "?",
                "HOTEL LAS TERRAZAS II - Quitar Producto",
                JOptionPane.YES_NO_OPTION);
            if (confirmar != JOptionPane.YES_OPTION) return;
            carrito.remove(fila);    // quita del List interno
            modelo.removeRow(fila);  // quita de la tabla visual
            calcularTotal();          // recalcula el total en pantalla
        });

        pnlBotones.add(btnQuitar);
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
        
        // =============================
        // VALIDAR TURNO DE CAJA MOSTRADOR (ID 2)
        // =============================
        try {
            com.adminHotel.service.OperationService opService = new com.adminHotel.service.OperationService();
            opService.validarTurnoParaVenta(2); // Caja Mostrador
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                e.getMessage(),
                "HOTEL LAS TERRAZAS II - VALIDACIÓN DE TURNO",
                JOptionPane.ERROR_MESSAGE);
            return; // No continuar con la venta
        }

        // =============================
        // METODO DE PAGO
        // =============================
        Object[] opcionesIconos = {
            new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/efectivo.png")),
            new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/nequi.png")) 
        };
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
            switch (seleccion) {
            case 0:
                seleccion = MetodoPagoEnum.EFECTIVO.getCodigo();
                break;
            case 1:
                seleccion = MetodoPagoEnum.NEQUI.getCodigo();
                break;
            }
        }
        
        BigDecimal totalFinal = totalVenta;
        if (seleccion == MetodoPagoEnum.NEQUI.getCodigo()) {
            try {
                Connection cnParam = DBConnection.getConnection();
                com.adminHotel.dao.ParametroDAO paramDAO = new com.adminHotel.dao.ParametroDAO(cnParam);
                BigDecimal recargo = paramDAO.obtenerValor("TRANSACCION_NEQUI");
                cnParam.close();
                if (recargo != null) {
                    totalFinal = totalVenta.add(recargo);
                    JOptionPane.showMessageDialog(this,
                        "Pago vía Nequi:\n" +
                        "  Subtotal productos: $" + totalVenta + "\n" +
                        "  Recargo transacción: $" + recargo + "\n" +
                        "  TOTAL A COBRAR: $" + totalFinal,
                        "HOTEL LAS TERRAZAS II - Recargo Nequi", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                System.err.println("No se pudo obtener recargo Nequi: " + ex.getMessage());
            }
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Confirmar venta por $" + totalFinal + "?",
                "HOTEL LAS TERRAZAS II - Confirmar Pago", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        Connection cn = null;
        try {
            cn = DBConnection.getConnection();
            cn.setAutoCommit(false);

            // 1. CABECERA DE VENTA
            com.adminHotel.dao.VentaDAO vDao = new com.adminHotel.dao.VentaDAO(cn);
            VentaVO vVo = new VentaVO();
            vVo.setIdCliente(null);
            vVo.setIdConceptoVenta(2); // VENTA MOSTRADOR
            vVo.setTotal(totalFinal);
            StringBuilder obs = new StringBuilder();
            for (DetalleTemporal det : carrito) {
                obs.append(det.getCantidad()).append("x ").append(det.getProducto().getNombre()).append(", ");
            }
            vVo.setObservacion(obs.toString());
            Integer idVenta = vDao.insertar(vVo);
            vVo.setIdVenta(idVenta);

            // 2. DETALLES Y DESCUENTO DE STOCK
            com.adminHotel.dao.VentaDetalleDAO detalleDAO = new com.adminHotel.dao.VentaDetalleDAO(cn);
            com.adminHotel.dao.ProductoDAO pDao = new com.adminHotel.dao.ProductoDAO(cn);
            for (DetalleTemporal det : carrito) {
                detalleDAO.insertarDetalleMostrador(
                        idVenta,
                        det.getProducto().getIdProducto(),
                        det.getCantidad(),
                        det.getProducto().getPrecioVenta());
                pDao.descontarStock(det.getProducto().getIdProducto(), det.getCantidad());
            }

            // 3. PAGO — con turno de CAJA MOSTRADOR estampado
            com.adminHotel.dao.PagoDAO pagoDAO = new com.adminHotel.dao.PagoDAO(cn);
            com.adminHotel.dao.TurnoCajaDAO turnoDAO = new com.adminHotel.dao.TurnoCajaDAO(cn);

            com.adminHotel.vo.PagoVO pago = new com.adminHotel.vo.PagoVO();
            pago.setIdVenta(idVenta);
            pago.setIdMetodo(seleccion);
            pago.setIdEstadoRegistro(com.adminHotel.util.EstadoRegistroEnum.ACTIVO.getCodigo());
            pago.setValor(totalFinal);

            // Estampar turno activo de CAJA MOSTRADOR (id_caja = 2)
            com.adminHotel.vo.TurnoCajaVO turnoActivo = turnoDAO.buscarTurnoAbierto(2);
            if (turnoActivo != null) {
                pago.setIdTurnoCaja(turnoActivo.getIdTurnoCaja());
            }

            Integer idPago = pagoDAO.insertar(pago);
            
            ReciboDAO reciboDAO = new ReciboDAO(cn);
            Integer idRecibo = reciboDAO.insertarReciboMostrador(idVenta, idPago);

            cn.commit();
            JOptionPane.showMessageDialog(this, "¡VENTA EXITOSA!\nInventario actualizado.");
            
            String metodoPagoStr = (seleccion == MetodoPagoEnum.NEQUI.getCodigo()) ? "NEQUI" : "EFECTIVO";            
            imprimirRecibo(idRecibo, totalFinal, metodoPagoStr);
            
            padre.vaciarCarrito();
            dispose();

        } catch (Exception e) {
            try { if (cn != null) cn.rollback(); } catch (Exception ex) {}
            JOptionPane.showMessageDialog(this, "Error en la venta: " + e.getMessage());
        } finally {
            try { if (cn != null) cn.close(); } catch (Exception ex) {}
        }
    }
    
	 // -----------------------------------------------
	 // IMPRIMIR RECIBO DE VENTA MOSTRADOR
	 // -----------------------------------------------
	 private void imprimirRecibo(Integer idRecibo, BigDecimal totalFinal, String metodoPagoStr) {
	
	     int printOpt = JOptionPane.showConfirmDialog(
	             this,
	             "¿DESEA IMPRIMIR EL RECIBO No. " + idRecibo + "?",
	             "HOTEL LAS TERRAZAS II - Imprimir",
	             JOptionPane.YES_NO_OPTION,
	             JOptionPane.QUESTION_MESSAGE);
	
	     if (printOpt == JOptionPane.YES_OPTION) {
	         try {
	             java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
	
	             java.awt.print.PageFormat pf = job.defaultPage();
	             java.awt.print.Paper papel = pf.getPaper();
	             double anchoMM = 58 * 2.8346;
	             double altoMM  = 600 + (carrito.size() * 30); // Alto dinámico según items
	             papel.setSize(anchoMM, altoMM);
	             papel.setImageableArea(0, 0, anchoMM, altoMM);
	             pf.setPaper(papel);
	
	             com.adminHotel.printer.TicketVentaMostrador ticket =
	                     new com.adminHotel.printer.TicketVentaMostrador(
	                             idRecibo,
	                             carrito,
	                             totalFinal,
	                             new java.sql.Timestamp(System.currentTimeMillis()),
	                             metodoPagoStr);
	
	             job.setPrintable(ticket, pf);
	             job.print(); // Sin diálogo de impresora, directo a la predeterminada
	
	         } catch (java.awt.print.PrinterException ex) {
	             JOptionPane.showMessageDialog(this,
	                     "Error al imprimir:\n" + ex.getMessage(),
	                     "HOTEL LAS TERRAZAS II - Error",
	                     JOptionPane.ERROR_MESSAGE);
	         }
	     } else {
				GUIToolkit.abrirCajon(this);
		 }
	 }

}
