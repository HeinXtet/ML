package com.heinhtet.deevd.servicesample

import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.heinhtet.deevd.servicesample.base.*
import com.heinhtet.deevd.servicesample.base.MediaPlayer


/**
 * Created by Hein Htet on 9/20/18.
 */


class WorkService : Service(), MediaPlayer.MediaPlayerListener,
        MediaPlayer.ProgressUpdateListener {

    private val TAG = "WorkService : "

    private lateinit var mContext: Context

    override fun updateProgress(progress: Long) {

    }

    override fun progressChange(progress: Long, player: ExoPlayer) {
        mediaPlayerListener?.progressChange(progress, player)
    }


    override fun trackChange(item: MediaItem) {
        Log.i(TAG, " Track Change $item")
        mediaPlayerListener?.trackChange(item)
    }

    override fun onStateChanged(state: Int) {
        mediaPlayerListener?.onStateChanged(state)
    }

    private val LOG_TAG = "WorkService"
    private var workBinder: IBinder = WorkBinder()
    lateinit var localMediaPlayer: LocalMediaPlayer
    private var mediaPlayerListener: MediaPlayer.MediaPlayerListener? = null

    override fun onCreate() {
        super.onCreate()
        Log.v(LOG_TAG, "in onCreate")
        mContext = this
        localMediaPlayer = LocalMediaPlayer(this, this, mContext)

    }

    fun start() {
        localMediaPlayer.startPlayback(true)
    }

    fun stop() {
        localMediaPlayer.pausePlayback()
    }

    override fun onBind(intent: Intent?): IBinder {
        localMediaPlayer.loadMediaItems(ArrayList(), 0)
        return workBinder
    }

    override fun onRebind(intent: Intent) {
        Log.v(LOG_TAG, "in onRebind")
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.v(LOG_TAG, "in onUnbind")
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(LOG_TAG, "in onDestroy")
    }

    fun setCallback(mediaPlayerListener: MediaPlayer.MediaPlayerListener) {
        this.mediaPlayerListener = mediaPlayerListener
    }

    inner class WorkBinder : Binder() {
        val service: WorkService
            get() = this@WorkService
    }

}