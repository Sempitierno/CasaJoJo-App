package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.ProductoEntity
import cl.lacasajojo.app.data.repository.MenuRepository
import cl.lacasajojo.app.util.agruparPorCategoria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ClienteMenuUi(
    val cargando: Boolean = true,
    val grupos: List<Pair<CategoriaProducto, List<ProductoEntity>>> = emptyList()
) {
    val hayMenu: Boolean
        get() = grupos.isNotEmpty()

    /** Hay al menos un plato de fondo disponible, requisito mínimo para poder pedir. */
    val sePuedePedir: Boolean
        get() = grupos.any { (categoria, productos) ->
            categoria == CategoriaProducto.PLATO_FONDO && productos.any { it.disponible }
        }
}

/** Menú publicado por el administrador, agrupado por categoría. */
class ClienteMenuViewModel(menu: MenuRepository) : ViewModel() {

    val ui: StateFlow<ClienteMenuUi> = menu.observarPublicados()
        .map { productos -> ClienteMenuUi(cargando = false, grupos = productos.agruparPorCategoria()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClienteMenuUi())
}
