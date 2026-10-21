package cl.lacasajojo.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.ui.components.BotonPrincipal
import cl.lacasajojo.app.ui.components.CampoClave
import cl.lacasajojo.app.ui.components.CampoTexto
import cl.lacasajojo.app.ui.components.FilaOpcion
import cl.lacasajojo.app.ui.components.MensajeError
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    PantallaBase(titulo = "Crear cuenta", onAtras = onVolver) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoTexto(
                valor = ui.nombre,
                onValorChange = viewModel::onNombreChange,
                etiqueta = "Nombre completo",
                error = ui.errores.nombre
            )
            CampoTexto(
                valor = ui.correo,
                onValorChange = viewModel::onCorreoChange,
                etiqueta = "Correo electrónico",
                error = ui.errores.correo,
                teclado = KeyboardType.Email
            )
            CampoClave(
                valor = ui.password,
                onValorChange = viewModel::onPasswordChange,
                etiqueta = "Contraseña (mínimo 6 caracteres)",
                error = ui.errores.password
            )
            CampoClave(
                valor = ui.confirmacion,
                onValorChange = viewModel::onConfirmacionChange,
                etiqueta = "Confirmar contraseña",
                error = ui.errores.confirmacion,
                accionIme = ImeAction.Done
            )

            Text(
                text = "Tipo de usuario",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Para las pruebas académicas puedes registrarte como cliente o como administrador.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilaOpcion(
                titulo = Rol.CLIENTE.etiqueta,
                descripcion = "Consulta el menú, hace pedidos y revisa su estado",
                seleccionado = ui.rol == Rol.CLIENTE,
                onClick = { viewModel.onRolChange(Rol.CLIENTE) }
            )
            FilaOpcion(
                titulo = Rol.ADMINISTRADOR.etiqueta,
                descripcion = "Publica el menú del día y gestiona las comandas",
                seleccionado = ui.rol == Rol.ADMINISTRADOR,
                onClick = { viewModel.onRolChange(Rol.ADMINISTRADOR) }
            )
            ui.errores.rol?.let { mensaje ->
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            ui.errores.general?.let { mensaje -> MensajeError(texto = mensaje) }

            BotonPrincipal(
                texto = "Crear cuenta",
                onClick = viewModel::registrar,
                cargando = ui.cargando,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (ui.registrado) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "¡Cuenta creada!") },
            text = {
                Text(
                    text = "Tu cuenta se registró correctamente. " +
                        "Ahora puedes iniciar sesión con tu correo y contraseña."
                )
            },
            confirmButton = {
                TextButton(onClick = onRegistroExitoso) {
                    Text(text = "Ir a iniciar sesión")
                }
            }
        )
    }
}
