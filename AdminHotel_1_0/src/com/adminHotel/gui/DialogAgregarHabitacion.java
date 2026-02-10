package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.adminHotel.dao.HabitacionDAO;
import com.adminHotel.vo.HabitacionVO;

public class DialogAgregarHabitacion extends JDialog {

   	private static final long serialVersionUID = -354043588626337243L;
	private JTextField txtNumero;
    private JTextField txtPiso;
    private JComboBox<String> cmbEstado;
    private JCheckBox chkActiva;

    public DialogAgregarHabitacion() {
        setTitle("Agregar Habitación");
        setModal(true);
        setSize(350, 250);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        inicializarFormulario();
        inicializarBotones();
    }

    private void inicializarFormulario() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        panel.add(new JLabel("Número habitación:"));
        txtNumero = new JTextField();
        panel.add(txtNumero);

        panel.add(new JLabel("Piso:"));
        txtPiso = new JTextField();
        panel.add(txtPiso);

        panel.add(new JLabel("Estado:"));
        cmbEstado = new JComboBox<>(new String[]{
                "DISPONIBLE", "OCUPADA", "POR_LIMPIAR"
        });
        panel.add(cmbEstado);

        panel.add(new JLabel("Activa:"));
        chkActiva = new JCheckBox();
        chkActiva.setSelected(true);
        panel.add(chkActiva);

        add(panel, BorderLayout.CENTER);
    }

    private void inicializarBotones() {
        JPanel panelBotones = new JPanel();

        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");

        btnAceptar.addActionListener(e -> guardarHabitacion());
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);

        add(panelBotones, BorderLayout.SOUTH);
    }

    private void guardarHabitacion() {
        try {
            int numero = Integer.parseInt(txtNumero.getText());
            int piso = Integer.parseInt(txtPiso.getText());
            String estado = cmbEstado.getSelectedItem().toString();
            boolean activa = chkActiva.isSelected();

            HabitacionVO hab = new HabitacionVO();
            hab.setNumeroHabitacion(numero);
            hab.setPiso(piso);
            hab.setEstado(estado);
            hab.setActiva(activa);
            hab.setTipo("SENCILLA"); // por ahora fijo

            new HabitacionDAO().insertar(hab);

            JOptionPane.showMessageDialog(this,
                    "Habitación creada correctamente");

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al crear habitación: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}