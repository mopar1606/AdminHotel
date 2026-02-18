package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.adminHotel.vo.ClienteVO;

public class ClienteDAO {
	
	private Connection cn;

    public ClienteDAO(Connection cn) {
        this.cn = cn;
    }
	
	public ClienteVO buscarPorDocumento(String documento) throws Exception {

	    String sql = "SELECT * FROM cliente WHERE documento = ? AND id_estado_registro = 1";

	    try (PreparedStatement ps = cn.prepareStatement(sql)) {

	        ps.setString(1, documento);

	        try (ResultSet rs = ps.executeQuery()) {

	            if (rs.next()) {
	                ClienteVO c = new ClienteVO();
	                c.setIdCliente(rs.getInt("id_cliente"));
	                c.setNombre(rs.getString("nombre"));
	                c.setDocumento(rs.getString("documento"));
	                c.setTelefono(rs.getString("telefono"));
	                c.setEmail(rs.getString("email"));
	                return c;
	            }
	        }
	    }

	    return null;
	}
	
	public int insertar(ClienteVO c) throws Exception {

	    String sql = "INSERT INTO cliente(nombre, documento, telefono, email, id_estado_registro) VALUES (?, ?, ?, ?, 1)";

	    try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	        ps.setString(1, c.getNombre());
	        ps.setString(2, c.getDocumento());
	        ps.setString(3, c.getTelefono());
	        ps.setString(4, c.getEmail());

	        ps.executeUpdate();

	        ResultSet rs = ps.getGeneratedKeys();
	        if (rs.next()) {
	            return rs.getInt(1);
	        }
	    }

	    throw new Exception("No se pudo crear cliente");
	}

}
