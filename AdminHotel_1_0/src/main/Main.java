package main;

import javax.swing.SwingUtilities;

import com.adminHotel.gui.MainFrame;

public class Main {

	public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new MainFrame().setVisible(true)
        );
    }

}
