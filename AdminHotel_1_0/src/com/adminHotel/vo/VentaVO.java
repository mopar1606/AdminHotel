package com.adminHotel.vo;

import java.math.BigDecimal;
import java.util.Date;

public class VentaVO {

    private Integer idVenta,idCliente,idHabitacion, idEstadoRegistro, idConceptoVenta;
    private Date fecha;
    private BigDecimal total;
    private String observacion;

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Integer getIdEstadoRegistro() {
        return idEstadoRegistro;
    }

    public void setIdEstadoRegistro(Integer idEstadoRegistro) {
        this.idEstadoRegistro = idEstadoRegistro;
    }

	public Integer getIdConceptoVenta() {
		return idConceptoVenta;
	}

	public void setIdConceptoVenta(Integer idConceptoVenta) {
		this.idConceptoVenta = idConceptoVenta;
	}

	public Integer getIdHabitacion() {
		return idHabitacion;
	}

	public void setIdHabitacion(Integer idHabitacion) {
		this.idHabitacion = idHabitacion;
	}
}