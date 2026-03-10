package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.adminHotel.util.ModuloEnum;
import com.adminHotel.util.SesionUsuario;

public class MainFrame extends JFrame {
    
	private static final long serialVersionUID = -4140128927858159336L;
	private JDesktopPane desktopPane;
	private Font fuenteMenu = new Font("Arial", Font.BOLD, 20);
	private JToolBar toolBar;
	
	public MainFrame() {
		setTitle("IS - HOTEL LAS TERRAZAS II");
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

    /*public MainFrame() {

        setTitle("IS - HOTEL LAS TERRAZAS II ");
        
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        desktopPane = new JDesktopPane();
        setContentPane(desktopPane);

        DesktopController.setDesktopPane(desktopPane);

        setJMenuBar(crearMenu());
    }*/
	
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
            System.err.println("No se encontró el icono: " + iconName);
        }

        btn.setToolTipText(texto);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setFocusable(false);
        btn.addActionListener(accion);
        
        // Propiedad específica de Substance para botones planos en Toolbar
        btn.putClientProperty("substancelaf.buttonFlat", Boolean.TRUE);
        
        return btn;
    }
    
    private JPanel crearStatusBar() {
    	JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        /*JLabel lblUser = new JLabel("CONECTADO: " + SesionUsuario.getNombreUsuario().toUpperCase());
        lblUser.setFont(new Font("Arial", Font.ITALIC, 12));
        lblUser.setForeground(new Color(50, 50, 50));*/
        
        //panel.add(lblUser);
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

    private JMenuBar crearMenu() {

        JMenuBar menuBar = new JMenuBar();

        // MENÚ PRINCIPAL
        JMenu menuSistema = new JMenu("Sistema");
        menuSistema.setFont(fuenteMenu);

        // SUBMENÚ CLIENTES
        JMenu menuClientes = new JMenu("Clientes");
        menuClientes.setFont(fuenteMenu);
        JMenuItem itemClientes = new JMenuItem("Administrar Clientes");
        itemClientes.setFont(fuenteMenu);
        itemClientes.addActionListener(e ->
                DesktopController.abrirCliente()
        );
        menuClientes.add(itemClientes);

        // SUBMENÚ HABITACIONES
        JMenu menuHabitaciones = new JMenu("Habitaciones");
        menuHabitaciones.setFont(fuenteMenu);
        JMenuItem itemHabitaciones = new JMenuItem("Administrar Habitaciones");
        itemHabitaciones.setFont(fuenteMenu);
        itemHabitaciones.addActionListener(e ->
                DesktopController.abrirHabitacion()
        );
        menuHabitaciones.add(itemHabitaciones);

        // SALIR
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.setFont(fuenteMenu);
        itemSalir.addActionListener(e -> System.exit(0));

        // ARMADO DEL MENÚ
        menuSistema.add(menuClientes);
        menuSistema.add(menuHabitaciones);
        menuSistema.addSeparator();
        menuSistema.add(itemSalir);
        
        // --- NUEVO: MENÚ MOSTRADOR (Ventas Rápidas) ---
        JMenu menuMostrador = new JMenu("Mostrador");
        menuMostrador.setFont(fuenteMenu);

        JMenuItem itemVentaMostrador = new JMenuItem("Nueva Venta Mostrador");
        itemVentaMostrador.setFont(fuenteMenu);
        itemVentaMostrador.addActionListener(e -> {
            // Aquí llamarás al DesktopController para abrir la ventana de venta
            DesktopController.abrirVentaMostrador();
        });

        menuMostrador.add(itemVentaMostrador);
        
     // --- NUEVO: MENÚ INVENTARIO (Administración) ---
        JMenu menuInventario = new JMenu("Inventario");
        menuInventario.setFont(fuenteMenu);

        JMenuItem itemAdminProductos = new JMenuItem("Administrar Productos");
        itemAdminProductos.setFont(fuenteMenu);
        itemAdminProductos.addActionListener(e -> {
            // Aquí llamarás al DesktopController para abrir el CRUD de productos
            DesktopController.abrirAdminProductos();
        });

        menuInventario.add(itemAdminProductos);

        // --- ARMADO FINAL ---
        menuBar.add(menuSistema);
        menuBar.add(menuMostrador);
        menuBar.add(menuInventario);

        return menuBar;
    }
}