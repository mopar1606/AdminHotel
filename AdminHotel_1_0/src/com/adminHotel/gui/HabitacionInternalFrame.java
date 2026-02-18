package com.adminHotel.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.adminHotel.dao.ClienteDAO;
import com.adminHotel.dao.HabitacionDAO;
import com.adminHotel.dao.HabitacionMovimientoDAO;
import com.adminHotel.dao.PagoDAO;
import com.adminHotel.dao.VentaDAO;
import com.adminHotel.dao.VentaDetalleDAO;
import com.adminHotel.service.CheckInService;
import com.adminHotel.util.DBConnection;
import com.adminHotel.util.EstadoBaseEnum;
import com.adminHotel.vo.ClienteVO;
import com.adminHotel.vo.HabitacionVO;
import com.adminHotel.vo.PagoVO;
import com.adminHotel.vo.VentaVO;

public class HabitacionInternalFrame extends JInternalFrame {

    private static final long serialVersionUID = -6216033766678853720L;

	private JPanel panelHabitaciones;
    private JPanel panelCentro;

    private HabitacionVO habitacionSeleccionada;

    // ------------------------------------------------
    // INSTANCIA INTERNAL FRAME HABITACION
    // ------------------------------------------------
    public HabitacionInternalFrame() {
        super("Mapa de Habitaciones", true, true, true, true);
        
        setLayout(new BorderLayout());
        
        SwingUtilities.invokeLater(() -> {

            JDesktopPane desktop = getDesktopPane();

            if (desktop != null) {
                Dimension size = desktop.getSize();

                int width = (int) (size.width * 0.90);
                int height = (int) (size.height * 0.90);

                setSize(width, height);
                setLocation((size.width - width) / 2, (size.height - height) / 2);
            }
        });

        inicializarPanelHabitaciones();
        inicializarPanelCentro();
    }

    // ------------------------------------------------
    // PANEL NORTE - HABITACIONES
    // ------------------------------------------------
    private void inicializarPanelHabitaciones() {

        panelHabitaciones = new JPanel();
        panelHabitaciones.setLayout(new BoxLayout(panelHabitaciones, BoxLayout.Y_AXIS));
        panelHabitaciones.setBorder(BorderFactory.createTitledBorder("Habitaciones"));

        cargarHabitaciones();

        JScrollPane scroll = new JScrollPane(panelHabitaciones);
        scroll.setBorder(null);

        add(scroll, BorderLayout.NORTH);
    }
    
    // ------------------------------------------------
    // CARGA DESDE BD Y AGRUPA POR PISO
    // ------------------------------------------------
    private void cargarHabitaciones() {

    	Connection cn = null;
    	try {
    		
    		cn = DBConnection.getConnection();
            cn.setAutoCommit(false);
            
            HabitacionDAO habitacionDAO = new HabitacionDAO(cn);

            List<HabitacionVO> lista = habitacionDAO.listarHabitaciones();

            // Agrupar por piso
            Map<Integer, List<HabitacionVO>> pisos = new TreeMap<>();

            for (HabitacionVO h : lista) {
                pisos.computeIfAbsent(h.getPiso(), k -> new ArrayList<>()).add(h);
            }

            // Crear UI por cada piso
            for (Integer piso : pisos.keySet()) {
                crearPiso(panelHabitaciones, piso, pisos.get(piso));
            }

        } catch (Exception e) {
        	e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error cargando habitaciones\n" + e);
        }
    }
    
    // -------------------------------
    // CREA BLOQUE DE PISO DINÁMICO
    // -------------------------------
    private void crearPiso(JPanel contenedor, Integer numeroPiso, List<HabitacionVO> habitaciones) {

        JPanel panelPiso = new JPanel(new BorderLayout());
        panelPiso.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // =========================
        // ICONO DEL PISO
        // =========================
        String rutaIcono = "/com/adminHotel/gui/complements/P" + numeroPiso + ".png";

        JLabel lblIcono = new JLabel();
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);

        try {
            lblIcono.setIcon(new ImageIcon(getClass().getResource(rutaIcono)));
        } catch (Exception e) {
            lblIcono.setText("P" + numeroPiso);
        }

        JButton botonTemp = new JButton();
        botonTemp.setPreferredSize(new Dimension(90, 70));

        lblIcono.setPreferredSize(new Dimension(50, botonTemp.getPreferredSize().height + 20));

        panelPiso.add(lblIcono, BorderLayout.WEST);

        // =========================
        // HABITACIONES
        // =========================
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        for (HabitacionVO h : habitaciones) {
            panelBotones.add(crearBotonHabitacion(h));
        }

        panelPiso.add(panelBotones, BorderLayout.CENTER);

