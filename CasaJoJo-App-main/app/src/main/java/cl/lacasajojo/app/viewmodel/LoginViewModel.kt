package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.data.repository.AuthRepository
import cl.lacasajojo.app.data.repository.LoginResultado
import cl.lacasajojo.app.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUi(
    val correo: String = "",
    val password: String = "",
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    /** Rol del usuario autenticado; cuando es distinto de null la pantalla navega a su panel. */
    val autenticado: Rol? = null
)

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUi())
    val ui: StateFlow<LoginUi> = _ui.asStateFlow()

    fun onCorreoChange(valor: String) {
        _ui.update { it.copy(correo = valor, errorCorreo = null, errorGeneral = null) }
    }

    fun onPasswordChange(valor: String) {
        _ui.update { it.copy(password = valor, errorPassword = null, errorGeneral = null) }
    }

    fun iniciarSesion() {
        val actual = _ui.value
        if (actual.cargando || actual.autenticado != null) return

        val errores = Validaciones.validarLogin(actual.correo, actual.password)
        if (errores.hayErrores) {
            _ui.update { it.copy(errorCorreo = errores.correo, errorPassword = errores.password) }
            return
        }

        _ui.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            when (val resultado = auth.iniciarSesion(actual.correo, actual.password)) {
                is LoginResultado.Exito ->
                    _ui.update { it.copy(cargando = false, autenticado = resultado.usuario.rol) }
                is LoginResultado.Fallo ->
                    _ui.update { it.copy(cargando = false, errorGeneral = resultado.mensaje) }
            }
        }
    }
}
