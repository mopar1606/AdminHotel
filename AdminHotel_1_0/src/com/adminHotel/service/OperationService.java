package com.adminHotel.service;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.adminHotel.dao.ClienteDAO;
import com.adminHotel.dao.ConsumibleDAO;
import com.adminHotel.dao.HabitacionDAO;
import com.adminHotel.dao.HabitacionMovimientoDAO;
import com.adminHotel.dao.PagoDAO;
import com.adminHotel.dao.ParametroDAO;
import com.adminHotel.dao.PrestamoConsumibleDAO;
import com.adminHotel.dao.ReciboDAO;
import com.adminHotel.dao.TurnoCajaDAO;
import com.adminHotel.dao.VentaDAO;
import com.adminHotel.dao.VentaDetalleDAO;
import com.adminHotel.printer.TicketCheckIn;
import com.adminHotel.util.ConceptoVentaEnum;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.EstadoHabitacionEnum;
import com.adminHotel.util.EstadoRegistroEnum;
import com.adminHotel.util.MetodoPagoEnum;
import com.adminHotel.vo.ClienteVO;
import com.adminHotel.vo.HabitacionVO;
import com.adminHotel.vo.PagoVO;
import com.adminHotel.vo.PrestamoConsumibleVO;
import com.adminHotel.vo.ReciboVO;
import com.adminHotel.vo.TurnoCajaVO;
import com.adminHotel.vo.VentaVO;

public class OperationService {

