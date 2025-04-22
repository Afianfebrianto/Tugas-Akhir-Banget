package com.afian.tugasakhir.View.Screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.afian.tugasakhir.Component.CardButtonBarAdmin
import com.afian.tugasakhir.Component.DosenList
import com.afian.tugasakhir.Component.Header
import com.afian.tugasakhir.Component.TopDosenLeaderboard
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.Controller.PeringkatDosenViewModel
import androidx.compose.runtime.getValue


@Composable
fun HomeAdminScreen(loginViewModel: LoginViewModel, navController: NavController, dosenViewModel: DosenViewModel, peringkatViewModel: PeringkatDosenViewModel = viewModel()) {
    // Ambil data pengguna dari ViewModel
    val user = loginViewModel.getUserData()

    // Jika user tidak ada, tampilkan pesan atau tampilan default
    val username = user?.user_name ?: "Guest"
    val identifier = user?.identifier ?: "No Identifier"
    val fotoProfile = user?.foto_profile ?: ""

    // --- Ambil State Peringkat dari ViewModel Peringkat ---
    val peringkatList by peringkatViewModel.peringkatList.collectAsState()
    val isLoadingPeringkat by peringkatViewModel.isLoading
    val errorPeringkat by peringkatViewModel.errorMessage
    val selectedMonthPeringkat by peringkatViewModel.selectedMonth
    val selectedYearPeringkat by peringkatViewModel.selectedYear

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
                CardButtonBarAdmin(navController)
                TopDosenLeaderboard(
                    modifier = Modifier.padding(horizontal = 8.dp), // Beri padding section
                    // Teruskan state dari peringkatViewModel sebagai parameter
                    peringkatList = peringkatList,
                    isLoading = isLoadingPeringkat,
                    errorMessage = errorPeringkat,
                    selectedMonth = selectedMonthPeringkat,
                    selectedYear = selectedYearPeringkat
                )
                DosenList(modifier = Modifier,navController = navController, viewModel = dosenViewModel)
            }
        }
    }
}