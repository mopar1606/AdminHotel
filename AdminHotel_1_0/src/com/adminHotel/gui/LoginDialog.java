package com.adminHotel.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.adminHotel.dao.UsuarioDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.SesionUsuario;
import com.adminHotel.vo.UsuarioVO;

public class LoginDialog extends JDialog {
	
	private static final long serialVersionUID = 6892966079693807642L;
	private JTextField txtUsuario;
    private JPasswordField txtClave;
    private JButton btnEntrar, btnSalir;
    private boolean autenticado = false;
    private Font fuente = new Font("Arial", Font.BOLD, 20);
    private Font fuenteBorder = new Font("Arial", Font.BOLD, 16);
    
    public LoginDialog(Frame parent) {
        super(parent, "HOTEL LAS TERRAZAS II - Acceso al Sistema", true);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setUndecorated(true);
        
        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 2));
        panel.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("INICIO DE SESION", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(41, 128, 185));

        txtUsuario = new JTextField();
        txtUsuario.setFont(fuente);
        
        txtUsuario.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                "USUARIO", 0, 0, fuenteBorder, new Color(41, 128, 185)));

        txtClave = new JPasswordField();
        txtClave.setFont(fuente);
        txtClave.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                "CONTRASEÑA", 0, 0, fuenteBorder, new Color(41, 128, 185)));

        btnEntrar = new JButton("ENTRAR");
        btnEntrar.setFont(fuente);
        btnEntrar.setBackground(new Color(46, 204, 113));
        btnEntrar.setForeground(Color.BLACK);

        btnSalir = new JButton("CANCELAR");
        btnSalir.setFont(fuente);

        panel.add(lblTitulo);
        panel.add(txtUsuario);
        panel.add(txtClave);
        panel.add(btnEntrar);
        panel.add(btnSalir);

        add(panel);

        // Eventos
        btnEntrar.addActionListener(e -> validarAcceso());
        btnSalir.addActionListener(e -> System.exit(0));
        
        // Enter para entrar
        txtClave.addActionListener(e -> validarAcceso());
    }

    private void validarAcceso() {
        String user = txtUsuario.getText();
        String pass = new String(txtClave.getPassword());

        try (Connection cn = DBConnection.getConnection()) {
            UsuarioDAO dao = new UsuarioDAO(cn);
            UsuarioVO vo = dao.validar(user, pass);

            if (vo != null) {
                // GUARDAMOS EN LA SESI�N GLOBAL
                SesionUsuario.iniciarSesion(vo.getIdUsuario(), vo.getNombreCompleto(), vo.getPermisos());
                dao.registrarAuditoria(vo.getIdUsuario(), "LOGIN", "usuario", "El usuario [" + vo.getNombreCompleto() + "] inici� sesi�n en el sistema");
                autenticado = true;
                dispose(); // Cerramos el login
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o Clave incorrectos", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error de conexi�n: " + e.getMessage());
        }
    }

    public boolean isAutenticado() { return autenticado; }
}
