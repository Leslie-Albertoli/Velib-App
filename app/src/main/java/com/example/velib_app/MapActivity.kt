package com.example.velib_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.velib_app.api.StationService
import com.example.velib_app.model.Station
import com.example.velib_app.model.StationDetails
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.google.android.libraries.places.api.Places

private const val TAG = "MapActivity"
class MapActivity : AppCompatActivity() {

    private val pERMISSION_ID = 42

    private var mapFragment: SupportMapFragment? = null
    private var currentLocation: LatLng = LatLng(48.78896362751979, 2.3272018540134964)
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var mMap: GoogleMap

    private val stations: MutableList<Station> = mutableListOf()
    private val stationDetails: MutableList<StationDetails> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_map) as? SupportMapFragment

        mapFragment?.getMapAsync {
            mMap = it
            getLastLocation()
            synchroApi()
            addMarkers(mMap)
        }


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

    private fun addMarkers(googleMap: GoogleMap) {
        stations.forEach { station ->
            val (_ , name, latitude, longitude) = station
            val velibCoordinate = LatLng(latitude, longitude)

            googleMap.addMarker(
                MarkerOptions()
                .position(velibCoordinate)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN + 21)) // hue = 141.0 activity?.let { bitmapDescriptorFromVector(it, R.drawable.ic_baseline_directions_bike_24) }
                .title(name))

        }

        googleMap.setOnMarkerClickListener {
            val stationClicked = stations.find { station ->
                it.title.equals(station.name)
            }
            val stationDetailsClicked = stationDetails.find {
                it.station_id == stationClicked?.station_id
            }
            Log.d(TAG, "synchroApiClicked: $stationClicked")
            Log.d(TAG, "synchroApiClickedDetails: $stationDetailsClicked")

            val name = stationClicked?.name
            val numBikesAvailable = stationDetailsClicked?.numBikesAvailable.toString()
            val numDocksAvailable = stationDetailsClicked?.numDocksAvailable.toString()

            val bundle = Bundle()

            bundle.putString("name", name)
            bundle.putString("numBikes", numBikesAvailable)
            bundle.putString("numDocks", numDocksAvailable)

            Log.d(TAG, "synchroApiClickedName: $name")
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtras(bundle)
//            intent.putExtra("name", name)
//            intent.putExtra("numBikesAvailable", numBikesAvailable)
//            intent.putExtra("numDocksAvailable", numDocksAvailable)
            startActivity(intent)
            true
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (!this::mFusedLocationClient.isInitialized) {
                    requestNewLocationData()
                } else {
                    mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                        val location: Location? = task.result
                        if (location == null) {
                            requestNewLocationData()
                        } else {
                            currentLocation = LatLng(location.latitude, location.longitude)
                            mMap.addMarker(MarkerOptions().position(currentLocation))
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16F))
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    // If current location could not be located, use last location
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            currentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
        }
    }

    // function to check if GPS is on
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // Check if location permissions are
    // granted to the application
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // Request permissions if not granted before
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            pERMISSION_ID
        )
    }

    // What must happen when permission is granted
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == pERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }


}
