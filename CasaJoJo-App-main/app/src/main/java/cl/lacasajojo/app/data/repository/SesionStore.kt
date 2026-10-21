package cl.lacasajojo.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Guarda quién tiene la sesión iniciada. Solo se almacena el id del usuario, NUNCA credenciales. */
interface SesionStore {
    val usuarioId: StateFlow<Long?>
    fun iniciar(id: Long)
    fun cerrar()
}

/**
 * Implementación con SharedPreferences privadas de la app. La sesión se mantiene al cerrar y abrir la
 * aplicación, hasta que el usuario cierra sesión. El archivo no contiene contraseñas ni datos personales.
 */
class SesionPreferences(
    contexto: Context,
    nombreArchivo: String = NOMBRE_POR_DEFECTO
) : SesionStore {

    private val preferencias: SharedPreferences =
        contexto.applicationContext.getSharedPreferences(nombreArchivo, Context.MODE_PRIVATE)

    private val estado = MutableStateFlow(leerGuardado())

    override val usuarioId: StateFlow<Long?> = estado.asStateFlow()

    private fun leerGuardado(): Long? {
        val valor = preferencias.getLong(CLAVE_USUARIO_ID, SIN_SESION)
        return if (valor == SIN_SESION) null else valor
    }

    override fun iniciar(id: Long) {
        preferencias.edit().putLong(CLAVE_USUARIO_ID, id).apply()
        estado.value = id
    }

    override fun cerrar() {
        preferencias.edit().remove(CLAVE_USUARIO_ID).apply()
        estado.value = null
    }

    companion object {
        const val NOMBRE_POR_DEFECTO = "sesion_casajojo"
        private const val CLAVE_USUARIO_ID = "usuario_id"
        private const val SIN_SESION = -1L
    }
}
