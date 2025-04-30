package com.afian.tugasakhir.View.Screen.HelperScreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.afian.tugasakhir.Component.LottieSuccessAnimation
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.Controller.Screen
import com.afian.tugasakhir.Controller.UiState
import com.afian.tugasakhir.R
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    loginViewModel: LoginViewModel,
    navController: NavController
) {
    val user = loginViewModel.getUserData()
    val username = user?.user_name ?: "Pengguna"
    val identifier = user?.identifier ?: "Tidak ada ID"
    // Gunakan URL asli atau null jika kosong agar placeholder Coil bekerja
    val fotoProfileUrl = user?.foto_profile.takeIf { !it.isNullOrBlank() }

    val logoutState by loginViewModel.logoutState.collectAsState()
    var showLogoutLottie by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // State contoh untuk toggle notifikasi
    var notificationEnabled by remember { mutableStateOf(true) }

    // LaunchedEffect untuk handle logout (Tidak Berubah - Sudah Benar)
    LaunchedEffect(logoutState) {
        when (logoutState) {
            is UiState.Success -> {
                showLogoutLottie = true // Tampilkan animasi
                Log.d("UserSettingsScreen", "Logout Success state detected. Showing Lottie...")
                // Tunggu animasi selesai, lalu reset state logout di ViewModel
                delay(2500L) // Beri waktu sedikit lebih lama untuk Lottie
                Log.d("UserSettingsScreen", "Lottie delay finished. Resetting logout state.")
                loginViewModel.resetLogoutStateToIdle() // Reset state logout
                // --- HAPUS SEMUA NAVIGASI DARI SINI ---
                // loginViewModel.resetLoginStateToIdle() // Reset login state bisa dipindah ke NavGraph jika perlu
                showLogoutLottie = false // Sembunyikan Lottie setelah reset state
            }
            is UiState.Error -> {
                showLogoutLottie = false
                val errorMessage = (logoutState as UiState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                loginViewModel.resetLogoutStateToIdle() // Reset state error logout
            }
            is UiState.Idle -> { showLogoutLottie = false }
            is UiState.Loading -> { showLogoutLottie = false }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Akun") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) { // Gunakan navigateUp
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { paddingValues -> // Terima paddingValues dari Scaffold

        // Box utama untuk memungkinkan overlay Lottie
        Box(modifier = Modifier.fillMaxSize()) {

            // Konten Utama dengan Padding dari Scaffold
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // <-- TERAPKAN PADDING SCAFFOLD
                        // Padding tambahan bisa ditambahkan di sini atau per elemen
                        // .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally // Pusatkan konten utama (Foto, Nama, ID)
            ) {
                // --- HAPUS HEADER KUSTOM ---

                Spacer(modifier = Modifier.height(24.dp)) // Jarak dari AppBar

                // Foto Profil
                Image(
                    painter = rememberAsyncImagePainter( // <-- Gunakan Async
                        model = fotoProfileUrl, // URL atau null
                        error = painterResource(id = R.drawable.placeholder_image_24), // Placeholder jika error/null
                        placeholder = painterResource(id = R.drawable.placeholder_image_24) // Placeholder saat loading
                    ),
                    contentDescription = "Foto Profil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp) // Ukuran bisa lebih besar
                        .clip(CircleShape)
                        .border(1.dp, Color.Gray, CircleShape) // Border tipis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Username
                Text(
                    text = username,
                    style = MaterialTheme.typography.headlineSmall
                )

                // Identifier
                Text(
                    text = identifier,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp)) // Jarak ke menu

                // --- Menu Options (Gunakan Column + Item Menu Kustom) ---
                Column(modifier = Modifier.padding(horizontal = 8.dp)) { // Beri padding horizontal untuk menu

                    SettingsMenuItem(
                        text = "Informasi Personal",
                        iconVector = Icons.Default.Person,
                        onClick = { navController.navigate(Screen.UserProfileEdit.route) }
                    )
                    Divider(modifier=Modifier.padding(horizontal=8.dp)) // Pemisah

                    // Item menu dengan Toggle
                    SettingsMenuToggleItem(
                        text = "Notifikasi",
                        iconVector = Icons.Default.Notifications,
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it /* TODO: Simpan state? */ }
                    )
                    Divider(modifier=Modifier.padding(horizontal=8.dp))

                    SettingsMenuItem(
                        text = "Bahasa",
                        iconVector = Icons.Default.MailOutline,
                        onClick = { /* TODO: Navigasi ke layar bahasa */ }
                    )
                    Divider(modifier=Modifier.padding(horizontal=8.dp))

                    SettingsMenuItem(
                        text = "Maps Debug",
                        // Jika pakai drawable resource
                        painter = painterResource(R.drawable.ic_map_24),
                        onClick = { navController.navigate(Screen.DebugMaps.route) }
                    )
                    Divider(modifier=Modifier.padding(horizontal=8.dp))

                    // Item menu untuk Logout
                    SettingsMenuItem(
                        text = "Logout",
                        iconVector = Icons.AutoMirrored.Filled.ExitToApp, // Icon auto-mirrored
                        enabled = logoutState !is UiState.Loading,
                        onClick = { loginViewModel.logout() },
                        contentColor = MaterialTheme.colorScheme.error, // Warna merah
                        isLoading = logoutState is UiState.Loading // Kirim status loading
                    )

                } // Akhir Column Menu
            } // Akhir Column Konten Utama

            // Tampilkan Lottie Sukses secara overlay jika state lokal true
            // (Tidak berubah)
            if (showLogoutLottie) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    LottieSuccessAnimation()
                }
            }

        } // Akhir Box Utama (untuk overlay)
    } // Akhir Scaffold
}

