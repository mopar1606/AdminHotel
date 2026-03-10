package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.adminHotel.vo.VentaVO;

public class VentaDAO {
	
	private Connection cn;

    public VentaDAO(Connection cn) {
        this.cn = cn;
    }
    
    public int insertar(VentaVO venta) throws Exception {

        String sql = "INSERT INTO venta(id_cliente, id_concepto_venta, fecha, total, observacion, id_estado_registro) "
                   + "VALUES (?, ?, NOW(), ?, ?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        	if (venta.getIdCliente() == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, venta.getIdCliente());
            }
        	
        	ps.setInt(2, venta.getIdConceptoVenta());

        	if (venta.getTotal() == null) {
                ps.setBigDecimal(3, java.math.BigDecimal.ZERO);
            } else {
                ps.setBigDecimal(3, venta.getTotal());
            }
        	
        	ps.setString(4, venta.getObservacion());
        	
        	ps.setInt(5, 1);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    venta.setIdVenta(id);
                    return id;
                }
            }
        }

        throw new Exception("No se pudo crear la venta");
    }

}
