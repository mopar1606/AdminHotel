package com.adminHotel.vo;

public class CajaVO {

    private Integer idCaja;
    private String nombre;
    private Integer idConceptoVenta;
    private Integer idEstadoRegistro;

    public Integer getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(Integer idCaja) {
        this.idCaja = idCaja;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getIdConceptoVenta() {
        return idConceptoVenta;
    }

    public void setIdConceptoVenta(Integer idConceptoVenta) {
        this.idConceptoVenta = idConceptoVenta;
    }

    public Integer getIdEstadoRegistro() {
        return idEstadoRegistro;
    }

    public void setIdEstadoRegistro(Integer idEstadoRegistro) {
        this.idEstadoRegistro = idEstadoRegistro;
    }
}
