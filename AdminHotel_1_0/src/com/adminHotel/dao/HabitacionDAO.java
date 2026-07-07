package com.adminHotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.adminHotel.util.EstadoRegistroEnum;
import com.adminHotel.vo.HabitacionVO;

public class HabitacionDAO {
    
    private Connection cn;

    public HabitacionDAO(Connection cn) {
        this.cn = cn;
    }

    // ===============================
    // INSERT
    // ===============================
    public boolean insertar(HabitacionVO vo) throws Exception {
        String sql = "INSERT INTO habitacion "
                + "(numero_habitacion, piso, descripcion, id_tipo_habitacion, "
                + "id_estado_habitacion, id_estado_registro, precio_sencilla, precio_doble, precio_tres) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, vo.getNumeroHabitacion());
            ps.setInt(2, vo.getPiso());
            ps.setString(3, vo.getDescripcion());
            ps.setInt(4, vo.getIdTipoHabitacion());
            ps.setInt(5, vo.getIdEstadoHabitacion());
            ps.setInt(6, vo.getIdEstadoRegistro());
            ps.setBigDecimal(7, vo.getPrecioSencilla());
            ps.setBigDecimal(8, vo.getPrecioDoble());
            ps.setBigDecimal(9, vo.getPrecioTres());
            return ps.executeUpdate() > 0;
        }
    }

    // ===============================
    // UPDATE
    // ===============================
    public boolean actualizar(HabitacionVO vo) throws Exception {
        String sql = "UPDATE habitacion SET "
                + "numero_habitacion = ?, "
                + "piso = ?, "
                + "descripcion = ?, "
                + "id_tipo_habitacion = ?, "
                + "id_estado_habitacion = ?, "
                + "id_estado_registro = ?, "
                + "precio_sencilla = ?, "
                + "precio_doble = ?, "
                + "precio_tres = ? "
                + "WHERE id_habitacion = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, vo.getNumeroHabitacion());
            ps.setInt(2, vo.getPiso());
            ps.setString(3, vo.getDescripcion());
            ps.setInt(4, vo.getIdTipoHabitacion());
            ps.setInt(5, vo.getIdEstadoHabitacion());
            ps.setInt(6, vo.getIdEstadoRegistro());
            ps.setBigDecimal(7, vo.getPrecioSencilla());
            ps.setBigDecimal(8, vo.getPrecioDoble());
            ps.setBigDecimal(9, vo.getPrecioTres());
            ps.setInt(10, vo.getIdHabitacion());
            return ps.executeUpdate() > 0;
        }
    }

    // ===============================
    // DELETE LOGICO
    // ===============================
    public boolean eliminar(Integer idHabitacion) throws Exception {
        String sql = "UPDATE habitacion SET id_estado_registro = ? WHERE id_habitacion = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, EstadoRegistroEnum.ELIMINADO.getCodigo());
            ps.setInt(2, idHabitacion);
            return ps.executeUpdate() > 0;
        }
    }


    // ===============================
    // OBTENER POR ID
    // ===============================
    public HabitacionVO buscarPorId(int id) throws Exception {
        HabitacionVO habitacion = null;
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT h.id_habitacion, h.numero_habitacion, h.piso, h.descripcion, ");
        sb.append("h.precio_sencilla, h.precio_doble, h.precio_tres, ");
        sb.append("th.descripcion AS tipo_descripcion, ");
        sb.append("eh.descripcion AS estado_descripcion ");
        sb.append("FROM habitacion h ");
        sb.append("LEFT JOIN tipo_habitacion th ON th.id_tipo_habitacion = h.id_tipo_habitacion ");
        sb.append("LEFT JOIN estado_habitacion eh ON eh.id_estado_habitacion = h.id_estado_habitacion ");
        sb.append("WHERE h.id_habitacion = ?");
        try (PreparedStatement ps = cn.prepareStatement(sb.toString())) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    habitacion = new HabitacionVO();
                    habitacion.setIdHabitacion(rs.getInt("id_habitacion"));
                    habitacion.setNumeroHabitacion(rs.getInt("numero_habitacion"));
                    habitacion.setPiso(rs.getInt("piso"));
                    habitacion.setDescripcion(rs.getString("descripcion"));
                    habitacion.setPrecioSencilla(rs.getBigDecimal("precio_sencilla"));
                    habitacion.setPrecioDoble(rs.getBigDecimal("precio_doble"));
                    habitacion.setPrecioTres(rs.getBigDecimal("precio_tres"));
                    habitacion.setTipoDescripcion(rs.getString("tipo_descripcion"));
                    habitacion.setEstadoDescripcion(rs.getString("estado_descripcion"));
                }
            }
        }
        return habitacion;
    }

    // ===============================
    // LISTAR TODAS
    // ===============================
    public List<HabitacionVO> listarHabitaciones() throws Exception {

        List<HabitacionVO> lista = new ArrayList<>();
        
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT h.id_habitacion, h.numero_habitacion, h.piso, h.descripcion, ");
        sb.append("h.id_tipo_habitacion, th.descripcion AS tipo_descripcion, ");
        sb.append("h.id_estado_habitacion, h.precio_sencilla, h.precio_doble, h.precio_tres, ");
        sb.append("eh.descripcion AS estado_descripcion ");
        sb.append("FROM habitacion h ");
        sb.append("INNER JOIN estado_habitacion eh ON eh.id_estado_habitacion = h.id_estado_habitacion ");
        sb.append("INNER JOIN tipo_habitacion th ON th.id_tipo_habitacion = h.id_tipo_habitacion ");
        sb.append("WHERE h.id_estado_registro = 1 ");
        sb.append("ORDER BY h.piso, h.numero_habitacion");
        try (PreparedStatement ps = cn.prepareStatement(sb.toString());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HabitacionVO h = new HabitacionVO();
                h.setIdHabitacion(rs.getInt("id_habitacion"));
                h.setNumeroHabitacion(rs.getInt("numero_habitacion"));
                h.setPiso(rs.getInt("piso"));
                h.setDescripcion(rs.getString("descripcion"));
                h.setIdTipoHabitacion(rs.getInt("id_tipo_habitacion"));
                h.setTipoDescripcion(rs.getString("tipo_descripcion"));
                h.setIdEstadoHabitacion(rs.getInt("id_estado_habitacion"));
                h.setPrecioSencilla(rs.getBigDecimal("precio_sencilla"));
                h.setPrecioDoble(rs.getBigDecimal("precio_doble"));
                h.setPrecioTres(rs.getBigDecimal("precio_tres"));
                h.setEstadoDescripcion(rs.getString("estado_descripcion"));
                lista.add(h);
            }
        }
        return lista;
    }

    // ===============================
    // LISTAR ACTIVAS
    // ===============================
    public List<HabitacionVO> listarActivas() throws Exception {

        List<HabitacionVO> lista = new ArrayList<>();

        String sql = "SELECT * FROM habitacion WHERE id_estado_registro = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, EstadoRegistroEnum.ACTIVO.getCodigo());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }

        return lista;
    }

    // ===============================
    // MAPEO ResultSet -> VO
    // ===============================
    private HabitacionVO mapear(ResultSet rs) throws Exception {

    	HabitacionVO vo = new HabitacionVO();
        vo.setIdHabitacion(rs.getInt("id_habitacion"));
        vo.setNumeroHabitacion(rs.getInt("numero_habitacion"));
        vo.setPiso(rs.getInt("piso"));
        vo.setDescripcion(rs.getString("descripcion"));
        vo.setPrecioSencilla(rs.getBigDecimal("precio_sencilla"));
        vo.setPrecioDoble(rs.getBigDecimal("precio_doble"));
        vo.setPrecioTres(rs.getBigDecimal("precio_tres"));
        vo.setIdTipoHabitacion(rs.getInt("id_tipo_habitacion"));
        vo.setIdEstadoHabitacion(rs.getInt("id_estado_habitacion"));
        vo.setIdEstadoRegistro(rs.getInt("id_estado_registro"));

        return vo;
    }
    
    // ===============================
    // ACTUALIZAR ESTADO HABITACION
    // ===============================
    public Integer actualizarEstado(int idHabitacion, int idEstado) throws Exception {

        String sql = "UPDATE habitacion SET id_estado_habitacion = ? WHERE id_habitacion = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idEstado);
            ps.setInt(2, idHabitacion);
            return ps.executeUpdate();
        }
    }
    
    // ===============================
    // OBTENER PRECIO HABITACION
    // ===============================
    public BigDecimal[] obtenerPrecioHabitacion(int idHabitacion) throws Exception {
    	
    	String sql = "SELECT precio_sencilla, precio_doble, precio_tres FROM habitacion WHERE id_habitacion = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idHabitacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new BigDecimal[]{
                        rs.getBigDecimal("precio_sencilla"),
                        rs.getBigDecimal("precio_doble"),
                        rs.getBigDecimal("precio_tres")
                    };
                } else {
                    throw new Exception("Habitaci�n no encontrada: " + idHabitacion);
                }
            }
        }
    }
    
    // ===============================
    // ACTUALIZAR SOLO PRECIOS 
    // ===============================
    public boolean actualizarPrecios(int idHabitacion, BigDecimal pSencilla, BigDecimal pDoble, BigDecimal pTres) throws Exception {
        String sql = "UPDATE habitacion SET precio_sencilla = ?, precio_doble = ?, precio_tres = ? WHERE id_habitacion = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, pSencilla);
            ps.setBigDecimal(2, pDoble);
            ps.setBigDecimal(3, pTres);
            ps.setInt(4, idHabitacion);
            return ps.executeUpdate() > 0;
        }
    }
}