package cl.lacasajojo.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/*
 * Animación de carga: una cacerola que se balancea mientras le caen
 * ingredientes (tomate, zanahoria, hoja y arvejas), con burbujas,
 * salpicaduras y vapor. Todo se dibuja con Canvas, sin imágenes.
 *
 * Uso:  CacerolaCargando(mensaje = "Cargando comandas")
 */

// Proporciones de la olla respecto al tamaño del Canvas
private const val BORDE_Y = 0.56f     // altura del borde de la olla
private const val OLLA_ANCHO = 0.62f  // ancho de la olla
private const val OLLA_ALTO = 0.32f   // alto del cuerpo de la olla
private const val CAIDA_FIN = 0.9f    // en qué parte del ciclo el ingrediente toca el caldo

private val colorCaldo = Color(0xFFFFB74D)
private val colorBurbuja = Color(0xFFFFE0B2)

private enum class TipoIngrediente { TOMATE, ZANAHORIA, HOJA, ARVEJAS }

// desfaseX: dónde cae (−0.5 a 0.5 del ancho de la olla); vueltas: cuánto gira al caer
private data class Ingrediente(val tipo: TipoIngrediente, val desfaseX: Float, val vueltas: Float)

private val ingredientes = listOf(
    Ingrediente(TipoIngrediente.TOMATE, -0.20f, 1.0f),
    Ingrediente(TipoIngrediente.ZANAHORIA, 0.16f, -1.5f),
    Ingrediente(TipoIngrediente.HOJA, -0.02f, 1.2f),
    Ingrediente(TipoIngrediente.ARVEJAS, 0.24f, 0.5f)
)

