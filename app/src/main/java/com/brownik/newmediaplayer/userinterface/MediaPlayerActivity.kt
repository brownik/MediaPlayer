package com.brownik.newmediaplayer.userinterface

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.brownik.newmediaplayer.R
import com.brownik.newmediaplayer.data.MediaInfoData
import com.brownik.newmediaplayer.data.MediaInfoViewModel
import com.brownik.newmediaplayer.service.MediaPlayerService
import com.brownik.newmediaplayer.userinterface.adapter.MediaInfoListAdapter
import com.brownik.newmediaplayer.databinding.ActivityMediaPlayerBinding

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPlayerBinding
    private val mediaBrowserCompat: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlayerService::class.java),
            connectionCallback,
            null
        )
    }
    private lateinit var mediaInfoViewModel: MediaInfoViewModel
    private val mediaController by lazy {
        MediaControllerCompat.getMediaController(this@MediaPlayerActivity)
    }
    private val mediaInfoListAdapter: MediaInfoListAdapter by lazy {
        MediaInfoListAdapter { data ->
            onMediaClickListener(data)
        }
    }
    private var firstSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMediaPlayerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        permissionCheck()

        binding.rvMediaInfo.adapter = mediaInfoListAdapter  // recyclerview adapter 생성
        mediaInfoViewModel = MediaInfoViewModel()  // ViewModel 생성
        setMediaInfoLiveData()
    }

    override fun onStart() {
        super.onStart()
        mediaBrowserCompat.connect()
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowserCompat.disconnect()
    }

    // LiveData 연결
    private fun setMediaInfoLiveData() {
        mediaInfoViewModel.mediaInfoList.observe(this@MediaPlayerActivity, Observer {
            mediaInfoListAdapter.submitList(it.toMutableList())  // LiveData list 변경에 따른 view 업데이트
        })

        mediaInfoViewModel.selectedMediaInfo.observe(this@MediaPlayerActivity, Observer {
            binding.ivPlayingImage.setImageURI(it.imagePath)
            binding.tvPlayingTitle.text = it.title
            binding.tvPlayingArtist.text = it.artist
        })
    }

    // recyclerview media 선택 항목 controller 전달
    private fun onMediaClickListener(data: MediaInfoData) {
        mediaController.transportControls.skipToQueueItem(data.id.toLong())
    }

    // MediaBrowserCompat 연결 확인 callback 및 controller 연결
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

    // 재생, 일시정지, 이전, 다음 버튼 클릭 시 변경
    fun buildTransPortControls() = with(binding) {
        btnState.setOnClickListener {
            val currentState = mediaController.playbackState.state
            MyObject.makeLog("currentState: $currentState")
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
        mediaController.registerCallback(controllerCallback)
    }

    // user control callback
    private var controllerCallback = object : MediaControllerCompat.Callback() {

        // FloatingBar, 현재 재생 목록 표시 변경
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            MyObject.makeLog("controllerCallback.onMetadataChanged")
            mediaInfoViewModel.selectedMedia(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            MyObject.makeLog("controllerCallback.onPlaybackStateChanged")
            val btnImage = when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> R.drawable.btn_pause
                else -> R.drawable.btn_play
            }
            binding.btnState.setBackgroundResource(btnImage)
        }

        // RecyclerView 목록 변경
        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            MyObject.makeLog("controllerCallback.onQueueChanged")
            if (queue != null) binding.tvMediaCount.text = queue.size.toString()
            else binding.tvMediaCount.text = "0"
            if (firstSet) mediaInfoViewModel.changeData(queue)
        }
    }

    // 최초 Media 목록 추가
    private val subscription = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>,
        ) {
            MyObject.makeLog("subscription.onChildrenLoaded")
            children.forEach {
                mediaController.addQueueItem(it.description)
            }
            mediaController.transportControls.prepare()
            mediaInfoViewModel.setData(children)
            firstSet = true
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