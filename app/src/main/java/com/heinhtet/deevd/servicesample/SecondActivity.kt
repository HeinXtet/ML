package com.heinhtet.deevd.servicesample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.heinhtet.deevd.helper.ServiceHelper
import com.heinhtet.deevd.servicesample.base.MediaPlayer

/**
 * Created by Hein Htet on 9/20/18.
 */
class SecondActivity : AppCompatActivity(), MediaPlayer.MediaPlayerListener {

    private val TAG = "SecondActivity  : "

    override fun onStateChanged(state: Int) {

    }

    override fun trackChange(item: MediaItem) {
    }

    override fun progressChange(progress: Long, player: ExoPlayer) {
        Log.i(TAG, " progress change $progress")
    }

    private lateinit var serviceHelper: ServiceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        serviceHelper = ServiceHelper(this, this)
        serviceHelper.register()
    }

    override fun onStop() {
        super.onStop()
        serviceHelper.unRegister()
    }
}