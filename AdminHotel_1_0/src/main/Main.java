package main;

import java.awt.Font;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.adminHotel.gui.LoginDialog;
import com.adminHotel.gui.MainFrame;

public class Main {

	public static void main(String[] args) {
	    
	    // Configurar fuentes globales primero (liviano)
	    UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 20));
	    UIManager.put("TextField.font", new Font("Arial", Font.BOLD, 20));
	    UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.BOLD, 20));
	    UIManager.put("TableHeader.font", new Font("Arial", Font.BOLD, 20));
	    System.setProperty("sun.awt.noerasebackground", "true");
	    // Cargar el L&F pesado ANTES de entrar al EDT para no bloquear
	    try {
	        UIManager.setLookAndFeel(
	            "org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel"
	        );
	        JFrame.setDefaultLookAndFeelDecorated(true);
	        JDialog.setDefaultLookAndFeelDecorated(true);
	    } catch (Exception e) {
	        System.err.println("Error al cargar Substance: " + e.getMessage());
	    }
	    // Luego sí al EDT
	    SwingUtilities.invokeLater(() -> {
	        LoginDialog login = new LoginDialog(null);
	        login.setVisible(true);
	        if (login.isAutenticado()) {
	            new MainFrame().setVisible(true);
	        } else {
	            System.exit(0);
	        }
	    });
	}
}
