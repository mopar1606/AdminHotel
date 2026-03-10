package com.adminHotel.service;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.adminHotel.dao.ClienteDAO;
import com.adminHotel.dao.HabitacionDAO;
import com.adminHotel.dao.HabitacionMovimientoDAO;
import com.adminHotel.dao.PagoDAO;
import com.adminHotel.dao.ReciboDAO;
import com.adminHotel.dao.VentaDAO;
import com.adminHotel.dao.VentaDetalleDAO;
import com.adminHotel.printer.TicketCheckIn;
import com.adminHotel.util.ConceptoVentaEnum;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.EstadoHabitacionEnum;
import com.adminHotel.util.EstadoRegistroEnum;
import com.adminHotel.vo.ClienteVO;
import com.adminHotel.vo.HabitacionVO;
import com.adminHotel.vo.PagoVO;
import com.adminHotel.vo.ReciboVO;
import com.adminHotel.vo.VentaVO;

public class OperationService {

	// -------------------------------
	// TRANSACTION CHECK IN
	// -------------------------------
	public void doTransactionCheckIn(
			HabitacionVO habitacion, 
			String documento, 
			Integer noches, 
			Integer metodoPago) 
					throws Exception {

		Connection cn = null;
		try {
			
			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);

			ClienteDAO clienteDAO = new ClienteDAO(cn);
			VentaDAO ventaDAO = new VentaDAO(cn);
			VentaDetalleDAO detalleDAO = new VentaDetalleDAO(cn);
			PagoDAO pagoDAO = new PagoDAO(cn);
			HabitacionMovimientoDAO movDAO = new HabitacionMovimientoDAO(cn);
			HabitacionDAO habitacionDAO = new HabitacionDAO(cn);
			ReciboDAO reciboDAO = new ReciboDAO(cn);

			// =============================
			// CLIENTE VALIDAR O CREAR
			// =============================
			ClienteVO cliente = clienteDAO.buscarPorDocumento(documento);
			if (cliente == null) {

                JTextField txtNombre = new JTextField();
                JTextField txtTelefono = new JTextField();
                JTextField txtEmail = new JTextField();

                Object[] campos = {
                        "Nombre:", txtNombre,
                        "Teléfono:", txtTelefono,
                        "Email:", txtEmail
                };

                int op = JOptionPane.showConfirmDialog(
                		null, 
                		campos, 
                		"HOTEL LAS TERRAZAS II - Crear Cliente...", 
                		JOptionPane.OK_CANCEL_OPTION);

                if (op != JOptionPane.OK_OPTION) throw new Exception("Registro de cliente cancelado...");

                cliente = new ClienteVO();
                cliente.setDocumento(documento);
                cliente.setNombre(txtNombre.getText());
                cliente.setTelefono(txtTelefono.getText());
                cliente.setEmail(txtEmail.getText());

                int idGenerado = clienteDAO.insertar(cliente);
                cliente.setIdCliente(idGenerado);
            }
			
			// =============================
			// REGISTRAR VENTA
			// =============================
			VentaVO venta = new VentaVO();
			venta.setIdCliente(cliente.getIdCliente());
			venta.setIdConceptoVenta(ConceptoVentaEnum.VENTA_HABITACION.getCodigo());
			venta.setTotal(new BigDecimal(noches).multiply(habitacion.getPrecio()));
			venta.setObservacion(
					"Check-in habitación "
					.concat(habitacion.getNumeroHabitacion().toString())
					.concat(" - " + noches).concat(" noche(s)"));

			Integer idVenta = ventaDAO.insertar(venta);
			venta.setIdVenta(idVenta);
			
			// =============================
	        // DETALLE (AHORA CON NOCHES)
	        // =============================
			detalleDAO.insertarDetalleHabitacion(venta, habitacion, noches);

			// =============================
			// PAGO
			// =============================
			PagoVO pago = new PagoVO();
			pago.setIdVenta(idVenta);
	        pago.setIdCliente(cliente.getIdCliente());
	        pago.setIdHabitacion(habitacion.getIdHabitacion());
	        pago.setIdMetodo(metodoPago);
	        pago.setIdEstadoRegistro(EstadoRegistroEnum.ACTIVO.getCodigo());
	        pago.setValor(new BigDecimal(noches).multiply(habitacion.getPrecio()));
	        Integer idPago = pagoDAO.insertar(pago);

	        // =============================
	        // MOVIMIENTO (CON NOCHES)
	        // =============================
			Integer idMov = movDAO.registrarEntrada(
					habitacion.getIdHabitacion(),
					cliente.getIdCliente(),
					noches);

			// =============================
	        // CAMBIAR ESTADO (SIN NUMERO MAGICO)
	        // =============================
			habitacionDAO.actualizarEstado(
					habitacion.getIdHabitacion(),
					EstadoHabitacionEnum.OCUPADA.getCodigo());
			
			// =============================
	        // REGISTRAR RECIBO
	        // =============================
			ReciboVO recibo = new ReciboVO();
			recibo.setIdCliente(cliente.getIdCliente());
			recibo.setIdHabitacion(habitacion.getIdHabitacion());
			recibo.setIdPago(idPago);
			recibo.setIdVenta(venta.getIdVenta());
			recibo.setIdHabitacionMovimiento(idMov);
			recibo.setNoches(noches);
			reciboDAO.insertarRecibo(recibo);

			cn.commit();

		} catch (Exception e) {
	        if (cn != null) {
	            try { cn.rollback(); } catch (Exception ignored) {}
	        }
	        throw e;
	    } finally {
	        if (cn != null) {
	            try {
	                cn.setAutoCommit(true);
	                cn.close();
	            } catch (Exception ignored) {}
	        }
	    }
	}
	
	// -------------------------------
	// TRANSACTION CHECK OUT
	// -------------------------------
	public void doTransactionCheckOut(
			HabitacionVO habitacion)
					throws Exception{
		
		Connection cn = null;		
		try {
			
			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);
			
			HabitacionMovimientoDAO movDAO = new HabitacionMovimientoDAO(cn);
	        HabitacionDAO habitacionDAO = new HabitacionDAO(cn);
	        
	        if (movDAO.registrarSalida(habitacion.getIdHabitacion()) == 0) {
	            throw new Exception("No se encontró un movimiento de entrada activo para esta habitación.");
	        }
	        
	        if (habitacionDAO.actualizarEstado(habitacion.getIdHabitacion(),EstadoHabitacionEnum.POR_ASEO.getCodigo()) == 0) {
	        	throw new Exception("No se actualizo estado de habitación.");
	        }
	        
	        cn.commit();
	        
		} catch (Exception e) {
	        if (cn != null) {
	            try { cn.rollback(); } catch (Exception ignored) {}
	        }
	        throw e;
	    } finally {
	        if (cn != null) {
	            try {
	                cn.setAutoCommit(true);
	                cn.close();
	            } catch (Exception ignored) {}
	        }
	    }
	}
	
	// -------------------------------
	// TRANSACTION CHANGE STATE
	// -------------------------------
	public void doTransactionChangeState(
			HabitacionVO habitacion,
			Integer state)
					throws Exception{
		
		Connection cn = null;		
		try {
			
			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);
			
			HabitacionDAO habitacionDAO = new HabitacionDAO(cn);
			if (habitacionDAO.actualizarEstado(habitacion.getIdHabitacion(), state) == 0) {
	        	throw new Exception("No se actualizo estado de habitación.");
	        }
			
			cn.commit();
			
		} catch (Exception e) {
	        if (cn != null) {
	            try { cn.rollback(); } catch (Exception ignored) {}
	        }
	        throw e;
	    } finally {
	        if (cn != null) {
	            try {
	                cn.setAutoCommit(true);
	                cn.close();
	            } catch (Exception ignored) {}
	        }
	    }
	}
	
	// -------------------------------
	// PRINT TICKET
	// -------------------------------
	public void doPrintTicket(Integer idHabitacion)throws Exception{
		Connection cn = null;		
		try {
			
			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);
			
			ReciboDAO reciboDAO = new ReciboDAO(cn);
			Map<String, Object> datosRecibo = reciboDAO.obtenerDatosParaTicket(idHabitacion);
		    
		    if (datosRecibo == null) {
	            throw new Exception("No se encontró ningún recibo registrado para la Habitación Nro: " + idHabitacion);
	        }
		    
		    System.out.println((java.math.BigDecimal) datosRecibo.get("precio"));
		    
		    PrinterJob job = PrinterJob.getPrinterJob();
		    
		    TicketCheckIn ticket = new TicketCheckIn(
		    	    (Integer) datosRecibo.get("id_recibo"),
		    	    datosRecibo.get("id_cliente").toString(),
		    	    datosRecibo.get("numero_habitacion").toString(),
		    	    (Integer) datosRecibo.get("noches"),
		    	    (BigDecimal) datosRecibo.get("total"),
		    	    (Timestamp) datosRecibo.get("fecha_emision"),
		    	    (BigDecimal) datosRecibo.get("precio")
		    	);
		    
		    PageFormat pf = job.defaultPage();
		    Paper paper = new Paper();
		    
		    paper.setSize(226, 600); 
		    paper.setImageableArea(5, 5, 216, 590);
		    pf.setPaper(paper);
		    pf.setOrientation(PageFormat.PORTRAIT);
		    
		    job.setPrintable(ticket, pf);
		    
		    try {
		        job.print(); 
		    } catch (PrinterException e) {
		        throw new Exception("Error físico de impresora: " + e.getMessage());
		    }
			
		} catch (Exception e) {
	        if (cn != null) {
	            try { cn.rollback(); } catch (Exception ignored) {}
	        }
	        throw e;
	    } finally {
	        if (cn != null) {
	            try {
	                cn.setAutoCommit(true);
	                cn.close();
	            } catch (Exception ignored) {}
	        }
	    }
		
	}
	
}
