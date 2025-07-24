/*
 * BaseMainActivity.kt
 * Implements the BaseMainActivity abstract class
 * BaseMainActivity is the default activity that hosts the player fragment and the settings fragment
 * It also manages the player view and its functionality
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-25 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor

import android.content.ComponentName
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentContainerView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import org.y20k.transistor.extensions.cancelSleepTimer
import org.y20k.transistor.extensions.play
import org.y20k.transistor.extensions.requestMetadataHistory
import org.y20k.transistor.extensions.requestSleepTimerRemaining
import org.y20k.transistor.extensions.requestSleepTimerRunning
import org.y20k.transistor.extensions.startSleepTimer
import org.y20k.transistor.helpers.AppThemeHelper
import org.y20k.transistor.helpers.FileHelper
import org.y20k.transistor.helpers.ImportHelper
import org.y20k.transistor.helpers.PreferencesHelper
import org.y20k.transistor.ui.MainActivityLayoutHolder
import org.y20k.transistor.ui.PlayerState


/*
 * BaseMainActivity class
 */
abstract class BaseMainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {


    /* Define log tag */
    private val TAG: String = MainActivity::class.java.simpleName


    /* Main class variables */
    private lateinit var systemBars: Insets
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var layout: MainActivityLayoutHolder
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController? get() = if (controllerFuture.isDone) controllerFuture.get() else null
    private var playerState: PlayerState = PlayerState()
    private val handler: Handler = Handler(Looper.getMainLooper())


    /* Overrides onCreate from AppCompatActivity */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // house keeping - if necessary
        if (PreferencesHelper.isHouseKeepingNecessary()) {
            // house-keeping 1: remove hard coded default image
            ImportHelper.removeDefaultStationImageUris(this)
            // house-keeping 2: if existing user detected, enable Edit Stations by default
            if (PreferencesHelper.loadCollectionSize() != -1) {
                // existing user detected - enable Edit Stations by default
                PreferencesHelper.saveEditStationsEnabled(true)
            }
            PreferencesHelper.saveHouseKeepingNecessaryState()
        }

        // set up views
        setContentView(R.layout.activity_main)
        layout = MainActivityLayoutHolder(findViewById(R.id.root_view))

        // load player state
        playerState = PreferencesHelper.loadPlayerState()

        // create .nomedia file - if not yet existing
        FileHelper.createNomediaFile(getExternalFilesDir(null))

