package com.example.velib_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.BaseColumns
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.example.velib_app.api.StationService
import com.example.velib_app.bdd.StationDao
import com.example.velib_app.bdd.StationDatabase
import com.example.velib_app.bdd.StationEntity
import com.example.velib_app.model.Station
import com.example.velib_app.model.StationDetails
import com.example.velib_app.utils.ActionButton
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val TAG = "MapActivity"
private const val PERMISSION_ID = 42
private const val MAPVIEW_BUNDLE_KEY: String = "MapViewBundleKey"

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val stations: MutableList<Station> = arrayListOf()
    private val stationsTitle: MutableList<String> = mutableListOf()
    lateinit var clusterManager: ClusterManager<Station>
    lateinit var mechanicalBikeFloatingActionButton: FloatingActionButton
    lateinit var eBikeFloatingActionButton: FloatingActionButton
    val stationDetails: MutableList<StationDetails> = mutableListOf()
    private var currentLocation: LatLng = LatLng(48.78896362751979, 2.3272018540134964)
    private var actionButtonBoolean = ActionButton.NONE

    lateinit var mapView: MapView
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var mMap: GoogleMap
    lateinit var locationSearchView: SearchView
    lateinit var cursorAdapter: CursorAdapter


    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("UseCompatLoadingForDrawables", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_VelibApp)
        setContentView(R.layout.activity_map)

        var mapViewBundle: Bundle? = null

        if (savedInstanceState !== null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(mapViewBundle)


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

        locationSearchView = findViewById(R.id.station_search_view)

        val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val to = intArrayOf(R.id.location_autocomplete)


        cursorAdapter = SimpleCursorAdapter(
            this,
            R.layout.search_item,
            null,
            from,
            to,
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )

        locationSearchView.suggestionsAdapter = cursorAdapter

        mechanicalBikeFloatingActionButton = findViewById(R.id.mechanical_floating_action_button)

        eBikeFloatingActionButton = findViewById(R.id.ebike_floating_action_button)

        val locationImageButton = findViewById<ImageButton>(R.id.location_image_button)

        val syncImageButton = findViewById<ImageButton>(R.id.synchro_api_image_button)

        locationImageButton.setOnClickListener {
            getLastLocation()
        }

        mapView.getMapAsync(this)
        mapView.getMapAsync {
            mMap = it
            addClusteredMarkers(mMap, actionButtonBoolean)
        }

        syncImageButton.setOnClickListener {
            synchroApi()
        }

        mechanicalBikeFloatingActionButton.setOnClickListener {
            if (mechanicalBikeFloatingActionButton.backgroundTintList
                == AppCompatResources.getColorStateList(this, R.color.marker_green)) {
                mechanicalBikeFloatingActionButton.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.teal_200)
            } else {
                mechanicalBikeFloatingActionButton.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.marker_green)
                eBikeFloatingActionButton.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.teal_200)
            }

            manageActionButton(ActionButton.MECHANICAL)
            updateClusteredMarkers(mMap, actionButtonBoolean)
        }

        eBikeFloatingActionButton.setOnClickListener {
            if (eBikeFloatingActionButton.backgroundTintList
                == AppCompatResources.getColorStateList(this, R.color.marker_blue)) {
                eBikeFloatingActionButton.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.teal_200)
            } else {
                eBikeFloatingActionButton.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.marker_blue)
                mechanicalBikeFloatingActionButton.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.teal_200)
            }
            manageActionButton(ActionButton.EBIKE)
            updateClusteredMarkers(mMap, actionButtonBoolean)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLastLocation()
        synchroApi()
        configureSuggestions(locationSearchView, cursorAdapter)
    }

    private fun manageActionButton(actionButton: ActionButton) {
            when(actionButtonBoolean) {
                ActionButton.NONE -> {
                    actionButtonBoolean = actionButton
                    // Toast.makeText(this, "Mechanical activated", Toast.LENGTH_SHORT).show()
                }
                actionButton -> {
                    actionButtonBoolean = ActionButton.NONE
//                    Toast.makeText(this, "none activated", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    actionButtonBoolean = actionButton

                }
            }
    }



