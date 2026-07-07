package com.adminHotel.vo;

import java.math.BigDecimal;
import java.util.Date;

public class GastoCajaVO {

    private Integer idGasto;
    private Integer idTurnoCaja;
    private Integer idUsuario;
    private String concepto;
    private BigDecimal valor;
    private Date fecha;
    private Integer idEstadoRegistro;

    // ------------------------------------------------------------------
    // Campos transientes: para uso en pantalla / reportes (no van a BD)
    // ------------------------------------------------------------------
    private String nombreUsuario; // JOIN con usuario
    private String nombreCaja;    // JOIN con caja via turno_caja

    public Integer getIdGasto() {
        return idGasto;
    }

    public void setIdGasto(Integer idGasto) {
        this.idGasto = idGasto;
    }

    public Integer getIdTurnoCaja() {
        return idTurnoCaja;
    }

    public void setIdTurnoCaja(Integer idTurnoCaja) {
        this.idTurnoCaja = idTurnoCaja;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Integer getIdEstadoRegistro() {
        return idEstadoRegistro;
    }

    public void setIdEstadoRegistro(Integer idEstadoRegistro) {
        this.idEstadoRegistro = idEstadoRegistro;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreCaja() {
        return nombreCaja;
    }

    public void setNombreCaja(String nombreCaja) {
        this.nombreCaja = nombreCaja;
    }
}