        // set up action bar
        setSupportActionBar(findViewById(R.id.main_toolbar))
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_host_container) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)
        supportActionBar?.hide()

        // set up edge to edge display
        setupEdgeToEdge()

        // set up playback controls
        setupPlaybackControls()

        // register listener for changes in shared preferences
        PreferencesHelper.registerPreferenceChangeListener(this)
    }


    /* Overrides onStart from AppCompatActivity */
    override fun onStart() {
        super.onStart()
        // initialize MediaController - connect to PlayerService
        initializeController()
    }


    /* Overrides onResume from AppCompatActivity */
    override fun onResume() {
        super.onResume()
        // assign volume buttons to music volume
        volumeControlStream = AudioManager.STREAM_MUSIC
        // load player state
        playerState = PreferencesHelper.loadPlayerState()
        // toggle periodic sleep timer update request
        togglePeriodicSleepTimerUpdateRequest()
    }


    /* Overrides onPause from AppCompatActivity */
    override fun onPause() {
        super.onPause()
        // stop receiving playback progress updates
        handler.removeCallbacks(periodicSleepTimerUpdateRequestRunnable)
    }


    /* Overrides onStop from AppCompatActivity */
    override fun onStop() {
        super.onStop()
        // release MediaController - cut connection to PlayerService
        releaseController()
    }


    /* Overrides onDestroy from AppCompatActivity */
    override fun onDestroy() {
        super.onDestroy()
        // unregister listener for changes in shared preferences
        PreferencesHelper.unregisterPreferenceChangeListener(this)
    }


    /* Overrides onSharedPreferenceChanged from OnSharedPreferenceChangeListener */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Keys.PREF_THEME_SELECTION -> {
                AppThemeHelper.setTheme(PreferencesHelper.loadThemeSelection())
            }
            Keys.PREF_ACTIVE_DOWNLOADS -> {
                layout.toggleDownloadProgressIndicator()
            }
            Keys.PREF_PLAYER_METADATA_HISTORY -> {
                requestMetadataUpdate()
            }
        }

    }



    /* Sets up margins/paddings for edge to edge view - for API 35 and above */
    private fun setupEdgeToEdge() {
        val rootView: ConstraintLayout = findViewById(R.id.root_view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
                // get measurements for status and navigation bar
                systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                // apply measurements to the fragment container containing the station list
                val mainHostContainer: FragmentContainerView = rootView.findViewById(R.id.main_host_container)
                mainHostContainer.updatePadding(
                    left = systemBars.left,
                    top = systemBars.top,
                    right = systemBars.right,
                )
                // return the insets
                insets
            }
        } else {
            // deactivate edge to edge
            rootView.fitsSystemWindows = true
        }
    }


    /* Overrides onSupportNavigateUp from AppCompatActivity */
    override fun onSupportNavigateUp(): Boolean {
        // Taken from: https://developer.android.com/guide/navigation/navigation-ui#action_bar
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_host_container) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    /* Initializes the MediaController - handles connection to PlayerService under the hood */
    private fun initializeController() {
        controllerFuture = MediaController.Builder(this, SessionToken(this, ComponentName(this, PlayerService::class.java))).buildAsync()
        controllerFuture.addListener({ setupController() }, MoreExecutors.directExecutor())
    }


    /* Releases MediaController */
    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }


    /* Sets up the MediaController */
    private fun setupController() {
        val controller: MediaController = this.controller ?: return
        controller.addListener(playerListener)
        requestMetadataUpdate()
        // wire up the playback controls
        setupPlaybackControls()
        // update play button state
        layout.togglePlayButton(controller.isPlaying)
    }


    /* Sets up the general playback controls */
    private fun setupPlaybackControls() {
        // main play/pause button
        layout.playButtonView.setOnClickListener {
            onPlayButtonTapped(playerState.stationPosition)
        }

        // set up sleep timer start button
        layout.playerSleepTimerStartButtonView.setOnClickListener {
            when (controller?.isPlaying) {
                true -> {
                    playerState.sleepTimerRunning = true
                    controller?.startSleepTimer()
                    togglePeriodicSleepTimerUpdateRequest()
                }

                else -> Toast.makeText(
                    this,
                    R.string.toast_message_sleep_timer_unable_to_start,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // set up sleep timer cancel button
        layout.playerSleepTimerCancelButtonView.setOnClickListener {
            layout.sheetSleepTimerRemainingTimeView.text = String()
            playerState.sleepTimerRunning = false
            controller?.cancelSleepTimer()
            togglePeriodicSleepTimerUpdateRequest()
        }
    }


    /* Handles play button tap */
    fun onPlayButtonTapped(stationPosition: Int) {
        // CASE: the selected station is playing
        if (controller?.isPlaying == true && stationPosition == playerState.stationPosition) {
            // stop playback
            controller?.pause()
        }
        // CASE: the selected station is not playing (another station might be playing)
        else {
            playerState.stationPosition = stationPosition
            // start playback
            controller?.play(this, stationPosition)
        }
    }


    /* Updates the player views */
    fun updatePlayerViews(station: org.y20k.transistor.core.Station) {
        layout.updatePlayerViews(this, station, playerState.isPlaying)
    }


    /* Requests an update of the sleep timer from the player service */
    private fun requestSleepTimerUpdate() {
        val resultFuture: ListenableFuture<SessionResult>? =
            controller?.requestSleepTimerRemaining()
        resultFuture?.addListener(kotlinx.coroutines.Runnable {
            val timeRemaining: Long = resultFuture.get().extras.getLong(Keys.EXTRA_SLEEP_TIMER_REMAINING)
            layout.updateSleepTimer(this, timeRemaining)
        }, MoreExecutors.directExecutor())
    }


    /* Requests an update of the metadata history from the player service */
    private fun requestMetadataUpdate() {
        val resultFuture: ListenableFuture<SessionResult>? = controller?.requestMetadataHistory()
        resultFuture?.addListener(kotlinx.coroutines.Runnable {
            val metadata: ArrayList<String>? = resultFuture.get().extras.getStringArrayList(Keys.EXTRA_METADATA_HISTORY)
            layout.updateMetadata(metadata?.toMutableList())
        }, MoreExecutors.directExecutor())
    }


    /* Toggle periodic update request of Sleep Timer state from player service */
    private fun togglePeriodicSleepTimerUpdateRequest() {
        if (playerState.sleepTimerRunning && playerState.isPlaying) {
            handler.removeCallbacks(periodicSleepTimerUpdateRequestRunnable)
            handler.postDelayed(periodicSleepTimerUpdateRequestRunnable, 0)
        } else {
            handler.removeCallbacks(periodicSleepTimerUpdateRequestRunnable)
            layout.sleepTimerRunningViews.visibility = View.GONE
        }
    }


    /*
     * Runnable: Periodically requests sleep timer state
     */
    private val periodicSleepTimerUpdateRequestRunnable: Runnable = object : Runnable {
        override fun run() {
            // update sleep timer view
            requestSleepTimerUpdate()
            // use the handler to start runnable again after specified delay
            handler.postDelayed(this, 500)
        }
    }
    /*
     * End of declaration
     */


    /*
     * Player.Listener: Called when one or more player states changed.
     */
    private var playerListener: Player.Listener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            // store state of playback
            playerState.isPlaying = isPlaying
            // animate state transition of play button(s)
            layout.animatePlaybackButtonStateTransition(this@BaseMainActivity, isPlaying)
            // toggle the sleep timer update subscription
            togglePeriodicSleepTimerUpdateRequest()

            if (isPlaying) {
                // playback is active
                layout.showPlayer()
                layout.showBufferingIndicator(buffering = false)
            } else {
                // playback is not active
                layout.updateSleepTimer(this@BaseMainActivity)
            }

            // update the sleep timer running state
            val resultFuture: ListenableFuture<SessionResult>? = controller?.requestSleepTimerRunning()
            resultFuture?.addListener(kotlinx.coroutines.Runnable {
                playerState.sleepTimerRunning = resultFuture.get().extras.getBoolean(Keys.EXTRA_SLEEP_TIMER_RUNNING, false)
            }, MoreExecutors.directExecutor())
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)

            if (playWhenReady && controller?.isPlaying == false) {
                layout.showBufferingIndicator(buffering = true)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            layout.togglePlayButton(false)
            layout.showBufferingIndicator(false)
            // TODO: display Toast error message
        }
    }
    /*
     * End of declaration
     */
}