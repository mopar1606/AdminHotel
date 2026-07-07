package com.adminHotel.gui;

import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;

import com.adminHotel.service.OperationService;

public class DesktopController {

    private static JDesktopPane desktopPane;

    private static ClienteInternalFrame clienteFrame;
    private static HabitacionInternalFrame habitacionFrame;
    private static AdminProductosInternalFrame adminProductosFrame;
    private static VentaMostradorInternalFrame ventaMostradorFrame;
    private static AdminConsumiblesInternalFrame adminConsumiblesFrame;
    private static CajaRegistradoraInternalFrame cajaRegistradoraFrame;
    private static AdminInformesInternalFrame adminInformesFrame;
    private static AdminConfigParamsInternalFrame adminConfigParamsFrame;

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
    	
    	try {
            OperationService service = new OperationService();
            if (service.doGetTurnoActivo(1) == null) { // 1 = Caja Hotel
                JOptionPane.showMessageDialog(
                    desktopPane,
                    "La Caja de Hotel se encuentra CERRADA.\n" +
                    "Debe realizar la apertura de caja antes de operar.",
                    "HOTEL LAS TERRAZAS II - Caja Cerrada",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(desktopPane,
                "Error al verificar estado de caja: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    	
        cerrarTodos();
        habitacionFrame = new HabitacionInternalFrame();
        desktopPane.add(habitacionFrame);
        habitacionFrame.setVisible(true);
    }
    
    public static void abrirVentaMostrador() {
    	
    	try {
            OperationService service = new OperationService();
            if (service.doGetTurnoActivo(2) == null) { // 2 = Caja Mostrador
                JOptionPane.showMessageDialog(
                    desktopPane,
                    "La Caja de Mostrador se encuentra CERRADA.\n" +
                    "Debe realizar la apertura de caja antes de operar.",
                    "HOTEL LAS TERRAZAS II - Caja Cerrada",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(desktopPane,
                "Error al verificar estado de caja: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    	
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

    public static void abrirAdminConsumibles() {
        cerrarTodos();
        adminConsumiblesFrame = new AdminConsumiblesInternalFrame();
        desktopPane.add(adminConsumiblesFrame);
        adminConsumiblesFrame.setVisible(true);
    }
    
    public static void abrirCajaRegistradora() {
    	cerrarTodos();
    	cajaRegistradoraFrame = new CajaRegistradoraInternalFrame();
    	desktopPane.add(cajaRegistradoraFrame);
    	cajaRegistradoraFrame.setVisible(true);
    }
    
    public static void abrirAdminInformes() {
    	cerrarTodos();
    	adminInformesFrame = new AdminInformesInternalFrame();
    	desktopPane.add(adminInformesFrame);
    	adminInformesFrame.setVisible(true);
    }
    
    public static void abrirAdminConfigParams() {
    	cerrarTodos();
    	adminConfigParamsFrame = new AdminConfigParamsInternalFrame();
    	desktopPane.add(adminConfigParamsFrame);
    	adminConfigParamsFrame.setVisible(true);
    }
}
