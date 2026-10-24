package cl.lacasajojo.app.util

import cl.lacasajojo.app.data.entity.EstadoPedido

/** Datos del local para que el cliente sepa dónde retirar su pedido. */
object Ubicacion {

    const val NOMBRE_LOCAL = "La Casa JoJo"

    /** Dirección en texto, para que el cliente pueda leerla aunque no abra el mapa. */
    const val DIRECCION = "El Álamo 408, 2570236 Viña del Mar, Valparaíso"

    /**
     * Enlace de Google Maps del local. Al abrirlo, Android lo entrega a la app Google Maps si está instalada
     * (con ruta y botón "Cómo llegar") o al navegador si no. Para cambiar la dirección, basta con reemplazar este enlace.
     */
    const val ENLACE_MAPS = "https://maps.app.goo.gl/pG9MwqQjdEjMgzkNA"

    /** El botón de ubicación aparece en el detalle del pedido desde que se empieza a preparar hasta que está listo. */
    fun mostrarEnPedido(estado: EstadoPedido): Boolean =
        estado == EstadoPedido.EN_PREPARACION || estado == EstadoPedido.LISTO_RETIRO
}
