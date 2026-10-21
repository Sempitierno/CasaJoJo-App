package cl.lacasajojo.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.lacasajojo.app.data.entity.EstadoPedido

/** Tarjeta de la app: fondo claro con borde fino. Si se entrega [onClick] es clicable. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarjetaJoJo(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    val borde = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, colors = colores, border = borde, content = contenido)
    } else {
        Card(modifier = modifier, colors = colores, border = borde, content = contenido)
    }
}

/** Tarjeta grande con ícono, título y descripción que navega a otra pantalla (paneles principales). */
@Composable
fun TarjetaAccion(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaJoJo(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Colores (fondo, texto) de la etiqueta de cada estado: maíz = recibido, ají = en preparación, palta = listo. */
@Composable
fun coloresEstado(estado: EstadoPedido): Pair<Color, Color> {
    val colores = MaterialTheme.colorScheme
    return when (estado) {
        EstadoPedido.RECIBIDO -> colores.secondaryContainer to colores.onSecondaryContainer
        EstadoPedido.EN_PREPARACION -> colores.tertiaryContainer to colores.onTertiaryContainer
        EstadoPedido.LISTO_RETIRO -> colores.primaryContainer to colores.onPrimaryContainer
    }
}

/** Color fuerte del estado (barra lateral de la comanda y línea de tiempo). */
@Composable
fun colorAcentoEstado(estado: EstadoPedido): Color {
    val colores = MaterialTheme.colorScheme
    return when (estado) {
        EstadoPedido.RECIBIDO -> colores.secondary
        EstadoPedido.EN_PREPARACION -> colores.tertiary
        EstadoPedido.LISTO_RETIRO -> colores.primary
    }
}

/** Etiqueta redondeada con el nombre del estado del pedido. */
@Composable
fun EtiquetaEstado(estado: EstadoPedido, modifier: Modifier = Modifier) {
    val (fondo, texto) = coloresEstado(estado)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = fondo,
        contentColor = texto
    ) {
        Text(
            text = estado.etiqueta,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

/** Título de sección (categorías del menú, secciones de formularios). */
@Composable
fun EncabezadoCategoria(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 20.dp, bottom = 4.dp)
    )
}

/** Fila "etiqueta · valor · precio" usada en comandas, resúmenes y detalles. */
@Composable
fun FilaDato(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    precio: String? = null,
    destacado: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = etiqueta,
            style = if (destacado) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = if (destacado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(112.dp)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (precio != null) {
            Text(
                text = precio,
                style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** Mensaje de error destacado (rojo) para errores generales de un formulario. */
@Composable
fun MensajeError(texto: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Logotipo y nombre de la marca (pantallas de inicio, inicio de sesión y registro). */
@Composable
fun EncabezadoMarca(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(text = "La Casa JoJo", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Almuerzos y menús del día",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Línea de tiempo vertical con los tres estados del pedido; marca hasta dónde ha avanzado. */
@Composable
fun LineaTiempoEstado(estadoActual: EstadoPedido, modifier: Modifier = Modifier) {
    val pasos = EstadoPedido.entries
    val colorActivo = colorAcentoEstado(estadoActual)
    val colorInactivo = MaterialTheme.colorScheme.outlineVariant
    Column(modifier = modifier.fillMaxWidth()) {
        pasos.forEachIndexed { indice, estado ->
            val alcanzado = estado.ordinal <= estadoActual.ordinal
            val esActual = estado == estadoActual
            val colorPaso = if (alcanzado) colorActivo else colorInactivo
            val hayPasoSiguiente = indice < pasos.size - 1
            val lineaAlcanzada = estado.ordinal < estadoActual.ordinal

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (alcanzado) colorPaso else Color.Transparent, CircleShape)
                            .border(2.dp, colorPaso, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (alcanzado) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                    if (hayPasoSiguiente) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(36.dp)
                                .background(if (lineaAlcanzada) colorActivo else colorInactivo)
                        )
                    }
                }
                Column(modifier = Modifier.padding(top = 3.dp, bottom = 8.dp)) {
                    Text(
                        text = estado.etiqueta,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (esActual) FontWeight.Bold else FontWeight.Normal,
                        color = if (alcanzado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (esActual) {
                        Text(
                            text = estado.mensajeCliente,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** Línea de un resumen de pedido: categoría (pequeña) y nombre del producto a la izquierda, precio a la derecha. */
@Composable
fun LineaResumen(
    categoria: String,
    nombre: String,
    precio: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoria,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = nombre, style = MaterialTheme.typography.bodyMedium)
        }
        if (precio != null) {
            Text(
                text = precio,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** Línea del total de un pedido. */
@Composable
fun LineaTotal(total: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Total", style = MaterialTheme.typography.titleMedium)
        Text(text = total, style = MaterialTheme.typography.titleMedium)
    }
}
