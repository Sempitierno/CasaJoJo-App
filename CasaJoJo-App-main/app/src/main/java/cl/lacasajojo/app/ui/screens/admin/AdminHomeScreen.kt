package cl.lacasajojo.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import cl.lacasajojo.app.ui.components.TarjetaPerfil
import cl.lacasajojo.app.ui.components.rememberSelectorFoto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.ui.components.DialogoConfirmacion
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.ui.components.TarjetaAccion
import cl.lacasajojo.app.viewmodel.AdminHomeViewModel

/** Panel principal del administrador: accesos al menú del día y a las comandas, con un resumen real. */
@Composable
fun AdminHomeScreen(
    nombre: String,
    fotoPerfil: String?,
    onFotoPerfil: (String) -> Unit,
    viewModel: AdminHomeViewModel,
    onMenu: () -> Unit,
    onPedidos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    var mensajeCamara by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(mensajeCamara) {
        mensajeCamara?.let { texto ->
            snackbarHostState.showSnackbar(texto)
            mensajeCamara = null
        }
    }
    val abrirCamara = rememberSelectorFoto(
        prefijo = "perfil",
        onFotoLista = onFotoPerfil,
        onError = { texto -> mensajeCamara = texto }
    )

    PantallaBase(
        titulo = "Panel administrativo",
        snackbarHostState = snackbarHostState,
        acciones = {
            IconButton(onClick = { confirmarSalida = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarjetaPerfil(
                nombre = nombre,
                detalle = "Administrador",
                fotoPerfil = fotoPerfil,
                onTomarFoto = abrirCamara
            )
            Text(
                text = if (nombre.isBlank()) "Hola" else "Hola, $nombre",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Publica el menú del día y prepara las comandas que llegan de los clientes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TarjetaAccion(
                titulo = "Menú del día",
                descripcion = if (ui.totalProductos == 0) {
                    "Aún no hay productos. Crea el primero."
                } else {
                    "${ui.totalProductos} productos · ${ui.publicados} publicados"
                },
                icono = Icons.Filled.Menu,
                onClick = onMenu
            )
            TarjetaAccion(
                titulo = "Comandas",
                descripcion = "${ui.recibidos} recibidas · ${ui.enPreparacion} en preparación · ${ui.listos} listas",
                icono = Icons.AutoMirrored.Filled.List,
                onClick = onPedidos
            )
        }
    }

    if (confirmarSalida) {
        DialogoConfirmacion(
            titulo = "Cerrar sesión",
            mensaje = "¿Quieres cerrar tu sesión?",
            textoConfirmar = "Cerrar sesión",
            onConfirmar = {
                confirmarSalida = false
                onCerrarSesion()
            },
            onCancelar = { confirmarSalida = false }
        )
    }
}
