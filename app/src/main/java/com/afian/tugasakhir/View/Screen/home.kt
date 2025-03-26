package com.afian.tugasakhir.View.Screen

import android.R.attr.identifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.afian.tugasakhir.Component.CardButton
import com.afian.tugasakhir.Component.CardButtonBar
import com.afian.tugasakhir.Component.DosenList
import com.afian.tugasakhir.Component.Header
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.R

@Composable
fun homeScreen1(){
    val Username=""
    val Identifier=""
    val Fotoprofile=""
    Column(modifier = Modifier.fillMaxSize().background(color = Color(0xFF1E3E62)).padding(top = 16.dp)) {
        Header(Username, Identifier, Fotoprofile)
        Card(
//            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                CardButtonBar()
                DosenList()
            }
        }
    }
}

@Composable
fun HomeScreen(loginViewModel: LoginViewModel) {
    // Ambil data pengguna dari ViewModel
    val user = loginViewModel.currentUser.value

    // Jika user tidak ada, tampilkan pesan atau tampilan default
    val username = user?.user_name ?: "Guest"
    val identifier = user?.identifier ?: "No Identifier"
    val fotoProfile = user?.foto_profile ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFF1E3E62))
            .padding(top = 16.dp)
    ) {
        Header(username, identifier, fotoProfile) // Panggil Header dengan data pengguna
        Card(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                CardButtonBar()
                DosenList()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLecturerScreen() {
//    homeScreen()
}

