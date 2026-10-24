package cl.lacasajojo.app.util

import cl.lacasajojo.app.data.entity.EstadoPedido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UbicacionTest {

    @Test
    fun el_boton_no_aparece_mientras_el_pedido_solo_esta_recibido() {
        assertFalse(Ubicacion.mostrarEnPedido(EstadoPedido.RECIBIDO))
    }

    @Test
    fun el_boton_aparece_en_preparacion_y_hasta_que_esta_listo() {
        assertTrue(Ubicacion.mostrarEnPedido(EstadoPedido.EN_PREPARACION))
        assertTrue(Ubicacion.mostrarEnPedido(EstadoPedido.LISTO_RETIRO))
    }

    @Test
    fun la_direccion_en_texto_es_la_del_local() {
        assertEquals("El Álamo 408, 2570236 Viña del Mar, Valparaíso", Ubicacion.DIRECCION)
    }

    @Test
    fun el_enlace_apunta_a_google_maps() {
        assertTrue(Ubicacion.ENLACE_MAPS.startsWith("https://maps.app.goo.gl/"))
    }
}
