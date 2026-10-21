package cl.lacasajojo.app.util

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/** Funciones de formato para mostrar precios, fechas y horas en español de Chile. */
object Formato {

    private val localeChile: Locale = Locale.forLanguageTag("es-CL")
    private val formatoFechaHora: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", localeChile)
    private val formatoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeChile)
    private val formatoHora: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", localeChile)

    /**
     * Pesos chilenos con punto como separador de miles: 3500 -> "$3.500".
     * Se implementa a mano para que el resultado sea idéntico en todos los dispositivos y configuraciones regionales.
     */
    fun precio(valor: Int): String {
        val digitos = abs(valor.toLong()).toString()
        val separado = StringBuilder()
        for ((indice, caracter) in digitos.withIndex()) {
            if (indice > 0 && (digitos.length - indice) % 3 == 0) {
                separado.append('.')
            }
            separado.append(caracter)
        }
        val signo = if (valor < 0) "-" else ""
        return signo + "\$" + separado.toString()
    }

    fun fechaHora(millis: Long, zona: ZoneId = ZoneId.systemDefault()): String =
        formatoFechaHora.format(Instant.ofEpochMilli(millis).atZone(zona))

    fun fecha(millis: Long, zona: ZoneId = ZoneId.systemDefault()): String =
        formatoFecha.format(Instant.ofEpochMilli(millis).atZone(zona))

    fun hora(millis: Long, zona: ZoneId = ZoneId.systemDefault()): String =
        formatoHora.format(Instant.ofEpochMilli(millis).atZone(zona))

    fun horaLocal(hora: LocalTime): String = formatoHora.format(hora)
}