	// -------------------------------
	// TRANSACTION BIT TIME - CHECK IN
	// -------------------------------
	public void doTransactionBitTime(HabitacionVO habitacion) throws Exception {

		Connection cn = null;
		try {

			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);
			ParametroDAO parametroDAO = new ParametroDAO(cn);
			ClienteDAO clienteDAO = new ClienteDAO(cn);
			VentaDAO ventaDAO = new VentaDAO(cn);
			VentaDetalleDAO detalleDAO = new VentaDetalleDAO(cn);
			PagoDAO pagoDAO = new PagoDAO(cn);
			HabitacionMovimientoDAO movDAO = new HabitacionMovimientoDAO(cn);
			HabitacionDAO habitacionDAO = new HabitacionDAO(cn);
			ReciboDAO reciboDAO = new ReciboDAO(cn);

			// =============================
			// VALIDAR TURNO DE CAJA
			// =============================
			validarTurnoParaVenta(1); // Caja Hotel

			// =============================
			// SOLICITAR PAGO
			// =============================
			BigDecimal tarifaRato = parametroDAO.obtenerValor("TARIFA_RATO");

			int opcion = JOptionPane.showConfirmDialog(null, "CLIENTE DEBE PAGAR ".concat(tarifaRato.toString()),
					"HOTEL LAS TERRAZAS II - Pedir Pago...", JOptionPane.YES_NO_OPTION);

			if (opcion != JOptionPane.YES_OPTION) {
				JOptionPane.showMessageDialog(null, "SI EL CLIENTE NO PAGA, NO SE ASIGNA HABITACION!!!",
						"HOTEL LAS TERRAZAS II - No Asigna Habitación...", JOptionPane.WARNING_MESSAGE);

				return;
			}

			// =============================
			// METODO DE PAGO
			// =============================
			Object[] opcionesIconos = {
					new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/efectivo.png")),
					new ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/nequi.png")) };
			int seleccion = JOptionPane.showOptionDialog(null, "CUAL ES EL METODO DE PAGO?",
					"HOTEL LAS TERRAZAS II - Metodo de Pago", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, opcionesIconos, opcionesIconos[0]);

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
			} else {
				if (opcion != JOptionPane.YES_OPTION) {
					JOptionPane.showMessageDialog(null, "DEBE SELECCIONAR METODO DE PAGO!!!",
							"HOTEL LAS TERRAZAS II - No Asigna Habitaci�n...", JOptionPane.WARNING_MESSAGE);
				}
			}

			// =============================
			// CLIENTE VALIDAR O CREAR
			// =============================
			ClienteVO cliente = clienteDAO.buscarPorDocumento("123000");

			// =============================
			// REGISTRAR VENTA
			// =============================
			VentaVO venta = new VentaVO();
			venta.setIdCliente(cliente.getIdCliente());
			venta.setIdHabitacion(habitacion.getIdHabitacion());
			venta.setIdConceptoVenta(ConceptoVentaEnum.VENTA_HABITACION.getCodigo());
			venta.setTotal(tarifaRato);
			venta.setObservacion(habitacion.getNumeroHabitacion().toString().concat(" - RATO"));
			Integer idVenta = ventaDAO.insertar(venta);
			venta.setIdVenta(idVenta);

			// =============================
			// DETALLE
			// =============================
			detalleDAO.insertarDetalleHabitacion(venta, habitacion, 0, tarifaRato);

			// =============================
			// PAGO
			// =============================
			PagoVO pago = new PagoVO();

			pago.setValor(tarifaRato);

			// RECARGO NEQUI
			if (seleccion == MetodoPagoEnum.NEQUI.getCodigo()) {
				ParametroDAO recargDAO = new ParametroDAO(cn);
				BigDecimal recargo = recargDAO.obtenerValor("TRANSACCION_NEQUI");
				if (recargo != null && recargo.compareTo(BigDecimal.ZERO) > 0) {
					pago.setValor(pago.getValor().add(recargo));
				}
			}

			TurnoCajaDAO turnoCajaDAO = new TurnoCajaDAO(cn);
			int idCajaDestino = (ConceptoVentaEnum.VENTA_MOSTRADOR.getCodigo() == venta.getIdConceptoVenta()) ? 2 : 1;
			TurnoCajaVO turnoActivo = turnoCajaDAO.buscarTurnoAbierto(idCajaDestino);
			if (turnoActivo != null) {
				pago.setIdTurnoCaja(turnoActivo.getIdTurnoCaja());
			}

			pago.setIdVenta(idVenta);
			pago.setIdCliente(cliente.getIdCliente());
			pago.setIdHabitacion(habitacion.getIdHabitacion());
			pago.setIdMetodo(seleccion);
			pago.setIdEstadoRegistro(EstadoRegistroEnum.ACTIVO.getCodigo());
			pago.setValor(tarifaRato);
			Integer idPago = pagoDAO.insertar(pago);

			// =============================
			// MOVIMIENTO
			// =============================
			Integer idMov = movDAO.registrarEntrada(habitacion.getIdHabitacion(), cliente.getIdCliente(), 0);

			// =============================
			// CAMBIAR ESTADO
			// =============================
			habitacionDAO.actualizarEstado(habitacion.getIdHabitacion(), EstadoHabitacionEnum.OCUPADA.getCodigo());

			// =============================
			// REGISTRAR RECIBO
			// =============================
			ReciboVO recibo = new ReciboVO();
			recibo.setIdCliente(cliente.getIdCliente());
			recibo.setIdHabitacion(habitacion.getIdHabitacion());
			recibo.setIdPago(idPago);
			recibo.setIdVenta(venta.getIdVenta());
			recibo.setIdHabitacionMovimiento(idMov);
			recibo.setNoches(-1);
			reciboDAO.insertarRecibo(recibo);

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

	// -------------------------------
	// TRANSACTION CHECK IN
	// -------------------------------
	public void doTransactionCheckIn(HabitacionVO habitacion, Integer noches, Integer personas, Integer metodoPago,
			List<PrestamoConsumibleVO> prestamos, BigDecimal auxprecio) throws Exception {

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
			ConsumibleDAO consumibleDAO = new ConsumibleDAO(cn);
			PrestamoConsumibleDAO prestamoDAO = new PrestamoConsumibleDAO(cn);
			ParametroDAO parametroDAO = new ParametroDAO(cn);

			// =============================
			// VALIDAR TURNO DE CAJA
			// =============================
			validarTurnoParaVenta(1); // Caja Hotel

			// =============================
			// PEDIR DOCUMENTO
			// =============================

			if (personas < 1)
				return;

			List<ClienteVO> huespedes = new ArrayList<ClienteVO>();

			for (int i = 0; i < personas; i++) {

				String documento = "";
				boolean validar = true;

				if (i == 0) {

					while (validar) {

						validar = false;

						documento = JOptionPane.showInputDialog(null, "Número Identificación del cliente:",
								"HOTEL LAS TERRAZAS II - Identificación Cliente...", JOptionPane.QUESTION_MESSAGE);

						if (documento != null) {
							documento = documento.trim();
							if (!documento.isEmpty()) {

								if (!documento.matches("\\d+")) {
									JOptionPane.showMessageDialog(null, "El documento solo debe contener números...",
											"HOTEL LAS TERRAZAS II - Número Documento Invalido...",
											JOptionPane.WARNING_MESSAGE);
									validar = true;
								}

							} else
								validar = true;
						} else
							return;
					}

				} else {

					while (validar) {

						validar = false;

						documento = JOptionPane.showInputDialog(null, "Número Identificación de Acompañante " + i + ":",
								"HOTEL LAS TERRAZAS II - Identificación Cliente...", JOptionPane.QUESTION_MESSAGE);

						if (documento != null) {
							documento = documento.trim();
							if (!documento.isEmpty()) {

								if (!documento.matches("\\d+")) {
									JOptionPane.showMessageDialog(null, "El documento solo debe contener números...",
											"HOTEL LAS TERRAZAS II - N�mero Documento Invalido...",
											JOptionPane.WARNING_MESSAGE);
									validar = true;
								}

							} else
								validar = true;
						} else
							return;
					}

				}

				// =============================
				// CLIENTE VALIDAR O CREAR
				// =============================
				ClienteVO cliente = clienteDAO.buscarPorDocumento(documento);
				if (cliente == null) {
					JTextField txtNombre = new JTextField();
					JTextField txtTelefono = new JTextField();
					Object[] campos = { "NOMBRE: ", txtNombre, "TELEFONO: ", txtTelefono };
					int op = JOptionPane.showConfirmDialog(null, campos, "HOTEL LAS TERRAZAS II - Crear Cliente...",
							JOptionPane.OK_CANCEL_OPTION);
					if (op != JOptionPane.OK_OPTION)
						throw new Exception("Registro de cliente cancelado...");
					cliente = new ClienteVO();
					cliente.setDocumento(documento);
					cliente.setNombre(txtNombre.getText());
					cliente.setTelefono(txtTelefono.getText());
					int idGenerado = clienteDAO.insertar(cliente);
					cliente.setIdCliente(idGenerado);
				}
				huespedes.add(cliente);

			}

			if (huespedes.isEmpty())
				return;

			// =============================
			// REGISTRAR VENTA
			// =============================
			VentaVO venta = new VentaVO();
			venta.setIdCliente(huespedes.get(0).getIdCliente());
			venta.setIdHabitacion(habitacion.getIdHabitacion());
			venta.setIdConceptoVenta(ConceptoVentaEnum.VENTA_HABITACION.getCodigo());
			venta.setTotal(new BigDecimal(noches).multiply(auxprecio));
			venta.setObservacion(
					(habitacion.getNumeroHabitacion().toString().concat(" - " + huespedes.size()).concat(" persona(s)"))
							.concat(" - " + noches).concat(" noche(s)"));
			Integer idVenta = ventaDAO.insertar(venta);
			venta.setIdVenta(idVenta);

			// =============================
			// DETALLE
			// =============================
			detalleDAO.insertarDetalleHabitacion(venta, habitacion, noches, auxprecio);

			// =============================
			// PAGO
			// =============================
			PagoVO pago = new PagoVO();

			// RECARGO NEQUI
			BigDecimal valorCheckIn = new BigDecimal(noches).multiply(auxprecio);
			if (metodoPago == MetodoPagoEnum.NEQUI.getCodigo()) {
				BigDecimal recargo = parametroDAO.obtenerValor("TRANSACCION_NEQUI");
				if (recargo != null) {
					valorCheckIn = valorCheckIn.add(recargo);
				}
			}

			pago.setIdVenta(idVenta);
			pago.setIdCliente(huespedes.get(0).getIdCliente());
			pago.setIdHabitacion(habitacion.getIdHabitacion());
			pago.setIdMetodo(metodoPago);
			pago.setIdEstadoRegistro(EstadoRegistroEnum.ACTIVO.getCodigo());
			pago.setValor(valorCheckIn);
			TurnoCajaDAO turnoCajaDAO = new TurnoCajaDAO(cn);
			TurnoCajaVO turnoActivoCheckIn = turnoCajaDAO.buscarTurnoAbierto(1);
			if (turnoActivoCheckIn != null)
				pago.setIdTurnoCaja(turnoActivoCheckIn.getIdTurnoCaja());
			Integer idPago = pagoDAO.insertar(pago);

			// =============================
			// MOVIMIENTO
			// =============================
			Integer idMov = 0;
			for (int i = 0; i < personas; i++) {

				if (i == 0) {
					idMov = movDAO.registrarEntrada(habitacion.getIdHabitacion(), huespedes.get(i).getIdCliente(),
							noches);
				} else {
					movDAO.registrarEntrada(habitacion.getIdHabitacion(), huespedes.get(i).getIdCliente(), noches);
				}

			}

			// =============================
			// PRÉSTAMO DE CONSUMIBLES (NUEVO) CON LOGS
			// =============================
			if (prestamos != null && !prestamos.isEmpty()) {
				for (PrestamoConsumibleVO prestamo : prestamos) {

					if (prestamo != null) {
						prestamo.setIdHabitacionMovimiento(idMov);
						Integer idPrestamo = prestamoDAO.insertar(prestamo);
						prestamo.setIdPrestamo(idPrestamo);

						int idConsumible = (prestamo.getIdConsumible() != null) ? prestamo.getIdConsumible().intValue()
								: 0;
						int cantidadEntregada = (prestamo.getCantidadEntregada() != null)
								? prestamo.getCantidadEntregada().intValue()
								: 0;

						if (idConsumible > 0 && cantidadEntregada > 0) {
							// Ajustar stock (restar)
							consumibleDAO.ajustarStock(idConsumible, -cantidadEntregada);
						}
					}
				}
			}

			// =============================
			// CAMBIAR ESTADO
			// =============================
			habitacionDAO.actualizarEstado(habitacion.getIdHabitacion(), EstadoHabitacionEnum.OCUPADA.getCodigo());

			// =============================
			// REGISTRAR RECIBO
			// =============================
			ReciboVO recibo = new ReciboVO();
			recibo.setIdCliente(huespedes.get(0).getIdCliente());
			recibo.setIdHabitacion(habitacion.getIdHabitacion());
			recibo.setIdPago(idPago);
			recibo.setIdVenta(venta.getIdVenta());
			recibo.setIdHabitacionMovimiento(idMov);
			recibo.setNoches(noches);
			reciboDAO.insertarRecibo(recibo);

			cn.commit();
		} catch (Exception e) {
			e.printStackTrace();

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

	// -------------------------------
	// TRANSACTION CHECK OUT CON MULTAS
	// -------------------------------
	public void doTransactionCheckOut(HabitacionVO habitacion, List<PrestamoConsumibleVO> devoluciones,
			Integer idPagoMulta /* SI HAY MULTAS */ ) throws Exception {

		Connection cn = null;
		try {
			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);

			HabitacionMovimientoDAO movDAO = new HabitacionMovimientoDAO(cn);
			HabitacionDAO habitacionDAO = new HabitacionDAO(cn);
			ConsumibleDAO consumibleDAO = new ConsumibleDAO(cn);
			PrestamoConsumibleDAO prestamoDAO = new PrestamoConsumibleDAO(cn);
			VentaDAO ventaDAO = new VentaDAO(cn);
			PagoDAO pagoDAO = new PagoDAO(cn);
			ParametroDAO parametroDAO = new ParametroDAO(cn);

			// 1. OBTENER EL MOVIMIENTO Y CLIENTE ACTIVO
			Integer idMovimientoActivo = movDAO.obtenerMovimientoActivo(habitacion.getIdHabitacion());
			Integer idClienteTitular = movDAO.obtenerClienteActivo(habitacion.getIdHabitacion());

			if (idMovimientoActivo == null) {
				throw new Exception("No se encontró un movimiento de entrada activo para esta habitación.");
			}

			// =============================
			// VERIFICAR PRÉSTAMOS PENDIENTES
			// =============================
			List<PrestamoConsumibleVO> prestamosPendientes = prestamoDAO
					.buscarPrestamosPendientesPorMovimiento(idMovimientoActivo);

			if (prestamosPendientes != null && !prestamosPendientes.isEmpty()) {
				// Si hay préstamos pendientes pero no se están devolviendo
				if (devoluciones == null || devoluciones.isEmpty()) {
					throw new Exception("¡ATENCIÓN! Hay " + prestamosPendientes.size()
							+ " objeto(s) prestado(s) pendientes de devolución.\n"
							+ "Debe registrar la devolución de todos los objetos antes de hacer check-out.");
				}

				// Verificar que todos los préstamos pendientes estén en las devoluciones
				for (PrestamoConsumibleVO prestamoPendiente : prestamosPendientes) {
					boolean encontrado = false;
					for (PrestamoConsumibleVO devolucion : devoluciones) {
						if (devolucion.getIdPrestamo() != null
								&& devolucion.getIdPrestamo().equals(prestamoPendiente.getIdPrestamo())) {
							encontrado = true;
							break;
						}
					}

					if (!encontrado) {
						throw new Exception("¡ATENCIÓN! El objeto '" + prestamoPendiente.getNombreConsumible() + "' "
								+ "(" + prestamoPendiente.getCantidadEntregada()
								+ " unidades) está pendiente de devolución.\n"
								+ "Debe registrar su devolución antes de hacer check-out.");
					}
				}
			}

			// =============================
			// PROCESAR DEVOLUCIONES DE CONSUMIBLES
			// =============================
			int totalObjetosNoDevueltos = 0;
			List<String> objetosNoDevueltos = new ArrayList<>();

			if (devoluciones != null && !devoluciones.isEmpty()) {
				for (PrestamoConsumibleVO dev : devoluciones) {

					int cantidadDevuelta = dev.getCantidadDevuelta();
					int cantidadPrestada = dev.getCantidadEntregada();
					int faltantes = cantidadPrestada - cantidadDevuelta;

					if (faltantes > 0) {
						dev.setEstado("CON_MULTA");
						totalObjetosNoDevueltos += faltantes;
						objetosNoDevueltos.add(dev.getNombreConsumible() + " (" + faltantes + " unidades)");
					} else {
						dev.setEstado("DEVUELTO");
					}

					prestamoDAO.actualizarDevolucion(dev);

					// Solo regresamos a bodega lo que físicamente devolvieron
					if (cantidadDevuelta > 0) {
						consumibleDAO.ajustarStock(dev.getIdConsumible(), cantidadDevuelta);
					}
				}
			}

			// =============================
			// REGISTRAR MULTA ÚNICA PARAMETRIZADA SI HAY OBJETOS NO DEVUELTOS
			// =============================
			if (totalObjetosNoDevueltos > 0) {

				if (idPagoMulta == null) {
					throw new Exception("Hay " + totalObjetosNoDevueltos
							+ " objeto(s) no devuelto(s), pero no se seleccionó método de pago.");
				}

				// =============================
				// VALIDAR TURNO DE CAJA PARA LA MULTA
				// =============================
				validarTurnoParaVenta(1); // Caja Hotel

				// Obtener valor de multa parametrizado
				BigDecimal valorMultaPorObjeto = parametroDAO.obtenerValor("MULTA_POR_OBJETO");
				if (valorMultaPorObjeto == null || valorMultaPorObjeto.compareTo(BigDecimal.ZERO) <= 0) {
					valorMultaPorObjeto = new BigDecimal("5000.00"); // Valor por defecto
				}

				// Calcular multa total
				BigDecimal totalMulta = valorMultaPorObjeto.multiply(new BigDecimal(totalObjetosNoDevueltos));

				// USAR ID FIJO PARA MULTAS = 3 (según la tabla concepto_venta)
				Integer idConceptoMulta = 3;

				VentaVO ventaMulta = new VentaVO();
				ventaMulta.setIdCliente(idClienteTitular);
				ventaMulta.setIdHabitacion(habitacion.getIdHabitacion());
				ventaMulta.setIdConceptoVenta(idConceptoMulta); // ID 3 = MULTAS
				ventaMulta.setTotal(totalMulta);
				ventaMulta.setObservacion(
						"Multa por " + totalObjetosNoDevueltos + " objeto(s) no devuelto(s) en CheckOut Habitación "
								+ habitacion.getNumeroHabitacion() + " - " + String.join(", ", objetosNoDevueltos));

				Integer idVentaMulta = ventaDAO.insertar(ventaMulta);

				PagoVO pagoMulta = new PagoVO();
				pagoMulta.setIdVenta(idVentaMulta);
				pagoMulta.setIdCliente(idClienteTitular);
				pagoMulta.setIdHabitacion(habitacion.getIdHabitacion());
				pagoMulta.setIdMetodo(idPagoMulta);
				pagoMulta.setIdEstadoRegistro(EstadoRegistroEnum.ACTIVO.getCodigo());
				pagoMulta.setValor(totalMulta);

				// ESTAMPAR TURNO ACTIVO
				TurnoCajaDAO turnoCajaDAOMulta = new TurnoCajaDAO(cn);
				TurnoCajaVO turnoActivoMulta = turnoCajaDAOMulta.buscarTurnoAbierto(1); // CAJA HOTEL
				if (turnoActivoMulta != null)
					pagoMulta.setIdTurnoCaja(turnoActivoMulta.getIdTurnoCaja());

				pagoDAO.insertar(pagoMulta);

				// Mostrar mensaje informativo
				JOptionPane.showMessageDialog(null,
						"Se aplicó multa por " + totalObjetosNoDevueltos + " objeto(s) no devuelto(s).\n"
								+ "Valor por objeto: $" + valorMultaPorObjeto + "\n" + "Multa total: $" + totalMulta
								+ "\n" + "Objetos: " + String.join(", ", objetosNoDevueltos),
						"HOTEL LAS TERRAZAS II - Multa Aplicada", JOptionPane.INFORMATION_MESSAGE);
			}

			// =============================
			// CHECK-OUT ESTANDAR
			// =============================
			movDAO.registrarSalida(habitacion.getIdHabitacion());

			if (habitacionDAO.actualizarEstado(habitacion.getIdHabitacion(),
					EstadoHabitacionEnum.POR_ASEO.getCodigo()) == 0) {
				throw new Exception("No se actualizó estado de habitación.");
			}

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

	// -------------------------------
	// TRANSACTION CHANGE STATE
	// -------------------------------
	public void doTransactionChangeState(HabitacionVO habitacion, Integer state) throws Exception {

		Connection cn = null;
		try {

			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);

			HabitacionDAO habitacionDAO = new HabitacionDAO(cn);
			if (habitacionDAO.actualizarEstado(habitacion.getIdHabitacion(), state) == 0) {
				throw new Exception("No se actualizo estado de habitaci�n.");
			}

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

	// -------------------------------
	// TRANSACTION ADICIONAR PERSONA
	// -------------------------------
	public void doTransactionAdicionarPersona(HabitacionVO habitacion, Integer metodoPago) throws Exception {
		Connection cn = null;
		try {
			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);
			ParametroDAO parametroDAO = new ParametroDAO(cn);
			VentaDAO ventaDAO = new VentaDAO(cn);
			VentaDetalleDAO detalleDAO = new VentaDetalleDAO(cn);
			PagoDAO pagoDAO = new PagoDAO(cn);
			HabitacionMovimientoDAO movDAO = new HabitacionMovimientoDAO(cn);
			ReciboDAO reciboDAO = new ReciboDAO(cn);

			// =============================
			// VALIDAR TURNO DE CAJA
			// =============================
			validarTurnoParaVenta(1); // Caja Hotel

			Integer idClienteTitular = movDAO.obtenerClienteActivo(habitacion.getIdHabitacion());
			if (idClienteTitular == null) {
				throw new Exception("No hay un cliente titular registrado en esta habitación.");
			}

			Integer idMov = movDAO.obtenerMovimientoActivo(habitacion.getIdHabitacion());

			BigDecimal tarifaAdicional = parametroDAO.obtenerValor("TARIFA_PERSONA_ADICIONAL");
			VentaVO venta = new VentaVO();
			venta.setIdCliente(idClienteTitular);
			venta.setIdHabitacion(habitacion.getIdHabitacion());
			venta.setIdConceptoVenta(ConceptoVentaEnum.VENTA_HABITACION.getCodigo());
			venta.setTotal(tarifaAdicional);
			venta.setObservacion("Adicional - ".concat(habitacion.getNumeroHabitacion().toString()));
			Integer idVenta = ventaDAO.insertar(venta);
			venta.setIdVenta(idVenta);
			detalleDAO.insertarDetallePersonaAdicional(venta, habitacion, tarifaAdicional);

			PagoVO pago = new PagoVO();

			// RECARGO NEQUI
			BigDecimal valorAdicional = tarifaAdicional;
			if (metodoPago == MetodoPagoEnum.NEQUI.getCodigo()) {
				ParametroDAO parDAO = new ParametroDAO(cn);
				BigDecimal recargo = parDAO.obtenerValor("TRANSACCION_NEQUI");
				if (recargo != null) {
					valorAdicional = valorAdicional.add(recargo);
				}
			}

			pago.setIdVenta(idVenta);
			pago.setIdCliente(idClienteTitular);
			pago.setIdHabitacion(habitacion.getIdHabitacion());
			pago.setIdMetodo(metodoPago);
			pago.setIdEstadoRegistro(EstadoRegistroEnum.ACTIVO.getCodigo());
			pago.setValor(valorAdicional);

			TurnoCajaDAO turnoCajaDAOAdicional = new TurnoCajaDAO(cn);
			TurnoCajaVO turnoActivoAdicional = turnoCajaDAOAdicional.buscarTurnoAbierto(1);
			if (turnoActivoAdicional != null)
				pago.setIdTurnoCaja(turnoActivoAdicional.getIdTurnoCaja());
			Integer idPago = pagoDAO.insertar(pago);

			ReciboVO recibo = new ReciboVO();
			recibo.setIdCliente(idClienteTitular);
			recibo.setIdHabitacion(habitacion.getIdHabitacion());
			recibo.setIdPago(idPago);
			recibo.setIdVenta(idVenta);

			// Condicional seguro por si es NULL
			if (idMov != null) {
				recibo.setIdHabitacionMovimiento(idMov);
			} else {
				recibo.setIdHabitacionMovimiento(0);
			}

			recibo.setNoches(0);
			reciboDAO.insertarRecibo(recibo);

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

	// -------------------------------
	// PRINT TICKET
	// -------------------------------
	public void doPrintTicket(Integer idHabitacion) throws Exception {
		Connection cn = null;
		try {

			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);

			ReciboDAO reciboDAO = new ReciboDAO(cn);
			Map<String, Object> datosRecibo = reciboDAO.obtenerDatosParaTicket(idHabitacion);

			if (datosRecibo == null) {
				throw new Exception("No se encontr� ning�n recibo registrado para la Habitaci�n Nro: " + idHabitacion);
			}

			PrinterJob job = PrinterJob.getPrinterJob();

			TicketCheckIn ticket = new TicketCheckIn((Integer) datosRecibo.get("id_recibo"),
					datosRecibo.get("id_cliente").toString(), datosRecibo.get("numero_habitacion").toString(),
					(Integer) datosRecibo.get("noches"), (BigDecimal) datosRecibo.get("total"),
					(Timestamp) datosRecibo.get("fecha_emision"), (BigDecimal) datosRecibo.get("precio"));

			PageFormat pf = job.defaultPage();
			Paper paper = new Paper();

			paper.setSize(164, 600);
			paper.setImageableArea(0, 0, 164, 590);
			pf.setPaper(paper);
			pf.setOrientation(PageFormat.PORTRAIT);

			job.setPrintable(ticket, pf);

			try {
				job.print();
			} catch (PrinterException e) {
				throw new Exception("Error fÍsico de impresora: " + e.getMessage());
			}

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

	// -------------------------------
	// PRINT TICKET ADICIONAL
	// -------------------------------
	public void doPrintTicketAdicional(Integer idHabitacion) throws Exception {
		Connection cn = null;
		try {
			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);

			ReciboDAO reciboDAO = new ReciboDAO(cn);
			Map<String, Object> datosRecibo = reciboDAO.obtenerDatosParaTicketAdicional(idHabitacion);

			if (datosRecibo == null) {
				throw new Exception(
						"No se encontro ningun recibo de PERSONA ADICIONAL registrado para esta habitacion.");
			}

			PrinterJob job = PrinterJob.getPrinterJob();

			TicketCheckIn ticket = new TicketCheckIn((Integer) datosRecibo.get("id_recibo"),
					datosRecibo.get("id_cliente").toString(), datosRecibo.get("numero_habitacion").toString(),
					(Integer) datosRecibo.get("noches"), (java.math.BigDecimal) datosRecibo.get("total"),
					(java.sql.Timestamp) datosRecibo.get("fecha_emision"),
					(java.math.BigDecimal) datosRecibo.get("precio"));

			job.setPrintable(ticket);

			if (job.printDialog()) {
				job.print();
			}

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

	// -------------------------------
	// TAKE SHOWER
	// -------------------------------
	public Integer doTransactionShower() throws Exception {
		Connection cn = null;
		try {
			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);
			ParametroDAO parametroDAO = new ParametroDAO(cn);
			ClienteDAO clienteDAO = new ClienteDAO(cn);
			VentaDAO ventaDAO = new VentaDAO(cn);
			PagoDAO pagoDAO = new PagoDAO(cn);
			ReciboDAO reciboDAO = new ReciboDAO(cn);

			// =============================
			// VALIDAR TURNO DE CAJA
			// =============================
			validarTurnoParaVenta(1); // Caja Hotel

			// =============================
			// TARIFA
			// =============================
			BigDecimal tarifaServicio = parametroDAO.obtenerValor("TARIFA_DUCHA");

			// =============================
			// SOLICITAR PAGO
			// =============================
			int opcion = JOptionPane.showConfirmDialog(null, "CLIENTE DEBE PAGAR ".concat(tarifaServicio.toString()),
					"HOTEL LAS TERRAZAS II - Pedir Pago Servicio...", JOptionPane.YES_NO_OPTION);
			if (opcion != JOptionPane.YES_OPTION) {
				JOptionPane.showMessageDialog(null, "SI EL CLIENTE NO PAGA, NO SE PRESTA EL SERVICIO!!!",
						"HOTEL LAS TERRAZAS II - Servicios...", JOptionPane.WARNING_MESSAGE);
				return null;
			}

			// =============================
			// METODO DE PAGO
			// =============================
			Object[] opcionesIconos = {
					new javax.swing.ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/efectivo.png")),
					new javax.swing.ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/nequi.png")) };
			int seleccion = JOptionPane.showOptionDialog(null, "CUAL ES EL METODO DE PAGO?",
					"HOTEL LAS TERRAZAS II - Metodo de Pago", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, opcionesIconos, opcionesIconos[0]);
			if (seleccion == JOptionPane.CLOSED_OPTION)
				return null;
			int metodoPago;
			switch (seleccion) {
			case 1:
				metodoPago = MetodoPagoEnum.NEQUI.getCodigo();
				break;
			default:
				metodoPago = MetodoPagoEnum.EFECTIVO.getCodigo();
				break;
			}

			// =============================
			// CLIENTE GENERICO DUCHA
			// =============================
			ClienteVO cliente = clienteDAO.buscarPorDocumento("456000");

			// =============================
			// REGISTRAR VENTA
			// =============================
			VentaVO venta = new VentaVO();
			venta.setIdCliente(cliente.getIdCliente());
			venta.setIdConceptoVenta(ConceptoVentaEnum.SERVICIO.getCodigo());
			venta.setTotal(tarifaServicio);
			venta.setObservacion("Servicio Ducha - TARIFA DUCHA");
			Integer idVenta = ventaDAO.insertar(venta);
			venta.setIdVenta(idVenta);

			// =============================
			// PAGO
			// =============================
			// RECARGO POR METODO DE PAGO (ej. NEQUI)
			BigDecimal valorFinal = tarifaServicio;
			if (metodoPago != MetodoPagoEnum.EFECTIVO.getCodigo()) {
			    BigDecimal recargo = parametroDAO.obtenerValor("TRANSACCION_NEQUI");
			    if (recargo != null) {
			        valorFinal = valorFinal.add(recargo);
			    }
			}
			PagoVO pago = new PagoVO();
			pago.setIdVenta(idVenta);
			pago.setIdCliente(cliente.getIdCliente());
			pago.setIdMetodo(metodoPago);
			pago.setIdEstadoRegistro(EstadoRegistroEnum.ACTIVO.getCodigo());
			pago.setValor(valorFinal);
			// ESTAMPAR TURNO ACTIVO
			TurnoCajaDAO turnoCajaDAOShower = new TurnoCajaDAO(cn);
			TurnoCajaVO turnoActivoShower = turnoCajaDAOShower.buscarTurnoAbierto(1);
			if (turnoActivoShower != null)
				pago.setIdTurnoCaja(turnoActivoShower.getIdTurnoCaja());
			Integer idPago = pagoDAO.insertar(pago);

			// =============================
			// RECIBO
			// =============================
			Integer idRecibo = reciboDAO.insertarReciboMostrador(idVenta, idPago);
			cn.commit();
			return idRecibo;

		} catch (Exception e) {
			if (cn != null)
				try {
					cn.rollback();
				} catch (Exception ignored) {
				}
			throw e;
		} finally {
			if (cn != null)
				try {
					cn.setAutoCommit(true);
					cn.close();
				} catch (Exception ignored) {
				}
		}
	}

	//-------------------------------
	//BATHROOM
	//-------------------------------
	public Integer doTransactionBathroom() throws Exception {
		Connection cn = null;
		try {
			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);
			ParametroDAO parametroDAO = new ParametroDAO(cn);
			ClienteDAO clienteDAO = new ClienteDAO(cn);
			VentaDAO ventaDAO = new VentaDAO(cn);
			PagoDAO pagoDAO = new PagoDAO(cn);
			ReciboDAO reciboDAO = new ReciboDAO(cn);

			// =============================
			// VALIDAR TURNO DE CAJA
			// =============================
			validarTurnoParaVenta(1); // Caja Hotel

			// =============================
			// TARIFA
			// =============================
			BigDecimal tarifaServicio = parametroDAO.obtenerValor("TARIFA_BANO");

			// =============================
			// SOLICITAR PAGO
			// =============================
			int opcion = JOptionPane.showConfirmDialog(null, "CLIENTE DEBE PAGAR ".concat(tarifaServicio.toString()),
					"HOTEL LAS TERRAZAS II - Pedir Pago Servicio...", JOptionPane.YES_NO_OPTION);
			if (opcion != JOptionPane.YES_OPTION) {
				JOptionPane.showMessageDialog(null, "SI EL CLIENTE NO PAGA, NO SE PRESTA EL SERVICIO!!!",
						"HOTEL LAS TERRAZAS II - Servicios...", JOptionPane.WARNING_MESSAGE);
				return null;
			}

			// =============================
			// METODO DE PAGO
			// =============================
			Object[] opcionesIconos = {
					new javax.swing.ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/efectivo.png")),
					new javax.swing.ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/nequi.png")) };
			int seleccion = JOptionPane.showOptionDialog(null, "CUAL ES EL METODO DE PAGO?",
					"HOTEL LAS TERRAZAS II - Metodo de Pago", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, opcionesIconos, opcionesIconos[0]);
			if (seleccion == JOptionPane.CLOSED_OPTION)
				return null;
			int metodoPago;
			switch (seleccion) {
			case 1:
				metodoPago = MetodoPagoEnum.NEQUI.getCodigo();
				break;
			default:
				metodoPago = MetodoPagoEnum.EFECTIVO.getCodigo();
				break;
			}

			// =============================
			// CLIENTE GENERICO BANO
			// =============================
			ClienteVO cliente = clienteDAO.buscarPorDocumento("789000");

			// =============================
			// REGISTRAR VENTA
			// =============================
			VentaVO venta = new VentaVO();
			venta.setIdCliente(cliente.getIdCliente());
			venta.setIdConceptoVenta(ConceptoVentaEnum.SERVICIO.getCodigo());
			venta.setTotal(tarifaServicio);
			venta.setObservacion("Servicio Baño - TARIFA BANO");
			Integer idVenta = ventaDAO.insertar(venta);
			venta.setIdVenta(idVenta);

			// =============================
			// PAGO
			// =============================
			// RECARGO POR METODO DE PAGO (ej. NEQUI)
			BigDecimal valorFinal = tarifaServicio;
			if (metodoPago != MetodoPagoEnum.EFECTIVO.getCodigo()) {
			    BigDecimal recargo = parametroDAO.obtenerValor("TRANSACCION_NEQUI");
			    if (recargo != null) {
			        valorFinal = valorFinal.add(recargo);
			    }
			}
			PagoVO pago = new PagoVO();
			pago.setIdVenta(idVenta);
			pago.setIdCliente(cliente.getIdCliente());
			pago.setIdMetodo(metodoPago);
			pago.setIdEstadoRegistro(EstadoRegistroEnum.ACTIVO.getCodigo());
			pago.setValor(valorFinal);
			// ESTAMPAR TURNO ACTIVO
			TurnoCajaDAO turnoCajaDAOBano = new TurnoCajaDAO(cn);
			TurnoCajaVO turnoActivoBano = turnoCajaDAOBano.buscarTurnoAbierto(1);
			if (turnoActivoBano != null)
				pago.setIdTurnoCaja(turnoActivoBano.getIdTurnoCaja());
			Integer idPago = pagoDAO.insertar(pago);

			// =============================
			// RECIBO
			// =============================
			Integer idRecibo = reciboDAO.insertarReciboMostrador(idVenta, idPago);
			cn.commit();
			return idRecibo;

		} catch (Exception e) {
			if (cn != null)
				try {
					cn.rollback();
				} catch (Exception ignored) {
				}
			throw e;
		} finally {
			if (cn != null)
				try {
					cn.setAutoCommit(true);
					cn.close();
				} catch (Exception ignored) {
				}
		}
	}

	//-------------------------------
	//LAVANDERIA
	//-------------------------------
	public Integer doTransactionLaundry() throws Exception {
		Connection cn = null;
		try {
			cn = DBConnection.getConnection();
			cn.setAutoCommit(false);
			ParametroDAO parametroDAO = new ParametroDAO(cn);
			ClienteDAO clienteDAO = new ClienteDAO(cn);
			VentaDAO ventaDAO = new VentaDAO(cn);
			PagoDAO pagoDAO = new PagoDAO(cn);
			ReciboDAO reciboDAO = new ReciboDAO(cn);

			// =============================
			// VALIDAR TURNO DE CAJA
			// =============================
			validarTurnoParaVenta(1); // Caja Hotel

			// =============================
			// TARIFA
			// =============================
			BigDecimal tarifaServicio = parametroDAO.obtenerValor("TARIFA_LAVANDERIA");
			if (tarifaServicio == null || tarifaServicio.compareTo(BigDecimal.ZERO) <= 0) {
				throw new Exception("No se encontró la tarifa TARIFA_LAVANDERIA en los parámetros del sistema.");
			}

			// =============================
			// SOLICITAR PAGO
			// =============================
			int opcion = JOptionPane.showConfirmDialog(null, "CLIENTE DEBE PAGAR ".concat(tarifaServicio.toString()),
					"HOTEL LAS TERRAZAS II - Pedir Pago Servicio...", JOptionPane.YES_NO_OPTION);
			if (opcion != JOptionPane.YES_OPTION) {
				JOptionPane.showMessageDialog(null, "SI EL CLIENTE NO PAGA, NO SE PRESTA EL SERVICIO!!!",
						"HOTEL LAS TERRAZAS II - Servicios...", JOptionPane.WARNING_MESSAGE);
				return null;
			}

			// =============================
			// METODO DE PAGO
			// =============================
			Object[] opcionesIconos = {
					new javax.swing.ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/efectivo.png")),
					new javax.swing.ImageIcon(getClass().getResource("/com/adminHotel/gui/complements/nequi.png")) };
			int seleccion = JOptionPane.showOptionDialog(null, "CUAL ES EL METODO DE PAGO?",
					"HOTEL LAS TERRAZAS II - Metodo de Pago", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, opcionesIconos, opcionesIconos[0]);
			if (seleccion == JOptionPane.CLOSED_OPTION)
				return null;
			int metodoPago;
			switch (seleccion) {
			case 1:
				metodoPago = MetodoPagoEnum.NEQUI.getCodigo();
				break;
			default:
				metodoPago = MetodoPagoEnum.EFECTIVO.getCodigo();
				break;
			}

			// =============================
			// CLIENTE GENERICO LAVANDERIA
			// =============================
			ClienteVO cliente = clienteDAO.buscarPorDocumento("987000");

			// =============================
			// REGISTRAR VENTA
			// =============================
			VentaVO venta = new VentaVO();
			venta.setIdCliente(cliente.getIdCliente());
			venta.setIdConceptoVenta(ConceptoVentaEnum.SERVICIO.getCodigo());
			venta.setTotal(tarifaServicio);
			venta.setObservacion("Servicio Lavanderia - TARIFA LAVANDERIA");
			Integer idVenta = ventaDAO.insertar(venta);
			venta.setIdVenta(idVenta);

			// =============================
			// PAGO
			// =============================
			// RECARGO POR METODO DE PAGO (ej. NEQUI)
			BigDecimal valorFinal = tarifaServicio;
			if (metodoPago != MetodoPagoEnum.EFECTIVO.getCodigo()) {
			    BigDecimal recargo = parametroDAO.obtenerValor("TRANSACCION_NEQUI");
			    if (recargo != null) {
			        valorFinal = valorFinal.add(recargo);
			    }
			}
			PagoVO pago = new PagoVO();
			pago.setIdVenta(idVenta);
			pago.setIdCliente(cliente.getIdCliente());
			pago.setIdMetodo(metodoPago);
			pago.setIdEstadoRegistro(EstadoRegistroEnum.ACTIVO.getCodigo());
			pago.setValor(valorFinal);
			// ESTAMPAR TURNO ACTIVO
			TurnoCajaDAO turnoCajaDAOLav = new TurnoCajaDAO(cn);
			TurnoCajaVO turnoActivoLav = turnoCajaDAOLav.buscarTurnoAbierto(1);
			if (turnoActivoLav != null)
				pago.setIdTurnoCaja(turnoActivoLav.getIdTurnoCaja());
			Integer idPago = pagoDAO.insertar(pago);

			// =============================
			// RECIBO
			// =============================
			Integer idRecibo = reciboDAO.insertarReciboMostrador(idVenta, idPago);
			cn.commit();
			return idRecibo;

		} catch (Exception e) {
			if (cn != null)
				try {
					cn.rollback();
				} catch (Exception ignored) {
				}
			throw e;
		} finally {
			if (cn != null)
				try {
					cn.setAutoCommit(true);
					cn.close();
				} catch (Exception ignored) {
				}
		}
	}

	// -------------------------------
	// VERIFICAR TURNO ACTIVO DE CAJA
	// Retorna el turno abierto de la caja solicitada.
	// Retorna null si la caja está CERRADA.
	// idCaja = 1 → Caja Hotel | idCaja = 2 → Caja Mostrador
	// -------------------------------
	public TurnoCajaVO doGetTurnoActivo(Integer idCaja) throws Exception {

		Connection cn = null;

		try {
			cn = DBConnection.getConnection();
			TurnoCajaDAO turnoDAO = new TurnoCajaDAO(cn);
			return turnoDAO.buscarTurnoAbierto(idCaja);

		} catch (Exception e) {
			throw e;
		} finally {
			if (cn != null) {
				try {
					cn.close();
				} catch (Exception ignored) {
				}
			}
		}
	}

	// -------------------------------
	// VALIDAR TURNO PARA VENTA
	// -------------------------------
	/**
	 * Valida si se puede registrar una venta en la caja especificada.
	 * 
	 * @param idCaja 1=Caja Hotel, 2=Caja Mostrador
	 * @throws Exception Si no se puede registrar venta (turno cerrado, vencido,
	 *                   etc.)
	 */
	public void validarTurnoParaVenta(Integer idCaja) throws Exception {
		OperationBoxService boxService = new OperationBoxService();
		TurnoSchedulerService scheduler = new TurnoSchedulerService();

		// 1. Verificar si se puede registrar venta
		if (!boxService.sePuedeRegistrarVenta(idCaja)) {

			// 2. Determinar el motivo específico
			Connection cn = null;
			try {
				cn = DBConnection.getConnection();
				TurnoCajaDAO turnoDAO = new TurnoCajaDAO(cn);

				// Verificar si hay turno abierto
				TurnoCajaVO turno = turnoDAO.buscarTurnoAbierto(idCaja);

				if (turno == null) {
					throw new Exception("¡NO SE PUEDEN REGISTRAR VENTAS!\n" + "No hay turno de caja abierto.\n"
							+ "Abra un turno en el módulo de Caja Registradora.");
				}

				// Verificar si el turno está vencido
				if (scheduler.hayTurnosVencidos()) {
					String nombreCaja = (idCaja == 1) ? "Hotel" : "Mostrador";
					throw new Exception("¡NO SE PUEDEN REGISTRAR VENTAS!\n" + "El turno de caja " + nombreCaja
							+ " está vencido (7AM/7PM).\n"
							+ "Debe realizar el cierre en el módulo de Caja Registradora.");
				}

				// Verificar si es cierre forzado
				if (turno.getForzadoCierre() != null && turno.getForzadoCierre() == 1) {
					throw new Exception(
							"¡NO SE PUEDEN REGISTRAR VENTAS!\n" + "El turno tiene cierre forzado pendiente.\n"
									+ "Realice el cierre completo en el módulo de Caja Registradora.");
				}

				// Motivo desconocido
				throw new Exception(
						"¡NO SE PUEDEN REGISTRAR VENTAS!\n" + "Estado del turno no válido para registrar ventas.");

			} finally {
				if (cn != null)
					try {
						cn.close();
					} catch (Exception ignored) {
					}
			}
		}
	}

	// ==========================================================
	// CAMBIO DE HABITACIÓN CON EXCEDENTE FORZOSO
	// ==========================================================
	public void doTransactionCambioHabitacion(int idHabVieja, int idHabNueva, java.math.BigDecimal excedente,
			Integer idMetodoPago) throws Exception {
		java.sql.Connection cn = null;
		try {
			cn = com.adminHotel.util.DBConnection.getConnection();
			cn.setAutoCommit(false); // Iniciamos transacción atómica
			// 0. Validar Caja si hay dinero de por medio
			TurnoCajaDAO turnoDAO = new TurnoCajaDAO(cn);
			TurnoCajaVO turnoActual = turnoDAO.buscarTurnoAbierto(1); // 1 = CAJA HOTEL

			Integer idTurnoCajaActivo = (turnoActual != null) ? turnoActual.getIdTurnoCaja() : null;

			if (excedente != null && excedente.compareTo(java.math.BigDecimal.ZERO) > 0) {
				if (idTurnoCajaActivo == null) {
					throw new Exception(
							"Debe abrir la CAJA HOTEL (turno activo) para poder registrar el pago del excedente.");
				}
			}
			// 1. Obtener datos clave de la estadía vieja
			String sqlMovActivo = "SELECT id_habitacion_movimiento, id_cliente FROM habitacion_movimiento "
					+ "WHERE id_habitacion = ? AND id_estado_registro = 1";

			Integer idMovimientoActivo = null;
			Integer idCliente = null;
			try (java.sql.PreparedStatement psMov = cn.prepareStatement(sqlMovActivo)) {
				psMov.setInt(1, idHabVieja);
				try (java.sql.ResultSet rsMov = psMov.executeQuery()) {
					if (rsMov.next()) {
						idMovimientoActivo = rsMov.getInt("id_habitacion_movimiento");
						idCliente = rsMov.getInt("id_cliente");
					}
				}
			}
			if (idMovimientoActivo == null)
				throw new Exception("No hay un check-in activo en el origen.");
			// 2. Si hay excedente, inyectar Venta y Pago directamente hacia la NUEVA
			// HABITACIÓN
			if (excedente != null && excedente.compareTo(java.math.BigDecimal.ZERO) > 0 && idMetodoPago != null) {
				com.adminHotel.dao.VentaDAO ventaDAO = new com.adminHotel.dao.VentaDAO(cn);
				com.adminHotel.dao.PagoDAO pagoDAO = new com.adminHotel.dao.PagoDAO(cn);
				// Generar la venta de Alojamiento (Concepto 1)
				com.adminHotel.vo.VentaVO venta = new com.adminHotel.vo.VentaVO();
				venta.setIdCliente(idCliente);
				venta.setIdHabitacion(idHabNueva);
				venta.setIdConceptoVenta(1); // 1 = Alojamiento
				venta.setTotal(excedente);
				venta.setObservacion("EXCEDENTE X CAMBIO HABITACIÓN (Hacia Pieza " + idHabNueva + ")");
				int idVenta = ventaDAO.insertar(venta);
				// Generar el Pago para que cuadre en la CAJA
				com.adminHotel.vo.PagoVO pago = new com.adminHotel.vo.PagoVO();
				pago.setIdVenta(idVenta);
				// pago.setIdCliente(idCliente); //(si aplica)
				pago.setIdTurnoCaja(idTurnoCajaActivo);
				pago.setIdMetodo(idMetodoPago);
				pago.setValor(excedente);
				// --- LA LÍNEA MÁGICA QUE EVITA EL ERROR NULL ---
				pago.setIdEstadoRegistro(1);
				pagoDAO.insertar(pago);
			}
			// 3. Trasladar el movimiento principal
			String sqlUpdMov = "UPDATE habitacion_movimiento SET id_habitacion = ? WHERE id_habitacion_movimiento = ?";
			try (java.sql.PreparedStatement ps = cn.prepareStatement(sqlUpdMov)) {
				ps.setInt(1, idHabNueva);
				ps.setInt(2, idMovimientoActivo);
				ps.executeUpdate();
			}
			// 4. Trasladar historiales físicos y cajeros
			String sqlUpdVenta = "UPDATE venta SET id_habitacion = ? WHERE id_habitacion = ? AND id_estado_registro = 1";
			try (java.sql.PreparedStatement ps = cn.prepareStatement(sqlUpdVenta)) {
				ps.setInt(1, idHabNueva);
				ps.setInt(2, idHabVieja);
				ps.executeUpdate();
			}
			// 5. Cambiar estados limpios
			com.adminHotel.dao.HabitacionDAO habDao = new com.adminHotel.dao.HabitacionDAO(cn);
			habDao.actualizarEstado(idHabVieja, 3); // 3 = Por aseo
			habDao.actualizarEstado(idHabNueva, 2); // 2 = Ocupada
			cn.commit();
		} catch (Exception e) {
			try {
				if (cn != null)
					cn.rollback();
			} catch (Exception ex) {
			}
			throw new Exception("Error en proceso de cambio: " + e.getMessage());
		} finally {
			try {
				if (cn != null)
					cn.close();
			} catch (Exception ex) {
			}
		}
	}
	
	// -------------------------------
	// RESERVAR HABITACION
	// -------------------------------
	public void doTransactionReservar(HabitacionVO habitacion, String nombreReserva) throws Exception {
	    Connection cn = null;
	    try {
	        cn = DBConnection.getConnection();
	        cn.setAutoCommit(false);
	        HabitacionDAO habitacionDAO = new HabitacionDAO(cn);
	        // 1. Verificar que sigue disponible
	        if (habitacionDAO.actualizarEstado(habitacion.getIdHabitacion(),
	                EstadoHabitacionEnum.RESERVADA.getCodigo()) == 0) {
	            throw new Exception("No se pudo actualizar el estado de la habitación.");
	        }
	        // 2. Registrar la reserva
	        String sql = "INSERT INTO reserva (id_habitacion, nombre_reserva, id_estado_registro) VALUES (?, ?, 1)";
	        try (java.sql.PreparedStatement ps = cn.prepareStatement(sql)) {
	            ps.setInt(1, habitacion.getIdHabitacion());
	            ps.setString(2, nombreReserva);
	            ps.executeUpdate();
	        }
	        cn.commit();
	    } catch (Exception e) {
	        if (cn != null) try { cn.rollback(); } catch (Exception ignored) {}
	        throw e;
	    } finally {
	        if (cn != null) try {
	            cn.setAutoCommit(true);
	            cn.close();
	        } catch (Exception ignored) {}
	    }
	}
	
	// -------------------------------
	// CANCELAR RESERVA
	// -------------------------------
	public void doTransactionCancelarReserva(HabitacionVO habitacion) throws Exception {
	    Connection cn = null;
	    try {
	        cn = DBConnection.getConnection();
	        cn.setAutoCommit(false);
	        HabitacionDAO habitacionDAO = new HabitacionDAO(cn);
	        // 1. Marcar la reserva como inactiva
	        String sql = "UPDATE reserva SET id_estado_registro = 2 " +
	                     "WHERE id_habitacion = ? AND id_estado_registro = 1";
	        try (java.sql.PreparedStatement ps = cn.prepareStatement(sql)) {
	            ps.setInt(1, habitacion.getIdHabitacion());
	            ps.executeUpdate();
	        }
	        // 2. Devolver la habitación a DISPONIBLE
	        if (habitacionDAO.actualizarEstado(habitacion.getIdHabitacion(),
	                EstadoHabitacionEnum.DISPONIBLE.getCodigo()) == 0) {
	            throw new Exception("No se pudo actualizar el estado de la habitación.");
	        }
	        cn.commit();
	    } catch (Exception e) {
	        if (cn != null) try { cn.rollback(); } catch (Exception ignored) {}
	        throw e;
	    } finally {
	        if (cn != null) try {
	            cn.setAutoCommit(true);
	            cn.close();
	        } catch (Exception ignored) {}
	    }
	}
}