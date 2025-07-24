/*
 * PlayerFragmentLayoutHolder.kt
 * Implements the PlayerFragmentLayoutHolder class
 * A PlayerFragmentLayoutHolder hold references to the views used in PlayerFragment
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-25 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor.ui

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.y20k.transistor.R
import org.y20k.transistor.helpers.PreferencesHelper


/*
 * PlayerFragmentLayoutHolder class
 */
data class PlayerFragmentLayoutHolder(var rootView: View) {

    /* Define log tag */
    private val TAG: String = PlayerFragmentLayoutHolder::class.java.simpleName


    /* Main class variables */
    var recyclerView: RecyclerView
    val layoutManager: LinearLayoutManager
    private var onboardingLayout: ConstraintLayout
    private var onboardingQuoteViews: Group
    private var onboardingImportViews: Group


    /* Init block */
    init {
        // find views
        recyclerView = rootView.findViewById(R.id.station_list)
        onboardingLayout = rootView.findViewById(R.id.onboarding_layout)
        onboardingQuoteViews = rootView.findViewById(R.id.onboarding_quote_views)
        onboardingImportViews = rootView.findViewById(R.id.onboarding_import_views)

        // set up RecyclerView
        layoutManager = CustomLayoutManager(rootView.context)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
    }


//    /* Toggle the Import Running indicator  */
//    fun toggleImportingStationViews() {
//        if (onboardingImportViews.visibility == View.INVISIBLE) {
//            onboardingImportViews.isVisible = true
//            onboardingQuoteViews.isVisible = false
//        } else {
//            onboardingImportViews.isVisible = false
//            onboardingQuoteViews.isVisible = true
//        }
//    }


    /* Toggles visibility of the onboarding screen */
    fun toggleOnboarding(context: Context, collectionSize: Int): Boolean {
        if (collectionSize == 0 && PreferencesHelper.loadCollectionSize() <= 0) {
            onboardingLayout.isVisible = true
            return true
        } else {
            onboardingLayout.isGone = true
            return false
        }
    }


    /*
     * Inner class: Custom LinearLayoutManager
     */
    private inner class CustomLayoutManager(context: Context): LinearLayoutManager(context, VERTICAL, false) {
        override fun supportsPredictiveItemAnimations(): Boolean {
            return true
        }
    }
    /*
     * End of inner class
     */


}
