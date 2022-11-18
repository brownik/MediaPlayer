package com.brownik.newmediaplayer.data

import android.net.Uri
import java.io.Serializable

data class MediaInfoData(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val duration: Long = 0,
    val imagePath: Uri? = null,
    val mediaPath: String,
    var isSelected: Boolean = false,
): Serializable