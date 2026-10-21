package cl.lacasajojo.app.ui.screens.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.ui.components.BotonPrincipal
import cl.lacasajojo.app.ui.components.CampoTexto
import cl.lacasajojo.app.ui.components.EncabezadoCategoria
import cl.lacasajojo.app.ui.components.EstadoVacio
import cl.lacasajojo.app.ui.components.FilaDato
import cl.lacasajojo.app.ui.components.FilaOpcion
import cl.lacasajojo.app.ui.components.LineaResumen
import cl.lacasajojo.app.ui.components.LineaTotal
import cl.lacasajojo.app.ui.components.MensajeError
import cl.lacasajojo.app.ui.components.PantallaBase
import cl.lacasajojo.app.ui.components.PantallaCargando
import cl.lacasajojo.app.ui.components.SelectorHoraRetiro
import cl.lacasajojo.app.ui.components.TarjetaJoJo
import cl.lacasajojo.app.util.Formato
import cl.lacasajojo.app.util.Validaciones
import cl.lacasajojo.app.viewmodel.NuevoPedidoUi
import cl.lacasajojo.app.viewmodel.NuevoPedidoViewModel

/**
 * Creación de un pedido: un producto por categoría, hora de retiro (selector nativo), especificaciones,
 * resumen con el total calculado automáticamente y diálogo de confirmación final.
 */
@Composable
fun NuevoPedidoScreen(
    viewModel: NuevoPedidoViewModel,
    onVolver: () -> Unit,
    onPedidoCreado: (String) -> Unit
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    // Cuando el pedido queda guardado, se navega a la pantalla de confirmación.
    LaunchedEffect(ui.pedidoCreadoId) {
        ui.pedidoCreadoId?.let { id -> onPedidoCreado(id) }
    }

    PantallaBase(titulo = "Nuevo pedido", onAtras = onVolver) { innerPadding ->
        when {
            ui.cargando -> PantallaCargando(modifier = Modifier.padding(innerPadding))
            !ui.hayMenu -> EstadoVacio(
                modifier = Modifier.padding(innerPadding),
                titulo = "Aún no hay menú publicado",
                mensaje = "El administrador todavía no publica el menú de hoy, " +
                    "por eso aún no se puede armar un pedido. Vuelve a intentarlo más tarde.",
                icono = Icons.Filled.Info
            )
            else -> FormularioPedido(ui = ui, viewModel = viewModel, innerPadding = innerPadding)
        }
    }

    if (ui.mostrarResumen) {
        DialogoResumenPedido(
            ui = ui,
            onConfirmar = viewModel::confirmar,
            onVolver = viewModel::cancelarConfirmacion
        )
    }
}

