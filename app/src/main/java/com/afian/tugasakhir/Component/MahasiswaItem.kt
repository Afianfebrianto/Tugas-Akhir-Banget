package com.afian.tugasakhir.Component

import android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.Model.PanggilanHistoryMahasiswaItem
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
@Composable
fun PanggilanItem(
    item: PanggilanHistoryMahasiswaItem,
    isResponding: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val inputFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }
    val outputFormat = remember { SimpleDateFormat("E, dd MMM yyyy HH:mm", Locale("id", "ID")) }

    val formatTimestamp: (String?) -> String = remember {
        { timestampString ->
            timestampString?.let {
                try {
                    inputFormat.parse(it)?.let { date -> outputFormat.format(date) } ?: it
                } catch (e: ParseException) {
                    it
                }
            } ?: "-"
        }
    }

    val formattedWaktuPanggil = formatTimestamp(item.waktu_panggil)
    val formattedWaktuRespon = formatTimestamp(item.waktu_respon)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = item.dosen_foto,
                        placeholder = painterResource(id = R.drawable.ic_menu_report_image),
                        error = painterResource(id = R.drawable.ic_menu_report_image)
                    ),
                    contentDescription = "Foto Dosen ${item.dosen_name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.dosen_name ?: "Nama Dosen Tidak Ada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Waktu Panggil: $formattedWaktuPanggil",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    val statusText = item.status.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                    val statusColor = when (item.status.lowercase()) {
                        "accepted" -> Color(0xFF4CAF50)
                        "declined" -> MaterialTheme.colorScheme.error
                        "pending" -> MaterialTheme.colorScheme.primary
                        else -> Color.Gray
                    }
                    Text(
                        text = "Status: $statusText",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (item.status == "pending") FontWeight.Bold else FontWeight.Normal,
                        color = statusColor
                    )

                    if (item.status.lowercase() != "pending") {
                        Text(
                            text = "Waktu Respon: $formattedWaktuRespon",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (item.status.lowercase() == "pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isResponding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end=8.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    Button(
                        onClick = onDecline,
                        enabled = !isResponding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Tolak")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = onAccept,
                        enabled = !isResponding,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Terima")
                    }
                }
            }
        }
    }
}