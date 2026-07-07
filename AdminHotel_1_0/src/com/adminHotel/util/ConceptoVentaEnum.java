package com.adminHotel.util;

public enum ConceptoVentaEnum {
	VENTA_HABITACION(1),
	VENTA_MOSTRADOR(2),
	MULTAS(3),
	SERVICIO(4);

    private final int codigo;

    ConceptoVentaEnum(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static ConceptoVentaEnum fromCodigo(int codigo) {
        for (ConceptoVentaEnum e : values()) {
            if (e.codigo == codigo) {
                return e;
            }
        }
        throw new IllegalArgumentException("C�digo inv�lido: " + codigo);
    }
}