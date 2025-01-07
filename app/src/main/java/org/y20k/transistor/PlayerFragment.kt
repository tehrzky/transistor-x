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
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener


/*
 * PlayerFragment class
 */
class PlayerFragment: BasePlayerFragment() {

    /* Main class variables */
    private lateinit var castContext: CastContext
    private var castEnabled: Boolean = false
    lateinit var castButton: MediaRouteButton


    /* Overrides onCreate from BasePlayerFragment */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialize Cast context
        castContext = CastContext.getSharedInstance(activity as Context)
    }


    /* Overrides onViewCreated from BasePlayerFragment */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // initialize cast button
        castButton = layout.rootView.findViewById(R.id.cast_button)
        castButton.setRemoteIndicatorDrawable(AppCompatResources.getDrawable(activity as Context, R.drawable.selector_cast_button))
        CastButtonFactory.setUpMediaRouteButton(activity as Context, castButton)
    }


    /* Overrides onResume from BasePlayerFragment */
    override fun onResume() {
        super.onResume()
        // toggle Cast button
        changeCastButtonVisibility(castEnabled)
        // start listening for Cast state changes
        castContext.addCastStateListener(customCastStateListener)
    }


    /* Overrides onPause from BasePlayerFragment */
    override fun onPause() {
        super.onPause()
        // stop listening for Cast state changes
        castContext.removeCastStateListener(customCastStateListener)
    }


    /* Toggles visibility of the cast button */
    fun changeCastButtonVisibility(visible: Boolean) {
        castButton.isVisible = visible
    }


    /*
     * Inner class: listener that is called when the Cast state changes
     */
    private val customCastStateListener = object : CastStateListener {
        override fun onCastStateChanged(state: Int) {
            castEnabled = state != CastState.NO_DEVICES_AVAILABLE
            changeCastButtonVisibility(castEnabled)
        }
    }
    /*
     * End of inner class
     */

}