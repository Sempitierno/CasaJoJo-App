package cl.lacasajojo.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombreCompleto: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val correo: String,
    val rol: Rol,
    val claveHash: String,
    val creadoEn: Long
)

/** Datos públicos del usuario para la interfaz (sin credenciales). Es una proyección de [UsuarioEntity]. */
data class PerfilUsuario(
    val id: Long,
    val nombreCompleto: String,
    val correo: String,
    val rol: Rol
)