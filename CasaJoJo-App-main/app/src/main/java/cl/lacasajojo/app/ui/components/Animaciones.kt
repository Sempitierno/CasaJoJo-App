package cl.lacasajojo.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Check de "pedido confirmado" animado: late y rebota continuamente
 * para que siempre se vea vivo y llamativo al confirmar un pedido.
 */
@Composable
fun IconoExitoAnimado(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "exito")

    // Escala que late/rebota continuamente
    val escala by transicion.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    // Halo que late de forma sincronizada
    val halo by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )

    val colorFondo = MaterialTheme.colorScheme.primaryContainer
    Box(modifier = modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .graphicsLayer {
                    scaleX = halo
                    scaleY = halo
                    alpha = 0.35f
                }
                .background(colorFondo, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(88.dp)
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                }
                .background(colorFondo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Pedido confirmado",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private var turnoCargaGlobal = 0

/**
 * Componente que muestra de forma alternada (1 a 1) la sartén y la cacerola
 * en cada estado de carga o acción de la aplicación.
 */
@Composable
fun AnimacionCargaAlternada(
    modifier: Modifier = Modifier,
    mensaje: String = "Cocinando",
    tamano: Dp = 150.dp
) {
    val mostrarSarten = remember {
        val actual = turnoCargaGlobal
        turnoCargaGlobal = (turnoCargaGlobal + 1) % 2
        actual == 0
    }

    if (mostrarSarten) {
        SartenCargando(modifier = modifier, mensaje = mensaje, tamano = tamano)
    } else {
        CacerolaCargando(modifier = modifier, mensaje = mensaje, tamano = tamano)
    }
}
