package com.brownik.newmediaplayer.data

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brownik.newmediaplayer.userinterface.MyObject
import kotlinx.coroutines.DisposableHandle

class MediaInfoViewModel: ViewModel() {

    private val _mediaInfoList = MutableLiveData<List<MediaInfoData>>()
    val mediaInfoList: LiveData<List<MediaInfoData>> = _mediaInfoList

    private val _selectedMediaInfo = MutableLiveData<MediaInfoData>()
    val selectedMediaInfo: LiveData<MediaInfoData> = _selectedMediaInfo

    // 최초 미디어 목록 세팅
    fun setData(queue: MutableList<MediaBrowserCompat.MediaItem>) {
        val list = mutableListOf<MediaInfoData>()
        queue.forEach {
            val id: String = it.description.mediaId.toString()
            val title: String = MyObject.reduceTextSize30(it.description.title.toString())
            val artist: String = MyObject.reduceTextSize30(it.description.subtitle.toString())
            val duration: Long = 0
            val imagePath: Uri? = it.description.iconUri
            val mediaPath: String = it.description.mediaId.toString()
            list.add(MediaInfoData(id, title, artist, duration, imagePath, mediaPath))
        }
        _mediaInfoList.value = list
    }

    // 미디어 Queue 변경 callback 후 목록 세팅
    fun changeData(queue: MutableList<MediaSessionCompat.QueueItem>?) {
        val list = mutableListOf<MediaInfoData>()
        queue?.forEach {
            val id: String = it.description.mediaId.toString()
            val title: String = MyObject.reduceTextSize30(it.description.title.toString())
            val artist: String = MyObject.reduceTextSize30(it.description.subtitle.toString())
            val duration: Long = 0
            val imagePath: Uri? = it.description.iconUri
            val mediaPath: String = it.description.mediaId.toString()
            list.add(MediaInfoData(id, title, artist, duration, imagePath, mediaPath))
        }
        _mediaInfoList.value = list
    }

    // 음악 선택에 따른 데이터 변경
    fun selectedMedia(metadata: MediaMetadataCompat?) {
        val list = _mediaInfoList.value
        if (metadata != null) {
            list?.forEach {
                it.isSelected = it.id == metadata.description?.mediaId
            }
            val id: String = metadata.description.mediaId.toString()
            val title: String = MyObject.reduceTextSize20(metadata.description.title.toString())
            val artist: String = MyObject.reduceTextSize20(metadata.description.subtitle.toString())
            val duration: Long = 0
            val imagePath: Uri? = metadata.description.iconUri
            val mediaPath: String = metadata.description.mediaId.toString()
            val selectedData = MediaInfoData(id, title, artist, duration, imagePath, mediaPath, true)
            _mediaInfoList.value = list?.toMutableList()
            _selectedMediaInfo.value = selectedData
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}