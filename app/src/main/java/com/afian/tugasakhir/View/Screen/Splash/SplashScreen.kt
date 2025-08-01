package com.afian.tugasakhir.View.Screen.Splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.afian.tugasakhir.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(navController: NavController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFD369)), // Warna background FFD369
        contentAlignment = Alignment.Center // Menempatkan konten di tengah
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally // Menempatkan konten di tengah secara horizontal
        ) {
            // Menampilkan Lottie Animation
            MyLottie()
            Spacer(modifier = Modifier.height(4.dp)) // Kurangi tinggi spacer agar lebih dekat
            // Menambahkan teks "DosenTracker" di bawah Lottie
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
        }
    }
    // Navigasi ke Welcome Screen setelah delay
    LaunchedEffect(Unit) {
        delay(2000) // Delay 2 detik
        navController.navigate("welcome")
    }
}


@Composable
fun MyLottie() {
    val preLoaderLottie by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.animate_logo)
    )

    val preLoaderProgress by animateLottieCompositionAsState(preLoaderLottie,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
    )

    LottieAnimation(
        composition = preLoaderLottie,
        progress = {preLoaderProgress},
//        modifier = Modifier.fillMaxSize()
    )

}

//@Preview(showBackground = true)
//@Composable
//fun Preview() {
//   SplashScreen()
//}