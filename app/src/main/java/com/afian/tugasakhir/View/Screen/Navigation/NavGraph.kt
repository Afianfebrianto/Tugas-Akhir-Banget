package com.afian.tugasakhir.View.Screen.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.View.Screen.admin.ScreenAdmin
import com.afian.tugasakhir.View.Screen.dosen.ScreenDosen
import com.afian.tugasakhir.View.Screen.LoginScreen.LoginScreen
import com.afian.tugasakhir.View.Screen.Splash.SplashScreen
import com.afian.tugasakhir.View.Screen.Welcome.WelcomeScreen
import com.afian.tugasakhir.View.Screen.Welcome.WelcomeScreen1
import com.afian.tugasakhir.View.Screen.admin.HomeAdminScreen
import com.afian.tugasakhir.View.Screen.dosen.HomeDosenScreen
import com.afian.tugasakhir.View.Screen.mahasiswa.HomeMhsScreen
import com.afian.tugasakhir.View.Screen.mahasiswa.ScreenMhs

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(LoginViewModel(), navController) }
        composable("dosen") { ScreenDosen() }
        composable("mahasiswa") { ScreenMhs() }
        composable("admin") { ScreenAdmin() }
    }
}

//@Composable
//fun NavigationGraph(){
//
//}

@Composable
fun NavigationGraph(navController: NavHostController, loginViewModel: LoginViewModel) {
    NavHost(navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("welcome") { WelcomeScreen(navController) }
        composable("welcome1") { WelcomeScreen1(navController) }
        composable("login") { LoginScreen(loginViewModel, navController) }
        composable("dosen") { HomeDosenScreen(loginViewModel) }
        composable("mahasiswa") { HomeMhsScreen(loginViewModel) }
        composable("admin") { HomeAdminScreen(loginViewModel) }
    }
}
