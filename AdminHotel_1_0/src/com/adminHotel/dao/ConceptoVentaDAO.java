package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ConceptoVentaDAO {
    
    private Connection cn;
    public ConceptoVentaDAO(Connection cn) {
        this.cn = cn;
    }
    
    // ==========================================
    // OBTENER ID DE CONCEPTO POR NOMBRE (Ej. "MULTAS")
    // ==========================================
    public Integer obtenerIdPorNombre(String nombreConcepto) throws Exception {
        // Según tu tabla, la columna se llama 'nombre_concepto'
        String sql = "SELECT id_concepto_venta FROM concepto_venta WHERE descripcion = ?";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreConcepto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_concepto_venta");
                }
            }
        }
        return null;
    }
}