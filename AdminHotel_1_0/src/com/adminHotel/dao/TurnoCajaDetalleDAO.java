package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.adminHotel.vo.TurnoCajaDetalleVO;

public class TurnoCajaDetalleDAO {
	
    private Connection cn;
    public TurnoCajaDetalleDAO(Connection cn) {
        this.cn = cn;
    }
    
    // Inserta el detalle de cuadre al momento de cerrar el turno
    public Integer insertar(TurnoCajaDetalleVO detalle) throws Exception {
        String sql = "INSERT INTO turno_caja_detalle "
                   + "(id_turno_caja, id_metodo, total_ingresos, total_egresos, "
                   + " monto_esperado, monto_real, diferencia) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, detalle.getIdTurnoCaja());
            ps.setInt(2, detalle.getIdMetodo());
            ps.setBigDecimal(3, detalle.getTotalIngresos());
            ps.setBigDecimal(4, detalle.getTotalEgresos());
            ps.setBigDecimal(5, detalle.getMontoEsperado());
            ps.setBigDecimal(6, detalle.getMontoReal());
            ps.setBigDecimal(7, detalle.getDiferencia());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new Exception("No se pudo guardar el detalle del turno");
    }
    
    // Obtiene los detalles de cierre de un turno específico (para tirilla/reporte)
    public List<TurnoCajaDetalleVO> listarPorTurno(Integer idTurnoCaja) throws Exception {
    	
        List<TurnoCajaDetalleVO> lista = new ArrayList<>();
        String sql = "SELECT tcd.id_turno_detalle, tcd.id_turno_caja, tcd.id_metodo, "
                   + "       tcd.total_ingresos, tcd.total_egresos, "
                   + "       tcd.monto_esperado, tcd.monto_real, tcd.diferencia, "
                   + "       mp.descripcion AS descripcion_metodo "
                   + "FROM turno_caja_detalle tcd "
                   + "INNER JOIN metodo_pago mp ON tcd.id_metodo = mp.id_metodo "
                   + "WHERE tcd.id_turno_caja = ?";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idTurnoCaja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TurnoCajaDetalleVO d = new TurnoCajaDetalleVO();
                    d.setIdTurnoDetalle(rs.getInt("id_turno_detalle"));
                    d.setIdTurnoCaja(rs.getInt("id_turno_caja"));
                    d.setIdMetodo(rs.getInt("id_metodo"));
                    d.setTotalIngresos(rs.getBigDecimal("total_ingresos"));
                    d.setTotalEgresos(rs.getBigDecimal("total_egresos"));
                    d.setMontoEsperado(rs.getBigDecimal("monto_esperado"));
                    d.setMontoReal(rs.getBigDecimal("monto_real"));
                    d.setDiferencia(rs.getBigDecimal("diferencia"));
                    d.setDescripcionMetodo(rs.getString("descripcion_metodo"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }
    
    // Consulta en tiempo real los acumulados del turno activo
    // Devuelve los 4 cuadrantes de la pantalla de control de caja
    public List<TurnoCajaDetalleVO> listarAcumuladosPorTurnosAbiertos() throws Exception {
    	
        List<TurnoCajaDetalleVO> lista = new ArrayList<>();
        String sql = "SELECT c.nombre AS nombre_caja, "
                   + "       mp.descripcion AS descripcion_metodo, "
                   + "       mp.id_metodo, tc.id_caja, tc.id_turno_caja, "
                   + "       COALESCE(SUM(p.valor), 0) AS total_ingresos "
                   + "FROM turno_caja tc "
                   + "INNER JOIN caja c ON tc.id_caja = c.id_caja "
                   + "CROSS JOIN metodo_pago mp "
                   + "LEFT JOIN pago p ON p.id_turno_caja = tc.id_turno_caja "
                   + "               AND p.id_metodo = mp.id_metodo "
                   + "               AND p.id_estado_registro = 1 "
                   + "WHERE tc.estado = 'ABIERTA' "
                   + "  AND mp.id_estado_registro = 1 "
                   + "GROUP BY tc.id_turno_caja, tc.id_caja, c.nombre, mp.id_metodo, mp.descripcion "
                   + "ORDER BY tc.id_caja, mp.id_metodo";
        
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TurnoCajaDetalleVO d = new TurnoCajaDetalleVO();
                d.setIdTurnoCaja(rs.getInt("id_turno_caja"));
                d.setIdMetodo(rs.getInt("id_metodo"));
                d.setTotalIngresos(rs.getBigDecimal("total_ingresos"));
                d.setNombreCaja(rs.getString("nombre_caja"));
                d.setDescripcionMetodo(rs.getString("descripcion_metodo"));
                lista.add(d);
            }
        }
        return lista;
    }
    
