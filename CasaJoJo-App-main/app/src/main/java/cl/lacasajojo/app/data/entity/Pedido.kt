package cl.lacasajojo.app.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Pedido (comanda).
 *
 * - [id]: identificador único (UUID) generado al abrir la pantalla de pedido; sirve además para
 *   evitar pedidos duplicados si se pulsa varias veces "Confirmar" (la inserción ignora IDs repetidos).
 * - [clienteId]: referencia al cliente que creó el pedido. [clienteNombre] y [clienteCorreo] son una
 *   copia (snapshot) para que la comanda conserve los datos tal como se registraron.
 * - [horaRetiro] y [creadoEn]: instantes en milisegundos desde epoch.
 * - [estadoNotificado]: último estado del que se avisó al cliente mediante notificación local.
 */
@Entity(
    tableName = "pedidos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["clienteId"]), Index(value = ["estado"])]
)
data class PedidoEntity(
    @PrimaryKey val id: String,
    val clienteId: Long,
    val clienteNombre: String,
    val clienteCorreo: String,
    val creadoEn: Long,
    val horaRetiro: Long,
    val especificaciones: String,
    val total: Int,
    val estado: EstadoPedido,
    val estadoNotificado: EstadoPedido,
    val actualizadoEn: Long,
    /** Nombre del archivo de la foto del pedido tomada por el administrador (null = sin foto). */
    val fotoPedido: String? = null
)

/**
 * Línea de un pedido. Guarda el NOMBRE y el PRECIO del producto al momento de pedir, de modo que
 * cambiar o eliminar productos del menú después no altera los pedidos anteriores.
 * [productoId] es solo una referencia informativa (no es clave foránea a propósito).
 */
@Entity(
    tableName = "detalles_pedido",
    foreignKeys = [
        ForeignKey(
            entity = PedidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["pedidoId"])]
)
data class DetallePedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pedidoId: String,
    val categoria: CategoriaProducto,
    val productoId: Long?,
    val nombreProducto: String,
    val precioUnitario: Int
)

/** Pedido junto con sus líneas de detalle (relación 1 a N resuelta por Room). */
data class PedidoConDetalle(
    @Embedded val pedido: PedidoEntity,
    @Relation(parentColumn = "id", entityColumn = "pedidoId")
    val detalles: List<DetallePedidoEntity>
)

/** Foto de perfil de un cliente (solo los que tienen foto). Sirve para mostrarla en las comandas del administrador. */
data class FotoCliente(
    val clienteId: Long,
    val fotoPerfil: String
)