@Composable
fun SettingsMenuItem(
    modifier: Modifier = Modifier,
    text: String,
    iconVector: ImageVector? = null, // Ikon dari Material Icons
    painter: Painter? = null, // Ikon dari Painter Resource
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    contentColor: Color = LocalContentColor.current // Warna default teks & ikon
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp) // Tinggi minimum standar item list
            .clickable(enabled = enabled && !isLoading, onClick = onClick) // Klik pada Row
            .padding(horizontal = 16.dp, vertical = 8.dp), // Padding standar item list
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tampilkan ikon jika ada
        when {
            isLoading -> { // Prioritaskan loading indicator
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = contentColor)
                Spacer(modifier = Modifier.width(16.dp))
            }
            iconVector != null -> {
                Icon(imageVector = iconVector, contentDescription = text, tint = contentColor)
                Spacer(modifier = Modifier.width(16.dp))
            }
            painter != null -> {
                Icon(painter = painter, contentDescription = text, tint = contentColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
            }
            else -> {
                // Beri ruang kosong seukuran ikon jika tidak ada ikon
                Spacer(modifier = Modifier.width(24.dp + 16.dp))
            }
        }

        // Teks Menu (ambil sisa ruang)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = contentColor
        )

        // Tampilkan panah kanan jika bisa diklik dan bukan loading
        if (!isLoading && onClick != {}) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.6f)
            )
        }
    }
}

/** Item menu dengan Switch toggle */
@Composable
fun SettingsMenuToggleItem(
    modifier: Modifier = Modifier,
    text: String,
    iconVector: ImageVector? = null,
    painter: Painter? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            // Klik pada Row juga bisa toggle, atau hanya pada Switch
            .clickable(enabled = enabled, onClick = { onCheckedChange(!checked) })
            .padding(horizontal = 16.dp, vertical = 4.dp), // Padding vertikal lebih kecil untuk toggle
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikon
        when {
            iconVector != null -> {
                Icon(imageVector = iconVector, contentDescription = text)
                Spacer(modifier = Modifier.width(16.dp))
            }
            painter != null -> {
                Icon(painter = painter, contentDescription = text, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
            }
            else -> { Spacer(modifier = Modifier.width(24.dp + 16.dp)) }
        }
        // Teks
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        // Switch Toggle
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

