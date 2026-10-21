package cl.lacasajojo.app.ui.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.ui.components.BotonPrincipal
import cl.lacasajojo.app.ui.components.EstadoVacio
import cl.lacasajojo.app.ui.components.EtiquetaEstado
import cl.lacasajojo.app.ui.components.FilaDato
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.ui.components.PantallaCargando
import cl.lacasajojo.app.ui.components.TarjetaJoJo
import cl.lacasajojo.app.util.Formato
import cl.lacasajojo.app.util.codigo
import cl.lacasajojo.app.viewmodel.DetallePedidoViewModel

/** Confirmación de pedido exitoso. Muestra los datos leídos desde Room, no valores simulados. */
@Composable
fun PedidoConfirmadoScreen(
    viewModel: DetallePedidoViewModel,
    onVerDetalle: () -> Unit,
    onVolverInicio: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val item = ui.pedido

    PantallaBase(titulo = "Pedido confirmado") { innerPadding ->
        when {
            ui.cargando -> PantallaCargando(modifier = Modifier.padding(innerPadding))
            item == null -> EstadoVacio(
                modifier = Modifier.padding(innerPadding),
                titulo = "No encontramos tu pedido",
                mensaje = "Revisa el historial de pedidos desde el inicio.",
                acciones = {
                    Button(onClick = onVolverInicio) {
                        Text(text = "Volver al inicio")
                    }
                }
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "¡Pedido confirmado!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tu pedido ${item.pedido.codigo} fue enviado a la cocina de La Casa JoJo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                TarjetaJoJo(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilaDato(
                            etiqueta = "Retiro",
                            valor = "${Formato.hora(item.pedido.horaRetiro)} hrs"
                        )
                        FilaDato(etiqueta = "Total", valor = Formato.precio(item.pedido.total))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Estado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(112.dp)
                            )
                            EtiquetaEstado(estado = item.pedido.estado)
                        }
                    }
                }

                BotonPrincipal(texto = "Ver detalle del pedido", onClick = onVerDetalle)
                OutlinedButton(
                    onClick = onVolverInicio,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Volver al inicio")
                }
            }
        }
    }
}
