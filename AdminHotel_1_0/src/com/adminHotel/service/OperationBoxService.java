package com.adminHotel.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import com.adminHotel.dao.CajaDAO;
import com.adminHotel.dao.GastoCajaDAO;
import com.adminHotel.dao.ParametroDAO;
import com.adminHotel.dao.PrestamoConsumibleDAO;
import com.adminHotel.dao.TurnoCajaDAO;
import com.adminHotel.dao.TurnoCajaDetalleDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.EstadoRegistroEnum;
import com.adminHotel.util.SesionUsuario;
import com.adminHotel.vo.CajaVO;
import com.adminHotel.vo.GastoCajaVO;
import com.adminHotel.vo.PrestamoConsumibleVO;
import com.adminHotel.vo.TurnoCajaDetalleVO;
import com.adminHotel.vo.TurnoCajaVO;

public class OperationBoxService {
	
	// -----------------------------------------------
	// ABRIR CAJA  — versión corregida
	// El idUsuario lo toma de la sesión activa,
	// igual que el resto del sistema.
	// -----------------------------------------------
	public TurnoCajaVO doOpenBox(Integer idCaja) throws Exception {
		
		Connection cn = null;
	    
	    try {
	        cn = DBConnection.getConnection();
	        cn.setAutoCommit(false);
	        
	        TurnoCajaDAO turnoDAO = new TurnoCajaDAO(cn);
	        ParametroDAO parametroDAO = new ParametroDAO(cn);
	        CajaDAO cajaDAO = new CajaDAO(cn);
	        
	        // 1. VERIFICAR TURNO YA ABIERTO
	        TurnoCajaVO turnoActivo = turnoDAO.buscarTurnoAbierto(idCaja);
	        if (turnoActivo != null) {
	            throw new Exception("La caja ya tiene un turno ABIERTO. Debe cerrarlo antes de abrir uno nuevo.");
	        }
	        
	        // 2. OBTENER CAJA
	        CajaVO caja = cajaDAO.buscarPorId(idCaja);
	        if (caja == null) {
	            throw new Exception("No se encontró la caja con ID: " + idCaja);
	        }
	        
	        // 3. BASE INICIAL DESDE PARÁMETROS
	        String claveParametro = "BASE_" + caja.getNombre().replace(" ", "_");
	        BigDecimal baseInicial = parametroDAO.obtenerValor(claveParametro);
	        if (baseInicial == null) {
	            throw new Exception("No se encontró el parámetro de base para: " + claveParametro);
	        }
	        
	        // 4. DETERMINAR TIPO DE TURNO (diurno/nocturno)
	        TurnoSchedulerService scheduler = new TurnoSchedulerService();
	        boolean esDiurno = scheduler.esTurnoDiurnoActual();
	        
	        // 5. CREAR TURNO
	        TurnoCajaVO nuevoTurno = new TurnoCajaVO();
	        nuevoTurno.setIdCaja(idCaja);
	        nuevoTurno.setIdUsuario(SesionUsuario.getIdUsuario());
	        nuevoTurno.setBaseInicialEfectivo(baseInicial);
	        nuevoTurno.setTurnoDiurno(esDiurno ? 1 : 0); // NUEVO CAMPO
	        nuevoTurno.setForzadoCierre(0); // NUEVO CAMPO - default no forzado
	        nuevoTurno.setEstado("ABIERTA");
	        nuevoTurno.setIdEstadoRegistro(EstadoRegistroEnum.ACTIVO.getCodigo());
	        
	        Integer idTurnoGenerado = turnoDAO.abrir(nuevoTurno);
	        nuevoTurno.setIdTurnoCaja(idTurnoGenerado);
	        nuevoTurno.setNombreCaja(caja.getNombre());
	        
	        cn.commit();
	        return nuevoTurno;
	    } catch (Exception e) {
	        if (cn != null) { try { cn.rollback(); } catch (Exception ignored) {} }
	        throw e;
	    } finally {
	        if (cn != null) {
	            try { cn.setAutoCommit(true); cn.close(); } catch (Exception ignored) {}
	        }
	    }
	}
    
