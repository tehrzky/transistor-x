/*
 * PlayerService.kt
 * Implements the PlayerService class
 * PlayerService add a SwapablePlayer used for Cast to BasePlayerService which otherwise holds most of the music service logic
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-25 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor

import android.util.Log
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.framework.CastContext
import org.y20k.transistor.cast.CastMediaItemConverter
import org.y20k.transistor.cast.SwappablePlayer


/*
 * PlayerService class
 */
@UnstableApi
class PlayerService: BasePlayerService() {


    /* Define log tag */
    private val TAG: String = PlayerService::class.java.simpleName


    /* Main class variables */
    override val player: SwappablePlayer by lazy { SwappablePlayer(localPlayer) }


    /* Overrides initializePlayer from BasePlayerService */
    override fun initializePlayer() {
        // switch to cast player if already casting
        if (castPlayer?.isCastSessionAvailable == true) { player.setPlayer(castPlayer!!)}
    }


    /*
     * Custom player for Cast playback
     */
    private val castPlayer: CastPlayer? by lazy {
        // if Cast is available, create a CastPlayer to handle communication with a Cast session
        try {
            val castContext = CastContext.getSharedInstance(this)
            val player = CastPlayer(castContext, CastMediaItemConverter()).apply {
                setSessionAvailabilityListener(CastSessionAvailabilityListener())
                addListener(playerListener)
                repeatMode = Player.REPEAT_MODE_ALL
            }
            player
        } catch (e : Exception) {
            // calling CastContext.getSharedInstance can throw various exceptions, all of which indicate that Cast is unavailable.
            Log.i(TAG, "Cast is not available on this device. " + "Exception thrown when attempting to obtain CastContext. " + e.message)
            null
        }
    }
    /*
     * End of declaration
     */


    /*
     * SessionAvailabilityListener to switch between local and cast player
     */
    private inner class CastSessionAvailabilityListener : SessionAvailabilityListener {

        override fun onCastSessionAvailable() {
            player.setPlayer(castPlayer!!)
        }

        override fun onCastSessionUnavailable() {
            player.setPlayer(localPlayer)
        }
    }
    /*
     * End of inner class
     */


}
