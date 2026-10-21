package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.PedidoConDetalle
import cl.lacasajojo.app.data.repository.AuthRepository
import cl.lacasajojo.app.data.repository.PedidoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DetallePedidoUi(
    val cargando: Boolean = true,
    /** null cuando el pedido no existe o no pertenece al cliente con la sesión iniciada. */
    val pedido: PedidoConDetalle? = null
)

/** Detalle de un pedido (también se usa en la pantalla de confirmación). Solo muestra pedidos propios. */
class DetallePedidoViewModel(
    auth: AuthRepository,
    pedidos: PedidoRepository,
    pedidoId: String
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val ui: StateFlow<DetallePedidoUi> = auth.usuarioActual
        .flatMapLatest { usuario ->
            if (usuario == null) {
                flowOf<PedidoConDetalle?>(null)
            } else {
                pedidos.observarUnoDeCliente(pedidoId, usuario.id)
            }
        }
        .map { pedido -> DetallePedidoUi(cargando = false, pedido = pedido) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetallePedidoUi())
}
