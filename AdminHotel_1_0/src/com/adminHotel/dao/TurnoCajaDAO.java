package com.adminHotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.adminHotel.vo.TurnoCajaVO;

public class TurnoCajaDAO {

    private Connection cn;
    public TurnoCajaDAO(Connection cn) {
        this.cn = cn;
    }

    // Abre un turno nuevo para una caja
    public Integer abrir(TurnoCajaVO turno) throws Exception {
        String sql = "INSERT INTO turno_caja " +
            "(id_caja, id_usuario, fecha_apertura, base_inicial_efectivo, " +
            " observacion_apertura, estado, id_estado_registro, turno_diurno) " // NUEVO CAMPO
            +
            "VALUES (?, ?, NOW(), ?, ?, 'ABIERTA', 1, ?)";

        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, turno.getIdCaja());
            ps.setInt(2, turno.getIdUsuario());
            ps.setBigDecimal(3, turno.getBaseInicialEfectivo());
            ps.setString(4, turno.getObservacionApertura());
            ps.setInt(5, turno.getTurnoDiurno() != null ? turno.getTurnoDiurno() : 1);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new Exception("No se pudo abrir el turno de caja");
    }

    // Cierra el turno: estampa fecha, estado y observacion
    public void cerrar(TurnoCajaVO turno) throws Exception {
        String sql = "UPDATE turno_caja SET " +
            "fecha_cierre = NOW(), estado = 'CERRADA', " +
            "observacion_cierre = ?, forzado_cierre = ? " +
            "WHERE id_turno_caja = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, turno.getObservacionCierre());
            ps.setInt(2, turno.getForzadoCierre() != null ? turno.getForzadoCierre() : 0);
            ps.setInt(3, turno.getIdTurnoCaja());
            ps.executeUpdate();
        }
    }

    // Busca el turno ABIERTO activo de una caja específica
    public TurnoCajaVO buscarTurnoAbierto(Integer idCaja) throws Exception {
        String sql = "SELECT tc.id_turno_caja, tc.id_caja, tc.id_usuario, " +
            "       tc.fecha_apertura, tc.base_inicial_efectivo, " +
            "       tc.observacion_apertura, tc.estado, tc.id_estado_registro, " +
            "       tc.turno_diurno, tc.forzado_cierre, " // NUEVOS CAMPOS
            +
            "       c.nombre AS nombre_caja, u.nombre_usuario AS nombre_usuario " +
            "FROM turno_caja tc " +
            "INNER JOIN caja c ON tc.id_caja = c.id_caja " +
            "INNER JOIN usuario u ON tc.id_usuario = u.id_usuario " +
            "WHERE tc.id_caja = ? AND tc.estado = 'ABIERTA'";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    TurnoCajaVO t = new TurnoCajaVO();
                    t.setIdTurnoCaja(rs.getInt("id_turno_caja"));
                    t.setIdCaja(rs.getInt("id_caja"));
                    t.setIdUsuario(rs.getInt("id_usuario"));
                    t.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                    t.setBaseInicialEfectivo(rs.getBigDecimal("base_inicial_efectivo"));
                    t.setObservacionApertura(rs.getString("observacion_apertura"));
                    t.setEstado(rs.getString("estado"));
                    t.setIdEstadoRegistro(rs.getInt("id_estado_registro"));
                    t.setTurnoDiurno(convertirTinyintAInteger(rs.getObject("turno_diurno")));
                    t.setForzadoCierre(convertirTinyintAInteger(rs.getObject("forzado_cierre")));
                    t.setNombreCaja(rs.getString("nombre_caja"));
                    t.setNombreUsuario(rs.getString("nombre_usuario"));

                    return t;
                }
            }
        }
        return null; // null = la caja está cerrada
    }

    // Historial de turnos de una caja (para reportes)
    public List < TurnoCajaVO > listarPorCaja(Integer idCaja) throws Exception {

        List < TurnoCajaVO > lista = new ArrayList < > ();
        String sql = "SELECT tc.id_turno_caja, tc.id_caja, tc.id_usuario, " +
            "       tc.fecha_apertura, tc.fecha_cierre, tc.base_inicial_efectivo, " +
            "       tc.estado, tc.observacion_cierre, tc.id_estado_registro, " +
            "       tc.turno_diurno, tc.forzado_cierre, " // NUEVOS CAMPOS
            +
            "       c.nombre AS nombre_caja, u.nombre_usuario AS nombre_usuario " +
            "FROM turno_caja tc " +
            "INNER JOIN caja c ON tc.id_caja = c.id_caja " +
            "INNER JOIN usuario u ON tc.id_usuario = u.id_usuario " +
            "WHERE tc.id_caja = ? " +
            "ORDER BY tc.fecha_apertura DESC";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCaja);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    TurnoCajaVO t = new TurnoCajaVO();
                    t.setIdTurnoCaja(rs.getInt("id_turno_caja"));
                    t.setIdCaja(rs.getInt("id_caja"));
                    t.setIdUsuario(rs.getInt("id_usuario"));
                    t.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                    t.setFechaCierre(rs.getTimestamp("fecha_cierre"));
                    t.setBaseInicialEfectivo(rs.getBigDecimal("base_inicial_efectivo"));
                    t.setEstado(rs.getString("estado"));
                    t.setObservacionCierre(rs.getString("observacion_cierre"));
                    t.setIdEstadoRegistro(rs.getInt("id_estado_registro"));
                    t.setTurnoDiurno(convertirTinyintAInteger(rs.getObject("turno_diurno")));
                    t.setForzadoCierre(convertirTinyintAInteger(rs.getObject("forzado_cierre")));
                    t.setNombreCaja(rs.getString("nombre_caja"));
                    t.setNombreUsuario(rs.getString("nombre_usuario"));
                    lista.add(t);
                }
            }
        }
        return lista;
    }

    // Agregar en TurnoCajaDAO
    public TurnoCajaVO buscarTurnoPorId(Integer idTurnoCaja) throws Exception {

        String sql = "SELECT tc.*, c.nombre AS nombre_caja, u.nombre_usuario AS nombre_usuario " +
            "FROM turno_caja tc " +
            "INNER JOIN caja c ON tc.id_caja = c.id_caja " +
            "INNER JOIN usuario u ON tc.id_usuario = u.id_usuario " +
            "WHERE tc.id_turno_caja = ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idTurnoCaja);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    TurnoCajaVO t = new TurnoCajaVO();
                    t.setIdTurnoCaja(rs.getInt("id_turno_caja"));
                    t.setIdCaja(rs.getInt("id_caja"));
                    t.setIdUsuario(rs.getInt("id_usuario"));
                    t.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                    t.setBaseInicialEfectivo(rs.getBigDecimal("base_inicial_efectivo"));
                    t.setEstado(rs.getString("estado"));
                    t.setIdEstadoRegistro(rs.getInt("id_estado_registro"));
                    t.setTurnoDiurno(convertirTinyintAInteger(rs.getObject("turno_diurno")));
                    t.setForzadoCierre(convertirTinyintAInteger(rs.getObject("forzado_cierre")));
                    t.setFechaCierre(rs.getTimestamp("fecha_cierre"));
                    t.setObservacionCierre(rs.getString("observacion_cierre"));
                    t.setNombreCaja(rs.getString("nombre_caja"));
                    t.setNombreUsuario(rs.getString("nombre_usuario"));

                    return t;
                }
            }
        }
        return null;
    }

    // Lista todos los turnos cerrados de todas las cajas (para el informe)
    public List < TurnoCajaVO > listarTodosCerrados() throws Exception {

        List < TurnoCajaVO > lista = new ArrayList < > ();
        String sql = "SELECT tc.id_turno_caja, tc.id_caja, tc.id_usuario, " +
            "       tc.fecha_apertura, tc.fecha_cierre, tc.base_inicial_efectivo, " +
            "       tc.estado, tc.observacion_cierre, tc.id_estado_registro, " +
            "       tc.turno_diurno, tc.forzado_cierre, " // NUEVOS CAMPOS
            +
            "       c.nombre AS nombre_caja, u.nombre_usuario AS nombre_usuario " +
            "FROM turno_caja tc " +
            "INNER JOIN caja c ON tc.id_caja = c.id_caja " +
            "INNER JOIN usuario u ON tc.id_usuario = u.id_usuario " +
            "WHERE tc.estado = 'CERRADA' " +
            "ORDER BY tc.fecha_cierre DESC";

        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TurnoCajaVO t = new TurnoCajaVO();
                t.setIdTurnoCaja(rs.getInt("id_turno_caja"));
                t.setIdCaja(rs.getInt("id_caja"));
                t.setIdUsuario(rs.getInt("id_usuario"));
                t.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                t.setFechaCierre(rs.getTimestamp("fecha_cierre"));
                t.setBaseInicialEfectivo(rs.getBigDecimal("base_inicial_efectivo"));
                t.setEstado(rs.getString("estado"));
                t.setObservacionCierre(rs.getString("observacion_cierre"));
                t.setIdEstadoRegistro(rs.getInt("id_estado_registro"));
                t.setTurnoDiurno(convertirTinyintAInteger(rs.getObject("turno_diurno")));
                t.setForzadoCierre(convertirTinyintAInteger(rs.getObject("forzado_cierre")));
                t.setNombreCaja(rs.getString("nombre_caja"));
                t.setNombreUsuario(rs.getString("nombre_usuario"));
                lista.add(t);
            }
        }
        return lista;
    }

    /**
     * Busca turnos abiertos que han pasado la hora de cierre
     * @param horaActual Hora actual en formato decimal (ej: 7.5 = 7:30 AM)
     */
    public List < TurnoCajaVO > buscarTurnosVencidos(BigDecimal horaActual) throws Exception {
        List < TurnoCajaVO > lista = new ArrayList < > ();

        // Consulta compleja que verifica si el turno ha pasado su hora de cierre
        String sql = "SELECT tc.*, c.nombre AS nombre_caja, u.nombre_usuario AS nombre_usuario " +
            "FROM turno_caja tc " +
            "INNER JOIN caja c ON tc.id_caja = c.id_caja " +
            "INNER JOIN usuario u ON tc.id_usuario = u.id_usuario " +
            "WHERE tc.estado = 'ABIERTA' " +
            "  AND (" +
            "    (tc.turno_diurno = 1 AND ? >= 19.0) " // Diurno vencido después de 7PM
            +
            "    OR (tc.turno_diurno = 0 AND ? >= 7.0 AND ? < 19.0) " // Nocturno vencido después de 7AM y antes de 7PM
            +
            "  )";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, horaActual);
            ps.setBigDecimal(2, horaActual);
            ps.setBigDecimal(3, horaActual);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TurnoCajaVO t = new TurnoCajaVO();
                    t.setIdTurnoCaja(rs.getInt("id_turno_caja"));
                    t.setIdCaja(rs.getInt("id_caja"));
                    t.setIdUsuario(rs.getInt("id_usuario"));
                    t.setFechaApertura(rs.getTimestamp("fecha_apertura"));
                    t.setBaseInicialEfectivo(rs.getBigDecimal("base_inicial_efectivo"));
                    t.setObservacionApertura(rs.getString("observacion_apertura"));
                    t.setEstado(rs.getString("estado"));
                    t.setIdEstadoRegistro(rs.getInt("id_estado_registro"));
                    t.setTurnoDiurno(convertirTinyintAInteger(rs.getObject("turno_diurno")));
                    t.setForzadoCierre(convertirTinyintAInteger(rs.getObject("forzado_cierre")));
                    t.setNombreCaja(rs.getString("nombre_caja"));
                    t.setNombreUsuario(rs.getString("nombre_usuario"));
                    lista.add(t);
                }
            }
        }
        return lista;
    }

    /**
     * Convierte un valor tinyint(1) de MySQL a Integer.
     * MySQL puede devolver Boolean para tinyint(1), así que manejamos ambos casos.
     * Si es NULL, retorna valor por defecto.
     */
    private Integer convertirTinyintAInteger(Object valor) {
        if (valor == null) {
            return 0; // Valor por defecto para NULL
        }

        if (valor instanceof Boolean) {
            // Si es Boolean, true = 1, false = 0
            return ((Boolean) valor) ? 1 : 0;
        } else if (valor instanceof Number) {
            // Si es Number (Integer, BigDecimal, etc.)
            return ((Number) valor).intValue();
        } else {
            // Intentar convertir de String
            try {
                return Integer.parseInt(valor.toString());
            } catch (NumberFormatException e) {
                return 0; // Valor por defecto si no se puede convertir
            }
        }
    }
    
    // Lista fechas únicas de cierres (para el combo del informe combinado)
    public List<String> listarFechasCierreUnicas() throws Exception {
        List<String> fechas = new ArrayList<>();
        String sql = "SELECT DATE(fecha_cierre) AS fecha_dia, " +
                     "       MAX(fecha_cierre) AS ultima_hora " +
                     "FROM turno_caja WHERE estado = 'CERRADA' " +
                     "GROUP BY DATE(fecha_cierre) " +
                     "ORDER BY fecha_dia DESC";
        try (PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Guardamos "yyyy-MM-dd HH:mm" para tener fecha y hora juntas
                java.sql.Timestamp hora = rs.getTimestamp("ultima_hora");
                String fechaRaw   = rs.getString("fecha_dia"); // "2026-04-16"
                String horaStr    = new java.text.SimpleDateFormat("HH:mm").format(hora);
                fechas.add(fechaRaw + " " + horaStr); // "2026-04-16 14:30"
            }
        }
        return fechas;
    }
}