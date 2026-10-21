package cl.lacasajojo.app.data

import android.content.Context
import cl.lacasajojo.app.data.database.AppDatabase
import cl.lacasajojo.app.data.repository.AuthRepository
import cl.lacasajojo.app.data.repository.MenuRepository
import cl.lacasajojo.app.data.repository.PedidoRepository
import cl.lacasajojo.app.data.repository.SesionPreferences
import cl.lacasajojo.app.data.repository.SesionStore
import cl.lacasajojo.app.util.NotificadorAndroid
import cl.lacasajojo.app.util.NotificadorPedidos

/**
 * Contenedor de dependencias manual (sin librerías de inyección, para mantener el código simple).
 * Se crea una sola vez en CasaJoJoApplication y los ViewModels reciben de aquí los repositorios.
 */
class AppContainer(contexto: Context) {

    private val contextoApp: Context = contexto.applicationContext

    val database: AppDatabase by lazy { AppDatabase.obtener(contextoApp) }

    val sesion: SesionStore by lazy { SesionPreferences(contextoApp) }

    val notificador: NotificadorPedidos by lazy { NotificadorAndroid(contextoApp) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(usuarioDao = database.usuarioDao(), sesion = sesion)
    }

    val menuRepository: MenuRepository by lazy {
        MenuRepository(productoDao = database.productoDao())
    }

    val pedidoRepository: PedidoRepository by lazy {
        PedidoRepository(pedidoDao = database.pedidoDao(), productoDao = database.productoDao())
    }
}
