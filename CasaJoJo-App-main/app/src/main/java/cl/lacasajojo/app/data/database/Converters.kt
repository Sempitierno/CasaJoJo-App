package cl.lacasajojo.app.data.database

import androidx.room.TypeConverter
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.EstadoPedido
import cl.lacasajojo.app.data.entity.Rol

/** Convierte los enums a texto (su nombre) para guardarlos en SQLite y viceversa. */
class Converters {

    @TypeConverter
    fun rolATexto(valor: Rol): String = valor.name

    @TypeConverter
    fun textoARol(valor: String): Rol = Rol.valueOf(valor)

    @TypeConverter
    fun categoriaATexto(valor: CategoriaProducto): String = valor.name

    @TypeConverter
    fun textoACategoria(valor: String): CategoriaProducto = CategoriaProducto.valueOf(valor)

    @TypeConverter
    fun estadoATexto(valor: EstadoPedido): String = valor.name

    @TypeConverter
    fun textoAEstado(valor: String): EstadoPedido = EstadoPedido.valueOf(valor)
}
