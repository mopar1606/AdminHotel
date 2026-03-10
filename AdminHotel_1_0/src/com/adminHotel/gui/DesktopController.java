package com.adminHotel.gui;

import javax.swing.JDesktopPane;

public class DesktopController {

    private static JDesktopPane desktopPane;

    private static ClienteInternalFrame clienteFrame;
    private static HabitacionInternalFrame habitacionFrame;
    private static AdminProductosInternalFrame adminProductosFrame;
    private static VentaMostradorInternalFrame ventaMostradorFrame;

    public static void setDesktopPane(JDesktopPane pane) {
        desktopPane = pane;
    }

    private static void cerrarTodos() {
        desktopPane.removeAll();
        desktopPane.repaint();
    }

    public static void abrirCliente() {
        cerrarTodos();
        clienteFrame = new ClienteInternalFrame();
        desktopPane.add(clienteFrame);
        clienteFrame.setVisible(true);
    }

    public static void abrirHabitacion() {
        cerrarTodos();
        habitacionFrame = new HabitacionInternalFrame();
        desktopPane.add(habitacionFrame);
        habitacionFrame.setVisible(true);
    }
    
    public static void abrirVentaMostrador() {
        cerrarTodos();
        ventaMostradorFrame = new VentaMostradorInternalFrame();
        desktopPane.add(ventaMostradorFrame);
        ventaMostradorFrame.setVisible(true);
    }
    
    public static void abrirAdminProductos() {
        cerrarTodos();
        adminProductosFrame = new AdminProductosInternalFrame();
        desktopPane.add(adminProductosFrame);
        adminProductosFrame.setVisible(true);
    }
}