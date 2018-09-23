package com.heinhtet.deevd.servicesample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.util.Log

/**
 * Created by Hein Htet on 9/23/18.
 */
class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        var workService = WorkService()
    }

    private val TAG = "App"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, " OnCreate App")
    }

}