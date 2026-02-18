package com.adminHotel.vo;

import java.math.BigDecimal;
import java.util.Date;

public class TarifaHabitacionVO {

    private Integer idTarifa;
    private Integer idTipoHabitacion;
    private BigDecimal precio;
    private Date fechaInicio;
    private Date fechaFin;
    private Integer idEstadoRegistro;

    public Integer getIdTarifa() {
        return idTarifa;
    }

    public void setIdTarifa(Integer idTarifa) {
        this.idTarifa = idTarifa;
    }

    public Integer getIdTipoHabitacion() {
        return idTipoHabitacion;
    }

    public void setIdTipoHabitacion(Integer idTipoHabitacion) {
        this.idTipoHabitacion = idTipoHabitacion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getIdEstadoRegistro() {
        return idEstadoRegistro;
    }

    public void setIdEstadoRegistro(Integer idEstadoRegistro) {
        this.idEstadoRegistro = idEstadoRegistro;
    }
}