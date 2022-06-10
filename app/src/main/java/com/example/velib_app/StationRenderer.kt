package com.example.velib_app

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.example.velib_app.bdd.StationEntity
import com.example.velib_app.utils.ActionButton
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator

class StationRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<StationEntity>,
    private val actionButton: ActionButton
): DefaultClusterRenderer<StationEntity>(context, map, clusterManager) {

    private val iconGenerator = IconGenerator(context)
    private val inflatedView = View.inflate(context, R.layout.marker_layout, null)
    private val drawableDefault = AppCompatResources.getDrawable(context, R.drawable.marker_image)
    private val drawableMechanical = AppCompatResources.getDrawable(context, R.drawable.marker_image_mechanical)
    private val drawableEbike = AppCompatResources.getDrawable(context, R.drawable.marker_image_ebike)
    private val textView = inflatedView.findViewById<TextView>(R.id.marker_title)
    private val frameLayout = inflatedView.findViewById<FrameLayout>(R.id.custom_marker_view)

    override fun onBeforeClusterItemRendered(item: StationEntity, markerOptions: MarkerOptions) {
        iconGenerator.setContentView(inflatedView)

        when(actionButton) {
            ActionButton.MECHANICAL -> {
                textView.text =
                    item.numBikesAvailableTypesMechanical.toString()
                frameLayout.background = drawableMechanical
                iconGenerator.setBackground(drawableMechanical)
            }
            ActionButton.EBIKE -> {
                textView.text =
                    item.numBikesAvailableTypesElectrical.toString()
                frameLayout.background = drawableEbike
                iconGenerator.setBackground(drawableEbike)
            }

            ActionButton.NONE -> {
                textView.text = item.numBikesAvailable.toString()
                iconGenerator.setBackground(drawableDefault)
            }
        }

        markerOptions
            .position(LatLng(item.lat, item.lon))
            .icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())) // BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN + 21)
    }

    override fun onClusterItemRendered(clusterItem: StationEntity, marker: Marker) {
        marker.tag = null
    }
}