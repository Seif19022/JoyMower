package com.joymower.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberImagePainter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.joymower.ui.theme.BackgroundColor
import com.joymower.ui.theme.Orange
import com.joymower.ui.theme.Poppins
import com.joymower.ui.theme.PrimaryTextColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private val grassheight = mutableStateOf(false)
private val logTimestamps = mutableStateListOf<String>()
private var controlMovementChanged = false
private const val MAX_LOGS = 8 // logs to display

private val storageReference = FirebaseStorage.getInstance().reference // for intruder detected image
private var imageURL = ""

@Composable
fun LogScreen(databaseReference: DatabaseReference) {
    val context = LocalContext.current
    val showImageDialog = remember { mutableStateOf(false) }

    fetchgrassheightlog(databaseReference) { mowed ->
        if (mowed && !grassheight.value) {
            grassheight.value = true
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val grassLogData = "Grass mowed at $currentTime"
            databaseReference.child("Log").push().setValue(grassLogData)

            // Log entry added successfully, now fetch the image URL from Firebase Storage
            val imageFileName = "images/caughtin4k.jpg"
            val imageRef = storageReference.child(imageFileName)
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                // Update the log entry with the image URL
                imageURL = uri.toString()
                databaseReference.child("intruderImageURL").setValue(imageURL)}
        }else if(!mowed && grassheight.value)
        {
            grassheight.value = false
        } else {

        }
    }

    fetchLogTimestamps(databaseReference)
    addLog(databaseReference) {controlMovement ->
        if (controlMovement == 1 && !controlMovementChanged) {
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logData = "Movement type changed to Manual at $currentTime"
            databaseReference.child("Log").push().setValue(logData)
            controlMovementChanged = true
        } else if (controlMovement == 0 && controlMovementChanged) {
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logData = "Movement type changed to Autonomous at $currentTime"
            databaseReference.child("Log").push().setValue(logData)
            controlMovementChanged = false
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Historical Data and Analytics Log",
                fontFamily = Poppins,
                fontSize = 18.sp,
                color = PrimaryTextColor,
                fontWeight = FontWeight.Bold
            )

            if (logTimestamps.isEmpty()) {
                Text(text = "All Clear!",fontFamily = Poppins,
                    fontSize = 13.sp,
                    color = PrimaryTextColor,
                    fontWeight = FontWeight.Bold)
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    logTimestamps.forEach { time ->
                        val isDetectedLog = time.contains("Grass Mowed")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = time,
                                fontFamily = Poppins,
                                fontSize = 13.sp,
                                color = PrimaryTextColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isDetectedLog) {
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            showImageDialog.value = true
                                        }
                                        .size(64.dp)
                                ) {
                                    Image(
                                        painter = rememberImagePainter(imageURL),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }}
            if (showImageDialog.value) {
                ImageDialog(
                    imageURL = imageURL,
                    onDismissRequest = { showImageDialog.value = false }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { clearLogData(databaseReference) },
                colors = buttonColors(Orange),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Clear Log Data")
            }
        }
    }
}

@Composable
fun ImageDialog(imageURL: String, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberImagePainter(imageURL),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun fetchgrassheightlog(databaseReference: DatabaseReference, ongrassheight: (Boolean) -> Unit) {
    val grassheightReference = databaseReference.child("GrassHeight").child("GrassHeight")
    grassheightReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val grassheight = snapshot.getValue(Int::class.java) == 1
            ongrassheight(grassheight)
        }

        override fun onCancelled(error: DatabaseError) {
            // Handle database error
        }
    })
}

private fun fetchLogTimestamps(databaseReference: DatabaseReference) {
    val logsReference = databaseReference.child("Log")
    logsReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            logTimestamps.clear()
            val logCount = snapshot.childrenCount
            val start = if (logCount > MAX_LOGS) logCount - MAX_LOGS else 0
            val end = logCount
            snapshot.children.toList().subList(start.toInt(), end.toInt()).forEach { childSnapshot ->
                val timestamp = childSnapshot.getValue(String::class.java)
                timestamp?.let { logTimestamps.add(it) }
            }

            // Delete old logs from Firebase
            if (logCount > MAX_LOGS) {
                val deleteStart = 0
                val deleteEnd = (logCount - MAX_LOGS).toInt()
                val deleteLogs = snapshot.children.toList().subList(deleteStart, deleteEnd)
                deleteLogs.forEach { childSnapshot ->
                    childSnapshot.ref.removeValue()
                }
            }
        }
        override fun onCancelled(error: DatabaseError) {
            // Handle database error
        }
    })
}

private fun clearLogData(databaseReference: DatabaseReference) {
    databaseReference.child("Log").removeValue()
}


private fun addLog(databaseReference: DatabaseReference, onChangemowed: (Int) -> Unit) {
    val controlMovementReference = databaseReference.child("RobotStatus").child("ControlMovement")
    controlMovementReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val controlMovement = snapshot.getValue(Int::class.java) ?: 0
            onChangemowed(controlMovement)
        }
        override fun onCancelled(error: DatabaseError) {
            // Handle database error
        }
    })
}
