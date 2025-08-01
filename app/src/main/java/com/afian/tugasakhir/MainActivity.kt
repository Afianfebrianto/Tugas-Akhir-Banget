package com.afian.tugasakhir

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.afian.tugasakhir.Controller.DosenViewModel
import com.afian.tugasakhir.Controller.LoginViewModel
import com.afian.tugasakhir.Controller.NavigationGraph
import com.afian.tugasakhir.ui.theme.TugasAkhirTheme

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels { ViewModelFactory(applicationContext) }
    private val dosenViewModel: DosenViewModel by viewModels()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            NavigationGraph(navController, loginViewModel,dosenViewModel )
            Log.d("MyApp", "NavController: $navController")
//            GeofencingMap()
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