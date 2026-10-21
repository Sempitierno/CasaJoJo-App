package cl.lacasajojo.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class FormatoTest {

    @Test
    fun precio_agrega_separador_de_miles() {
        assertEquals("\$3.500", Formato.precio(3500))
        assertEquals("\$12.990", Formato.precio(12990))
    }

    @Test
    fun precio_con_millones() {
        assertEquals("\$1.250.000", Formato.precio(1_250_000))
    }

    @Test
    fun precio_menor_a_mil_no_lleva_separador() {
        assertEquals("\$900", Formato.precio(900))
        assertEquals("\$0", Formato.precio(0))
    }

    @Test
    fun precio_negativo_conserva_el_signo() {
        assertEquals("-\$1.500", Formato.precio(-1500))
    }

    @Test
    fun hora_local_siempre_tiene_dos_digitos() {
        assertEquals("09:05", Formato.horaLocal(LocalTime.of(9, 5)))
        assertEquals("13:30", Formato.horaLocal(LocalTime.of(13, 30)))
    }

    @Test
    fun fecha_y_hora_respetan_la_zona_indicada() {
        val zona = ZoneId.of("America/Santiago")
        val instante = ZonedDateTime.of(2026, 3, 15, 13, 45, 0, 0, zona).toInstant().toEpochMilli()
        assertEquals("15/03/2026 13:45", Formato.fechaHora(instante, zona))
        assertEquals("15/03/2026", Formato.fecha(instante, zona))
        assertEquals("13:45", Formato.hora(instante, zona))
    }
}
