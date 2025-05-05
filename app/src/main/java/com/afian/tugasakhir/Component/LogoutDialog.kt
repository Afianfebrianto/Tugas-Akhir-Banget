package com.afian.tugasakhir.Component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

// --- 👇 Composable Baru untuk Dialog Konfirmasi 👇 ---
@Composable
fun LogoutConfirmationDialog(
    onConfirmLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, // Panggil onDismiss jika user klik di luar dialog atau tombol back system
        title = { Text("Konfirmasi Logout") },
        text = { Text("Apakah Anda yakin ingin keluar dari akun ini?") },
        confirmButton = {
            TextButton(onClick = onConfirmLogout) { // Panggil onConfirmLogout saat tombol Logout diklik
                Text("Logout", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { // Panggil onDismiss saat tombol Batal diklik
                Text("Batal")
            }
        }
    )
}
// --- 👆 ---
