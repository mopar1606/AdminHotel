package com.adminHotel.vo;

import java.util.Date;

public class HabitacionMovimientoVO {

    private Integer idHabitacionMovimiento;
    private Integer idHabitacion;
    private Integer idCliente;
    private Date fechaEntrada;
    private Date fechaSalida;
    private Integer idEstadoRegistro;

    public Integer getIdHabitacionMovimiento() {
        return idHabitacionMovimiento;
    }

    public void setIdHabitacionMovimiento(Integer idHabitacionMovimiento) {
        this.idHabitacionMovimiento = idHabitacionMovimiento;
    }

    public Integer getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Integer idHabitacion) {
        this.idHabitacion = idHabitacion;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Date getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(Date fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public Date getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(Date fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public Integer getIdEstadoRegistro() {
        return idEstadoRegistro;
    }

    public void setIdEstadoRegistro(Integer idEstadoRegistro) {
        this.idEstadoRegistro = idEstadoRegistro;
    }
}