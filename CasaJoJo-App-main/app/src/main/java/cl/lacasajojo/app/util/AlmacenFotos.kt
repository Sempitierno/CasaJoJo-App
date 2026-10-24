package cl.lacasajojo.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Manejo de las fotos tomadas con la cámara. Todo queda en el almacenamiento privado de la app
 * (`filesDir/fotos`); en la base de datos solo se guarda el NOMBRE del archivo, nunca una ruta absoluta.
 */
object AlmacenFotos {

    private const val CARPETA = "fotos"

    private fun carpeta(contexto: Context): File =
        File(contexto.filesDir, CARPETA).apply { mkdirs() }

    /** Archivo vacío y único donde la cámara escribirá la foto. [prefijo]: "pedido" o "perfil". */
    fun crearArchivoNuevo(contexto: Context, prefijo: String): File =
        File(carpeta(contexto), "${prefijo}_${System.currentTimeMillis()}.jpg")

    /**
     * Archivo de una foto ya guardada. Solo se usa el nombre final ([File.getName]), de modo que un valor
     * como "../x" no pueda salir de la carpeta de fotos.
     */
    fun archivo(contexto: Context, nombre: String): File =
        File(carpeta(contexto), File(nombre).name)

    /** URI temporal (FileProvider) que se entrega a la app de cámara para escribir en [archivo]. */
    fun uriParaCamara(contexto: Context, archivo: File): Uri =
        FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)

    /**
     * Copia a la carpeta privada una foto elegida de la galería (URI entregado por el selector de fotos del sistema).
     * Devuelve el NOMBRE del archivo guardado o null si no se pudo leer. Es una operación de disco: llamar fuera del
     * hilo principal.
     */
    fun importar(contexto: Context, uri: Uri, prefijo: String): String? {
        val destino = crearArchivoNuevo(contexto, prefijo)
        return try {
            contexto.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: return null
            if (esValida(destino)) {
                destino.name
            } else {
                destino.delete()
                null
            }
        } catch (e: Exception) {
            destino.delete()
            null
        }
    }

    /** Un archivo se considera foto válida si existe y no está vacío (la cámara puede cancelarse). */
    fun esValida(archivo: File): Boolean = archivo.exists() && archivo.length() > 0

    fun eliminar(contexto: Context, nombre: String?) {
        if (nombre.isNullOrBlank()) return
        runCatching { archivo(contexto, nombre).delete() }
    }

    /**
     * Carga la foto reducida (máx. [ladoMaximo] px) y corregida según la orientación EXIF, para no gastar
     * memoria con fotos de 12 MP. Debe llamarse fuera del hilo principal. Devuelve null si no se puede leer.
     */
    fun cargarBitmap(contexto: Context, nombre: String, ladoMaximo: Int = 1024): Bitmap? {
        val archivo = archivo(contexto, nombre)
        if (!esValida(archivo)) return null
        return runCatching {
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(archivo.path, limites)
            val opciones = BitmapFactory.Options().apply {
                inSampleSize = calcularMuestreo(limites.outWidth, limites.outHeight, ladoMaximo)
            }
            val bitmap = BitmapFactory.decodeFile(archivo.path, opciones) ?: return null
            rotarSegunExif(bitmap, archivo)
        }.getOrNull()
    }

    /** Potencia de 2 más grande que mantiene ambos lados >= [ladoMaximo]. Lógica pura (probada en JVM). */
    fun calcularMuestreo(ancho: Int, alto: Int, ladoMaximo: Int): Int {
        if (ancho <= 0 || alto <= 0 || ladoMaximo <= 0) return 1
        var muestreo = 1
        while (ancho / (muestreo * 2) >= ladoMaximo && alto / (muestreo * 2) >= ladoMaximo) {
            muestreo *= 2
        }
        return muestreo
    }

    private fun rotarSegunExif(bitmap: Bitmap, archivo: File): Bitmap {
        val grados = when (
            ExifInterface(archivo.path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (grados == 0f) return bitmap
        val matriz = Matrix().apply { postRotate(grados) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
    }
}
