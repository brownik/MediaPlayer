package com.brownik.newmediaplayer

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import java.util.*
import java.util.concurrent.TimeUnit


object MediaLibrary {

    var mediaList = arrayListOf<MediaInfoData>()
    var media = TreeMap<String, MediaMetadataCompat>()
    private val albumRes = HashMap<String, Int>()
    private val mediaFileName = HashMap<String, String>()

    @SuppressLint("Range")
    fun makeMediaList(context: Context) {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC
        val sortOrder = MediaStore.Audio.Media.TITLE
        val cursor = context.contentResolver.query(uri, null, selection, null, sortOrder)
        cursor?.let {
            it.moveToFirst()
            if (cursor.count > 0) {
                do {
                    val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                    val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val imagePath = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID))
                    val mediaPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    mediaList.add(MediaInfoData(id, title, artist, duration, imagePath, mediaPath))
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
    }

    private fun getImageUri(path: Long?): String{
        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        val albumUri = Uri.withAppendedPath(artworkUri, path.toString())
        return albumUri.toString()
    }

    fun makeMediaMetadataList(){
        mediaList.forEach { data ->
            media[data.id] = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, data.id)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, data.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, data.artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                    TimeUnit.MILLISECONDS.convert(data.duration, TimeUnit.MICROSECONDS)
                )
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, getImageUri(data.imagePath))
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, getImageUri(data.imagePath))
                .build()
        }
    }

    fun getMediaList(): MutableList<MediaBrowserCompat.MediaItem> {
        val result: MutableList<MediaBrowserCompat.MediaItem> = ArrayList()
        for (metadata in media.values) {
            result.add(
                MediaBrowserCompat.MediaItem(
                    metadata.description,
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
            )
        }
        return result
    }

    fun getMetadata(mediaId: String): MediaMetadataCompat{
        val builder = MediaMetadataCompat.Builder()

        return builder.build()
    }
}