package com.afian.tugasakhir.Component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCustomTopAppBar(
    title: String, // <-- Parameter title ditambahkan di sini
    navController: NavController,
    // Anda bisa menambahkan parameter lain jika perlu, misalnya actions
    // actions: @Composable RowScope.() -> Unit = {}
) {
    Column {
        TopAppBar(
            title = { Text(title) }, // <-- Gunakan parameter title
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali"
                    )
                }
            },
            // actions = actions, // <-- Gunakan parameter actions jika ada
//            colors = TopAppBarDefaults.topAppBarColors(
//                containerColor = Color.White,
//                titleContentColor = MaterialTheme.colorScheme.onSurface,
//                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
//                actionIconContentColor = MaterialTheme.colorScheme.onSurface // Warna untuk ikon di actions
//            ),
            modifier = Modifier.shadow(elevation = 10.dp)
            // scrollBehavior = scrollBehavior // Jika Anda ingin menambahkan scroll behavior
        )
//        Divider(color = Color.LightGray, thickness = 1.dp)
    }
}