    // Consulta en tiempo real las ventas/pagos individuales del turno activo
    public List<TurnoCajaDetalleVO> listarVentasDetalladasPorTurnosAbiertos() throws Exception {
    	
        List<TurnoCajaDetalleVO> lista = new ArrayList<>();
        
        String sql = "SELECT tc.id_caja, tc.id_turno_caja, c.nombre AS nombre_caja, "
                + "       p.id_metodo, p.valor AS total_ingresos, "
                + "       mp.descripcion AS nombre_metodo, "
                + "       v.observacion "
                + "FROM turno_caja tc "
                + "INNER JOIN caja c ON tc.id_caja = c.id_caja "
                + "INNER JOIN pago p ON p.id_turno_caja = tc.id_turno_caja "
                + "INNER JOIN metodo_pago mp ON p.id_metodo = mp.id_metodo "
                + "INNER JOIN venta v ON p.id_venta = v.id_venta "
                + "WHERE tc.estado = 'ABIERTA' "
                + "  AND p.id_estado_registro = 1 "
                + "ORDER BY p.fecha ASC";
     
	     try (PreparedStatement ps = cn.prepareStatement(sql);
	          ResultSet rs = ps.executeQuery()) {
	         while (rs.next()) {
	             TurnoCajaDetalleVO d = new TurnoCajaDetalleVO();
	             d.setIdTurnoCaja(rs.getInt("id_turno_caja"));
	             d.setIdCaja(rs.getInt("id_caja"));
	             d.setIdMetodo(rs.getInt("id_metodo"));
	             d.setTotalIngresos(rs.getBigDecimal("total_ingresos"));
	             d.setNombreCaja(rs.getString("nombre_caja"));
	             
	             // Traemos los datos separados de la base
	             String metodoP = rs.getString("nombre_metodo");
	             String obs = rs.getString("observacion");
	             
	             // Ajuste inteligente: Concatenamos "MÉTODO (Observación)"
	             // evitando meter (null) o recargar visualmente la interfaz si está vacío
	             if (obs != null && !obs.trim().isEmpty()) {
	                 d.setDescripcionMetodo(obs);
	             } else {
	                 d.setDescripcionMetodo(metodoP);
	             }
	             
	             lista.add(d);
            }
        }
        return lista;
    }
    
    // Ventas detalladas de un turno específico (para informe de cierre histórico)
    public List<TurnoCajaDetalleVO> listarVentasPorTurno(Integer idTurnoCaja) throws Exception {
        List<TurnoCajaDetalleVO> lista = new ArrayList<>();
        String sql = "SELECT p.id_pago, p.valor AS total_ingresos, "
                   + "       mp.descripcion AS nombre_metodo, "
                   + "       v.observacion, v.id_venta, p.fecha, "
                   + "       cv.descripcion AS concepto "
                   + "FROM pago p "
                   + "INNER JOIN venta v ON p.id_venta = v.id_venta "
                   + "INNER JOIN metodo_pago mp ON p.id_metodo = mp.id_metodo "
                   + "INNER JOIN concepto_venta cv ON v.id_concepto_venta = cv.id_concepto_venta "
                   + "WHERE p.id_turno_caja = ? "
                   + "  AND p.id_estado_registro = 1 "
                   + "ORDER BY p.fecha ASC";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idTurnoCaja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TurnoCajaDetalleVO d = new TurnoCajaDetalleVO();
                    d.setIdTurnoCaja(idTurnoCaja);
                    d.setTotalIngresos(rs.getBigDecimal("total_ingresos"));
                    d.setDescripcionMetodo(rs.getString("nombre_metodo"));
                    // Reusar campo descripciónMetodo para mostrar concepto + observación
                    String concepto = rs.getString("concepto");
                    String obs      = rs.getString("observacion");
                    // Armamos el Concepto (Ej: VENTA HABITACIÓN - 2 Noches)
                    if (obs != null && !obs.trim().isEmpty()) {
                        d.setNombreCaja(concepto + " - " + obs);
                    } else {
                        d.setNombreCaja(concepto);
                    }
                    // Conservamos intacto el Método de Pago (Ej: EFECTIVO o NEQUI)
                    d.setDescripcionMetodo(rs.getString("nombre_metodo"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }
    
    // Ventas de UNA caja específica para una fecha de cierre
    public List<TurnoCajaDetalleVO> listarVentasPorFechaYCaja(String fechaDia, int idCaja) throws Exception {
        List<TurnoCajaDetalleVO> lista = new ArrayList<>();
        String sql = "SELECT p.valor AS total_ingresos, " +
                     "       mp.descripcion AS nombre_metodo, " +
                     "       v.observacion, " +
                     "       cv.descripcion AS concepto " +
                     "FROM pago p " +
                     "INNER JOIN venta v          ON p.id_venta = v.id_venta " +
                     "INNER JOIN metodo_pago mp   ON p.id_metodo = mp.id_metodo " +
                     "INNER JOIN concepto_venta cv ON v.id_concepto_venta = cv.id_concepto_venta " +
                     "INNER JOIN turno_caja tc    ON p.id_turno_caja = tc.id_turno_caja " +
                     "WHERE DATE(tc.fecha_cierre) = ? " +
                     "  AND tc.id_caja = ? " +
                     "  AND tc.estado = 'CERRADA' " +
                     "  AND p.id_estado_registro = 1 " +
                     "ORDER BY p.fecha ASC";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fechaDia);
            ps.setInt(2, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TurnoCajaDetalleVO d = new TurnoCajaDetalleVO();
                    d.setTotalIngresos(rs.getBigDecimal("total_ingresos"));
                    String metodo   = rs.getString("nombre_metodo");
                    String obs      = rs.getString("observacion");
                    String concepto = rs.getString("concepto");
                    // Columna descripción: "CONCEPTO - Observación"
                    if (obs != null && !obs.trim().isEmpty()) {
                        d.setNombreCaja(concepto + " - " + obs);
                    } else {
                        d.setNombreCaja(concepto);
                    }
                    d.setDescripcionMetodo(metodo);
                    lista.add(d);
                }
            }
        }
        return lista;
    }
}