package com.siraj.smarttravelplanningassistant.repository

import com.siraj.smarttravelplanningassistant.database.Trip
import com.siraj.smarttravelplanningassistant.database.TripDao

class TripRepository(private val tripDao: TripDao) {
    suspend fun addTrip(trip: Trip) {
        tripDao.insertTrip(trip)
    }

    suspend fun getTripsForUser(email: String): List<Trip> {
        return tripDao.getTripsByUser(email)
    }
}
