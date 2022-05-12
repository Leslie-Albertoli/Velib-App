package com.example.velib_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.velib_app.api.StationService
import com.example.velib_app.model.Station
import com.example.velib_app.model.StationDetails
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val TAG = "MapFragment"

class MapFragment : SupportMapFragment(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null

    private val stations: MutableList<Station> = mutableListOf()
    private val stationDetails: MutableList<StationDetails> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        synchroApi()
        googleMap = p0

        stations.forEach { station ->
            val (_ , name, latitude, longitude) = station
            val velibCoordinate = LatLng(latitude, longitude)
            // EPF position
            val cachan = LatLng(48.78896362751979, 2.3272018540134964)
            googleMap!!.addMarker(MarkerOptions()
                .position(velibCoordinate)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN + 21)) // hue = 141.0
                .title(name))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLng(cachan))
            googleMap!!.setMinZoomPreference(16.0f)
        }

        googleMap?.setOnMarkerClickListener {
            val stationClicked = stations.find { station ->
                it.title.equals(station.name)
            }
            val stationDetailsClicked = stationDetails.find {
                it.station_id == stationClicked?.station_id
            }

            val name = stationClicked?.name
            val numBikesAvailable = stationDetailsClicked?.numBikesAvailable.toString()
            val numDocksAvailable= stationDetailsClicked?.numDocksAvailable.toString()
            val intent = Intent(getActivity(), DetailsFragment::class.java)
            /*intent.putExtra(name, "name")
            intent.putExtra(numBikesAvailable, "numBikesAvailable")
            intent.putExtra(numDocksAvailable, "numDocksAvailable")*/
            intent.putExtra("name", name)
            intent.putExtra("numBikesAvailable", numBikesAvailable)
            intent.putExtra("numDocksAvailable", numDocksAvailable)
            startActivity(intent)

            /*Log.d(TAG, "synchroApiClicked: $stationClicked")
            Log.d(TAG, "synchroApiClickedDetails: $stationDetailsClicked")*/
            /*Log.d(TAG, "name: $name")
            Log.d(TAG, "numBikesAvailable: $numBikesAvailable")
            Log.d(TAG, "numDocksAvailable: $numDocksAvailable")*/
            true
        }
        // Set default position
        // Add a marker in Sydney and move the camera
//        val vietnam = LatLng(14.0583, 108.2772) // 14.0583° N, 108.2772° E
//        googleMap!!.addMarker(MarkerOptions().position(vietnam).title("Marker in Vietnam"))
//        googleMap?.moveCamera(CameraUpdateFactory.newLatLng(vietnam))
//        googleMap?.setOnMapClickListener { latLng ->
//            val markerOptions = MarkerOptions()
//            markerOptions.position(latLng)
//            markerOptions.title(latLng.latitude.toString() + " : " + latLng.longitude)
//            // Clear previously click position.
//            googleMap?.clear()
//            // Zoom the Marker
//            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
//            // Add Marker on Map
//            googleMap?.addMarker(markerOptions)
//        }
    }

    private fun synchroApi() {

        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()


        val retrofit = Retrofit.Builder()
            .baseUrl("https://velib-metropole-opendata.smoove.pro/opendata/Velib_Metropole/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()

        val service = retrofit.create(StationService::class.java)

        runBlocking {
            val resultStation = service.getStations()
            val resultStationDetails = service.getStationDetails()
            Log.d(TAG, "synchroApi: ${resultStation.data.stations}")
            Log.d(TAG, "synchroApi: ${resultStationDetails.data.stations}")

            resultStation.data.stations.map {
                stations.add(it)
            }

            resultStationDetails.data.stations.map {
                stationDetails.add(it)
            }
        }
    }

}