    // -----------------------------------------------
    // CERRAR CAJA
    // Consolida los totales por método de pago,
    // guarda el detalle del cuadre y cierra el turno.
    // -----------------------------------------------
    public void doCloseBox(
            Integer idTurnoCaja,
            List<TurnoCajaDetalleVO> detallesCuadre,
            String observacionCierre) throws Exception {
    	
        Connection cn = null;
        
        try {
            cn = DBConnection.getConnection();
            cn.setAutoCommit(false);
            
            TurnoCajaDAO       turnoDAO  = new TurnoCajaDAO(cn);
            TurnoCajaDetalleDAO detalleDAO = new TurnoCajaDetalleDAO(cn);
            GastoCajaDAO       gastoDAO  = new GastoCajaDAO(cn);
            
            // 1. VERIFICAR QUE EL TURNO ESTÉ ABIERTO
            TurnoCajaVO turno = turnoDAO.buscarTurnoPorId(idTurnoCaja);
            if (turno == null || !"ABIERTA".equals(turno.getEstado())) {
                throw new Exception("El turno no existe o ya fue cerrado.");
            }
            
            // 2. PARA CADA MÉTODO DE PAGO: CALCULAR DIFERENCIA Y GUARDAR DETALLE
            //    La GUI envía en 'detallesCuadre' lo que el recepcionista declaró (montoReal)
            //    y el totalIngresos calculado automáticamente de los pagos del turno.
            for (TurnoCajaDetalleVO detalle : detallesCuadre) {
                detalle.setIdTurnoCaja(idTurnoCaja);
                // El monto esperado para EFECTIVO = base + ingresos - egresos
                // El monto esperado para NEQUI   = solo ingresos (no hay billetes físicos)
                BigDecimal montoEsperado;
                if (esMetodoEfectivo(detalle.getIdMetodo())) {
                    // Para efectivo sumamos la base inicial y restamos los gastos de caja
                    BigDecimal totalEgresos = gastoDAO.sumarGastosPorTurno(idTurnoCaja);
                    detalle.setTotalEgresos(totalEgresos);
                    montoEsperado = turno.getBaseInicialEfectivo()
                                        .add(detalle.getTotalIngresos())
                                        .subtract(totalEgresos);
                } else {
                    // Para Nequi u otros digitales: solo los ingresos recibidos
                    detalle.setTotalEgresos(BigDecimal.ZERO);
                    montoEsperado = detalle.getTotalIngresos();
                }
                detalle.setMontoEsperado(montoEsperado);
                // Diferencia: positivo = sobrante, negativo = faltante
                BigDecimal diferencia = detalle.getMontoReal().subtract(montoEsperado);
                detalle.setDiferencia(diferencia);
                detalleDAO.insertar(detalle);
            }
            
            // 3. CERRAR EL TURNO EN LA CABECERA
            TurnoCajaVO turnoParaCerrar = new TurnoCajaVO();
            turnoParaCerrar.setIdTurnoCaja(idTurnoCaja);
            turnoParaCerrar.setObservacionCierre(observacionCierre);
            if (observacionCierre != null && observacionCierre.contains("FORZADO")) {
                turnoParaCerrar.setForzadoCierre(1);
            } else {
                turnoParaCerrar.setForzadoCierre(0);
            }
            turnoDAO.cerrar(turnoParaCerrar);
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
    
    // -----------------------------------------------
    // REGISTRAR GASTO DE CAJA
    // Registra un retiro de efectivo del cajón durante el turno.
    // -----------------------------------------------
    public void doRegisterExpense(GastoCajaVO gasto) throws Exception {
    	
        Connection cn = null;
        
        try {
        	
            cn = DBConnection.getConnection();
            cn.setAutoCommit(false);
            
            TurnoCajaDAO turnoDAO = new TurnoCajaDAO(cn);
            GastoCajaDAO gastoDAO = new GastoCajaDAO(cn);
            
            // 1. VERIFICAR QUE EL TURNO ESTÉ ABIERTO
            TurnoCajaVO turno = turnoDAO.buscarTurnoPorId(gasto.getIdTurnoCaja());
            if (turno == null || !"ABIERTA".equals(turno.getEstado())) {
                throw new Exception("No hay un turno de caja abierto para registrar el gasto.");
            }
            
            // 2. VALIDAR QUE EL VALOR SEA POSITIVO
            if (gasto.getValor() == null || gasto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new Exception("El valor del gasto debe ser mayor a cero.");
            }
            
            gasto.setIdUsuario(SesionUsuario.getIdUsuario());
            
            gasto.setIdEstadoRegistro(EstadoRegistroEnum.ACTIVO.getCodigo());
            
            gastoDAO.insertar(gasto);
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
    
    // -----------------------------------------------
    // CONSULTAR ACUMULADOS EN TIEMPO REAL
    // Retorna los 4 cuadrantes de la pantalla de control
    // de caja (Efectivo Hotel, Nequi Hotel, Efectivo Mostrador, Nequi Mostrador)
    // -----------------------------------------------
    public List<TurnoCajaDetalleVO> doGetLiveAccumulados() throws Exception {
    	
        Connection cn = null;
        
        try {
            cn = DBConnection.getConnection();
            TurnoCajaDetalleDAO detalleDAO = new TurnoCajaDetalleDAO(cn);
            return detalleDAO.listarAcumuladosPorTurnosAbiertos();
        } catch (Exception e) {
            throw e;
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (Exception ignored) {}
            }
        }
    }
    
    // -----------------------------------------------
    // OBTENER TURNO ACTIVO DE UNA CAJA
    // La GUI usará esto para saber si debe pedir abrir o mostrar el panel activo.
    // -----------------------------------------------
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
                try { cn.close(); } catch (Exception ignored) {}
            }
        }
    }
    
