package cl.lacasajojo.app.util

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

object Tiempo {

    /**
     * Convierte una hora del día (por ejemplo 13:30) en el instante de HOY a esa hora,
     * en milisegundos desde epoch. [ahoraMillis] define qué día es "hoy".
     */
    fun horaDeHoyAMillis(
        hora: LocalTime,
        ahoraMillis: Long,
        zona: ZoneId = ZoneId.systemDefault()
    ): Long {
        val hoy = Instant.ofEpochMilli(ahoraMillis).atZone(zona).toLocalDate()
        return hoy.atTime(hora).atZone(zona).toInstant().toEpochMilli()
    }
}
