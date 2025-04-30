package com.afian.tugasakhir.Controller

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.afian.tugasakhir.Component.GeofencingMap
import com.afian.tugasakhir.View.Screen.HelperScreen.CombinedDosenListScreen
import com.afian.tugasakhir.View.Screen.HelperScreen.DosenPanggilMahasiswaScreen
import com.afian.tugasakhir.View.Screen.HelperScreen.DosenRiwayatPanggilanScreen
import com.afian.tugasakhir.View.Screen.HelperScreen.KtmScreen
import com.afian.tugasakhir.View.Screen.HelperScreen.MonitoringDosenScreen
import com.afian.tugasakhir.View.Screen.HelperScreen.NotificationMhsScreen
import com.afian.tugasakhir.View.Screen.HelperScreen.UserAddScreen
import com.afian.tugasakhir.View.Screen.HelperScreen.UserRecoveryScreen
import com.afian.tugasakhir.View.Screen.HelperScreen.UserSettingsScreen
import com.afian.tugasakhir.View.Screen.LoginScreen.LoginScreen
import com.afian.tugasakhir.View.Screen.Splash.SplashScreen
import com.afian.tugasakhir.View.Screen.Welcome.WelcomeScreen
import com.afian.tugasakhir.View.Screen.Welcome.WelcomeScreen1
import com.afian.tugasakhir.View.Screen.admin.HomeAdminScreen
import com.afian.tugasakhir.View.Screen.dosen.HomeDosenScreen
import com.afian.tugasakhir.View.Screen.mahasiswa.HomeMhsScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.afian.tugasakhir.Model.User
import com.afian.tugasakhir.View.Screen.HelperScreen.UpdatePasswordScreen
import com.afian.tugasakhir.View.Screen.HelperScreen.UserProfileEditScreen


