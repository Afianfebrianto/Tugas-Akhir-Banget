package com.afian.tugasakhir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.View.Screen.Navigation.NavGraph
import com.afian.tugasakhir.View.Screen.LoginScreen.LoginScreen
import com.afian.tugasakhir.View.Screen.Navigation.NavigationGraph
import com.afian.tugasakhir.ui.theme.TugasAkhirTheme

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mengatur fullscreen
        enableEdgeToEdge()
        setContent {
            // Inisialisasi NavController
//            val navController = rememberNavController()
//            NavGraph(navController)

//           DosenScreen()
            val navController = rememberNavController()
            NavigationGraph(navController, loginViewModel)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TugasAkhirTheme {
        Greeting("Android")
    }
}