/*
 * MainActivityLayoutHolder.kt
 * Implements the MainActivityLayoutHolder class
 * A MainActivityLayoutHolder hold references to the views used in MainActivity
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-25 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */

package org.y20k.transistor.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.android.material.card.MaterialCardView
import org.y20k.transistor.Keys
import org.y20k.transistor.R
import org.y20k.transistor.core.Station
import org.y20k.transistor.helpers.DateTimeHelper
import org.y20k.transistor.helpers.ImageHelper
import org.y20k.transistor.helpers.PreferencesHelper
import org.y20k.transistor.helpers.UiHelper


/*
 * PlayerFragmentLayoutHolder class
 */
data class MainActivityLayoutHolder (var rootView: View) {

    /* Define log tag */
    private val TAG: String = MainActivityLayoutHolder::class.java.simpleName

    /* Main class variables */
    private lateinit var systemBars: Insets
    val playerCardView: MaterialCardView
    private var playerPlaybackViews: Group
    private var playerStationInfoViews: Group
    var sleepTimerRunningViews: Group
    private var downloadProgressIndicator: ProgressBar
    private var stationImageView: ImageView
    private var stationNameView: TextView
    private var metadataView: TextView
    var playButtonView: ImageButton
    var bufferingIndicator: ProgressBar
    private var playerStreamingLinkHeadline: TextView
    private var playerStreamingLinkView: TextView
    private var playerMetadataHistoryHeadline: TextView
    private var playerMetadataHistoryView: TextView
    var playerNextMetadataView: ImageButton
    var playerPreviousMetadataView: ImageButton
    var playerCopyMetadataButtonView: ImageButton
    var playerSleepTimerStartButtonView: ImageButton
    var playerSleepTimerCancelButtonView: ImageButton
    var sheetSleepTimerRemainingTimeView: TextView
    private var metadataHistory: MutableList<String>
    private var metadataHistoryPosition: Int
    private var isBuffering: Boolean


    /* Init block */
    init {
        // find views
        playerCardView = rootView.findViewById(R.id.player_card)
        playerPlaybackViews = rootView.findViewById(R.id.playback_views)
        playerStationInfoViews = rootView.findViewById(R.id.station_info_views)
        sleepTimerRunningViews = rootView.findViewById(R.id.sleep_timer_running_views)
        downloadProgressIndicator = rootView.findViewById(R.id.download_progress_indicator)
        stationImageView = rootView.findViewById(R.id.station_icon)
        stationNameView = rootView.findViewById(R.id.player_station_name)
        metadataView = rootView.findViewById(R.id.player_station_metadata)
        playButtonView = rootView.findViewById(R.id.player_play_button)
        bufferingIndicator = rootView.findViewById(R.id.player_buffering_indicator)
        playerStreamingLinkView = rootView.findViewById(R.id.sheet_streaming_link)
        playerStreamingLinkHeadline = rootView.findViewById(R.id.sheet_streaming_link_headline)
        playerMetadataHistoryHeadline = rootView.findViewById(R.id.sheet_metadata_headline)
        playerMetadataHistoryView = rootView.findViewById(R.id.sheet_metadata_history)
        playerNextMetadataView = rootView.findViewById(R.id.sheet_next_metadata_button)
        playerPreviousMetadataView = rootView.findViewById(R.id.sheet_previous_metadata_button)
        playerCopyMetadataButtonView = rootView.findViewById(R.id.copy_station_metadata_button)
        playerSleepTimerStartButtonView = rootView.findViewById(R.id.sleep_timer_start_button)
        playerSleepTimerCancelButtonView = rootView.findViewById(R.id.sleep_timer_cancel_button)
        sheetSleepTimerRemainingTimeView = rootView.findViewById(R.id.sleep_timer_remaining_time)

        // set up variables
        metadataHistory = PreferencesHelper.loadMetadataHistory()
        metadataHistoryPosition = metadataHistory.size - 1
        isBuffering = false

        // set up metadata history next and previous buttons
        playerPreviousMetadataView.setOnClickListener {
            if (metadataHistory.isNotEmpty()) {
                if (metadataHistoryPosition > 0) {
                    metadataHistoryPosition -= 1
                } else {
                    metadataHistoryPosition = metadataHistory.size - 1
                }
                playerMetadataHistoryView.text = metadataHistory[metadataHistoryPosition]
            }
        }
        playerNextMetadataView.setOnClickListener {
            if (metadataHistory.isNotEmpty()) {
                if (metadataHistoryPosition < metadataHistory.size - 1) {
                    metadataHistoryPosition += 1
                } else {
                    metadataHistoryPosition = 0
                }
                playerMetadataHistoryView.text = metadataHistory[metadataHistoryPosition]
            }
        }
        playerMetadataHistoryView.setOnLongClickListener {
            copyMetadataHistoryToClipboard()
            return@setOnLongClickListener true
        }
        playerMetadataHistoryHeadline.setOnLongClickListener {
            copyMetadataHistoryToClipboard()
            return@setOnLongClickListener true
        }

        // set up edge to edge display
        setupEdgeToEdge()

        // set layout for player
        setupPlayer()
    }


