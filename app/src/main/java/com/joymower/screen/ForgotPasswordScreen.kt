package com.joymower.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.OutlinedTextField
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.internal.composableLambda
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.joymower.R
import com.joymower.navigation.Screen
import com.joymower.ui.theme.BackgroundColor

@Composable
fun ForgotPasswordScreen(navController: NavHostController) {
    // State for the entered email address
    val emailState = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.forgot_password_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text(text = stringResource(R.string.email_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Perform the password reset logic here
                val email = emailState.value
                if (email.isNotBlank()) {
                    resetPassword(email, showDialog, navController)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.reset_password_button_label),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(text = "Success") },
                text = { Text(text = "Password reset email sent successfully") },
                confirmButton = {
                    Button(onClick = {
                        showDialog.value = false

                        // Navigate back to the sign-in screen
                        navController.navigate(Screen.Auth.route) {
                            // Pop up to the sign-in screen, removing the reset password screen from the back stack
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    }) {
                        Text(text = "OK")
                    }
                }
            )
        }
    }

    LaunchedEffect(showDialog.value) {
        if (showDialog.value) {
            navController.navigate(Screen.Auth.route) {
                // Pop up to the sign-in screen, removing the reset password screen from the back stack
                popUpTo(Screen.ForgotPassword.route) { inclusive = true }
            }
        }
    }
}
private fun resetPassword(email: String, showDialog: MutableState<Boolean>, navController: NavHostController) {
    val firebaseAuth = FirebaseAuth.getInstance()

    firebaseAuth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Password reset email sent successfully
                showDialog.value = true
            } else {
                // Handle password reset failure
                // Provide feedback to the user
                showDialog.value = true // You might want to show the dialog even on failure for user feedback
            }
        }
        .addOnFailureListener { exception ->
            // Handle exceptions
            exception.printStackTrace()
            // Provide feedback to the user
            showDialog.value = true // You might want to show the dialog even on failure for user feedback
        }
}



