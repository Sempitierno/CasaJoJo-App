package cl.lacasajojo.app.ui.components

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cl.lacasajojo.app.util.AlmacenFotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Prepara la cámara del dispositivo y devuelve la función que la abre.
 *
 * Usa la app de cámara del sistema (TakePicture): es el recurso nativo, no requiere el permiso CAMERA y la foto
 * se guarda en la carpeta privada de la app. El nombre del archivo pendiente se conserva con rememberSaveable,
 * así que sobrevive a rotaciones mientras la cámara está abierta.
 *
 * @param prefijo "pedido" o "perfil" (nombre inicial del archivo).
 * @param onFotoLista recibe el NOMBRE del archivo ya guardado.
 * @param onError recibe un mensaje para mostrar (sin cámara disponible, error al abrirla).
 */
@Composable
fun rememberAbrirCamara(
    prefijo: String,
    onFotoLista: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val contexto = LocalContext.current
    var archivoPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    val alFotoLista by rememberUpdatedState(onFotoLista)
    val alError by rememberUpdatedState(onError)

    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val nombre = archivoPendiente
        archivoPendiente = null
        if (nombre != null) {
            val archivo = AlmacenFotos.archivo(contexto, nombre)
            if (exito && AlmacenFotos.esValida(archivo)) {
                alFotoLista(nombre)
            } else {
                // Cancelada: no se deja un archivo vacío ocupando espacio.
                AlmacenFotos.eliminar(contexto, nombre)
            }
        }
    }

    return {
        try {
            val archivo = AlmacenFotos.crearArchivoNuevo(contexto, prefijo)
            archivoPendiente = archivo.name
            lanzador.launch(AlmacenFotos.uriParaCamara(contexto, archivo))
        } catch (e: ActivityNotFoundException) {
            archivoPendiente = null
            alError("Este dispositivo no tiene una app de cámara disponible.")
        } catch (e: Exception) {
            archivoPendiente = null
            alError("No se pudo abrir la cámara. Intenta nuevamente.")
        }
    }
}

/**
 * Selector de origen de la foto: pregunta si se toma con la cámara o se elige de la galería y devuelve la función que
 * abre ese diálogo. La galería usa el selector de fotos del sistema (PickVisualMedia): no necesita permisos y la app
 * solo recibe la foto que el usuario elige. En ambos casos la foto queda copiada en la carpeta privada de la app.
 *
 * @param prefijo "pedido", "perfil" o "plato" (nombre inicial del archivo).
 * @param onFotoLista recibe el NOMBRE del archivo ya guardado.
 * @param onError recibe un mensaje para mostrar.
 */
@Composable
fun rememberSelectorFoto(
    prefijo: String,
    onFotoLista: (String) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    var mostrarOpciones by rememberSaveable { mutableStateOf(false) }
    val alFotoLista by rememberUpdatedState(onFotoLista)
    val alError by rememberUpdatedState(onError)

    val abrirCamara = rememberAbrirCamara(prefijo = prefijo, onFotoLista = onFotoLista, onError = onError)

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            alcance.launch {
                val nombre = withContext(Dispatchers.IO) { AlmacenFotos.importar(contexto, uri, prefijo) }
                if (nombre != null) {
                    alFotoLista(nombre)
                } else {
                    alError("No se pudo cargar la foto elegida. Intenta con otra.")
                }
            }
        }
    }

    if (mostrarOpciones) {
        AlertDialog(
            onDismissRequest = { mostrarOpciones = false },
            title = { Text(text = "Agregar foto") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            mostrarOpciones = false
                            abrirCamara()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "Tomar foto con la cámara") }
                    TextButton(
                        onClick = {
                            mostrarOpciones = false
                            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "Elegir de la galería") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarOpciones = false }) { Text(text = "Cancelar") }
            }
        )
    }

    return { mostrarOpciones = true }
}

/** Carga una foto guardada fuera del hilo principal. Devuelve null mientras carga o si no existe. */
@Composable
private fun rememberFoto(nombre: String?, ladoMaximo: Int): ImageBitmap? {
    val contexto = LocalContext.current
    val foto by produceState<ImageBitmap?>(initialValue = null, nombre, ladoMaximo) {
        value = if (nombre.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                AlmacenFotos.cargarBitmap(contexto, nombre, ladoMaximo)?.asImageBitmap()
            }
        }
    }
    return foto
}

/** Foto rectangular de un pedido; si no hay foto no dibuja nada. */
@Composable
fun FotoPedido(
    nombreArchivo: String?,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var ampliada by remember { mutableStateOf(false) }
    val foto = rememberFoto(nombreArchivo, ladoMaximo = 1024) ?: return
    Image(
        bitmap = foto,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClickLabel = "Ampliar foto") { ampliada = true }
    )
    if (ampliada) {
        DialogoFoto(nombreArchivo = nombreArchivo, descripcion = contentDescription) { ampliada = false }
    }
}

/** Foto a pantalla casi completa. Se cierra tocando fuera de la foto, la foto o el botón Atrás. */
@Composable
fun DialogoFoto(
    nombreArchivo: String?,
    descripcion: String,
    onCerrar: () -> Unit
) {
    val foto = rememberFoto(nombreArchivo, ladoMaximo = 2048)
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClickLabel = "Cerrar foto", onClick = onCerrar)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (foto != null) {
                Image(
                    bitmap = foto,
                    contentDescription = descripcion,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
    }
}

/** Miniatura cuadrada de un plato (listas del menú). Reserva su espacio mientras la foto carga. */
@Composable
fun FotoMiniatura(
    nombreArchivo: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 72.dp
) {
    var ampliada by remember { mutableStateOf(false) }
    val foto = rememberFoto(nombreArchivo, ladoMaximo = 256)
    Surface(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = foto != null, onClickLabel = "Ampliar foto") { ampliada = true },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (foto != null) {
            Image(
                bitmap = foto,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(tamano)
            )
        }
    }
    if (ampliada) {
        DialogoFoto(nombreArchivo = nombreArchivo, descripcion = contentDescription) { ampliada = false }
    }
}

/** Avatar circular: foto de perfil o, si no hay, la inicial del nombre. */
@Composable
fun AvatarPerfil(
    nombreArchivo: String?,
    nombre: String,
    modifier: Modifier = Modifier,
    tamano: Dp = 72.dp
) {
    val foto = rememberFoto(nombreArchivo, ladoMaximo = 256)
    Surface(
        modifier = modifier.size(tamano),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        if (foto != null) {
            Image(
                bitmap = foto,
                contentDescription = "Foto de perfil de $nombre",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(tamano)
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = nombre.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

/** Tarjeta de perfil de los paneles principales (admin y cliente): avatar, nombre y botón para tomar la foto. */
@Composable
fun TarjetaPerfil(
    nombre: String,
    detalle: String,
    fotoPerfil: String?,
    onTomarFoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaJoJo(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarPerfil(nombreArchivo = fotoPerfil, nombre = nombre)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = nombre.ifBlank { "Mi perfil" },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onTomarFoto) {
                    Text(text = if (fotoPerfil == null) "Agregar foto de perfil" else "Cambiar foto de perfil")
                }
            }
        }
    }
}
