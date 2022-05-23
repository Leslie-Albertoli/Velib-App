package com.example.velib_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class FavorisAdapter(val favorisList: List<Long>) :
    RecyclerView.Adapter<FavorisAdapter.FavorisViewHolder>() {

    class FavorisViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavorisViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val favorisView = inflater.inflate(R.layout.adapter_favoris, parent, false)
        return FavorisViewHolder(favorisView)
    }

    override fun onBindViewHolder(holder: FavorisViewHolder, position: Int) {
        val favoris = favorisList[position] //kotlin list ~ tableau

        /*holder.view.setOnClickListener {
            val context = it.context
            val intent = Intent(context, DetailsClientActivity::class.java)
            intent.putExtra("client_id", position)
            context.startActivity(intent)
        }*/

        /*holder.view.setOnClickListener {
            /*val context = it.context
            val intent = Intent(context, DetailsClientActivity::class.java)
            intent.putExtra("client_id", position)
            context.startActivity(intent)*/

            val stationId = "a"
            val name = "a"
            val numBikesAvailable = "1"
            val numDocksAvailable = "1"
            val capacity = "2"
            val numBikesAvailableTypesMechanical = "1"
            val numBikesAvailableTypesElectrical = "0"

            val bundle = Bundle()

            bundle.putString("stationId", stationId)
            bundle.putString("name", name)
            bundle.putString("numBikes", numBikesAvailable)
            bundle.putString("numDocks", numDocksAvailable)
            bundle.putString("capacity", capacity)
            bundle.putString("numBikesAvailableTypesMechanical", numBikesAvailableTypesMechanical)
            bundle.putString("numBikesAvailableTypesElectrical", numBikesAvailableTypesElectrical)

            /*val intent = Intent(holder.itemView.context, DetailsActivity::class.java)
            intent.putExtras(bundle)
            holder.itemView.context.startActivity(intent)
            true*/
            val context = it.context
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
            true
        }*/

        val clientTextview =
            holder.view.findViewById<TextView>(R.id.adapter_station_name_textview)

        clientTextview.text = "${favoris}"
        //clientTextview.text = "${favoris.firstname} ${client.lastname}"

        /*val clientImageview =
            holder.view.findViewById<ImageView>(R.id.adapter_client_imageview)
        clientImageview.setClient(client)*/

    }

    override fun getItemCount() = favorisList.size

}