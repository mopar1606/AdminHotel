package com.adminHotel.service;

import java.math.BigDecimal;
import java.sql.Connection;

import com.adminHotel.dao.ClienteDAO;
import com.adminHotel.dao.HabitacionDAO;
import com.adminHotel.dao.HabitacionMovimientoDAO;
import com.adminHotel.dao.PagoDAO;
import com.adminHotel.dao.VentaDAO;
import com.adminHotel.dao.VentaDetalleDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.vo.ClienteVO;
import com.adminHotel.vo.HabitacionVO;
import com.adminHotel.vo.PagoVO;
import com.adminHotel.vo.VentaVO;

public class CheckInService {

	public void realizarCheckIn(HabitacionVO habitacion, String documento, ClienteVO datosNuevoCliente,
			boolean registrarPago) throws Exception {

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

			// =============================
			// CLIENTE
			// =============================
			ClienteVO cliente = clienteDAO.buscarPorDocumento(documento);

			if (cliente == null) {
				int idGenerado = clienteDAO.insertar(datosNuevoCliente);
				datosNuevoCliente.setIdCliente(idGenerado);
				cliente = datosNuevoCliente;
			}

			// =============================
			// PAGO
			// =============================
			BigDecimal valor = BigDecimal.ZERO;

			if (registrarPago) {
				valor = habitacion.getPrecio(); // ya lo tienes cargado
			}

			// =============================
			// VENTA
			// =============================
			VentaVO venta = new VentaVO();
			venta.setIdCliente(cliente.getIdCliente());
			venta.setTotal(valor);
			venta.setObservacion("Check-in habitación " + habitacion.getNumeroHabitacion());

			int idVenta = ventaDAO.insertar(venta);
			venta.setIdVenta(idVenta);

			detalleDAO.insertarDetalleHabitacion(venta, habitacion);

			if (registrarPago) {
				PagoVO pago = new PagoVO();
				pago.setIdCliente(cliente.getIdCliente());
				pago.setIdHabitacion(habitacion.getIdHabitacion());
				pago.setIdVenta(idVenta);
				pago.setValor(valor);

				pagoDAO.insertar(pago);
			}

			// =============================
			// MOVIMIENTO
			// =============================
			movDAO.registrarEntrada(habitacion.getIdHabitacion(), cliente.getIdCliente());

			// =============================
			// CAMBIAR ESTADO
			// =============================
			habitacionDAO.actualizarEstado(habitacion.getIdHabitacion(), 2); // OCUPADA

			cn.commit();

		} catch (Exception e) {

			if (cn != null) {
				try {
					cn.rollback();
				} catch (Exception ignored) {
				}
			}

			throw e;

		} finally {

			if (cn != null) {
				try {
					cn.setAutoCommit(true);
					cn.close();
				} catch (Exception ignored) {
				}
			}
		}
	}

}