// Opsional tapi direkomendasikan: Definisikan Route di satu tempat
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Welcome1 : Screen("welcome1")
    object Login : Screen("login")
    object HomeDosen : Screen("dosen")
    object HomeMahasiswa : Screen("mahasiswa")
    object HomeAdmin : Screen("admin")
    object UserSettings : Screen("user_settings")
    object DebugMaps : Screen("debug_maps")
    object NotificationMhs : Screen("notification_mhs") // Nama lebih spesifik
    object InformasiDosen : Screen("informasi_dosen")
    object DosenPanggilMahasiswa : Screen("cari_mahasiswa")
    object DosenRiwayatPanggilan : Screen("riwayat_panggilan_dosen")
    object MonitoringDosen : Screen("monitoring_dosen")
    object AddUserExcel : Screen("add_user_excel") // Nama lebih spesifik
    object UserRecovery : Screen("user_recovery")
    object KtmDigital : Screen("ktm_digital")
    object ListDosen : Screen("list_dosen_screen")
    object UserProfileEdit : Screen("profile_edit")
    object UpdatePassword : Screen("update_password/{identifier}") // Route dengan argumen
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel, // Diterima dari MainActivity
    dosenViewModel: DosenViewModel  // Diterima dari MainActivity
    // Tambahkan ViewModel lain yang di-scope ke Activity jika ada
    // peringkatViewModel: PeringkatDosenViewModel // Contoh
) {
    // Ambil status login awal HANYA SEKALI saat pertama kali komposisi
    val initialLoginState = remember { loginViewModel.isLoggedIn() }
    val initialRole = remember { loginViewModel.getUserRole() }
    val startDestination = remember {
        if (initialLoginState) {
            val role = initialRole // Gunakan role awal
            Log.d("NavigationGraph", "Initial State: Logged In | Role: $role")
            when (role) {
                "dosen" -> Screen.HomeDosen.route
                "mhs" -> Screen.HomeMahasiswa.route
                "admin" -> Screen.HomeAdmin.route
                else -> Screen.Welcome.route // Fallback jika role awal aneh
            }
        } else {
            Log.d("NavigationGraph", "Initial State: Not Logged In")
            Screen.Welcome.route // Tujuan jika awalnya tidak login
        }
    }
    Log.d("NavigationGraph", "Final Initial Start Destination: $startDestination")
    // --- 👆 AKHIR PERBAIKAN START DESTINATION 👆 ---

    // --- LaunchedEffect untuk handle PERUBAHAN state login/logout (TETAP DIPERLUKAN) ---
    // Ambil state terkini untuk dipantau oleh LaunchedEffect
    val isLoggedInFlowState by loginViewModel.loginUiState.collectAsState() // Atau state lain yg merepresentasikan login

    // Ambil state terkini untuk dipantau
    val currentUser: User? = loginViewModel.currentUser
    LaunchedEffect(currentUser) { // Bereaksi saat user state berubah
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        val userIsCurrentlyLoggedIn = currentUser != null
        Log.d("navgraph", "Auth State Changed Effect Triggered: LoggedIn=$userIsCurrentlyLoggedIn, CurrentRoute=$currentRoute")

        if (!userIsCurrentlyLoggedIn) { // JIKA USER BARU SAJA LOGOUT
            // Jika kita TIDAK sedang di layar Welcome atau Login
            if (currentRoute != null && currentRoute != Screen.Welcome.route && currentRoute != Screen.Login.route) {
                Log.i("Navgraph", "EFFECT Action: User logged out, NOT on auth screen ($currentRoute). Forcing Welcome.")
                // Lakukan navigasi paksa ke Welcome dan clear stack
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
                // Reset state login di ViewModel SETELAH navigasi dipicu
                loginViewModel.resetLoginStateToIdle()
            } else {
                Log.d("Navgraph", "EFFECT Action: User logged out, but already on auth screen ($currentRoute). No navigation needed.")
                // Tetap reset state login jika mendarat di sini setelah logout
                loginViewModel.resetLoginStateToIdle()
            }
        }
        // --- 👇 HAPUS BLOK ELSE IF INI 👇 ---
        // HAPUS logika yang memaksa navigasi DARI Welcome/Login KE Home.
        // Biarkan LoginScreen yang menanganinya berdasarkan LoginUiState.
        /* HAPUS MULAI DARI SINI
        else if (userIsCurrentlyLoggedIn && (currentRoute == Screen.Welcome.route || currentRoute == Screen.Login.route)) {
             // Jika user login TAPI masih di Welcome/Login -> JANGAN paksa ke Home dari sini
             Log.w(NAV_GRAPH_TAG, "EFFECT Action: User logged in, but on auth screen ($currentRoute). Navigation handled by LoginScreen.")
             // val role = loginViewModel.getUserRole()
             // val homeRoute = when (role) { ... }
             // navController.navigate(homeRoute) { ... }
         }
        HAPUS SAMPAI SINI */
        else { // User logged in dan tidak di welcome/login
            Log.d("Navgraph", "EFFECT Action: User logged in and on main screen ($currentRoute). No navigation needed by this effect.")
        }
        // --- 👆 AKHIR BLOK HAPUS 👆 ---
    } // Akhir LaunchedEffect

//    LaunchedEffect(currentUser) { // Bereaksi saat user state berubah
//        val currentRoute = navController.currentBackStackEntry?.destination?.route
//        val userIsCurrentlyLoggedIn = currentUser != null // Cek login dari state terkini
//
//        Log.d("NavigationGraph", "Auth State Changed Effect: LoggedIn=$userIsCurrentlyLoggedIn, CurrentRoute=$currentRoute")
//
//        if (!userIsCurrentlyLoggedIn && currentRoute != null && currentRoute != Screen.Welcome.route && currentRoute != Screen.Login.route) {
//            // Jika user BARU SAJA logout DAN sedang TIDAK di Welcome/Login -> paksa ke Welcome
//            Log.i("NavigationGraph", "EFFECT: User logged out, not on auth screen ($currentRoute). Forcing Welcome.")
//            navController.navigate(Screen.Welcome.route) { // Ganti ke Screen.Login.route jika perlu
//                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
//                launchSingleTop = true
//            }
//        } else if (userIsCurrentlyLoggedIn && (currentRoute == Screen.Welcome.route || currentRoute == Screen.Login.route)) {
//            // Jika user BARU SAJA login DAN sedang di Welcome/Login -> paksa ke Home
//            val role = loginViewModel.getUserRole() // Dapatkan role terkini
//            val homeRoute = when (role) {
//                "dosen" -> Screen.HomeDosen.route
//                "mhs" -> Screen.HomeMahasiswa.route
//                "admin" -> Screen.HomeAdmin.route
//                else -> Screen.Welcome.route // Fallback jika role aneh
//            }
//            Log.i("NavigationGraph", "EFFECT: User logged in, on auth screen ($currentRoute). Forcing $homeRoute.")
//            navController.navigate(homeRoute) {
//                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
//                launchSingleTop = true
//            }
//        } else {
//            Log.d("NavigationGraph", "EFFECT: Auth state matches current location ($currentRoute, loggedIn=$userIsCurrentlyLoggedIn). No navigation needed by effect.")
//        }
//    }


    NavHost(navController = navController, startDestination = startDestination) {
        // Gunakan Screen.route untuk konsistensi
        composable(Screen.Splash.route) { SplashScreen(navController) } // Splash mungkin tidak perlu jika startDestination sudah benar
        composable(Screen.Welcome.route) { WelcomeScreen(navController) }
        composable(Screen.Welcome1.route) { WelcomeScreen1(navController) }
        composable(Screen.Login.route) { LoginScreen(loginViewModel, navController) }

        // --- Layar yang Membutuhkan ViewModel dari Activity ---
        composable(Screen.HomeDosen.route) {
            // Jika layar ini butuh viewmodel lain, buat di sini atau teruskan dari MainActivity
            val peringkatViewModel: PeringkatDosenViewModel = viewModel() // Buat instance baru scope NavGraph
            HomeDosenScreen(
                loginViewModel = loginViewModel, // Teruskan dari parameter
                navController = navController,
                dosenViewModel = dosenViewModel, // Teruskan dari parameter
                peringkatViewModel = peringkatViewModel // Teruskan instance baru/dari Activity
            )
        }
        composable(Screen.HomeMahasiswa.route) {
            val notificationViewModel: NotificationViewModel = viewModel() // Buat instance baru
            HomeMhsScreen(
                loginViewModel = loginViewModel, // Teruskan dari parameter
                navController = navController,
                dosenViewModel
            )
        }
        composable(Screen.HomeAdmin.route) {
            // Jika perlu ViewModel khusus Admin
            // val adminViewModel: AdminViewModel = viewModel()
            HomeAdminScreen(
                loginViewModel = loginViewModel, // Teruskan dari parameter
                navController = navController,
                dosenViewModel

                // adminViewModel = adminViewModel
            )
        }
        composable(Screen.UserSettings.route) {
            UserSettingsScreen(
                loginViewModel = loginViewModel, // Teruskan dari parameter
                navController = navController
            )
        }
        composable(Screen.InformasiDosen.route) {
            CombinedDosenListScreen(
                navController = navController,
                viewModel = dosenViewModel, // <-- Gunakan instance yg diteruskan
            )
        }
        composable(Screen.DosenPanggilMahasiswa.route) {
            DosenPanggilMahasiswaScreen(
                navController = navController,
                dosenViewModel = dosenViewModel, // <-- Gunakan instance yg diteruskan
                loginViewModel = loginViewModel // <-- Teruskan instance yg diteruskan
            )
        }
        composable(Screen.DosenRiwayatPanggilan.route) {
            // === 👇 PERBAIKI VIEWMODEL SCOPING 👇 ===
            DosenRiwayatPanggilanScreen(
                navController = navController,
                dosenViewModel = dosenViewModel, // <-- Gunakan instance yg diteruskan
                loginViewModel = loginViewModel // <-- Teruskan instance yg diteruskan
            )
            // === 👆 AKHIR PERBAIKAN 👆 ===
        }
        composable(Screen.MonitoringDosen.route) {
            // Monitoring pakai ViewModel terpisah, jadi buat baru di sini OK
            val peringkatViewModel: PeringkatDosenViewModel = viewModel()
            MonitoringDosenScreen(
                navController = navController,
                viewModel = peringkatViewModel
            )
        }
        composable(Screen.AddUserExcel.route) {
            // Jika perlu ViewModel khusus Add User
            // val userAddViewModel: UserAddViewModel = viewModel()
            UserAddScreen(navController = navController /*, viewModel = userAddViewModel */)
        }
        composable(Screen.UserRecovery.route) {
            // Jika perlu ViewModel khusus User Management
            val userManagementViewModel: UserManagementViewModel = viewModel()
            UserRecoveryScreen(
                navController = navController,
                viewModel = userManagementViewModel
            )
        }
        composable(Screen.KtmDigital.route) {
            KtmScreen(
                navController = navController,
                loginViewModel = loginViewModel // Butuh data user login
            )
        }
        composable(Screen.ListDosen.route) { // Layar penuh list dosen di kampus
            CombinedDosenListScreen(
                navController = navController,
                viewModel = dosenViewModel // <-- Gunakan instance yg diteruskan
            )
        }

        // --- Layar Lain ---
        composable(Screen.DebugMaps.route){ GeofencingMap() }
        composable(Screen.NotificationMhs.route){ // Layar notif Mahasiswa
           NotificationMhsScreen()
        }
        composable(Screen.UserProfileEdit.route) { // Ganti "profile_edit" dgn nama route Anda
            // Dapatkan ViewModel (bisa pakai viewModel() jika tidak perlu scope Activity)
            val userProfileViewModel: UserProfileViewModel = viewModel()
            UserProfileEditScreen(
                navController = navController,
                loginViewModel = loginViewModel, // Kirim LoginViewModel untuk user ID awal
                viewModel = userProfileViewModel
            )
        }

        composable(
            route = Screen.UpdatePassword.route, // Contoh: "update_password/{identifier}"
            arguments = listOf(navArgument("identifier") { type = NavType.StringType })
        ) { backStackEntry ->
            val identifier = backStackEntry.arguments?.getString("identifier")
            if (identifier != null) {
                // Buat instance ViewModel baru khusus layar ini
                val updatePasswordViewModel: UpdatePasswordViewModel = viewModel(
                    // Jika ViewModel butuh SavedStateHandle, factory default akan memberikannya
                )
                UpdatePasswordScreen(
                    navController = navController,
                    identifier = identifier, // Teruskan identifier
                    loginViewModel = loginViewModel, // Untuk dapat role setelah sukses
                    viewModel = updatePasswordViewModel
                )
            } else {
                // Handle jika identifier null (seharusnya tidak terjadi jika navigasi benar)
                Text("Error: Identifier pengguna tidak ditemukan.")
                // Mungkin navigasi kembali ke login?
                // LaunchedEffect(Unit){ navController.popBackStack() }
            }
        }
    } // Akhir NavHost
}