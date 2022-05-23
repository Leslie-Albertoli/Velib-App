package com.example.velib_app.model

import android.os.Parcel
import android.os.Parcelable
//import kotlinx.parcelize.Parcelize

//@Parcelize
data class StationDetails(
    val station_id: Long,
    val numBikesAvailable: Int,
    val num_bikes_available_types: List<Map<String, Int>>,
    val numDocksAvailable: Int,
    val is_installed: Int,
    val is_returning: Int,
    val is_renting: Int
): Parcelable {
    //var addresses:ArrayList<num_bikes_available_types> = ArrayList()

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readInt(),
        listOf<Map<String, Int>>(),
        //createTypedArrayList(Address.CREATOR)?:ArrayList(),
        //parcel.createTypedArrayList(num_bikes_available_types.CREATOR)?:ArrayList(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(station_id)
        parcel.writeInt(numBikesAvailable)
        parcel.writeInt(numDocksAvailable)
        parcel.writeInt(is_installed)
        parcel.writeInt(is_returning)
        parcel.writeInt(is_renting)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StationDetails> {
        override fun createFromParcel(parcel: Parcel): StationDetails {
            return StationDetails(parcel)
        }

        override fun newArray(size: Int): Array<StationDetails?> {
            return arrayOfNulls(size)
        }
    }
}