package cl.lacasajojo.app.data

import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.ProductoEntity

/**
 * Menú de DEMOSTRACIÓN con datos ficticios. Solo se carga si el administrador lo pide expresamente
 * (botón "Cargar menú de ejemplo" cuando todavía no hay productos). Cada nombre termina en "(demo)".
 */
object DatosDemostracion {

    private const val NOTA = "Producto de demostración (datos ficticios)."

    val productos: List<ProductoEntity> = listOf(
        ProductoEntity(nombre = "Pollo asado (demo)", categoria = CategoriaProducto.PLATO_FONDO, descripcion = NOTA, precio = 4500),
        ProductoEntity(nombre = "Pastel de choclo (demo)", categoria = CategoriaProducto.PLATO_FONDO, descripcion = NOTA, precio = 4200),
        ProductoEntity(nombre = "Lasaña de verduras (demo)", categoria = CategoriaProducto.PLATO_FONDO, descripcion = NOTA, precio = 4300),
        ProductoEntity(nombre = "Arroz graneado (demo)", categoria = CategoriaProducto.ACOMPANAMIENTO, descripcion = NOTA, precio = 1000),
        ProductoEntity(nombre = "Puré de papas (demo)", categoria = CategoriaProducto.ACOMPANAMIENTO, descripcion = NOTA, precio = 1200),
        ProductoEntity(nombre = "Papas fritas (demo)", categoria = CategoriaProducto.ACOMPANAMIENTO, descripcion = "$NOTA Agotado: sirve para probar productos no disponibles.", precio = 1500, disponible = false),
        ProductoEntity(nombre = "Ensalada chilena (demo)", categoria = CategoriaProducto.ENSALADA, descripcion = NOTA, precio = 1200),
        ProductoEntity(nombre = "Ensalada de lechuga y palta (demo)", categoria = CategoriaProducto.ENSALADA, descripcion = NOTA, precio = 1800),
        ProductoEntity(nombre = "Leche asada (demo)", categoria = CategoriaProducto.POSTRE, descripcion = NOTA, precio = 1300),
        ProductoEntity(nombre = "Fruta de la estación (demo)", categoria = CategoriaProducto.POSTRE, descripcion = NOTA, precio = 900),
        ProductoEntity(nombre = "Jugo natural de frutilla (demo)", categoria = CategoriaProducto.BEBIDA, descripcion = NOTA, precio = 1500),
        ProductoEntity(nombre = "Agua mineral (demo)", categoria = CategoriaProducto.BEBIDA, descripcion = NOTA, precio = 1000)
    )
}
