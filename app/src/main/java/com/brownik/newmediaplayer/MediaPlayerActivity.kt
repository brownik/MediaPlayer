package com.brownik.newmediaplayer

import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.brownik.newmediaplayer.databinding.ActivityMediaPlayerBinding

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPlayerBinding
    private lateinit var mediaBrowserCompat: MediaBrowserCompat
    private val mediaController by lazy {
        MediaControllerCompat.getMediaController(this@MediaPlayerActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMediaPlayerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        permissionCheck()

        mediaBrowserCompat = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlayerService::class.java),
            connectionCallback,
            null
        )
    }

    override fun onStart() {
        super.onStart()
        mediaBrowserCompat.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowserCompat.disconnect()
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            MyObject.makeLog("connectionCallback.onConnected")
            mediaBrowserCompat.sessionToken.also { token ->
                val mediaController = MediaControllerCompat(
                    this@MediaPlayerActivity,
                    token
                )
                MediaControllerCompat.setMediaController(this@MediaPlayerActivity, mediaController)
            }
            buildTransPortControls()
            mediaBrowserCompat.subscribe(mediaBrowserCompat.root, subscription)
        }

        override fun onConnectionSuspended() {
            MyObject.makeLog("connectionCallback.onConnectionSuspended")
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            MyObject.makeLog("connectionCallback.onConnectionFailed")
            super.onConnectionFailed()
        }
    }

    fun buildTransPortControls() = with(binding) {
        btnState.setOnClickListener {
            val currentState = mediaController.playbackState.state
            if (currentState == PlaybackStateCompat.STATE_PLAYING) {
                mediaController.transportControls.pause()
            } else {
                mediaController.transportControls.play()
            }
        }
        btnNext.setOnClickListener {
            mediaController.transportControls.skipToNext()
        }
        btnPrev.setOnClickListener {
            mediaController.transportControls.skipToPrevious()
        }
        val metadata = mediaController.metadata
        mediaController.registerCallback(controllerCallback)
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            MyObject.makeLog("controllerCallback.onMetadataChanged")
            // data 변경
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            MyObject.makeLog("controllerCallback.onPlaybackStateChanged")
            // view 변경
        }
    }

    private val subscription = object : MediaBrowserCompat.SubscriptionCallback(){
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
        ) {
            MyObject.makeLog("subscription.onChildrenLoaded")

            children.forEach{
                mediaController.addQueueItem(it.description)
            }
            mediaController.transportControls.prepare()
        }
    }

    // 권한 허용 체크
    private fun permissionCheck() {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            Context.MODE_PRIVATE
        )
    }
}