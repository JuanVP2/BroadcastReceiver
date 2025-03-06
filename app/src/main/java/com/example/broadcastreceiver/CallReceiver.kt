package com.example.broadcastreceiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CallLog
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat

class CallReceiver : BroadcastReceiver() {

    companion object {
        private var smsEnviado = false
        private var ultimoEstado: String? = null


        var customMessage: String = "Lo siento, estoy ocupado. Te responderé pronto."
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d("CallReceiver", ">>> onReceive() ejecutado")

        if (context == null || intent == null) {
            Log.e("CallReceiver", ">>> Context o Intent son nulos, saliendo...")
            return
        }


        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            if (state == ultimoEstado) return
            ultimoEstado = state


            if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                var incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (incomingNumber == null) {
                    incomingNumber = obtenerUltimaLlamada(context)
                }
                if (incomingNumber != null && !smsEnviado) {
                    Log.d("CallReceiver", ">>> Llamada entrante detectada. Número: $incomingNumber")
                    Log.d("CallReceiver", ">>> Mensaje personalizado: $customMessage")


                    val serviceIntent = Intent(context, CallService::class.java).apply {
                        putExtra("phoneNumber", incomingNumber)
                    }


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }

                    smsEnviado = true
                }
            }

            if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                Log.d("CallReceiver", ">>> Llamada finalizada.")

                smsEnviado = false
            }
        }
    }


    private fun obtenerUltimaLlamada(context: Context): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("CallReceiver", ">>> Permiso READ_CALL_LOG no concedido")
            return null
        }

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER),
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )

        return cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            } else null
        }
    }
}
