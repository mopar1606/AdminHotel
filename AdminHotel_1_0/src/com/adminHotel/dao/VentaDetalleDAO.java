package com.adminHotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.adminHotel.util.EstadoRegistroEnum;
import com.adminHotel.vo.HabitacionVO;
import com.adminHotel.vo.VentaVO;

public class VentaDetalleDAO {
	
	private Connection cn;

    public VentaDetalleDAO(Connection cn) {
        this.cn = cn;
    }
    
    public void insertarDetalleHabitacion(VentaVO venta, HabitacionVO habitacion, Integer noches) throws Exception {
    	
    	if (habitacion.getPrecio() == null) {
            throw new Exception("La habitación no tiene precio definido...");
        }
    	
    	if (noches == null || noches <= 0) {
            throw new Exception("El número de noches debe ser mayor a cero.");
        }
    	
    	String sql = "INSERT INTO venta_detalle "
                + "(id_venta, tipo_item, id_referencia, cantidad, precio_unitario, subtotal, id_estado_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    	BigDecimal precio = habitacion.getPrecio();
        BigDecimal subtotal = precio.multiply(new BigDecimal(noches));

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, venta.getIdVenta());
            ps.setString(2, "HABITACION");
            ps.setInt(3, habitacion.getIdHabitacion());
            ps.setInt(4, noches);
            ps.setBigDecimal(5, precio);
            ps.setBigDecimal(6, subtotal);
            ps.setInt(7, EstadoRegistroEnum.ACTIVO.getCodigo());

            ps.executeUpdate();
        }
    }

}
