package com.joymower.screen

import android.os.CountDownTimer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.joymower.ui.theme.BackgroundColor
import com.joymower.ui.theme.Blue500

// to hold the scheduled mowing duration
private var scheduledMowingDuration: Long = 0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MowingScreen() {

    // Local state to hold the duration input
    var durationInput by remember { mutableStateOf(0) }
    var remainingTime =  remember { mutableStateOf(0L) }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Remaining time counter
        Text(
            text = formatTime(remainingTime.value),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        // Duration input field
        TextField(
            value = durationInput.toString(),
            onValueChange = { durationInput = it.toIntOrNull() ?: 0 },
            label = { Text("Mowing Duration (in seconds)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(16.dp)
        )

        // Start mowing button
        Button(
            onClick = {
                if (durationInput > 0) {
                    scheduledMowingDuration = durationInput * 1000L // Convert seconds to milliseconds
                    startScheduledMowing(remainingTime)
                }
            },
            colors = ButtonDefaults.buttonColors(Blue500)
        ) {
            Text(text = "Start Scheduled Mowing",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

private fun startScheduledMowing(rt: MutableState<Long>) {

    // Set "RobotStatus/WorkingStatus" to true
    val db = FirebaseDatabase.getInstance()
    val workingStatusRef = db.getReference("RobotStatus/WorkingStatus")
    val scheduledMowingRef = db.getReference("RobotStatus/Scheduledmowing")
    workingStatusRef.setValue(true)
    scheduledMowingRef.setValue(true)
    // Calculate the end time based on the scheduled duration
    val endTime = System.currentTimeMillis() + scheduledMowingDuration

    // Start a countdown timer for the scheduled duration
    object : CountDownTimer(scheduledMowingDuration, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            rt.value = endTime - System.currentTimeMillis()
        }

        override fun onFinish() {
            // Timer finished, set "RobotStatus/WorkingStatus" to false
            workingStatusRef.setValue(false)
            rt.value = 0L
        }
    }.start()
}

private fun formatTime(timeInMillis: Long): String {
    val seconds = (timeInMillis / 1000) % 60
    val minutes = (timeInMillis / (1000 * 60)) % 60
    val hours = (timeInMillis / (1000 * 60 * 60)) % 24

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}