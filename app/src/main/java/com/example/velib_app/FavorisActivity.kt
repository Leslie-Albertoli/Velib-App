package com.example.velib_app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.velib_app.bdd.FavorisDao
import com.example.velib_app.bdd.FavorisDatabase
import com.example.velib_app.bdd.FavorisEntity
import com.example.velib_app.model.Station
import com.example.velib_app.model.StationDetails
import kotlinx.coroutines.runBlocking

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
        runBlocking {
            val getAllId: List<Long> = favorisDao.getAllId()
            var favorisAdapter: FavorisAdapter? = null
            favorisAdapter = FavorisAdapter(getAllId)
            recyclerView.adapter = favorisAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.im_arrow_back);
        actionBar?.setDisplayHomeAsUpEnabled(true);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }
}