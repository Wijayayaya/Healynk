package com.example.healynk.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegister: (String, String) -> Unit,
    onNavigateLogin: () -> Unit,
    onRegistrationConsumed: () -> Unit,
    errorMessage: String?,
    registrationSuccess: Boolean
) {
    val (email, setEmail) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }
    val (confirmPassword, setConfirmPassword) = remember { mutableStateOf("") }
    val (passwordVisible, setPasswordVisible) = remember { mutableStateOf(false) }
    val (confirmPasswordVisible, setConfirmPasswordVisible) = remember { mutableStateOf(false) }
    val (localError, setLocalError) = remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            snackbarHostState.showSnackbar("Pendaftaran berhasil, silakan login")
            onRegistrationConsumed()
            onNavigateLogin()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Daftar akun Healynk", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = email,
                onValueChange = setEmail,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    setPassword(it)
                    setLocalError(null)
                },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                trailingIcon = {
                    IconButton(onClick = { setPasswordVisible(!passwordVisible) }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password"
                        )
                    }
                }
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    setConfirmPassword(it)
                    setLocalError(null)
                },
                label = { Text("Konfirmasi Password") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                trailingIcon = {
                    IconButton(onClick = { setConfirmPasswordVisible(!confirmPasswordVisible) }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Sembunyikan password" else "Tampilkan password"
                        )
                    }
                }
            )
            val errorToShow = localError ?: errorMessage
            errorToShow?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        setLocalError("Password tidak cocok")
                    } else {
                        setLocalError(null)
                        onRegister(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Daftar")
            }
            TextButton(
                onClick = onNavigateLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Sudah punya akun? Masuk")
            }
        }
    }
}
