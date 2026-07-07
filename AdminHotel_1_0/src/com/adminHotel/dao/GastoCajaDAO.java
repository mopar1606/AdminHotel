package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.adminHotel.vo.GastoCajaVO;

public class GastoCajaDAO {
	
    private Connection cn;
    public GastoCajaDAO(Connection cn) {
        this.cn = cn;
    }
    
    // Registra un gasto/retiro de efectivo del cajón
    public Integer insertar(GastoCajaVO gasto) throws Exception {
    	
        String sql = "INSERT INTO gasto_caja "
                   + "(id_turno_caja, id_usuario, concepto, valor, fecha, id_estado_registro) "
                   + "VALUES (?, ?, ?, ?, NOW(), 1)";
        
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, gasto.getIdTurnoCaja());
            ps.setInt(2, gasto.getIdUsuario());
            ps.setString(3, gasto.getConcepto());
            ps.setBigDecimal(4, gasto.getValor());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new Exception("No se pudo registrar el gasto de caja");
    }
    
    // Lista los gastos de un turno específico (para tirilla de cierre)
    public List<GastoCajaVO> listarPorTurno(Integer idTurnoCaja) throws Exception {
    	
        List<GastoCajaVO> lista = new ArrayList<>();
        String sql = "SELECT gc.id_gasto, gc.id_turno_caja, gc.id_usuario, "
                   + "       gc.concepto, gc.valor, gc.fecha, gc.id_estado_registro, "
                   + "       u.nombre_usuario AS nombre_usuario "
                   + "FROM gasto_caja gc "
                   + "INNER JOIN usuario u ON gc.id_usuario = u.id_usuario "
                   + "WHERE gc.id_turno_caja = ? AND gc.id_estado_registro = 1 "
                   + "ORDER BY gc.fecha ASC";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idTurnoCaja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GastoCajaVO g = new GastoCajaVO();
                    g.setIdGasto(rs.getInt("id_gasto"));
                    g.setIdTurnoCaja(rs.getInt("id_turno_caja"));
                    g.setIdUsuario(rs.getInt("id_usuario"));
                    g.setConcepto(rs.getString("concepto"));
                    g.setValor(rs.getBigDecimal("valor"));
                    g.setFecha(rs.getTimestamp("fecha"));
                    g.setIdEstadoRegistro(rs.getInt("id_estado_registro"));
                    g.setNombreUsuario(rs.getString("nombre_usuario"));
                    lista.add(g);
                }
            }
        }
        return lista;
    }
    
    // Suma total de gastos de un turno (para calcular monto_esperado al cerrar)
    public java.math.BigDecimal sumarGastosPorTurno(Integer idTurnoCaja) throws Exception {
    	
        String sql = "SELECT COALESCE(SUM(valor), 0) AS total "
                   + "FROM gasto_caja "
                   + "WHERE id_turno_caja = ? AND id_estado_registro = 1";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idTurnoCaja);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("total");
                }
            }
        }
        return java.math.BigDecimal.ZERO;
    }
}