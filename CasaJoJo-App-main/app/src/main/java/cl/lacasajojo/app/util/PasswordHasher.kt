package cl.lacasajojo.app.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Protege las contraseñas con PBKDF2-HMAC-SHA256 y una sal aleatoria distinta por usuario.
 *
 * Lo que se guarda es un único texto: "pbkdf2_sha256:iteraciones:sal(Base64):hash(Base64)".
 * La contraseña original nunca se almacena. Guardar las iteraciones junto al hash permite subirlas
 * en el futuro sin invalidar las cuentas existentes.
 *
 * Es un mecanismo razonable para un prototipo local; en producción la autenticación debe resolverla un backend.
 */
class PasswordHasher(private val iteraciones: Int = ITERACIONES_POR_DEFECTO) {

    fun hash(password: String): String {
        val sal = ByteArray(LARGO_SAL_BYTES).also { SecureRandom().nextBytes(it) }
        val derivada = derivar(password, sal, iteraciones)
        return listOf(
            IDENTIFICADOR,
            iteraciones.toString(),
            codificar(sal),
            codificar(derivada)
        ).joinToString(SEPARADOR)
    }

    /** true si [password] corresponde al texto guardado. Comparación en tiempo constante. */
    fun verificar(password: String, almacenado: String): Boolean {
        val partes = almacenado.split(SEPARADOR)
        if (partes.size != 4 || partes[0] != IDENTIFICADOR) return false
        val iter = partes[1].toIntOrNull() ?: return false
        if (iter <= 0) return false
        return try {
            val sal = Base64.getDecoder().decode(partes[2])
            val esperado = Base64.getDecoder().decode(partes[3])
            val calculado = derivar(password, sal, iter)
            MessageDigest.isEqual(esperado, calculado)
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun derivar(password: String, sal: ByteArray, iter: Int): ByteArray {
        val especificacion = PBEKeySpec(password.toCharArray(), sal, iter, LARGO_CLAVE_BITS)
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacion).encoded
        } finally {
            especificacion.clearPassword()
        }
    }

    private fun codificar(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    companion object {
        const val ITERACIONES_POR_DEFECTO = 100_000
        private const val LARGO_SAL_BYTES = 16
        private const val LARGO_CLAVE_BITS = 256
        private const val ALGORITMO = "PBKDF2WithHmacSHA256"
        private const val IDENTIFICADOR = "pbkdf2_sha256"
        private const val SEPARADOR = ":"
    }
}
