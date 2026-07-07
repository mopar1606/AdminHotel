package com.adminHotel.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalTime;
import java.util.List;

import com.adminHotel.dao.ParametroDAO;
import com.adminHotel.dao.TurnoCajaDAO;
import com.adminHotel.util.DBConnection;
import com.adminHotel.vo.TurnoCajaDetalleVO;
import com.adminHotel.vo.TurnoCajaVO;

public class TurnoSchedulerService {

    private static final int ID_CAJA_HOTEL = 1;
    private static final int ID_CAJA_MOSTRADOR = 2;

    /**
     * Verifica si es hora de cierre forzoso (7AM o 7PM) Retorna true si debe
     * bloquear ventas
     */
    public boolean esHoraDeCierreForzoso() {
        LocalTime ahora = LocalTime.now();
        int hora = ahora.getHour();

        // 7:00 AM o 7:00 PM
        return (hora == 7 && ahora.getMinute() == 0) || (hora == 19 && ahora.getMinute() == 0);
    }

    /**
     * Verifica si hay turnos vencidos que no se han cerrado
     */
    public boolean hayTurnosVencidos() throws Exception {
        Connection cn = null;
        try {
            cn = DBConnection.getConnection();
            ParametroDAO paramDAO = new ParametroDAO(cn);
            TurnoCajaDAO turnoDAO = new TurnoCajaDAO(cn);

            // Obtener parámetros de horario
            BigDecimal horaCierreDiurno = paramDAO.obtenerValor("HORA_CIERRE_DIURNO");
            BigDecimal horaCierreNocturno = paramDAO.obtenerValor("HORA_CIERRE_NOCTURNO");
            BigDecimal tolerancia = paramDAO.obtenerValor("TOLERANCIA_CIERRE_MINUTOS");

            if (horaCierreDiurno == null || horaCierreNocturno == null || tolerancia == null) {
                return false; // Parámetros no configurados
            }

            LocalTime ahora = LocalTime.now();
            int horaActual = ahora.getHour();
            int minutoActual = ahora.getMinute();

            // Verificar caja hotel
            TurnoCajaVO turnoHotel = turnoDAO.buscarTurnoAbierto(ID_CAJA_HOTEL);
            if (turnoHotel != null) {

                boolean vencidoHotel = esTurnoVencido(turnoHotel, horaActual, minutoActual, horaCierreDiurno,
                    horaCierreNocturno, tolerancia);

                if (vencidoHotel) {
                    return true;
                }
            }

            // Verificar caja mostrador
            TurnoCajaVO turnoMostrador = turnoDAO.buscarTurnoAbierto(ID_CAJA_MOSTRADOR);
            if (turnoMostrador != null) {

                boolean vencidoMostrador = esTurnoVencido(turnoMostrador, horaActual, minutoActual, horaCierreDiurno,
                    horaCierreNocturno, tolerancia);

                if (vencidoMostrador) {
                    return true;
                }
            }
            return false;

        } finally {
            if (cn != null)
                try {
                    cn.close();
                } catch (Exception ignored) {}
        }
    }

    private boolean esTurnoVencido(TurnoCajaVO turno, int horaActual, int minutoActual, BigDecimal horaCierreDiurno,
        BigDecimal horaCierreNocturno, BigDecimal tolerancia) {

        // Determinar si el turno es diurno o nocturno
        boolean esDiurno = turno.getTurnoDiurno() == null || turno.getTurnoDiurno() == 1;
        BigDecimal horaCierre = esDiurno ? horaCierreDiurno : horaCierreNocturno;

        // Convertir hora decimal a horas y minutos
        int horaCierreInt = horaCierre.intValue();
        int minutosCierre = horaCierre.subtract(new BigDecimal(horaCierreInt)).multiply(new BigDecimal(60)).intValue();

        // Calcular minutos transcurridos desde hora de cierre
        int minutosDesdeCierre = calcularMinutosDesdeCierre(horaActual, minutoActual, horaCierreInt, minutosCierre,
            esDiurno);

        // Si minutosDesdeCierre es negativo, significa que aún no es hora de cierre
        if (minutosDesdeCierre < 0) {
            return false;
        }

        // Si han pasado más minutos que la tolerancia, el turno está vencido
        boolean vencido = minutosDesdeCierre > tolerancia.intValue();

        return vencido;
    }

