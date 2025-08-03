package com.siraj.smarttravelplanningassistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siraj.smarttravelplanningassistant.database.Trip
import com.siraj.smarttravelplanningassistant.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripViewModel(private val repository: TripRepository) : ViewModel() {

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            repository.addTrip(trip)
            loadTrips(trip.userEmail)
        }
    }

    fun loadTrips(userEmail: String) {
        viewModelScope.launch {
            _trips.value = repository.getTripsForUser(userEmail)
        }
    }
}
