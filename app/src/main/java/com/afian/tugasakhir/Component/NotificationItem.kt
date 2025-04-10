package com.afian.tugasakhir.Component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afian.tugasakhir.data.NotificationHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun NotificationItem(notification: NotificationHistoryItem) {
    // Pindahkan Formatter ke sini agar diremember per item jika perlu
    val dateFormatter = remember { SimpleDateFormat("E, dd MMM yyyy HH:mm", Locale("id", "ID")) } // Format Indonesia
    val formattedDate = remember(notification.timestamp) {
        dateFormatter.format(Date(notification.timestamp))
    }

    Column(modifier = Modifier.fillMaxWidth()) { // Isi lebar penuh
        notification.title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = notification.body,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall, // Ubah ke bodySmall agar tidak terlalu besar
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}