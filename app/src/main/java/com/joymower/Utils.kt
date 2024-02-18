package com.joymower

data class ProfileEntry(val name: String, val value: String)

fun getProfileEntries(): List<ProfileEntry> {
    return listOf(ProfileEntry("Email", "JoyMower@gmail.com"),
        ProfileEntry("Twitter", "@Joy_Mower"),
        ProfileEntry("Phone", "00010001"))
}