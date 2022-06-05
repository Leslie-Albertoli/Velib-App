package com.example.velib_app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.velib_app.bdd.*
import com.example.velib_app.model.Station
import com.example.velib_app.model.StationDetails
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class FavorisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoris)

        val recyclerView =
            findViewById<RecyclerView>(R.id.list_clients_recyclerview)

        recyclerView.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            ) //Affichage lineaire vertical

        val bddFavoris = FavorisDatabase.createDatabase(this)
        val favorisDao = bddFavoris.favorisDao()
        val stationDatabase = StationDatabase.createDatabase(this)
        val stationDao = stationDatabase.stationDao()
        runBlocking {
            val getAllId: List<Long> = favorisDao.getAllId()
            var getAllIdNotNull: List<Long>? = null

            getAllIdNotNull?.forEach {
                val getSattionId = stationDao.findByStationIdStation(it).station_id
                if (getSattionId != null) {
                    getAllIdNotNull += it
                }
            }

            var favorisAdapter: FavorisAdapter? = null
            if (getAllIdNotNull != null) {
                favorisAdapter = FavorisAdapter(getAllIdNotNull)
            }
            recyclerView.adapter = favorisAdapter
        }
        stationDatabase.close()
        bddFavoris.close()
    }

    override fun onResume() {
        super.onResume()
        setContentView(R.layout.activity_favoris)

        val recyclerView =
            findViewById<RecyclerView>(R.id.list_clients_recyclerview)

        recyclerView.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            ) //Affichage lineaire vertical

        val bddFavoris = FavorisDatabase.createDatabase(this)
        val favorisDao = bddFavoris.favorisDao()
        runBlocking {
            val getAllId: List<Long> = favorisDao.getAllId()
            var favorisAdapter: FavorisAdapter? = null
            favorisAdapter = FavorisAdapter(getAllId)
            recyclerView.adapter = favorisAdapter
        }
        bddFavoris.close()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.menu, menu)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.im_arrow_back);
        actionBar?.setDisplayHomeAsUpEnabled(true);

        menu?.findItem(R.id.item_liste_favoris)?.setVisible(false)
        menu?.findItem(R.id.item_favoris)?.setVisible(false)
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
            R.id.item_map -> {
                val intent = Intent(this, MapActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            }
        }
        return true
    }
}