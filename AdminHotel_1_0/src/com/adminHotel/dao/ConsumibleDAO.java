package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.adminHotel.vo.ConsumibleVO;

public class ConsumibleDAO {

    private Connection cn;

    public ConsumibleDAO(Connection cn) {
        this.cn = cn;
    }

    // ===============================
    // LISTAR TODO CON CONTROL DE INVENTARIO
    // ===============================
    public List < ConsumibleVO > listarTodo() throws Exception {
        List < ConsumibleVO > lista = new ArrayList < > ();

        // Aqu� agregamos c.fecha_compra a la selecci�n de la base de datos
        String sql = "SELECT c.id_consumible, c.nombre, c.precio_compra, c.precio_multa, c.fecha_compra, c.stock, c.id_estado_registro, c.es_prestable, " +
            "IFNULL((SELECT SUM(cantidad_entregada - cantidad_devuelta) FROM prestamo_consumible " +
            "WHERE id_consumible = c.id_consumible AND estado = 'PRESTADO'), 0) AS en_habitaciones, " +
            "IFNULL((SELECT SUM(cantidad_entregada - cantidad_devuelta) FROM prestamo_consumible " +
            "WHERE id_consumible = c.id_consumible AND estado = 'CON_MULTA'), 0) AS perdidos_cobrados " +
            "FROM consumible c " +
            "WHERE c.id_estado_registro = 1 " +
            "ORDER BY c.nombre ASC";

        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ConsumibleVO c = new ConsumibleVO();
                c.setIdConsumible(rs.getInt("id_consumible"));
                c.setNombre(rs.getString("nombre"));
                c.setPrecioCompra(rs.getBigDecimal("precio_compra"));
                c.setPrecioMulta(rs.getBigDecimal("precio_multa"));
                c.setStock(rs.getInt("stock"));
                c.setFechaCompra(rs.getDate("fecha_compra"));
                c.setIdEstadoRegistro(rs.getInt("id_estado_registro"));

                c.setEsPrestable(rs.getBoolean("es_prestable"));

                c.setEnHabitaciones(rs.getInt("en_habitaciones"));
                c.setPerdidosCobrados(rs.getInt("perdidos_cobrados"));

                lista.add(c);
            }
        }
        return lista;
    }

    // ===============================
    // LISTAR ACTIVOS CON STOCK (Para Check-In)
    // ===============================
    public List < ConsumibleVO > listarActivosParaPrestamo() throws Exception {
        List < ConsumibleVO > lista = new ArrayList < > ();
        // Solo lista los que tienen stock y que S� son prestables
        String sql = "SELECT * FROM consumible WHERE id_estado_registro = 1 AND stock > 0 AND es_prestable = TRUE ORDER BY nombre ASC";

        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ConsumibleVO c = new ConsumibleVO();
                c.setIdConsumible(rs.getInt("id_consumible"));
                c.setNombre(rs.getString("nombre"));
                c.setPrecioMulta(rs.getBigDecimal("precio_multa"));
                c.setStock(rs.getInt("stock"));
                // Leemos si es prestable
                c.setEsPrestable(rs.getBoolean("es_prestable"));
                lista.add(c);
            }
        }
        return lista;
    }

    // ===============================
    // INSERTAR
    // ===============================
    public Integer insertar(ConsumibleVO c) throws Exception {
        String sql = "INSERT INTO consumible (nombre, stock, precio_compra, precio_multa, fecha_compra, es_prestable, id_estado_registro) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre().toUpperCase().trim());
            ps.setInt(2, c.getStock());
            ps.setBigDecimal(3, c.getPrecioCompra());
            ps.setBigDecimal(4, c.getPrecioMulta());
            if (c.getFechaCompra() != null) {
                ps.setDate(5, c.getFechaCompra());
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }
            ps.setBoolean(6, (c.getEsPrestable() != null) ? c.getEsPrestable() : false);
            ps.setInt(7, (c.getIdEstadoRegistro() != null) ? c.getIdEstadoRegistro() : 1);

            try {
                ps.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    throw new Exception("El producto '" + c.getNombre() + "' ya existe en el sistema.");
                }
                throw e;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    c.setIdConsumible(id);
                    return id;
                }
            }
        }
        throw new Exception("No se pudo registrar el producto.");
    }

    // ===============================
    // ACTUALIZAR
    // ===============================
    public boolean actualizar(ConsumibleVO c) throws Exception {
        String sql = "UPDATE consumible SET nombre = ?, precio_compra = ?, precio_multa = ?, fecha_compra = ?, es_prestable = ? WHERE id_consumible = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre().toUpperCase().trim());
            ps.setBigDecimal(2, c.getPrecioCompra());
            ps.setBigDecimal(3, c.getPrecioMulta());
            if (c.getFechaCompra() != null) {
                ps.setDate(4, c.getFechaCompra());
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            ps.setBoolean(5, (c.getEsPrestable() != null) ? c.getEsPrestable() : false);
            ps.setInt(6, c.getIdConsumible());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new Exception("Error: Ya existe otro producto con el nombre '" + c.getNombre() + "'.");
            }
            throw e;
        }
    }

    // ===============================
    // AJUSTAR STOCK (+ o -)
    // ===============================
    public void ajustarStock(int idConsumible, int cantidad) throws Exception {

        // Obtener stock actual ANTES
        String sqlSelect = "SELECT stock FROM consumible WHERE id_consumible = ?";
        int stockAntes = 0;
        try (PreparedStatement psSelect = cn.prepareStatement(sqlSelect)) {
            psSelect.setInt(1, idConsumible);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    stockAntes = rs.getInt("stock");
                }
            }
        }

        // Actualizar stock
        String sqlUpdate = "UPDATE consumible SET stock = stock + ? WHERE id_consumible = ?";
        try (PreparedStatement ps = cn.prepareStatement(sqlUpdate)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idConsumible);

            int filasAff = ps.executeUpdate();

            // Obtener stock actual DESPUÉS
            int stockDespues = 0;
            try (PreparedStatement psSelect2 = cn.prepareStatement(sqlSelect)) {
                psSelect2.setInt(1, idConsumible);
                try (ResultSet rs = psSelect2.executeQuery()) {
                    if (rs.next()) {
                        stockDespues = rs.getInt("stock");
                    }
                }
            }

            if (filasAff == 0) {
                throw new Exception("No se encontró el consumible con ID: " + idConsumible);
            }
        }
    }

    // ===============================
    // ELIMINAR LOGICO
    // ===============================
    public void eliminar(int idConsumible) throws Exception {
        String sql = "UPDATE consumible SET id_estado_registro = 3 WHERE id_consumible = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConsumible);
            ps.executeUpdate();
        }
    }

    // ===============================
    // LISTAR ACTIVOS PRESTABLES CON SUS TOTALES (Para panel dinámico)
    // ===============================
    public List < ConsumibleVO > listarPrestablesConEstado() throws Exception {
        List < ConsumibleVO > lista = new ArrayList < > ();
        String sql = "SELECT c.id_consumible, c.nombre, c.stock, " +
            "IFNULL((SELECT SUM(cantidad_entregada - cantidad_devuelta) FROM prestamo_consumible " +
            "WHERE id_consumible = c.id_consumible AND estado = 'PRESTADO'), 0) AS en_habitaciones " +
            "FROM consumible c " +
            "WHERE c.id_estado_registro = 1 AND c.es_prestable = TRUE " +
            "ORDER BY c.nombre ASC";

        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ConsumibleVO c = new ConsumibleVO();
                c.setIdConsumible(rs.getInt("id_consumible"));
                c.setNombre(rs.getString("nombre"));
                c.setStock(rs.getInt("stock"));
                c.setEnHabitaciones(rs.getInt("en_habitaciones")); // Rellenamos el total prestado
                lista.add(c);
            }
        }
        return lista;
    }

    // ===============================
    // OBTENER STOCK ACTUAL
    // ===============================
    public Integer obtenerStock(Integer idConsumible) throws Exception {
        String sql = "SELECT stock FROM consumible WHERE id_consumible = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConsumible);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock");
                } else {
                    throw new Exception("No se encontró el consumible con ID: " + idConsumible);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error al obtener stock: " + e.getMessage());
        }
    }
}