package com.adminHotel.vo;

import java.math.BigDecimal;
import java.util.Date;

public class PagoVO {

    private Integer idPago;
    private Integer idVenta;
    private Date fecha;
    private Integer idMetodo;
    private BigDecimal valor;
    private Integer idEstadoRegistro;
    private Integer idCliente;
    private Integer idHabitacion;

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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Integer getIdMetodo() {
        return idMetodo;
    }

    public void setIdMetodo(Integer idMetodo) {
        this.idMetodo = idMetodo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getIdEstadoRegistro() {
        return idEstadoRegistro;
    }

    public void setIdEstadoRegistro(Integer idEstadoRegistro) {
        this.idEstadoRegistro = idEstadoRegistro;
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
}