package cl.lacasajojo.app.data.repository

import cl.lacasajojo.app.data.dao.UsuarioDao
import cl.lacasajojo.app.data.entity.UsuarioEntity

class UsuarioRepository(private val dao: UsuarioDao) {
    suspend fun obtenerUsuarios(): List<UsuarioEntity> = dao.obtenerUsuarios()
    suspend fun insertar(usuario: UsuarioEntity) = dao.insertar(usuario)
    suspend fun eliminar(usuario: UsuarioEntity) = dao.eliminar(usuario)
}