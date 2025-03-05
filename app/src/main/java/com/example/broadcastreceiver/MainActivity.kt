package com.example.broadcastreceiver

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val serviceIntent = Intent(this, CallService::class.java)
        startService(serviceIntent)

        setContent {
            AutoReplyCallAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf(TextFieldValue("")) }
    var customMessage by remember {
        mutableStateOf(TextFieldValue("Lo siento, estoy ocupado. Te responderé pronto."))
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
                    CallReceiver.customMessage = message
                    Toast.makeText(
                        context,
                        "Respuesta automática configurada para $number",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Toast.makeText(
                        context,
                        "Por favor ingrese un número y mensaje",
                        Toast.LENGTH_SHORT
                    ).show()
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
    MaterialTheme(
        content = content
    )
}