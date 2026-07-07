package com.adminHotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ParametroDAO {
	
	private Connection cn;
	
    public ParametroDAO(Connection cn) {
        this.cn = cn;
    }
    
    /**
     * Retorna el valor decimal de un par�metro por su clave.
     * Ejemplo: obtenerValor("TARIFA_PERSONA_ADICIONAL")
     */
    public BigDecimal obtenerValor(String clave) throws Exception {
        String sql = "SELECT valor FROM parametro WHERE clave = ? AND id_estado_registro = 1";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("valor");
                } else {
                    throw new Exception("Par�metro no encontrado: " + clave);
                }
            }
        }
    }
    
 // Listar todos los parámetros activos del sistema
    public List<String[]> listarTodos() throws Exception {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT id_parametro, clave, valor, descripcion " +
                     "FROM parametro WHERE id_estado_registro = 1 ORDER BY clave";
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    String.valueOf(rs.getInt("id_parametro")),
                    rs.getString("clave"),
                    rs.getString("valor"),
                    rs.getString("descripcion") != null ? rs.getString("descripcion") : ""
                });
            }
        }
        return lista;
    }
    
 // Actualizar el valor de un parámetro por su ID
    public boolean actualizarValor(int idParametro, String nuevoValor) throws Exception {
        String sql = "UPDATE parametro SET valor = ? WHERE id_parametro = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoValor);
            ps.setInt(2, idParametro);
            return ps.executeUpdate() > 0;
        }
    }
}
