package cl.lacasajojo.app.util

import cl.lacasajojo.app.data.entity.PedidoEntity

/** Abstracción de las notificaciones locales (permite reemplazarla por un doble de prueba). */
interface NotificadorPedidos {

    /** true si ahora se pueden mostrar notificaciones (permiso concedido y notificaciones activadas). */
    fun puedeNotificar(): Boolean

    /** Muestra la notificación del estado actual del pedido. Devuelve true si se mostró. */
    fun notificarCambioEstado(pedido: PedidoEntity): Boolean
}
