package cl.lacasajojo.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import cl.lacasajojo.app.data.dao.UsuarioDao
import cl.lacasajojo.app.data.entity.PerfilUsuario
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.data.entity.UsuarioEntity
import cl.lacasajojo.app.util.ErroresRegistro
import cl.lacasajojo.app.util.PasswordHasher
import cl.lacasajojo.app.util.Validaciones
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

/** Registro, inicio y cierre de sesión, todo sin conexión a Internet. */
class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val sesion: SesionStore,
    private val hasher: PasswordHasher = PasswordHasher(),
    private val reloj: () -> Long = { System.currentTimeMillis() }
) {

    /** Usuario con la sesión iniciada (null si no hay sesión). Se actualiza solo al iniciar o cerrar sesión. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val usuarioActual: Flow<PerfilUsuario?> = sesion.usuarioId.flatMapLatest { id ->
        if (id == null) flowOf<PerfilUsuario?>(null) else usuarioDao.observarPerfil(id)
    }

    suspend fun registrar(
        nombreCompleto: String,
        correo: String,
        password: String,
        confirmacion: String,
        rol: Rol?
    ): RegistroResultado {
        val errores = Validaciones.validarRegistro(nombreCompleto, correo, password, confirmacion, rol)
        if (errores.hayErrores || rol == null) {
            return RegistroResultado.Fallo(errores)
        }
        val correoNormalizado = Validaciones.normalizarCorreo(correo)
        try {
            if (usuarioDao.buscarPorCorreo(correoNormalizado) != null) {
                return RegistroResultado.Fallo(ErroresRegistro(correo = MENSAJE_CORREO_DUPLICADO))
            }
            // El hash es costoso a propósito: se calcula fuera del hilo principal.
            val claveHash = withContext(Dispatchers.Default) { hasher.hash(password) }
            val usuario = UsuarioEntity(
                nombreCompleto = nombreCompleto.trim(),
                correo = correoNormalizado,
                rol = rol,
                claveHash = claveHash,
                creadoEn = reloj()
            )
            val id = usuarioDao.insertar(usuario)
            return RegistroResultado.Exito(
                PerfilUsuario(id, usuario.nombreCompleto, usuario.correo, usuario.rol)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: SQLiteConstraintException) {
            // Protección extra si dos registros con el mismo correo coinciden en el tiempo.
            return RegistroResultado.Fallo(ErroresRegistro(correo = MENSAJE_CORREO_DUPLICADO))
        } catch (e: Exception) {
            return RegistroResultado.Fallo(
                ErroresRegistro(general = "No se pudo completar el registro. Intenta nuevamente.")
            )
        }
    }

    suspend fun iniciarSesion(correo: String, password: String): LoginResultado {
        val errores = Validaciones.validarLogin(correo, password)
        if (errores.hayErrores) {
            return LoginResultado.Fallo(errores.correo ?: errores.password ?: MENSAJE_CREDENCIALES)
        }
        try {
            val usuario = usuarioDao.buscarPorCorreo(Validaciones.normalizarCorreo(correo))
            if (usuario == null) {
                return LoginResultado.Fallo(MENSAJE_CREDENCIALES)
            }
            val coincide = withContext(Dispatchers.Default) { hasher.verificar(password, usuario.claveHash) }
            if (!coincide) {
                return LoginResultado.Fallo(MENSAJE_CREDENCIALES)
            }
            sesion.iniciar(usuario.id)
            return LoginResultado.Exito(
                PerfilUsuario(usuario.id, usuario.nombreCompleto, usuario.correo, usuario.rol)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return LoginResultado.Fallo("No se pudo iniciar sesión. Intenta nuevamente.")
        }
    }

    fun cerrarSesion() {
        sesion.cerrar()
    }

    companion object {
        const val MENSAJE_CORREO_DUPLICADO = "Ya existe una cuenta registrada con este correo"
        const val MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos"
    }
}
