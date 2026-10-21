package cl.lacasajojo.app.data.repository

import cl.lacasajojo.app.data.dao.PedidoDao
import cl.lacasajojo.app.data.dao.ProductoDao
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.DetallePedidoEntity
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.data.entity.PedidoConDetalle
import cl.lacasajojo.app.data.entity.PedidoEntity
import cl.lacasajojo.app.util.Validaciones
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/** Creación y consulta de pedidos (comandas) y cambios de estado. */
class PedidoRepository(
    private val pedidoDao: PedidoDao,
    private val productoDao: ProductoDao,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) {

    /** Todos los pedidos (administrador). */
    fun observarTodos(): Flow<List<PedidoConDetalle>> = pedidoDao.observarTodos()

    /** Solo los pedidos del cliente indicado. */
    fun observarDeCliente(clienteId: Long): Flow<List<PedidoConDetalle>> =
        pedidoDao.observarDeCliente(clienteId)

    /** Un pedido, únicamente si pertenece al cliente indicado. */
    fun observarUnoDeCliente(pedidoId: String, clienteId: Long): Flow<PedidoConDetalle?> =
        pedidoDao.observarUnoDeCliente(pedidoId, clienteId)

    suspend fun obtener(pedidoId: String): PedidoConDetalle? = pedidoDao.obtenerPorId(pedidoId)

    fun observarPendientesDeNotificar(clienteId: Long): Flow<List<PedidoEntity>> =
        pedidoDao.observarPendientesDeNotificar(clienteId)

    suspend fun marcarNotificado(pedidoId: String, estado: EstadoPedido) {
        pedidoDao.marcarEstadoNotificado(pedidoId, estado)
    }

    /** Cambia el estado de un pedido (administrador). Devuelve true si se guardó. */
    suspend fun cambiarEstado(pedidoId: String, nuevoEstado: EstadoPedido): Boolean {
        return try {
            pedidoDao.actualizarEstado(pedidoId, nuevoEstado, reloj()) > 0
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Valida y guarda un pedido nuevo en estado "Recibido".
     *
     * - Vuelve a leer los productos desde la base de datos: usa los precios vigentes y rechaza productos
     *   que ya no estén publicados o disponibles.
     * - Guarda nombre y precio de cada producto en el detalle (copia histórica).
     * - El total se calcula sumando los precios.
     * - Si ya existe un pedido con el mismo id (pulsación repetida), no lo duplica.
     */
    suspend fun crearPedido(solicitud: SolicitudPedido): PedidoResultado {
        val ahora = reloj()
        val errores = Validaciones.validarPedido(
            hayPlatoDeFondo = solicitud.seleccion.containsKey(CategoriaProducto.PLATO_FONDO),
            horaRetiroMillis = solicitud.horaRetiroMillis,
            ahoraMillis = ahora,
            especificaciones = solicitud.especificaciones
        )
        if (errores.hayErrores) {
            return PedidoResultado.Invalido(errores)
        }

        try {
            val productos = productoDao.obtenerPorIds(solicitud.seleccion.values.toList())
            val productosPorId = productos.associateBy { it.id }

            val detalles = mutableListOf<DetallePedidoEntity>()
            for ((categoria, productoId) in solicitud.seleccion) {
                val producto = productosPorId[productoId]
                if (producto == null ||
                    producto.categoria != categoria ||
                    !producto.publicado ||
                    !producto.disponible
                ) {
                    val nombre = producto?.nombre ?: "seleccionado"
                    return PedidoResultado.Rechazado(
                        "El producto $nombre ya no está disponible. Revisa tu selección e inténtalo de nuevo."
                    )
                }
                detalles.add(
                    DetallePedidoEntity(
                        pedidoId = solicitud.idPedido,
                        categoria = categoria,
                        productoId = producto.id,
                        nombreProducto = producto.nombre,
                        precioUnitario = producto.precio
                    )
                )
            }

            val detallesOrdenados = detalles.sortedBy { it.categoria.ordinal }
            val total = detallesOrdenados.sumOf { it.precioUnitario }
            val pedido = PedidoEntity(
                id = solicitud.idPedido,
                clienteId = solicitud.cliente.id,
                clienteNombre = solicitud.cliente.nombreCompleto,
                clienteCorreo = solicitud.cliente.correo,
                creadoEn = ahora,
                horaRetiro = solicitud.horaRetiroMillis,
                especificaciones = solicitud.especificaciones.trim(),
                total = total,
                estado = EstadoPedido.RECIBIDO,
                estadoNotificado = EstadoPedido.RECIBIDO,
                actualizadoEn = ahora
            )
            // Si el id ya existía (pulsación repetida) devuelve false y no se duplica; el resultado es el mismo.
            pedidoDao.insertarPedidoCompleto(pedido, detallesOrdenados)
            return PedidoResultado.Exito(pedido.id, total)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return PedidoResultado.Rechazado("No se pudo guardar el pedido. Intenta nuevamente.")
        }
    }
}
