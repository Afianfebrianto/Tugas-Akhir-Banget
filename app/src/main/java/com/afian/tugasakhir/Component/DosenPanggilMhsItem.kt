package com.afian.tugasakhir.Component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.afian.tugasakhir.Model.PanggilanHistoryDosenItem
import com.afian.tugasakhir.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DosenPanggilanHistoryItem(item: PanggilanHistoryDosenItem) {

    val inputFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }
    val outputFormat = remember { SimpleDateFormat("E, dd MMM yy HH:mm", Locale("id", "ID")) }
    val formatTimestamp: (String?) -> String = remember {
        { ts -> ts?.let { try { inputFormat.parse(it)?.let { d -> outputFormat.format(d) } ?: it } catch (e: ParseException) { it } } ?: "-" }
    }
    val formattedWaktuPanggil = formatTimestamp(item.waktu_panggil)
    val formattedWaktuRespon = formatTimestamp(item.waktu_respon)

    val statusColor = when (item.status.lowercase()) {
        "accepted" -> Color(0xFF4CAF50)
        "declined" -> MaterialTheme.colorScheme.error
        "pending" -> MaterialTheme.colorScheme.primary
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = item.mahasiswa_foto,
                    placeholder = painterResource(id = R.drawable.placeholder_image),
                    error = painterResource(id = R.drawable.placeholder_image)
                ),
                contentDescription = "Foto ${item.mahasiswa_name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(45.dp).clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.mahasiswa_name ?: "Nama Mahasiswa Tdk Ada",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dipanggil: $formattedWaktuPanggil",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Status: ${item.status.replaceFirstChar { it.titlecase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                if (item.status.lowercase() != "pending") {
                    Text(
                        text = "Direspon: $formattedWaktuRespon",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}