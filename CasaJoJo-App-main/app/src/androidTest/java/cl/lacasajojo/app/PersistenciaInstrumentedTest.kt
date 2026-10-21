package cl.lacasajojo.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.data.repository.LoginResultado
import cl.lacasajojo.app.data.repository.PedidoResultado
import cl.lacasajojo.app.data.repository.SolicitudPedido
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Persistencia real en disco: se cierra la base de datos y se vuelve a abrir el mismo archivo,
 * igual que al cerrar y volver a abrir la aplicación.
 */
@RunWith(AndroidJUnit4::class)
class PersistenciaInstrumentedTest {

    private val archivoBd = "casajojo_prueba_persistencia.db"
    private lateinit var contexto: Context

    @Before
    fun preparar() {
        contexto = ApplicationProvider.getApplicationContext()
        contexto.deleteDatabase(archivoBd)
    }

    @After
    fun limpiar() {
        contexto.deleteDatabase(archivoBd)
    }

    @Test
    fun usuarios_menu_y_pedidos_persisten_al_reabrir_la_base_de_datos() = runTest {
        // --- Primera "ejecución" de la app ---
        val primera = EntornoPrueba(contexto, archivoBd)
        val cliente = primera.registrar("cliente@correo.cl", Rol.CLIENTE, "Clara Cliente")
        val plato = primera.crearProducto("Pollo asado", CategoriaProducto.PLATO_FONDO, 4500)
        val idPedido = UUID.randomUUID().toString()
        val creado = primera.pedidos.crearPedido(
            SolicitudPedido(
                idPedido = idPedido,
                cliente = cliente,
                seleccion = mapOf(CategoriaProducto.PLATO_FONDO to plato),
                especificaciones = "sin sal",
                horaRetiroMillis = primera.retiroEnMinutos(45)
            )
        )
        assertTrue(creado is PedidoResultado.Exito)
        assertTrue(primera.pedidos.cambiarEstado(idPedido, EstadoPedido.EN_PREPARACION))
        primera.cerrar()

        // --- Segunda "ejecución": se abre el mismo archivo ---
        val segunda = EntornoPrueba(contexto, archivoBd)
        try {
            assertNotNull(segunda.database.usuarioDao().buscarPorCorreo("cliente@correo.cl"))
            assertEquals(1, segunda.menu.observarTodos().first().size)

            val pedidos = segunda.pedidos.observarDeCliente(cliente.id).first()
            assertEquals(1, pedidos.size)
            assertEquals(idPedido, pedidos[0].pedido.id)
            assertEquals(EstadoPedido.EN_PREPARACION, pedidos[0].pedido.estado)
            assertEquals(4500, pedidos[0].pedido.total)
            assertEquals("sin sal", pedidos[0].pedido.especificaciones)
            assertEquals("Pollo asado", pedidos[0].detalles[0].nombreProducto)

            // Y las credenciales siguen permitiendo iniciar sesión.
            val login = segunda.auth.iniciarSesion("cliente@correo.cl", "clave123")
            assertTrue(login is LoginResultado.Exito)
        } finally {
            segunda.cerrar()
        }
    }
}
