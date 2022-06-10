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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.BaseColumns
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CursorAdapter
import android.widget.SearchView
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.example.velib_app.api.StationService
import com.example.velib_app.bdd.StationDatabase
import com.example.velib_app.bdd.StationEntity
import com.example.velib_app.utils.ActionButton
import com.example.velib_app.utils.CheckNetworkConnection
import com.example.velib_app.utils.isInternetOn
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val TAG = "MapActivity"
private const val PERMISSION_ID = 42
private const val MAPVIEW_BUNDLE_KEY: String = "MapViewBundleKey"


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var stationEntityList: MutableList<StationEntity> = mutableListOf()
    private var currentLocation: LatLng = LatLng(48.78896362751979, 2.3272018540134964)
    private var actionButtonBoolean = ActionButton.NONE

    lateinit var mapView: MapView
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var mMap: GoogleMap
    lateinit var locationSearchView: SearchView
    lateinit var cursorAdapter: CursorAdapter
    lateinit var clusterManager: ClusterManager<StationEntity>
    lateinit var mechanicalBikeFloatingActionButton: FloatingActionButton
    lateinit var eBikeFloatingActionButton: FloatingActionButton
    lateinit var syncFloatingActionButton: FloatingActionButton
    private lateinit var checkNetworkConnection: CheckNetworkConnection


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

        val locationFloatingActionButton = findViewById<FloatingActionButton>(R.id.location_image_button)

        syncFloatingActionButton = findViewById(R.id.synchro_api_image_button)

        locationFloatingActionButton.setOnClickListener {
            requestPermissions()
            if (checkPermissions()) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Activez la localisation pour pouvoir utiliser ce bouton", Toast.LENGTH_SHORT).show()
            }
        }

        mapView.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (isInternet(this)) {
            isInternetOn = true
            synchroApi()
        } else {
            val stationDatabase = StationDatabase.createDatabase(this)
            val stationDao = stationDatabase.stationDao()
            runBlocking {
                stationEntityList = stationDao.getAllStation()
            }
            mechanicalBikeFloatingActionButton.setOnClickListener {
                Toast.makeText(this, "Activez votre connection internet pour utiliser ce bouton", Toast.LENGTH_SHORT).show()
            }
            syncFloatingActionButton.setOnClickListener {
                Toast.makeText(this,
                    "Activer votre connexion internet pour resynchroniser les données des stations",
                    Toast.LENGTH_LONG).show()
            }
        }

        if (stationEntityList.isNotEmpty()) {
            setFloatingButtonsClickListenerIfStationsAvailable()
        }
        addClusteredMarkers(mMap, actionButtonBoolean)
        configureSuggestions(locationSearchView, cursorAdapter)
        getLastLocation()
        callNetworkConnection()

    }

    private fun setFloatingButtonsClickListenerIfStationsAvailable() {
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

    private fun manageActionButton(actionButton: ActionButton) {
        actionButtonBoolean = when(actionButtonBoolean) {
            ActionButton.NONE -> {
                actionButton
            }
            actionButton -> {
                ActionButton.NONE
            }
            else -> {
                actionButton
            }
        }
    }

    // pour faire les requetes API de manière asynchrone mais n'update pas l'UI dès que l'appel est terminé...
//    val retrofit = Retrofit.Builder()
//        .baseUrl("https://velib-metropole-opendata.smoove.pro/opendata/Velib_Metropole/")
//        .addConverterFactory(MoshiConverterFactory.create())
//        .build()
//        .create(StationService::class.java)
//
//
//    GlobalScope.launch(Dispatchers.IO) {
//        val stationsResult = retrofit.getStations()
//        val stationsDetailsResults = retrofit.getStationDetails()
//        if (stationsResult.isSuccessful) {
//            stationsResult.body()?.data?.stations?.map {
//                stations.add(it)
//            }
//
//            stationsResult.body()?.data?.stations?.map {
//                stationsTitle.add(it.name)
//            }
//        }
//
//        if (stationsDetailsResults.isSuccessful) {
//            stationsDetailsResults.body()?.data?.stations?.map {
//                stationDetails.add(it)
//            }
//        }
//    }

    private fun asynchroApi(view: View) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://velib-metropole-opendata.smoove.pro/opendata/Velib_Metropole/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(StationService::class.java)


        GlobalScope.launch(Dispatchers.IO) {
            val stationsResult = retrofit.getStations()
            val stationsDetailsResults = retrofit.getStationDetails()


            val stationDatabase = StationDatabase.createDatabase(applicationContext)
            val stationDao = stationDatabase.stationDao()

            stationDao.deleteAllStations()

            stationsResult.data.stations.zip(stationsDetailsResults.data.stations).map {
                StationEntity(
                    it.first.station_id,
                    it.first.name,
                    it.first.lat,
                    it.first.lon,
                    it.first.capacity,
                    it.first.stationCode,
                    it.second.numBikesAvailable,
                    it.second.num_bikes_available_types[0]["mechanical"],
                    it.second.num_bikes_available_types[1]["ebike"],
                    it.second.numDocksAvailable,
                    it.second.is_installed,
                    it.second.is_returning,
                    it.second.is_renting,
                    it.second.last_reported,
                    !it.first.rental_methods.isNullOrEmpty()
                )
            }.map {
                stationDao.insertStation(it)
                stationEntityList.add(it)
            }

            stationDatabase.close()
            view.clearAnimation()
        }

        runOnUiThread {
            updateClusteredMarkers(mMap, actionButtonBoolean)
        }
    }

    private fun synchroApi() {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://velib-metropole-opendata.smoove.pro/opendata/Velib_Metropole/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(StationService::class.java)

        runBlocking {
            val resultStation = service.getStations()
            val resultStationDetails = service.getStationDetails()
            Log.d(TAG, "synchroApi: ${resultStation.data.stations}")
            Log.d(TAG, "synchroApi: ${resultStationDetails.data.stations}")

            val stationDatabase = StationDatabase.createDatabase(applicationContext)
            val stationDao = stationDatabase.stationDao()

            stationDao.deleteAllStations()

            resultStation.data.stations.zip(resultStationDetails.data.stations).map {
                StationEntity(
                    it.first.station_id,
                    it.first.name,
                    it.first.lat,
                    it.first.lon,
                    it.first.capacity,
                    it.first.stationCode,
                    it.second.numBikesAvailable,
                    it.second.num_bikes_available_types[0]["mechanical"],
                    it.second.num_bikes_available_types[1]["ebike"],
                    it.second.numDocksAvailable,
                    it.second.is_installed,
                    it.second.is_returning,
                    it.second.is_renting,
                    it.second.last_reported,
                    !it.first.rental_methods.isNullOrEmpty()
                )
            }.map {
                stationDao.insertStation(it)
                stationEntityList.add(it)
            }

            stationDatabase.close()
        }

    }

    private fun addClusteredMarkers(googleMap: GoogleMap, actionButton: ActionButton) {

        clusterManager = ClusterManager<StationEntity>(this, googleMap)
        clusterManager.renderer = StationRenderer(this, googleMap, clusterManager, actionButton)

        clusterManager.addItems(stationEntityList)
        clusterManager.cluster()

        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }

        clusterManager.setOnClusterItemClickListener {

            val stationId = it.station_id.toString()

            val bundle = Bundle()

            bundle.putString("stationId", stationId)
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
            true

        }
    }

    private fun updateClusteredMarkers(googleMap: GoogleMap, actionButton: ActionButton) {
        clusterManager.renderer = StationRenderer(this, googleMap, clusterManager, actionButton)
        clusterManager.clearItems()
        clusterManager.addItems(stationEntityList)
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
                val stationFound = stationEntityList.find {
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
                    stationEntityList.forEachIndexed { index, station ->
                        Log.d(TAG, station.name)
                        if (station.name.contains(query, true)) {
                            cursor.addRow(arrayOf(index, station.name))
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
        menu?.findItem(R.id.item_map)?.isVisible = false
        menu?.findItem(R.id.item_favoris)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        val intent = Intent(this, FavorisActivity::class.java)
        startActivity(intent)
        return true
    }

    // check if there is internet during launch
    private fun isInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            }
        }
        return false
    }

    // monitor internet activity when inside the app
    private fun callNetworkConnection() {
        checkNetworkConnection = CheckNetworkConnection(application)
        checkNetworkConnection.observe(this) { isConnected ->
            isInternetOn = isConnected
            if (isInternetOn) {
                if (stationEntityList.isEmpty()) {
                    synchroApi()
                    addClusteredMarkers(mMap, actionButtonBoolean)
                }
                syncFloatingActionButton.setOnClickListener {
                    val rotation = AnimationUtils.loadAnimation(this, R.anim.ic_play_synchro_api)
                    it.startAnimation(rotation)
                    asynchroApi(it)
                }
            } else {
                syncFloatingActionButton.setOnClickListener {
                    Toast.makeText(this,
                        "Activer votre connexion internet pour resynchroniser les données des stations",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}
