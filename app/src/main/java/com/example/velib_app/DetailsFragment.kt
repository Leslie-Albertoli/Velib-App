package com.example.velib_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.velib_app.model.Station
import com.example.velib_app.model.StationDetails

private const val TAG = "Extra"

class DetailsFragment : AppCompatActivity(){

    private var detailsStationName : TextView? = null
    private var detailsNbBike : TextView? = null
    private var detailsNbPlace : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_details)

        var name = intent.getStringExtra("name")
        val numBikesAvailable = intent.getStringExtra("numBikesAvailable")
        val numDocksAvailable = intent.getStringExtra("numDocksAvailable")

        detailsStationName = findViewById<TextView>(R.id.details_station_name_textview)
        detailsNbBike = findViewById<TextView>(R.id.details_nb_bike_textview)
        detailsNbPlace = findViewById<TextView>(R.id.details_nb_place_textview)

        detailsStationName?.setText(name)
        detailsNbBike?.setText(numBikesAvailable)
        detailsNbPlace?.setText(numDocksAvailable)

        /*
        https://fr.acervolima.com/comment-ajouter-et-personnaliser-le-bouton-retour-de-la-barre-d-action-dans-android/
        bouton retour
        favoris
        ajout données
        changer nom fragment -> activity

        Log.d(TAG, "name: $name")
        Log.d(TAG, "numBikesAvailable: $numBikesAvailable")
        Log.d(TAG, "numDocksAvailable: $numDocksAvailable")*/
    }
}