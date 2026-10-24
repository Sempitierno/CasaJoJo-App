package cl.lacasajojo.app.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.data.entity.ProductoEntity
import cl.lacasajojo.app.ui.components.DialogoConfirmacion
import cl.lacasajojo.app.ui.components.EncabezadoCategoria
import cl.lacasajojo.app.ui.components.EstadoVacio
import cl.lacasajojo.app.ui.components.FotoMiniatura
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.ui.components.PantallaCargando
import cl.lacasajojo.app.ui.components.TarjetaJoJo
import cl.lacasajojo.app.util.Formato
import cl.lacasajojo.app.viewmodel.AdminMenuViewModel

/**
 * Administración del menú del día: lista por categoría, con interruptores de publicación y
 * disponibilidad, y acciones para crear, editar y eliminar productos.
 */
@Composable
fun AdminMenuScreen(
    viewModel: AdminMenuViewModel,
    onVolver: () -> Unit,
    onNuevoProducto: () -> Unit,
    onEditarProducto: (Long) -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var productoAEliminar by remember { mutableStateOf<ProductoEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { mensaje -> snackbarHostState.showSnackbar(mensaje) }
    }

    PantallaBase(
        titulo = "Menú del día",
        onAtras = onVolver,
        snackbarHostState = snackbarHostState,
        botonFlotante = {
            ExtendedFloatingActionButton(
                onClick = onNuevoProducto,
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                text = { Text(text = "Nuevo producto") }
            )
        }
    ) { innerPadding ->
        when {
            ui.cargando -> PantallaCargando(modifier = Modifier.padding(innerPadding))
            !ui.hayProductos -> EstadoVacio(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                titulo = "Aún no hay productos en el menú",
                mensaje = "Crea los platos, acompañamientos, ensaladas, postres y bebidas del día. " +
                    "Los productos publicados aparecerán en el menú de los clientes.",
                icono = Icons.Filled.Menu,
                acciones = {
                    Button(onClick = onNuevoProducto) {
                        Text(text = "Crear primer producto")
                    }
                    OutlinedButton(onClick = viewModel::cargarDemostracion) {
                        Text(text = "Cargar menú de ejemplo")
                    }
                    Text(
                        text = "El menú de ejemplo usa datos ficticios y se puede editar o eliminar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ui.grupos.forEach { (categoria, productos) ->
                    item(key = "cab_${categoria.name}") {
                        EncabezadoCategoria(texto = categoria.etiqueta)
                    }
                    items(productos, key = { producto -> "prod_${producto.id}" }) { producto ->
                        TarjetaProductoAdmin(
                            producto = producto,
                            onPublicado = { valor -> viewModel.cambiarPublicado(producto, valor) },
                            onDisponible = { valor -> viewModel.cambiarDisponible(producto, valor) },
                            onEditar = { onEditarProducto(producto.id) },
                            onEliminar = { productoAEliminar = producto }
                        )
                    }
                }
            }
        }
    }

    productoAEliminar?.let { producto ->
        DialogoConfirmacion(
            titulo = "Eliminar producto",
            mensaje = "¿Eliminar «${producto.nombre}» del menú? " +
                "Los pedidos que ya lo incluyen no se modificarán.",
            textoConfirmar = "Eliminar",
            onConfirmar = {
                viewModel.eliminar(producto)
                productoAEliminar = null
            },
            onCancelar = { productoAEliminar = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaProductoAdmin(
    producto: ProductoEntity,
    onPublicado: (Boolean) -> Unit,
    onDisponible: (Boolean) -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    TarjetaJoJo(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (producto.fotoProducto != null) {
                    FotoMiniatura(
                        nombreArchivo = producto.fotoProducto,
                        contentDescription = "Foto de ${producto.nombre}",
                        tamano = 48.dp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar ${producto.nombre}"
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar ${producto.nombre}"
                    )
                }
            }
            if (producto.descripcion.isNotBlank()) {
                Text(
                    text = producto.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Row(
                modifier = Modifier.padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = Formato.precio(producto.precio),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = producto.publicado,
                    onClick = { onPublicado(!producto.publicado) },
                    label = { Text(text = if (producto.publicado) "Publicado" else "Oculto") }
                )
                FilterChip(
                    selected = producto.disponible,
                    onClick = { onDisponible(!producto.disponible) },
                    label = { Text(text = if (producto.disponible) "Disponible" else "Agotado") }
                )
            }
        }
    }
}
