package cl.lacasajojo.app

import android.content.Context
import androidx.room.Room
import cl.lacasajojo.app.data.database.AppDatabase
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.PerfilUsuario
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.data.repository.AuthRepository
import cl.lacasajojo.app.data.repository.MenuRepository
import cl.lacasajojo.app.data.repository.PedidoRepository
import cl.lacasajojo.app.data.repository.ProductoResultado
import cl.lacasajojo.app.data.repository.RegistroResultado
import cl.lacasajojo.app.data.repository.SesionStore
import cl.lacasajojo.app.util.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertTrue

/** Sesión en memoria para las pruebas (la real usa SharedPreferences). */
class SesionEnMemoria : SesionStore {
    private val estado = MutableStateFlow<Long?>(null)
    override val usuarioId: StateFlow<Long?> = estado.asStateFlow()
    override fun iniciar(id: Long) {
        estado.value = id
    }
    override fun cerrar() {
        estado.value = null
    }
}

/**
 * Arma base de datos Room REAL (en memoria o en archivo) y los repositorios de la app,
 * con un reloj controlable y pocas iteraciones de hash para que las pruebas sean rápidas.
 */
class EntornoPrueba(contexto: Context, nombreArchivoBd: String? = null) {

    /** Instante "actual" de las pruebas; se puede mover para simular el paso del tiempo. */
    var ahoraMillis: Long = 1_800_000_000_000L

    val database: AppDatabase = if (nombreArchivoBd == null) {
        Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    } else {
        Room.databaseBuilder(contexto, AppDatabase::class.java, nombreArchivoBd)
            .allowMainThreadQueries()
            .build()
    }

    val sesion = SesionEnMemoria()

    val auth = AuthRepository(
        usuarioDao = database.usuarioDao(),
        sesion = sesion,
        hasher = PasswordHasher(iteraciones = 1_000),
        reloj = { ahoraMillis }
    )

    val menu = MenuRepository(productoDao = database.productoDao())

    val pedidos = PedidoRepository(
        pedidoDao = database.pedidoDao(),
        productoDao = database.productoDao(),
        reloj = { ahoraMillis }
    )

    fun cerrar() {
        database.close()
    }

    // ---------- Atajos para preparar datos ----------

    suspend fun registrar(
        correo: String,
        rol: Rol,
        nombre: String = "Usuario de Prueba"
    ): PerfilUsuario {
        val resultado = auth.registrar(nombre, correo, "clave123", "clave123", rol)
        assertTrue("El registro debía ser exitoso: $resultado", resultado is RegistroResultado.Exito)
        return (resultado as RegistroResultado.Exito).usuario
    }

    suspend fun crearProducto(
        nombre: String,
        categoria: CategoriaProducto,
        precio: Int,
        disponible: Boolean = true,
        publicado: Boolean = true
    ): Long {
        val resultado = menu.guardar(
            id = 0L,
            nombre = nombre,
            categoria = categoria,
            descripcion = "",
            precioTexto = precio.toString(),
            disponible = disponible,
            publicado = publicado
        )
        assertTrue("El producto debía guardarse: $resultado", resultado is ProductoResultado.Exito)
        return (resultado as ProductoResultado.Exito).id
    }

    /** Retiro dentro de [minutos] minutos a partir del "ahora" de la prueba (independiente de la zona horaria). */
    fun retiroEnMinutos(minutos: Int): Long = ahoraMillis + minutos * 60_000L
}
