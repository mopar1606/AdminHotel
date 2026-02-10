package com.adminHotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.adminHotel.util.DBConnection;
import com.adminHotel.vo.HabitacionVO;

public class HabitacionDAO {

    public void insertar(HabitacionVO h) {
        String sql = "INSERT INTO habitacion (numero_habitacion, piso, tipo, estado, activa) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, h.getNumeroHabitacion());
            ps.setInt(2, h.getPiso());
            ps.setString(3, h.getTipo());
            ps.setString(4, h.getEstado());
            ps.setBoolean(5, h.isActiva());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}