package com.siraj.smarttravelplanningassistant

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.siraj.smarttravelplanningassistant.data.AppDatabase
import com.siraj.smarttravelplanningassistant.data.Trip
import com.siraj.smarttravelplanningassistant.data.TripDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TripDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TripDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()  // For testing only, avoid on production
            .build()
        dao = db.tripDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertTrip_shouldAppearInList() = runTest {
        val trip = Trip(
            id = 0,
            userEmail = "testuser@example.com",
            destination = "Melbourne",
            startDate = "2025-09-01",
            endDate = "2025-09-05",
            travelers = 1,
            notes = "Holiday trip"
        )

        dao.insertTrip(trip)
        val trips = dao.getTripsByUser("testuser@example.com")
        assertTrue(trips.any { it.destination == "Melbourne" })
    }

    @Test
    fun insertMultipleTrips_andCheckCount() = runTest {
        val trip1 = Trip(
            id = 0,
            userEmail = "testuser@example.com",
            destination = "Gold Coast",
            startDate = "2025-10-01",
            endDate = "2025-10-05",
            travelers = 2,
            notes = "Family trip"
        )
        val trip2 = Trip(
            id = 0,
            userEmail = "testuser@example.com",
            destination = "Brisbane",
            startDate = "2025-11-01",
            endDate = "2025-11-03",
            travelers = 1,
            notes = "Business trip"
        )

        dao.insertTrip(trip1)
        dao.insertTrip(trip2)

        val trips = dao.getTripsByUser("testuser@example.com")
        assertEquals(2, trips.size)
    }
}
