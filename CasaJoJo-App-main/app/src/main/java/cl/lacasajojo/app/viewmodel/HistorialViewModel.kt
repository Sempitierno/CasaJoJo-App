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

data class HistorialUi(
    val cargando: Boolean = true,
    val pedidos: List<PedidoConDetalle> = emptyList()
)

/**
 * Historial de pedidos del cliente con la sesión iniciada. La consulta filtra por el id del
 * cliente, por lo que nunca se muestran pedidos de otros usuarios.
 */
class HistorialViewModel(
    auth: AuthRepository,
    pedidos: PedidoRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val ui: StateFlow<HistorialUi> = auth.usuarioActual
        .flatMapLatest { usuario ->
            if (usuario == null) {
                flowOf(emptyList<PedidoConDetalle>())
            } else {
                pedidos.observarDeCliente(usuario.id)
            }
        }
        .map { lista -> HistorialUi(cargando = false, pedidos = lista) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUi())
}
