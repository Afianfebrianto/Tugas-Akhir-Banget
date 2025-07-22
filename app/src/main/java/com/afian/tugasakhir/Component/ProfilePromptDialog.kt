package com.afian.tugasakhir.Component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ProfilePromptDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Ikon Profil") },
        title = { Text(text = "Lengkapi Profil Anda") },
        text = { Text("Foto profil atau nomor HP Anda sepertinya belum lengkap. Mohon lengkapi data Anda.") },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text("Lengkapi Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Nanti Saja")
            }
        }
    )
}