package com.adminHotel.util;

public enum ModuloEnum {
	
	INVENTARIO(1),
	CONTROL_HABITACIONES(2),
	VENTAS_VITRINA(3),
	ADMIN_USUARIOS(4);

    private final int codigo;

    ModuloEnum(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static ModuloEnum fromCodigo(int codigo) {
        for (ModuloEnum e : values()) {
            if (e.codigo == codigo) {
                return e;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }
}
