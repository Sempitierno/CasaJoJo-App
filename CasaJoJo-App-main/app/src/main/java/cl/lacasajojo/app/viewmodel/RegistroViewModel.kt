package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.data.repository.AuthRepository
import cl.lacasajojo.app.data.repository.RegistroResultado
import cl.lacasajojo.app.util.ErroresRegistro
import cl.lacasajojo.app.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUi(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmacion: String = "",
    /** Sin valor por defecto: el usuario debe elegir su tipo explícitamente. */
    val rol: Rol? = null,
    val errores: ErroresRegistro = ErroresRegistro(),
    val cargando: Boolean = false,
    val registrado: Boolean = false
)

class RegistroViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _ui = MutableStateFlow(RegistroUi())
    val ui: StateFlow<RegistroUi> = _ui.asStateFlow()

    fun onNombreChange(valor: String) {
        _ui.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null, general = null)) }
    }

    fun onCorreoChange(valor: String) {
        _ui.update { it.copy(correo = valor, errores = it.errores.copy(correo = null, general = null)) }
    }

    fun onPasswordChange(valor: String) {
        _ui.update {
            it.copy(
                password = valor,
                errores = it.errores.copy(password = null, confirmacion = null, general = null)
            )
        }
    }

    fun onConfirmacionChange(valor: String) {
        _ui.update { it.copy(confirmacion = valor, errores = it.errores.copy(confirmacion = null, general = null)) }
    }

    fun onRolChange(valor: Rol) {
        _ui.update { it.copy(rol = valor, errores = it.errores.copy(rol = null)) }
    }

    fun registrar() {
        val actual = _ui.value
        if (actual.cargando || actual.registrado) return

        val errores = Validaciones.validarRegistro(
            actual.nombre, actual.correo, actual.password, actual.confirmacion, actual.rol
        )
        if (errores.hayErrores) {
            _ui.update { it.copy(errores = errores) }
            return
        }

        _ui.update { it.copy(cargando = true, errores = ErroresRegistro()) }
        viewModelScope.launch {
            val resultado = auth.registrar(
                actual.nombre, actual.correo, actual.password, actual.confirmacion, actual.rol
            )
            when (resultado) {
                is RegistroResultado.Exito ->
                    _ui.update { it.copy(cargando = false, registrado = true) }
                is RegistroResultado.Fallo ->
                    _ui.update { it.copy(cargando = false, errores = resultado.errores) }
            }
        }
    }
}
