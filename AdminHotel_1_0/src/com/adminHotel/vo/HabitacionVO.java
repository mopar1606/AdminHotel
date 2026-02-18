package com.adminHotel.vo;

import java.math.BigDecimal;

public class HabitacionVO {

    private Integer idHabitacion;
    private Integer numeroHabitacion;
    private Integer piso;
    private String descripcion;
    private Integer idTipoHabitacion;
    private Integer idEstadoHabitacion;
    private Integer idEstadoRegistro;
    private String estadoDescripcion;
    private String tipoDescripcion;
    private BigDecimal precio; 

    public Integer getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Integer idHabitacion) {
        this.idHabitacion = idHabitacion;
    }

    public Integer getNumeroHabitacion() {
        return numeroHabitacion;
    }

    public void setNumeroHabitacion(Integer numeroHabitacion) {
        this.numeroHabitacion = numeroHabitacion;
    }

    public Integer getPiso() {
        return piso;
    }

    public void setPiso(Integer piso) {
        this.piso = piso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getIdTipoHabitacion() {
        return idTipoHabitacion;
    }

    public void setIdTipoHabitacion(Integer idTipoHabitacion) {
        this.idTipoHabitacion = idTipoHabitacion;
    }

    public Integer getIdEstadoHabitacion() {
        return idEstadoHabitacion;
    }

    public void setIdEstadoHabitacion(Integer idEstadoHabitacion) {
        this.idEstadoHabitacion = idEstadoHabitacion;
    }

    public Integer getIdEstadoRegistro() {
        return idEstadoRegistro;
    }

    public void setIdEstadoRegistro(Integer idEstadoRegistro) {
        this.idEstadoRegistro = idEstadoRegistro;
    }
    
    public String getEstadoDescripcion() {
        return estadoDescripcion;
    }

    public void setEstadoDescripcion(String estadoDescripcion) {
        this.estadoDescripcion = estadoDescripcion;
    }

    public String getTipoDescripcion() {
        return tipoDescripcion;
    }

    public void setTipoDescripcion(String tipoDescripcion) {
        this.tipoDescripcion = tipoDescripcion;
    }

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}
}