        panelPiso.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedor.add(panelPiso);
    }

    // ----------------------------------
    // BOTÓN HABITACIÓN
    // ----------------------------------
    private JButton crearBotonHabitacion(HabitacionVO h) {

        JButton boton = new JButton(String.valueOf(h.getNumeroHabitacion()));

        boton.setPreferredSize(new Dimension(90, 70));
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);

        // =========================
        // ESTADO
        // =========================
        String estado = h.getEstadoDescripcion();

        if (estado == null) estado = "DISPONIBLE";

        String rutaIcono;

        switch (estado) {

            case "OCUPADA":
                boton.setBackground(new Color(231, 76, 60));
                rutaIcono = "/com/adminHotel/gui/complements/ocupada.png";
                break;

            case "POR ASEO":
                boton.setBackground(new Color(243, 156, 18));
                rutaIcono = "/com/adminHotel/gui/complements/limpiar.png";
                break;
                
            case "NO DISPONIBLE":
                boton.setBackground(new Color(181, 176, 176));
                rutaIcono = "/com/adminHotel/gui/complements/nosirve.png";
                break;

            default:
                boton.setBackground(new Color(46, 204, 113));
                rutaIcono = "/com/adminHotel/gui/complements/disponible.png";
                break;
        }

        // =========================
        // ICONO SEGURO
        // =========================
        java.net.URL url = getClass().getResource(rutaIcono);
        if (url != null) {
            boton.setIcon(new ImageIcon(url));
        }

        boton.setHorizontalTextPosition(SwingConstants.CENTER);
        boton.setVerticalTextPosition(SwingConstants.BOTTOM);

        boton.addActionListener(e -> mostrarDetalleHabitacion(h));

        return boton;
    }

    // -------------------------------
    // PANEL CENTRAL
    // -------------------------------
    private void inicializarPanelCentro() {

        panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBorder(BorderFactory.createTitledBorder("Detalle y Operaciones"));

        JLabel lbl = new JLabel("Seleccione una habitación para ver el detalle", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 16));
        panelCentro.add(lbl, BorderLayout.CENTER);

        add(panelCentro, BorderLayout.CENTER);
    }
    
    // 	-------------------------------
    // MOSTRAR DETALLE HABITACION
    // -------------------------------
    private void mostrarDetalleHabitacion(HabitacionVO habitacion) {
    	
    	habitacionSeleccionada = habitacion;

        panelCentro.removeAll();

        JPanel contenedor = new JPanel();
        contenedor.setLayout(new GridLayout(1, 2, 10, 10));
        contenedor.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        contenedor.add(crearPanelInfo(habitacion));
        contenedor.add(crearPanelOperaciones(habitacion));

        panelCentro.add(contenedor, BorderLayout.CENTER);

        panelCentro.revalidate();
        panelCentro.repaint();
    }
    
    // 	-------------------------------
    // MOSTRAR DETALLE HABITACION
    // -------------------------------
    private JPanel crearPanelInfo(HabitacionVO h) {

        JPanel panel = new JPanel(new GridLayout(0,2,5,5));
        panel.setBorder(BorderFactory.createTitledBorder("Información"));

        panel.add(new JLabel("Número:"));
        panel.add(new JLabel(String.valueOf(h.getNumeroHabitacion())));

        panel.add(new JLabel("Piso:"));
        panel.add(new JLabel(String.valueOf(h.getPiso())));

        panel.add(new JLabel("Tipo:"));
        panel.add(new JLabel(h.getTipoDescripcion() != null ? h.getTipoDescripcion() : "-"));

        panel.add(new JLabel("Estado:"));
        panel.add(new JLabel(h.getEstadoDescripcion() != null ? h.getEstadoDescripcion() : "-"));

        panel.add(new JLabel("Descripción:"));
        panel.add(new JLabel(h.getDescripcion() != null ? h.getDescripcion() : "-"));

        return panel;
    }
    
    // -------------------------------
    // OPERACIONES
    // -------------------------------
    private JPanel crearPanelOperaciones(HabitacionVO h) {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        panel.setBorder(BorderFactory.createTitledBorder("Operaciones"));

        JButton btnAccion = new JButton();
        btnAccion.setPreferredSize(new Dimension(180, 40));

        String estado = h.getEstadoDescripcion();

        if (estado == null) estado = "DISPONIBLE";

        switch (estado) {

            case "DISPONIBLE":
                btnAccion.setText("Asignar");
                btnAccion.addActionListener(e -> realizarCheckInUI(h));
                break;

            case "OCUPADA":
            	btnAccion.setText("Check-out");
                btnAccion.addActionListener(e -> realizarCheckInUI(h));
                break;

            case "POR ASEO":
                btnAccion.setText("Marcar disponible");
                Connection cn = null;
                btnAccion.addActionListener(e -> {
					try {
						cambiarEstadoHabitacion(cn, h, "DISPONIBLE");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				});
                break;

            default:
                btnAccion.setVisible(false);
                break;
        }

        panel.add(btnAccion);

        return panel;
    }
    
    // -------------------------------
    // CHECK IN
    // -------------------------------
    private void realizarCheckInUI(HabitacionVO habitacion) {

        try {

            String documento = JOptionPane.showInputDialog(this, "Número Documento:");

            if (documento == null || documento.trim().isEmpty()) return;

            ClienteVO nuevoCliente = null;

            int opcion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Registrar pago ahora?",
                    "Confirmar Pago",
                    JOptionPane.YES_NO_OPTION
            );

            boolean registrarPago = (opcion == JOptionPane.YES_OPTION);

            CheckInService service = new CheckInService();

            service.realizarCheckIn(
                    habitacion,
                    documento,
                    nuevoCliente,
                    registrarPago
            );

            // SOLO UI DESPUÉS DEL ÉXITO
            refrescarMapaHabitaciones();
            JOptionPane.showMessageDialog(this, "Check-in realizado correctamente");

        } catch (Exception ex) {

            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error en check-in\n" + ex.getMessage());
        }
    }
    
    
    private void realizarCheckIn(HabitacionVO habitacion) {
    	
    	Connection cn = null;
    	try {
    		
    		cn = DBConnection.getConnection();
            cn.setAutoCommit(false);
            
            // =============================
            // PEDIR DOCUMENTO
            // =============================
            String documento = JOptionPane.showInputDialog(this, "Numero Documento del cliente:");
            if (documento == null || documento.trim().isEmpty()) return;

            ClienteDAO clienteDAO = new ClienteDAO(cn);
            ClienteVO cliente = clienteDAO.buscarPorDocumento(documento);
            
            // =============================
            // SI NO EXISTE -> CREAR
            // =============================
            if (cliente == null) {

                JTextField txtNombre = new JTextField();
                JTextField txtTelefono = new JTextField();
                JTextField txtEmail = new JTextField();

                Object[] campos = {
                        "Nombre:", txtNombre,
                        "Teléfono:", txtTelefono,
                        "Email:", txtEmail
                };

                int op = JOptionPane.showConfirmDialog(this, campos, "Registrar Cliente", JOptionPane.OK_CANCEL_OPTION);

                if (op != JOptionPane.OK_OPTION) return;

                cliente = new ClienteVO();
                cliente.setDocumento(documento);
                cliente.setNombre(txtNombre.getText());
                cliente.setTelefono(txtTelefono.getText());
                cliente.setEmail(txtEmail.getText());

                int idGenerado = clienteDAO.insertar(cliente);
                cliente.setIdCliente(idGenerado);
            }
            
            // =============================
            // CAPTURAR PAGO (SIN GUARDAR)
            // =============================            
            PagoVO pago = solicitarPago(cliente, habitacion);

            if (pago == null || pago.getValor() == BigDecimal.ZERO) {
                JOptionPane.showMessageDialog(this, "No se registró el pago...");
                return;
            }
            
            // =============================
            // CREAR VENTA
            // =============================
            VentaDAO ventaDAO = new VentaDAO(cn);

            VentaVO venta = new VentaVO();
            venta.setIdCliente(cliente.getIdCliente());
            venta.setTotal(pago.getValor());
            venta.setObservacion("Check-in habitación " + habitacion.getNumeroHabitacion());

            int idVenta = ventaDAO.insertar(venta);
            venta.setIdVenta(idVenta);
            
            // =============================
            // CREAR DETALLE
            // =============================
            VentaDetalleDAO detalleDAO = new VentaDetalleDAO(cn);
            detalleDAO.insertarDetalleHabitacion(venta, habitacion);
            
            // =============================
            // AHORA SI -> GUARDAR PAGO
            // =============================
            pago.setIdVenta(idVenta);

            PagoDAO pagoDAO = new PagoDAO(cn);
            if (pago != null) {            	
                pagoDAO.insertar(pago);
            }
            
            // =============================
            // REGISTRAR MOVIMIENTO
            // =============================
            HabitacionMovimientoDAO movDAO = new HabitacionMovimientoDAO(cn);
            movDAO.registrarEntrada(habitacion.getIdHabitacion(), cliente.getIdCliente());
            
            // =============================
            // CAMBIAR ESTADO
            // =============================
            cambiarEstadoHabitacion(cn, habitacion, "OCUPADA");
            
            // =============================
            // TODO OK -> COMMIT
            // =============================
            cn.commit();

            JOptionPane.showMessageDialog(this, "Check-in realizado correctamente");
    		
    	} catch (Exception ex) {

            try {
                if (cn != null) cn.rollback();
            } catch (Exception ignored) {}

            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error en check-in\n" + ex.getMessage());

        } finally {
            try {
                if (cn != null) cn.setAutoCommit(true);
            } catch (Exception ignored) {}
        }       
    }
    
	// -------------------------------
    // CAMBIO DE ESTADO
    // -------------------------------    
    private void cambiarEstadoHabitacion(Connection cn, HabitacionVO h, String nuevoEstado) throws Exception {
    	
    	boolean conexionPropia = false;

        try {
            // =============================
            // VALIDAR CONEXIÓN
            // =============================
            if (cn == null) {
                cn = DBConnection.getConnection();
                cn.setAutoCommit(false);
                conexionPropia = true; // esta conexión la maneja este método
            }
            
            HabitacionDAO habitacionDAO = new HabitacionDAO(cn);

            // animación simple mientras actualiza
            panelCentro.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            int idEstado = obtenerIdEstado(nuevoEstado);

            //  actualizar BD
            habitacionDAO.actualizarEstado(h.getIdHabitacion(), idEstado);

            // recargar todo el mapa
            refrescarMapaHabitaciones();

            // volver a consultar habitación actualizada
            List<HabitacionVO> lista = habitacionDAO.listarActivas();

            for (HabitacionVO hab : lista) {
                if (hab.getIdHabitacion() == h.getIdHabitacion()) {
                    mostrarDetalleHabitacion(hab);
                    break;
                }
            }

            // efecto visual
            JOptionPane.showMessageDialog(this, "Estado actualizado a " + nuevoEstado);
            
            if (conexionPropia) {
                cn.commit();
            }

        } catch (Exception e) {

            if (conexionPropia && cn != null) {
                try {
                    cn.rollback();
                } catch (Exception ignored) {}
            }

            throw e;

        } finally {

            if (conexionPropia && cn != null) {
                try {
                    cn.setAutoCommit(true);
                    cn.close();
                } catch (Exception ignored) {}
            }
        }
    }
    
    // -------------------------------
    // OBTENER ID ESTADO
    // -------------------------------
    private int obtenerIdEstado(String descripcion) {

        switch (descripcion) {
            case "DISPONIBLE": return 1;
            case "OCUPADA": return 2;
            case "POR ASEO": return 3;
            case "NO DISPONIBLE": return 4;
            default: return 1;
        }
    }
    
    // =============================
    // REFRESCAR MAPA
    // =============================
    private void refrescarMapaHabitaciones() {
		panelHabitaciones.removeAll();
		cargarHabitaciones();
		panelHabitaciones.revalidate();
		panelHabitaciones.repaint();
	}
    
    // =============================
    // CHECK OUT
    // =============================
    private void realizarCheckOut(HabitacionVO habitacion) {

    	Connection cn = null;
    	try {
    		
    		cn = DBConnection.getConnection();
            cn.setAutoCommit(false);

            int op = JOptionPane.showConfirmDialog(this,"Confirmar salida de la habitación " + habitacion.getNumeroHabitacion() + "?","Check-out",JOptionPane.YES_NO_OPTION);

            if (op != JOptionPane.YES_OPTION) return;

            HabitacionMovimientoDAO movDAO = new HabitacionMovimientoDAO(cn);

            // cerrar ocupación
            movDAO.registrarSalida(habitacion.getIdHabitacion());

            // cambiar estado
            cambiarEstadoHabitacion(cn, habitacion, "POR ASEO");

        } catch (Exception ex) {

            try {
                if (cn != null) cn.rollback();
            } catch (Exception ignored) {}

            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error en check-in\n" + ex.getMessage());

        } finally {
            try {
                if (cn != null) cn.setAutoCommit(true);
            } catch (Exception ignored) {}
        }
    }
    
    // =============================
    // SOLICITAR PAGO
    // =============================    
    private PagoVO solicitarPago(ClienteVO cliente, HabitacionVO habitacion) {
    	
    	int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿CLIENTE PAGO?",
                "CONFIRMAR PAGO",
                JOptionPane.YES_NO_OPTION
        );
        
    	if (opcion != JOptionPane.YES_OPTION) {
            return null;
        }

        // Validación defensiva (por seguridad)
        if (habitacion == null || habitacion.getPrecio() == null) {
            JOptionPane.showMessageDialog(this, 
                    "La habitación no tiene precio cargado.");
            return null;
        }

        BigDecimal valor = habitacion.getPrecio();
        
        PagoVO pago = new PagoVO();
        pago.setIdCliente(cliente.getIdCliente());
        pago.setIdHabitacion(habitacion.getIdHabitacion());
        pago.setIdMetodo(EstadoBaseEnum.UNO.getCodigo());
        pago.setIdEstadoRegistro(EstadoBaseEnum.UNO.getCodigo());
        pago.setValor(valor);

        return pago;
    }
}