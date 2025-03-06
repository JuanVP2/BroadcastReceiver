package com.example.broadcastreceiver

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat

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
        val customMessage = intent?.getStringExtra("customMessage") ?: "Lo siento, estoy ocupado."

        Log.d("CallService", "Número entrante detectado: $phoneNumber")
        Log.d("CallService", "Mensaje a enviar: $customMessage")

        // Obtener el número configurado en SharedPreferences
        val sharedPref: SharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE)
        val configuredNumber = sharedPref.getString("phoneNumber", null)

        if (configuredNumber != null) {
            Log.d("CallService", "Usando número configurado: $configuredNumber")
            enviarSMS(configuredNumber, customMessage)
        } else {
            Log.e("CallService", "No hay número configurado, no se enviará SMS.")
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Llamada detectada")
            .setContentText("Número detectado: $phoneNumber")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    private fun enviarSMS(numero: String, mensaje: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(numero, null, mensaje, null, null)
            Log.d("CallService", "SMS enviado correctamente a: $numero con mensaje: $mensaje")
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
