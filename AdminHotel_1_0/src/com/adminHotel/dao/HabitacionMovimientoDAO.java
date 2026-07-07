package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.adminHotel.util.EstadoRegistroEnum;

public class HabitacionMovimientoDAO {
	
	private Connection cn;

    public HabitacionMovimientoDAO(Connection cn) {
        this.cn = cn;
    }
	
    // ==========================================
 	// REGISTRAR ENTRADA HABITACION
 	// ==========================================
	public Integer registrarEntrada(Integer idHabitacion, Integer idCliente, Integer noches) throws Exception {
		
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO habitacion_movimiento (");
		sb.append("id_habitacion, id_cliente, fecha_entrada, noches, fecha_salida_prevista, id_estado_registro ");
		sb.append(") ");
		sb.append("VALUES (?, ?, NOW(), ?, DATE_ADD(NOW(), INTERVAL ? DAY), ?)");

		try (PreparedStatement ps = cn.prepareStatement(sb.toString(), java.sql.Statement.RETURN_GENERATED_KEYS)) {

	    	ps.setInt(1, idHabitacion);
	        ps.setInt(2, idCliente);
	        ps.setInt(3, noches);
	        ps.setInt(4, noches);
	        ps.setInt(5, EstadoRegistroEnum.ACTIVO.getCodigo());

	        ps.executeUpdate();
	        
	        try (ResultSet rs = ps.getGeneratedKeys()) {
	        	if (rs.next()) return rs.getInt(1);
	        }
	    }
	    
	    throw new Exception("No se pudo registrar entrada...");
	}
	
	// ==========================================
	// REGISTRAR SALIDA DE HABITACION
	// ==========================================
	public Integer registrarSalida(Integer idHabitacion) throws Exception {

	    String sql =
	        "UPDATE habitacion_movimiento " +
	        "SET fecha_salida = NOW() " +
	        "WHERE id_habitacion = ? " +
	        "AND fecha_salida IS NULL";

	    try (PreparedStatement ps = cn.prepareStatement(sql)) {

	        ps.setInt(1, idHabitacion);
	        return ps.executeUpdate();
	    }
	}
	
	// ==========================================
	// BUSCAR CLIENTE ACTIVO EN HABITACION
	// ==========================================
	public Integer obtenerClienteActivo(Integer idHabitacion) throws Exception {
	    String sql = "SELECT id_cliente FROM habitacion_movimiento "
	               + "WHERE id_habitacion = ? AND fecha_salida IS NULL "
	               + "ORDER BY id_habitacion_movimiento DESC LIMIT 1";
	    
	    try (PreparedStatement ps = cn.prepareStatement(sql)) {
	        ps.setInt(1, idHabitacion);
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt("id_cliente");
	            }
	        }
	    }
	    return null; // Si no hay cliente activo registrado
	}
	
	// ==========================================
    // OBTENER ID DEL MOVIMIENTO ACTIVO (Para Préstamos)
    // ==========================================
    public Integer obtenerMovimientoActivo(Integer idHabitacion) throws Exception {
        String sql = "SELECT id_habitacion_movimiento FROM habitacion_movimiento "
                   + "WHERE id_habitacion = ? AND fecha_salida IS NULL "
                   + "ORDER BY id_habitacion_movimiento DESC LIMIT 1";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idHabitacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_habitacion_movimiento");
                }
            }
        }
        return null; // Si no hay movimiento activo registrado
    }
}
