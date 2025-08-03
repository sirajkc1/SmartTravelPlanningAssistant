package com.siraj.smarttravelplanningassistant.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TripDao {
    @Insert
    suspend fun insertTrip(trip: Trip)

    @Query("SELECT * FROM trips WHERE userEmail = :email")
    suspend fun getTripsByUser(email: String): List<Trip>
}
