package com.joymower.util

import androidx.annotation.DrawableRes
import com.joymower.R

sealed class OnBoardingPage(
    @DrawableRes
    val image: Int,
    val title: String,
    val description: String
) {
    object First : OnBoardingPage(
        image = R.drawable.first,
        title = "Live Feed Stream",
        description = "With JoyMower, you can remotely see your garden, grass height condition!"
    )

    object Second : OnBoardingPage(
        image = R.drawable.second,
        title = "Manual Control",
        description = "With the Manual Control feature, you can take full control of your robot's movement and explore your environment in real-time!"
    )

    object Third : OnBoardingPage(
        image = R.drawable.third,
        title = "Alerts and Notifications",
        description = "With the Alerts and Notifications feature, you can receive real-time updates about what's happening in your environment!"
    )
}
