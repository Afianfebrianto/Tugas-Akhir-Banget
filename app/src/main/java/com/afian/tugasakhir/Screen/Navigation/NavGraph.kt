package com.afian.tugasakhir.Screen.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.Screen.admin.ScreenAdmin
import com.afian.tugasakhir.Screen.dosen.ScreenDosen
import com.afian.tugasakhir.Screen.LoginScreen.LoginScreen
import com.afian.tugasakhir.Screen.mahasiswa.ScreenMhs

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(LoginViewModel(), navController) }
        composable("dosen") { ScreenDosen() }
        composable("mahasiswa") { ScreenMhs() }
        composable("admin") { ScreenAdmin() }
    }
}