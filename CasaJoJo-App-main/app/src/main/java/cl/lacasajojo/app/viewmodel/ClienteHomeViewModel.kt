package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.PedidoEntity
import cl.lacasajojo.app.data.repository.AuthRepository
import cl.lacasajojo.app.data.repository.PedidoRepository
import cl.lacasajojo.app.util.NotificadorPedidos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Panel del cliente. Además de la navegación, se encarga de las notificaciones locales:
 * cuando el administrador cambia el estado de un pedido, la fila queda con estado distinto al
 * "último estado notificado". Este ViewModel detecta esa diferencia en la base de datos del
 * cliente con la sesión iniciada, muestra la notificación y marca el estado como notificado.
 */
class ClienteHomeViewModel(
    private val auth: AuthRepository,
    private val pedidos: PedidoRepository,
    private val notificador: NotificadorPedidos
) : ViewModel() {

    private val reintentos = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val pendientes: Flow<List<PedidoEntity>> = auth.usuarioActual.flatMapLatest { usuario ->
        if (usuario == null) {
            flowOf(emptyList<PedidoEntity>())
        } else {
            pedidos.observarPendientesDeNotificar(usuario.id)
        }
    }

    init {
        viewModelScope.launch {
            combine(pendientes, reintentos) { lista, _ -> lista }
                .collect { lista -> notificarPendientes(lista) }
        }
    }

    /** Se llama después de responder la solicitud del permiso de notificaciones (Android 13+). */
    fun reintentarNotificaciones() {
        reintentos.update { it + 1 }
    }

    private suspend fun notificarPendientes(lista: List<PedidoEntity>) {
        if (lista.isEmpty() || !notificador.puedeNotificar()) return
        for (pedido in lista) {
            if (notificador.notificarCambioEstado(pedido)) {
                pedidos.marcarNotificado(pedido.id, pedido.estado)
            }
        }
    }
}
