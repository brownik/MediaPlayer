package com.brownik.newmediaplayer

import java.io.Serializable

data class MediaInfoData(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val duration: Long = 0,
    val imagePath: Long? = null,
    val mediaPath: String = "",
    var isSelected: Boolean = false,
): Serializable