package cl.lacasajojo.app.ui.screens.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.data.entity.PedidoConDetalle
import cl.lacasajojo.app.ui.components.EstadoVacio
import cl.lacasajojo.app.ui.components.EtiquetaEstado
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.ui.components.PantallaCargando
import cl.lacasajojo.app.ui.components.TarjetaJoJo
import cl.lacasajojo.app.util.Formato
import cl.lacasajojo.app.util.codigo
import cl.lacasajojo.app.util.resumenProductos
import cl.lacasajojo.app.viewmodel.HistorialViewModel

/** Historial de pedidos del cliente con la sesión iniciada. El estado se actualiza solo desde Room. */
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel,
    onVolver: () -> Unit,
    onVerPedido: (String) -> Unit,
    onNuevoPedido: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    PantallaBase(titulo = "Mis pedidos", onAtras = onVolver) { innerPadding ->
        when {
            ui.cargando -> PantallaCargando(modifier = Modifier.padding(innerPadding))
            ui.pedidos.isEmpty() -> EstadoVacio(
                modifier = Modifier.padding(innerPadding),
                titulo = "Aún no tienes pedidos",
                mensaje = "Cuando hagas tu primer pedido aparecerá aquí con su estado actual.",
                icono = Icons.Filled.ShoppingCart,
                acciones = {
                    Button(onClick = onNuevoPedido) {
                        Text(text = "Hacer un pedido")
                    }
                }
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ui.pedidos, key = { item -> item.pedido.id }) { item ->
                    TarjetaPedidoCliente(item = item, onClick = { onVerPedido(item.pedido.id) })
                }
            }
        }
    }
}

@Composable
private fun TarjetaPedidoCliente(
    item: PedidoConDetalle,
    onClick: () -> Unit
) {
    val pedido = item.pedido
    TarjetaJoJo(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Pedido ${pedido.codigo}", style = MaterialTheme.typography.titleMedium)
                EtiquetaEstado(estado = pedido.estado)
            }
            Text(
                text = "Realizado el ${Formato.fechaHora(pedido.creadoEn)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.resumenProductos(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Retiro: ${Formato.hora(pedido.horaRetiro)} hrs",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = Formato.precio(pedido.total),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
