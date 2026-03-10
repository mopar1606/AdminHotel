package com.adminHotel.vo;

import java.util.List;

public class UsuarioVO {
	
	private Integer idUsuario;
	private String usuario, clave, nombreCompleto;
	private List<Integer> permisos; 
	
	public Integer getIdUsuario() {
		return idUsuario;
	}
	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getClave() {
		return clave;
	}
	public void setClave(String clave) {
		this.clave = clave;
	}
	public String getNombreCompleto() {
		return nombreCompleto;
	}
	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}
	public List<Integer> getPermisos() {
		return permisos;
	}
	public void setPermisos(List<Integer> permisos) {
		this.permisos = permisos;
	}
}
