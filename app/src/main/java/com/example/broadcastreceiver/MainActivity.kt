package com.example.broadcastreceiver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNecessaryPermissions()


        val serviceIntent = Intent(this, CallService::class.java)
        startService(serviceIntent)


        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(CallReceiver(), filter)

        setContent {
            AutoReplyCallAppTheme {
                MainScreen()
            }
        }
    }

    private fun requestNecessaryPermissions() {
        val permissionsNeeded = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.SEND_SMS
        )

        val list = permissionsNeeded.filter {
            ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (list.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, list.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    val sharedPref: SharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE)

    var phoneNumber by remember {
        mutableStateOf(TextFieldValue(sharedPref.getString("phoneNumber", "") ?: ""))
    }
    var customMessage by remember {
        mutableStateOf(TextFieldValue(sharedPref.getString("customMessage", "Lo siento, estoy ocupado. Te responderé pronto.") ?: ""))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Auto Reply Call App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Número de teléfono") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = customMessage,
            onValueChange = { customMessage = it },
            label = { Text("Mensaje de respuesta automática") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            maxLines = 3
        )

        Button(
            onClick = {
                val number = phoneNumber.text.trim()
                val message = customMessage.text.trim()

                if (number.isNotEmpty() && message.isNotEmpty()) {
                    // Guarda la configuración en SharedPreferences
                    sharedPref.edit().putString("phoneNumber", number)
                        .putString("customMessage", message)
                        .apply()
                    CallReceiver.customMessage = message
                    Toast.makeText(context, "Respuesta automática configurada para $number", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Por favor ingrese un número y mensaje", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Configurar Auto-Respuesta")
        }
    }
}

@Composable
fun AutoReplyCallAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
