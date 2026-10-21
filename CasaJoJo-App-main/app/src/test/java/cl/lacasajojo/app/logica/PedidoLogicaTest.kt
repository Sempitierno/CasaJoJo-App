package cl.lacasajojo.app.logica

import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.DetallePedidoEntity
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.data.entity.PedidoConDetalle
import cl.lacasajojo.app.data.entity.PedidoEntity
import cl.lacasajojo.app.data.entity.ProductoEntity
import cl.lacasajojo.app.data.repository.MenuRepository
import cl.lacasajojo.app.util.OrdenPedidos
import cl.lacasajojo.app.util.agruparPorCategoria
import cl.lacasajojo.app.util.codigo
import cl.lacasajojo.app.util.detalleDe
import cl.lacasajojo.app.util.resumenProductos
import cl.lacasajojo.app.viewmodel.NuevoPedidoUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class PedidoLogicaTest {

    private fun producto(
        id: Long,
        categoria: CategoriaProducto,
        precio: Int,
        nombre: String = "Producto $id",
        disponible: Boolean = true
    ) = ProductoEntity(
        id = id,
        nombre = nombre,
        categoria = categoria,
        precio = precio,
        disponible = disponible
    )

    private fun pedido(
        id: String,
        estado: EstadoPedido,
        horaRetiro: Long,
        creadoEn: Long,
        detalles: List<DetallePedidoEntity> = emptyList()
    ) = PedidoConDetalle(
        pedido = PedidoEntity(
            id = id,
            clienteId = 1,
            clienteNombre = "Cliente de prueba",
            clienteCorreo = "cliente@prueba.cl",
            creadoEn = creadoEn,
            horaRetiro = horaRetiro,
            especificaciones = "",
            total = 0,
            estado = estado,
            estadoNotificado = estado,
            actualizadoEn = creadoEn
        ),
        detalles = detalles
    )

    // ---------- Estados ----------

    @Test
    fun los_estados_avanzan_en_el_orden_del_caso() {
        assertEquals(EstadoPedido.EN_PREPARACION, EstadoPedido.RECIBIDO.siguiente())
        assertEquals(EstadoPedido.LISTO_RETIRO, EstadoPedido.EN_PREPARACION.siguiente())
        assertNull(EstadoPedido.LISTO_RETIRO.siguiente())
    }

    @Test
    fun los_estados_pueden_retroceder_para_corregir_errores() {
        assertNull(EstadoPedido.RECIBIDO.anterior())
        assertEquals(EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION.anterior())
        assertEquals(EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO_RETIRO.anterior())
    }

    @Test
    fun hay_exactamente_tres_estados_con_sus_nombres() {
        assertEquals(
            listOf("Recibido", "En preparación", "Listo para retiro"),
            EstadoPedido.entries.map { it.etiqueta }
        )
    }

    // ---------- Categorías y menú ----------

    @Test
    fun las_categorias_estan_en_el_orden_del_menu() {
        assertEquals(
            listOf("Plato de fondo", "Acompañamiento", "Ensalada", "Postre", "Bebida"),
            CategoriaProducto.entries.map { it.etiqueta }
        )
    }

    @Test
    fun solo_el_plato_de_fondo_es_obligatorio() {
        assertEquals(listOf(CategoriaProducto.PLATO_FONDO), CategoriaProducto.entries.filter { it.obligatoria })
    }

    @Test
    fun el_menu_se_ordena_por_categoria_y_luego_por_nombre() {
        val desordenado = listOf(
            producto(1, CategoriaProducto.BEBIDA, 1000, "Jugo"),
            producto(2, CategoriaProducto.PLATO_FONDO, 4000, "Pollo"),
            producto(3, CategoriaProducto.POSTRE, 900, "Flan"),
            producto(4, CategoriaProducto.PLATO_FONDO, 4200, "Albóndigas")
        )
        val ordenado = MenuRepository.ordenarPorCategoria(desordenado).map { it.nombre }
        assertEquals(listOf("Albóndigas", "Pollo", "Flan", "Jugo"), ordenado)
    }

    @Test
    fun agrupar_por_categoria_omite_las_categorias_vacias() {
        val productos = listOf(
            producto(1, CategoriaProducto.PLATO_FONDO, 4000),
            producto(2, CategoriaProducto.PLATO_FONDO, 4200),
            producto(3, CategoriaProducto.BEBIDA, 1000)
        )
        val grupos = productos.agruparPorCategoria()
        assertEquals(listOf(CategoriaProducto.PLATO_FONDO, CategoriaProducto.BEBIDA), grupos.map { it.first })
        assertEquals(2, grupos[0].second.size)
        assertTrue(emptyList<ProductoEntity>().agruparPorCategoria().isEmpty())
    }

    // ---------- Total y validación del formulario de pedido ----------

    @Test
    fun el_total_suma_los_precios_de_los_productos_elegidos() {
        val menu = mapOf(
            CategoriaProducto.PLATO_FONDO to listOf(producto(1, CategoriaProducto.PLATO_FONDO, 4500)),
            CategoriaProducto.BEBIDA to listOf(
                producto(2, CategoriaProducto.BEBIDA, 1500),
                producto(3, CategoriaProducto.BEBIDA, 1000)
            )
        )
        val ui = NuevoPedidoUi(
            cargando = false,
            menu = menu,
            seleccion = mapOf(CategoriaProducto.PLATO_FONDO to 1L, CategoriaProducto.BEBIDA to 3L)
        )
        assertEquals(5500, ui.total)
        assertEquals(listOf(1L, 3L), ui.productosSeleccionados.map { it.id })
    }

    @Test
    fun sin_seleccion_el_total_es_cero() {
        assertEquals(0, NuevoPedidoUi(cargando = false).total)
    }

    @Test
    fun sin_plato_de_fondo_ni_hora_no_se_puede_confirmar() {
        val ui = NuevoPedidoUi(cargando = false)
        assertEquals(2, ui.pendientes.size)
        assertFalse(ui.puedeConfirmar)
    }

    @Test
    fun con_plato_de_fondo_y_hora_se_puede_confirmar() {
        val menu = mapOf(
            CategoriaProducto.PLATO_FONDO to listOf(producto(1, CategoriaProducto.PLATO_FONDO, 4500))
        )
        val ui = NuevoPedidoUi(
            cargando = false,
            menu = menu,
            seleccion = mapOf(CategoriaProducto.PLATO_FONDO to 1L),
            horaRetiro = LocalTime.of(13, 30)
        )
        assertTrue(ui.pendientes.isEmpty())
        assertTrue(ui.puedeConfirmar)
    }

    @Test
    fun no_se_puede_confirmar_mientras_se_envia() {
        val menu = mapOf(
            CategoriaProducto.PLATO_FONDO to listOf(producto(1, CategoriaProducto.PLATO_FONDO, 4500))
        )
        val ui = NuevoPedidoUi(
            cargando = false,
            menu = menu,
            seleccion = mapOf(CategoriaProducto.PLATO_FONDO to 1L),
            horaRetiro = LocalTime.of(13, 30),
            enviando = true
        )
        assertFalse(ui.puedeConfirmar)
    }

    // ---------- Orden de las comandas para cocina ----------

    @Test
    fun la_cocina_ve_primero_el_retiro_mas_proximo_y_al_final_lo_ya_listo() {
        val a = pedido("A", EstadoPedido.RECIBIDO, horaRetiro = 3000, creadoEn = 1)
        val b = pedido("B", EstadoPedido.EN_PREPARACION, horaRetiro = 1000, creadoEn = 2)
        val c = pedido("C", EstadoPedido.LISTO_RETIRO, horaRetiro = 500, creadoEn = 3)
        val d = pedido("D", EstadoPedido.LISTO_RETIRO, horaRetiro = 100, creadoEn = 9)

        val orden = OrdenPedidos.paraCocina(listOf(a, c, d, b)).map { it.pedido.id }

        // Pendientes por hora de retiro (B, A); listos del más reciente al más antiguo (D, C).
        assertEquals(listOf("B", "A", "D", "C"), orden)
    }

    @Test
    fun con_la_misma_hora_de_retiro_gana_el_pedido_mas_antiguo() {
        val primero = pedido("P1", EstadoPedido.RECIBIDO, horaRetiro = 1000, creadoEn = 1)
        val segundo = pedido("P2", EstadoPedido.RECIBIDO, horaRetiro = 1000, creadoEn = 2)
        val orden = OrdenPedidos.paraCocina(listOf(segundo, primero)).map { it.pedido.id }
        assertEquals(listOf("P1", "P2"), orden)
    }

    // ---------- Helpers de presentación ----------

    @Test
    fun el_codigo_corto_sale_del_identificador_unico() {
        val p = pedido("a1b2c3d4-5678-90ab-cdef-1234567890ab", EstadoPedido.RECIBIDO, 0, 0).pedido
        assertEquals("#A1B2C3D4", p.codigo)
    }

    @Test
    fun el_resumen_de_productos_sigue_el_orden_de_las_categorias() {
        val detalles = listOf(
            DetallePedidoEntity(1, "x", CategoriaProducto.BEBIDA, 3, "Jugo", 1000),
            DetallePedidoEntity(2, "x", CategoriaProducto.PLATO_FONDO, 1, "Pollo", 4500),
            DetallePedidoEntity(3, "x", CategoriaProducto.ENSALADA, 2, "Chilena", 1200)
        )
        val conDetalle = pedido("x", EstadoPedido.RECIBIDO, 0, 0, detalles)
        assertEquals("Pollo, Chilena, Jugo", conDetalle.resumenProductos())
        assertEquals("Pollo", conDetalle.detalleDe(CategoriaProducto.PLATO_FONDO)?.nombreProducto)
        assertNull(conDetalle.detalleDe(CategoriaProducto.POSTRE))
    }
}
