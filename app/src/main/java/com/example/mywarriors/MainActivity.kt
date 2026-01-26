package com.example.mywarriors

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DEFAULT_ARGS_KEY

class MainActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }// Use to store



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background (DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {



        Text(
            text = "WARRIOR LOGIN",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 40.dp)
        )




        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.LightGray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = WarriorGreen,
                unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )// Login input

        Spacer(modifier = Modifier.height(16.dp))




        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.LightGray) },
            visualTransformation = PasswordVisualTransformation(), // HidesText
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor =  WarriorGreen,
                unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )/// PassWord Input

        Spacer(modifier = Modifier.height(30.dp))






        Button(
            onClick = {

                Log.d("Login", "User clicked Login with: $email")
            },
            colors = ButtonDefaults.buttonColors(containerColor = WarriorGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("ENTER THE ARENA", fontSize = 18.sp, color = Color.White)
        }
    }
}
val WarriorGreen = Color(0xFF4CAF50)
val Warrior1 = Color(0xFF2196F3)
val Warrior2 = Color(0xFF2196F3)
val Warrior3 = Color(0xFF2196F3)
val Warrior4 = Color(0xFF2196F3)
val DarkBackground = Color(0xFF121212)// color templates change name and color see fit
