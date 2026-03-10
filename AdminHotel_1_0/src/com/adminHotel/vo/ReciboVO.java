package com.adminHotel.vo;

import java.sql.Timestamp;

public class ReciboVO {
	
	private Integer idRecibo;
	private Integer idCliente;
	private Integer idHabitacion;
	private Integer idPago;
    private Integer idVenta;
    private Integer idHabitacionMovimiento;
    private Integer noches;
    private Timestamp fecha;
    
	public Integer getIdRecibo() {
		return idRecibo;
	}
	public void setIdRecibo(Integer idRecibo) {
		this.idRecibo = idRecibo;
	}
	public Integer getIdCliente() {
		return idCliente;
	}
	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
	}
	public Integer getIdHabitacion() {
		return idHabitacion;
	}
	public void setIdHabitacion(Integer idHabitacion) {
		this.idHabitacion = idHabitacion;
	}
	public Integer getIdPago() {
		return idPago;
	}
	public void setIdPago(Integer idPago) {
		this.idPago = idPago;
	}
	public Integer getIdVenta() {
		return idVenta;
	}
	public void setIdVenta(Integer idVenta) {
		this.idVenta = idVenta;
	}
	public Integer getIdHabitacionMovimiento() {
		return idHabitacionMovimiento;
	}
	public void setIdHabitacionMovimiento(Integer idHabitacionMovimiento) {
		this.idHabitacionMovimiento = idHabitacionMovimiento;
	}
	public Integer getNoches() {
		return noches;
	}
	public void setNoches(Integer noches) {
		this.noches = noches;
	}
	public Timestamp getFecha() {
		return fecha;
	}
	public void setFecha(Timestamp fecha) {
		this.fecha = fecha;
	}   
}
