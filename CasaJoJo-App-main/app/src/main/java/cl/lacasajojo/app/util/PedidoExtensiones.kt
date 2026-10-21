package cl.lacasajojo.app.util

import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.DetallePedidoEntity
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.data.entity.PedidoConDetalle
import cl.lacasajojo.app.data.entity.PedidoEntity

/** Código corto y legible del pedido, derivado de su identificador único: "#A1B2C3D4". */
val PedidoEntity.codigo: String
    get() = "#" + id.take(8).uppercase()

/** Líneas del pedido en el orden de las categorías del menú. */
fun PedidoConDetalle.detallesOrdenados(): List<DetallePedidoEntity> =
    detalles.sortedBy { it.categoria.ordinal }

fun PedidoConDetalle.detalleDe(categoria: CategoriaProducto): DetallePedidoEntity? =
    detalles.firstOrNull { it.categoria == categoria }

/** Nombres de los productos separados por coma, para mostrar un resumen corto. */
fun PedidoConDetalle.resumenProductos(): String =
    detallesOrdenados().joinToString(", ") { it.nombreProducto }

/** Orden de las comandas para la cocina. */
object OrdenPedidos {

    /**
     * Primero lo pendiente (Recibido y En preparación) por hora de retiro, el retiro más próximo primero;
     * después los pedidos ya listos, los más recientes primero.
     */
    fun paraCocina(pedidos: List<PedidoConDetalle>): List<PedidoConDetalle> {
        val pendientes = pedidos.filter { it.pedido.estado != EstadoPedido.LISTO_RETIRO }
        val listos = pedidos.filter { it.pedido.estado == EstadoPedido.LISTO_RETIRO }
        val pendientesOrdenados = pendientes.sortedWith(
            compareBy<PedidoConDetalle>({ it.pedido.horaRetiro }, { it.pedido.creadoEn })
        )
        val listosOrdenados = listos.sortedByDescending { it.pedido.creadoEn }
        return pendientesOrdenados + listosOrdenados
    }
}