    // -----------------------------------------------
    // HELPER PRIVADO
    // Determina si un método de pago es efectivo físico.
    // Usamos el ID 1 que corresponde a EFECTIVO en la BD.
    // -----------------------------------------------
    private boolean esMetodoEfectivo(Integer idMetodo) {
        return idMetodo != null && idMetodo == 1;
    }
    
    // -----------------------------------------------
    // OBTENER TODOS LOS PRÉSTAMOS EN CURSO
    // Para mostrar en el tablero de la caja registradora
    // -----------------------------------------------
    public List<PrestamoConsumibleVO> doGetPrestamosActivos() throws Exception {
        Connection cn = null;
        try {
            cn = DBConnection.getConnection();
            PrestamoConsumibleDAO dao = new PrestamoConsumibleDAO(cn);
            return dao.listarTodosLosActivos();
        } catch (Exception e) {
            throw e;
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (Exception ignored) {}
            }
        }
    }
    
    // -----------------------------------------------
    // OBTENER VENTAS DETALLADAS EN TIEMPO REAL
    // Retorna los ingresos 1 a 1 para listar en las tablas
    // -----------------------------------------------
    public List<TurnoCajaDetalleVO> doGetLiveVentasDetalle() throws Exception {
        Connection cn = null;
        try {
            cn = DBConnection.getConnection();
            TurnoCajaDetalleDAO detalleDAO = new TurnoCajaDetalleDAO(cn);
            return detalleDAO.listarVentasDetalladasPorTurnosAbiertos();
        } catch (Exception e) {
            throw e;
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (Exception ignored) {}
            }
        }
    }
    
    // -----------------------------------------------
    // OBTENER RESUMEN DE ARTÍCULOS PRESTABLES (TARJETAS)
    // -----------------------------------------------
    public List<com.adminHotel.vo.ConsumibleVO> doGetPrestablesConEstado() throws Exception {
        Connection cn = null;
        try {
            cn = DBConnection.getConnection();
            com.adminHotel.dao.ConsumibleDAO dao = new com.adminHotel.dao.ConsumibleDAO(cn);
            return dao.listarPrestablesConEstado();
        } catch (Exception e) {
            throw e;
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (Exception ignored) {}
            }
        }
    }
    
	 // -----------------------------------------------
	 // VERIFICAR SI SE PUEDEN REGISTRAR VENTAS
	 // -----------------------------------------------
	 /**
	  * Verifica si se pueden registrar ventas en una caja específica.
	  * @param idCaja 1=Caja Hotel, 2=Caja Mostrador
	  * @return true si se pueden registrar ventas, false en caso contrario
	  */
	 public boolean sePuedeRegistrarVenta(Integer idCaja) throws Exception {
	     Connection cn = null;
	     try {
	         cn = DBConnection.getConnection();
	         TurnoCajaDAO turnoDAO = new TurnoCajaDAO(cn);
	         TurnoSchedulerService scheduler = new TurnoSchedulerService();
	         
	         // 1. Verificar si hay turno abierto
	         TurnoCajaVO turno = turnoDAO.buscarTurnoAbierto(idCaja);
	         if (turno == null) {
	             return false; // No hay turno abierto
	         }
	         
	         // 2. Verificar si el turno está vencido
	         if (scheduler.hayTurnosVencidos()) {
	             return false; // Turno vencido, no permitir ventas
	         }
	         
	         // 3. Verificar si es cierre forzado
	         if (turno.getForzadoCierre() != null && turno.getForzadoCierre() == 1) {
	             return false; // Cierre forzado pendiente
	         }
	         
	         // 4. Verificar estado general
	         if (!"ABIERTA".equals(turno.getEstado())) {
	             return false; // Estado no válido
	         }
	         
	         // 5. Verificar registro activo
	         if (turno.getIdEstadoRegistro() == null || turno.getIdEstadoRegistro() != 1) {
	             return false; // Registro inactivo o eliminado
	         }
	         
	         return true; // Todo OK, se pueden registrar ventas
	         
	     } catch (Exception e) {
	         // Loggear error y retornar false por seguridad
	         System.err.println("Error en sePuedeRegistrarVenta: " + e.getMessage());
	         return false;
	     } finally {
	         if (cn != null) {
	             try { cn.close(); } catch (Exception ignored) {}
	         }
	     }
	 }
}