package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.ProductoEntity
import cl.lacasajojo.app.data.repository.MenuRepository
import cl.lacasajojo.app.util.agruparPorCategoria
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminMenuUi(
    val cargando: Boolean = true,
    val grupos: List<Pair<CategoriaProducto, List<ProductoEntity>>> = emptyList()
) {
    val hayProductos: Boolean
        get() = grupos.isNotEmpty()
}

/** Lista y administra el menú del día: publicar, marcar disponibilidad, eliminar. */
class AdminMenuViewModel(private val menu: MenuRepository) : ViewModel() {

    private val canalMensajes = Channel<String>(Channel.BUFFERED)

    /** Mensajes de una sola vez para mostrar en un Snackbar. */
    val mensajes: Flow<String> = canalMensajes.receiveAsFlow()

    val ui: StateFlow<AdminMenuUi> = menu.observarTodos()
        .map { productos -> AdminMenuUi(cargando = false, grupos = productos.agruparPorCategoria()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminMenuUi())

    fun cambiarPublicado(producto: ProductoEntity, publicado: Boolean) {
        viewModelScope.launch {
            if (!menu.cambiarPublicado(producto.id, publicado)) {
                canalMensajes.trySend("No se pudo actualizar la publicación. Intenta nuevamente.")
            }
        }
    }

    fun cambiarDisponible(producto: ProductoEntity, disponible: Boolean) {
        viewModelScope.launch {
            if (!menu.cambiarDisponible(producto.id, disponible)) {
                canalMensajes.trySend("No se pudo actualizar la disponibilidad. Intenta nuevamente.")
            }
        }
    }

    fun eliminar(producto: ProductoEntity) {
        viewModelScope.launch {
            val eliminado = menu.eliminar(producto.id)
            canalMensajes.trySend(
                if (eliminado) {
                    "«${producto.nombre}» fue eliminado del menú."
                } else {
                    "No se pudo eliminar el producto. Intenta nuevamente."
                }
            )
        }
    }

    fun cargarDemostracion() {
        viewModelScope.launch {
            val agregados = menu.cargarMenuDemostracion()
            val mensaje = when {
                agregados == null -> "No se pudo cargar el menú de ejemplo."
                agregados == 0 -> "Ya existen productos en el menú."
                else -> "Se cargaron $agregados productos de ejemplo (datos ficticios)."
            }
            canalMensajes.trySend(mensaje)
        }
    }
}
