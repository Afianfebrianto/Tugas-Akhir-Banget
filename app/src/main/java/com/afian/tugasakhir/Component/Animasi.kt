package com.afian.tugasakhir.Component

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.afian.tugasakhir.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

// --- Composable Terpisah untuk Animasi Lottie ---

@Composable
fun LottieSuccessAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.sucses_animated)) // Ganti nama file
    // Putar animasi sekali saja
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1f // Atur kecepatan jika perlu
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(150.dp) // Ukuran animasi sukses
    )
}

@Composable
fun LottieFailAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.dannied_animate)) // Ganti nama file
    // Putar animasi sekali saja
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1f
    )
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(150.dp) // Ukuran animasi gagal
    )
}