@Composable
private fun FormularioPedido(
    ui: NuevoPedidoUi,
    viewModel: NuevoPedidoViewModel,
    innerPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .imePadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item(key = "intro") {
            Text(
                text = "Arma tu almuerzo eligiendo un producto por categoría. " +
                    "El plato de fondo es obligatorio; lo demás es opcional.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        for (categoria in CategoriaProducto.entries) {
            item(key = "cab_${categoria.name}") {
                EncabezadoCategoria(
                    texto = if (categoria.obligatoria) {
                        "${categoria.etiqueta} · obligatorio"
                    } else {
                        "${categoria.etiqueta} · opcional"
                    }
                )
            }

            val productos = ui.menu[categoria].orEmpty()
            if (productos.isEmpty()) {
                item(key = "vacio_${categoria.name}") {
                    Text(
                        text = "No hay opciones publicadas en esta categoría por ahora.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                if (!categoria.obligatoria) {
                    item(key = "ninguno_${categoria.name}") {
                        FilaOpcion(
                            titulo = categoria.textoNinguno,
                            seleccionado = ui.seleccion[categoria] == null,
                            onClick = { viewModel.seleccionar(categoria, null) }
                        )
                    }
                }
                items(productos, key = { producto -> "prod_${producto.id}" }) { producto ->
                    FilaOpcion(
                        titulo = producto.nombre,
                        descripcion = producto.descripcion.ifBlank { null },
                        precio = Formato.precio(producto.precio),
                        seleccionado = ui.seleccion[categoria] == producto.id,
                        habilitado = producto.disponible,
                        aviso = if (producto.disponible) null else "No disponible",
                        onClick = { viewModel.seleccionar(categoria, producto.id) }
                    )
                }
            }
        }

        item(key = "cab_retiro") {
            EncabezadoCategoria(texto = "Hora de retiro")
        }
        item(key = "retiro") {
            SelectorHoraRetiro(
                hora = ui.horaRetiro,
                error = ui.errorHora,
                onHoraSeleccionada = viewModel::cambiarHora
            )
        }

        item(key = "cab_notas") {
            EncabezadoCategoria(texto = "Especificaciones o restricciones alimentarias")
        }
        item(key = "notas") {
            CampoTexto(
                valor = ui.especificaciones,
                onValorChange = viewModel::cambiarEspecificaciones,
                etiqueta = "Ej.: sin gluten, sin cebolla, alergia a frutos secos",
                ayuda = "${ui.especificaciones.length}/${Validaciones.MAX_ESPECIFICACIONES}",
                unaSolaLinea = false,
                minLineas = 3,
                accionIme = ImeAction.Default
            )
        }

        item(key = "resumen") {
            ResumenPedido(ui = ui)
        }

        item(key = "confirmar") {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ui.errorGeneral?.let { mensaje -> MensajeError(texto = mensaje) }
                BotonPrincipal(
                    texto = "Revisar y confirmar pedido",
                    onClick = viewModel::solicitarConfirmacion,
                    habilitado = ui.puedeConfirmar
                )
            }
        }
    }
}

/** Resumen en vivo: productos elegidos y total calculado automáticamente. */
@Composable
private fun ResumenPedido(ui: NuevoPedidoUi) {
    TarjetaJoJo(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Resumen del pedido", style = MaterialTheme.typography.titleMedium)

            if (ui.productosSeleccionados.isEmpty()) {
                Text(
                    text = "Aún no has elegido productos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ui.productosSeleccionados.forEach { producto ->
                    LineaResumen(
                        categoria = producto.categoria.etiqueta,
                        nombre = producto.nombre,
                        precio = Formato.precio(producto.precio)
                    )
                }
            }

            HorizontalDivider()
            LineaTotal(total = Formato.precio(ui.total))

            ui.horaRetiro?.let { hora ->
                FilaDato(etiqueta = "Retiro", valor = "${Formato.horaLocal(hora)} hrs")
            }

            if (ui.pendientes.isNotEmpty()) {
                Text(
                    text = "Para confirmar: " + ui.pendientes.joinToString(" · ").lowercase() + ".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/** Resumen final antes de enviar el pedido a la cocina. */
@Composable
private fun DialogoResumenPedido(
    ui: NuevoPedidoUi,
    onConfirmar: () -> Unit,
    onVolver: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!ui.enviando) onVolver() },
        title = { Text(text = "Confirmar pedido") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ui.productosSeleccionados.forEach { producto ->
                    LineaResumen(
                        categoria = producto.categoria.etiqueta,
                        nombre = producto.nombre,
                        precio = Formato.precio(producto.precio)
                    )
                }
                HorizontalDivider()
                LineaTotal(total = Formato.precio(ui.total))
                ui.horaRetiro?.let { hora ->
                    FilaDato(etiqueta = "Retiro", valor = "${Formato.horaLocal(hora)} hrs")
                }
                if (ui.especificaciones.isNotBlank()) {
                    FilaDato(etiqueta = "Notas", valor = ui.especificaciones)
                }
                Text(
                    text = "Revisa que todo esté correcto antes de enviar el pedido a la cocina.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirmar, enabled = !ui.enviando) {
                Text(text = if (ui.enviando) "Enviando…" else "Confirmar pedido")
            }
        },
        dismissButton = {
            TextButton(onClick = onVolver, enabled = !ui.enviando) {
                Text(text = "Volver")
            }
        }
    )
}
