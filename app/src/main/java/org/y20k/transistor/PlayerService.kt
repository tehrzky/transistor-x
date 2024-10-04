/*
 * PlayerService.kt
 * Implements the PlayerService class
 * PlayerService add a SwapablePlayer used for Cast to BasePlayerService which otherwise holds most of the music service logic
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-24 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor

import android.util.Log
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
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


    override fun initializePlayer() {
        // switch to cast player if already casting
        if (castPlayer?.isCastSessionAvailable == true) { player.setPlayer(castPlayer!!)}
    }


    /*
     * Custom Player for local playback
     */
    private val localPlayer: Player by lazy {
        // step 1: create the local player
        val exoPlayer: ExoPlayer = ExoPlayer.Builder(this).apply {
            setAudioAttributes(AudioAttributes.DEFAULT, true)
            setHandleAudioBecomingNoisy(true)
            setLoadControl(loadControl)
            setMediaSourceFactory(DefaultMediaSourceFactory(this@PlayerService).setLoadErrorHandlingPolicy(loadErrorHandlingPolicy))
        }.build()
        exoPlayer.addAnalyticsListener(analyticsListener)
        exoPlayer.addListener(playerListener)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        // manually add seek to next and seek to previous since headphones issue them and they are translated to next and previous station
        val player = object : ForwardingPlayer(exoPlayer) {
            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon().add(COMMAND_SEEK_TO_NEXT).add(COMMAND_SEEK_TO_PREVIOUS).build()
            }
            override fun isCommandAvailable(command: Int): Boolean {
                return availableCommands.contains(command)
            }
            override fun getDuration(): Long {
                return C.TIME_UNSET // this will hide progress bar for HLS stations in the notification
            }
        }
        player
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
