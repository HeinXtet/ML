package com.heinhtet.deevd.servicesample

import android.content.BroadcastReceiver
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.support.v4.media.session.MediaButtonReceiver
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import com.google.android.exoplayer2.ExoPlayer
import com.heinhtet.deevd.servicesample.helper.MusicManager
import com.heinhtet.deevd.serviceeample.model.SongHelper
import com.heinhtet.deevd.servicesample.base.MediaPlayer
import com.heinhtet.deevd.servicesample.utils.FormatUtils
import kotlinx.android.synthetic.main.activity_main.*


data class MediaItem(var title: String, var path: String)


class MainActivity : AppCompatActivity(), MediaPlayer.MediaPlayerListener {


    private lateinit var musicManager: MusicManager


    override fun onStateChanged(state: Int, playWhenReady: Boolean) {
        Log.i(TAG, " player state change $state")
    }

    override fun trackChange(item: MediaItem) {
        Log.i(TAG, " track  change $item")
    }

    override fun progressChange(progress: Long, player: ExoPlayer) {
        Log.i(TAG, " progress change  change $progress ${player.duration}")
        seek_bar.max = player.duration.toInt()
        seek_bar.progress = progress.toInt()
        start_time.text = FormatUtils.formatMusicTime(progress)
        total_time.text = FormatUtils.formatMusicTime(player.duration)
    }

    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    musicManager.seekTo(p1.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
        findViewById<Button>(R.id.print_timestamp).setOnClickListener {
            musicManager.loadMediaItems()
            musicManager.play()
        }
        findViewById<Button>(R.id.next).setOnClickListener {
            musicManager.pause()
        }
        findViewById<Button>(R.id.stop_service).setOnClickListener {
            startActivity(Intent(this@MainActivity, SecondActivity::class.java))
        }
    }


    override fun onStart() {
        super.onStart()
        gettingSong()
        musicManager = MusicManager(this, this)
        musicManager.register()
    }

    private fun gettingSong() {
        val songHelper = SongHelper()
        songHelper.scanSongs(this, "external")
        songHelper.getSongs().forEach {
            Log.i(TAG, "load media item ${it.toString()}")
            AppConstants.list.add(MediaItem(it.title, it.filePath))
        }
        AppConstants.list.removeAt(0)
    }

    override fun onStop() {
        super.onStop()
        musicManager.unRegister()
    }

    override fun onDestroy() {
        super.onDestroy()
        // musicManager.stopService()
    }

}
