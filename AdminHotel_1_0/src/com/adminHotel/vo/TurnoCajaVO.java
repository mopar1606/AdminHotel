package com.adminHotel.vo;

import java.math.BigDecimal;
import java.util.Date;

public class TurnoCajaVO {

    private Integer idTurnoCaja;
    private Integer idCaja;
    private Integer idUsuario;
    private Date fechaApertura;
    private BigDecimal baseInicialEfectivo;
    private String observacionApertura;
    private Date fechaCierre;
    private String estado;  // 'ABIERTA' o 'CERRADA'
    private String observacionCierre;

    private Integer idEstadoRegistro;
    
    private Integer turnoDiurno; 
    private Integer forzadoCierre; 

    // ------------------------------------------------------------------
    // Campos transientes: para uso en pantalla / reportes (no van a BD)
    // ------------------------------------------------------------------
    private String nombreCaja;    // JOIN con tabla caja
    private String nombreUsuario; // JOIN con tabla usuario

    public Integer getIdTurnoCaja() {
        return idTurnoCaja;
    }

    public void setIdTurnoCaja(Integer idTurnoCaja) {
        this.idTurnoCaja = idTurnoCaja;
    }

    public Integer getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(Integer idCaja) {
        this.idCaja = idCaja;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Date getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(Date fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public BigDecimal getBaseInicialEfectivo() {
        return baseInicialEfectivo;
    }

    public void setBaseInicialEfectivo(BigDecimal baseInicialEfectivo) {
        this.baseInicialEfectivo = baseInicialEfectivo;
    }

    public String getObservacionApertura() {
        return observacionApertura;
    }

    public void setObservacionApertura(String observacionApertura) {
        this.observacionApertura = observacionApertura;
    }

    public Date getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Date fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacionCierre() {
        return observacionCierre;
    }

    public void setObservacionCierre(String observacionCierre) {
        this.observacionCierre = observacionCierre;
    }

    public Integer getIdEstadoRegistro() {
        return idEstadoRegistro;
    }

    public void setIdEstadoRegistro(Integer idEstadoRegistro) {
        this.idEstadoRegistro = idEstadoRegistro;
    }

    public String getNombreCaja() {
        return nombreCaja;
    }

    public void setNombreCaja(String nombreCaja) {
        this.nombreCaja = nombreCaja;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

	public Integer getTurnoDiurno() {
		return turnoDiurno;
	}

	public void setTurnoDiurno(Integer turnoDiurno) {
		this.turnoDiurno = turnoDiurno;
	}

	public Integer getForzadoCierre() {
		return forzadoCierre;
	}

	public void setForzadoCierre(Integer forzadoCierre) {
		this.forzadoCierre = forzadoCierre;
	}
}
