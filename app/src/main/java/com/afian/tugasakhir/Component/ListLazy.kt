package com.afian.tugasakhir.Component

import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Model.Dosen
import com.afian.tugasakhir.R
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.request.RequestOptions

@Composable
fun DosenList(viewModel: DosenViewModel = viewModel()) {
    // Fetch the list of Dosen
    val dosenList = viewModel.dosenList.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Dosen on Campus", style = MaterialTheme.typography.titleLarge)

        LazyColumn {
            items(dosenList.value) { dosen ->
                DosenItem(dosen)
            }
        }
    }
}

@Composable
fun DosenItem(dosen: Dosen) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Load image using Coil or any other image loading library
            Image(
                painter = rememberImagePainter(dosen.foto_profile ?: R.drawable.placeholder_image), // Replace with your placeholder
                contentDescription = "Profile Picture",
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = dosen.user_name, style = MaterialTheme.typography.labelLarge)
                Text(text = dosen.identifier, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Preview
@Composable
fun PreviewListDosen() {
    DosenList()
}