package com.example.velib_app

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.velib_app.bdd.FavorisDao
import com.example.velib_app.bdd.FavorisDatabase
import com.example.velib_app.bdd.FavorisEntity
import kotlinx.coroutines.runBlocking

private const val TAG = "DetailsActivity"

/*
bouton retour https://fr.acervolima.com/comment-ajouter-et-personnaliser-le-bouton-retour-de-la-barre-d-action-dans-android/
favoris
astuce : ctrl + alt + L pour réaligner tout le code ==> Merci !

Log.d(TAG, "numDocks: $numDocksAvailable")

btn_star_big_off
*/

class DetailsActivity : AppCompatActivity() {
    var stationIdThis: Long = -1
    //var bddFavoris: FavorisDatabase? = null
    //var FavorisDao: FavorisDao? = null
    /*val bddFavoris = FavorisDatabase.createDatabase(this)
    val FavorisDao = bddFavoris.favorisDao()*/

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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item?.itemId) {
            R.id.item_favoris -> {
                val bddFavoris = FavorisDatabase.createDatabase(this)
                val favorisDao = bddFavoris.favorisDao()

                if (!isFavoris(favorisDao)) { //==false
                    item.setIcon(R.drawable.im_favoris_star_on)
                    insertFavoris(favorisDao)
                    Toast.makeText(this, "Favoris ajouté", Toast.LENGTH_LONG).show()
                    //isFavoris = true
                    //=> insert id dans la bdd des favoris
                } else {
                    item.setIcon(R.drawable.im_favoris_star_off)
                    deleteFavoris(favorisDao)
                    Toast.makeText(this, "Favoris supprimé", Toast.LENGTH_LONG).show()
                    //isFavoris = false
                    //=> delete id dans la bdd des favoris
                }
                bddFavoris.close()
            }
            else -> {
                Toast.makeText(this, "Action inconnu", Toast.LENGTH_LONG).show()
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

    /*val bundle = intent.extras
        val stationIdLongFavorisEntity: FavorisEntity = FavorisEntity(stationIdLong)
        val getAllFavoris: List<FavorisEntity> = FavorisDao.getAll()
        val findByStationIdFavoris: FavorisEntity = FavorisDao.findByStationId(stationIdLong)
        val insertAllFavoris: Unit = FavorisDao.insertAll(stationIdLongFavorisEntity)
        val deleteFavoris: Unit = FavorisDao.delete(stationIdLongFavorisEntity)
    */

}