    private int calcularMinutosDesdeCierre(int horaActual, int minutoActual,
        int horaCierre, int minutoCierre, boolean esDiurno) {

        // ============================================
        // LÓGICA PARA TURNO NOCTURNO (7PM a 7AM)
        // ============================================
        if (!esDiurno) {

            // Turno nocturno cierra a las 7:00 AM del día siguiente
            if (horaCierre == 7) {

                // ESCENARIO 1: Noche (7PM - 11:59PM) → Turno RECIÉN ABIERTO
                if (horaActual >= 19) {
                    return -1; // No vence
                }

                // ESCENARIO 2: Madrugada (12AM - 6:59AM) → Turno ACTIVO
                else if (horaActual < 7) {
                    return -1; // No vence
                }

                // ESCENARIO 3: Día (7AM - 6:59PM) → ¡TURNO VENCIDO!
                else {
                    // Calcular minutos desde las 7AM de hoy
                    int totalMinutosActual = horaActual * 60 + minutoActual;
                    int totalMinutosCierre = 7 * 60 + minutoCierre;
                    int minutos = totalMinutosActual - totalMinutosCierre;
					
                    return minutos;
                }
            }
        }

        // ============================================
        // LÓGICA PARA TURNO DIURNO (7AM a 7PM)
        // ============================================

        int totalMinutosActual = horaActual * 60 + minutoActual;
        int totalMinutosCierre = horaCierre * 60 + minutoCierre;

        // Si ya pasó la hora de cierre
        if (totalMinutosActual >= totalMinutosCierre) {
            int minutos = totalMinutosActual - totalMinutosCierre;
            return minutos;
        }
        // Si aún no es hora de cierre
        else {
            return -1;
        }
    }

    /**
     * Cierra turnos vencidos automáticamente (forzado por sistema)
     */
    public void cerrarTurnosVencidos() throws Exception {
        Connection cn = null;
        try {
            cn = DBConnection.getConnection();
            cn.setAutoCommit(false);

            TurnoCajaDAO turnoDAO = new TurnoCajaDAO(cn);
            OperationBoxService boxService = new OperationBoxService();

            // Cerrar caja hotel si está abierta y vencida
            TurnoCajaVO turnoHotel = turnoDAO.buscarTurnoAbierto(ID_CAJA_HOTEL);
            if (turnoHotel != null && hayTurnosVencidos()) {
                cerrarTurnoForzado(turnoHotel, boxService);
            }

            // Cerrar caja mostrador si está abierta y vencida
            TurnoCajaVO turnoMostrador = turnoDAO.buscarTurnoAbierto(ID_CAJA_MOSTRADOR);
            if (turnoMostrador != null && hayTurnosVencidos()) {
                cerrarTurnoForzado(turnoMostrador, boxService);
            }

            cn.commit();

        } catch (Exception e) {
            if (cn != null)
                try {
                    cn.rollback();
                } catch (Exception ignored) {}
            throw e;
        } finally {
            if (cn != null)
                try {
                    cn.setAutoCommit(true);
                    cn.close();
                } catch (Exception ignored) {}
        }
    }

    private void cerrarTurnoForzado(TurnoCajaVO turno, OperationBoxService boxService) throws Exception {
        // Crear detalle de cierre forzado
        List < TurnoCajaDetalleVO > detalles = boxService.doGetLiveAccumulados();
        for (TurnoCajaDetalleVO detalle: detalles) {
            if (detalle.getIdTurnoCaja().equals(turno.getIdTurnoCaja())) {
                detalle.setMontoReal(detalle.getTotalIngresos()); // Asumir que cuadra
            }
        }

        // Cerrar con observación de forzado
        String observacion = "CIERRE FORZADO POR SISTEMA - Turno vencido";
        boxService.doCloseBox(turno.getIdTurnoCaja(), detalles, observacion);

        // Marcar como forzado en la base de datos
        marcarCierreForzado(turno.getIdTurnoCaja());
    }

    private void marcarCierreForzado(Integer idTurnoCaja) throws Exception {
        Connection cn = null;
        try {
            cn = DBConnection.getConnection();
            String sql = "UPDATE turno_caja SET forzado_cierre = 1 WHERE id_turno_caja = ?";
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, idTurnoCaja);
                ps.executeUpdate();
            }
        } finally {
            if (cn != null)
                try {
                    cn.close();
                } catch (Exception ignored) {}
        }
    }

    /**
     * Determina el tipo de turno actual (diurno/nocturno) basado en la hora
     */
    public boolean esTurnoDiurnoActual() {
    	LocalTime ahora = LocalTime.now();
        int hora = ahora.getHour();
        // Diurno: 6:00 AM a 6:59 PM (margen de 1 hora antes del turno de 7 AM)
        // Nocturno: 7:00 PM a 5:59 AM
        return hora >= 6 && hora < 19;
    }
}