package com.afian.tugasakhir.View.Screen.HelperScreen

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.afian.tugasakhir.Controller.LoginViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.afian.tugasakhir.Component.MyCustomTopAppBar
import com.afian.tugasakhir.Controller.Screen
import com.afian.tugasakhir.R
import com.afian.tugasakhir.Controller.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileEditScreen(
    navController: NavController,
    loginViewModel: LoginViewModel, // Untuk mendapatkan user ID awal
    viewModel: UserProfileViewModel = viewModel() // ViewModel untuk layar ini
) {
    val context = LocalContext.current
    val loggedInUserId = loginViewModel.getUserData()?.user_id

    // Ambil state dari ViewModel
    val profileData by viewModel.profileData.collectAsState()
    val noHpInput by viewModel.noHpInput
    val informasiInput by viewModel.informasiInput
    val selectedImageUri by viewModel.selectedImageUri
    val isLoading by viewModel.isLoading
    val isSaving by viewModel.isSaving
    val saveResult by viewModel.saveResult

    // Muat data profil saat layar pertama kali dibuat
    LaunchedEffect(key1 = loggedInUserId) {
        if (loggedInUserId != null && loggedInUserId != -1) {
            viewModel.loadUserProfile(loggedInUserId)
        } else {
            // Handle jika user ID tidak ada (seharusnya tidak terjadi jika layar ini diakses setelah login)
            Log.e("UserProfileEditScreen", "User ID is invalid or null.")
            Toast.makeText(context, "Gagal memuat profil: User tidak valid.", Toast.LENGTH_LONG).show()
            navController.navigateUp() // Kembali jika tidak ada user ID
        }
    }

    // Tampilkan hasil simpan (Toast)
    LaunchedEffect(saveResult) {
        saveResult?.let { (success, message) ->
            Toast.makeText(context, message, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
            viewModel.clearSaveResult() // Hapus pesan setelah ditampilkan
            if (success) {
                // Opsional: Kembali ke layar sebelumnya setelah sukses?
                // navController.navigateUp()
            }
        }
    }

    // --- 👇 REAKSI TERHADAP EVENT SUKSES UPDATE 👇 ---
    LaunchedEffect(Unit) { // Cukup subscribe sekali
        Log.d("UserProfileEditScreen", ">>> [CollectEffect] Mulai setup collection untuk updateSuccessEvent...") // Log setup
        try {
            viewModel.updateSuccessEvent.collect { updatedProfileData ->
                Log.i("UserProfileEditScreen", ">>> [CollectEffect] BERHASIL COLLECT event! Data: $updatedProfileData") // Log event diterima

                // 1. Panggil fungsi di LoginViewModel
                Log.d("UserProfileEditScreen", ">>> [CollectEffect] Memanggil updateLocalUserData...")
                loginViewModel.updateLocalUserData(updatedProfileData)
                Log.d("UserProfileEditScreen", ">>> [CollectEffect] Selesai updateLocalUserData.")

                // --- Tentukan Route Tujuan ---
                val userRole = loginViewModel.getUserData()?.role
                Log.d("UserProfileEditScreen", ">>> [CollectEffect] Role dari LoginViewModel: $userRole") // Log role

                val destinationRoute = when (userRole?.lowercase()) {
                    "admin" -> Screen.HomeAdmin.route
                    "dosen" -> Screen.HomeDosen.route
                    "mhs" -> Screen.HomeMahasiswa.route
                    else -> {
                        Log.e("UserProfileEditScreen", ">>> [CollectEffect] Role tidak dikenali: '$userRole'. Navigasi ke login.")
                        Screen.Login.route
                    }
                }
                Log.d("UserProfileEditScreen", ">>> [CollectEffect] Ditentukan Destination Route: $destinationRoute") // Log route

                // 2. Navigasi
                Log.d("UserProfileEditScreen", ">>> [CollectEffect] Mencoba navigasi ke: $destinationRoute") // Log sebelum navigasi
                try {
                    navController.navigate(destinationRoute) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                    Log.i("UserProfileEditScreen", ">>> [CollectEffect] Panggilan navigasi ke '$destinationRoute' berhasil.") // Log setelah panggil navigate
                } catch (navError: Exception) {
                    // Log error navigasi secara spesifik
                    Log.e("UserProfileEditScreen", "!!! [CollectEffect] GAGAL NAVIGASI ke '$destinationRoute' !!!", navError)
                }
            }
        } catch (collectError: Exception) {
            // Log jika ada error saat proses collect itu sendiri
            Log.e("UserProfileEditScreen", "!!! [CollectEffect] Error saat collect updateSuccessEvent !!!", collectError)
        }
    }

    // Launcher untuk memilih gambar (Photo Picker modern)
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia() // Kontrak baru
    ) { uri: Uri? ->
        viewModel.onImageSelected(uri) // Update ViewModel dengan URI terpilih
    }


    Scaffold(
        topBar = {
            MyCustomTopAppBar(title = "Edit Informasi Personal", navController)
//            TopAppBar(
//                title = { Text("Edit Informasi Personal") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.navigateUp() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
//                    }
//                }
//            )
        }
    ) { innerPadding ->
        // Tampilkan loading utama jika data profil belum ada
        if (isLoading && profileData == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (profileData == null) {
            // Tampilkan pesan jika data profil gagal dimuat total
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Gagal memuat data profil.", modifier = Modifier.padding(16.dp))
            }
        }
        // Tampilkan form jika data profil sudah ada
        else {
            // Gunakan data dari state ViewModel
            val currentProfile = profileData!! // Aman karena sudah dicek null

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()) // Buat bisa scroll
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto Profil (Bisa Diklik untuk Ganti)
                Box(contentAlignment = Alignment.BottomEnd) { // Box untuk overlay ikon edit
                    Image(
                        painter = rememberAsyncImagePainter(
                            // Tampilkan gambar terpilih JIKA ADA, jika tidak tampilkan dari data profil
                            model = selectedImageUri ?: currentProfile.foto_profile,
                            error = painterResource(id = R.drawable.placeholder_image_24),
                            placeholder = painterResource(id = R.drawable.placeholder_image_24)
                        ),
                        contentDescription = "Foto Profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Gray, CircleShape)
                            .clickable {
                                // Luncurkan Photo Picker
                                pickMediaLauncher.launch(
                                    // Minta hanya tipe gambar
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    )
                    // Ikon kecil untuk menandakan bisa diedit (opsional)
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24), // Ganti dengan ikon edit
                        contentDescription = "Edit Foto",
                        modifier = Modifier
                            .size(30.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                // Nama (Read Only)
                Text(
                    text = currentProfile.user_name,
                    style = MaterialTheme.typography.headlineSmall
                )
                // Identifier (Read Only)
                Text(
                    text = currentProfile.identifier,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input No HP
                OutlinedTextField(
                    value = noHpInput,
                    onValueChange = viewModel::onNoHpChanged, // Referensi fungsi
                    label = { Text("Nomor HP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Informasi
                OutlinedTextField(
                    value = informasiInput,
                    onValueChange = viewModel::onInformasiChanged, // Referensi fungsi
                    label = { Text("Informasi / Deskripsi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp), // Minimal tinggi untuk multi-baris
                    maxLines = 5 // Contoh batas baris
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Tombol Simpan
                Button(
                    onClick = { viewModel.saveProfile(context) },
                    enabled = !isSaving, // Disable saat menyimpan
                    modifier = Modifier.fillMaxWidth(0.7f) // Lebar tombol
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize), strokeWidth = 2.dp)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Menyimpan...")
                    } else {
                        Text("Simpan Perubahan")
                    }
                }
            } // Akhir Column konten utama
        } // Akhir Else (jika profileData tidak null)
    } // Akhir Scaffold
}