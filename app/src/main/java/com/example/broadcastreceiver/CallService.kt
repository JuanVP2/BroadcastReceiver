package com.example.broadcastreceiver

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import android.util.Log

class CallService : Service() {

    companion object {
        const val CHANNEL_ID = "CallServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phoneNumber") ?: "Número desconocido"
        Log.d("CallService", "Iniciando servicio para número: $phoneNumber")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Llamada detectada")
            .setContentText("Número: $phoneNumber")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1, notification)

        enviarSMS(phoneNumber)

        return START_NOT_STICKY
    }

    private fun enviarSMS(numero: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(numero, null, "Lo siento, estoy ocupado.", null, null)
            Log.d("CallService", "SMS enviado correctamente a: $numero")
        } catch (e: Exception) {
            Log.e("CallService", "Error al enviar SMS: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canal de Servicio de Llamadas",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?) = null
}
