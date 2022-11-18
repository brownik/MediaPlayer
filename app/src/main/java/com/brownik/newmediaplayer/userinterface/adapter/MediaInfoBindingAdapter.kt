package com.brownik.newmediaplayer.userinterface.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import com.brownik.newmediaplayer.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

object MediaInfoBindingAdapter {

//    private val artworkUri = Uri.parse("content://media/external/audio/albumart")

    @SuppressLint("CheckResult")
    @BindingAdapter("app:loadImage")
    @JvmStatic
    fun loadImage(targetView: ImageView, path: Uri?) {
//        val albumUri = Uri.withAppendedPath(artworkUri, path.toString())
        Glide.with(targetView.context)
            .load(path)
            .error(R.drawable.basic_img)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(15)))
            .into(targetView)
    }

    @SuppressLint("SetTextI18n")
    @BindingAdapter("app:makeDuration")
    @JvmStatic
    fun makeDuration(targetView: TextView, duration: Long?) {
        duration?.let {
            val time = it / 1000
            val minute = time / 60
            val second = time % 60
            targetView.text = ("${minute}분 ${second}초")
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    @BindingAdapter("app:changeColor")
    @JvmStatic
    fun changeColor(targetView: TextView, isSelected: Boolean) {
        val colorResId = if (isSelected) R.color.selected else R.color.not_selected
        targetView.setTextColor(targetView.context.getColor(colorResId))
    }
}