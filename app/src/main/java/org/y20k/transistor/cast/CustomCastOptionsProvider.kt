/*
 * CustomCastOptionsProvider.kt
 * Implements the CustomCastOptionsProvider class
 * Provides configuration options to the Cast framework - used in AndroidManifest.xml
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-25 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor.cast

import android.content.Context
import androidx.media3.cast.DefaultCastOptionsProvider.APP_ID_DEFAULT_RECEIVER_WITH_DRM
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import java.util.Collections

/*
 * CustomCastOptionsProvider class
 * Credit: https://github.com/androidx/media/blob/d833d59124d795afc146322fe488b2c0d4b9af6a/libraries/cast/src/main/java/androidx/media3/cast/DefaultCastOptionsProvider.java
 */
class CustomCastOptionsProvider: OptionsProvider {
    override fun getCastOptions(p0: Context): CastOptions {
        val mediaOptions = CastMediaOptions.Builder()
            // prevent two playback notifications from being created
            .setMediaSessionEnabled(false)
            .setNotificationOptions(null)
            .build()
        return CastOptions.Builder()
            .setResumeSavedSession(false)
            .setEnableReconnectionService(false)
            .setReceiverApplicationId(APP_ID_DEFAULT_RECEIVER_WITH_DRM)
            .setStopReceiverApplicationWhenEndingSession(true)
            .setCastMediaOptions(mediaOptions)
            .build()
    }

    override fun getAdditionalSessionProviders(p0: Context): MutableList<SessionProvider> {
        return Collections.emptyList()
    }
}