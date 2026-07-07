package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.adminHotel.util.ModuloEnum;
import com.adminHotel.util.SesionUsuario;

public class MainFrame extends JFrame {
    
	private static final long serialVersionUID = -4140128927858159336L;
	private JDesktopPane desktopPane;
	private JToolBar toolBar;
	
	public MainFrame() {
		setTitle("IS - HOTEL LAS TERRAZAS II");
		
		java.awt.GraphicsConfiguration gc = getGraphicsConfiguration();
	    java.awt.Insets screenInsets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(gc);
	    java.awt.Rectangle screenBounds = gc.getBounds();
	    int width = screenBounds.width - screenInsets.left - screenInsets.right;
	    int height = screenBounds.height - screenInsets.top - screenInsets.bottom;
	    
	    setSize(width, height);
	    setLocation(screenInsets.left, screenInsets.top);
	    setExtendedState(JFrame.MAXIMIZED_BOTH);
		
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    getContentPane().setLayout(new BorderLayout());
	    desktopPane = new JDesktopPane();
	    desktopPane.setBackground(new Color(228, 236, 247)); 
	    DesktopController.setDesktopPane(desktopPane);
	    getContentPane().add(desktopPane, BorderLayout.CENTER);
	    toolBar = crearToolBar();
	    getContentPane().add(toolBar, BorderLayout.NORTH);
	    getContentPane().add(crearStatusBar(), BorderLayout.SOUTH);
	    this.revalidate();
	    this.repaint();
	}

	private JToolBar crearToolBar() {
    	JToolBar tb = new JToolBar();
    	tb.setFloatable(false);
        tb.setRollover(true);
        tb.setPreferredSize(new Dimension(0, 90));
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 130, 180)));
        
        if (SesionUsuario.tienePermiso(ModuloEnum.CONTROL_HABITACIONES.getCodigo())) {
        	tb.add(Box.createHorizontalGlue());        
            tb.add(crearBoton("HABITACIONES", "/com/adminHotel/gui/complements/rooms.png", e -> abrirHabitaciones()));
        }
        
        if (SesionUsuario.tienePermiso(ModuloEnum.VENTAS_VITRINA.getCodigo())) {
        	tb.add(Box.createHorizontalGlue());
        	tb.add(crearBoton("VITRINA", "/com/adminHotel/gui/complements/store.png", e -> abrirMostrador()));
        }
        
        if (SesionUsuario.tienePermiso(ModuloEnum.INVENTARIO.getCodigo())) {
        	tb.add(Box.createHorizontalGlue());
        	tb.add(crearBoton("INVENTARIO", "/com/adminHotel/gui/complements/inventory.png", e -> abrirInventario()));
        }

        if (SesionUsuario.tienePermiso(ModuloEnum.CONSUMIBLES.getCodigo())) {
            tb.add(Box.createHorizontalGlue());
            tb.add(crearBoton("CONSUMIBLES", "/com/adminHotel/gui/complements/consumables.png", e -> abrirConsumibles()));
        }
        
        tb.add(Box.createHorizontalGlue());
        tb.add(crearBoton("BOX", "/com/adminHotel/gui/complements/box.png", e -> abrirCajaRegistradora()));
        
        if (SesionUsuario.tienePermiso(ModuloEnum.REPORTS.getCodigo())) {
        	tb.add(Box.createHorizontalGlue());
            tb.add(crearBoton("REPORTES", "/com/adminHotel/gui/complements/report.png", e -> abrirAdminInformes()));
        }
        
        if (SesionUsuario.tienePermiso(ModuloEnum.CONFIG_HOTEL.getCodigo())) {
        	tb.add(Box.createHorizontalGlue());
            tb.add(crearBoton("CONFIGURACIONES", "/com/adminHotel/gui/complements/config.png", e -> abrirAdminConfigParams()));
        }

        // Espacio para empujar el perfil a la derecha
        tb.add(Box.createHorizontalGlue());
        tb.add(crearBoton("SALIR", "/com/adminHotel/gui/complements/exit.png", e -> System.exit(0)));
        
        tb.add(Box.createHorizontalGlue());

        return tb;
    }
    
    private JButton crearBoton(String texto, String iconName, java.awt.event.ActionListener accion) {
        JButton btn = new JButton();
        
        // Intenta cargar el icono
        try {
        	java.net.URL url = getClass().getResource(iconName);
    		if (url != null) {
    			btn.setIcon(new ImageIcon(url));
    		}
        } catch (Exception e) {
            System.err.println("No se encontr? el icono: " + iconName);
        }

        btn.setToolTipText(texto);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setFocusable(false);
        btn.addActionListener(accion);
        
        // Propiedad espec?fica de Substance para botones planos en Toolbar
        btn.putClientProperty("substancelaf.buttonFlat", Boolean.TRUE);
        
        return btn;
    }
    
    private JPanel crearStatusBar() {
    	JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());
        return panel;
    }
    
    private void abrirHabitaciones() {
    	DesktopController.abrirHabitacion();
    }
    
    private void abrirMostrador() {
    	DesktopController.abrirVentaMostrador();
    }
    
    private void abrirInventario() {
    	DesktopController.abrirAdminProductos();
    }

    private void abrirConsumibles() {
        DesktopController.abrirAdminConsumibles();
    }
    
    private void abrirCajaRegistradora() {
    	DesktopController.abrirCajaRegistradora();
    }
    
    private void abrirAdminInformes() {
    	DesktopController.abrirAdminInformes();
    }
    
    private void abrirAdminConfigParams() {
    	DesktopController.abrirAdminConfigParams();
    }
}
