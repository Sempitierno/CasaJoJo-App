package cl.lacasajojo.app.ui.screens.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.data.entity.ProductoEntity
import cl.lacasajojo.app.ui.components.EncabezadoCategoria
import cl.lacasajojo.app.ui.components.EstadoVacio
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.ui.components.PantallaCargando
import cl.lacasajojo.app.ui.components.TarjetaJoJo
import cl.lacasajojo.app.util.Formato
import cl.lacasajojo.app.viewmodel.ClienteMenuViewModel

/** Menú publicado por el administrador, agrupado por categoría. Los productos agotados se ven atenuados. */
@Composable
fun ClienteMenuScreen(
    viewModel: ClienteMenuViewModel,
    onVolver: () -> Unit,
    onHacerPedido: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    PantallaBase(
        titulo = "Menú del día",
        onAtras = onVolver,
        botonFlotante = {
            if (ui.sePuedePedir) {
                ExtendedFloatingActionButton(
                    onClick = onHacerPedido,
                    icon = { Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null) },
                    text = { Text(text = "Hacer pedido") }
                )
            }
        }
    ) { innerPadding ->
        when {
            ui.cargando -> PantallaCargando(modifier = Modifier.padding(innerPadding))
            !ui.hayMenu -> EstadoVacio(
                modifier = Modifier.padding(innerPadding),
                titulo = "Aún no hay menú publicado",
                mensaje = "El equipo de La Casa JoJo publica el menú cada mañana. " +
                    "Vuelve a revisar un poco más tarde.",
                icono = Icons.Filled.Menu
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ui.grupos.forEach { (categoria, productos) ->
                    item(key = "cab_${categoria.name}") {
                        EncabezadoCategoria(texto = categoria.etiqueta)
                    }
                    items(productos, key = { producto -> "prod_${producto.id}" }) { producto ->
                        TarjetaProductoMenu(producto = producto)
                    }
                }
                if (!ui.sePuedePedir) {
                    item(key = "aviso_sin_platos") {
                        Text(
                            text = "Por ahora no hay platos de fondo disponibles, " +
                                "así que todavía no se pueden hacer pedidos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaProductoMenu(producto: ProductoEntity) {
    val transparencia = if (producto.disponible) 1f else 0.55f
    TarjetaJoJo(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(transparencia),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = producto.nombre, style = MaterialTheme.typography.titleMedium)
                if (producto.descripcion.isNotBlank()) {
                    Text(
                        text = producto.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (producto.disponible) "Disponible" else "No disponible",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (producto.disponible) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            Text(
                text = Formato.precio(producto.precio),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.alpha(transparencia)
            )
        }
    }
}
