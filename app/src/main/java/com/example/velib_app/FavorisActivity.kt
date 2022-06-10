package com.example.velib_app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.velib_app.bdd.FavorisDatabase
import com.example.velib_app.utils.CheckNetworkConnection
import com.example.velib_app.utils.isInternetOn
import kotlinx.coroutines.runBlocking
class FavorisActivity : AppCompatActivity() {

    private lateinit var checkNetworkConnection: CheckNetworkConnection

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
            recyclerView.adapter = FavorisAdapter(getAllId)
        }
        bddFavoris.close()
    }

    override fun onResume() {
        super.onResume()
        setContentView(R.layout.activity_favoris)

        callNetworkConnection()

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
            recyclerView.adapter = FavorisAdapter(getAllId)
        }
        bddFavoris.close()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.menu, menu)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.im_arrow_back)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        menu?.findItem(R.id.item_liste_favoris)?.isVisible = false
        menu?.findItem(R.id.item_favoris)?.isVisible = false
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
        }
        return true
    }

    private fun callNetworkConnection() {
        checkNetworkConnection = CheckNetworkConnection(application)
        checkNetworkConnection.observe(this) { isConnected ->
            isInternetOn = isConnected
        }
    }
}