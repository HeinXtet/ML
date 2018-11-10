package com.heinhtet.deevd.servicesample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.util.Log
import com.heinhtet.deevd.servicesample.helper.MusicManager

/**
 * Created by Hein Htet on 9/23/18.
 */
class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var musicManager: MusicManager
    }

    private val TAG = "App"
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, " OnCreate App")
        musicManager = MusicManager(this)
    }

}