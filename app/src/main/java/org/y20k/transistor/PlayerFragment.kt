/*
 * PlayerFragment.kt
 * Implements the PlayerFragment class
 * PlayerFragment adds a cast button listener to the BasePlayerFragment which it extends
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-24 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor

import android.content.Context
import android.os.Bundle
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState.NO_DEVICES_AVAILABLE
import com.google.android.gms.cast.framework.CastStateListener


/*
 * PlayerFragment class
 */
class PlayerFragment: BasePlayerFragment() {

    /* Main class variables */
    private lateinit var castContext: CastContext
    private var castEnabled: Boolean = false


    /* Overrides onCreate from BasePlayerFragment */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialize Cast context
        castContext = CastContext.getSharedInstance(activity as Context)
    }


    /* Overrides onResume from BasePlayerFragment */
    override fun onResume() {
        super.onResume()
        // toggle Cast button
        layout.changeCastButtonVisibility(castEnabled)
        // start listening for Cast state changes
        castContext.addCastStateListener(customCastStateListener)
    }


    /* Overrides onPause from BasePlayerFragment */
    override fun onPause() {
        super.onPause()
        // stop listening for Cast state changes
        castContext.removeCastStateListener(customCastStateListener)
    }


    /*
     * Inner class: listener that is called when the Cast state changes
     */
    private val customCastStateListener = object : CastStateListener {
        override fun onCastStateChanged(state: Int) {
            castEnabled = state != NO_DEVICES_AVAILABLE
            layout.changeCastButtonVisibility(castEnabled)
        }
    }
    /*
     * End of inner class
     */

}