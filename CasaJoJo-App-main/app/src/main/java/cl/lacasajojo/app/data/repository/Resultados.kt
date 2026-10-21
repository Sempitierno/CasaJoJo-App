package cl.lacasajojo.app.data.repository

import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.PerfilUsuario
import cl.lacasajojo.app.util.ErroresPedido
import cl.lacasajojo.app.util.ErroresProducto
import cl.lacasajojo.app.util.ErroresRegistro

/** Resultado de registrar un usuario. */
sealed interface RegistroResultado {
    data class Exito(val usuario: PerfilUsuario) : RegistroResultado
    data class Fallo(val errores: ErroresRegistro) : RegistroResultado
}

/** Resultado de iniciar sesión. */
sealed interface LoginResultado {
    data class Exito(val usuario: PerfilUsuario) : LoginResultado
    data class Fallo(val mensaje: String) : LoginResultado
}

/** Resultado de crear o editar un producto. */
sealed interface ProductoResultado {
    data class Exito(val id: Long) : ProductoResultado
    data class Invalido(val errores: ErroresProducto) : ProductoResultado
    data class Fallo(val mensaje: String) : ProductoResultado
}

/** Resultado de crear un pedido. */
sealed interface PedidoResultado {
    data class Exito(val pedidoId: String, val total: Int) : PedidoResultado
    data class Invalido(val errores: ErroresPedido) : PedidoResultado
    data class Rechazado(val mensaje: String) : PedidoResultado
}

/**
 * Datos para crear un pedido.
 * [seleccion] asocia cada categoría con el id del producto elegido (máximo uno por categoría).
 */
data class SolicitudPedido(
    val idPedido: String,
    val cliente: PerfilUsuario,
    val seleccion: Map<CategoriaProducto, Long>,
    val especificaciones: String,
    val horaRetiroMillis: Long
)
