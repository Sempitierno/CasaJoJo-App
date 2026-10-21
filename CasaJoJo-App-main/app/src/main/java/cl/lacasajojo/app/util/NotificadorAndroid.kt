package cl.lacasajojo.app.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.lacasajojo.app.MainActivity
import cl.lacasajojo.app.R
import cl.lacasajojo.app.data.entity.PedidoEntity

/**
 * Notificaciones locales (no requieren Internet). En Android 13+ (API 33) exigen el permiso
 * POST_NOTIFICATIONS, que se solicita en la pantalla principal del cliente. Si el permiso no está
 * concedido, la app sigue funcionando con normalidad y simplemente no muestra avisos.
 */
class NotificadorAndroid(contexto: Context) : NotificadorPedidos {

    private val contexto: Context = contexto.applicationContext

    init {
        crearCanal()
    }

    override fun puedeNotificar(): Boolean {
        val permisoConcedido = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        return permisoConcedido && NotificationManagerCompat.from(contexto).areNotificationsEnabled()
    }

    override fun notificarCambioEstado(pedido: PedidoEntity): Boolean {
        if (!puedeNotificar()) return false

        val intent = Intent(contexto, MainActivity::class.java).apply {
            // SINGLE_TOP: si la app ya está abierta se reutiliza la pantalla actual en vez de reiniciarla.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            contexto,
            pedido.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val titulo = "Pedido ${pedido.codigo}: ${pedido.estado.etiqueta}"
        val mensaje = pedido.estado.mensajeCliente

        val notificacion = NotificationCompat.Builder(contexto, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(contexto).notify(pedido.id.hashCode(), notificacion)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    private fun crearCanal() {
        val canal = NotificationChannel(
            CANAL_ID,
            "Estado de pedidos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos cuando cambia el estado de tus pedidos en La Casa JoJo."
        }
        contexto.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
    }

    private companion object {
        const val CANAL_ID = "estado_pedidos"
    }
}
