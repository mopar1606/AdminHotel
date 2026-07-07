package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.adminHotel.vo.CajaVO;

public class CajaDAO {
	
    private Connection cn;
    public CajaDAO(Connection cn) {
        this.cn = cn;
    }
    
    public List<CajaVO> listar() throws Exception {
    	
        List<CajaVO> lista = new ArrayList<>();
        String sql = "SELECT id_caja, nombre, id_concepto_venta, id_estado_registro "
                   + "FROM caja WHERE id_estado_registro = 1";
        
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CajaVO c = new CajaVO();
                c.setIdCaja(rs.getInt("id_caja"));
                c.setNombre(rs.getString("nombre"));
                c.setIdConceptoVenta(rs.getInt("id_concepto_venta"));
                c.setIdEstadoRegistro(rs.getInt("id_estado_registro"));
                lista.add(c);
            }
        }
        return lista;
    }
    
    public CajaVO buscarPorId(Integer idCaja) throws Exception {
    	
        String sql = "SELECT id_caja, nombre, id_concepto_venta, id_estado_registro "
                   + "FROM caja WHERE id_caja = ?";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CajaVO c = new CajaVO();
                    c.setIdCaja(rs.getInt("id_caja"));
                    c.setNombre(rs.getString("nombre"));
                    c.setIdConceptoVenta(rs.getInt("id_concepto_venta"));
                    c.setIdEstadoRegistro(rs.getInt("id_estado_registro"));
                    return c;
                }
            }
        }
        return null;
    }
}