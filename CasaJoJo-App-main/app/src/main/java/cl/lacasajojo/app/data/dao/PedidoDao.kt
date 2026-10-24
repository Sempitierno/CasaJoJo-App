package cl.lacasajojo.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cl.lacasajojo.app.data.entity.DetallePedidoEntity
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.data.entity.FotoCliente
import cl.lacasajojo.app.data.entity.PedidoConDetalle
import cl.lacasajojo.app.data.entity.PedidoEntity
import kotlinx.coroutines.flow.Flow

/** DAO de pedidos. Es una clase abstracta para poder definir la transacción [insertarPedidoCompleto]. */
@Dao
abstract class PedidoDao {

    /** Devuelve -1 (sin insertar) si ya existe un pedido con el mismo id: evita duplicados. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertarPedido(pedido: PedidoEntity): Long

    @Insert
    abstract suspend fun insertarDetalles(detalles: List<DetallePedidoEntity>)

    /** Guarda el pedido y sus líneas en una sola transacción. Devuelve false si el pedido ya existía. */
    @Transaction
    open suspend fun insertarPedidoCompleto(
        pedido: PedidoEntity,
        detalles: List<DetallePedidoEntity>
    ): Boolean {
        val fila = insertarPedido(pedido)
        if (fila == -1L) return false
        insertarDetalles(detalles)
        return true
    }

    /** Todos los pedidos (vista del administrador). */
    @Transaction
    @Query("SELECT * FROM pedidos ORDER BY creadoEn DESC")
    abstract fun observarTodos(): Flow<List<PedidoConDetalle>>

    /** Fotos de perfil de los usuarios que tienen una (el administrador las muestra junto a cada comanda). */
    @Query("SELECT id AS clienteId, fotoPerfil FROM usuarios WHERE fotoPerfil IS NOT NULL")
    abstract fun observarFotosClientes(): Flow<List<FotoCliente>>

    /** Solo los pedidos del cliente indicado, del más reciente al más antiguo. */
    @Transaction
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId ORDER BY creadoEn DESC")
    abstract fun observarDeCliente(clienteId: Long): Flow<List<PedidoConDetalle>>

    /** Un pedido, solo si pertenece al cliente indicado (un cliente no puede ver pedidos ajenos). */
    @Transaction
    @Query("SELECT * FROM pedidos WHERE id = :pedidoId AND clienteId = :clienteId LIMIT 1")
    abstract fun observarUnoDeCliente(pedidoId: String, clienteId: Long): Flow<PedidoConDetalle?>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE id = :pedidoId LIMIT 1")
    abstract suspend fun obtenerPorId(pedidoId: String): PedidoConDetalle?

    /** Pedidos del cliente cuyo estado cambió y aún no se le ha avisado con una notificación. */
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId AND estado <> estadoNotificado")
    abstract fun observarPendientesDeNotificar(clienteId: Long): Flow<List<PedidoEntity>>

    @Query("UPDATE pedidos SET estado = :estado, actualizadoEn = :ahora WHERE id = :pedidoId")
    abstract suspend fun actualizarEstado(pedidoId: String, estado: EstadoPedido, ahora: Long): Int

    @Query("UPDATE pedidos SET estadoNotificado = :estado WHERE id = :pedidoId")
    abstract suspend fun marcarEstadoNotificado(pedidoId: String, estado: EstadoPedido): Int

    /** Guarda (o quita, con null) la foto del pedido. Devuelve las filas modificadas. */
    @Query("UPDATE pedidos SET fotoPedido = :foto, actualizadoEn = :ahora WHERE id = :pedidoId")
    abstract suspend fun actualizarFoto(pedidoId: String, foto: String?, ahora: Long): Int

    @Update
    abstract suspend fun actualizar(pedido: PedidoEntity)

    @Delete
    abstract suspend fun eliminar(pedido: PedidoEntity)
}
