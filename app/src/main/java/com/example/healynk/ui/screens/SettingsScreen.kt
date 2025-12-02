package com.example.healynk.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.healynk.R
import com.example.healynk.viewmodel.UiState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues

private const val MAX_BURN_GOAL_VALUE = 9999
private const val MAX_BURN_GOAL_DIGITS = 4

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00897B),
                        Color(0xFF00897B)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text(
                    text = "Pengaturan",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                CardSection(title = "Profil") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        ProfileAvatar(photoUrl = uiState.photoUrl)
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = uiState.displayName.ifBlank { "Pengguna" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2C2C)
                            )
                            Text(
                                text = uiState.userEmail.ifBlank { "Belum ada email" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                SettingsActionRow(
                    title = "Ubah nama pengguna",
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
                    title = "Ubah kata sandi",
                    subtitle = "Setel ulang kata sandi"
                ) {
                    passwordInput = ""
                    showPasswordDialog = true
                }
            }
        }
            item {
                CardSection(title = "Keamanan") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PIN Lokal",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2C2C),
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (uiState.hasPin) "PIN aktif" else "Belum ada PIN",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666)
                            )
                        }
                        TextButton(
                            onClick = onRemovePin,
                            enabled = uiState.hasPin
                        ) {
                            Text(
                                text = "Hapus PIN",
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.hasPin) Color(0xFF00897B) else Color(0xFFCCCCCC)
                            )
                        }
                    }
                }
            }
            item {
                uiState.error?.let {
                    Card(
                        colors = CardDefaults.cardColors(Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = it,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = "Keluar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }

    if (showNameDialog) {
        EditValueDialog(
            title = "Ubah nama pengguna",
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
            title = "Ubah kata sandi",
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
    Card(
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00897B)
            )
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            content()
        }
    }
}

@Composable
private fun ProfileAvatar(photoUrl: String?) {
    if (photoUrl.isNullOrBlank()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo_healynk),
            contentDescription = "Foto profil",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )
    } else {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Foto profil",
            modifier = Modifier
                .size(64.dp)
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
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C),
                fontSize = 16.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF666666)
            )
        }
        TextButton(onClick = onClick) {
            Text(
                text = "Ubah",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00897B)
            )
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
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C)
            )
        },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                keyboardOptions = if (isPassword) KeyboardOptions.Default else KeyboardOptions.Default,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = Color(0xFF00897B)
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00897B)
                )
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
