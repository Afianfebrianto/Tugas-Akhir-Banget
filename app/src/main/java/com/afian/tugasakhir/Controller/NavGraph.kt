package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.afian.tugasakhir.Component.GeofencingMap
import com.afian.tugasakhir.View.Screen.HelperScreen.NotificationScreen
import com.afian.tugasakhir.View.Screen.HelperScreen.UserSettingsScreen
import com.afian.tugasakhir.View.Screen.LoginScreen.LoginScreen
import com.afian.tugasakhir.View.Screen.Splash.SplashScreen
import com.afian.tugasakhir.View.Screen.Welcome.WelcomeScreen
import com.afian.tugasakhir.View.Screen.Welcome.WelcomeScreen1
import com.afian.tugasakhir.View.Screen.admin.HomeAdminScreen
import com.afian.tugasakhir.View.Screen.dosen.HomeDosenScreen
import com.afian.tugasakhir.View.Screen.mahasiswa.HomeMhsScreen


//@Composable
//fun NavigationGraph(){
//
//}

//@Composable
//fun NavigationGraph(navController: NavHostController, loginViewModel: LoginViewModel) {
//
//    NavHost(navController, startDestination = "splash") {
//        composable("splash") { SplashScreen(navController) }
//        composable("welcome") { WelcomeScreen(navController) }
//        composable("welcome1") { WelcomeScreen1(navController) }
//        composable("login") { LoginScreen(loginViewModel, navController) }
//        composable("dosen") { HomeDosenScreen(loginViewModel) }
//        composable("mahasiswa") { HomeMhsScreen(loginViewModel) }
//        composable("admin") { HomeAdminScreen(loginViewModel) }
//    }
//}

//@Composable
//fun NavigationGraph(navController: NavHostController, loginViewModel: LoginViewModel) {
//    // Periksa status login dan peran pengguna
//    val isLoggedIn = loginViewModel.isLoggedIn()
//    val userRole = loginViewModel.getUserRole()
//
//    // Tentukan layar awal berdasarkan status login
//    val startDestination = when {
//        isLoggedIn -> {
//            when (userRole) {
//                "dosen" -> "dosen"
//                "mhs" -> "mahasiswa"
//                "admin" -> "admin"
//                else -> "splash"
//            }
//        }
//        else -> "splash"
//
//    }
//
//    NavHost(navController, startDestination = startDestination) {
//        composable("splash") { SplashScreen(navController) }
//        composable("welcome") { WelcomeScreen(navController) }
//        composable("welcome1") { WelcomeScreen1(navController) }
//        composable("login") { LoginScreen(loginViewModel, navController) }
//        composable("dosen") { HomeDosenScreen(loginViewModel) }
//        composable("mahasiswa") { HomeMhsScreen(loginViewModel) }
//        composable("admin") { HomeAdminScreen(loginViewModel) }
//    }
//}

@Composable
fun NavigationGraph(navController: NavHostController, loginViewModel: LoginViewModel) {
    val isLoggedIn = loginViewModel.isLoggedIn()
    Log.d("NavigationGraph", "Is user logged in: $isLoggedIn") // Log status login
    val userRole = loginViewModel.getUserRole()
    Log.d("NavigationGraph", "User role: $userRole")

    NavHost(navController, startDestination = if (isLoggedIn) {
        when (userRole) {
            "dosen" -> "dosen"
            "mhs" -> "mahasiswa"
            "admin" -> "admin"
            else -> "splash"
        }
    } else {
        "splash"
    })
    {
        composable("splash") { SplashScreen(navController) }
        composable("welcome") { WelcomeScreen(navController) }
        composable("welcome1") { WelcomeScreen1(navController) }
        composable("login") { LoginScreen(loginViewModel, navController) }
        composable("dosen") { HomeDosenScreen(loginViewModel,navController) }
        composable("mahasiswa") { HomeMhsScreen(loginViewModel,navController) }
        composable("admin") { HomeAdminScreen(loginViewModel,navController) }
        composable("user_settings") { UserSettingsScreen(loginViewModel,navController) }
        composable ("debug_maps"){ GeofencingMap() }
        composable ("notification"){ NotificationScreen() }
    }
}