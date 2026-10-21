package cl.lacasajojo.app.ui.components

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.lacasajojo.app.util.Formato
import java.time.LocalTime

/** Campo de texto con mensaje de error (rojo) o ayuda opcional debajo. */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    accionIme: ImeAction = ImeAction.Next,
    unaSolaLinea: Boolean = true,
    minLineas: Int = 1,
    enabled: Boolean = true,
    onAccionIme: () -> Unit = {}
) {
    val foco = LocalFocusManager.current
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = etiqueta) },
        isError = error != null,
        supportingText = {
            val texto = error ?: ayuda
            if (texto != null) {
                Text(text = texto)
            }
        },
        singleLine = unaSolaLinea,
        minLines = minLineas,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = teclado, imeAction = accionIme),
        keyboardActions = KeyboardActions(
            onNext = { foco.moveFocus(FocusDirection.Down) },
            onDone = {
                foco.clearFocus()
                onAccionIme()
            }
        )
    )
}

/** Campo de contraseña con botón para mostrar u ocultar el texto. */
@Composable
fun CampoClave(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    accionIme: ImeAction = ImeAction.Next,
    onAccionIme: () -> Unit = {}
) {
    val foco = LocalFocusManager.current
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = etiqueta) },
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(text = error)
            }
        },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) {
                Text(text = if (visible) "Ocultar" else "Mostrar")
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = accionIme),
        keyboardActions = KeyboardActions(
            onNext = { foco.moveFocus(FocusDirection.Down) },
            onDone = {
                foco.clearFocus()
                onAccionIme()
            }
        )
    )
}

/** Botón principal a todo el ancho; muestra un indicador de progreso mientras [cargando] es true. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    habilitado: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(text = texto)
    }
}

/**
 * Fila seleccionable tipo "radio" (una opción entre varias). Si [habilitado] es false se ve atenuada
 * y no puede seleccionarse (productos no disponibles).
 */
@Composable
fun FilaOpcion(
    titulo: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    descripcion: String? = null,
    precio: String? = null,
    habilitado: Boolean = true,
    aviso: String? = null
) {
    val transparencia = if (habilitado) 1f else 0.5f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = seleccionado,
                enabled = habilitado,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(selected = seleccionado, onClick = null, enabled = habilitado)
        Column(modifier = Modifier.weight(1f).alpha(transparencia)) {
            Text(text = titulo, style = MaterialTheme.typography.bodyLarge)
            if (descripcion != null) {
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (aviso != null) {
                Text(
                    text = aviso,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        if (precio != null) {
            Text(
                text = precio,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(transparencia)
            )
        }
    }
}

/** Fila con título, descripción y un interruptor (Switch). */
@Composable
fun FilaInterruptor(
    titulo: String,
    descripcion: String,
    marcado: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = marcado, onCheckedChange = onCambio)
    }
}

/**
 * Selector de hora de retiro. Usa el diálogo NATIVO de Android (android.app.TimePickerDialog),
 * que respeta el formato de 12 o 24 horas configurado en el teléfono.
 */
@Composable
fun SelectorHoraRetiro(
    hora: LocalTime?,
    error: String?,
    onHoraSeleccionada: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedButton(
            onClick = {
                val inicial = hora ?: LocalTime.now().plusMinutes(30)
                TimePickerDialog(
                    contexto,
                    TimePickerDialog.OnTimeSetListener { _, horaElegida, minutoElegido ->
                        onHoraSeleccionada(LocalTime.of(horaElegida, minutoElegido))
                    },
                    inicial.hour,
                    inicial.minute,
                    DateFormat.is24HourFormat(contexto)
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Text(
                text = if (hora == null) {
                    "Elegir hora de retiro"
                } else {
                    "Retiro a las ${Formato.horaLocal(hora)}  (cambiar)"
                }
            )
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
