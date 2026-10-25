package cl.lacasajojo.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.ui.components.AnimacionCargaAlternada
import cl.lacasajojo.app.ui.components.EncabezadoMarca
import cl.lacasajojo.app.viewmodel.EstadoSesion

/**
 * Pantalla de arranque: muestra la marca y la animación de carga alternada durante al menos 3 segundos.
 * Luego redirige: sin sesión -> inicio de sesión; con sesión -> panel del administrador o del cliente.
 */
@Composable
fun InicioScreen(
    estado: EstadoSesion,
    onSinSesion: () -> Unit,
    onSesionActiva: (Rol) -> Unit
) {
    var listoParaRedirigir by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        listoParaRedirigir = true
    }

    LaunchedEffect(estado, listoParaRedirigir) {
        if (listoParaRedirigir) {
            when (estado) {
                is EstadoSesion.Cargando -> Unit
                is EstadoSesion.SinSesion -> onSinSesion()
                is EstadoSesion.Activa -> onSesionActiva(estado.usuario.rol)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            EncabezadoMarca()
            AnimacionCargaAlternada(mensaje = "Iniciando La Casa JoJo", tamano = 160.dp)
        }
    }
}
