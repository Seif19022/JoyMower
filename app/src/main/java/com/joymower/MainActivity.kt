@file:Suppress("DEPRECATION")

package com.joymower

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.joymower.navigation.SetupNavGraph
import com.joymower.notifications.FirebaseService
import com.joymower.notifications.NotificationData
import com.joymower.notifications.PushNotification
import com.joymower.notifications.RetrofitInstance
import com.joymower.ui.theme.JoyMowerTheme
import com.joymower.viewmodel.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TOPIC = "/topics/myTopic2"

@ExperimentalAnimationApi
@ExperimentalPagerApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val TAG = "MainActivity"

    @Inject
    lateinit var splashViewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        var eToken: String? = null
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                FirebaseService.token = token
                eToken = token
            }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        val db = FirebaseDatabase.getInstance()

        // Mowing Notification
        val robotStatusRef = db.getReference("RobotStatus/WorkingStatus")
        val scheduleMowingRef = db.getReference("RobotStatus/ScheduledMowing")
        var statusChanged = false

        robotStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val existingValue = dataSnapshot.getValue(Boolean::class.java)

                scheduleMowingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(scheduleMowingSnapshot: DataSnapshot) {
                        val scheduleMowingValue = scheduleMowingSnapshot.getValue(Boolean::class.java)

                        if (existingValue == false && !statusChanged && scheduleMowingValue == true) {
                            statusChanged = true // Set the flag to true to prevent duplicate notifications

                            val notification = PushNotification(
                                NotificationData("[JoyMower]: Scheduled Mower Finished", "Your scheduled Mowing has finished!"),
                                eToken ?: ""
                            )
                            sendNotification(notification)
                            scheduleMowingRef.setValue(false)
                        } else if (existingValue == true) {
                            statusChanged = false // Reset the flag when the value changes back to false
                        }
                    }

                    override fun onCancelled(scheduleMowingError: DatabaseError) {
                        Log.e(TAG, "Database Error: $scheduleMowingError") // For debugging
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database Error: $error")
            }
        })


        // Grassheight Notification
        val intruderRef = db.getReference("GrassHeight/GrassHeight")
        var grassheight = false // Flag to track notification status

        intruderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val existingValue = dataSnapshot.getValue(Int::class.java)

                if (existingValue == 1 && !grassheight) {
                    grassheight = true // Set the flag to true to prevent duplicate notifications

                    val notification = PushNotification(
                        NotificationData("[JoyMower]: Grass Mowed", "The grass height is all equal now!"),
                        eToken ?: ""
                    )
                    sendNotification(notification)
                } else if (existingValue == 0) {
                    grassheight = false // Reset the flag when the value changes back to 0
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database Error: $error") // For debugging
            }
        })

        // Battery Status Notification
        val batteryStatusRef = db.getReference("RobotStatus/BatteryStatus")
        var lowBatteryNotified = false // Flag to track notification status

        batteryStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val batteryStatus = dataSnapshot.getValue(Double::class.java)

                if (batteryStatus != null && batteryStatus < 20.0 && !lowBatteryNotified) {
                    lowBatteryNotified = true // Set the flag to true to prevent duplicate notifications

                    val comment = "Low battery level! Please charge the robot."
                    val notification = PushNotification(
                        NotificationData("Battery Status", comment),
                        eToken ?: ""
                    )
                    sendNotification(notification)
                } else if (batteryStatus != null && batteryStatus >= 20.0) {
                    lowBatteryNotified = false // Reset the flag when the battery status increases above the threshold
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database Error: $error") // For debugging
            }
        })



        installSplashScreen().setKeepOnScreenCondition {
            !splashViewModel.isLoading.value
        }
        setContent {
            JoyMowerTheme {
                val screen by splashViewModel.startDestination
                val navController = rememberNavController()
                SetupNavGraph(navController = navController, startDestination = screen)
            }
        }
    }
    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                //Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                //Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            //Log.e(TAG, e.toString())
        }
    }

}
