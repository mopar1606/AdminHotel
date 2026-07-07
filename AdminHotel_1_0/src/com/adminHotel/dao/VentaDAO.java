package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.adminHotel.util.EstadoRegistroEnum;
import com.adminHotel.vo.VentaVO;

public class VentaDAO {
	
	private Connection cn;

    public VentaDAO(Connection cn) {
        this.cn = cn;
    }
    
    public int insertar(VentaVO venta) throws Exception {
        String sql = "INSERT INTO venta(id_cliente, id_habitacion, id_concepto_venta, fecha, total, observacion, id_estado_registro) "
                   + "VALUES (?, ?, ?, NOW(), ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (venta.getIdCliente() == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, venta.getIdCliente());
            }
            
            if (venta.getIdHabitacion() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, venta.getIdHabitacion());
            }
            
            // Índices 3 en adelante corregidos en secuencia
            ps.setInt(3, venta.getIdConceptoVenta());
            
            if (venta.getTotal() == null) {
                ps.setBigDecimal(4, java.math.BigDecimal.ZERO);
            } else {
                ps.setBigDecimal(4, venta.getTotal());
            }
            
            ps.setString(5, venta.getObservacion());
            ps.setInt(6, EstadoRegistroEnum.ACTIVO.getCodigo()); 
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new Exception("No se pudo generar el ID de la Venta.");
    }
}
