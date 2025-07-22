package com.afian.tugasakhir.Component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.afian.tugasakhir.R

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
            .background(Color(0xFF3B3F46)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF3B3F46),
            contentColor = Color(0xFFFFD166)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Composable
fun Header(username: String, identifier: String, profilePictureUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Text Column
        Column {
            Text(
                text = "Hello,\n$username",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_badge),
                    contentDescription = "Badge Icon",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = identifier,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

            Image(
                painter = rememberImagePainter(profilePictureUrl ?: R.drawable.placeholder_image),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape) // Clip the image to a circle
            )
    }
}


@Preview
@Composable
fun Preview() {
val Username = "Afian Febrianto"
    val Identifier = "13020210018"
    val Foto = "987654dfghjkiuytf"
    Header(username = Username, identifier = Identifier, profilePictureUrl = Foto )
}
