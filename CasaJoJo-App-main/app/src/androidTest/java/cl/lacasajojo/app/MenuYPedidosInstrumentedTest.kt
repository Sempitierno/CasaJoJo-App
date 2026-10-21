package cl.lacasajojo.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.data.entity.PerfilUsuario
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.data.repository.PedidoResultado
import cl.lacasajojo.app.data.repository.ProductoResultado
import cl.lacasajojo.app.data.repository.SolicitudPedido
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Menú (administrador y cliente) y pedidos, probados contra una base de datos Room real. */
@RunWith(AndroidJUnit4::class)
class MenuYPedidosInstrumentedTest {

    private lateinit var entorno: EntornoPrueba

    @Before
    fun preparar() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        entorno = EntornoPrueba(contexto)
    }

    @After
    fun limpiar() {
        entorno.cerrar()
    }

    private fun solicitud(
        cliente: PerfilUsuario,
        seleccion: Map<CategoriaProducto, Long>,
        idPedido: String = UUID.randomUUID().toString(),
        especificaciones: String = "",
        retiroEnMinutos: Int = 60
    ) = SolicitudPedido(
        idPedido = idPedido,
        cliente = cliente,
        seleccion = seleccion,
        especificaciones = especificaciones,
        horaRetiroMillis = entorno.retiroEnMinutos(retiroEnMinutos)
    )

    // ---------- Menú ----------

    @Test
    fun el_administrador_crea_y_edita_un_producto() = runTest {
        val id = entorno.crearProducto("Pollo asado", CategoriaProducto.PLATO_FONDO, 4500)

        val original = entorno.menu.obtener(id)
        assertNotNull(original)
        assertEquals("Pollo asado", original!!.nombre)
        assertEquals(4500, original.precio)

        val edicion = entorno.menu.guardar(
            id = id,
            nombre = "Pollo asado con papas",
            categoria = CategoriaProducto.PLATO_FONDO,
            descripcion = "Con papas doradas",
            precioTexto = "4800",
            disponible = true,
            publicado = true
        )
        assertTrue(edicion is ProductoResultado.Exito)

        val editado = entorno.menu.obtener(id)!!
        assertEquals("Pollo asado con papas", editado.nombre)
        assertEquals("Con papas doradas", editado.descripcion)
        assertEquals(4800, editado.precio)
    }

    @Test
    fun rechaza_productos_con_datos_invalidos() = runTest {
        val resultado = entorno.menu.guardar(0L, "", null, "", "0", true, true)
        assertTrue(resultado is ProductoResultado.Invalido)
        assertEquals(0, entorno.database.productoDao().contar())
    }

    @Test
    fun el_cliente_solo_ve_los_productos_publicados() = runTest {
        entorno.crearProducto("Visible", CategoriaProducto.PLATO_FONDO, 4000, publicado = true)
        entorno.crearProducto("Oculto", CategoriaProducto.PLATO_FONDO, 4100, publicado = false)

        val delCliente = entorno.menu.observarPublicados().first()
        val delAdministrador = entorno.menu.observarTodos().first()

        assertEquals(listOf("Visible"), delCliente.map { it.nombre })
        assertEquals(2, delAdministrador.size)
    }

    @Test
    fun un_producto_no_disponible_se_muestra_pero_marcado_como_no_disponible() = runTest {
        entorno.crearProducto("Agotado", CategoriaProducto.POSTRE, 1000, disponible = false)

        val menu = entorno.menu.observarPublicados().first()

        assertEquals(1, menu.size)
        assertFalse(menu[0].disponible)
    }

    @Test
    fun la_disponibilidad_y_la_publicacion_se_pueden_cambiar() = runTest {
        val id = entorno.crearProducto("Flan", CategoriaProducto.POSTRE, 900)

        assertTrue(entorno.menu.cambiarDisponible(id, false))
        assertFalse(entorno.menu.obtener(id)!!.disponible)

        assertTrue(entorno.menu.cambiarPublicado(id, false))
        assertTrue(entorno.menu.observarPublicados().first().isEmpty())
    }

    @Test
    fun el_menu_se_entrega_ordenado_por_categoria() = runTest {
        entorno.crearProducto("Jugo", CategoriaProducto.BEBIDA, 1000)
        entorno.crearProducto("Pollo", CategoriaProducto.PLATO_FONDO, 4000)
        entorno.crearProducto("Flan", CategoriaProducto.POSTRE, 900)

        val categorias = entorno.menu.observarTodos().first().map { it.categoria }

        assertEquals(
            listOf(CategoriaProducto.PLATO_FONDO, CategoriaProducto.POSTRE, CategoriaProducto.BEBIDA),
            categorias
        )
    }

    @Test
    fun el_menu_de_demostracion_solo_se_carga_si_no_hay_productos() = runTest {
        val primera = entorno.menu.cargarMenuDemostracion()
        assertNotNull(primera)
        assertTrue(primera!! > 0)

        val segunda = entorno.menu.cargarMenuDemostracion()
        assertEquals(0, segunda)
        assertTrue(entorno.menu.observarTodos().first().all { it.nombre.endsWith("(demo)") })
    }

    // ---------- Pedidos ----------

    @Test
    fun crea_un_pedido_con_total_calculado_y_estado_recibido() = runTest {
        val cliente = entorno.registrar("cliente@correo.cl", Rol.CLIENTE, "Clara Cliente")
        val plato = entorno.crearProducto("Pollo", CategoriaProducto.PLATO_FONDO, 4500)
        val arroz = entorno.crearProducto("Arroz", CategoriaProducto.ACOMPANAMIENTO, 1000)
        val jugo = entorno.crearProducto("Jugo", CategoriaProducto.BEBIDA, 1500)

        val resultado = entorno.pedidos.crearPedido(
            solicitud(
                cliente,
                mapOf(
                    CategoriaProducto.PLATO_FONDO to plato,
                    CategoriaProducto.ACOMPANAMIENTO to arroz,
                    CategoriaProducto.BEBIDA to jugo
                ),
                especificaciones = "  sin gluten  "
            )
        )

        assertTrue(resultado is PedidoResultado.Exito)
        val exito = resultado as PedidoResultado.Exito
        assertEquals(7000, exito.total)

        val guardado = entorno.pedidos.obtener(exito.pedidoId)
        assertNotNull(guardado)
        assertEquals(EstadoPedido.RECIBIDO, guardado!!.pedido.estado)
        assertEquals(7000, guardado.pedido.total)
        assertEquals(cliente.id, guardado.pedido.clienteId)
        assertEquals("Clara Cliente", guardado.pedido.clienteNombre)
        assertEquals("cliente@correo.cl", guardado.pedido.clienteCorreo)
        assertEquals("sin gluten", guardado.pedido.especificaciones)
        assertEquals(entorno.ahoraMillis, guardado.pedido.creadoEn)
        assertEquals(entorno.retiroEnMinutos(60), guardado.pedido.horaRetiro)
        assertEquals(3, guardado.detalles.size)
    }

    @Test
    fun el_pedido_conserva_nombres_y_precios_aunque_el_menu_cambie_despues() = runTest {
        val cliente = entorno.registrar("cliente@correo.cl", Rol.CLIENTE)
        val plato = entorno.crearProducto("Pollo asado", CategoriaProducto.PLATO_FONDO, 4500)
        val resultado = entorno.pedidos.crearPedido(
            solicitud(cliente, mapOf(CategoriaProducto.PLATO_FONDO to plato))
        ) as PedidoResultado.Exito

        // El administrador cambia el nombre y el precio...
        entorno.menu.guardar(plato, "Pollo al horno", CategoriaProducto.PLATO_FONDO, "", "9990", true, true)
        // ...y después elimina el producto del menú.
        assertTrue(entorno.menu.eliminar(plato))

        val pedido = entorno.pedidos.obtener(resultado.pedidoId)
        assertNotNull("El pedido no debe borrarse al eliminar un producto", pedido)
        assertEquals("Pollo asado", pedido!!.detalles[0].nombreProducto)
        assertEquals(4500, pedido.detalles[0].precioUnitario)
        assertEquals(4500, pedido.pedido.total)
    }

    @Test
    fun rechaza_un_pedido_sin_plato_de_fondo() = runTest {
        val cliente = entorno.registrar("cliente@correo.cl", Rol.CLIENTE)
        val bebida = entorno.crearProducto("Jugo", CategoriaProducto.BEBIDA, 1500)

        val resultado = entorno.pedidos.crearPedido(
            solicitud(cliente, mapOf(CategoriaProducto.BEBIDA to bebida))
        )

        assertTrue(resultado is PedidoResultado.Invalido)
        assertNotNull((resultado as PedidoResultado.Invalido).errores.platoDeFondo)
        assertTrue(entorno.pedidos.observarTodos().first().isEmpty())
    }

    @Test
    fun rechaza_una_hora_de_retiro_anterior_a_la_actual() = runTest {
        val cliente = entorno.registrar("cliente@correo.cl", Rol.CLIENTE)
        val plato = entorno.crearProducto("Pollo", CategoriaProducto.PLATO_FONDO, 4500)

        val resultado = entorno.pedidos.crearPedido(
            solicitud(cliente, mapOf(CategoriaProducto.PLATO_FONDO to plato), retiroEnMinutos = -30)
        )

        assertTrue(resultado is PedidoResultado.Invalido)
        assertNotNull((resultado as PedidoResultado.Invalido).errores.horaRetiro)
    }

    @Test
    fun rechaza_un_producto_que_dejo_de_estar_disponible() = runTest {
        val cliente = entorno.registrar("cliente@correo.cl", Rol.CLIENTE)
        val plato = entorno.crearProducto("Pollo", CategoriaProducto.PLATO_FONDO, 4500)
        entorno.menu.cambiarDisponible(plato, false)

        val resultado = entorno.pedidos.crearPedido(
            solicitud(cliente, mapOf(CategoriaProducto.PLATO_FONDO to plato))
        )

        assertTrue(resultado is PedidoResultado.Rechazado)
        assertTrue(entorno.pedidos.observarTodos().first().isEmpty())
    }

    @Test
    fun una_pulsacion_repetida_no_duplica_el_pedido() = runTest {
        val cliente = entorno.registrar("cliente@correo.cl", Rol.CLIENTE)
        val plato = entorno.crearProducto("Pollo", CategoriaProducto.PLATO_FONDO, 4500)
        val mismoId = UUID.randomUUID().toString()
        val peticion = solicitud(cliente, mapOf(CategoriaProducto.PLATO_FONDO to plato), idPedido = mismoId)

        val primero = entorno.pedidos.crearPedido(peticion)
        val segundo = entorno.pedidos.crearPedido(peticion)

        assertTrue(primero is PedidoResultado.Exito)
        assertTrue(segundo is PedidoResultado.Exito)
        val todos = entorno.pedidos.observarTodos().first()
        assertEquals(1, todos.size)
        assertEquals(1, todos[0].detalles.size)
    }

    @Test
    fun el_administrador_cambia_el_estado_y_el_cliente_lo_ve() = runTest {
        val cliente = entorno.registrar("cliente@correo.cl", Rol.CLIENTE)
        val plato = entorno.crearProducto("Pollo", CategoriaProducto.PLATO_FONDO, 4500)
        val creado = entorno.pedidos.crearPedido(
            solicitud(cliente, mapOf(CategoriaProducto.PLATO_FONDO to plato))
        ) as PedidoResultado.Exito

        entorno.ahoraMillis += 5 * 60_000L
        assertTrue(entorno.pedidos.cambiarEstado(creado.pedidoId, EstadoPedido.EN_PREPARACION))
        val enPreparacion = entorno.pedidos.observarDeCliente(cliente.id).first()
        assertEquals(EstadoPedido.EN_PREPARACION, enPreparacion[0].pedido.estado)
        assertEquals(entorno.ahoraMillis, enPreparacion[0].pedido.actualizadoEn)

        assertTrue(entorno.pedidos.cambiarEstado(creado.pedidoId, EstadoPedido.LISTO_RETIRO))
        val vistoPorElCliente = entorno.pedidos.observarUnoDeCliente(creado.pedidoId, cliente.id).first()
        assertEquals(EstadoPedido.LISTO_RETIRO, vistoPorElCliente!!.pedido.estado)
    }

    @Test
    fun cambiar_el_estado_de_un_pedido_inexistente_devuelve_falso() = runTest {
        assertFalse(entorno.pedidos.cambiarEstado("no-existe", EstadoPedido.LISTO_RETIRO))
    }

    @Test
    fun cada_cliente_solo_ve_sus_propios_pedidos() = runTest {
        val ana = entorno.registrar("ana@correo.cl", Rol.CLIENTE, "Ana")
        val beto = entorno.registrar("beto@correo.cl", Rol.CLIENTE, "Beto")
        val plato = entorno.crearProducto("Pollo", CategoriaProducto.PLATO_FONDO, 4500)
        val deAna = entorno.pedidos.crearPedido(
            solicitud(ana, mapOf(CategoriaProducto.PLATO_FONDO to plato))
        ) as PedidoResultado.Exito
        entorno.pedidos.crearPedido(solicitud(beto, mapOf(CategoriaProducto.PLATO_FONDO to plato)))
        entorno.pedidos.crearPedido(solicitud(beto, mapOf(CategoriaProducto.PLATO_FONDO to plato)))

        val pedidosDeAna = entorno.pedidos.observarDeCliente(ana.id).first()
        val pedidosDeBeto = entorno.pedidos.observarDeCliente(beto.id).first()

        assertEquals(1, pedidosDeAna.size)
        assertEquals(2, pedidosDeBeto.size)
        assertTrue(pedidosDeAna.all { it.pedido.clienteId == ana.id })
        assertTrue(pedidosDeBeto.all { it.pedido.clienteId == beto.id })
        // El administrador sí ve todos.
        assertEquals(3, entorno.pedidos.observarTodos().first().size)
        // Beto no puede abrir el detalle de un pedido de Ana.
        assertNull(entorno.pedidos.observarUnoDeCliente(deAna.pedidoId, beto.id).first())
    }

    @Test
    fun el_cambio_de_estado_queda_pendiente_de_notificar_hasta_que_se_marca() = runTest {
        val cliente = entorno.registrar("cliente@correo.cl", Rol.CLIENTE)
        val plato = entorno.crearProducto("Pollo", CategoriaProducto.PLATO_FONDO, 4500)
        val creado = entorno.pedidos.crearPedido(
            solicitud(cliente, mapOf(CategoriaProducto.PLATO_FONDO to plato))
        ) as PedidoResultado.Exito

        // Recién creado: el cliente ya conoce el estado "Recibido", no hay nada que avisar.
        assertTrue(entorno.pedidos.observarPendientesDeNotificar(cliente.id).first().isEmpty())

        entorno.pedidos.cambiarEstado(creado.pedidoId, EstadoPedido.EN_PREPARACION)
        val pendientes = entorno.pedidos.observarPendientesDeNotificar(cliente.id).first()
        assertEquals(1, pendientes.size)
        assertEquals(EstadoPedido.EN_PREPARACION, pendientes[0].estado)

        entorno.pedidos.marcarNotificado(creado.pedidoId, EstadoPedido.EN_PREPARACION)
        assertTrue(entorno.pedidos.observarPendientesDeNotificar(cliente.id).first().isEmpty())
    }
}
