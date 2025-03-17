package com.afian.tugasakhir.View.Screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afian.tugasakhir.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerScreen() {
    var username = "Afian"
    var identifier = "13020210018"

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        TopAppBar(
            title = { Text("Hello,\n$username") },
        )

        // Search Bar
        var searchQuery = remember { mutableStateOf(TextFieldValue("")) }
        TextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            label = { Text("Search Lecturers") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Lecturer List
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Lecturers on Campus", fontSize = 20.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            // Sample Lecturer Items
            for (i in 1..5) {
                LecturerItem(username = "Username", lastUpdate = "Last Update 17:28 / 21/09/2024")
            }
        }
    }
}

@Composable
fun LecturerItem(username: String, lastUpdate: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
//        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for lecturer image
            Image(
                painter = painterResource(id =R.drawable.placeholder_image), // Replace with actual image resource
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = username, fontSize = 16.sp)
                Text(text = lastUpdate, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLecturerScreen() {
    LecturerScreen()
}

