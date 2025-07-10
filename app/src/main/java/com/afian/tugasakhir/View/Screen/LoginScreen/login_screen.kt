package com.afian.tugasakhir.View.Screen.LoginScreen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.afian.tugasakhir.Component.ButtonAbu
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // Import Material 3
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.afian.tugasakhir.Component.LottieFailAnimation
import com.afian.tugasakhir.Component.LottieSuccessAnimation
import com.afian.tugasakhir.Controller.LoginUiState
import com.afian.tugasakhir.Controller.Screen
import kotlinx.coroutines.delay
private const val LOGIN_TAG = "LoginScreen"

@Composable
fun LoginScreen(viewModel: LoginViewModel, navController: NavController) {
    Log.d(LOGIN_TAG, "Composing...")
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val defaultTextColor = Color(0xFF1E3E62) // Ganti jika warna teks normal Anda berbeda
    val errorColor = MaterialTheme.colorScheme.error

    // Ambil status login dari ViewModel

    // Untuk memindahkan fokus saat tombol login ditekan
    val focusManager = LocalFocusManager.current

    // Ambil UI State dari ViewModel
    val loginUiState by viewModel.loginUiState.collectAsState()

    // Variabel untuk menyimpan pesan error dari state
    val errorMessage = remember(loginUiState) {
        (loginUiState as? LoginUiState.Error)?.message
    }
    val isError = errorMessage != null
    val context = LocalContext.current

    // --- Efek untuk Navigasi atau Reset State ---
    LaunchedEffect(loginUiState) {
        Log.d(LOGIN_TAG, "LaunchedEffect: loginUiState changed to -> $loginUiState")

        // Gunakan 'when' untuk menangani semua kemungkinan state
        when (val state = loginUiState) { // Assign state ke variabel lokal 'state'

            is LoginUiState.PasswordUpdateRequired -> {
                Log.i(LOGIN_TAG, "State is PasswordUpdateRequired. Preparing navigation...")
                val user = state.user // Akses user dari 'state'
                // Validasi identifier sebelum navigasi
                if (user.identifier.isBlank() && user.role.isBlank()) {
                    Log.e(LOGIN_TAG, "Cannot navigate to update password: Identifier is blank!")
                    Toast.makeText(context, "Error: Identifier pengguna tidak valid.", Toast.LENGTH_LONG).show()
                    viewModel.resetLoginStateToIdle() // Reset state jika identifier tidak valid
                    return@LaunchedEffect // Hentikan efek ini
                }

                // Pastikan route update password Anda didefinisikan dengan argumen
                // Contoh: Screen.UpdatePassword.route = "update_password/{identifier}"
                val baseRoute = Screen.UpdatePassword.route
                if (!baseRoute.contains("{identifier}")) {
                    Log.e(LOGIN_TAG,"Route ${Screen.UpdatePassword.route} harus mengandung {identifier}!")
                    Toast.makeText(context, "Error: Konfigurasi route salah.", Toast.LENGTH_LONG).show()
                    viewModel.resetLoginStateToIdle()
                    return@LaunchedEffect
                }
                // Buat route tujuan dengan mengganti placeholder
                val destinationRoute = baseRoute.replace("{identifier}", user.identifier)
                Log.d(LOGIN_TAG, "Calculated Destination Route for UpdatePassword: $destinationRoute")

                // Dapatkan ID start destination untuk popUpTo
                val startDestinationId = navController.graph.findStartDestination().id
                Log.d(LOGIN_TAG, "Navigating to UpdatePassword. Popping up to Start Destination ID: $startDestinationId")

                if (user.identifier.isNotBlank() && user.role.isNotBlank()) { // Pastikan role juga tidak blank
                    val destinationRoute = Screen.UpdatePassword.route
                        .replace("{identifier}", user.identifier)
                        .replace("{role}", user.role)


                try {
                    // Langsung navigasi ke layar update password
                    navController.navigate(destinationRoute) {
                        // Hapus semua backstack sampai ke awal NavGraph
//                        viewModel.resetLoginStateToIdle()
                        popUpTo(startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                    Log.i(LOGIN_TAG, "Navigation to UpdatePassword attempted.")
                } catch (e: Exception) {
                    Log.e(LOGIN_TAG, "Error during navigation to UpdatePassword: ${e.message}", e)
                    Toast.makeText(context, "Gagal membuka halaman update password.", Toast.LENGTH_LONG).show()
                    viewModel.resetLoginStateToIdle() // Reset jika navigasi gagal
                }
                    viewModel.resetLoginStateToIdle()
                } else {Log.e("LoginScreen","elses identifiernull and role null")}
                // JANGAN reset state UI login di sini, biarkan UpdatePasswordScreen yang aktif
            }

            is LoginUiState.Success -> {
                Log.i(LOGIN_TAG, "State is Success. Starting 6s delay for navigation...") // Delay 6 detik
                delay(1000L) // Tunggu animasi sukses
                val user = state.user // Akses user dari 'state'
                // Tentukan tujuan berdasarkan role menggunakan Screen object
                val destination = when (user.role) {
                    "dosen" -> Screen.HomeDosen.route
                    "mhs" -> Screen.HomeMahasiswa.route
                    "admin" -> Screen.HomeAdmin.route // Pastikan Screen.HomeAdmin.route ada
                    else -> null
                }

                if (destination != null) {
                    Log.i(LOGIN_TAG, "Delay finished. Navigating to '$destination'")
                    val startDestinationId = navController.graph.findStartDestination().id
                    navController.navigate(destination) {
                        // Hapus semua backstack sampai ke awal NavGraph
                        popUpTo(startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                    // Anda bisa reset state login di sini jika mau, atau biarkan sampai logout
                    // viewModel.resetLoginStateToIdle()
                } else {
                    Log.e(LOGIN_TAG, "Delay finished. Unknown role '${user.role}'. Resetting state.")
                    Toast.makeText(context, "Role pengguna tidak dikenal.", Toast.LENGTH_SHORT).show()
                    viewModel.resetLoginStateToIdle() // Kembali ke form jika role aneh
                }
            }

            is LoginUiState.Error -> {
                Log.w(LOGIN_TAG, "State is Error. Starting 4s delay for reset...")
                delay(1000L) // Tunggu animasi error / pesan tampil
                Log.d(LOGIN_TAG, "Error reset delay finished. Calling resetLoginStateToIdle().")
                viewModel.resetLoginStateToIdle() // Kembali ke state Idle (form)
            }

            is LoginUiState.Idle -> {
                Log.d(LOGIN_TAG, "State is Idle. No navigation/reset effect.")
                // Tidak melakukan apa-apa
            }

            is LoginUiState.Loading -> {
                Log.d(LOGIN_TAG, "State is Loading. No navigation/reset effect.")
                // Tidak melakukan apa-apa
            }
        } // Akhir when
    } // Akhir LaunchedEffect
    // --- Akhir Efek Navigasi ---


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFD369)),
        contentAlignment = Alignment.Center // Memposisikan semuanya di tengah
    ) {
        // Gunakan when untuk menampilkan UI berdasarkan state
        when (loginUiState) {
            is LoginUiState.PasswordUpdateRequired ->{
                Text("Update Password Require")
            }
            is LoginUiState.Loading -> {
                // Tampilkan Indikator Loading
                CircularProgressIndicator(color = Color(0xFF1E3E62))
                // Atau bisa juga Lottie loading jika punya
            }
            is LoginUiState.Success -> {
                // Tampilkan Animasi Lottie Sukses
                LottieSuccessAnimation()
            }
            is LoginUiState.Error -> {
                // Tampilkan Animasi Lottie Gagal dan Pesan Error
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LottieFailAnimation()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (loginUiState as LoginUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    // Opsional: Tambah tombol "Coba Lagi" untuk reset state
                    // Button(onClick = { viewModel.resetLoginState() }) { Text("Coba Lagi") }
                }
            }

            is LoginUiState.Idle -> {
        Column(
            modifier = Modifier.fillMaxWidth(), // Mengisi lebar penuh
            horizontalAlignment = Alignment.CenterHorizontally // Memposisikan kolom di tengah horizontal
        )   {
            Image(
                painter = painterResource(id = R.drawable.logowithshadow), // Ganti dengan nama file gambar
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp) // Sesuaikan ukuran gambar
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Dosen")
                    }
                    append("Tracker")
                },
                color = Color(0xFF1E3E62), // Warna teks 1E3E62
                fontSize = 24.sp, // Ukuran font
                modifier = Modifier.padding(top = 8.dp) // Jarak antara Lottie dan teks
            )

            Spacer(modifier = Modifier.height(40.dp))
            // Teks Login yang di tengah
            Text(
                text = "Login",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    color = Color(0xFF1E3E62),
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            TextField(
                value = username,
                onValueChange = {
                    username = it
                },
                placeholder = { Text(text = "Username / NIDN / NIM") }, // Kembali pakai placeholder
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                // Gunakan TextFieldDefaults dari M3
                colors = TextFieldDefaults.colors(
                    // Atur warna teks berdasarkan kondisi error
                    focusedTextColor = if (isError) errorColor else defaultTextColor,
                    unfocusedTextColor = if (isError) errorColor else defaultTextColor,
                    // Atur warna placeholder jika perlu (opsional)
                    focusedPlaceholderColor = if (isError) errorColor.copy(alpha = 0.7f) else Color.Gray,
                    unfocusedPlaceholderColor = if (isError) errorColor.copy(alpha = 0.7f) else Color.Gray,
                    // Atur warna cursor (opsional)
                    cursorColor = if (isError) errorColor else MaterialTheme.colorScheme.primary,
                    // Tetap buat indicator transparan
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    // Atur warna container (background) jika perlu
                    focusedContainerColor = Color.White, // Warna background default textfield
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    errorContainerColor = Color.White // Atur warna background saat error jika ingin beda
                ),
                // Tetap teruskan flag isError (meskipun visualnya kita atur manual via colors)
                isError = false,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 10.dp)
            )
            // --- TextField Password ---
            // --- TextField Password ---
            TextField(
                value = password,
                onValueChange = {
                    password = it
                },
                placeholder = { Text(text = "Password") }, // Kembali pakai placeholder
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                // Atur warna berdasarkan error
                colors = TextFieldDefaults.colors(
                    focusedTextColor = if (isError) errorColor else defaultTextColor,
                    unfocusedTextColor = if (isError) errorColor else defaultTextColor,
                    focusedPlaceholderColor = if (isError) errorColor.copy(alpha = 0.7f) else Color.Gray,
                    unfocusedPlaceholderColor = if (isError) errorColor.copy(alpha = 0.7f) else Color.Gray,
                    cursorColor = if (isError) errorColor else MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    errorContainerColor = Color.White
                ),
                // Tetap teruskan flag isError
                isError = false,
                trailingIcon = {
                    val image = if (passwordVisible)
                        painterResource(id = R.drawable.visibility_24px)
                    else
                        painterResource(id = R.drawable.visibility_off_24px)

                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = image, contentDescription = description) // Pastikan ini ada
                    }
                },
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.login(username, password)
                }),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 5.dp)
            )

            Spacer(modifier = Modifier.height(26.dp))
            // Button "Masuk" di sebelah kanan
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.Absolute.Center // Posisikan tombol di kanan
            ) {
                ButtonAbu(
                    text = "Masuk",
                    modifier = Modifier.width(180.dp),
                    onClick = {
                        focusManager.clearFocus()
                        // Langsung panggil login
                        viewModel.login(username, password)
                    }
                )
            }
        }
                }
        }
    }
}
