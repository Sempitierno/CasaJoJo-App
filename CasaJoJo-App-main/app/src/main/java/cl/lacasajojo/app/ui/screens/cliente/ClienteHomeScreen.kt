package cl.lacasajojo.app.ui.screens.cliente

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import cl.lacasajojo.app.ui.components.TarjetaPerfil
import cl.lacasajojo.app.ui.components.TarjetaUbicacion
import cl.lacasajojo.app.ui.components.rememberAbrirMapas
import cl.lacasajojo.app.ui.components.rememberSelectorFoto
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.lacasajojo.app.ui.components.DialogoConfirmacion
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.ui.components.TarjetaAccion
import cl.lacasajojo.app.viewmodel.ClienteHomeViewModel

/**
 * Panel principal del cliente. Al abrirlo (Android 13+) solicita el permiso para mostrar
 * notificaciones locales, que avisan cuando cambia el estado de sus pedidos.
 */
@Composable
fun ClienteHomeScreen(
    nombre: String,
    fotoPerfil: String?,
    onFotoPerfil: (String) -> Unit,
    viewModel: ClienteHomeViewModel,
    onMenu: () -> Unit,
    onNuevoPedido: () -> Unit,
    onHistorial: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val contexto = LocalContext.current
    var permisoSolicitado by rememberSaveable { mutableStateOf(false) }
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Con el permiso recién concedido, se muestran los avisos que estaban pendientes.
        viewModel.reintentarNotificaciones()
    }

    LaunchedEffect(Unit) {
        val faltaPermiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        if (faltaPermiso && !permisoSolicitado) {
            permisoSolicitado = true
            lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var mensajeCamara by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(mensajeCamara) {
        mensajeCamara?.let { texto ->
            snackbarHostState.showSnackbar(texto)
            mensajeCamara = null
        }
    }
    val abrirMapas = rememberAbrirMapas()
    val abrirCamara = rememberSelectorFoto(
        prefijo = "perfil",
        onFotoLista = onFotoPerfil,
        onError = { texto -> mensajeCamara = texto }
    )

    PantallaBase(
        titulo = "La Casa JoJo",
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
                detalle = "Cliente",
                fotoPerfil = fotoPerfil,
                onTomarFoto = abrirCamara
            )
            Text(
                text = if (nombre.isBlank()) "Hola" else "Hola, $nombre",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "¿Qué almorzamos hoy? Revisa el menú, arma tu pedido y elige a qué hora retirarlo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TarjetaUbicacion(onVerMapa = abrirMapas)
            TarjetaAccion(
                titulo = "Ver menú del día",
                descripcion = "Platos, acompañamientos, ensaladas, postres y bebidas",
                icono = Icons.Filled.Menu,
                onClick = onMenu
            )
            TarjetaAccion(
                titulo = "Hacer un pedido",
                descripcion = "Arma tu combinación y elige la hora de retiro",
                icono = Icons.Filled.ShoppingCart,
                onClick = onNuevoPedido
            )
            TarjetaAccion(
                titulo = "Mis pedidos",
                descripcion = "Historial y estado de tus pedidos",
                icono = Icons.AutoMirrored.Filled.List,
                onClick = onHistorial
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
