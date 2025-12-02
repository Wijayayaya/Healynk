package com.example.healynk.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healynk.R

@Composable
fun PinSetupScreen(onSetPin: (String) -> Unit) {
    val (pin, setPin) = remember { mutableStateOf("") }
    val (confirmPin, setConfirmPin) = remember { mutableStateOf("") }
    val (pinVisible, setPinVisible) = remember { mutableStateOf(false) }
    val (confirmPinVisible, setConfirmPinVisible) = remember { mutableStateOf(false) }
    val isValid = pin.length >= 4 && pin == confirmPin

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00897B),
                        Color(0xFF26A69A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Keamanan Akun",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Buat PIN untuk melindungi akun Anda",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.95f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Image(
                painter = painterResource(id = R.drawable.logo_healynk),
                contentDescription = "Logo Healynk",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    Text(
                        text = "Buat PIN",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00897B),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Text(
                        text = "PIN harus terdiri dari minimal 4 angka",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6) setPin(it) },
                        label = { Text("PIN") },
                        placeholder = { Text("Masukkan PIN Anda") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF00897B)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { setPinVisible(!pinVisible) }) {
                                Icon(
                                    if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (pinVisible) "Sembunyikan PIN" else "Tampilkan PIN"
                                )
                            }
                        },
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF00897B)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 6) setConfirmPin(it) },
                        label = { Text("Konfirmasi PIN") },
                        placeholder = { Text("Masukkan ulang PIN Anda") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF00897B)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { setConfirmPinVisible(!confirmPinVisible) }) {
                                Icon(
                                    if (confirmPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPinVisible) "Sembunyikan PIN" else "Tampilkan PIN"
                                )
                            }
                        },
                        visualTransformation = if (confirmPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFF00897B)
                        )
                    )

                    if (!isValid && confirmPin.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (pin.length < 4) "PIN minimal 4 angka" else "PIN tidak cocok",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { if (isValid) onSetPin(pin) },
                        enabled = isValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00897B),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        )
                    ) {
                        Text(
                            "Simpan PIN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
