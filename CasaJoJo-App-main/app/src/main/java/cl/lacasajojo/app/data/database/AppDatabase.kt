package cl.lacasajojo.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.lacasajojo.app.data.dao.PedidoDao
import cl.lacasajojo.app.data.dao.ProductoDao
import cl.lacasajojo.app.data.dao.UsuarioDao
import cl.lacasajojo.app.data.entity.DetallePedidoEntity
import cl.lacasajojo.app.data.entity.PedidoEntity
import cl.lacasajojo.app.data.entity.ProductoEntity
import cl.lacasajojo.app.data.entity.UsuarioEntity

/**
 * Base de datos local (Room sobre SQLite). Todos los datos viven solo en el dispositivo.
 *
 * Estrategia de inicio: una única instancia (singleton) creada la primera vez que se necesita.
 * La versión actual es 3 (v2: fotos de perfil y de pedido; v3: foto del plato). Cuando el esquema cambie, se debe subir [version]
 * y agregar una Migration en [obtener]. Se evita fallbackToDestructiveMigration() a propósito, para no borrar
 * pedidos de forma silenciosa. El esquema se exporta a app/schemas para apoyar las migraciones.
 */
@Database(
    entities = [
        UsuarioEntity::class,
        ProductoEntity::class,
        PedidoEntity::class,
        DetallePedidoEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun productoDao(): ProductoDao

    abstract fun pedidoDao(): PedidoDao

    companion object {
        const val NOMBRE_ARCHIVO = "casajojo.db"

        /** v1 -> v2: agrega las columnas (opcionales) de las fotos. Los datos existentes se conservan. */
        /** v2 -> v3: agrega la columna (opcional) de la foto del plato. */
        val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE productos ADD COLUMN fotoProducto TEXT")
            }
        }

        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuarios ADD COLUMN fotoPerfil TEXT")
                db.execSQL("ALTER TABLE pedidos ADD COLUMN fotoPedido TEXT")
            }
        }

        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(contexto: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_ARCHIVO
                ).addMigrations(MIGRACION_1_2, MIGRACION_2_3).build().also { instancia = it }
            }
        }
    }
}
