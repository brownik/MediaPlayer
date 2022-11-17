package com.brownik.newmediaplayer

import android.media.MediaPlayer
import android.support.v4.media.MediaMetadataCompat

class MediaPlayerAdapter {
    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false
    private var currentPosition = -1

    fun initMediaPlayerAdapter(){
        mediaPlayer = MediaPlayer()
    }

    fun onPlay(metadata: MediaMetadataCompat){
        if (isPlaying) {
            mediaPlayer.apply {
                stop()
                reset()
                setDataSource(metadata.description.mediaId)
                prepare()
                start()
            }
        }else{
            mediaPlayer.start()
        }
    }

    fun onPause(){
        mediaPlayer.pause()
    }

    fun release(){
        mediaPlayer.release()
    }
}