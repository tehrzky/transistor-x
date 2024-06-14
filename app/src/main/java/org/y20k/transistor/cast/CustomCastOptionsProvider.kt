package org.y20k.transistor.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions

class CustomCastOptionsProvider: OptionsProvider {
    override fun getCastOptions(p0: Context): CastOptions {
        val mediaOptions = CastMediaOptions.Builder()
            .build()
        return CastOptions.Builder()
            .setReceiverApplicationId("A12D4273") // todo extract to string resource
            .setCastMediaOptions(mediaOptions)
            .build()
    }

    override fun getAdditionalSessionProviders(p0: Context): MutableList<SessionProvider>? {
        return null
    }
}