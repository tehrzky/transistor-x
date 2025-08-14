/*
 * Station.kt
 * Implements the Station class
 * A Station object holds the basic data of a radio station
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-25 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package org.y20k.transistor.core

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import kotlinx.parcelize.Parcelize
import java.util.*

/*
 * Station class
 */
@Keep
@Parcelize
data class Station(
    @Expose var uuid: String = UUID.randomUUID().toString(),
    @Expose var name: String = "",
    @Expose var streamUris: MutableList<String> = mutableListOf(),
    @Expose var stream: String = "",
    @Expose var image: String = "",
    @Expose var smallImage: String = "",
    @Expose var imageColor: Int = -1,
    @Expose var starred: Boolean = false,
    @Expose var isPlaying: Boolean = false,
    @Expose var nameManuallySet: Boolean = false,
    @Expose var imageManuallySet: Boolean = false,
    @Expose var remoteImageLocation: String = "",
    @Expose var remoteStationLocation: String = "",
    @Expose var radioBrowserStationUuid: String = "",
    @Expose var homepage: String = "",
    @Expose var modificationDate: Date = Date()
) : Parcelable {

    /* Returns the primary stream URI */
    fun getStreamUri(): String {
        return if (streamUris.isNotEmpty()) {
            streamUris[0]
        } else {
            stream
        }
    }

    /* Creates a deep copy of a Station */
    fun deepCopy(): Station {
        return Station(
            uuid = uuid,
            name = name,
            streamUris = streamUris.toMutableList(),
            stream = stream,
            image = image,
            smallImage = smallImage,
            imageColor = imageColor,
            starred = starred,
            isPlaying = isPlaying,
            nameManuallySet = nameManuallySet,
            imageManuallySet = imageManuallySet,
            remoteImageLocation = remoteImageLocation,
            remoteStationLocation = remoteStationLocation,
            radioBrowserStationUuid = radioBrowserStationUuid,
            homepage = homepage,
            modificationDate = Date(modificationDate.time)
        )
    }

    /* Overrides toString method */
    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Station Name: $name\n")
        stringBuilder.append("UUID: $uuid\n")
        stringBuilder.append("Stream URI: ${getStreamUri()}\n")
        stringBuilder.append("Starred: $starred\n")
        stringBuilder.append("Playing: $isPlaying\n")
        stringBuilder.append("Image: $image\n")
        stringBuilder.append("Small Image: $smallImage\n")
        stringBuilder.append("Image Color: $imageColor\n")
        stringBuilder.append("Remote Image Location: $remoteImageLocation\n")
        stringBuilder.append("Remote Station Location: $remoteStationLocation\n")
        stringBuilder.append("Radio Browser UUID: $radioBrowserStationUuid\n")
        stringBuilder.append("Homepage: $homepage\n")
        stringBuilder.append("Name Manually Set: $nameManuallySet\n")
        stringBuilder.append("Image Manually Set: $imageManuallySet\n")
        stringBuilder.append("Modification Date: $modificationDate\n")
        return stringBuilder.toString()
    }
}
