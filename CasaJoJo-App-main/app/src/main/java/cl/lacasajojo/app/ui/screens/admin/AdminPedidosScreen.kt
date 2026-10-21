package cl.lacasajojo.app.ui.screens.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.data.entity.PedidoConDetalle
import cl.lacasajojo.app.ui.components.BotonPrincipal
import cl.lacasajojo.app.ui.components.EstadoVacio
import cl.lacasajojo.app.ui.components.EtiquetaEstado
import cl.lacasajojo.app.ui.components.FilaDato
import cl.lacasajojo.app.ui.components.LineaResumen
import cl.lacasajojo.app.ui.components.LineaTotal
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.ui.components.PantallaCargando
import cl.lacasajojo.app.ui.components.TarjetaJoJo
import cl.lacasajojo.app.ui.components.colorAcentoEstado
import cl.lacasajojo.app.util.Formato
import cl.lacasajojo.app.util.codigo
import cl.lacasajojo.app.util.detalleDe
import cl.lacasajojo.app.viewmodel.AdminPedidosViewModel

/**
 * Comandas: todos los pedidos guardados en el dispositivo. Los pendientes se ordenan por hora de retiro
 * (el más próximo primero) y se pueden filtrar por estado. Desde cada tarjeta se cambia el estado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPedidosScreen(
    viewModel: AdminPedidosViewModel,
    onVolver: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { mensaje -> snackbarHostState.showSnackbar(mensaje) }
    }

    PantallaBase(
        titulo = "Comandas",
        onAtras = onVolver,
        snackbarHostState = snackbarHostState
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = ui.filtro == null,
                    onClick = { viewModel.filtrar(null) },
                    label = { Text(text = "Todos (${ui.total})") }
                )
                EstadoPedido.entries.forEach { estado ->
                    FilterChip(
                        selected = ui.filtro == estado,
                        onClick = { viewModel.filtrar(estado) },
                        label = { Text(text = "${estado.etiqueta} (${ui.conteos[estado] ?: 0})") }
                    )
                }
            }

            when {
                ui.cargando -> PantallaCargando()
                ui.total == 0 -> EstadoVacio(
                    titulo = "Aún no hay pedidos",
                    mensaje = "Cuando los clientes confirmen pedidos aparecerán aquí, " +
                        "ordenados por hora de retiro.",
                    icono = Icons.AutoMirrored.Filled.List
                )
                ui.pedidos.isEmpty() -> EstadoVacio(
                    titulo = "No hay pedidos en este estado",
                    mensaje = "Prueba con otro filtro o con la opción \"Todos\".",
                    icono = Icons.AutoMirrored.Filled.List
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ui.pedidos, key = { item -> item.pedido.id }) { item ->
                        TarjetaComanda(
                            item = item,
                            onCambiarEstado = { nuevo -> viewModel.cambiarEstado(item.pedido, nuevo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaComanda(
    item: PedidoConDetalle,
    onCambiarEstado: (EstadoPedido) -> Unit
) {
    val pedido = item.pedido
    val acento = colorAcentoEstado(pedido.estado)

    TarjetaJoJo(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Barra lateral del color del estado (maíz, ají o palta): se lee de un vistazo en cocina.
                .drawBehind {
                    drawRect(color = acento, size = Size(width = 6.dp.toPx(), height = size.height))
                }
                .padding(start = 22.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pedido ${pedido.codigo}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Creado ${Formato.fechaHora(pedido.creadoEn)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EtiquetaEstado(estado = pedido.estado)
            }

            Text(
                text = "Retiro a las ${Formato.hora(pedido.horaRetiro)}",
                style = MaterialTheme.typography.titleLarge
            )

            FilaDato(etiqueta = "Cliente", valor = pedido.clienteNombre)
            FilaDato(etiqueta = "Correo", valor = pedido.clienteCorreo)

            HorizontalDivider()

            CategoriaProducto.entries.forEach { categoria ->
                val detalle = item.detalleDe(categoria)
                LineaResumen(
                    categoria = categoria.etiqueta,
                    nombre = detalle?.nombreProducto ?: "—",
                    precio = detalle?.let { Formato.precio(it.precioUnitario) }
                )
            }

            if (pedido.especificaciones.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Especificaciones o restricciones",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = pedido.especificaciones,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            HorizontalDivider()

            LineaTotal(total = Formato.precio(pedido.total))

            AccionesEstado(estado = pedido.estado, onCambiarEstado = onCambiarEstado)
        }
    }
}

/** Botón para avanzar al siguiente estado y, si hubo un error, volver al anterior. */
@Composable
private fun AccionesEstado(
    estado: EstadoPedido,
    onCambiarEstado: (EstadoPedido) -> Unit
) {
    val siguiente = estado.siguiente()
    val anterior = estado.anterior()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (siguiente != null) {
            BotonPrincipal(
                texto = textoAccion(siguiente),
                onClick = { onCambiarEstado(siguiente) }
            )
        } else {
            Text(
                text = "Pedido listo. Esperando el retiro del cliente.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (anterior != null) {
            TextButton(onClick = { onCambiarEstado(anterior) }) {
                Text(text = "Volver a «${anterior.etiqueta}»")
            }
        }
    }
}

private fun textoAccion(destino: EstadoPedido): String = when (destino) {
    EstadoPedido.RECIBIDO -> "Marcar como recibido"
    EstadoPedido.EN_PREPARACION -> "Iniciar preparación"
    EstadoPedido.LISTO_RETIRO -> "Marcar listo para retiro"
}
