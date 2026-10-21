package cl.lacasajojo.app.ui.screens.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.ui.components.EstadoVacio
import cl.lacasajojo.app.ui.components.EtiquetaEstado
import cl.lacasajojo.app.ui.components.FilaDato
import cl.lacasajojo.app.ui.components.LineaResumen
import cl.lacasajojo.app.ui.components.LineaTiempoEstado
import cl.lacasajojo.app.ui.components.LineaTotal
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.ui.components.PantallaCargando
import cl.lacasajojo.app.ui.components.TarjetaJoJo
import cl.lacasajojo.app.util.Formato
import cl.lacasajojo.app.util.codigo
import cl.lacasajojo.app.util.detallesOrdenados
import cl.lacasajojo.app.viewmodel.DetallePedidoViewModel

/** Detalle de un pedido propio: estado con línea de tiempo, datos de retiro y productos con precios históricos. */
@Composable
fun DetallePedidoScreen(
    viewModel: DetallePedidoViewModel,
    onVolver: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val item = ui.pedido

    PantallaBase(
        titulo = if (item == null) "Pedido" else "Pedido ${item.pedido.codigo}",
        onAtras = onVolver
    ) { innerPadding ->
        when {
            ui.cargando -> PantallaCargando(modifier = Modifier.padding(innerPadding))
            item == null -> EstadoVacio(
                modifier = Modifier.padding(innerPadding),
                titulo = "No encontramos este pedido",
                mensaje = "Es posible que no exista o que pertenezca a otra cuenta.",
                acciones = {
                    OutlinedButton(onClick = onVolver) {
                        Text(text = "Volver")
                    }
                }
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TarjetaJoJo(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estado del pedido",
                                style = MaterialTheme.typography.titleMedium
                            )
                            EtiquetaEstado(estado = item.pedido.estado)
                        }
                        LineaTiempoEstado(estadoActual = item.pedido.estado)
                    }
                }

                TarjetaJoJo(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Datos del pedido", style = MaterialTheme.typography.titleMedium)
                        FilaDato(etiqueta = "Identificador", valor = item.pedido.codigo)
                        FilaDato(etiqueta = "ID único", valor = item.pedido.id)
                        FilaDato(etiqueta = "Creado", valor = Formato.fechaHora(item.pedido.creadoEn))
                        FilaDato(
                            etiqueta = "Hora de retiro",
                            valor = "${Formato.hora(item.pedido.horaRetiro)} hrs"
                        )
                    }
                }

                TarjetaJoJo(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Tu pedido", style = MaterialTheme.typography.titleMedium)
                        item.detallesOrdenados().forEach { detalle ->
                            LineaResumen(
                                categoria = detalle.categoria.etiqueta,
                                nombre = detalle.nombreProducto,
                                precio = Formato.precio(detalle.precioUnitario)
                            )
                        }
                        if (item.pedido.especificaciones.isNotBlank()) {
                            HorizontalDivider()
                            FilaDato(etiqueta = "Notas", valor = item.pedido.especificaciones)
                        }
                        HorizontalDivider()
                        LineaTotal(total = Formato.precio(item.pedido.total))
                    }
                }
            }
        }
    }
}
