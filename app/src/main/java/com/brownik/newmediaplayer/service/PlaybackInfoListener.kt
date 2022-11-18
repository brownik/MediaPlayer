package com.brownik.newmediaplayer.service

import android.support.v4.media.session.PlaybackStateCompat

interface PlaybackInfoListener {
    fun onPlaybackStateChange(state: PlaybackStateCompat)
    fun onPlaybackCompleted()
}