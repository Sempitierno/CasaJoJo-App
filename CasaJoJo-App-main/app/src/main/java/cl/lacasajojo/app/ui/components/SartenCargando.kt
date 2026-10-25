package cl.lacasajojo.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/*
 * Animación de carga: una sartén sobre el fuego que "salta" cada cierto tiempo,
 * lanzando los ingredientes al aire (huevo, tomate, pimentón y champiñón).
 * Llamas que titilan y gotitas de aceite que chisporrotean. Todo con Canvas.
 *
 * Uso:  SartenCargando(mensaje = "Preparando comandas")
 */

// Proporciones respecto al tamaño del Canvas
private const val SARTEN_X = 0.40f     // centro horizontal de la sartén (el mango va a la derecha)
private const val SARTEN_Y = 0.60f     // altura de la superficie de la sartén
private const val RADIO_X = 0.29f      // radio horizontal
private const val RADIO_Y = 0.085f     // radio vertical (perspectiva)
private const val FIN_SALTO = 0.30f    // parte del ciclo en que la sartén se levanta
private const val INCLINACION = 8f    // grados que se inclina al saltar
private const val DURACION_VUELO = 0.55f // parte del ciclo que cada ingrediente pasa en el aire

private val colorMetal = Color(0xFF37474F)
private val colorInterior = Color(0xFF607D8B)
private val colorMango = Color(0xFF6D4C41)
private val colorAceite = Color(0xFFFFE082)

private enum class TipoComida { HUEVO, TOMATE, PIMENTON, CHAMPINON }

// dx/dy: posición dentro de la sartén (fracción del radio); altura: cuánto sube al saltar
private data class Comida(val tipo: TipoComida, val dx: Float, val dy: Float, val altura: Float, val vueltas: Float)

private val comidas = listOf(
    Comida(TipoComida.HUEVO, -0.35f, 0.05f, 0.26f, 0.5f),
    Comida(TipoComida.TOMATE, 0.25f, -0.15f, 0.32f, -1f),
    Comida(TipoComida.PIMENTON, 0.0f, 0.20f, 0.22f, 1.5f),
    Comida(TipoComida.CHAMPINON, 0.55f, 0.15f, 0.28f, -1.2f)
)

