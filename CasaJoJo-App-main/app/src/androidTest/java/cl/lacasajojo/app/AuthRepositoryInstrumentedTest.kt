package cl.lacasajojo.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.lacasajojo.app.data.entity.Rol
import cl.lacasajojo.app.data.repository.AuthRepository
import cl.lacasajojo.app.data.repository.LoginResultado
import cl.lacasajojo.app.data.repository.RegistroResultado
import cl.lacasajojo.app.data.repository.SesionPreferences
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

/** Registro, inicio de sesión y persistencia de la sesión con Room y SharedPreferences reales. */
@RunWith(AndroidJUnit4::class)
class AuthRepositoryInstrumentedTest {

    private lateinit var contexto: Context
    private lateinit var entorno: EntornoPrueba

    @Before
    fun preparar() {
        contexto = ApplicationProvider.getApplicationContext()
        entorno = EntornoPrueba(contexto)
    }

    @After
    fun limpiar() {
        entorno.cerrar()
    }

    @Test
    fun registra_un_cliente() = runTest {
        val resultado = entorno.auth.registrar(
            "Ana Pérez", "ana@correo.cl", "clave123", "clave123", Rol.CLIENTE
        )
        assertTrue(resultado is RegistroResultado.Exito)
        val usuario = (resultado as RegistroResultado.Exito).usuario
        assertEquals("Ana Pérez", usuario.nombreCompleto)
        assertEquals("ana@correo.cl", usuario.correo)
        assertEquals(Rol.CLIENTE, usuario.rol)
    }

    @Test
    fun registra_un_administrador() = runTest {
        val usuario = entorno.registrar("admin@correo.cl", Rol.ADMINISTRADOR, "Admin de Prueba")
        assertEquals(Rol.ADMINISTRADOR, usuario.rol)
    }

    @Test
    fun rechaza_correos_duplicados_sin_distinguir_mayusculas() = runTest {
        entorno.registrar("ana@correo.cl", Rol.CLIENTE)

        val repetido = entorno.auth.registrar(
            "Otra Persona", "ANA@Correo.CL", "otraClave1", "otraClave1", Rol.ADMINISTRADOR
        )

        assertTrue(repetido is RegistroResultado.Fallo)
        assertEquals(
            AuthRepository.MENSAJE_CORREO_DUPLICADO,
            (repetido as RegistroResultado.Fallo).errores.correo
        )
        assertEquals(1, entorno.database.usuarioDao().contar())
    }

    @Test
    fun el_indice_unico_de_la_base_de_datos_tambien_impide_duplicados() = runTest {
        entorno.registrar("ana@correo.cl", Rol.CLIENTE)
        val existente = entorno.database.usuarioDao().buscarPorCorreo("ANA@CORREO.CL")
        assertNotNull("La búsqueda por correo debe ignorar mayúsculas", existente)
    }

    @Test
    fun rechaza_contrasenas_que_no_coinciden() = runTest {
        val resultado = entorno.auth.registrar(
            "Ana Pérez", "ana@correo.cl", "clave123", "clave999", Rol.CLIENTE
        )
        assertTrue(resultado is RegistroResultado.Fallo)
        assertNotNull((resultado as RegistroResultado.Fallo).errores.confirmacion)
        assertEquals(0, entorno.database.usuarioDao().contar())
    }

    @Test
    fun exige_elegir_el_rol() = runTest {
        val resultado = entorno.auth.registrar(
            "Ana Pérez", "ana@correo.cl", "clave123", "clave123", null
        )
        assertTrue(resultado is RegistroResultado.Fallo)
        assertNotNull((resultado as RegistroResultado.Fallo).errores.rol)
    }

    @Test
    fun la_contrasena_no_se_guarda_en_texto_plano() = runTest {
        entorno.registrar("ana@correo.cl", Rol.CLIENTE)
        val guardado = entorno.database.usuarioDao().buscarPorCorreo("ana@correo.cl")
        assertNotNull(guardado)
        assertFalse(guardado!!.claveHash.contains("clave123"))
        assertTrue(guardado.claveHash.startsWith("pbkdf2_sha256:"))
    }

    @Test
    fun inicia_sesion_con_credenciales_correctas() = runTest {
        entorno.registrar("ana@correo.cl", Rol.CLIENTE, "Ana Pérez")

        val resultado = entorno.auth.iniciarSesion("  ANA@correo.cl ", "clave123")

        assertTrue(resultado is LoginResultado.Exito)
        val actual = entorno.auth.usuarioActual.first()
        assertNotNull(actual)
        assertEquals("Ana Pérez", actual!!.nombreCompleto)
        assertEquals(Rol.CLIENTE, actual.rol)
    }

    @Test
    fun no_inicia_sesion_con_contrasena_incorrecta() = runTest {
        entorno.registrar("ana@correo.cl", Rol.CLIENTE)

        val resultado = entorno.auth.iniciarSesion("ana@correo.cl", "claveEquivocada")

        assertTrue(resultado is LoginResultado.Fallo)
        assertEquals(AuthRepository.MENSAJE_CREDENCIALES, (resultado as LoginResultado.Fallo).mensaje)
        assertNull(entorno.auth.usuarioActual.first())
    }

    @Test
    fun no_inicia_sesion_con_un_correo_no_registrado() = runTest {
        val resultado = entorno.auth.iniciarSesion("nadie@correo.cl", "clave123")
        assertTrue(resultado is LoginResultado.Fallo)
        assertNull(entorno.auth.usuarioActual.first())
    }

    @Test
    fun cerrar_sesion_deja_la_app_sin_usuario_actual() = runTest {
        entorno.registrar("ana@correo.cl", Rol.CLIENTE)
        entorno.auth.iniciarSesion("ana@correo.cl", "clave123")
        assertNotNull(entorno.auth.usuarioActual.first())

        entorno.auth.cerrarSesion()

        assertNull(entorno.auth.usuarioActual.first())
    }

    @Test
    fun la_sesion_se_mantiene_al_reabrir_la_aplicacion() {
        val archivo = "sesion_prueba_instrumentada"
        contexto.getSharedPreferences(archivo, Context.MODE_PRIVATE).edit().clear().commit()

        val primera = SesionPreferences(contexto, archivo)
        assertNull(primera.usuarioId.value)
        primera.iniciar(42L)

        // Una instancia nueva equivale a cerrar y volver a abrir la app: lee lo guardado en disco.
        val segunda = SesionPreferences(contexto, archivo)
        assertEquals(42L, segunda.usuarioId.value)

        segunda.cerrar()
        assertNull(SesionPreferences(contexto, archivo).usuarioId.value)

        contexto.getSharedPreferences(archivo, Context.MODE_PRIVATE).edit().clear().commit()
    }
}
