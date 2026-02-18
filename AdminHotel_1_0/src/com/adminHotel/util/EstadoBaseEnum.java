package com.adminHotel.util;

public enum EstadoBaseEnum {

    UNO(1),
    DOS(2),
    TRES(3);

    private final int codigo;

    EstadoBaseEnum(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static EstadoBaseEnum fromCodigo(int codigo) {
        for (EstadoBaseEnum e : values()) {
            if (e.codigo == codigo) {
                return e;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }
}