    /* Updates the player views */
    fun updatePlayerViews(context: Context, station: Station, isPlaying: Boolean) {

        // set default metadata views, when playback has stopped
        if (!isPlaying) {
            metadataView.text = station.name
            playerMetadataHistoryView.text = station.name
//            sheetMetadataHistoryView.isSelected = true
        }

        // update name
        stationNameView.text = station.name

        // update cover
        if (station.imageColor != -1) {
            stationImageView.setBackgroundColor(station.imageColor)
        }
        stationImageView.setImageBitmap(ImageHelper.getStationImage(context, station.smallImage))
        stationImageView.contentDescription = "${context.getString(R.string.descr_player_station_image)}: ${station.name}"

        // update streaming link
        playerStreamingLinkView.text = station.getStreamUri()

        // update click listeners
        playerStreamingLinkHeadline.setOnClickListener{ copyToClipboard(context, playerStreamingLinkView.text) }
        playerStreamingLinkView.setOnClickListener{ copyToClipboard(context, playerStreamingLinkView.text) }
        playerMetadataHistoryHeadline.setOnClickListener { copyToClipboard(context, playerMetadataHistoryView.text) }
        playerMetadataHistoryView.setOnClickListener { copyToClipboard(context, playerMetadataHistoryView.text) }
        playerCopyMetadataButtonView.setOnClickListener { copyToClipboard(context, playerMetadataHistoryView.text) }

    }


