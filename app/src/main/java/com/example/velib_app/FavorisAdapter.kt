package com.example.velib_app

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.velib_app.bdd.*
import kotlinx.coroutines.runBlocking

private const val TAG = "FavorisAdapter"

class FavorisAdapter(
    val favorisList: List<Long>
) :
    RecyclerView.Adapter<FavorisAdapter.FavorisViewHolder>() {

    class FavorisViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavorisViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val favorisView = inflater.inflate(R.layout.adapter_favoris, parent, false)
        return FavorisViewHolder(favorisView)
    }

    override fun onBindViewHolder(holder: FavorisViewHolder, position: Int) {
        val favoris = favorisList[position] //kotlin list ~ tableau
        val favorisTextview =
            holder.view.findViewById<TextView>(R.id.adapter_station_name_textview)
        val favorisImageview =
            holder.view.findViewById<ImageView>(R.id.adapter_favoris_imageview)
        /*val stationFavoris = stationList.find {
            it.station_id.equals(favoris)
        }
        val stationFavorisDetails = stationDetails.find {
            it.station_id.equals(favoris)
        }*/

        /*favorisTextview.text =
            "${stationFavoris?.name}" //favorisTextview.text = "${favoris.firstname} ${client.lastname}"*/

        val context = holder.view.context
        val stationDatabase = StationDatabase.createDatabase(context)
        val stationDao = stationDatabase.stationDao()
        favorisTextview.text = "${findByStationIdStation(stationDao, favoris)?.name}"
        stationDatabase.close()


        holder.view.setOnClickListener {
            val context = it.context
            val stationDatabase = StationDatabase.createDatabase(context)
            val stationDao = stationDatabase.stationDao()
            val stationId = findByStationIdStation(stationDao, favoris)?.station_id.toString()
            val name = findByStationIdStation(stationDao, favoris)?.name
            val numBikesAvailable =
                findByStationIdStation(stationDao, favoris)?.numBikesAvailable.toString()
            val numDocksAvailable =
                findByStationIdStation(stationDao, favoris)?.numDocksAvailable.toString()
            val capacity = findByStationIdStation(stationDao, favoris)?.capacity.toString()
            val numBikesAvailableTypesMechanical = findByStationIdStation(
                stationDao,
                favoris
            )?.numBikesAvailableTypesMechanical.toString()
            val numBikesAvailableTypesElectrical = findByStationIdStation(
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
            /*val bundle = Bundle()
            bundle.putString("stationId", stationId)
            bundle.putString("name", name)
            bundle.putString("numBikes", numBikesAvailable)
            bundle.putString("numDocks", numDocksAvailable)
            bundle.putString("capacity", capacity)
            bundle.putString("numBikesAvailableTypesMechanical", numBikesAvailableTypesMechanical)
            bundle.putString("numBikesAvailableTypesElectrical", numBikesAvailableTypesElectrical)*/
            context.startActivity(intent)
        }

        holder.view.setOnLongClickListener {
            val context = it.context
            //favorisImageview.setImageResource(R.drawable.im_favoris_star_off)
            //val stationId = stationFavoris?.station_id
            val stationDatabase = StationDatabase.createDatabase(context)
            val stationDao = stationDatabase.stationDao()
            val stationId = findByStationIdStation(stationDao, favoris)?.station_id
            stationDatabase.close()
            if (stationId !== null) {
                val bddFavoris = FavorisDatabase.createDatabase(context)
                val favorisDao = bddFavoris.favorisDao()
                if (!isFavoris(favorisDao, stationId)) { //==false
                    favorisImageview.setImageResource(R.drawable.im_favoris_star_on)
                    insertFavoris(favorisDao, stationId)
                    Toast.makeText(context, "Favoris ajouté", Toast.LENGTH_LONG).show()
                } else {
                    deleteFavoris(favorisDao, stationId)
                    val deletePosition: Int = holder.adapterPosition
                    (favorisList!! as ArrayList).removeAt(deletePosition)
                    notifyItemRemoved(deletePosition)
                    notifyItemRangeChanged(deletePosition, favorisList.size)
                    Toast.makeText(context, "Favoris supprimé", Toast.LENGTH_LONG).show()
                    /*
                    favorisImageview.setImageResource(R.drawable.im_favoris_star_off)
                    deleteFavoris(favorisDao, stationId)
                    Toast.makeText(context, "Favoris supprimé", Toast.LENGTH_LONG).show()
                    */
                }
                bddFavoris.close()
            }
            true
        }

    }

    override fun getItemCount() = favorisList.size

    fun isFavoris(favorisDao: FavorisDao, stationId: Long): Boolean {
        var isFavoris = false
        runBlocking {
            val findByStationIdFavoris: FavorisEntity = favorisDao.findByStationId(stationId)
            isFavoris = findByStationIdFavoris != null
        }
        return isFavoris
    }

    fun insertFavoris(favorisDao: FavorisDao, stationId: Long) {
        val stationIdLongFavorisEntity: FavorisEntity = FavorisEntity(stationId)
        runBlocking {
            favorisDao.insert(stationIdLongFavorisEntity)
        }
    }

    fun deleteFavoris(favorisDao: FavorisDao, stationId: Long) {
        val stationIdLongFavorisEntity: FavorisEntity = FavorisEntity(stationId)
        runBlocking {
            favorisDao.delete(stationIdLongFavorisEntity)
        }
    }

    fun findByStationIdStation(stationDao: StationDao, stationId: Long): StationEntity? {
        var station: StationEntity? = null
        runBlocking {
            station = stationDao.findByStationIdStation(stationId)
        }
        return station
    }
}


