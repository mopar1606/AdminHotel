package com.adminHotel.util;

public enum EstadoRegistroEnum {

    ACTIVO(1),
    INACTIVO(2),
    ELIMINADO(3);

    private final int codigo;

    EstadoRegistroEnum(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static EstadoRegistroEnum fromCodigo(int codigo) {
        for (EstadoRegistroEnum e : values()) {
            if (e.codigo == codigo) {
                return e;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }
}