    /* Copies given string to clipboard */
    private fun copyToClipboard(context: Context, clipString: CharSequence) {
        val clip: ClipData = ClipData.newPlainText("simple text", clipString)
        val cm: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(clip)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            // since API 33 (TIRAMISU) the OS displays its own notification when content is copied to the clipboard
            Toast.makeText(context, R.string.toast_message_copied_to_clipboard, Toast.LENGTH_LONG).show()
        }
    }


    /* Copies collected metadata to clipboard */
    private fun copyMetadataHistoryToClipboard() {
        val metadataHistory: MutableList<String> = PreferencesHelper.loadMetadataHistory()
        val stringBuilder: StringBuilder = StringBuilder()
        metadataHistory.forEach { stringBuilder.append("${it.trim()}\n")}
        copyToClipboard(rootView.context, stringBuilder.toString())
    }


    /* Updates the metadata views */
    fun updateMetadata(metadataHistoryList: MutableList<String>?) {
        if (!metadataHistoryList.isNullOrEmpty()) {
            metadataHistory = metadataHistoryList
            if (metadataHistory.last() != metadataView.text) {
                metadataHistoryPosition = metadataHistory.size - 1
                val metadataString = metadataHistory[metadataHistoryPosition]
                metadataView.text = metadataString
                playerMetadataHistoryView.text = metadataString
                playerMetadataHistoryView.isSelected = true
            }
        }
    }


    /* Updates sleep timer views */
    fun updateSleepTimer(context: Context, timeRemaining: Long = 0L) {
        when (timeRemaining) {
            0L -> {
                sleepTimerRunningViews.isGone = true
            }
            else -> {
                sleepTimerRunningViews.isVisible = true
                val sleepTimerTimeRemaining = DateTimeHelper.convertToMinutesAndSeconds(timeRemaining)
                sheetSleepTimerRemainingTimeView.text = sleepTimerTimeRemaining
                sheetSleepTimerRemainingTimeView.contentDescription = "${context.getString(R.string.descr_expanded_player_sleep_timer_remaining_time)}: ${sleepTimerTimeRemaining}"            }
        }
    }


    /* Toggles play/pause button */
    fun togglePlayButton(isPlaying: Boolean) {
        if (isPlaying) {
            playButtonView.setImageResource(R.drawable.ic_player_stop_symbol_48dp)
            bufferingIndicator.isVisible = false
        } else {
            playButtonView.setImageResource(R.drawable.ic_player_play_symbol_48dp)
            bufferingIndicator.isVisible = isBuffering
        }
    }


    /* Toggles buffering indicator */
    fun showBufferingIndicator(buffering: Boolean) {
        bufferingIndicator.isVisible = buffering
        isBuffering = buffering
    }


    /* Toggles visibility of player depending on playback state - hiding it when playback is stopped (not paused or playing) */
    fun togglePlayerVisibility(playbackState: Int): Boolean {
        when (playbackState) {
            PlaybackStateCompat.STATE_STOPPED -> return hidePlayer()
            PlaybackStateCompat.STATE_NONE -> return hidePlayer()
            PlaybackStateCompat.STATE_ERROR -> return hidePlayer()
            else -> return showPlayer()
        }
    }


    /* Toggles visibility of the download progress indicator */
    fun toggleDownloadProgressIndicator() {
        when (PreferencesHelper.loadActiveDownloads()) {
            Keys.ACTIVE_DOWNLOADS_EMPTY -> downloadProgressIndicator.isGone = true
            else -> downloadProgressIndicator.isVisible = true
        }
    }


    /* Initiates the rotation animation of the play button  */
    fun animatePlaybackButtonStateTransition(context: Context, isPlaying: Boolean) {
        when (isPlaying) {
            true -> {
                // rotate and morph to stop icon
                playButtonView.setImageResource(R.drawable.anim_play_to_stop_48dp)
                val morphDrawable: AnimatedVectorDrawable = playButtonView.drawable as AnimatedVectorDrawable
                morphDrawable.start()
            }
            false -> {
                // rotate and morph to play icon
                playButtonView.setImageResource(R.drawable.anim_stop_to_play_48dp)
                val morphDrawable: AnimatedVectorDrawable = playButtonView.drawable as AnimatedVectorDrawable
                morphDrawable.start()
            }
        }
    }


    /* Shows player */
    fun showPlayer(): Boolean {
//        toggleListPadding(false)
        playerPlaybackViews.visibility = View.VISIBLE
        playerStationInfoViews.visibility = View.GONE
        return true
    }


    /* Hides player */
    fun hidePlayer(): Boolean {
//        toggleListPadding(true)
        playerPlaybackViews.visibility = View.GONE
        playerStationInfoViews.visibility = View.GONE
        return true
    }


    /* Minimizes player sheet if expanded */
    fun navigateBackTogglesPlaybackViewsIfNecessary(): Boolean {
        return if (playerStationInfoViews.isVisible && playerPlaybackViews.isGone) {
            hidePlayerInfoViews()
            true
        } else {
            false
        }
    }


    /* Shows the playback views and hides the info views */
    private fun showPlayerPlaybackViews() {
        playerPlaybackViews.isVisible = true
        playerStationInfoViews.isGone = true
        bufferingIndicator.isVisible = isBuffering
        sleepTimerRunningViews.isGone = true
    }


    /* Shows the info views and hides the playback views */
    private fun showPlayerInfoViews() {
        playerStationInfoViews.isVisible = true
        sleepTimerRunningViews.isGone = sheetSleepTimerRemainingTimeView.text.isEmpty()
    }

    /* Shows the info views and hides the playback views */
    private fun hidePlayerInfoViews() {
        playerStationInfoViews.isGone = true
        sleepTimerRunningViews.isGone = sheetSleepTimerRemainingTimeView.text.isEmpty()
    }


    /* Toggles between showing the playback views (default) and the station info views */
    private fun togglePlayerInfoViews() {
        if (playerStationInfoViews.isGone) {
            showPlayerInfoViews()
        } else if (playerStationInfoViews.isVisible) {
            hidePlayerInfoViews()
        }
    }


    /* Sets up the player */
    private fun setupPlayer() {
        playerCardView.setOnClickListener { togglePlayerInfoViews() }
        stationImageView.setOnClickListener { togglePlayerInfoViews() }
        stationNameView.setOnClickListener { togglePlayerInfoViews() }
        metadataView.setOnClickListener { togglePlayerInfoViews() }
    }


    /* Sets up margins/paddings for edge to edge view - for API 35 and above */
    private fun setupEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                // get measurements for status and navigation bar
                systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())

                // apply measurements to the download progress indicator
                downloadProgressIndicator.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topMargin = systemBars.top
                }

                // apply measurements to the player card
                playerCardView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    bottomMargin = (Keys.PLAYER_BOTTOM_MARGIN * UiHelper.getDensityScalingFactor(rootView.context)).toInt() + systemBars.bottom
                }

                // return the insets
                insets
            }
        } else {
            // deactivate edge to edge for main activity
            rootView.fitsSystemWindows = true
        }
    }


//    /* Toggles the bottom padding for the fragment container containing the podcast list */
//    private fun toggleListPadding(playerHidden: Boolean) {
//        if (this::systemBars.isInitialized) {
//            if (playerHidden) {
//                recyclerView.updatePadding(bottom = systemBars.bottom) // todo bottom should be 0
//            } else {
//                recyclerView.updatePadding(bottom = systemBars.bottom + (Keys.BOTTOM_SHEET_PEEK_HEIGHT * ImageHelper.getDensityScalingFactor(rootView.context)).toInt())
//            }
//        } else {
//            if (playerHidden) {
//                recyclerView.updatePadding(bottom = 0)
//            } else {
//                recyclerView.updatePadding(bottom = (Keys.BOTTOM_SHEET_PEEK_HEIGHT * ImageHelper.getDensityScalingFactor(rootView.context)).toInt())
//            }
//        }
//    }


}