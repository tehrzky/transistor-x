package org.y20k.transistor.core

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Station(
    val id: Long,
    val name: String,
    val streamUri: Uri,
    val imageUri: Uri,
    val isFavorite: Boolean,
    val isDeleted: Boolean,
    val category: String = "All Stations"
) : Parcelable {
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Station> {
            override fun createFromParcel(parcel: Parcel): Station {
                return Station(
                    parcel.readLong(),
                    parcel.readString()!!,
                    parcel.readParcelable(Uri::class.java.classLoader)!!,
                    parcel.readParcelable(Uri::class.java.classLoader)!!,
                    parcel.readInt() == 1,
                    parcel.readInt() == 1,
                    parcel.readString() ?: "All Stations"
                )
            }

            override fun newArray(size: Int): Array<Station?> = arrayOfNulls(size)
        }
    }
}
