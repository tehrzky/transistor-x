/*
 * CastMediaItemConverter.kt
 * Implements the CastMediaItemConverter class
 * A implementation of a MediaItemConverter to convert from a MediaItem to a Cast MediaQueueItem
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-24 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor.cast

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.OptIn
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi

import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage
import org.y20k.transistor.Keys

/*
 * CastMediaItemConverter class
 * Credit: https://github.com/android/uamp/blob/ef5076bf4279adfccafa746c92da6ec86607f284/common/src/main/java/com/example/android/uamp/media/CastMediaItemConverter.kt
 */
@OptIn(UnstableApi::class)
internal class CastMediaItemConverter : MediaItemConverter {

    private val defaultMediaItemConverter = DefaultMediaItemConverter()

    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val castMediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)
        //castMediaMetadata.putString("uamp.mediaid", mediaItem.mediaId) // todo what does this do?
        mediaItem.mediaMetadata.title?.let {
            castMediaMetadata.putString(MediaMetadata.KEY_TITLE, it.toString() )
        }
        mediaItem.mediaMetadata.subtitle?.let {
            castMediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, it.toString())
        }
        mediaItem.mediaMetadata.artist?.let {
            castMediaMetadata.putString(MediaMetadata.KEY_ARTIST, it.toString())
        }
        mediaItem.mediaMetadata.albumTitle?.let {
            castMediaMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, it.toString())
        }
        mediaItem.mediaMetadata.albumArtist?.let {
            castMediaMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, it.toString())
        }
        mediaItem.mediaMetadata.composer?.let {
            castMediaMetadata.putString(MediaMetadata.KEY_COMPOSER, it.toString())
        }
        mediaItem.mediaMetadata.trackNumber?.let{
            castMediaMetadata.putInt(MediaMetadata.KEY_TRACK_NUMBER, it)
        }
        mediaItem.mediaMetadata.discNumber?.let {
            castMediaMetadata.putInt(MediaMetadata.KEY_DISC_NUMBER, it)
        }
        val mediaInfo = MediaInfo.Builder(mediaItem.localConfiguration!!.uri.toString())
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType(MimeTypes.AUDIO_MPEG)
        mediaItem.localConfiguration?.let {
            mediaInfo.setContentUrl(it.uri.toString())
        }
        mediaItem.mediaMetadata.extras?.let { bundle ->
            // use the original artwork URI for Cast (URIs starting with content:// are useless on a Cast device)
            bundle.getString(Keys.KEY_ORIGINAL_ARTWORK_URI)?.let {
                castMediaMetadata.addImage(WebImage(Uri.parse(it)))
            }
            mediaInfo.setStreamDuration(bundle.getLong(MediaMetadataCompat.METADATA_KEY_DURATION,0))
        }
        mediaInfo.setMetadata(castMediaMetadata)
        val mediaQueueItem = defaultMediaItemConverter.toMediaQueueItem(mediaItem)
        mediaQueueItem.media?.customData?.let {
            mediaInfo.setCustomData(it)
        }
        return MediaQueueItem.Builder(mediaInfo.build()).build()
    }

    override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
        return defaultMediaItemConverter.toMediaItem(mediaQueueItem)
    }
}