package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.adminHotel.vo.ProductoVO;

public class ProductoDAO {
	
	private Connection cn;

    public ProductoDAO(Connection cn) {
        this.cn = cn;
    }
    
 // Para llenar el Combo o la Tabla de ventas
    public List<ProductoVO> listarActivos() throws Exception {
        List<ProductoVO> lista = new ArrayList<>();
        String sql = "SELECT id_producto, nombre, precio_venta, stock, codigo_barras FROM producto WHERE id_estado_registro = 1 AND stock > 0";
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductoVO p = new ProductoVO();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                p.setStock(rs.getInt("stock"));
                p.setCodigoBarras(rs.getString("codigo_barras"));
                lista.add(p);
            }
        }
        return lista;
    }
    
 // MÉTODO CRÍTICO: Descontar stock
    public void descontarStock(int idProducto, int cantidad) throws Exception {
        String sql = "UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad); // Validación extra para no quedar en negativo
            
            int filasAff = ps.executeUpdate();
            if (filasAff == 0) {
                throw new Exception("No hay stock suficiente para el producto ID: " + idProducto);
            }
        }
    }
    
    public int insertar(ProductoVO p) throws Exception {
        // 1. SQL con los campos de tu tabla producto
        String sql = "INSERT INTO producto (nombre, precio_compra, precio_venta, stock, id_estado_registro, codigo_barras) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        // 2. Usamos RETURN_GENERATED_KEYS para obtener el ID asignado
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, p.getNombre().toUpperCase().trim()); // Normalizamos a mayúsculas
            ps.setBigDecimal(2, p.getPrecioCompra());
            ps.setBigDecimal(3, p.getPrecioVenta());
            ps.setInt(4, p.getStock());
            
            // id_estado_registro (1 = ACTIVO por defecto)
            ps.setInt(5, (p.getIdEstadoRegistro() != null) ? p.getIdEstadoRegistro() : 1);
            
            ps.setString(6, p.getCodigoBarras());

            // 3. Ejecutar y validar duplicados
            try {
                ps.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // Código MySQL para Duplicate Entry
                    throw new Exception("El producto '" + p.getNombre() + "' ya existe en el sistema.");
                }
                throw e;
            }

            // 4. Retornar el ID generado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    p.setIdProducto(id);
                    return id;
                }
            }
        }
        throw new Exception("No se pudo registrar el producto.");
    }
    
    public boolean existeProducto(String nombre) throws Exception {
        String sql = "SELECT COUNT(*) FROM producto WHERE nombre = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Lista todos los productos para la tabla del administrador.
     * Incluye productos inactivos para que puedan ser reactivados.
     */
    public List<ProductoVO> listarTodo() throws Exception {
        List<ProductoVO> lista = new ArrayList<>();
        String sql = "SELECT p.*, e.descripcion as estado_nombre " +
                     "FROM producto p " +
                     "JOIN estado_registro e ON p.id_estado_registro = e.id_estado_registro " +
                     "ORDER BY p.nombre ASC";

        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductoVO p = new ProductoVO();
                p.setIdProducto(rs.getInt("id_producto"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecioCompra(rs.getBigDecimal("precio_compra"));
                p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                p.setStock(rs.getInt("stock"));
                p.setIdEstadoRegistro(rs.getInt("id_estado_registro"));
                p.setCodigoBarras(rs.getString("codigo_barras"));
                // Si tienes un campo String en el VO para el nombre del estado:
                // p.setEstadoNombre(rs.getString("estado_nombre"));
                lista.add(p);
            }
        }
        return lista;
    }

    /**
     * Actualiza los datos de un producto (Nombre, Precios, Estado).
     */
    public boolean actualizar(ProductoVO p) throws Exception {
        String sql = "UPDATE producto SET nombre = ?, precio_compra = ?, precio_venta = ?, id_estado_registro = ?,  codigo_barras = ?" +
                     "WHERE id_producto = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre().toUpperCase().trim());
            ps.setBigDecimal(2, p.getPrecioCompra());
            ps.setBigDecimal(3, p.getPrecioVenta());
            ps.setInt(4, p.getIdEstadoRegistro());
            ps.setString(5, p.getCodigoBarras());
            ps.setInt(6, p.getIdProducto());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new Exception("Error: Ya existe otro producto con el nombre '" + p.getNombre() + "'.");
            }
            throw e;
        }
    }

    /**
     * Ajuste de Stock (Entrada de Mercancía).
     * Suma la cantidad recibida al stock actual.
     */
    public boolean sumarStock(int idProducto, int cantidadRecibida) throws Exception {
        // SQL que suma al valor actual (atómico para evitar errores de concurrencia)
        String sql = "UPDATE producto SET stock = stock + ? WHERE id_producto = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, cantidadRecibida);
            ps.setInt(2, idProducto);

            return ps.executeUpdate() > 0;
        }
    }
    
    public ProductoVO buscarPorCodigo(String codigo) throws Exception {
        String sql = "SELECT * FROM producto WHERE codigo_barras = ? AND id_estado_registro = 1";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProductoVO p = new ProductoVO();
                    p.setIdProducto(rs.getInt("id_producto"));
                    p.setNombre(rs.getString("nombre"));
                    p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                    p.setStock(rs.getInt("stock"));
                    p.setCodigoBarras(rs.getString("codigo_barras"));
                    return p;
                }
            }
        }
        return null;
    }
}
