package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.data.repository.MenuRepository
import cl.lacasajojo.app.data.repository.PedidoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AdminHomeUi(
    val cargando: Boolean = true,
    val totalProductos: Int = 0,
    val publicados: Int = 0,
    val recibidos: Int = 0,
    val enPreparacion: Int = 0,
    val listos: Int = 0
)

/** Resumen del panel del administrador (calculado con datos reales de Room). */
class AdminHomeViewModel(
    menu: MenuRepository,
    pedidos: PedidoRepository
) : ViewModel() {

    val ui: StateFlow<AdminHomeUi> = combine(
        menu.observarTodos(),
        pedidos.observarTodos()
    ) { productos, listaPedidos ->
        AdminHomeUi(
            cargando = false,
            totalProductos = productos.size,
            publicados = productos.count { it.publicado },
            recibidos = listaPedidos.count { it.pedido.estado == EstadoPedido.RECIBIDO },
            enPreparacion = listaPedidos.count { it.pedido.estado == EstadoPedido.EN_PREPARACION },
            listos = listaPedidos.count { it.pedido.estado == EstadoPedido.LISTO_RETIRO }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AdminHomeUi())
}
