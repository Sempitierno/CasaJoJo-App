package cl.lacasajojo.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.R
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.ui.components.BotonPrincipal
import cl.lacasajojo.app.ui.components.CampoClave
import cl.lacasajojo.app.ui.components.CampoTexto
import cl.lacasajojo.app.ui.components.MensajeError
import cl.lacasajojo.app.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: (Rol) -> Unit,
    onIrARegistro: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    // Cuando el inicio de sesión resulta correcto, se navega al panel que corresponde al rol.
    LaunchedEffect(ui.autenticado) {
        ui.autenticado?.let { rol -> onLoginExitoso(rol) }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // NUEVO LOGO
            Image(
                painter = painterResource(id = R.drawable.logo_casa_jojo),
                contentDescription = "Logo Casa Jo-Jo",
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Ingresa con tu correo y contraseña. No necesitas conexión a Internet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CampoTexto(
                valor = ui.correo,
                onValorChange = viewModel::onCorreoChange,
                etiqueta = "Correo electrónico",
                error = ui.errorCorreo,
                teclado = KeyboardType.Email
            )
            CampoClave(
                valor = ui.password,
                onValorChange = viewModel::onPasswordChange,
                etiqueta = "Contraseña",
                error = ui.errorPassword,
                accionIme = ImeAction.Done,
                onAccionIme = viewModel::iniciarSesion
            )

            ui.errorGeneral?.let { mensaje -> MensajeError(texto = mensaje) }

            BotonPrincipal(
                texto = "Iniciar sesión",
                onClick = viewModel::iniciarSesion,
                cargando = ui.cargando
            )

            TextButton(onClick = onIrARegistro) {
                Text(text = "¿No tienes cuenta? Regístrate")
            }
        }
    }
}