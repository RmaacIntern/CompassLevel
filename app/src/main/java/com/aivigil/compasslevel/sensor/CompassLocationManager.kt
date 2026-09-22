package com.aivigil.compasslevel.sensor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val latitudeDms: String = "00°00'00\" N",
    val longitudeDms: String = "00°00'00\" E",
    val altitudeMeters: Double = 0.0,
    val altitudeFeet: Double = 0.0,
    val speedKmh: Float = 0f,
    val speedMph: Float = 0f,
    val bearing: Float = 0f,
    val accuracyMeters: Float = 0f,
    val address: String = "Locating GPS position...",
    val hasFix: Boolean = false,
    val isGpsEnabled: Boolean = false
)

class CompassLocationManager(private val context: Context) : LocationListener {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationState = MutableStateFlow(LocationData())
    val locationState: StateFlow<LocationData> = _locationState.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isListening = false

    private val fusedLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            updateLocationData(location)
        }
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isLocationEnabled(): Boolean {
        val lm = locationManager ?: return false
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    fun openLocationSettings() {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            _locationState.value = _locationState.value.copy(
                address = "Location permission required",
                hasFix = false
            )
            return
        }

        val locationEnabled = isLocationEnabled()
        _locationState.value = _locationState.value.copy(isGpsEnabled = locationEnabled)

        // 1. Instantly check fused location client lastLocation
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (lastLoc != null) {
                    updateLocationData(lastLoc)
                }
            }
        } catch (e: Exception) {
            // Fallback
        }

        // 2. Instantly check native LocationManager cached locations across all providers
        try {
            locationManager?.let { lm ->
                val allProviders = lm.allProviders
                var bestCached: Location? = null
                for (provider in allProviders) {
                    try {
                        val loc = lm.getLastKnownLocation(provider) ?: continue
                        if (bestCached == null || loc.time > bestCached.time) {
                            bestCached = loc
                        }
                    } catch (e: SecurityException) {
                        // Skip
                    }
                }
                if (bestCached != null && !_locationState.value.hasFix) {
                    updateLocationData(bestCached)
                }
            }
        } catch (e: Exception) {
            // Skip
        }

        // 3. Request immediate fresh fix from FusedLocationProviderClient
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { freshLoc ->
                    if (freshLoc != null) {
                        updateLocationData(freshLoc)
                    }
                }
        } catch (e: Exception) {
            // Skip
        }

        if (isListening) return
        isListening = true

        // 4. Register continuous high-accuracy updates with FusedLocationProviderClient
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(500L)
                .setMinUpdateDistanceMeters(0f)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                fusedLocationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            // If Google Play Services is unavailable, fallback to native LocationManager
        }

        // 5. Also register native LocationManager for all enabled providers as dual-source fallback
        try {
            locationManager?.let { lm ->
                val providers = lm.getProviders(true)
                for (provider in providers) {
                    try {
                        lm.requestLocationUpdates(
                            provider,
                            1000L,
                            0f,
                            this,
                            Looper.getMainLooper()
                        )
                    } catch (e: Exception) {
                        // Skip provider
                    }
                }
            }
        } catch (e: Exception) {
            // Skip
        }
    }

    fun stopLocationUpdates() {
        if (!isListening) return
        isListening = false
        try {
            fusedLocationClient.removeLocationUpdates(fusedLocationCallback)
        } catch (e: Exception) {
            // Ignore
        }
        try {
            locationManager?.removeUpdates(this)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun forceRefresh() {
        stopLocationUpdates()
        startLocationUpdates()
    }

    override fun onLocationChanged(location: Location) {
        updateLocationData(location)
    }

    private fun updateLocationData(location: Location) {
        val lat = location.latitude
        val lon = location.longitude
        val altM = if (location.hasAltitude()) location.altitude else 0.0
        val altFt = altM * 3.28084
        val spdMps = if (location.hasSpeed()) location.speed else 0f
        val spdKmh = spdMps * 3.6f
        val spdMph = spdKmh * 0.621371f
        val brg = if (location.hasBearing()) location.bearing else 0f
        val acc = if (location.hasAccuracy()) location.accuracy else 0f

        val latDms = formatDms(lat, isLatitude = true)
        val lonDms = formatDms(lon, isLatitude = false)

        _locationState.value = _locationState.value.copy(
            latitude = lat,
            longitude = lon,
            latitudeDms = latDms,
            longitudeDms = lonDms,
            altitudeMeters = altM,
            altitudeFeet = altFt,
            speedKmh = spdKmh,
            speedMph = spdMph,
            bearing = brg,
            accuracyMeters = acc,
            hasFix = true,
            isGpsEnabled = isLocationEnabled()
        )

        // Reverse geocoding in background
        coroutineScope.launch {
            reverseGeocode(lat, lon)
        }
    }

    private fun reverseGeocode(lat: Double, lon: Double) {
        if (!Geocoder.isPresent()) {
            _locationState.value = _locationState.value.copy(
                address = "${formatDecimal(lat, 4)}°, ${formatDecimal(lon, 4)}°"
            )
            return
        }
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lon, 1) { addresses ->
                    handleAddress(addresses.firstOrNull(), lat, lon)
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                handleAddress(addresses?.firstOrNull(), lat, lon)
            }
        } catch (e: Exception) {
            // Fallback gracefully to coordinates
            _locationState.value = _locationState.value.copy(
                address = "${formatDecimal(lat, 4)}°, ${formatDecimal(lon, 4)}°"
            )
        }
    }

    private fun handleAddress(address: Address?, lat: Double, lon: Double) {
        if (address != null) {
            val parts = mutableListOf<String>()
            address.subLocality?.let { parts.add(it) }
            address.locality?.let { parts.add(it) }
            address.adminArea?.let { parts.add(it) }
            address.countryName?.let { parts.add(it) }

            val resolved = if (parts.isNotEmpty()) {
                parts.joinToString(", ")
            } else {
                address.getAddressLine(0) ?: "${formatDecimal(lat, 4)}°, ${formatDecimal(lon, 4)}°"
            }
            _locationState.value = _locationState.value.copy(address = resolved)
        } else {
            _locationState.value = _locationState.value.copy(
                address = "${formatDecimal(lat, 4)}°, ${formatDecimal(lon, 4)}°"
            )
        }
    }

    private fun formatDms(deg: Double, isLatitude: Boolean): String {
        val direction = if (isLatitude) {
            if (deg >= 0) "N" else "S"
        } else {
            if (deg >= 0) "E" else "W"
        }
        val absDeg = abs(deg)
        val d = absDeg.toInt()
        val m = ((absDeg - d) * 60).toInt()
        val s = String.format(Locale.US, "%.1f", (absDeg - d - m / 60.0) * 3600.0)
        return "$d°$m'$s\" $direction"
    }

    private fun formatDecimal(value: Double, places: Int): String {
        return String.format(Locale.US, "%.${places}f", value)
    }

    override fun onProviderEnabled(provider: String) {
        _locationState.value = _locationState.value.copy(isGpsEnabled = isLocationEnabled())
        startLocationUpdates()
    }

    override fun onProviderDisabled(provider: String) {
        _locationState.value = _locationState.value.copy(isGpsEnabled = isLocationEnabled())
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}
