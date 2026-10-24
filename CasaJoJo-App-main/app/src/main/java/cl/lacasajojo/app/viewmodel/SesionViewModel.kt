package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.PerfilUsuario
import cl.lacasajojo.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de la sesión local: se resuelve al abrir la app leyendo el usuario guardado en Room. */
sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object SinSesion : EstadoSesion
    data class Activa(val usuario: PerfilUsuario) : EstadoSesion
}

/** ViewModel de ámbito Activity: sabe quién tiene la sesión iniciada y permite cerrarla. */
class SesionViewModel(private val auth: AuthRepository) : ViewModel() {

    val estado: StateFlow<EstadoSesion> = auth.usuarioActual
        .map { perfil -> aEstado(perfil) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    /** Guarda la foto de perfil del usuario con sesión activa. La pantalla se actualiza sola (Room emite el cambio). */
    fun guardarFotoPerfil(nombreArchivo: String) {
        val usuario = (estado.value as? EstadoSesion.Activa)?.usuario ?: return
        viewModelScope.launch { auth.cambiarFotoPerfil(usuario.id, nombreArchivo) }
    }

    fun cerrarSesion() {
        auth.cerrarSesion()
    }

    private fun aEstado(perfil: PerfilUsuario?): EstadoSesion =
        if (perfil == null) EstadoSesion.SinSesion else EstadoSesion.Activa(perfil)
}
