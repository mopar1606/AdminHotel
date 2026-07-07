package com.adminHotel.util;

public enum EstadoHabitacionEnum {

    DISPONIBLE(1),
    OCUPADA(2),
    POR_ASEO(3),
    NO_DISPONIBLE(4),
    RESERVADA(5);

    private final int codigo;

    EstadoHabitacionEnum(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static EstadoHabitacionEnum fromCodigo(int codigo) {
        for (EstadoHabitacionEnum e : values()) {
            if (e.codigo == codigo) {
                return e;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }
}
