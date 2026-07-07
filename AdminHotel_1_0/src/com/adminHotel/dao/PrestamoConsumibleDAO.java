package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.adminHotel.vo.PrestamoConsumibleVO;

public class PrestamoConsumibleDAO {

    private Connection cn;

    public PrestamoConsumibleDAO(Connection cn) {
        this.cn = cn;
    }

    // ===============================
    // INSERTAR PRESTAMO (CHECK-IN)
    // ===============================
    public Integer insertar(PrestamoConsumibleVO prestamo) throws Exception {
        
        String sql = "INSERT INTO prestamo_consumible " +
                     "(id_consumible, id_habitacion_movimiento, cantidad_entregada, " +
                     "cantidad_devuelta, estado, id_estado_registro) " +
                     "VALUES (?, ?, ?, ?, ?, 1)";
        
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, prestamo.getIdConsumible());
            ps.setInt(2, prestamo.getIdHabitacionMovimiento());
            ps.setInt(3, prestamo.getCantidadEntregada());
            ps.setInt(4, prestamo.getCantidadDevuelta() != null ? prestamo.getCantidadDevuelta() : 0);
            ps.setString(5, prestamo.getEstado() != null ? prestamo.getEstado() : "PRESTADO");
            
            int filasAfectadas = ps.executeUpdate();
            
            // Obtener el ID generado (id_prestamo)
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Integer idGenerado = rs.getInt(1);
                    return idGenerado;
                } else {
                    throw new Exception("No se pudo obtener el ID del préstamo insertado");
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error al insertar préstamo: " + e.getMessage());
        }
    }

    // ===============================
    // ACTUALIZAR PRESTAMO (CHECK-OUT)
    // ===============================
    public void actualizarDevolucion(PrestamoConsumibleVO prestamo) throws Exception {
        String sql = "UPDATE prestamo_consumible SET " +
                     "cantidad_devuelta = ?, estado = ? " +
                     "WHERE id_prestamo = ?";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, prestamo.getCantidadDevuelta());
            ps.setString(2, prestamo.getEstado());
            ps.setInt(3, prestamo.getIdPrestamo());
            ps.executeUpdate();
        }
    }

    // ===============================
    // LISTAR PRESTAMOS ACTIVOS DE UNA HABITACI�N
    // ===============================
    public List < PrestamoConsumibleVO > listarPrestamosActivos(Integer idMovimiento) throws Exception {
        List < PrestamoConsumibleVO > lista = new ArrayList < > ();

        String sql = "SELECT p.id_prestamo, p.id_habitacion_movimiento, p.id_consumible, " +
            "p.cantidad_entregada, p.cantidad_devuelta, p.estado, " +
            "c.nombre, c.precio_multa " +
            "FROM prestamo_consumible p " +
            "INNER JOIN consumible c ON c.id_consumible = p.id_consumible " +
            "WHERE p.id_habitacion_movimiento = ? AND p.estado = 'PRESTADO'";
        
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMovimiento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PrestamoConsumibleVO vo = new PrestamoConsumibleVO();
                    vo.setIdPrestamo(rs.getInt("id_prestamo"));
                    vo.setIdHabitacionMovimiento(rs.getInt("id_habitacion_movimiento"));
                    vo.setIdConsumible(rs.getInt("id_consumible"));
                    vo.setCantidadEntregada(rs.getInt("cantidad_entregada"));
                    vo.setCantidadDevuelta(rs.getInt("cantidad_devuelta"));
                    vo.setEstado(rs.getString("estado"));
                    vo.setNombreConsumible(rs.getString("nombre"));
                    vo.setPrecioMulta(rs.getBigDecimal("precio_multa"));
                    lista.add(vo);
                }
            }
        }
        return lista;
    }

    // ===============================
    // LISTAR TODOS LOS PRESTAMOS ACTIVOS (PARA CAJA REGISTRADORA)
    // ===============================
    public List < PrestamoConsumibleVO > listarTodosLosActivos() throws Exception {
    	
        List < PrestamoConsumibleVO > lista = new ArrayList < > ();
        String sql = "SELECT p.id_prestamo, p.id_consumible, " +
            "p.cantidad_entregada, p.cantidad_devuelta, p.estado, " +
            "c.nombre AS nombre_consumible, " +
            "h.numero_habitacion " +
            "FROM prestamo_consumible p " +
            "INNER JOIN consumible c ON c.id_consumible = p.id_consumible " +
            "INNER JOIN habitacion_movimiento hm ON hm.id_habitacion_movimiento = p.id_habitacion_movimiento " +
            "INNER JOIN habitacion h ON h.id_habitacion = hm.id_habitacion " +
            "WHERE p.estado = 'PRESTADO' " +
            "ORDER BY h.numero_habitacion";
        
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int fila = 1;
            while (rs.next()) {
            	
                PrestamoConsumibleVO vo = new PrestamoConsumibleVO();
                vo.setIdPrestamo(rs.getInt("id_prestamo"));
                vo.setIdConsumible(rs.getInt("id_consumible"));
                vo.setCantidadEntregada(rs.getInt("cantidad_entregada"));
                vo.setCantidadDevuelta(rs.getInt("cantidad_devuelta"));
                vo.setEstado(rs.getString("estado"));
                vo.setNombreConsumible(rs.getString("nombre_consumible"));
                // Reutilizamos idHabitacionMovimiento para pasar el número de habitación al VO
                vo.setIdHabitacionMovimiento(rs.getInt("numero_habitacion"));
                lista.add(vo);
                fila++;
            }
        }
        return lista;
    }

    /**
     * Busca todos los préstamos pendientes (no devueltos) para un movimiento de habitación
     */
    public List < PrestamoConsumibleVO > buscarPrestamosPendientesPorMovimiento(Integer idMovimiento) throws Exception {
        List < PrestamoConsumibleVO > lista = new ArrayList < > ();
        String sql = "SELECT pc.*, c.nombre as nombre_consumible " +
            "FROM prestamo_consumible pc " +
            "INNER JOIN consumible c ON pc.id_consumible = c.id_consumible " +
            "WHERE pc.id_habitacion_movimiento = ? " +
            "  AND (pc.estado IS NULL OR pc.estado NOT IN ('DEVUELTO', 'CON_MULTA'))";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMovimiento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PrestamoConsumibleVO prestamo = new PrestamoConsumibleVO();
                    prestamo.setIdPrestamo(rs.getInt("id_prestamo"));
                    prestamo.setIdConsumible(rs.getInt("id_consumible"));
                    prestamo.setIdHabitacionMovimiento(rs.getInt("id_habitacion_movimiento"));
                    prestamo.setCantidadEntregada(rs.getInt("cantidad_entregada"));
                    prestamo.setCantidadDevuelta(rs.getInt("cantidad_devuelta"));
                    prestamo.setEstado(rs.getString("estado"));
                    prestamo.setNombreConsumible(rs.getString("nombre_consumible"));
                    lista.add(prestamo);
                }
            }
        }
        return lista;
    }
}