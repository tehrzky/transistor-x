/*
 * PlayerFragment.kt
 * Implements the PlayerFragment class
 * PlayerFragment adds a cast button listener to the BasePlayerFragment which it extends
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-25 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext


/*
 * PlayerFragment class
 */
class PlayerFragment: BasePlayerFragment() {

    /* Define log tag */
    private val TAG: String = PlayerFragment::class.java.simpleName


    /* Main class variables */
    private var castContext: CastContext? = null
    lateinit var castButton: MediaRouteButton


    /* Overrides onCreate from BasePlayerFragment */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialize Cast context
        try {
            castContext = CastContext.getSharedInstance(activity as Context)
        } catch (e: Exception) {
            Log.e(TAG, "Cast framework unavailable", e)
        }
    }


    /* Overrides onViewCreated from BasePlayerFragment */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // initialize cast button
        castButton = layout.rootView.findViewById(R.id.cast_button)
        if (castContext != null) {
            try {
                castButton.setRemoteIndicatorDrawable(AppCompatResources.getDrawable(activity as Context, R.drawable.selector_cast_button))
                CastButtonFactory.setUpMediaRouteButton(requireContext(), castButton)
                castButton.isVisible = true
            } catch (e: Exception) {
                Log.e(TAG, "Cast button setup failed.", e)
                castButton.isVisible = false
            }
        } else {
            castButton.isVisible = false
        }
    }


//    /*
//     * Inner class: listener that is called when the Cast state changes
//     */
//    private val customCastStateListener = object : CastStateListener {
//        override fun onCastStateChanged(state: Int) {
//            castEnabled = state != CastState.NO_DEVICES_AVAILABLE
//            changeCastButtonVisibility(castEnabled)
//        }
//    }
//    /*
//     * End of inner class
//     */

}