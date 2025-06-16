package com.afian.tugasakhir.Component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.afian.tugasakhir.Controller.Screen
import com.afian.tugasakhir.R

@Composable
fun CardButtonBarDosen(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CardButton(
            iconRes = R.drawable.ic_group_search, // Replace with your icon resource
            label = stringResource(R.string.informasi_dosen),
            onClick = {
                // TODO: Handle Informasi Dosen click
                navController.navigate("informasi_dosen")
            }
        )
        CardButton(
            iconRes = R.drawable.ic_notifications, // Replace with your icon resource
            label = stringResource(R.string.pemberitahuan),
            onClick = {
                navController.navigate("riwayat_panggilan_dosen")
            }
        )
        CardButton(
            iconRes = R.drawable.person_search_24px, // Replace with your icon resource
            label = stringResource(R.string.cari_mahasiswa),
            onClick = {
                navController.navigate("cari_mahasiswa")
                // TODO: Handle KTM Digital click
            }
        )
        CardButton(
            iconRes = R.drawable.ic_settings, // Replace with your icon resource
            label = stringResource(R.string.pengaturan),
            onClick = {
                // TODO: Handle Pengaturan click
                navController.navigate("user_settings")
            }
        )
    }
}

@Composable
fun CardButtonBarMhs(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CardButton(
            iconRes = R.drawable.ic_group_search, // Replace with your icon resource
            label = stringResource(R.string.informasi_dosen),
            onClick = {
                // TODO: Handle Informasi Dosen click
                navController.navigate("informasi_dosen")
            }
        )
        CardButton(
            iconRes = R.drawable.ic_notifications, // Replace with your icon resource
            label = stringResource(R.string.pemberitahuan),
            onClick = {
                navController.navigate(Screen.NotificationMhs.route)
            }
        )
//        CardButton(
//            iconRes = R.drawable.ic_id_card, // Replace with your icon resource
//            label = stringResource(R.string.ktm_digital),
//            onClick = {
//                // TODO: Handle KTM Digital click
//                navController.navigate("ktm_digital")
//            }
//        )
        CardButton(
            iconRes = R.drawable.ic_settings, // Replace with your icon resource
            label = stringResource(R.string.pengaturan),
            onClick = {
                // TODO: Handle Pengaturan click
                navController.navigate("user_settings")
            }
        )
    }
}

@Composable
fun CardButtonBarAdmin(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CardButton(
            iconRes = R.drawable.co_present_24px, // Replace with your icon resource
            label = stringResource(R.string.monitoring_dosen),
            onClick = {
                navController.navigate("monitoring_dosen")
                // TODO: Handle Informasi Dosen click
            }
        )
        CardButton(
            iconRes = R.drawable.person_add_24px, // Replace with your icon resource
            label = stringResource(R.string.user_add),
            onClick = {
                navController.navigate("add_user")
                // TODO: Handle Informasi Dosen click
            }
        )
        CardButton(
            iconRes = R.drawable.manage_accounts_24px, // Replace with your icon resource
            label = stringResource(R.string.recovery_account),
            onClick = {
                navController.navigate("user_recovery")
                // TODO: Handle KTM Digital click
            }
        )
//        CardButton(
//            iconRes = R.drawable.description_24px, // Replace with your icon resource
//            label = "Download Report",
//            onClick = {
//                // TODO: Handle Pengaturan click
//            }
//        )
        CardButton(
            iconRes = R.drawable.ic_settings, // Replace with your icon resource
            label = stringResource(R.string.settings),
            onClick = {
                // TODO: Handle Pengaturan click
                navController.navigate("user_settings")
            }
        )
    }
}


@Composable
fun CardButton(iconRes: Int, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(85.dp) // Set a fixed size for each button
            .padding(4.dp)
            .clickable(onClick = onClick), // Add clickable modifier
        shape = RoundedCornerShape(8.dp),
//        backgroundColor = Color.White // Set background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 8.sp,
                color = Color.Black
            )
        }
    }
}


//@Preview
//@Composable
//fun Preview1() {
//    CardButtonBar()
//}