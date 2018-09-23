package com.heinhtet.deevd.servicesample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import com.google.android.exoplayer2.ExoPlayer
import com.heinhtet.deevd.serviceeample.model.SongHelper
import com.heinhtet.deevd.servicesample.base.MediaPlayer
import com.heinhtet.deevd.servicesample.utils.FormatUtils
import kotlinx.android.synthetic.main.activity_main.*


data class MediaItem(var title: String, var path: String)


class MainActivity : AppCompatActivity(), MediaPlayer.MediaPlayerListener {

    override fun onStateChanged(state: Int) {
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
    var mBoundService: WorkService? = null
    var mServiceBound = false
    private lateinit var songlist: ArrayList<MediaItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    mBoundService?.localMediaPlayer?.seekTo(p1.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })


        findViewById<Button>(R.id.print_timestamp).setOnClickListener {
            if (mServiceBound) {
                mBoundService?.start()
            }
        }
        findViewById<Button>(R.id.next).setOnClickListener {
            if (mServiceBound) {
                mBoundService?.stop()
            }
        }
        findViewById<Button>(R.id.stop_service).setOnClickListener {
            mBoundService?.stop()
        }


    }

    override fun onStart() {
        super.onStart()
        gettingSong()
        val workService = WorkService()
        val intent = Intent(this, workService::class.java)
        startService(intent)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun gettingSong() {
        songlist = ArrayList()
        val songHelper = SongHelper()
        songHelper.scanSongs(this, "external")
        songHelper.getSongs().forEach {
            songlist.add(MediaItem(it.title, it.filePath))
            AppConstants.list.addAll(songlist)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mServiceBound) {
            unbindService(mServiceConnection)

            mServiceBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //stopService()
    }


    private fun stopService() {
        if (mServiceBound) {
            unbindService(mServiceConnection)
            mServiceBound = false
        }
        val workService = WorkService()
        val intent = Intent(this@MainActivity,
                workService::class.java)
        stopService(intent)
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mServiceBound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val myBinder = service as WorkService.WorkBinder
            mBoundService = myBinder.service
            mBoundService?.setCallback(this@MainActivity)
            mServiceBound = true
        }
    }
}
