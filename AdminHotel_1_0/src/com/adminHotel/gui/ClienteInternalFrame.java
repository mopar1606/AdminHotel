package com.adminHotel.gui;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ClienteInternalFrame extends JInternalFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4482038715805909180L;

	public ClienteInternalFrame() {

        super("Gestión de Clientes",
                true, true, true, true);

        setSize(800, 500);
        setLocation(50, 50);

        JPanel panel = new JPanel();
        panel.add(new JLabel("CRUD de Clientes aquí"));

        add(panel);
    }
}