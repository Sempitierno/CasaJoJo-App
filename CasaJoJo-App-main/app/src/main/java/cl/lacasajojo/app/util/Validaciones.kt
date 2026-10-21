package cl.lacasajojo.app.util

import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.Rol
import java.util.Locale

/** Mensajes de error por campo del formulario de registro (null = campo válido). */
data class ErroresRegistro(
    val nombre: String? = null,
    val correo: String? = null,
    val password: String? = null,
    val confirmacion: String? = null,
    val rol: String? = null,
    val general: String? = null
) {
    val hayErrores: Boolean
        get() = nombre != null || correo != null || password != null ||
            confirmacion != null || rol != null || general != null
}

data class ErroresLogin(
    val correo: String? = null,
    val password: String? = null
) {
    val hayErrores: Boolean
        get() = correo != null || password != null
}

data class ErroresProducto(
    val nombre: String? = null,
    val categoria: String? = null,
    val descripcion: String? = null,
    val precio: String? = null
) {
    val hayErrores: Boolean
        get() = nombre != null || categoria != null || descripcion != null || precio != null
}

data class ErroresPedido(
    val platoDeFondo: String? = null,
    val horaRetiro: String? = null,
    val especificaciones: String? = null
) {
    val hayErrores: Boolean
        get() = platoDeFondo != null || horaRetiro != null || especificaciones != null
}

/** Reglas de validación puras (sin Android ni base de datos), por lo que se prueban fácilmente. */
object Validaciones {

    const val MIN_LARGO_CLAVE = 6
    const val MAX_NOMBRE_PERSONA = 80
    const val MAX_NOMBRE_PRODUCTO = 60
    const val MAX_DESCRIPCION = 160
    const val MAX_PRECIO = 999_999
    const val MAX_ESPECIFICACIONES = 300

    private val patronCorreo =
        Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}")

    /** Correo en minúsculas y sin espacios: así "ANA@Correo.cl" y "ana@correo.cl" son el mismo. */
    fun normalizarCorreo(correo: String): String = correo.trim().lowercase(Locale.ROOT)

    fun esCorreoValido(correo: String): Boolean {
        val limpio = correo.trim()
        return limpio.length <= 254 && patronCorreo.matches(limpio)
    }

    fun validarRegistro(
        nombreCompleto: String,
        correo: String,
        password: String,
        confirmacion: String,
        rol: Rol?
    ): ErroresRegistro {
        val nombre = nombreCompleto.trim()
        val errorNombre = when {
            nombre.isEmpty() -> "Ingresa tu nombre completo"
            nombre.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            nombre.length > MAX_NOMBRE_PERSONA -> "El nombre puede tener como máximo $MAX_NOMBRE_PERSONA caracteres"
            else -> null
        }
        val errorCorreo = when {
            correo.isBlank() -> "Ingresa tu correo electrónico"
            !esCorreoValido(correo) -> "Ingresa un correo válido, por ejemplo nombre@correo.cl"
            else -> null
        }
        val errorPassword = when {
            password.isEmpty() -> "Ingresa una contraseña"
            password.length < MIN_LARGO_CLAVE -> "La contraseña debe tener al menos $MIN_LARGO_CLAVE caracteres"
            else -> null
        }
        val errorConfirmacion = when {
            confirmacion.isEmpty() -> "Confirma tu contraseña"
            confirmacion != password -> "Las contraseñas no coinciden"
            else -> null
        }
        val errorRol = if (rol == null) "Selecciona el tipo de usuario" else null
        return ErroresRegistro(
            nombre = errorNombre,
            correo = errorCorreo,
            password = errorPassword,
            confirmacion = errorConfirmacion,
            rol = errorRol
        )
    }

    fun validarLogin(correo: String, password: String): ErroresLogin {
        val errorCorreo = when {
            correo.isBlank() -> "Ingresa tu correo electrónico"
            !esCorreoValido(correo) -> "Ingresa un correo válido, por ejemplo nombre@correo.cl"
            else -> null
        }
        val errorPassword = if (password.isEmpty()) "Ingresa tu contraseña" else null
        return ErroresLogin(correo = errorCorreo, password = errorPassword)
    }

    fun validarProducto(
        nombre: String,
        categoria: CategoriaProducto?,
        descripcion: String,
        precioTexto: String
    ): ErroresProducto {
        val nombreLimpio = nombre.trim()
        val errorNombre = when {
            nombreLimpio.isEmpty() -> "Ingresa el nombre del producto"
            nombreLimpio.length > MAX_NOMBRE_PRODUCTO -> "Máximo $MAX_NOMBRE_PRODUCTO caracteres"
            else -> null
        }
        val errorCategoria = if (categoria == null) "Selecciona una categoría" else null
        val errorDescripcion =
            if (descripcion.trim().length > MAX_DESCRIPCION) "Máximo $MAX_DESCRIPCION caracteres" else null

        val precio = precioTexto.trim().toIntOrNull()
        val errorPrecio = when {
            precioTexto.isBlank() -> "Ingresa el precio"
            precio == null -> "El precio debe ser un número entero, sin puntos ni símbolos"
            precio <= 0 -> "El precio debe ser mayor que cero"
            precio > MAX_PRECIO -> "El precio máximo es ${Formato.precio(MAX_PRECIO)}"
            else -> null
        }
        return ErroresProducto(
            nombre = errorNombre,
            categoria = errorCategoria,
            descripcion = errorDescripcion,
            precio = errorPrecio
        )
    }

    /**
     * Reglas de un pedido: plato de fondo obligatorio, hora de retiro elegida y no anterior a la hora actual
     * (se compara a nivel de minuto), y especificaciones de largo razonable.
     */
    fun validarPedido(
        hayPlatoDeFondo: Boolean,
        horaRetiroMillis: Long?,
        ahoraMillis: Long,
        especificaciones: String
    ): ErroresPedido {
        val errorPlato = if (hayPlatoDeFondo) null else "Elige un plato de fondo para continuar"
        val minutoActual = ahoraMillis / 60_000L * 60_000L
        val errorHora = when {
            horaRetiroMillis == null -> "Elige la hora estimada de retiro"
            horaRetiroMillis < minutoActual -> "La hora de retiro no puede ser anterior a la hora actual"
            else -> null
        }
        val errorEspecificaciones =
            if (especificaciones.trim().length > MAX_ESPECIFICACIONES) "Máximo $MAX_ESPECIFICACIONES caracteres" else null
        return ErroresPedido(
            platoDeFondo = errorPlato,
            horaRetiro = errorHora,
            especificaciones = errorEspecificaciones
        )
    }
}
