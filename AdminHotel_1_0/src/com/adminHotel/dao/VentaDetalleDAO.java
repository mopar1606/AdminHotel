package com.adminHotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.adminHotel.vo.HabitacionVO;
import com.adminHotel.vo.VentaVO;

public class VentaDetalleDAO {
	
	private Connection cn;

    public VentaDetalleDAO(Connection cn) {
        this.cn = cn;
    }
    
    public void insertarDetalleHabitacion(VentaVO venta, HabitacionVO habitacion) throws Exception {
    	
    	if (habitacion.getPrecio() == null) {
            throw new Exception("La habitación no tiene precio definido");
        }
    	
    	String sql = "INSERT INTO venta_detalle "
                + "(id_venta, tipo_item, id_referencia, cantidad, precio_unitario, subtotal, id_estado_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    	BigDecimal precio = habitacion.getPrecio();
        int cantidad = 1;
        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, venta.getIdVenta());
            ps.setString(2, "HABITACION");
            ps.setInt(3, habitacion.getIdHabitacion());
            ps.setInt(4, cantidad);
            ps.setBigDecimal(5, precio);
            ps.setBigDecimal(6, subtotal);
            ps.setInt(7, 1);

            ps.executeUpdate();
        }
    }

}
