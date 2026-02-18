package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.adminHotel.vo.PagoVO;

public class PagoDAO {
	
	private Connection cn;

    public PagoDAO(Connection cn) {
        this.cn = cn;
    }
    
    public void insertar(PagoVO pago) throws Exception {

        String sql = "INSERT INTO pago(id_venta, id_metodo, valor, id_estado_registro) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, pago.getIdVenta());
            ps.setInt(2, pago.getIdMetodo());
            ps.setBigDecimal(3, pago.getValor());
            ps.setInt(4, pago.getIdEstadoRegistro());

            ps.executeUpdate();
        }
    }

}
