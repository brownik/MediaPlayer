package com.brownik.newmediaplayer.userinterface

import android.content.Context
import android.util.Log
import android.widget.Toast

object MyObject {
    fun makeLog(text: String){
        Log.d("qwe123", text)
    }

    fun makeToast(context: Context, text: String){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun reduceTextSize30(text: String): String{
        val newText = if (text.length >= 30) {
            "${text.substring(0, 30)}..."
        }else{
            text
        }
        return newText
    }

    fun reduceTextSize20(text: String): String{
        val newText = if (text.length >= 20) {
            "${text.substring(0, 20)}..."
        }else{
            text
        }
        return newText
    }
}