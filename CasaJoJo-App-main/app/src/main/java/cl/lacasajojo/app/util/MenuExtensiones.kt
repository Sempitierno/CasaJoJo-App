package cl.lacasajojo.app.util

import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.ProductoEntity

/**
 * Agrupa los productos por categoría respetando el orden del menú (plato de fondo, acompañamiento,
 * ensalada, postre, bebida). Las categorías sin productos no aparecen en el resultado.
 */
fun List<ProductoEntity>.agruparPorCategoria(): List<Pair<CategoriaProducto, List<ProductoEntity>>> =
    CategoriaProducto.entries.mapNotNull { categoria ->
        val delGrupo = this.filter { it.categoria == categoria }
        if (delGrupo.isEmpty()) null else categoria to delGrupo
    }
