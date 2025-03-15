package com.afian.tugasakhir.Component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ButtonAbu(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF3B3F46)), // Warna dark grey seperti pada gambar
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3B3F46),
            contentColor = Color(0xFFFFD166) // Warna kuning pada teks
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Preview
@Composable
fun CustomButtonPreview() {
    ButtonAbu(text = "Lets'start") {
        // Handle click action
    }
}
