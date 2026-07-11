package com.trabajofinaldam.notification;

// NUEVOS IMPORTS AGREGADOS
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.trabajofinaldam.R;
import com.trabajofinaldam.data.local.DatabaseHelper;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
        int pendientes = db.obtenerTareasPendientes().size();

        if (pendientes > 0) {
            enviarNotificacion("¡EcoTask te necesita! 🌿",
                    "Tienes " + pendientes + " tareas esperando. ¡Haz un poco de progreso hoy!");
        }

        return Result.success();
    }

    private void enviarNotificacion(String titulo, String mensaje) {
        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "ecotask_reminders";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // --- AQUÍ ESTÁ LA CORRECCIÓN ---
        // Verificamos si el dispositivo tiene Android 13 (TIRAMISU) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Si tiene los permisos concedidos, lanza la notificación
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                nm.notify(1, builder.build());
            }
        } else {
            // Para versiones de Android más antiguas (menores a 13), lanza la notificación directamente
            nm.notify(1, builder.build());
        }
    }
}
