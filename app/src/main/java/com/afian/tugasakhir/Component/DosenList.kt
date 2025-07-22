package com.afian.tugasakhir.Component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.R

@Composable
fun DosenList(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: DosenViewModel = viewModel()
) {
    val dosenListFiltered by viewModel.filteredDosenList.collectAsState()
    val isLoading by viewModel.isLoadingDosen

    val dosenOnCampus by viewModel.filteredDosenList.collectAsState()
    val dosenNotInCampus by viewModel.filteredDosenNotInCampusList.collectAsState()

    var selectedDosen by remember { mutableStateOf<Dosen?>(null) }

    val previewItemCount = 4

    val displayedDosenList = dosenListFiltered.take(previewItemCount)
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saat Ini di Kampus (${dosenListFiltered.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { viewModel.refreshData() },
                enabled = !isLoading
            ) {
                if (isLoading && dosenListFiltered.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang Daftar"
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            when {
                isLoading && displayedDosenList.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                !isLoading && displayedDosenList.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Tidak ada dosen di kampus"
                            else "Dosen \"$searchQuery\" tidak ditemukan di kampus.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        displayedDosenList.forEach { dosen ->
                            DosenItem(
                                dosen = dosen,
                                onClick = { selectedDosen = it }
                            )
                        }
                    }
                }
            }
        }

        if (dosenListFiltered.size > previewItemCount && !isLoading) {
            TextButton(
                onClick = { navController.navigate("informasi_dosen") },
                modifier = Modifier.align(Alignment.End).padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Lihat Semua")
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

    }

    selectedDosen?.let { dosenData ->
        DosenDetailDialog(
            dosen = dosenData,
            onDismissRequest = { selectedDosen = null },
            dosenOnCampusList = dosenOnCampus,
            dosenNotInCampusList = dosenNotInCampus
        )
    }
}


@Composable
fun DosenItem(
    dosen: Dosen,
    onClick: (Dosen) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(dosen) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = dosen.foto_profile,
                    placeholder = painterResource(id = R.drawable.placeholder_image_24),
                    error = painterResource(id = R.drawable.placeholder_image_24)
                ),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = dosen.user_name, style = MaterialTheme.typography.titleMedium)
                Text(text = dosen.identifier, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}