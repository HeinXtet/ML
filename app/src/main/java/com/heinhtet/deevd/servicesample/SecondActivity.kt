package com.heinhtet.deevd.servicesample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.heinhtet.deevd.servicesample.helper.MusicManager
import com.heinhtet.deevd.servicesample.base.MediaPlayer
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by Hein Htet on 9/20/18.
 */
class SecondActivity : AppCompatActivity(), MediaPlayer.MediaPlayerListener {

    private val TAG = "SecondActivity  : "

    override fun onStateChanged(state: Int ,playWhenReady:Boolean) {

    }

    override fun trackChange(item: MediaItem) {
    }

    override fun progressChange(progress: Long, player: ExoPlayer) {
        Log.i(TAG, " progress change $progress")
    }

    private lateinit var musicManager: MusicManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        stop_service.setOnClickListener {
            musicManager.pause()
        }

    }

    override fun onStart() {
        super.onStart()
        musicManager = MusicManager(this, this)
        musicManager.register()
    }


    override fun onStop() {
        super.onStop()
        musicManager.unRegister()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}