package cl.lacasajojo.app.data.entity

/** Tipo de usuario. En este prototipo académico se elige libremente al registrarse. */
enum class Rol(val etiqueta: String) {
    ADMINISTRADOR("Administrador"),
    CLIENTE("Cliente")
}

/**
 * Categorías del menú. El orden de declaración es el orden en que se muestran en toda la app.
 * [obligatoria]: el plato de fondo es el único elemento obligatorio de un pedido.
 */
enum class CategoriaProducto(
    val etiqueta: String,
    val obligatoria: Boolean,
    val textoNinguno: String
) {
    PLATO_FONDO("Plato de fondo", true, "Sin plato de fondo"),
    ACOMPANAMIENTO("Acompañamiento", false, "Sin acompañamiento"),
    ENSALADA("Ensalada", false, "Sin ensalada"),
    POSTRE("Postre", false, "Sin postre"),
    BEBIDA("Bebida", false, "Sin bebida")
}

/** Estados de preparación de un pedido (comanda). */
enum class EstadoPedido(val etiqueta: String, val mensajeCliente: String) {
    RECIBIDO(
        "Recibido",
        "Recibimos tu pedido. Pronto comenzaremos a prepararlo."
    ),
    EN_PREPARACION(
        "En preparación",
        "Estamos preparando tu pedido."
    ),
    LISTO_RETIRO(
        "Listo para retiro",
        "¡Tu pedido está listo! Ya puedes pasar a retirarlo."
    );

    /** Estado al que avanza el pedido, o null si ya está en el último. */
    fun siguiente(): EstadoPedido? = when (this) {
        RECIBIDO -> EN_PREPARACION
        EN_PREPARACION -> LISTO_RETIRO
        LISTO_RETIRO -> null
    }

    /** Estado anterior (para corregir un cambio por error), o null si es el primero. */
    fun anterior(): EstadoPedido? = when (this) {
        RECIBIDO -> null
        EN_PREPARACION -> RECIBIDO
        LISTO_RETIRO -> EN_PREPARACION
    }
}
