package com.example.velib_app

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "DetailsActivity"

/*
bouton retour https://fr.acervolima.com/comment-ajouter-et-personnaliser-le-bouton-retour-de-la-barre-d-action-dans-android/
favoris
astuce : ctrl + alt + L pour réaligner tout le code ==> Merci !

Log.d(TAG, "numDocks: $numDocksAvailable")
*/

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val detailsStationNameTextView = findViewById<TextView>(R.id.details_station_name_textview)
        val detailsNbBikeTextView = findViewById<TextView>(R.id.details_nb_bike_textview)
        val detailsNbPlaceTextView = findViewById<TextView>(R.id.details_nb_place_textview)
        val detailsCapacityTextView = findViewById<TextView>(R.id.details_capacity_textview)
        val detailsNbMechanicalTextView = findViewById<TextView>(R.id.details_nb_mechanical_textview)
        val detailsNbElectricalTextView = findViewById<TextView>(R.id.details_nb_electrical_textview)

        val bundle = intent.extras

        if (bundle !== null) {
            detailsStationNameTextView.text = bundle.getString("name")
            detailsNbBikeTextView.text = bundle.getString("numBikes")
            detailsNbPlaceTextView.text = bundle.getString("numDocks")
            detailsCapacityTextView.text = bundle.getString("capacity")
            detailsNbMechanicalTextView.text = bundle.getString("num_bikes_available_types_mechanical")
            detailsNbElectricalTextView.text = bundle.getString("num_bikes_available_types_electrical")

        }
        /*val a =bundle?.getString("num_bikes_available_types")
        Log.d(TAG, "num_bikes_available_types: $a")*/
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item?.itemId) {
            R.id.item_favoris -> {
                var isFavoris = false
                if (isFavoris == false) {
                    item.setIcon(R.drawable.im_favoris_star_on)
                    Toast.makeText(this, "Favoris", Toast.LENGTH_LONG).show()
                    isFavoris = true
                } else {
                    item.setIcon(R.drawable.im_favoris_star_off)
                    isFavoris = false
                }
            }
            else -> {
                Toast.makeText(this, "Action inconnu", Toast.LENGTH_LONG).show()
            }
        }
        return true
    }

/*val dbFavoris = Room.databaseBuilder(
    applicationContext,
    FavorisDatabase::class.java, "listFavoris.db"
).build()

val FavorisDao = dbFavoris.FavorisDao()
val users: List<FavorisEntity> = FavorisDao.getAll()*/


}