/*
class FavorisAdapter(
    val favorisList: List<Long>,
    val stationList: MutableList<Station>,
    val stationDetails: MutableList<StationDetails>
) :
    RecyclerView.Adapter<FavorisAdapter.FavorisViewHolder>() {

    class FavorisViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavorisViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val favorisView = inflater.inflate(R.layout.adapter_favoris, parent, false)
        return FavorisViewHolder(favorisView)
    }

    override fun onBindViewHolder(holder: FavorisViewHolder, position: Int) {
        val favoris = favorisList[position] //kotlin list ~ tableau
        val favorisTextview =
            holder.view.findViewById<TextView>(R.id.adapter_station_name_textview)
        val favorisImageview =
            holder.view.findViewById<ImageView>(R.id.adapter_favoris_imageview)
        val stationFavoris = stationList.find {
            it.station_id.equals(favoris)
        }
        val stationFavorisDetails = stationDetails.find {
            it.station_id.equals(favoris)
        }

        favorisTextview.text =
            "${stationFavoris?.name}" //favorisTextview.text = "${favoris.firstname} ${client.lastname}"

        holder.view.setOnClickListener {
            val stationId = stationFavoris?.station_id.toString()
            val name = stationFavoris?.name
            val numBikesAvailable = stationFavorisDetails?.numBikesAvailable.toString()
            val numDocksAvailable = stationFavorisDetails?.numDocksAvailable.toString()
            val capacity = stationFavoris?.capacity.toString()
            Log.d(TAG, "stationFavorisDetails: $stationFavorisDetails")
            /*val numBikesAvailableTypesMechanical = stationFavorisDetails?.num_bikes_available_types?.get(0)
                ?.get("mechanical")
                .toString()
            val numBikesAvailableTypesElectrical = stationFavorisDetails?.num_bikes_available_types?.get(1)
                ?.get("ebike")
                .toString()*/

            val context = it.context
            val intent = Intent(context, DetailsActivity::class.java)

            intent.putExtra("stationId", stationId)
            intent.putExtra("name", name)
            intent.putExtra("numBikes", numBikesAvailable)
            intent.putExtra("numDocks", numDocksAvailable)
            intent.putExtra("capacity", capacity)
            //intent.putExtra("numBikesAvailableTypesMechanical", numBikesAvailableTypesMechanical)
            //intent.putExtra("numBikesAvailableTypesElectrical", numBikesAvailableTypesElectrical)
            /*val bundle = Bundle()
            bundle.putString("stationId", stationId)
            bundle.putString("name", name)
            bundle.putString("numBikes", numBikesAvailable)
            bundle.putString("numDocks", numDocksAvailable)
            bundle.putString("capacity", capacity)
            bundle.putString("numBikesAvailableTypesMechanical", numBikesAvailableTypesMechanical)
            bundle.putString("numBikesAvailableTypesElectrical", numBikesAvailableTypesElectrical)*/

            context.startActivity(intent)
        }

        holder.view.setOnLongClickListener {
            val context = it.context
            //favorisImageview.setImageResource(R.drawable.im_favoris_star_off)
            val stationId = stationFavoris?.station_id
            if (stationId !== null) {
                val bddFavoris = FavorisDatabase.createDatabase(context)
                val favorisDao = bddFavoris.favorisDao()
                if (!isFavoris(favorisDao, stationId)) { //==false
                    favorisImageview.setImageResource(R.drawable.im_favoris_star_on)
                    insertFavoris(favorisDao, stationId)
                    Toast.makeText(context, "Favoris ajouté", Toast.LENGTH_LONG).show()
                } else {
                    deleteFavoris(favorisDao, stationId)
                    val deletePosition: Int = holder.adapterPosition
                    (favorisList!! as ArrayList).removeAt(deletePosition)
                    notifyItemRemoved(deletePosition)
                    notifyItemRangeChanged(deletePosition, favorisList.size)
                    Toast.makeText(context, "Favoris supprimé", Toast.LENGTH_LONG).show()

                    /*
                    favorisImageview.setImageResource(R.drawable.im_favoris_star_off)
                    deleteFavoris(favorisDao, stationId)
                    Toast.makeText(context, "Favoris supprimé", Toast.LENGTH_LONG).show()
                    */
                }
                bddFavoris.close()

            }
            true
        }

    }

    override fun getItemCount() = favorisList.size

    fun isFavoris(favorisDao: FavorisDao, stationId: Long): Boolean {
        var isFavoris = false
        runBlocking {
            val findByStationIdFavoris: FavorisEntity = favorisDao.findByStationId(stationId)
            isFavoris = findByStationIdFavoris != null
        }
        return isFavoris
    }

    fun insertFavoris(favorisDao: FavorisDao, stationId: Long) {
        val stationIdLongFavorisEntity: FavorisEntity = FavorisEntity(stationId)
        runBlocking {
            favorisDao.insert(stationIdLongFavorisEntity)
        }
    }

    fun deleteFavoris(favorisDao: FavorisDao, stationId: Long) {
        val stationIdLongFavorisEntity: FavorisEntity = FavorisEntity(stationId)
        runBlocking {
            favorisDao.delete(stationIdLongFavorisEntity)
        }
    }

}
 */