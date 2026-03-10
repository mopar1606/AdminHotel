package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.adminHotel.vo.UsuarioVO;

public class UsuarioDAO {
	
	private Connection cn;

    public UsuarioDAO(Connection cn) {
        this.cn = cn;
    }
    
    public UsuarioVO validar(String user, String pass) throws Exception {
        // Consultamos el usuario y sus módulos permitidos en un solo JOIN
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.nombre_completo, m.id_modulo as modulo " +
                     "FROM usuario u " +
                     "LEFT JOIN permiso p ON u.id_usuario = p.id_usuario " +
                     "LEFT JOIN modulo m ON p.id_modulo = m.id_modulo " +
                     "WHERE u.nombre_usuario = ? AND u.clave = ? AND u.id_estado_registro = 1";

        UsuarioVO usuario = null;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass); // Nota: Aquí deberías usar HASH en el futuro
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> permisos = new ArrayList<>();
                while (rs.next()) {
                    if (usuario == null) {
                        usuario = new UsuarioVO();
                        usuario.setIdUsuario(rs.getInt("id_usuario"));
                        usuario.setUsuario(rs.getString("nombre_usuario"));
                        usuario.setNombreCompleto(rs.getString("nombre_completo"));
                    }
                    if (rs.getString("modulo") != null) {
                        permisos.add(rs.getInt("modulo"));
                    }
                }
                if (usuario != null) usuario.setPermisos(permisos);
            }
        }
        return usuario;
    }
    
    public void registrarAuditoria(int idUsuario, String evento, String tabla, String descripcion) {
        String sql = "INSERT INTO auditoria (id_usuario, evento, tabla_afectada, descripcion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, evento);
            ps.setString(3, tabla);
            ps.setString(4, descripcion);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error en auditoría: " + e.getMessage());
        }
    }
}
