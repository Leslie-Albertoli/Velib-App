package com.example.velib_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.velib_app.model.Station
import com.example.velib_app.model.StationDetails

private const val TAG = "FavorisAdapter"

class FavorisAdapter(val favorisList: List<Long>, val stationList: MutableList<Station>, val stationDetails: MutableList<StationDetails>) :
    RecyclerView.Adapter<FavorisAdapter.FavorisViewHolder>() {

    class FavorisViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavorisViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val favorisView = inflater.inflate(R.layout.adapter_favoris, parent, false)
        return FavorisViewHolder(favorisView)
    }

    override fun onBindViewHolder(holder: FavorisViewHolder, position: Int) {
        val favoris = favorisList[position] //kotlin list ~ tableau

        val clientTextview =
            holder.view.findViewById<TextView>(R.id.adapter_station_name_textview)
        val stationFavoris = stationList.find {
            it.station_id.equals(favoris)
        }
        val stationFavorisDetails = stationDetails.find{
            it.station_id.equals(favoris)
        }
        clientTextview.text = "${stationFavoris?.name}"
        //clientTextview.text = "${favoris.firstname} ${client.lastname}"

        /*val clientImageview =
            holder.view.findViewById<ImageView>(R.id.adapter_client_imageview)
        clientImageview.setClient(client)*/


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

        /*holder.view.setOnClickListener {
            val context = it.context
            val intent = Intent(context, DetailsClientActivity::class.java)
            intent.putExtra("client_id", position)
            context.startActivity(intent)
        }*/


    }

    override fun getItemCount() = favorisList.size

}