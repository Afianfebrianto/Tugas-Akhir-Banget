package com.afian.tugasakhir.Screen.LoginScreen

import android.util.Log
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.afian.tugasakhir.ui.theme.TugasAkhirTheme

@Composable
fun LoginOri() {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFD369)),
        contentAlignment = Alignment.Center // Memposisikan semuanya di tengah
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(), // Mengisi lebar penuh
            horizontalAlignment = Alignment.CenterHorizontally // Memposisikan kolom di tengah horizontal
        ) {
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

            // TextField Username di tengah
            TextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text(text = "Username") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 1,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Mengatur lebar TextField
                    .padding(bottom = 10.dp)
            )

            // TextField Password di tengah
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(text = "Password") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 1,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Mengatur lebar TextField
                    .padding(bottom = 20.dp)
            )
            // Button "Masuk" di sebelah kanan
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.Absolute.Center // Posisikan tombol di kanan
            ) {
                ButtonAbu(text = "Masuk", modifier = Modifier .width(180.dp)) {

                }
            }
        }
    }
}


@Composable
fun LoginScreen(viewModel: LoginViewModel, navController: NavController) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFD369)),
        contentAlignment = Alignment.Center // Memposisikan semuanya di tengah
    ) {
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

            // TextField Username di tengah
            TextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text(text = "Username") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 1,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Mengatur lebar TextField
                    .padding(bottom = 10.dp)
            )

            // TextField Password di tengah
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(text = "Password") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 1,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Mengatur lebar TextField
                    .padding(bottom = 20.dp)
            )
            // Button "Masuk" di sebelah kanan
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.Absolute.Center // Posisikan tombol di kanan
            ) {
                ButtonAbu(text = "Masuk", modifier = Modifier .width(180.dp), onClick = { viewModel.login(username, password) { user ->
                    when (user.role) {
                        "dosen" -> {
                            navController.navigate("dosen")
                            Log.d("LoginScreen", "Navigating to Dosen Screen")
                        }
                        "mhs" -> {
                            navController.navigate("mahasiswa")
                            Log.d("LoginScreen", "Navigating to Mahasiswa Screen")
                        }
                        "admin" -> {
                            navController.navigate("admin")
                            Log.d("LoginScreen", "Navigating to Admin Screen")
                        }
                    }
                } })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    TugasAkhirTheme {
        LoginOri()
    }
}