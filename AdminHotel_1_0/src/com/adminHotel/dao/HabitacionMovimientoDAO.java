package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class HabitacionMovimientoDAO {
	
	private Connection cn;

    public HabitacionMovimientoDAO(Connection cn) {
        this.cn = cn;
    }
	
	public void registrarEntrada(int idHabitacion, int idCliente) throws Exception {

	    String sql = "INSERT INTO habitacion_movimiento(id_habitacion, id_cliente, fecha_entrada, id_estado_registro) VALUES (?, ?, NOW(), 1)";

	    try (PreparedStatement ps = cn.prepareStatement(sql)) {

	        ps.setInt(1, idHabitacion);
	        ps.setInt(2, idCliente);

	        ps.executeUpdate();
	    }
	}
	
	public void registrarSalida(int idHabitacion) throws Exception {

	    String sql =
	        "UPDATE habitacion_movimiento " +
	        "SET fecha_salida = NOW() " +
	        "WHERE id_habitacion = ? " +
	        "AND fecha_salida IS NULL";

	    try (PreparedStatement ps = cn.prepareStatement(sql)) {

	        ps.setInt(1, idHabitacion);
	        ps.executeUpdate();
	    }
	}

}
