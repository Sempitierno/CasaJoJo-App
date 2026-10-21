package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.data.entity.PedidoConDetalle
import cl.lacasajojo.app.data.entity.PedidoEntity
import cl.lacasajojo.app.data.repository.PedidoRepository
import cl.lacasajojo.app.util.OrdenPedidos
import cl.lacasajojo.app.util.codigo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PedidosAdminUi(
    val cargando: Boolean = true,
    /** Filtro activo; null significa "Todos". */
    val filtro: EstadoPedido? = null,
    /** Pedidos ya filtrados y ordenados para cocina. */
    val pedidos: List<PedidoConDetalle> = emptyList(),
    val conteos: Map<EstadoPedido, Int> = emptyMap(),
    val total: Int = 0
)

/** Comandas: lista todos los pedidos, permite filtrarlos por estado y cambiar su estado. */
class AdminPedidosViewModel(private val pedidosRepo: PedidoRepository) : ViewModel() {

    private val filtroSeleccionado = MutableStateFlow<EstadoPedido?>(null)
    private val canalMensajes = Channel<String>(Channel.BUFFERED)

    val mensajes: Flow<String> = canalMensajes.receiveAsFlow()

    val ui: StateFlow<PedidosAdminUi> = combine(
        pedidosRepo.observarTodos(),
        filtroSeleccionado
    ) { pedidos, filtroActual ->
        val ordenados = OrdenPedidos.paraCocina(pedidos)
        PedidosAdminUi(
            cargando = false,
            filtro = filtroActual,
            pedidos = if (filtroActual == null) {
                ordenados
            } else {
                ordenados.filter { it.pedido.estado == filtroActual }
            },
            conteos = EstadoPedido.entries.associateWith { estado ->
                pedidos.count { it.pedido.estado == estado }
            },
            total = pedidos.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PedidosAdminUi())

    fun filtrar(estado: EstadoPedido?) {
        filtroSeleccionado.value = estado
    }

    fun cambiarEstado(pedido: PedidoEntity, nuevoEstado: EstadoPedido) {
        if (pedido.estado == nuevoEstado) return
        viewModelScope.launch {
            val guardado = pedidosRepo.cambiarEstado(pedido.id, nuevoEstado)
            canalMensajes.trySend(
                if (guardado) {
                    "Pedido ${pedido.codigo}: ${nuevoEstado.etiqueta}. " +
                        "El cliente verá el cambio y recibirá su notificación al iniciar sesión."
                } else {
                    "No se pudo cambiar el estado. Intenta nuevamente."
                }
            )
        }
    }
}
