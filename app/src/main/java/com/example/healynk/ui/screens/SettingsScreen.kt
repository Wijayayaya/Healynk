package com.example.healynk.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.healynk.R
import com.example.healynk.viewmodel.UiState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun SettingsScreen(
    uiState: UiState,
    onSignOut: () -> Unit,
    onRemovePin: () -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onUpdatePhotoUrl: (String?) -> Unit,
    onUpdateEmail: (String) -> Unit,
    onUpdatePassword: (String) -> Unit,
    onUploadPhoto: (Uri) -> Unit,
    onUpdateBurnGoal: (Int) -> Unit,
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(onUploadPhoto)
    }

    var showNameDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var burnGoalInput by remember { mutableStateOf(uiState.dailyCaloriesGoal.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { Text("Settings", style = MaterialTheme.typography.headlineSmall) }
        item {
            CardSection(title = "Profil") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(photoUrl = uiState.photoUrl)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = uiState.displayName.ifBlank { "Pengguna" },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(uiState.userEmail.ifBlank { "Belum ada email" })
                    }
                }
                SettingsActionRow(
                    title = "Ubah username",
                    subtitle = "Perbarui nama profil"
                ) {
                    nameInput = uiState.displayName
                    showNameDialog = true
                }
                SettingsActionRow(
                    title = "Ubah foto profil",
                    subtitle = "Ambil dari galeri"
                ) {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                SettingsActionRow(
                    title = "Ubah email",
                    subtitle = "Ganti alamat login"
                ) {
                    emailInput = uiState.userEmail
                    showEmailDialog = true
                }
                SettingsActionRow(
                    title = "Ubah password",
                    subtitle = "Setel ulang kata sandi"
                ) {
                    passwordInput = ""
                    showPasswordDialog = true
                }
            }
        }
        item {
            CardSection(title = "Keamanan") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("PIN lokal", fontWeight = FontWeight.Bold)
                        Text(if (uiState.hasPin) "PIN aktif" else "Belum ada PIN")
                    }
                    TextButton(onClick = onRemovePin, enabled = uiState.hasPin) {
                        Text("Hapus PIN")
                    }
                }
            }
        }
        item {
            CardSection(title = "Target Kalori Terbakar") {
                OutlinedTextField(
                    value = burnGoalInput,
                    onValueChange = { text ->
                        val sanitized = text.filter { it.isDigit() }
                        burnGoalInput = sanitized
                        sanitized.toIntOrNull()?.let(onUpdateBurnGoal)
                    },
                    label = { Text("Target harian (kcal)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
        item {
            uiState.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
        item {
            Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Logout")
            }
        }
    }

    if (showNameDialog) {
        EditValueDialog(
            title = "Ubah username",
            value = nameInput,
            onValueChange = { nameInput = it },
            confirmLabel = "Simpan"
        ) {
            if (nameInput.isNotBlank()) {
                onUpdateDisplayName(nameInput)
            }
            showNameDialog = false
        }
    }
    if (showEmailDialog) {
        EditValueDialog(
            title = "Ubah email",
            value = emailInput,
            onValueChange = { emailInput = it },
            confirmLabel = "Simpan"
        ) {
            if (emailInput.isNotBlank()) {
                onUpdateEmail(emailInput)
            }
            showEmailDialog = false
        }
    }
    if (showPasswordDialog) {
        EditValueDialog(
            title = "Ubah password",
            value = passwordInput,
            onValueChange = { passwordInput = it },
            confirmLabel = "Simpan",
            isPassword = true
        ) {
            if (passwordInput.length >= 6) {
                onUpdatePassword(passwordInput)
            }
            showPasswordDialog = false
        }
    }
}

@Composable
private fun CardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun ProfileAvatar(photoUrl: String?) {
    if (photoUrl.isNullOrBlank()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_healynk_logo),
            contentDescription = "Avatar",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
    } else {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onClick) {
            Text("Edit")
        }
    }
}

@Composable
private fun EditValueDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    confirmLabel: String,
    isPassword: Boolean = false,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        title = { Text(title) },
        text = {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                keyboardOptions = if (isPassword) KeyboardOptions.Default else KeyboardOptions.Default,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel) }
        }
    )
}
