package cl.lacasajojo.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class AlmacenFotosTest {

    @Test
    fun foto_pequena_no_se_reduce() {
        assertEquals(1, AlmacenFotos.calcularMuestreo(800, 600, 1024))
    }

    @Test
    fun foto_de_12_megapixeles_se_reduce_sin_bajar_del_lado_maximo() {
        // 4000x3000 con lado máximo 1024: 4000/2=2000, 4000/4=1000 (<1024) -> muestreo 2.
        assertEquals(2, AlmacenFotos.calcularMuestreo(4000, 3000, 1024))
    }

    @Test
    fun avatar_usa_un_muestreo_mayor() {
        assertEquals(8, AlmacenFotos.calcularMuestreo(4000, 3000, 256))
    }

    @Test
    fun medidas_invalidas_no_rompen() {
        assertEquals(1, AlmacenFotos.calcularMuestreo(0, 0, 1024))
        assertEquals(1, AlmacenFotos.calcularMuestreo(4000, 3000, 0))
    }
}
