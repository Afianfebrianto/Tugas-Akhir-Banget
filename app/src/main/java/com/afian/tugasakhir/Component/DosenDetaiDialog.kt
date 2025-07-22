package com.afian.tugasakhir.Component

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.R
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosenDetailDialog(
    dosen: Dosen,
    onDismissRequest: () -> Unit,
    dosenOnCampusList: List<Dosen>,
    dosenNotInCampusList: List<Dosen>
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val namaDosen = dosen.user_name
    val isActuallyOnCampus = dosenOnCampusList.any { it.identifier == dosen.identifier }

    val indicatorColor = if (isActuallyOnCampus) {
        Color(0xFF4CAF50)
    } else {
        Color(0xFFF44336)
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3E62)),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        Log.d("DosenDetailDialog", "Tombol Close diklik!")
                        onDismissRequest()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Dialog",
                            tint = Color.White
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 8.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = dosen.foto_profile,
                                placeholder = painterResource(id = R.drawable.placeholder_image),
                                error = painterResource(id = R.drawable.placeholder_image)
                            ),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Fakultas Ilmu Komputer",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = dosen.user_name,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = dosen.identifier,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!dosen.informasi.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Text(
                                    text = dosen.informasi,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Justify
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Button(
                            onClick = {
                                val nomorHp = dosen.no_hp
                                if (!nomorHp.isNullOrBlank()) {
                                    try {
                                        var formattedNumber = nomorHp.filter { it.isDigit() }
                                        if (formattedNumber.startsWith("0")) {
                                            formattedNumber = "62" + formattedNumber.substring(1)
                                        } else if (!formattedNumber.startsWith("62")) {
                                            formattedNumber = "62$formattedNumber"
                                        }
                                        val templatePesan = "Assalamualaikum Pak/Bu ${namaDosen}, saya ingin bertanya terkait..."
                                        val encodedPesan = URLEncoder.encode(templatePesan, "UTF-8")
                                        val url = "https://wa.me/$formattedNumber?text=$encodedPesan"
                                        uriHandler.openUri(url)
                                    } catch (e: Exception) {
                                        Log.e("DosenDetailDialog", "Failed to open WhatsApp: ${e.message}")
                                        Toast.makeText(context, "Gagal membuka WhatsApp.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Log.w("DosenDetailDialog", "Nomor HP Dosen kosong.")
                                    Toast.makeText(context, "Nomor HP dosen tidak tersedia.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(35.dp)
                        ) {
                            Text("Chat On WhatsApp", color = Color.Black, style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.whatsapp),
                                contentDescription = "WhatsApp Icon",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(60.dp)
                            )


                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(indicatorColor)
                )
            }
        }
    }
}