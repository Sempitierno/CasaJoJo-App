package cl.lacasajojo.app.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.util.Ubicacion

/**
 * Devuelve la función que abre la ubicación del local en Google Maps (o en el navegador si no está instalada).
 * No necesita permisos: solo entrega un enlace al sistema.
 */
@Composable
fun rememberAbrirMapas(): () -> Unit {
    val contexto = LocalContext.current
    return {
        try {
            contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Ubicacion.ENLACE_MAPS)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                contexto,
                "No hay ninguna app disponible para abrir el mapa en este dispositivo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

/** Tarjeta del detalle del pedido: indica cómo llegar a retirarlo. */
@Composable
fun TarjetaRetiro(
    estado: EstadoPedido,
    onVerUbicacion: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaJoJo(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Retira tu pedido", style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (estado == EstadoPedido.LISTO_RETIRO) {
                    "Tu pedido ya está listo. Mira cómo llegar a ${Ubicacion.NOMBRE_LOCAL}."
                } else {
                    "Estamos preparando tu pedido. Mira cómo llegar a ${Ubicacion.NOMBRE_LOCAL}."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SelectionContainer {
                Text(
                    text = Ubicacion.DIRECCION,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            BotonPrincipal(texto = "Ver ubicación del local", onClick = onVerUbicacion)
        }
    }
}

/**
 * Tarjeta "Dónde estamos": muestra la dirección ESCRITA (se puede seleccionar y copiar) y, debajo, el botón que abre
 * Google Maps. Así el cliente sabe llegar aunque el mapa no se abra o no tenga Internet.
 */
@Composable
fun TarjetaUbicacion(
    onVerMapa: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaJoJo(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Dónde estamos", style = MaterialTheme.typography.titleMedium)
            Text(
                text = Ubicacion.NOMBRE_LOCAL,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SelectionContainer {
                Text(
                    text = Ubicacion.DIRECCION,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            BotonPrincipal(texto = "Abrir en Google Maps", onClick = onVerMapa)
        }
    }
}