@Composable
fun SartenCargando(
    modifier: Modifier = Modifier,
    mensaje: String? = "Cocinando",
    tamano: Dp = 160.dp
) {
    val transicion = rememberInfiniteTransition(label = "sarten")

    // "Reloj" de la animación: va de 0 a 1 una y otra vez
    val tiempo by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1800, easing = LinearEasing)),
        label = "tiempo"
    )

    // Puntos suspensivos del texto
    val puntos by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1600, easing = LinearEasing)),
        label = "puntos"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(tamano)) {
            val salto = alturaSalto(tiempo)
            dibujarFuego(tiempo)
            // La sartén sube y se inclina al saltar (pivote en el mango, como si alguien la moviera)
            translate(top = -salto * size.height * 0.05f) {
                rotate(
                    degrees = salto * INCLINACION,
                    pivot = Offset(size.width * (SARTEN_X + RADIO_X + 0.22f), size.height * SARTEN_Y)
                ) {
                    dibujarSarten()
                    dibujarComidas(tiempo, enElAire = false) // los que están en la sartén se mueven con ella
                }
            }
            dibujarComidas(tiempo, enElAire = true)
            dibujarAceite(tiempo)
        }
        if (mensaje != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = mensaje + ".".repeat(puntos.toInt().coerceAtMost(3)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// 0 = sartén en reposo, 1 = punto más alto del salto
private fun alturaSalto(tiempo: Float): Float =
    if (tiempo < FIN_SALTO) sin(tiempo / FIN_SALTO * PI.toFloat()) else 0f

// ---------- Fuego ----------
private fun DrawScope.dibujarFuego(tiempo: Float) {
    val w = size.width
    val h = size.height
    val cx = w * SARTEN_X
    val baseY = h * 0.90f

    // Quemador
    drawRoundRect(
        color = Color(0xFF424242),
        topLeft = Offset(cx - w * 0.22f, baseY),
        size = Size(w * 0.44f, h * 0.035f),
        cornerRadius = CornerRadius(h * 0.02f)
    )

    // Tres llamas que titilan a distinto ritmo
    for (i in -1..1) {
        val x = cx + i * w * 0.12f
        val titileo = 0.75f + 0.25f * sin((tiempo * 6f + i * 0.7f) * 2f * PI.toFloat())
        val alto = h * 0.12f * titileo
        dibujarLlama(x, baseY, alto, w * 0.05f, Color(0xFFFF7043))       // exterior naranja
        dibujarLlama(x, baseY, alto * 0.6f, w * 0.028f, Color(0xFFFFEE58)) // interior amarillo
    }
}

private fun DrawScope.dibujarLlama(x: Float, baseY: Float, alto: Float, ancho: Float, color: Color) {
    val llama = Path().apply {
        moveTo(x, baseY - alto)
        cubicTo(x + ancho, baseY - alto * 0.4f, x + ancho * 0.8f, baseY, x, baseY)
        cubicTo(x - ancho * 0.8f, baseY, x - ancho, baseY - alto * 0.4f, x, baseY - alto)
        close()
    }
    drawPath(llama, color)
}

// ---------- Sartén ----------
private fun DrawScope.dibujarSarten() {
    val w = size.width
    val h = size.height
    val cx = w * SARTEN_X
    val cy = h * SARTEN_Y
    val rx = w * RADIO_X
    val ry = h * RADIO_Y
    val grosor = h * 0.05f

    // Mango (detrás, sale hacia la derecha)
    rotate(degrees = -10f, pivot = Offset(cx + rx * 0.9f, cy)) {
        drawRoundRect(
            color = colorMango,
            topLeft = Offset(cx + rx * 0.9f, cy - h * 0.02f),
            size = Size(w * 0.30f, h * 0.04f),
            cornerRadius = CornerRadius(h * 0.02f)
        )
    }

    // Base y costado (da volumen)
    drawOval(colorMetal, Offset(cx - rx, cy - ry + grosor), Size(rx * 2f, ry * 2f))
    drawRect(colorMetal, Offset(cx - rx, cy), Size(rx * 2f, grosor))

    // Borde superior e interior
    drawOval(colorMetal, Offset(cx - rx, cy - ry), Size(rx * 2f, ry * 2f))
    val margen = 0.88f
    drawOval(
        colorInterior,
        Offset(cx - rx * margen, cy - ry * margen),
        Size(rx * 2f * margen, ry * 2f * margen)
    )
    // Brillo
    drawOval(
        Color.White.copy(alpha = 0.15f),
        Offset(cx - rx * 0.6f, cy - ry * 0.6f),
        Size(rx * 0.7f, ry * 0.6f)
    )
}

// ---------- Ingredientes ----------
// enElAire = true dibuja solo los que van volando; false, solo los que están en la sartén
private fun DrawScope.dibujarComidas(tiempo: Float, enElAire: Boolean) {
    val w = size.width
    val h = size.height
    val cx = w * SARTEN_X
    val cy = h * SARTEN_Y
    val rx = w * RADIO_X
    val ry = h * RADIO_Y

    comidas.forEachIndexed { i, comida ->
        val baseX = cx + comida.dx * rx
        val baseY = cy + comida.dy * ry

        // Despegan justo cuando la sartén empieza a subir, uno tras otro
        val inicio = 0.02f + i * 0.04f
        val p = (tiempo - inicio) / DURACION_VUELO
        val volando = p in 0f..1f
        if (volando != enElAire) return@forEachIndexed

        val x: Float
        val y: Float
        val giro: Float
        if (volando) {
            // Arco hacia arriba y vuelta a la sartén, girando
            val arco = sin(p * PI.toFloat())
            x = baseX + arco * w * 0.02f * (if (i % 2 == 0) 1f else -1f)
            y = baseY - arco * h * comida.altura
            giro = p * 360f * comida.vueltas
        } else {
            // En la sartén: chisporrotea un poquito
            x = baseX
            y = baseY + sin((tiempo * 12f + i) * 2f * PI.toFloat()) * h * 0.003f
            giro = 0f
        }

        translate(left = x, top = y) {
            rotate(degrees = giro, pivot = Offset.Zero) {
                dibujarComida(comida.tipo, w * 0.055f)
            }
        }
    }
}

// Dibuja un ingrediente centrado en (0,0)
private fun DrawScope.dibujarComida(tipo: TipoComida, s: Float) {
    when (tipo) {
        TipoComida.HUEVO -> {
            drawOval(Color.White, Offset(-s * 1.3f, -s * 0.7f), Size(s * 2.6f, s * 1.4f))
            drawCircle(Color(0xFFFFB300), radius = s * 0.5f, center = Offset.Zero)
        }
        TipoComida.TOMATE -> { // rodaja de tomate
            drawCircle(Color(0xFFE53935), radius = s * 0.9f, center = Offset.Zero)
            drawCircle(Color(0xFFEF9A9A), radius = s * 0.55f, center = Offset.Zero)
            for (k in 0..2) {
                val ang = k * 2f * PI.toFloat() / 3f
                drawCircle(
                    Color(0xFFFFF59D),
                    radius = s * 0.12f,
                    center = Offset(kotlin.math.cos(ang) * s * 0.3f, sin(ang) * s * 0.3f)
                )
            }
        }
        TipoComida.PIMENTON -> { // tira de pimentón
            drawRoundRect(
                Color(0xFF43A047),
                topLeft = Offset(-s * 1.2f, -s * 0.3f),
                size = Size(s * 2.4f, s * 0.6f),
                cornerRadius = CornerRadius(s * 0.3f)
            )
        }
        TipoComida.CHAMPINON -> {
            drawRoundRect(
                Color(0xFFEFEBE9),
                topLeft = Offset(-s * 0.25f, 0f),
                size = Size(s * 0.5f, s * 0.6f),
                cornerRadius = CornerRadius(s * 0.15f)
            )
            drawArc(
                Color(0xFFA1887F),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(-s * 0.8f, -s * 0.6f),
                size = Size(s * 1.6f, s * 1.2f)
            )
        }
    }
}

// ---------- Gotitas de aceite chisporroteando ----------
private fun DrawScope.dibujarAceite(tiempo: Float) {
    val w = size.width
    val h = size.height
    val cx = w * SARTEN_X
    val cy = h * SARTEN_Y
    val rx = w * RADIO_X
    val posiciones = listOf(-0.6f, -0.2f, 0.3f, 0.7f)

    posiciones.forEachIndexed { i, pos ->
        val fase = (tiempo * 2f + i * 0.27f) % 1f
        drawCircle(
            color = colorAceite.copy(alpha = 1f - fase),
            radius = w * 0.012f * (1f - fase * 0.5f),
            center = Offset(
                cx + pos * rx + sin(fase * PI.toFloat()) * w * 0.02f,
                cy - h * 0.06f - fase * h * 0.12f
            )
        )
    }
}
