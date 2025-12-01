package com.example.healynk.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun PinSetupScreen(onSetPin: (String) -> Unit) {
    val (pin, setPin) = remember { mutableStateOf("") }
    val (confirmPin, setConfirmPin) = remember { mutableStateOf("") }
    val (pinVisible, setPinVisible) = remember { mutableStateOf(false) }
    val (confirmPinVisible, setConfirmPinVisible) = remember { mutableStateOf(false) }
    val isValid = pin.length >= 4 && pin == confirmPin

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Buat PIN Healynk", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = setPin,
            label = { Text("PIN") },
            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { setPinVisible(!pinVisible) }) {
                    Icon(
                        imageVector = if (pinVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (pinVisible) "Sembunyikan PIN" else "Tampilkan PIN"
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPin,
            onValueChange = setConfirmPin,
            label = { Text("Konfirmasi PIN") },
            visualTransformation = if (confirmPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { setConfirmPinVisible(!confirmPinVisible) }) {
                    Icon(
                        imageVector = if (confirmPinVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (confirmPinVisible) "Sembunyikan PIN" else "Tampilkan PIN"
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { if (isValid) onSetPin(pin) }, enabled = isValid, modifier = Modifier.fillMaxWidth()) {
            Text("Simpan PIN")
        }
    }
}
