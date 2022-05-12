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

private const val TAG = "DetailsActivity"

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

//        val numDocksAvailable = intent.getStringExtra("numDocksAvailable")

//        Log.d(TAG, "numBikes: $numBikesAvailable")
//        Log.d(TAG, "numDocks: $numDocksAvailable")

        val detailsStationNameTextView = findViewById<TextView>(R.id.details_station_name_textview)
        val detailsNbBikeTextView = findViewById<TextView>(R.id.details_nb_bike_textview)
        val detailsNbPlaceTextView = findViewById<TextView>(R.id.details_nb_place_textview)

        val bundle = intent.extras

        if (bundle !== null) {
            detailsStationNameTextView.text = bundle.getString("name")
            detailsNbBikeTextView.text = bundle.getString("numBikes")
            detailsNbPlaceTextView.text = bundle.getString("numDocks")
        }
//        detailsNbBikeTextView.text = numBikesAvailable
//        detailsNbPlaceTextView.text = numDocksAvailable

        /*
        https://fr.acervolima.com/comment-ajouter-et-personnaliser-le-bouton-retour-de-la-barre-d-action-dans-android/
        bouton retour
        favoris
        ajout données
        changer nom fragment -> activity
        astuce : ctrl + alt + L pour réaligner tout le code

        Log.d(TAG, "name: $name")
        Log.d(TAG, "numBikesAvailable: $numBikesAvailable")
        Log.d(TAG, "numDocksAvailable: $numDocksAvailable")*/
    }
}