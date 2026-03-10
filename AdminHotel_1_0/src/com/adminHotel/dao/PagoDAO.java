package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.adminHotel.vo.PagoVO;

public class PagoDAO {
	
	private Connection cn;

    public PagoDAO(Connection cn) {
        this.cn = cn;
    }
    
    public Integer insertar(PagoVO pago) throws Exception {

        String sql = "INSERT INTO pago(id_venta, id_metodo, valor, id_estado_registro) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pago.getIdVenta());
            ps.setInt(2, pago.getIdMetodo());
            ps.setBigDecimal(3, pago.getValor());
            ps.setInt(4, pago.getIdEstadoRegistro());

            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
	            if (rs.next()) {
	                return rs.getInt(1);
	            }
	        }
        }
        
        throw new Exception("No se pudo crear el pago");
    }
}
