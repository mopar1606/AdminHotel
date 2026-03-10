package main;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.adminHotel.gui.LoginDialog;
import com.adminHotel.gui.MainFrame;

public class Main {

	public static void main(String[] args) {
		
		/*try {
	        // Configura el estilo tipo Office (Limpio y claro)
			UIManager.put("Component.accentColor", Color.decode("#4f81bd"));
		    UIManager.put("Button.arc", 8); 
		    UIManager.put("Component.focusColor", Color.decode("#d0e3f7"));
		    UIManager.put("Table.selectionBackground", Color.decode("#d0e3f7"));
		    UIManager.put("Table.selectionForeground", Color.BLACK);
		    
		    FlatIntelliJLaf.setup();
		    
		    SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
	        
	    } catch (Exception ex) {
	        System.err.println("Fallo al inicializar LaF");
	    }*/
		
		// 1. Configuración de seguridad para Substance
	    System.setProperty("sun.awt.noerasebackground", "true");
		try {
			// 2. Establecer el Look and Feel
	        UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel");
	        
	        // 3. Forzar decoración de ventanas (IMPORTANTE para que no se vea el estilo nativo)
	        JFrame.setDefaultLookAndFeelDecorated(true);
	        JDialog.setDefaultLookAndFeelDecorated(true);
	        
	        SwingUtilities.invokeLater(() -> {
                // 1. Mostrar Login
                LoginDialog login = new LoginDialog(null);
                login.setVisible(true);
                // 2. Si autenticó, abrir el sistema
                if (login.isAutenticado()) {
                    new MainFrame().setVisible(true);
                } else {
                    System.exit(0);
                }
            });

	        
	    } catch (Exception e) {
	        System.err.println("Error al cargar Substance: " + e.getMessage());
	    }
		
		/*UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 18));
        UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.BOLD, 18));*/
        
        /*LoginDialog login = new LoginDialog(null);
        login.setVisible(true);
        
        if (login.isAutenticado()) {
            EventQueue.invokeLater(() -> {
                try {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }*/
        
    }
}
