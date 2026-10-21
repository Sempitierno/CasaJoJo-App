package cl.lacasajojo.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class TiempoTest {

    private val zona = ZoneId.of("America/Santiago")

    private fun millis(dia: Int, hora: Int, minuto: Int): Long =
        ZonedDateTime.of(2026, 5, dia, hora, minuto, 0, 0, zona).toInstant().toEpochMilli()

    @Test
    fun hora_de_hoy_usa_el_dia_de_ahora() {
        val ahora = millis(dia = 20, hora = 10, minuto = 30)
        val esperado = millis(dia = 20, hora = 13, minuto = 15)
        assertEquals(esperado, Tiempo.horaDeHoyAMillis(LocalTime.of(13, 15), ahora, zona))
    }

    @Test
    fun hora_de_hoy_no_cambia_de_dia_aunque_la_hora_ya_paso() {
        val ahora = millis(dia = 20, hora = 18, minuto = 0)
        val esperado = millis(dia = 20, hora = 9, minuto = 0)
        assertEquals(esperado, Tiempo.horaDeHoyAMillis(LocalTime.of(9, 0), ahora, zona))
    }
}
