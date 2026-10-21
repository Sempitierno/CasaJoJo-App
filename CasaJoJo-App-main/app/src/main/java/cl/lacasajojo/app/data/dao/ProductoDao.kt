package cl.lacasajojo.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.lacasajojo.app.data.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Insert
    suspend fun insertar(producto: ProductoEntity): Long

    @Insert
    suspend fun insertarTodos(productos: List<ProductoEntity>)

    @Update
    suspend fun actualizar(producto: ProductoEntity)

    @Delete
    suspend fun eliminar(producto: ProductoEntity)

    @Query("DELETE FROM productos WHERE id = :id")
    suspend fun eliminarPorId(id: Long): Int

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): ProductoEntity?

    @Query("SELECT * FROM productos WHERE id IN (:ids)")
    suspend fun obtenerPorIds(ids: List<Long>): List<ProductoEntity>

    /** Todos los productos (vista del administrador). El orden por categoría se aplica en el repositorio. */
    @Query("SELECT * FROM productos ORDER BY id")
    fun observarTodos(): Flow<List<ProductoEntity>>

    /** Solo los productos publicados (vista del cliente). */
    @Query("SELECT * FROM productos WHERE publicado = 1 ORDER BY id")
    fun observarPublicados(): Flow<List<ProductoEntity>>

    @Query("UPDATE productos SET publicado = :publicado WHERE id = :id")
    suspend fun cambiarPublicado(id: Long, publicado: Boolean): Int

    @Query("UPDATE productos SET disponible = :disponible WHERE id = :id")
    suspend fun cambiarDisponible(id: Long, disponible: Boolean): Int

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contar(): Int
}
