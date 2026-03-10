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

}
