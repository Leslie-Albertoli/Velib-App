package com.example.velib_app

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.velib_app.bdd.FavorisDao
import com.example.velib_app.bdd.FavorisDatabase
import com.example.velib_app.bdd.FavorisEntity
import kotlinx.coroutines.runBlocking


private const val TAG = "DetailsActivity"

/*
astuce : ctrl + alt + L pour réaligner tout le code

Log.d(TAG, "numDocks: $numDocksAvailable")

btn_star_big_off
else -> {
    Toast.makeText(this, "Action inconnu", Toast.LENGTH_LONG).show()
}
*/

class DetailsActivity : AppCompatActivity() {
    var stationIdThis: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_details)

        val detailsStationNameTextView = findViewById<TextView>(R.id.details_station_name_textview)
        val detailsNbBikeTextView = findViewById<TextView>(R.id.details_nb_bike_textview)
        val detailsNbPlaceTextView = findViewById<TextView>(R.id.details_nb_place_textview)
        val detailsCapacityTextView = findViewById<TextView>(R.id.details_capacity_textview)
        val detailsNbMechanicalTextView =
            findViewById<TextView>(R.id.details_nb_mechanical_textview)
        val detailsNbElectricalTextView =
            findViewById<TextView>(R.id.details_nb_electrical_textview)

        val bundle = intent.extras

        if (bundle !== null) {
            stationIdThis = bundle.getString("stationId")?.toLong() ?: -1
            detailsStationNameTextView.text = bundle.getString("name")
            detailsNbBikeTextView.text = bundle.getString("numBikes")
            detailsNbPlaceTextView.text = bundle.getString("numDocks")
            detailsCapacityTextView.text = bundle.getString("capacity")
            detailsNbMechanicalTextView.text =
                bundle.getString("numBikesAvailableTypesMechanical")
            detailsNbElectricalTextView.text =
                bundle.getString("numBikesAvailableTypesElectrical")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.im_arrow_back);
        actionBar?.setDisplayHomeAsUpEnabled(true);

        val bddFavoris = FavorisDatabase.createDatabase(this)
        val favorisDao = bddFavoris.favorisDao()
        if (isFavoris(favorisDao)) { //==false
            menu?.findItem(R.id.item_favoris)?.setIcon(R.drawable.im_favoris_star_on)
        } else {
            menu?.findItem(R.id.item_favoris)?.setIcon(R.drawable.im_favoris_star_off)
        }
        bddFavoris.close()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        when (item?.itemId) {
            R.id.item_favoris -> {
                val bddFavoris = FavorisDatabase.createDatabase(this)
                val favorisDao = bddFavoris.favorisDao()
                /*runBlocking {
                    val getAll: List<FavorisEntity> = favorisDao.getAll()
                    Log.d(TAG, "1-----------------: $getAll")
                }*/
                if (!isFavoris(favorisDao)) { //==false
                    item.setIcon(R.drawable.im_favoris_star_on)
                    insertFavoris(favorisDao)
                    Toast.makeText(this, "Favoris ajouté", Toast.LENGTH_LONG).show()
                } else {
                    item.setIcon(R.drawable.im_favoris_star_off)
                    deleteFavoris(favorisDao)
                    Toast.makeText(this, "Favoris supprimé", Toast.LENGTH_LONG).show()
                }
                /*runBlocking {
                    val getAll: List<FavorisEntity> = favorisDao.getAll()
                    Log.d(TAG, "2-----------------: $getAll")
                }*/
                bddFavoris.close()
            }
        }
        return true
    }

    fun isFavoris(favorisDao: FavorisDao): Boolean {
        var isFavoris = false
        runBlocking {
            val findByStationIdFavoris: FavorisEntity = favorisDao.findByStationId(stationIdThis)
            isFavoris = findByStationIdFavoris != null
        }
        Log.d(TAG, "isFavoris: $isFavoris")
        return isFavoris
    }

    fun insertFavoris(favorisDao: FavorisDao) {
        val stationIdLongFavorisEntity: FavorisEntity = FavorisEntity(stationIdThis)
        runBlocking {
            favorisDao.insert(stationIdLongFavorisEntity)
        }
    }

    fun deleteFavoris(favorisDao: FavorisDao) {
        val stationIdLongFavorisEntity: FavorisEntity = FavorisEntity(stationIdThis)
        runBlocking {
            favorisDao.delete(stationIdLongFavorisEntity)
        }
    }
}