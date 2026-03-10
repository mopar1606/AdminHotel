package com.adminHotel.util;

public enum MetodoPagoEnum {
	
	EFECTIVO(1),
    NEQUI(2),
    DAVIPLATA(3);

    private final int codigo;

    MetodoPagoEnum(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static MetodoPagoEnum fromCodigo(int codigo) {
        for (MetodoPagoEnum e : values()) {
            if (e.codigo == codigo) {
                return e;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }
}
