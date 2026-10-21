package cl.lacasajojo.app.util

import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.Rol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidacionesTest {

    // ---------- Correo ----------

    @Test
    fun correos_validos_son_aceptados() {
        assertTrue(Validaciones.esCorreoValido("ana@correo.cl"))
        assertTrue(Validaciones.esCorreoValido("ana.perez+jojo@mail.example.com"))
        assertTrue(Validaciones.esCorreoValido("  ana@correo.cl  "))
    }

    @Test
    fun correos_invalidos_son_rechazados() {
        assertFalse(Validaciones.esCorreoValido(""))
        assertFalse(Validaciones.esCorreoValido("ana"))
        assertFalse(Validaciones.esCorreoValido("ana@"))
        assertFalse(Validaciones.esCorreoValido("ana@correo"))
        assertFalse(Validaciones.esCorreoValido("@correo.cl"))
        assertFalse(Validaciones.esCorreoValido("ana@@correo.cl"))
        assertFalse(Validaciones.esCorreoValido("ana perez@correo.cl"))
        assertFalse(Validaciones.esCorreoValido("ana@correo.c"))
    }

    @Test
    fun normalizar_correo_ignora_mayusculas_y_espacios() {
        assertEquals("ana@correo.cl", Validaciones.normalizarCorreo("  ANA@Correo.CL "))
        assertEquals(
            Validaciones.normalizarCorreo("Ana@Correo.cl"),
            Validaciones.normalizarCorreo("ana@correo.CL")
        )
    }

    // ---------- Registro ----------

    @Test
    fun registro_valido_no_tiene_errores() {
        val errores = Validaciones.validarRegistro(
            "Ana Pérez", "ana@correo.cl", "clave123", "clave123", Rol.CLIENTE
        )
        assertFalse(errores.hayErrores)
    }

    @Test
    fun registro_con_campos_vacios_marca_cada_error() {
        val errores = Validaciones.validarRegistro("", "", "", "", null)
        assertNotNull(errores.nombre)
        assertNotNull(errores.correo)
        assertNotNull(errores.password)
        assertNotNull(errores.confirmacion)
        assertNotNull(errores.rol)
        assertTrue(errores.hayErrores)
    }

    @Test
    fun registro_exige_que_las_contrasenas_coincidan() {
        val errores = Validaciones.validarRegistro(
            "Ana Pérez", "ana@correo.cl", "clave123", "clave124", Rol.CLIENTE
        )
        assertEquals("Las contraseñas no coinciden", errores.confirmacion)
        assertNull(errores.password)
    }

    @Test
    fun registro_exige_largo_minimo_de_contrasena() {
        val errores = Validaciones.validarRegistro(
            "Ana Pérez", "ana@correo.cl", "12345", "12345", Rol.CLIENTE
        )
        assertNotNull(errores.password)
    }

    @Test
    fun registro_exige_elegir_el_tipo_de_usuario() {
        val errores = Validaciones.validarRegistro(
            "Ana Pérez", "ana@correo.cl", "clave123", "clave123", null
        )
        assertEquals("Selecciona el tipo de usuario", errores.rol)
    }

    @Test
    fun registro_rechaza_correo_con_formato_invalido() {
        val errores = Validaciones.validarRegistro(
            "Ana Pérez", "ana-correo", "clave123", "clave123", Rol.ADMINISTRADOR
        )
        assertNotNull(errores.correo)
    }

    // ---------- Inicio de sesión ----------

    @Test
    fun login_valida_campos_obligatorios() {
        assertTrue(Validaciones.validarLogin("", "").hayErrores)
        assertTrue(Validaciones.validarLogin("ana@correo.cl", "").hayErrores)
        assertFalse(Validaciones.validarLogin("ana@correo.cl", "x").hayErrores)
    }

    // ---------- Producto ----------

    @Test
    fun producto_valido_no_tiene_errores() {
        val errores = Validaciones.validarProducto(
            "Pollo asado", CategoriaProducto.PLATO_FONDO, "Con salsa", "4500"
        )
        assertFalse(errores.hayErrores)
    }

    @Test
    fun producto_exige_nombre_categoria_y_precio() {
        val errores = Validaciones.validarProducto("  ", null, "", "")
        assertNotNull(errores.nombre)
        assertNotNull(errores.categoria)
        assertNotNull(errores.precio)
    }

    @Test
    fun producto_rechaza_precios_invalidos() {
        assertNotNull(Validaciones.validarProducto("A", CategoriaProducto.POSTRE, "", "0").precio)
        assertNotNull(Validaciones.validarProducto("A", CategoriaProducto.POSTRE, "", "-5").precio)
        assertNotNull(Validaciones.validarProducto("A", CategoriaProducto.POSTRE, "", "3.500").precio)
        assertNotNull(Validaciones.validarProducto("A", CategoriaProducto.POSTRE, "", "abc").precio)
        assertNotNull(Validaciones.validarProducto("A", CategoriaProducto.POSTRE, "", "1000000").precio)
        assertNull(Validaciones.validarProducto("A", CategoriaProducto.POSTRE, "", "999999").precio)
    }

    @Test
    fun descripcion_es_opcional_pero_tiene_largo_maximo() {
        assertNull(
            Validaciones.validarProducto("A", CategoriaProducto.POSTRE, "", "1000").descripcion
        )
        val larga = "x".repeat(Validaciones.MAX_DESCRIPCION + 1)
        assertNotNull(
            Validaciones.validarProducto("A", CategoriaProducto.POSTRE, larga, "1000").descripcion
        )
    }

    // ---------- Pedido ----------

    private val ahora = 1_800_000_000_000L

    @Test
    fun pedido_valido_no_tiene_errores() {
        val errores = Validaciones.validarPedido(true, ahora + 3_600_000L, ahora, "sin cebolla")
        assertFalse(errores.hayErrores)
    }

    @Test
    fun pedido_exige_plato_de_fondo() {
        val errores = Validaciones.validarPedido(false, ahora + 3_600_000L, ahora, "")
        assertNotNull(errores.platoDeFondo)
        assertNull(errores.horaRetiro)
    }

    @Test
    fun pedido_exige_hora_de_retiro() {
        val errores = Validaciones.validarPedido(true, null, ahora, "")
        assertNotNull(errores.horaRetiro)
    }

    @Test
    fun hora_de_retiro_no_puede_ser_anterior_a_ahora() {
        assertNotNull(Validaciones.validarPedido(true, ahora - 120_000L, ahora, "").horaRetiro)
    }

    @Test
    fun hora_de_retiro_en_el_minuto_actual_es_valida() {
        val minutoActual = ahora / 60_000L * 60_000L
        assertNull(Validaciones.validarPedido(true, minutoActual, ahora, "").horaRetiro)
    }

    @Test
    fun especificaciones_tienen_largo_maximo() {
        val larga = "x".repeat(Validaciones.MAX_ESPECIFICACIONES + 1)
        assertNotNull(Validaciones.validarPedido(true, ahora + 60_000L, ahora, larga).especificaciones)
    }
}
