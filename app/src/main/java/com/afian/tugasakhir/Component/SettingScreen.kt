package com.afian.tugasakhir.Component

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.Model.User
import com.afian.tugasakhir.R

@Composable
fun UserSettingsScreen(loginViewModel: LoginViewModel,navController: NavController) {
    // Ambil data pengguna dari ViewModel
    val user = loginViewModel.getUserData()

    // Jika user tidak ada, tampilkan pesan atau tampilan default
    val username = user?.user_name ?: "Guest"
    val identifier = user?.identifier ?: "No Identifier"
    val fotoProfile = user?.foto_profile ?: R.drawable.placeholder_image // Ganti dengan placeholder jika tidak ada foto

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Kustom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 46.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "User Settings",
                style = MaterialTheme.typography.headlineMedium
            )
        }



        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            // Foto Profil
            Image(
                painter = rememberImagePainter(fotoProfile),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape) // Clip the image to a circle
                    .border(2.dp, Color.Gray, CircleShape) // Optional: Add border
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username
            Text(
                text = username,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Identifier
            Text(
                text = identifier,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Options
            Column {
                // Personal Information
                TextButton(onClick = { /* TODO: Navigate to Personal Information */ }) {
                    Text("Personal Information")
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }

                // Notification
                TextButton(onClick = { /* TODO: Toggle Notification */ }) {
                    Text("Notification")
                    Icon(painter = painterResource(R.drawable.ic_toggle_on_24), contentDescription = null)
                }

                // Language
                TextButton(onClick = { /* TODO: Navigate to Language Settings */ }) {
                    Text("Language")
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }

                // Logout
                TextButton(onClick = { loginViewModel.logout()
                navController.navigate("login")}) {
                    Text("Logout")
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                }
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun UserSettingsScreenPreview() {
//    // Buat instance ViewModel dummy untuk preview
//    val dummyUser = User(
//        user_id = 1,
//        identifier = "13020210018",
//        user_name = "User Name",
//        role = "mahasiswa",
//        foto_profile = "https://example.com/profile.jpg",
//        update_password = 0
//    )
//
//    val loginViewModel = object : LoginViewModel(ApplicationProvider.getApplicationContext()) {
//        init {
//            currentUser.value = dummyUser
//        }
//    }
//
//    UserSettingsScreen(loginViewModel)
//}
