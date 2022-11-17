package com.brownik.newmediaplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat

private const val MY_MEDIA_ROOT_ID = "media_root_id"

class MediaPlayerService : MediaBrowserServiceCompat() {

    private lateinit var mediaSessionCompat: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var mediaPlayerAdapter: MediaPlayerAdapter

    @SuppressLint("NotificationId0")
    override fun onCreate() {
        MyObject.makeLog("MediaPlayerService.onCreate")
        super.onCreate()
        mediaSessionCompat = MediaSessionCompat(applicationContext, "TAG").apply {

            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                    or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                    or MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
            )
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())
            setCallback(callback)
            setSessionToken(sessionToken)
        }
        mediaPlayerAdapter = MediaPlayerAdapter()
        mediaPlayerAdapter.initMediaPlayerAdapter()
        MediaLibrary.makeMediaList(this)
        MediaLibrary.makeMediaMetadataList()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot? {
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        result.sendResult(MediaLibrary.getMediaList())
    }

    private val callback = object : MediaSessionCompat.Callback() {

        private var playlist: ArrayList<MediaSessionCompat.QueueItem> = ArrayList()
        private var queueIndex = -1
        private var preparedMedia: MediaMetadataCompat? = null

        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            playlist.add(MediaSessionCompat.QueueItem(description, description.hashCode().toLong()))
            queueIndex = if (queueIndex == -1) 0 else queueIndex
            mediaSessionCompat.setQueue(playlist)
        }

        override fun onPrepare() {
            MyObject.makeLog("callback.onPrepare")
            MyObject.makeLog("size: ${playlist.size}")

            if (queueIndex < 0 && playlist.isEmpty()) return

            val mediaId = playlist[queueIndex].description.mediaId
            preparedMedia = MediaLibrary.getMetadata(mediaId.toString())

            mediaSessionCompat.setMetadata(preparedMedia)

            if (!mediaSessionCompat.isActive) {
                mediaSessionCompat.isActive = true
            }
        }

        override fun onPlay() {
            MyObject.makeLog("callback.onPlay")
            if (playlist.size <= 0) return
            if (preparedMedia == null) onPrepare()
            else mediaPlayerAdapter.onPlay(preparedMedia!!)
        }

        override fun onPause() {
            MyObject.makeLog("callback.onPause")
            mediaPlayerAdapter.onPause()
        }

        override fun onSkipToNext() {
            MyObject.makeLog("callback.onSkipToNext")
            queueIndex = if (queueIndex == playlist.size - 1) 0 else queueIndex + 1
            onPrepare()
            onPlay()
        }

        override fun onSkipToPrevious() {
            MyObject.makeLog("callback.onSkipToPrevious")
            queueIndex = if (queueIndex == 0) playlist.size - 1 else queueIndex - 1
            onPrepare()
            onPlay()
        }
    }
}