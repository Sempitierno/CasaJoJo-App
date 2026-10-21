package cl.lacasajojo.app.data.repository

import cl.lacasajojo.app.data.DatosDemostracion
import cl.lacasajojo.app.data.dao.ProductoDao
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.ProductoEntity
import cl.lacasajojo.app.util.Validaciones
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Administración del menú del día (administrador) y consulta del menú publicado (cliente). */
class MenuRepository(private val productoDao: ProductoDao) {

    /** Todos los productos, ordenados por categoría. Vista del administrador. */
    fun observarTodos(): Flow<List<ProductoEntity>> =
        productoDao.observarTodos().map { ordenarPorCategoria(it) }

    /** Solo los productos publicados, ordenados por categoría. Vista del cliente. */
    fun observarPublicados(): Flow<List<ProductoEntity>> =
        productoDao.observarPublicados().map { ordenarPorCategoria(it) }

    suspend fun obtener(id: Long): ProductoEntity? = productoDao.obtenerPorId(id)

    /** Crea el producto (id = 0) o actualiza el existente (id distinto de 0), validando los datos. */
    suspend fun guardar(
        id: Long,
        nombre: String,
        categoria: CategoriaProducto?,
        descripcion: String,
        precioTexto: String,
        disponible: Boolean,
        publicado: Boolean
    ): ProductoResultado {
        val errores = Validaciones.validarProducto(nombre, categoria, descripcion, precioTexto)
        val precio = precioTexto.trim().toIntOrNull()
        if (errores.hayErrores || categoria == null || precio == null) {
            return ProductoResultado.Invalido(errores)
        }
        val producto = ProductoEntity(
            id = id,
            nombre = nombre.trim(),
            categoria = categoria,
            descripcion = descripcion.trim(),
            precio = precio,
            disponible = disponible,
            publicado = publicado
        )
        return try {
            if (id == 0L) {
                ProductoResultado.Exito(productoDao.insertar(producto))
            } else {
                productoDao.actualizar(producto)
                ProductoResultado.Exito(id)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ProductoResultado.Fallo("No se pudo guardar el producto. Intenta nuevamente.")
        }
    }

    /** Elimina un producto del menú. Los pedidos anteriores NO se alteran (guardan su propia copia). */
    suspend fun eliminar(id: Long): Boolean = ejecutar { productoDao.eliminarPorId(id) > 0 }

    suspend fun cambiarPublicado(id: Long, publicado: Boolean): Boolean =
        ejecutar { productoDao.cambiarPublicado(id, publicado) > 0 }

    suspend fun cambiarDisponible(id: Long, disponible: Boolean): Boolean =
        ejecutar { productoDao.cambiarDisponible(id, disponible) > 0 }

    /**
     * Inserta el menú de demostración (datos ficticios) solo si todavía no hay productos.
     * Devuelve cuántos productos agregó (0 si ya había productos) o null si ocurrió un error.
     */
    suspend fun cargarMenuDemostracion(): Int? {
        return try {
            if (productoDao.contar() > 0) {
                0
            } else {
                productoDao.insertarTodos(DatosDemostracion.productos)
                DatosDemostracion.productos.size
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun ejecutar(operacion: suspend () -> Boolean): Boolean {
        return try {
            operacion()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        /** Orden de la minuta: por categoría (plato de fondo, acompañamiento, ensalada, postre, bebida) y luego por nombre. */
        fun ordenarPorCategoria(productos: List<ProductoEntity>): List<ProductoEntity> =
            productos.sortedWith(
                compareBy<ProductoEntity>({ it.categoria.ordinal }, { it.nombre.lowercase() })
            )
    }
}
