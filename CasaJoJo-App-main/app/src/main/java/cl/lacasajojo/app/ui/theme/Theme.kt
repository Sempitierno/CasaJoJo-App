package cl.lacasajojo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * Identidad visual de La Casa JoJo, inspirada en la cocina chilena:
 *   - Primario: verde "palta"  (platos listos, acciones principales)
 *   - Secundario: amarillo "maíz" (pedidos recién recibidos)
 *   - Terciario: rojo "ají" (pedidos en preparación, alertas de atención)
 * No se usa el color dinámico del sistema para conservar siempre la identidad de la marca.
 */
private val EsquemaClaro = lightColorScheme(
    primary = Color(0xFF2B6A3C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC3EBC9),
    onPrimaryContainer = Color(0xFF00210C),
    inversePrimary = Color(0xFF97D5A3),
    secondary = Color(0xFF8A6100),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDF9A),
    onSecondaryContainer = Color(0xFF2B1D00),
    tertiary = Color(0xFFAF3A2B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDAD4),
    onTertiaryContainer = Color(0xFF410002),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF2F5EC),
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFF8FAF4),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDDE5DB),
    onSurfaceVariant = Color(0xFF414941),
    surfaceTint = Color(0xFF2B6A3C),
    inverseSurface = Color(0xFF2F312D),
    inverseOnSurface = Color(0xFFF0F1EA),
    outline = Color(0xFF717971),
    outlineVariant = Color(0xFFC1C9BF),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFF8FAF4),
    surfaceDim = Color(0xFFD9DBD3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F7EF),
    surfaceContainer = Color(0xFFEEF1E8),
    surfaceContainerHigh = Color(0xFFE8EBE2),
    surfaceContainerHighest = Color(0xFFE2E5DC)
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFF97D5A3),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF1B5130),
    onPrimaryContainer = Color(0xFFB2F1BD),
    inversePrimary = Color(0xFF2B6A3C),
    secondary = Color(0xFFF2BF4A),
    onSecondary = Color(0xFF402D00),
    secondaryContainer = Color(0xFF5C4200),
    onSecondaryContainer = Color(0xFFFFDF9A),
    tertiary = Color(0xFFFFB4A8),
    onTertiary = Color(0xFF690006),
    tertiaryContainer = Color(0xFF8C1E14),
    onTertiaryContainer = Color(0xFFFFDAD4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121411),
    onBackground = Color(0xFFE2E3DD),
    surface = Color(0xFF121411),
    onSurface = Color(0xFFE2E3DD),
    surfaceVariant = Color(0xFF414941),
    onSurfaceVariant = Color(0xFFC1C9BF),
    surfaceTint = Color(0xFF97D5A3),
    inverseSurface = Color(0xFFE2E3DD),
    inverseOnSurface = Color(0xFF2F312D),
    outline = Color(0xFF8B938A),
    outlineVariant = Color(0xFF414941),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF383A36),
    surfaceDim = Color(0xFF121411),
    surfaceContainerLowest = Color(0xFF0D0F0C),
    surfaceContainerLow = Color(0xFF1A1C19),
    surfaceContainer = Color(0xFF1E201D),
    surfaceContainerHigh = Color(0xFF282B27),
    surfaceContainerHighest = Color(0xFF333531)
)

private val FormasJoJo = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun CasaJoJoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = TipografiaJoJo,
        shapes = FormasJoJo,
        content = content
    )
}