@Composable
fun CacerolaCargando(
    modifier: Modifier = Modifier,
    mensaje: String? = "Cocinando",
    tamano: Dp = 160.dp,
    colorOlla: Color = Color(0xFFAF3A2B) // rojo "ají" de la marca
) {
    val transicion = rememberInfiniteTransition(label = "cacerola")

    // "Reloj" de la animación: va de 0 a 1 una y otra vez
    val tiempo by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 2400, easing = LinearEasing)),
        label = "tiempo"
    )

    // La olla se balancea de lado a lado
    val balanceo by transicion.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "balanceo"
    )

    // Puntos suspensivos del texto: "", ".", "..", "..."
    val puntos by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1600, easing = LinearEasing)),
        label = "puntos"
    )

    val colorVapor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(tamano)) {
            // El orden importa: lo que se dibuja después queda encima
            dibujarVapor(tiempo, colorVapor)
            dibujarIngredientes(tiempo)  // antes de la olla, así "entran" en ella
            rotate(degrees = balanceo, pivot = Offset(size.width / 2f, size.height * 0.9f)) {
                dibujarOlla(tiempo, colorOlla)
            }
            dibujarSalpicaduras(tiempo)
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

// ---------- Olla ----------
private fun DrawScope.dibujarOlla(tiempo: Float, color: Color) {
    val w = size.width
    val h = size.height
    val ancho = w * OLLA_ANCHO
    val izq = (w - ancho) / 2f
    val bordeY = h * BORDE_Y
    val alto = h * OLLA_ALTO
    val oscuro = Color.Black.copy(alpha = 0.25f)

    // Asas
    val asaAncho = w * 0.10f
    val asaAlto = h * 0.05f
    val asaY = bordeY + h * 0.04f
    drawRoundRect(color, Offset(izq - asaAncho * 0.8f, asaY), Size(asaAncho, asaAlto), CornerRadius(asaAlto / 2f))
    drawRoundRect(color, Offset(izq + ancho - asaAncho * 0.2f, asaY), Size(asaAncho, asaAlto), CornerRadius(asaAlto / 2f))

    // Cuerpo: abajo redondeado, arriba recto
    val radio = w * 0.09f
    drawRoundRect(color, Offset(izq, bordeY), Size(ancho, alto), CornerRadius(radio))
    drawRect(color, Offset(izq, bordeY), Size(ancho, alto / 2f))

    // Brillo para dar volumen
    drawLine(
        color = Color.White.copy(alpha = 0.35f),
        start = Offset(izq + ancho * 0.13f, bordeY + alto * 0.30f),
        end = Offset(izq + ancho * 0.13f, bordeY + alto * 0.70f),
        strokeWidth = w * 0.025f,
        cap = StrokeCap.Round
    )

    // Borde (óvalo, como si la viéramos un poco desde arriba)
    val extra = w * 0.03f
    val bordeAlto = h * 0.10f
    val bordeTopLeft = Offset(izq - extra, bordeY - bordeAlto / 2f)
    val bordeSize = Size(ancho + extra * 2f, bordeAlto)
    drawOval(color, bordeTopLeft, bordeSize)
    drawOval(oscuro, bordeTopLeft, bordeSize)

    // Caldo
    val margen = w * 0.035f
    val caldoTopLeft = Offset(izq + margen - extra, bordeY - bordeAlto / 2f + bordeAlto * 0.15f)
    val caldoSize = Size(ancho - margen * 2f + extra * 2f, bordeAlto * 0.7f)
    drawOval(colorCaldo, caldoTopLeft, caldoSize)

    // Burbujas que crecen y se achican
    for (i in 0..2) {
        val pulso = abs(sin((tiempo * 2f + i * 0.33f) * PI.toFloat()))
        drawCircle(
            color = colorBurbuja,
            radius = w * 0.018f * (0.4f + 0.6f * pulso),
            center = Offset(izq + ancho * (0.3f + i * 0.2f), bordeY + bordeAlto * (0.02f * i))
        )
    }
}

// ---------- Ingredientes cayendo ----------
private fun DrawScope.dibujarIngredientes(tiempo: Float) {
    val w = size.width
    val h = size.height
    val bordeY = h * BORDE_Y
    val inicioY = -h * 0.08f
    val ancho = w * OLLA_ANCHO

    ingredientes.forEachIndexed { i, ing ->
        // Cada ingrediente parte en un momento distinto del ciclo
        val fase = (tiempo + i / ingredientes.size.toFloat()) % 1f
        if (fase >= CAIDA_FIN) return@forEachIndexed // ya cayó dentro de la olla

        // Caída con "gravedad": empieza lento y acelera (fase al cuadrado)
        val avance = (fase * fase) / (CAIDA_FIN * CAIDA_FIN)
        val x = w / 2f + ing.desfaseX * ancho
        val y = inicioY + (bordeY - inicioY) * avance

        translate(left = x, top = y) {
            rotate(degrees = fase * 360f * ing.vueltas, pivot = Offset.Zero) {
                dibujarIngrediente(ing.tipo, w * 0.07f)
            }
        }
    }
}

// Dibuja un ingrediente centrado en (0,0)
private fun DrawScope.dibujarIngrediente(tipo: TipoIngrediente, s: Float) {
    when (tipo) {
        TipoIngrediente.TOMATE -> {
            drawCircle(Color(0xFFE53935), radius = s, center = Offset.Zero)
            drawCircle(Color(0xFF2E7D32), radius = s * 0.3f, center = Offset(0f, -s * 0.85f))
            drawCircle(Color.White.copy(alpha = 0.4f), radius = s * 0.25f, center = Offset(-s * 0.4f, -s * 0.3f))
        }
        TipoIngrediente.ZANAHORIA -> { // rodaja de zanahoria
            drawCircle(Color(0xFFFB8C00), radius = s * 0.85f, center = Offset.Zero)
            drawCircle(Color(0xFFFFB74D), radius = s * 0.45f, center = Offset.Zero)
        }
        TipoIngrediente.HOJA -> {
            drawOval(Color(0xFF43A047), topLeft = Offset(-s * 1.1f, -s * 0.5f), size = Size(s * 2.2f, s))
            drawLine(Color(0xFF1B5E20), Offset(-s, 0f), Offset(s, 0f), strokeWidth = s * 0.12f)
        }
        TipoIngrediente.ARVEJAS -> {
            for (j in -1..1) {
                drawCircle(Color(0xFF7CB342), radius = s * 0.4f, center = Offset(j * s * 0.75f, 0f))
            }
        }
    }
}

// ---------- Salpicaduras al caer ----------
private fun DrawScope.dibujarSalpicaduras(tiempo: Float) {
    val w = size.width
    val h = size.height
    val bordeY = h * BORDE_Y
    val ancho = w * OLLA_ANCHO

    ingredientes.forEachIndexed { i, ing ->
        val fase = (tiempo + i / ingredientes.size.toFloat()) % 1f
        if (fase < CAIDA_FIN) return@forEachIndexed

        // p va de 0 a 1 durante la salpicadura
        val p = (fase - CAIDA_FIN) / (1f - CAIDA_FIN)
        val x = w / 2f + ing.desfaseX * ancho
        val alturaGota = sin(p * PI.toFloat()) * h * 0.08f
        val radio = w * 0.014f * (1f - p)
        drawCircle(colorCaldo, radio, Offset(x - p * w * 0.06f, bordeY - alturaGota))
        drawCircle(colorCaldo, radio, Offset(x + p * w * 0.06f, bordeY - alturaGota))
    }
}

// ---------- Vapor ----------
private fun DrawScope.dibujarVapor(tiempo: Float, color: Color) {
    val w = size.width
    val h = size.height
    val bordeY = h * BORDE_Y
    val ancho = w * OLLA_ANCHO

    for (i in 0..2) {
        val fase = (tiempo + i / 3f) % 1f
        val x = w / 2f + (i - 1) * ancho * 0.28f
        val inicioY = bordeY - h * 0.08f - fase * h * 0.10f
        val largo = h * 0.22f
        val camino = Path()
        val segmentos = 12
        for (s in 0..segmentos) {
            val t = s / segmentos.toFloat()
            val y = inicioY - t * largo
            val dx = sin((t * 2f + fase) * 2f * PI.toFloat()) * w * 0.025f
            if (s == 0) camino.moveTo(x + dx, y) else camino.lineTo(x + dx, y)
        }
        // Se desvanece a medida que sube
        drawPath(
            path = camino,
            color = color.copy(alpha = color.alpha * (1f - fase)),
            style = Stroke(width = w * 0.018f, cap = StrokeCap.Round)
        )
    }
}
