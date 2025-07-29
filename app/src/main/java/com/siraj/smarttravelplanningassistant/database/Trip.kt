package com.siraj.smarttravelplanningassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val travelers: Int,
    val notes: String
)
