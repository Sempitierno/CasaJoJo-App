package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.repository.MenuRepository
import cl.lacasajojo.app.data.repository.ProductoResultado
import cl.lacasajojo.app.util.ErroresProducto
import cl.lacasajojo.app.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductoFormUi(
    val cargando: Boolean = false,
    val esNuevo: Boolean = true,
    val noEncontrado: Boolean = false,
    val nombre: String = "",
    val categoria: CategoriaProducto? = null,
    val descripcion: String = "",
    /** El precio se edita como texto (solo dígitos) y se valida al guardar. */
    val precio: String = "",
    val disponible: Boolean = true,
    val publicado: Boolean = true,
    val errores: ErroresProducto = ErroresProducto(),
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val mensajeError: String? = null
)

/** Formulario para crear (productoId = 0) o editar un producto del menú. */
class ProductoFormViewModel(
    private val menu: MenuRepository,
    private val productoId: Long
) : ViewModel() {

    private val _ui = MutableStateFlow(
        ProductoFormUi(cargando = productoId != 0L, esNuevo = productoId == 0L)
    )
    val ui: StateFlow<ProductoFormUi> = _ui.asStateFlow()

    init {
        if (productoId != 0L) {
            viewModelScope.launch {
                val producto = menu.obtener(productoId)
                if (producto == null) {
                    _ui.update { it.copy(cargando = false, noEncontrado = true) }
                } else {
                    _ui.update {
                        it.copy(
                            cargando = false,
                            nombre = producto.nombre,
                            categoria = producto.categoria,
                            descripcion = producto.descripcion,
                            precio = producto.precio.toString(),
                            disponible = producto.disponible,
                            publicado = producto.publicado
                        )
                    }
                }
            }
        }
    }

    fun onNombreChange(valor: String) {
        _ui.update {
            it.copy(
                nombre = valor.take(Validaciones.MAX_NOMBRE_PRODUCTO + 10),
                errores = it.errores.copy(nombre = null),
                mensajeError = null
            )
        }
    }

    fun onCategoriaChange(valor: CategoriaProducto) {
        _ui.update { it.copy(categoria = valor, errores = it.errores.copy(categoria = null), mensajeError = null) }
    }

    fun onDescripcionChange(valor: String) {
        _ui.update {
            it.copy(
                descripcion = valor.take(Validaciones.MAX_DESCRIPCION + 10),
                errores = it.errores.copy(descripcion = null),
                mensajeError = null
            )
        }
    }

    fun onPrecioChange(valor: String) {
        // Solo dígitos, máximo 7 caracteres.
        val soloDigitos = valor.filter { it.isDigit() }.take(7)
        _ui.update { it.copy(precio = soloDigitos, errores = it.errores.copy(precio = null), mensajeError = null) }
    }

    fun onDisponibleChange(valor: Boolean) {
        _ui.update { it.copy(disponible = valor) }
    }

    fun onPublicadoChange(valor: Boolean) {
        _ui.update { it.copy(publicado = valor) }
    }

    fun guardar() {
        val actual = _ui.value
        if (actual.guardando || actual.guardado) return

        val errores = Validaciones.validarProducto(
            actual.nombre, actual.categoria, actual.descripcion, actual.precio
        )
        if (errores.hayErrores) {
            _ui.update { it.copy(errores = errores) }
            return
        }

        _ui.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            val resultado = menu.guardar(
                id = productoId,
                nombre = actual.nombre,
                categoria = actual.categoria,
                descripcion = actual.descripcion,
                precioTexto = actual.precio,
                disponible = actual.disponible,
                publicado = actual.publicado
            )
            when (resultado) {
                is ProductoResultado.Exito ->
                    _ui.update { it.copy(guardando = false, guardado = true) }
                is ProductoResultado.Invalido ->
                    _ui.update { it.copy(guardando = false, errores = resultado.errores) }
                is ProductoResultado.Fallo ->
                    _ui.update { it.copy(guardando = false, mensajeError = resultado.mensaje) }
            }
        }
    }
}
