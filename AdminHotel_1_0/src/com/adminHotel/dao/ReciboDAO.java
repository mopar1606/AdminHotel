package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.adminHotel.vo.ReciboVO;

public class ReciboDAO {
	
	private Connection cn;

    public ReciboDAO(Connection cn) {
        this.cn = cn;
    }
	
    // =============================
	// REGISTRAR RECIBO DE PAGO
	// =============================
	public Integer insertarRecibo(ReciboVO recibo) throws Exception {
		
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO recibo (");
		sb.append("id_cliente, ");
		sb.append("id_Habitacion, ");
		sb.append("id_Pago, ");
		sb.append("id_Venta, ");
		sb.append("id_HabitacionMovimiento, ");
		sb.append("noches, ");
		sb.append("fecha_emision) VALUES (?, ?, ?, ?, ?, ?, NOW())");
		
		try (PreparedStatement ps = cn.prepareStatement(sb.toString(), Statement.RETURN_GENERATED_KEYS)) {
	        ps.setInt(1, recibo.getIdCliente());
	        ps.setInt(2, recibo.getIdHabitacion());
	        ps.setInt(3, recibo.getIdPago());
	        ps.setInt(4, recibo.getIdVenta());
	        ps.setInt(5, recibo.getIdHabitacionMovimiento());
	        ps.setInt(6, recibo.getNoches());
	        
	        ps.executeUpdate();
	        
	        try (ResultSet rs = ps.getGeneratedKeys()) {
	            if (rs.next()) return rs.getInt(1);
	        }
	    }
		
		throw new Exception("No se pudo registrar recibo...");		
	}
	
	// =============================
	// CARGAR DATOS PARA IMPRIMIR RECIBO
	// =============================
	public Map<String, Object> obtenerDatosParaTicket(Integer idHabitacion) throws Exception {
		
		String sql = "SELECT r.id_recibo, c.documento, h.numero_habitacion, r.noches, v.total, r.fecha_emision, h.precio " +
                "FROM recibo r " +
                "JOIN cliente c ON r.id_cliente = c.id_cliente " +
                "JOIN habitacion h ON r.id_habitacion = h.id_habitacion " +
                "JOIN venta v ON r.id_venta = v.id_venta " +
                "WHERE r.id_habitacion = ? " +
                "ORDER BY r.id_recibo DESC LIMIT 1";

	   Map<String, Object> datos = null;
	
	   try (PreparedStatement ps = cn.prepareStatement(sql)) {
	       ps.setInt(1, idHabitacion);
	
	       try (ResultSet rs = ps.executeQuery()) {
	           if (rs.next()) {
	               datos = new HashMap<>();
	               datos.put("id_recibo", rs.getInt("id_recibo"));
	               datos.put("id_cliente", rs.getString("documento"));
	               datos.put("numero_habitacion", rs.getString("numero_habitacion"));
	               datos.put("noches", rs.getInt("noches"));
	               datos.put("total", rs.getBigDecimal("total"));	               
	               datos.put("fecha_emision", rs.getTimestamp("fecha_emision"));
	               datos.put("precio", rs.getBigDecimal("precio"));
	               
	           }
	       }
	   }
	   return datos;
	}
}
