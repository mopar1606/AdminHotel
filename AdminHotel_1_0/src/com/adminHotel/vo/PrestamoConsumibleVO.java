package com.adminHotel.vo;

import java.math.BigDecimal;

public class PrestamoConsumibleVO {
	
	private Integer idPrestamo, idHabitacionMovimiento, idConsumible, cantidadEntregada, cantidadDevuelta;
    private String estado, nombreConsumible;
    private BigDecimal precioMulta;
    
	public Integer getIdPrestamo() {
		return idPrestamo;
	}
	public void setIdPrestamo(Integer idPrestamo) {
		this.idPrestamo = idPrestamo;
	}
	public Integer getIdHabitacionMovimiento() {
		return idHabitacionMovimiento;
	}
	public void setIdHabitacionMovimiento(Integer idHabitacionMovimiento) {
		this.idHabitacionMovimiento = idHabitacionMovimiento;
	}
	public Integer getIdConsumible() {
		return idConsumible;
	}
	public void setIdConsumible(Integer idConsumible) {
		this.idConsumible = idConsumible;
	}
	public Integer getCantidadEntregada() {
		return cantidadEntregada;
	}
	public void setCantidadEntregada(Integer cantidadEntregada) {
		this.cantidadEntregada = cantidadEntregada;
	}
	public Integer getCantidadDevuelta() {
		return cantidadDevuelta;
	}
	public void setCantidadDevuelta(Integer cantidadDevuelta) {
		this.cantidadDevuelta = cantidadDevuelta;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public String getNombreConsumible() {
		return nombreConsumible;
	}
	public void setNombreConsumible(String nombreConsumible) {
		this.nombreConsumible = nombreConsumible;
	}
	public BigDecimal getPrecioMulta() {
		return precioMulta;
	}
	public void setPrecioMulta(BigDecimal precioMulta) {
		this.precioMulta = precioMulta;
	}
}
