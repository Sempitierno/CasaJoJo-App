package cl.lacasajojo.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.data.repository.PedidoResultado
import cl.lacasajojo.app.data.repository.SolicitudPedido
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/** Fotos de perfil y de pedido guardadas en Room (la cámara en sí se prueba a mano en el dispositivo). */
@RunWith(AndroidJUnit4::class)
class FotosInstrumentedTest {

    private lateinit var entorno: EntornoPrueba

    @Before
    fun preparar() {
        entorno = EntornoPrueba(ApplicationProvider.getApplicationContext<Context>())
    }

    @After
    fun limpiar() {
        entorno.cerrar()
    }

    @Test
    fun el_usuario_nuevo_no_tiene_foto_y_puede_guardar_una() = runTest {
        val cliente = entorno.registrar("foto.cliente@ejemplo.cl", Rol.CLIENTE)
        assertNull(cliente.fotoPerfil)

        assertTrue(entorno.auth.cambiarFotoPerfil(cliente.id, "perfil_123.jpg"))

        entorno.sesion.iniciar(cliente.id)
        val actual = entorno.auth.usuarioActual.first()
        assertEquals("perfil_123.jpg", actual?.fotoPerfil)
    }

    @Test
    fun el_administrador_ve_la_foto_de_perfil_solo_de_los_clientes_que_la_tienen() = runTest {
        val conFoto = entorno.registrar("con.foto@ejemplo.cl", Rol.CLIENTE)
        val sinFoto = entorno.registrar("sin.foto@ejemplo.cl", Rol.CLIENTE)
        assertTrue(entorno.auth.cambiarFotoPerfil(conFoto.id, "perfil_777.jpg"))

        val fotos = entorno.pedidos.observarFotosClientes().first()

        assertEquals("perfil_777.jpg", fotos[conFoto.id])
        assertFalse(fotos.containsKey(sinFoto.id))
    }

    @Test
    fun cambiar_foto_de_un_usuario_inexistente_devuelve_false() = runTest {
        assertFalse(entorno.auth.cambiarFotoPerfil(9_999L, "perfil_x.jpg"))
    }

    @Test
    fun el_administrador_asigna_foto_a_un_pedido_y_el_cliente_la_ve() = runTest {
        val cliente = entorno.registrar("pedido.foto@ejemplo.cl", Rol.CLIENTE)
        val plato = entorno.crearProducto("Pollo (demo)", CategoriaProducto.PLATO_FONDO, 4500)
        val idPedido = UUID.randomUUID().toString()
        val resultado = entorno.pedidos.crearPedido(
            SolicitudPedido(
                idPedido = idPedido,
                cliente = cliente,
                seleccion = mapOf(CategoriaProducto.PLATO_FONDO to plato),
                especificaciones = "",
                horaRetiroMillis = entorno.retiroEnMinutos(60)
            )
        )
        assertTrue(resultado is PedidoResultado.Exito)
        assertNull(entorno.pedidos.obtener(idPedido)?.pedido?.fotoPedido)

        assertTrue(entorno.pedidos.asignarFoto(idPedido, "pedido_456.jpg"))

        val vistoPorCliente = entorno.pedidos.observarUnoDeCliente(idPedido, cliente.id).first()
        assertEquals("pedido_456.jpg", vistoPorCliente?.pedido?.fotoPedido)
    }

    @Test
    fun asignar_foto_a_un_pedido_inexistente_devuelve_false() = runTest {
        assertFalse(entorno.pedidos.asignarFoto("no-existe", "pedido_x.jpg"))
    }

    @Test
    fun el_producto_guarda_y_conserva_su_foto_al_editarlo() = runTest {
        val resultado = entorno.menu.guardar(
            id = 0L,
            nombre = "Lasaña (demo)",
            categoria = CategoriaProducto.PLATO_FONDO,
            descripcion = "",
            precioTexto = "4300",
            disponible = true,
            publicado = true,
            fotoProducto = "plato_789.jpg"
        )
        val id = (resultado as cl.lacasajojo.app.data.repository.ProductoResultado.Exito).id
        assertEquals("plato_789.jpg", entorno.menu.obtener(id)?.fotoProducto)

        // Editar quitando la foto (null) la elimina del producto.
        entorno.menu.guardar(id, "Lasaña (demo)", CategoriaProducto.PLATO_FONDO, "", "4300", true, true, null)
        assertNull(entorno.menu.obtener(id)?.fotoProducto)
    }
}
