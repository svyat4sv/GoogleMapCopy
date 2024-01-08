package com.example.googlemapcopy

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var editTextLocation: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Инициализация Google Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBhVAf2aJpZhxPh_8_AbLAVcblQIE3wxYY")
        }

        // Настройка EditText и кнопки
        editTextLocation = findViewById(R.id.editTextLocation)
        val buttonFindPath = findViewById<Button>(R.id.buttonFindPath)
        buttonFindPath.setOnClickListener {
            val location = editTextLocation.text.toString()
            if (location.isNotEmpty()) {
                findPath(location)
            }
        }

        // Инициализация карты
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Дополнительные настройки карты по желанию
    }
    private fun findPath(destination: String) {
        val placesClient = Places.createClient(this)
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(destination)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            for (prediction in response.autocompletePredictions) {
                placesClient.fetchPlace(FetchPlaceRequest.builder(prediction.placeId, listOf(Place.Field.LAT_LNG)).build())
                    .addOnSuccessListener { fetchPlaceResponse ->
                        val place = fetchPlaceResponse.place
                        place.latLng?.let {
                            drawPath(it)
                        }
                    }.addOnFailureListener { e ->
                        if (e is ApiException) {
                            Log.e("MapsActivity", "Place not found: ${e.statusCode}")
                        }
                    }
            }
        }.addOnFailureListener { e ->
            if (e is ApiException) {
                Log.e("MapsActivity", "Place not found: ${e.statusCode}")
            }
        }
    }

    private fun drawPath(destinationLatLng: LatLng) {
        mMap.clear() // Очистить все текущие маркеры
        mMap.addMarker(MarkerOptions().position(destinationLatLng).title("Destination"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 10f))
    }
}