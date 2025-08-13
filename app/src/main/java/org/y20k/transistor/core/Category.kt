package org.y20k.transistor.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val id: String,
    val name: String,
    val position: Int
) : Parcelable {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category("all", "All Stations", 0),
            Category("music", "Music", 1),
            Category("news", "News", 2),
            Category("talk", "Talk", 3),
            Category("intl", "International", 4)
        )
    }
}
