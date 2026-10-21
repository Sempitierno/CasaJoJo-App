package cl.lacasajojo.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    // Pocas iteraciones solo para que la prueba sea rápida; la app usa 100.000 por defecto.
    private val hasher = PasswordHasher(iteraciones = 1_000)

    @Test
    fun el_texto_guardado_no_contiene_la_clave_en_texto_plano() {
        val almacenado = hasher.hash("MiClaveSecreta1")
        assertFalse(almacenado.contains("MiClaveSecreta1"))
        assertTrue(almacenado.startsWith("pbkdf2_sha256:1000:"))
    }

    @Test
    fun verifica_la_clave_correcta() {
        val almacenado = hasher.hash("clave123")
        assertTrue(hasher.verificar("clave123", almacenado))
    }

    @Test
    fun rechaza_una_clave_incorrecta() {
        val almacenado = hasher.hash("clave123")
        assertFalse(hasher.verificar("clave124", almacenado))
        assertFalse(hasher.verificar("", almacenado))
        assertFalse(hasher.verificar("CLAVE123", almacenado))
    }

    @Test
    fun la_misma_clave_genera_hashes_distintos_por_la_sal() {
        val primero = hasher.hash("clave123")
        val segundo = hasher.hash("clave123")
        assertNotEquals(primero, segundo)
        assertTrue(hasher.verificar("clave123", primero))
        assertTrue(hasher.verificar("clave123", segundo))
    }

    @Test
    fun rechaza_textos_guardados_con_formato_invalido() {
        assertFalse(hasher.verificar("x", "texto-sin-formato"))
        assertFalse(hasher.verificar("x", "pbkdf2_sha256:abc:def:ghi"))
        assertFalse(hasher.verificar("x", "pbkdf2_sha256:1000:!!!:!!!"))
        assertFalse(hasher.verificar("x", "otro_algoritmo:1000:AAAA:AAAA"))
        assertFalse(hasher.verificar("x", ""))
    }

    @Test
    fun verifica_hashes_creados_con_otra_cantidad_de_iteraciones() {
        val almacenado = PasswordHasher(iteraciones = 2_000).hash("abc12345")
        assertTrue(hasher.verificar("abc12345", almacenado))
    }
}
