package com.afian.tugasakhir.View.Screen.Welcome

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.afian.tugasakhir.Controller.Screen
import com.afian.tugasakhir.R
import com.afian.tugasakhir.View.Screen.Splash.MyLottie
import com.afian.tugasakhir.View.Screen.Splash.SplashScreen


@Composable
fun WelcomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFD369)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gambar dari Drawable
            MyLottie()
            Text(
                text = "Welcome to DosenTracker!",
                color = Color(0xFF1E3E62),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Tombol Next di kanan bawah
        IconButton(
            onClick = {
                //todo next to login screen
                navController.navigate("welcome1")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 56.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}



@Composable
fun WelcomeScreen1(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Jika gambar belum termuat, tetap hitam
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.fikomwelcome), // Ganti dengan nama gambar yang sesuai
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay agar teks lebih terbaca
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)) // Efek gelap di atas gambar
        )

        // Konten Utama
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to DosenTracker!",
                color = Color(0xFFFFD369), // Warna kuning sesuai tema sebelumnya
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Geofencing-based mobile application to track the presence of lecturers at the Faculty of Computer Science (FIKOM).",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Button "let's start"
            Button(
                onClick = {
                    //todo next page Login
                    navController.navigate(Screen.Login.route)
                },
                colors = ButtonDefaults.buttonColors(Color(0xFFFFD369)),
                modifier = Modifier.fillMaxWidth(0.6f) // Atur lebar tombol
            ) {
                Text(text = "let's start", color = Color.Black, fontSize = 16.sp)
            }
        }
    }
    BackHandler {
        // Mengarahkan kembali ke HomeScreen jika berada di halaman selain HomeScreen
        navController.navigate(Screen.Welcome.route) {
            // Menghapus seluruh stack navigasi yang ada sebelum HomeScreen
            popUpTo(Screen.Welcome.route) { inclusive = true }
            launchSingleTop = true
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun PreviewWelcome1() {
//    val navController: NavHostController
//WelcomeScreen1()
//}
//@Preview(showBackground = true)
//@Composable
//fun PreviewWelcome() {
//    val navController: NavHostController
//    WelcomeScreen()
//}
