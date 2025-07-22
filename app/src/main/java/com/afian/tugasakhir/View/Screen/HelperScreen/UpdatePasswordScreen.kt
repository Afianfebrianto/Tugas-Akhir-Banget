package com.afian.tugasakhir.View.Screen.HelperScreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.Controller.Screen
import com.afian.tugasakhir.Controller.UiState
import com.afian.tugasakhir.Controller.UpdatePasswordViewModel
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.afian.tugasakhir.R // Import R untuk ikon visibility
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePasswordScreen(
    navController: NavController,
    identifier: String, // Terima identifier dari NavGraph
    loginViewModel: LoginViewModel, // Untuk dapat role setelah sukses update
    role: String,
    viewModel: UpdatePasswordViewModel = viewModel() // Gunakan ViewModel baru
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // State dari ViewModel
    val newPassword by viewModel.newPassword
    val confirmPassword by viewModel.confirmPassword
    val passwordVisible by viewModel.passwordVisible
    val updateState by viewModel.updateState
    val validationError by viewModel.validationError

    // Lottie Composition
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.password))

    // Efek untuk navigasi setelah sukses update password
    LaunchedEffect(updateState) {
        if (updateState is UiState.Success<*>) {
            Toast.makeText(context, "Password berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            delay(1000L) // Beri waktu user baca toast

            // Ambil role dari LoginViewModel menggunakan identifier yg disimpan
            // atau idealnya, API update password mengembalikan data user lengkap
            val user = loginViewModel.getUserData() // Ambil data user terakhir yg login
//            val role = if (user?.identifier == identifier) user.role else loginViewModel.getUserRole() // Dapatkan role

            val destination = when (role) { // Navigasi ke home sesuai role
                "dosen" -> Screen.HomeDosen.route
                "mhs" -> Screen.HomeMahasiswa.route
                "admin" -> Screen.HomeAdmin.route
                else -> Screen.Login.route // Fallback ke login jika role tidak ditemukan
            }
            Log.i("UpdatePasswordScreen", "Password updated, navigating to $destination")
            navController.navigate(destination) {
                // Hapus semua backstack sampai awal grafik
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
            if (destination != Screen.Login.route) { // Jangan navigasi ke login lagi
                val startDestinationId = navController.graph.findStartDestination().id
                Log.d("UpdatePasswordScreen", "Navigating to '$destination', popping up to ID $startDestinationId")
                navController.navigate(destination) { // Navigasi ke home
                    popUpTo(startDestinationId) { inclusive = true } // Clear semua stack
                    launchSingleTop = true
                }
            } else {
                Log.e("UpdatePasswordScreen", "Cannot determine home destination, navigating back to Login. Role: $role")
                navController.navigate(destination) {  }
            }
        } else if (updateState is UiState.Error) {
            // Tampilkan error API menggunakan Toast
            Toast.makeText(context, (updateState as UiState.Error).message, Toast.LENGTH_LONG).show()
            // State akan kembali ke Idle oleh ViewModel
        }
    }

    Scaffold(
        containerColor = Color(0xFFFFD369),
//        topBar = { TopAppBar(title = { Text("Update Password Default") }) }
        // Tidak ada navigation icon agar user tidak bisa skip
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp), // Padding lebih besar
            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center // Pusatkan form
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever, // Agar animasi berulang terus
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Buat Password Baru",
                style = MaterialTheme.typography.headlineSmall, // Dibuat lebih besar
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Update password untuk mengamankan akun Anda.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp) // Beri sedikit padding agar tidak terlalu lebar
            )

            Spacer(modifier = Modifier.height(32.dp)) // Jarak lebih besar ke form

            // Field Password Baru
            OutlinedTextField(
                value = newPassword,
                onValueChange = { viewModel.newPassword.value = it },
                label = { Text("Password Baru") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { viewModel.passwordVisible.value = !passwordVisible }) {
                        Icon(painterResource(if (passwordVisible) R.drawable.visibility_24px else R.drawable.visibility_off_24px), "")
                    }
                },
                isError = validationError != null, // Error jika ada pesan validasi
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Field Konfirmasi Password Baru
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { viewModel.confirmPassword.value = it },
                label = { Text("Konfirmasi Password Baru") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { viewModel.passwordVisible.value = !passwordVisible }) {
                        Icon(painterResource(if (passwordVisible) R.drawable.visibility_24px else R.drawable.visibility_off_24px), "")
                    }
                },
                isError = validationError != null, // Error jika ada pesan validasi
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.updatePassword() // Panggil update saat 'Done'
                }),
                modifier = Modifier.fillMaxWidth()
            )

            // Tampilkan pesan error validasi jika ada
            validationError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Simpan
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.updatePassword()
                },
                enabled = updateState !is UiState.Loading, // Disable saat loading
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3E62))

            ) {
                if (updateState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize), strokeWidth = 2.dp)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Menyimpan...")
                } else {
                    Text("Simpan Password Baru")
                }
            }
        } // Akhir Column
    } // Akhir Scaffold
}
