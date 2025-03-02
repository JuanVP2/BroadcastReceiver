package com.example.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.CallLog
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class CallReceiver : BroadcastReceiver() {
    companion object {
        private var ultimoNumeroDetectado: String? = null
        private var smsEnviado = false
        private var ultimoEstado: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            Log.e("CallReceiver", ">>> Context o Intent son nulos, saliendo...")
            return
        }

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)


            if (state == ultimoEstado) {
                return
            }
            ultimoEstado = state

            var incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            Log.d("CallReceiver", ">>> Estado de la llamada: $state")


            if (incomingNumber == null) {
                incomingNumber = obtenerUltimaLlamada(context)
            }

            if (state == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber != null) {
                if (smsEnviado) {
                    Log.d("CallReceiver", ">>> SMS ya fue enviado en esta llamada, evitando duplicados.")
                    return
                }

                smsEnviado = true
                ultimoNumeroDetectado = incomingNumber
                Log.d("CallReceiver", ">>> Detectado número: $incomingNumber")
                enviarRespuestaAutomatica(context, incomingNumber)
            }

            if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                Log.d("CallReceiver", ">>> Llamada finalizada.")
                smsEnviado = false
            }
        }
    }

    private fun obtenerUltimaLlamada(context: Context): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Log.e("CallReceiver", ">>> Permiso READ_CALL_LOG no concedido")
            return null
        }

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER),
            null, null,
            CallLog.Calls.DATE + " DESC"
        )

        return cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            } else null
        }
    }

    private fun enviarRespuestaAutomatica(context: Context, numero: String) {
        val mensaje = "Lo siento, estoy ocupado. Te responderé pronto."

        Log.d("CallReceiver", ">>> Enviando SMS a: $numero")
        Log.d("CallReceiver", ">>> Mensaje: $mensaje")

        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(numero, null, mensaje, null, null)
            Log.d("CallReceiver", ">>> SMS enviado correctamente a: $numero")
        } catch (e: Exception) {
            Log.e("CallReceiver", ">>> Error al enviar SMS: ${e.message}")
        }
    }
}
