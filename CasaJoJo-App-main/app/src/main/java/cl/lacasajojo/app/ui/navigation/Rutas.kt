package cl.lacasajojo.app.ui.navigation

import cl.lacasajojo.app.data.entity.Rol

/** Rutas de Navigation Compose. Las que llevan {argumento} se arman con las funciones de abajo. */
object Rutas {
    const val INICIO = "inicio"
    const val LOGIN = "login"
    const val REGISTRO = "registro"

    const val ADMIN_HOME = "admin/home"
    const val ADMIN_MENU = "admin/menu"
    const val ADMIN_PEDIDOS = "admin/pedidos"
    const val ARG_PRODUCTO_ID = "productoId"
    const val ADMIN_PRODUCTO_RUTA = "admin/producto/{productoId}"

    const val CLIENTE_HOME = "cliente/home"
    const val CLIENTE_MENU = "cliente/menu"
    const val CLIENTE_NUEVO_PEDIDO = "cliente/nuevo-pedido"
    const val CLIENTE_HISTORIAL = "cliente/historial"
    const val ARG_PEDIDO_ID = "pedidoId"
    const val CLIENTE_DETALLE_RUTA = "cliente/pedido/{pedidoId}"
    const val CLIENTE_CONFIRMADO_RUTA = "cliente/confirmado/{pedidoId}"

    /** productoId = 0 significa "producto nuevo". */
    fun adminProducto(productoId: Long): String = "admin/producto/$productoId"

    fun clienteDetalle(pedidoId: String): String = "cliente/pedido/$pedidoId"

    fun clienteConfirmado(pedidoId: String): String = "cliente/confirmado/$pedidoId"

    /** Pantalla principal según el tipo de usuario. */
    fun inicioDe(rol: Rol): String = if (rol == Rol.ADMINISTRADOR) ADMIN_HOME else CLIENTE_HOME
}
