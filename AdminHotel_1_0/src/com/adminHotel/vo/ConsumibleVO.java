package com.adminHotel.vo;

import java.math.BigDecimal;

public class ConsumibleVO {
	
	private Integer idConsumible, idEstadoRegistro, stock, enHabitaciones, perdidosCobrados;
    private String nombre;
	private BigDecimal precioCompra, precioMulta;
	private java.sql.Date fechaCompra;
	private Boolean esPrestable;
	
	public Integer getIdConsumible() {
		return idConsumible;
	}
	public void setIdConsumible(Integer idConsumible) {
		this.idConsumible = idConsumible;
	}
	public Integer getIdEstadoRegistro() {
		return idEstadoRegistro;
	}
	public void setIdEstadoRegistro(Integer idEstadoRegistro) {
		this.idEstadoRegistro = idEstadoRegistro;
	}
	public Integer getStock() {
		return stock;
	}
	public void setStock(Integer stock) {
		this.stock = stock;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public BigDecimal getPrecioCompra() {
		return precioCompra;
	}
	public void setPrecioCompra(BigDecimal precioCompra) {
		this.precioCompra = precioCompra;
	}
	public Integer getEnHabitaciones() {
		return enHabitaciones;
	}
	public void setEnHabitaciones(Integer enHabitaciones) {
		this.enHabitaciones = enHabitaciones;
	}
	public Integer getPerdidosCobrados() {
		return perdidosCobrados;
	}
	public void setPerdidosCobrados(Integer perdidosCobrados) {
		this.perdidosCobrados = perdidosCobrados;
	}
	public BigDecimal getPrecioMulta() {
		return precioMulta;
	}
	public void setPrecioMulta(BigDecimal precioMulta) {
		this.precioMulta = precioMulta;
	}
	public java.sql.Date getFechaCompra() {
		return fechaCompra;
	}
	public void setFechaCompra(java.sql.Date fechaCompra) {
		this.fechaCompra = fechaCompra;
	}
	public Boolean getEsPrestable() {
		return esPrestable;
	}
	public void setEsPrestable(Boolean esPrestable) {
		this.esPrestable = esPrestable;
	}
}
