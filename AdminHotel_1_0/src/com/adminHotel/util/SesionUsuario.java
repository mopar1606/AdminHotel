package com.adminHotel.util;

import java.util.ArrayList;
import java.util.List;

public class SesionUsuario {
	
	private static int idUsuario;
    private static String nombreUsuario;
    private static List<Integer> permisos = new ArrayList<>();
    
    public static void iniciarSesion(int id, String nombre, List<Integer> listaPermisos) {
        idUsuario = id;
        nombreUsuario = nombre;
        permisos = (listaPermisos != null) ? listaPermisos : new ArrayList<>();
    }
    
    public static int getIdUsuario() {
        return idUsuario;
    }

    public static String getNombreUsuario() {
        return nombreUsuario;
    }
    
    /**
     * Verifica si el usuario actual tiene permiso para un módulo.
     * Ejemplo: SesionUsuario.tienePermiso("INVENTARIO")
     */
    public static boolean tienePermiso(Integer modulo) {
        return permisos.contains(modulo);
    }
    
    // Para cerrar el sistema
    public static void cerrarSesion() {
        idUsuario = 0;
        nombreUsuario = null;
        permisos.clear();
    }
}
