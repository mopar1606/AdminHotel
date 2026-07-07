package com.adminHotel.vo;

import java.math.BigDecimal;

public class TurnoCajaDetalleVO {

    private Integer idTurnoDetalle;
    private Integer idTurnoCaja;
    private Integer idMetodo;
    private Integer idCaja;

    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private BigDecimal montoEsperado;
    private BigDecimal montoReal;
    private BigDecimal diferencia;

    // ------------------------------------------------------------------
    // Campos transientes: para uso en pantalla / reportes (no van a BD)
    // ------------------------------------------------------------------
    private String descripcionMetodo;  // JOIN con metodo_pago
    private String nombreCaja;         // JOIN con caja via turno_caja

    public Integer getIdTurnoDetalle() {
        return idTurnoDetalle;
    }

    public void setIdTurnoDetalle(Integer idTurnoDetalle) {
        this.idTurnoDetalle = idTurnoDetalle;
    }

    public Integer getIdTurnoCaja() {
        return idTurnoCaja;
    }

    public void setIdTurnoCaja(Integer idTurnoCaja) {
        this.idTurnoCaja = idTurnoCaja;
    }

    public Integer getIdMetodo() {
        return idMetodo;
    }

    public void setIdMetodo(Integer idMetodo) {
        this.idMetodo = idMetodo;
    }

    public BigDecimal getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(BigDecimal totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public BigDecimal getTotalEgresos() {
        return totalEgresos;
    }

    public void setTotalEgresos(BigDecimal totalEgresos) {
        this.totalEgresos = totalEgresos;
    }

    public BigDecimal getMontoEsperado() {
        return montoEsperado;
    }

    public void setMontoEsperado(BigDecimal montoEsperado) {
        this.montoEsperado = montoEsperado;
    }

    public BigDecimal getMontoReal() {
        return montoReal;
    }

    public void setMontoReal(BigDecimal montoReal) {
        this.montoReal = montoReal;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public String getDescripcionMetodo() {
        return descripcionMetodo;
    }

    public void setDescripcionMetodo(String descripcionMetodo) {
        this.descripcionMetodo = descripcionMetodo;
    }

    public String getNombreCaja() {
        return nombreCaja;
    }

    public void setNombreCaja(String nombreCaja) {
        this.nombreCaja = nombreCaja;
    }

	public Integer getIdCaja() {
		return idCaja;
	}

	public void setIdCaja(Integer idCaja) {
		this.idCaja = idCaja;
	}
}
