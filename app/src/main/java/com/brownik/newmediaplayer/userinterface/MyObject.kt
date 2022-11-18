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
}