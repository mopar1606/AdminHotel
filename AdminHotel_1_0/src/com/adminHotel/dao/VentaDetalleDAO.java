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
    
    // ===============================
    // DETALLE HABITACION (CHECK-IN)
    // recibe el precio ya resuelto (sencilla o doble) desde OperationService
    // ===============================
    public void insertarDetalleHabitacion(VentaVO venta, HabitacionVO habitacion, Integer noches, BigDecimal precioNoche) throws Exception {
        if (precioNoche == null) {
            throw new Exception("La habitación no tiene precio definido.");
        }
        if (noches == null || noches < 0) {
            throw new Exception("El número de noches debe ser mayor a cero.");
        }
        String sql = "INSERT INTO venta_detalle "
                + "(id_venta, tipo_item, id_referencia, cantidad, precio_unitario, subtotal, id_estado_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        BigDecimal subtotal = precioNoche.multiply(new BigDecimal(noches));
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, venta.getIdVenta());
            ps.setString(2, "HABITACION");
            ps.setInt(3, habitacion.getIdHabitacion());
            ps.setInt(4, noches);
            ps.setBigDecimal(5, precioNoche);
            ps.setBigDecimal(6, subtotal);
            ps.setInt(7, EstadoRegistroEnum.ACTIVO.getCodigo());
            ps.executeUpdate();
        }
    }
    
    // ===============================
    // DETALLE PERSONA ADICIONAL
    // ===============================
    public void insertarDetallePersonaAdicional(VentaVO venta, HabitacionVO habitacion, BigDecimal tarifa) throws Exception {
        if (tarifa == null) {
            throw new Exception("La tarifa adicional no est� definida.");
        }
        String sql = "INSERT INTO venta_detalle "
                + "(id_venta, tipo_item, id_referencia, cantidad, precio_unitario, subtotal, id_estado_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, venta.getIdVenta());
            ps.setString(2, "PERSONA_ADICIONAL");
            ps.setInt(3, habitacion.getIdHabitacion());
            ps.setInt(4, 1);
            ps.setBigDecimal(5, tarifa);
            ps.setBigDecimal(6, tarifa);
            ps.setInt(7, EstadoRegistroEnum.ACTIVO.getCodigo());
            ps.executeUpdate();
        }
    }
    
 // ===============================
 // DETALLE PRODUCTO MOSTRADOR
 // ===============================
 public void insertarDetalleMostrador(Integer idVenta, Integer idProducto, Integer cantidad, BigDecimal precioUnitario) throws Exception {
     if (precioUnitario == null) {
         throw new Exception("El precio del producto no está definido.");
     }
     String sql = "INSERT INTO venta_detalle "
                + "(id_venta, tipo_item, id_referencia, cantidad, precio_unitario, subtotal, id_estado_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
     BigDecimal subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
     try (PreparedStatement ps = cn.prepareStatement(sql)) {
         ps.setInt(1, idVenta);
         ps.setString(2, "PRODUCTO");
         ps.setInt(3, idProducto);
         ps.setInt(4, cantidad);
         ps.setBigDecimal(5, precioUnitario);
         ps.setBigDecimal(6, subtotal);
         ps.setInt(7, EstadoRegistroEnum.ACTIVO.getCodigo());
         ps.executeUpdate();
     }
 }
}
