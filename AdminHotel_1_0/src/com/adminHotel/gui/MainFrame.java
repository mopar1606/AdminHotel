package com.adminHotel.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainFrame extends JFrame {
    
	private static final long serialVersionUID = -4140128927858159336L;
	private JDesktopPane desktopPane;

    public MainFrame() {

        setTitle("Hotel Management System");
        
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        desktopPane = new JDesktopPane();
        setContentPane(desktopPane);

        DesktopController.setDesktopPane(desktopPane);

        setJMenuBar(crearMenu());
    }

    private JMenuBar crearMenu() {

        JMenuBar menuBar = new JMenuBar();

        // MENÚ PRINCIPAL
        JMenu menuSistema = new JMenu("Sistema");

        // SUBMENÚ CLIENTES
        JMenu menuClientes = new JMenu("Clientes");
        JMenuItem itemClientes = new JMenuItem("Administrar Clientes");
        itemClientes.addActionListener(e ->
                DesktopController.abrirCliente()
        );
        menuClientes.add(itemClientes);

        // SUBMENÚ HABITACIONES
        JMenu menuHabitaciones = new JMenu("Habitaciones");
        JMenuItem itemHabitaciones = new JMenuItem("Administrar Habitaciones");
        itemHabitaciones.addActionListener(e ->
                DesktopController.abrirHabitacion()
        );
        menuHabitaciones.add(itemHabitaciones);

        // SALIR
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));

        // ARMADO DEL MENÚ
        menuSistema.add(menuClientes);
        menuSistema.add(menuHabitaciones);
        menuSistema.addSeparator();
        menuSistema.add(itemSalir);

        menuBar.add(menuSistema);

        return menuBar;
    }
}