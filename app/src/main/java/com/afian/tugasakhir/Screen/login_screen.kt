package com.afian.tugasakhir.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afian.tugasakhir.ui.theme.TugasAkhirTheme

class login_screen {
}

@Composable
fun Login(){
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ){
            Column {
                Row(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize =40.sp,
                        )
                    )
                }

                Row(
                    modifier = Modifier
                    .padding(bottom = 10.dp)) {
                    TextField(
                        value = username,
                        onValueChange = {username = it},
                        placeholder = { Text(text = "Username") },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 1,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
                Row (
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                ){
                    TextField(
                        value = password,
                        onValueChange = {password = it},
                        placeholder = { Text(text = "Password") },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 1,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }
                Row (){
                    Button(
                        onClick = {
                            //todo action button
                        },
                        modifier = Modifier
                            .align()
                    ) {
                        Text(text = "Masuk")
                    }
                }
            }


        }

    }


@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    TugasAkhirTheme {
        Login()
    }
}