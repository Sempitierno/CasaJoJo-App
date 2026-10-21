package cl.lacasajojo.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Elemento del menú del día.
 *
 * - [publicado]: el cliente solo ve los productos publicados.
 * - [disponible]: un producto publicado pero no disponible se muestra al cliente, pero no puede seleccionarse.
 * - [precio]: pesos chilenos enteros (CLP no usa decimales).
 */
@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val categoria: CategoriaProducto,
    val descripcion: String = "",
    val precio: Int,
    val disponible: Boolean = true,
    val publicado: Boolean = true
)
