package com.example.velib_app

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.velib_app.bdd.*
import kotlinx.coroutines.runBlocking
import java.util.*

private const val TAG = "DetailsActivity"
private const val RENTAL_CREDIT_CARD_TEXT = "Paiement disponible par carte de crédit"
//astuce : ctrl + alt + L pour réaligner tout le code

class DetailsActivity : AppCompatActivity() {
    var stationIdThis: Long = -1
    var menuActivity: Menu? = null

    lateinit var stationEntity: StationEntity

    @RequiresApi(Build.VERSION_CODES.O)
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
        val detailsLastReportedTextView = findViewById<TextView>(R.id.details_last_reported_text_view)
        val detailsRentalMethodsTextView = findViewById<TextView>(R.id.rental_methods_text_view)

        val bundle = intent.extras
        val stationDatabase = StationDatabase.createDatabase(this)
        val stationDao = stationDatabase.stationDao()
        if (bundle !== null) {
            stationIdThis = bundle.getString("stationId")?.toLong() ?: -1
        }

        runBlocking {
            stationEntity = stationDao.findByStationIdStation(stationIdThis)
        }

        detailsStationNameTextView.text = stationEntity.name
        detailsNbBikeTextView.text = stationEntity.numBikesAvailable.toString()
        detailsNbPlaceTextView.text = stationEntity.numDocksAvailable.toString()
        detailsCapacityTextView.text = stationEntity.capacity.toString()
        detailsNbMechanicalTextView.text =
            stationEntity.numBikesAvailableTypesMechanical.toString()
        detailsNbElectricalTextView.text =
            stationEntity.numBikesAvailableTypesElectrical.toString()
        detailsLastReportedTextView.text = "Dernière mise à jour le : ${getDateTime(stationEntity.last_reported)}"
        if (stationEntity.rental_methods) {
            detailsRentalMethodsTextView.text = RENTAL_CREDIT_CARD_TEXT
        }
    }

    override fun onResume() {
        super.onResume()
        val bddFavoris = FavorisDatabase.createDatabase(this)
        val favorisDao = bddFavoris.favorisDao()
        if (isFavoris(favorisDao)) {
            menuActivity?.findItem(R.id.item_favoris)?.setIcon(R.drawable.im_favoris_star_on)
        } else {
            menuActivity?.findItem(R.id.item_favoris)?.setIcon(R.drawable.im_favoris_star_off)
        }
        bddFavoris.close()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu, menu)

        menuActivity = menu

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.im_arrow_back);
        actionBar?.setDisplayHomeAsUpEnabled(true);

        val bddFavoris = FavorisDatabase.createDatabase(this)
        val favorisDao = bddFavoris.favorisDao()
        if (isFavoris(favorisDao)) {
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

        when (item.itemId) {
            R.id.item_map -> {
                val intent = Intent(this, MapActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            }
            R.id.item_liste_favoris -> {
                val intent = Intent(this, FavorisActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            }
            R.id.item_favoris -> {
                val bddFavoris = FavorisDatabase.createDatabase(this)
                val favorisDao = bddFavoris.favorisDao()
                if (!isFavoris(favorisDao)) {
                    item.setIcon(R.drawable.im_favoris_star_on)
                    insertFavoris(favorisDao)
                    Toast.makeText(this, "Favoris ajouté", Toast.LENGTH_LONG).show()
                } else {
                    item.setIcon(R.drawable.im_favoris_star_off)
                    deleteFavoris(favorisDao)
                    Toast.makeText(this, "Favoris supprimé", Toast.LENGTH_LONG).show()
                }
                bddFavoris.close()
            }
        }
        return true
    }

    private fun isFavoris(favorisDao: FavorisDao): Boolean {
        var isFavoris: Boolean
        runBlocking {
            val findByStationIdFavoris: FavorisEntity = favorisDao.findByStationId(stationIdThis)
            isFavoris = findByStationIdFavoris != null
        }
        return isFavoris
    }

    private fun insertFavoris(favorisDao: FavorisDao) {
        val stationIdLongFavorisEntity = FavorisEntity(stationIdThis)
        runBlocking {
            favorisDao.insert(stationIdLongFavorisEntity)
        }
    }

    private fun deleteFavoris(favorisDao: FavorisDao) {
        val stationIdLongFavorisEntity = FavorisEntity(stationIdThis)
        runBlocking {
            favorisDao.delete(stationIdLongFavorisEntity)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDateTime(timeStamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.FRANCE)
        val date = Date(timeStamp * 1000)
        return sdf.format(date)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_details_land)
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_details)
        }
    }
}