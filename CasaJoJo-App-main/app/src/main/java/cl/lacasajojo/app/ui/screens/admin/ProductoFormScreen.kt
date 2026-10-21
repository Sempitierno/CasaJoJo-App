package cl.lacasajojo.app.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.ui.components.BotonPrincipal
import cl.lacasajojo.app.ui.components.CampoTexto
import cl.lacasajojo.app.ui.components.EstadoVacio
import cl.lacasajojo.app.ui.components.FilaInterruptor
import cl.lacasajojo.app.ui.components.FilaOpcion
import cl.lacasajojo.app.ui.components.MensajeError
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.ui.components.PantallaCargando
import cl.lacasajojo.app.util.Validaciones
import cl.lacasajojo.app.viewmodel.ProductoFormViewModel

/** Crear o editar un producto del menú. Todos los cambios se guardan en Room. */
@Composable
fun ProductoFormScreen(
    viewModel: ProductoFormViewModel,
    onVolver: () -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    LaunchedEffect(ui.guardado) {
        if (ui.guardado) {
            Toast.makeText(contexto, "Producto guardado", Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }

    PantallaBase(
        titulo = if (ui.esNuevo) "Nuevo producto" else "Editar producto",
        onAtras = onVolver
    ) { innerPadding ->
        when {
            ui.cargando -> PantallaCargando(modifier = Modifier.padding(innerPadding))
            ui.noEncontrado -> EstadoVacio(
                modifier = Modifier.padding(innerPadding),
                titulo = "Producto no encontrado",
                mensaje = "Es posible que haya sido eliminado del menú.",
                acciones = {
                    OutlinedButton(onClick = onVolver) {
                        Text(text = "Volver al menú")
                    }
                }
            )
            else -> Column(
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
                    etiqueta = "Nombre del plato o producto",
                    error = ui.errores.nombre
                )

                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                CategoriaProducto.entries.forEach { categoria ->
                    FilaOpcion(
                        titulo = categoria.etiqueta,
                        seleccionado = ui.categoria == categoria,
                        onClick = { viewModel.onCategoriaChange(categoria) }
                    )
                }
                ui.errores.categoria?.let { mensaje ->
                    Text(
                        text = mensaje,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                CampoTexto(
                    valor = ui.descripcion,
                    onValorChange = viewModel::onDescripcionChange,
                    etiqueta = "Descripción (opcional)",
                    error = ui.errores.descripcion,
                    ayuda = "${ui.descripcion.length}/${Validaciones.MAX_DESCRIPCION}",
                    unaSolaLinea = false,
                    minLineas = 2,
                    accionIme = ImeAction.Default
                )
                CampoTexto(
                    valor = ui.precio,
                    onValorChange = viewModel::onPrecioChange,
                    etiqueta = "Precio en pesos chilenos (solo números)",
                    error = ui.errores.precio,
                    ayuda = "Ejemplo: 3500 se mostrará como \$3.500",
                    teclado = KeyboardType.Number,
                    accionIme = ImeAction.Done
                )

                FilaInterruptor(
                    titulo = "Disponible",
                    descripcion = "Si se desactiva, el cliente ve el producto pero no puede elegirlo.",
                    marcado = ui.disponible,
                    onCambio = viewModel::onDisponibleChange
                )
                FilaInterruptor(
                    titulo = "Publicado en el menú del día",
                    descripcion = "Los clientes solo ven los productos publicados.",
                    marcado = ui.publicado,
                    onCambio = viewModel::onPublicadoChange
                )

                ui.mensajeError?.let { mensaje -> MensajeError(texto = mensaje) }

                BotonPrincipal(
                    texto = "Guardar producto",
                    onClick = viewModel::guardar,
                    cargando = ui.guardando,
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedButton(
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cancelar")
                }
            }
        }
    }
}
