package cl.lacasajojo.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.lacasajojo.app.data.entity.PerfilUsuario
import cl.lacasajojo.app.data.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    /** Falla con SQLiteConstraintException si el correo ya existe (índice único NOCASE). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    /** El correo se compara sin distinguir mayúsculas (la columna usa collation NOCASE). */
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT id, nombreCompleto, correo, rol FROM usuarios WHERE id = :id LIMIT 1")
    fun observarPerfil(id: Long): Flow<PerfilUsuario?>

    @Query("SELECT id, nombreCompleto, correo, rol FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPerfil(id: Long): PerfilUsuario?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Delete
    suspend fun eliminar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios ORDER BY id DESC")
    suspend fun obtenerUsuarios(): List<UsuarioEntity>
}