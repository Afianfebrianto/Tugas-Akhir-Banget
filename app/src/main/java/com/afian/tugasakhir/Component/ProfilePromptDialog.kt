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
    onConfirm: () -> Unit // Aksi untuk pindah ke layar edit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Ikon Profil") }, // Ganti ikon jika perlu
        title = { Text(text = "Lengkapi Profil Anda") },
        text = { Text("Foto profil atau nomor HP Anda sepertinya belum lengkap. Mohon lengkapi data Anda.") },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm() // Jalankan aksi konfirmasi (navigasi)
                    // onDismissRequest() // Dialog akan otomatis dismiss saat onConfirm dipanggil di pemanggil? Atau panggil di sini juga? Umumnya cukup di pemanggil.
                }
            ) {
                Text("Lengkapi Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { // Panggil dismiss saat batal
                Text("Nanti Saja")
            }
        }
    )
}