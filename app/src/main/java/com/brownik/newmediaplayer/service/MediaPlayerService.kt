package com.brownik.newmediaplayer.service

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.brownik.newmediaplayer.MediaLibrary
import com.brownik.newmediaplayer.userinterface.MyObject

private const val MY_MEDIA_ROOT_ID = "media_root_id"

class MediaPlayerService : MediaBrowserServiceCompat() {

    private lateinit var mediaSessionCompat: MediaSessionCompat
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var mediaPlayerAdapter: MediaPlayerAdapter
    private val mediaNotificationManager: MediaNotificationManager by lazy {
        MediaNotificationManager(this@MediaPlayerService)
    }
    private var isRunning = false

    @RequiresApi(Build.VERSION_CODES.O)
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
        mediaPlayerAdapter = MediaPlayerAdapter(MediaPlayerListener())
        mediaPlayerAdapter.initMediaPlayerAdapter()
        mediaNotificationManager.createChannel()
        MediaLibrary.makeMediaList(this)
        MediaLibrary.makeMediaMetadataList()
    }

    // 블랙/화이트 유저 구분 등과 같은 역할
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot {
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    // MediaData 넘겨주기
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        result.sendResult(MediaLibrary.getMediaList())
    }

    // Callback
    private val callback = object : MediaSessionCompat.Callback() {

        private var playlist: ArrayList<MediaSessionCompat.QueueItem> = ArrayList()
        private var queueIndex = -1
        var preparedMedia: MediaMetadataCompat? = null

        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            val existMedia = playlist.any { it.description.mediaId == description?.mediaId }
            if (!existMedia) {
                playlist.add(MediaSessionCompat.QueueItem(
                    description,
                    description.hashCode().toLong())
                )
            }
            queueIndex = if (queueIndex == -1) 0 else queueIndex
            mediaSessionCompat.setQueue(playlist)
        }

        override fun onPrepare() {
            MyObject.makeLog("callback.onPrepare")

            if (queueIndex < 0 && playlist.isEmpty()) return

            val mediaId = playlist[queueIndex].description.mediaId
            preparedMedia = MediaLibrary.getMetadata(mediaId.toString())
            if (preparedMedia != null) {
                mediaSessionCompat.setMetadata(preparedMedia)
            }

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

        override fun onSkipToQueueItem(id: Long) {
            queueIndex =
                playlist.indices.find { playlist[it].description.mediaId == id.toString() }!!
            onPrepare()
            onPlay()
        }
    }

    inner class MediaPlayerListener : PlaybackInfoListener {

        private val serviceManager: ServiceManager = ServiceManager()

        override fun onPlaybackCompleted() {}
        override fun onPlaybackStateChange(state: PlaybackStateCompat) {
            MyObject.makeLog("MediaPlayerListener.onPlaybackStateChange.${state.state}")
            mediaSessionCompat.setPlaybackState(state)

            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> serviceManager.serviceStart(state)
                PlaybackStateCompat.STATE_STOPPED -> serviceManager.serviceStop()
                else -> serviceManager.updateNotification(state)
            }
        }

        inner class ServiceManager {
            fun serviceStart(state: PlaybackStateCompat) {
                MyObject.makeLog("MediaPlayerListener.ServiceManager.serviceStart")
                isRunning = true
                ContextCompat.startForegroundService(
                    this@MediaPlayerService,
                    Intent(this@MediaPlayerService,
                        MediaPlayerService::class.java
                    )
                )
                showNotification(state)
            }

            fun updateNotification(state: PlaybackStateCompat) {
                MyObject.makeLog("MediaPlayerListener.ServiceManager.updateNotification")
                showNotification(state)
            }

            fun serviceStop() {
                MyObject.makeLog("MediaPlayerListener.ServiceManager.serviceStop")
                isRunning = false
                stopForeground(false)
                stopSelf()
            }

            private fun showNotification(state: PlaybackStateCompat) {
                MyObject.makeLog("MediaPlayerListener.ServiceManager.showNotification")
                callback.preparedMedia?.let { metadata ->
                    val notificationId = mediaNotificationManager.getNotificationId()
                    val notification = mediaNotificationManager
                        .getNotification(metadata, state, sessionToken!!)
                        .build()
                    startForeground(notificationId, notification)
                }
            }
        }
    }
}