//        eBikeFloatingActionButton.setOnClickListener {
//            actionButtonBoolean = when(actionButtonBoolean) {
//                ActionButton.NONE -> ActionButton.EBIKE
//                ActionButton.MECHANICAL -> ActionButton.EBIKE
//                ActionButton.EBIKE -> ActionButton.NONE
//            }
//            manageActionButton(actionButtonBoolean, mMap)
//        }
//        when(actionButton) {
//            ActionButton.MECHANICAL -> {
//                val clusterManager: ClusterManager<Station> = ClusterManager<Station>(this, mMap)
//                clusterManager.renderer = StationRenderer(this, mMap, clusterManager, stationDetails, actionButtonBoolean)
//                clusterManager.addItems(stations)
//                clusterManager.cluster()
//                Toast.makeText(this, "Mechanical activated", Toast.LENGTH_SHORT).show()
//            }
//            ActionButton.EBIKE -> Toast.makeText(this, "ebike activated", Toast.LENGTH_SHORT).show()
//            else -> {
//                Toast.makeText(this, "none activated", Toast.LENGTH_SHORT).show()
//            }
//        }

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

            resultStation.data.stations.map {
                stationsTitle.add(it.name)
            }

            resultStationDetails.data.stations.map {
                stationDetails.add(it)
            }
        }
    }

    private fun addClusteredMarkers(googleMap: GoogleMap, actionButton: ActionButton) {

        clusterManager = ClusterManager<Station>(this, googleMap)
        clusterManager.renderer = StationRenderer(this, googleMap, clusterManager, stationDetails, actionButton)

        clusterManager.addItems(stations)
        clusterManager.cluster()

        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }

        clusterManager.setOnClusterItemClickListener {
            val stationClicked = stations.find { station ->
                it.title == station.name
            }
            val stationDetailsClicked = stationDetails.find {
                it.station_id == stationClicked?.station_id
            }
            Log.d(TAG, "synchroApiClicked: $stationClicked")
            Log.d(TAG, "synchroApiClickedDetails: $stationDetailsClicked")


            if (stationClicked !== null && stationDetailsClicked !== null) {
                val stationIdBdd: Long = stationClicked.station_id
                val nameBdd = stationClicked.name
                val numBikesAvailableBdd = stationDetailsClicked.numBikesAvailable
                val numDocksAvailableBdd = stationDetailsClicked.numDocksAvailable
                val capacityBdd = stationClicked.capacity
                val numBikesAvailableTypesMechanicalBdd =
                    stationDetailsClicked.num_bikes_available_types[0]["mechanical"]
                val numBikesAvailableTypesElectricalBdd =
                    stationDetailsClicked.num_bikes_available_types[1]["ebike"]
                val stationCode = stationClicked.stationCode
                val stationLat = stationClicked.lat
                val stationLon = stationClicked.lon
                val stationIsInstalled = stationDetailsClicked.is_installed
                val stationIsReturning = stationDetailsClicked.is_returning
                val stationIsRenting = stationDetailsClicked.is_renting

                val stationDatabase = StationDatabase.createDatabase(this)
                val stationDao = stationDatabase.stationDao()
                if (!isStation(stationDao, stationIdBdd)) {
                    insertStation(
                        stationDao,
                        stationIdBdd,
                        nameBdd,
                        stationLat,
                        stationLon,
                        capacityBdd,
                        stationCode,
                        numBikesAvailableBdd,
                        numBikesAvailableTypesMechanicalBdd,
                        numBikesAvailableTypesElectricalBdd,
                        numDocksAvailableBdd,
                        stationIsInstalled,
                        stationIsReturning,
                        stationIsRenting
                    )
                }
                stationDatabase.close()
            }

            val stationId = stationClicked?.station_id.toString()
            val name = stationClicked?.name
            val numBikesAvailable = stationDetailsClicked?.numBikesAvailable.toString()
            val numDocksAvailable = stationDetailsClicked?.numDocksAvailable.toString()
            val capacity = stationClicked?.capacity.toString()
            val numBikesAvailableTypesMechanical =
                stationDetailsClicked?.num_bikes_available_types?.get(0)
                    ?.get("mechanical")
                    .toString()
            val numBikesAvailableTypesElectrical =
                stationDetailsClicked?.num_bikes_available_types?.get(1)
                    ?.get("ebike")
                    .toString()

            val bundle = Bundle()

            bundle.putString("stationId", stationId)
            bundle.putString("name", name)
            bundle.putString("numBikes", numBikesAvailable)
            bundle.putString("numDocks", numDocksAvailable)
            bundle.putString("capacity", capacity)
            bundle.putString("numBikesAvailableTypesMechanical", numBikesAvailableTypesMechanical)
            bundle.putString("numBikesAvailableTypesElectrical", numBikesAvailableTypesElectrical)

            Log.d(TAG, "synchroApiClickedName: $name")
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
            true


//            val intent = Intent(this, FavorisActivity::class.java)
//            startActivity(intent)
//            true

        }
    }

    private fun isStation(stationDao: StationDao, stationId: Long): Boolean {
        var isStation: Boolean
        runBlocking {
            val findByStationIdFavorisStation: StationEntity =
                stationDao.findByStationIdStation(stationId)
            isStation = findByStationIdFavorisStation != null
        }
        return isStation
    }

    private fun insertStation(
        stationDao: StationDao,
        stationId: Long,
        name: String?,
        lat: Double?,
        lon: Double?,
        capacity: Int?,
        stationCode: String?,
        numBikesAvailable: Int?,
        numBikesAvailableTypesMechanical: Int?,
        numBikesAvailableTypesElectrical: Int?,
        numDocksAvailable: Int?,
        is_installed: Int?,
        is_returning: Int?,
        is_renting: Int?
    ) {
        val stationIdLongFavorisEntityStation = StationEntity(
            stationId,
            name,
            lat,
            lon,
            capacity,
            stationCode,
            numBikesAvailable,
            numBikesAvailableTypesMechanical,
            numBikesAvailableTypesElectrical,
            numDocksAvailable,
            is_installed,
            is_returning,
            is_renting
        )
        runBlocking {
            stationDao.insertStation(stationIdLongFavorisEntityStation)
        }
    
    }

    private fun updateClusteredMarkers(googleMap: GoogleMap, actionButton: ActionButton) {
        clusterManager.renderer = StationRenderer(this, googleMap, clusterManager, stationDetails, actionButton)
        clusterManager.cluster()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            mMap.isMyLocationEnabled = true
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
                            mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLocation,
                                    16F
                                )
                            )
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
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // Check if location permissions are
    // granted to the application
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // Request permissions if not granted before
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    // What must happen when permission is granted
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun configureSuggestions(searchView: SearchView, cursorAdapter: CursorAdapter) {

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                val notCaseSensitiveQuery: String? = query?.lowercase()
                val stationFound = stations.find {
                    it.name.lowercase() == notCaseSensitiveQuery
                }
                Log.d(TAG, "$stationFound")
                if (stationFound !== null) {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(stationFound.lat, stationFound.lon), 16F
                        )
                    )
                }
                return false
            }


            override fun onQueryTextChange(query: String?): Boolean {

                val cursor =
                    MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))

                query?.let {
                    stationsTitle.forEachIndexed { index, station ->
                        if (station.contains(query, true)) {
                            cursor.addRow(arrayOf(index, station))
                        }
                    }
                }
                cursorAdapter.changeCursor(cursor)
                return true
            }

        })

        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            @SuppressLint("Range")
            override fun onSuggestionClick(position: Int): Boolean {

                val cursor: Cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
                val selection =
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
                searchView.setQuery(selection, true)
                return true
            }

        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle: Bundle? = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        menu?.findItem(R.id.item_map)?.setVisible(false)
        menu?.findItem(R.id.item_favoris)?.setVisible(false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        val intent = Intent(this, FavorisActivity::class.java)
        startActivity(intent)
        return true
    }

}
