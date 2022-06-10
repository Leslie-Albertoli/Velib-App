package com.example.velib_app

import android.content.Intent
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.velib_app.bdd.*
import kotlinx.coroutines.runBlocking

private const val TAG = "FavorisAdapter"

class FavorisAdapter(private val favorisList: List<Long>) :
    RecyclerView.Adapter<FavorisAdapter.FavorisViewHolder>() {

    class FavorisViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavorisViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val favorisView = inflater.inflate(R.layout.adapter_favoris, parent, false)
        return FavorisViewHolder(favorisView)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: FavorisViewHolder, position: Int) {
        val favoris = favorisList[position] //kotlin list ~ tableau
        Log.d(TAG, "favorisList: $favorisList")
        val favorisTextview =
            holder.view.findViewById<TextView>(R.id.adapter_station_name_textview)
        val favorisImageview =
            holder.view.findViewById<ImageView>(R.id.adapter_favoris_imageview)
        val favorisDeleteImageView =
            holder.view.findViewById<ImageView>(R.id.adapter_favoris_delete_imageview)
        val context = holder.view.context
        val stationDatabase = StationDatabase.createDatabase(context)
        val stationDao = stationDatabase.stationDao()

        favorisTextview.text = "${findStationByStationId(stationDao, favoris)?.name}"

        holder.view.setOnClickListener {
            val stationId = findStationByStationId(stationDao, favoris)?.station_id.toString()
            val name = findStationByStationId(stationDao, favoris)?.name
            val numBikesAvailable =
                findStationByStationId(stationDao, favoris)?.numBikesAvailable.toString()
            val numDocksAvailable =
                findStationByStationId(stationDao, favoris)?.numDocksAvailable.toString()
            val capacity = findStationByStationId(stationDao, favoris)?.capacity.toString()
            val numBikesAvailableTypesMechanical = findStationByStationId(
                stationDao,
                favoris
            )?.numBikesAvailableTypesMechanical.toString()
            val numBikesAvailableTypesElectrical = findStationByStationId(
                stationDao,
                favoris
            )?.numBikesAvailableTypesElectrical.toString()
            stationDatabase.close()
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra("stationId", stationId)
            intent.putExtra("name", name)
            intent.putExtra("numBikes", numBikesAvailable)
            intent.putExtra("numDocks", numDocksAvailable)
            intent.putExtra("capacity", capacity)
            intent.putExtra("numBikesAvailableTypesMechanical", numBikesAvailableTypesMechanical)
            intent.putExtra("numBikesAvailableTypesElectrical", numBikesAvailableTypesElectrical)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            context.startActivity(intent)
        }

        favorisDeleteImageView.setOnClickListener {
            deleteFavorisDialog(it, holder, favorisImageview, favoris)
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun deleteFavorisDialog(
        view: View,
        holder: FavorisViewHolder,
        favorisImageview: ImageView,
        favoris: Long
    ) {
        val context = view.context
        val stationDatabase = StationDatabase.createDatabase(context)
        val stationDao = stationDatabase.stationDao()
        val stationId = findStationByStationId(stationDao, favoris)?.station_id

        val findStationByStationId = findStationByStationId(stationDao, favoris)?.name

        stationDatabase.close()

        AlertDialog.Builder(context)
            .setTitle(R.string.confirm_delete_dialog_title)
            .setMessage(
                Html.fromHtml(
                    "Voulez-vous vraiment supprimer la station <i>${findStationByStationId}</i> de votre liste des favoris ? Cette action est irréversible !",
                    Html.FROM_HTML_MODE_LEGACY
                )
            )
            .setPositiveButton(R.string.yes) { _, _ ->


                if (stationId !== null) {
                    val bddFavoris = FavorisDatabase.createDatabase(context)
                    val favorisDao = bddFavoris.favorisDao()
                    if (!isFavoris(favorisDao, stationId)) {
                        favorisImageview.setImageResource(R.drawable.im_favoris_star_on)
                        insertFavoris(favorisDao, stationId)
                        Toast.makeText(context, "Favoris ajouté", Toast.LENGTH_SHORT).show()
                    } else {
                        deleteFavoris(favorisDao, stationId)
                        val deletePosition: Int = holder.adapterPosition
                        (favorisList as ArrayList).removeAt(deletePosition)
                        notifyItemRemoved(deletePosition)
                        notifyItemRangeChanged(deletePosition, favorisList.size)
                        Toast.makeText(context, "Favoris supprimé", Toast.LENGTH_SHORT).show()
                    }
                    bddFavoris.close()
                }
            }
            .setNegativeButton(R.string.no) { _, _ ->
            }
            .show()
    }

    override fun getItemCount() = favorisList.size

    private fun isFavoris(favorisDao: FavorisDao, stationId: Long): Boolean {
        var isFavoris: Boolean
        runBlocking {
            val findByStationIdFavoris: FavorisEntity = favorisDao.findByStationId(stationId)
            isFavoris = findByStationIdFavoris != null
        }
        return isFavoris
    }

    private fun insertFavoris(favorisDao: FavorisDao, stationId: Long) {
        val stationIdLongFavorisEntity = FavorisEntity(stationId)
        runBlocking {
            favorisDao.insert(stationIdLongFavorisEntity)
        }
    }

    private fun deleteFavoris(favorisDao: FavorisDao, stationId: Long) {
        val stationIdLongFavorisEntity = FavorisEntity(stationId)
        runBlocking {
            favorisDao.delete(stationIdLongFavorisEntity)
        }
    }

    private fun findStationByStationId(stationDao: StationDao, stationId: Long): StationEntity? {
        var station: StationEntity?
        runBlocking {
            station = stationDao.findStationByStationId(stationId)
        }
        return station
    }
}