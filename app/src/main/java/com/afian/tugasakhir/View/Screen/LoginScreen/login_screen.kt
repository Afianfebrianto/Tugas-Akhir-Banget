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
import androidx.compose.ui.res.painterResource
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
import com.afian.tugasakhir.Component.LottieFailAnimation
import com.afian.tugasakhir.Component.LottieSuccessAnimation
import com.afian.tugasakhir.Controller.LoginUiState
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

    // --- Efek untuk Navigasi Setelah Sukses ---
    LaunchedEffect(loginUiState) {
        Log.d(LOGIN_TAG, "LaunchedEffect: loginUiState changed to -> $loginUiState")
        if (loginUiState is LoginUiState.Success) {
            Log.i(LOGIN_TAG, "LaunchedEffect: Success state detected. Starting 2s delay for navigation...")
            // Tunggu animasi sukses selesai (misal 2 detik)
            delay(6000L)
            val user = (loginUiState as LoginUiState.Success).user
            // Lakukan navigasi berdasarkan role
            val destination = when (user.role) {
                "dosen" -> "dosen"
                "mhs" -> "mahasiswa"
                "admin" -> "admin"
                else -> null // Role tidak dikenal, mungkin kembali ke login?
            }
            if (destination != null) {
                Log.i(LOGIN_TAG, "LaunchedEffect: Delay finished. Navigating to '$destination'")
                navController.navigate(destination) {
                    // Hapus stack login agar tidak bisa kembali dengan back button
                    popUpTo("welcome") { inclusive = true } // Asumsi route sebelum login adalah 'welcome'
                    launchSingleTop = true // Hindari instance ganda
                }
            } else {
                Log.e(LOGIN_TAG, "LaunchedEffect: Delay finished. Unknown role '${user.role}'. Resetting state.")
                Log.e("LoginScreen", "Unknown user role: ${user.role}. Cannot navigate.")
                // Mungkin tampilkan pesan error atau reset state
                viewModel.resetLoginStateToIdle() // Kembali ke form?
                Toast.makeText(context, "Role pengguna tidak dikenal.", Toast.LENGTH_SHORT).show()
            }
        }
        // Efek untuk reset state error setelah beberapa detik (opsional)
        else if (loginUiState is LoginUiState.Error) {
            Log.w(LOGIN_TAG, "LaunchedEffect: Error state detected. Starting 4s delay for reset...")
            delay(4000L) // Tunggu 4 detik setelah error tampil
            viewModel.resetLoginStateToIdle() // Kembali ke state Idle (form)
        }
    }
    // --- Akhir Efek Navigasi ---

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFD369)),
        contentAlignment = Alignment.Center // Memposisikan semuanya di tengah
    ) {
        // Gunakan when untuk menampilkan UI berdasarkan state
        when (loginUiState) {
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
                trailingIcon = { // Ikon visibility tetap sama
                    val image = if (passwordVisible)
                        R.drawable.visibility_24px
                    else R.drawable.visibility_off_24px
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
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
