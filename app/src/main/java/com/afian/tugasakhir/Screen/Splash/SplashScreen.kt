package com.afian.tugasakhir.Screen.Splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.afian.tugasakhir.Greeting
import com.afian.tugasakhir.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition


@Composable
fun SplashScreen() {
    // Mengatur warna background
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
                modifier = Modifier.padding(top = 16.dp) // Jarak antara Lottie dan teks
            )
        }
    }
}


@Composable
fun MyLottie() {
    val preLoaderLottie by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.logo)
    )

    val preLoaderProgress by animateLottieCompositionAsState(preLoaderLottie,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
    )

    LottieAnimation(
        composition = preLoaderLottie,
        progress = {preLoaderProgress},
        modifier = Modifier.size(300.dp)
    )

}

@Preview(showBackground = true)
@Composable
fun Preview() {
   SplashScreen()
}