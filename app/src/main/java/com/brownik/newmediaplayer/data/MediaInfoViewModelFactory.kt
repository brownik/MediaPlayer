package com.brownik.newmediaplayer.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MediaInfoViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MediaInfoViewModel::class.java)) {
            MediaInfoViewModel() as T
        } else {
            throw IllegalArgumentException()
        }
    }
}