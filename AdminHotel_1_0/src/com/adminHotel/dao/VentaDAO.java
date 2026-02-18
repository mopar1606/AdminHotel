package com.adminHotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import com.adminHotel.vo.VentaVO;

public class VentaDAO {
	
	private Connection cn;

    public VentaDAO(Connection cn) {
        this.cn = cn;
    }
    
    public int insertar(VentaVO venta) throws Exception {

        String sql = "INSERT INTO venta(id_cliente, fecha, total, observacion, id_estado_registro) "
                   + "VALUES (?, NOW(), ?, ?, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (venta.getIdCliente() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, venta.getIdCliente());
            }

            if (venta.getTotal() == null) {
                ps.setBigDecimal(2, BigDecimal.ZERO);
            } else {
                ps.setBigDecimal(2, venta.getTotal());
            }
            ps.setString(3, venta.getObservacion());
            ps.setInt(4, 1);

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
