package com.heinhtet.deevd.servicesample.helper

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.heinhtet.deevd.servicesample.WorkService
import com.heinhtet.deevd.servicesample.base.MediaPlayer

/**
 * Created by Hein Htet on 9/23/18.
 */
class MusicMa {

    private var mBoundService = WorkService()
    private var mServiceBound = false
    private lateinit var mContext: Context
    private lateinit var mediaPlayerListener: MediaPlayer.MediaPlayerListener

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mInstance: MusicMa? = null

        fun get(): MusicMa {
            if (mInstance == null) {
                synchronized(MusicMa::class.java)
                {
                    if (mInstance == null) {
                        mInstance = MusicMa()
                    }
                }
            }
            return mInstance!!
        }
    }

    fun init(context: Context, mediaPlayerListener: MediaPlayer.MediaPlayerListener) {
        this.mContext = context
        this.mediaPlayerListener = mediaPlayerListener
    }

    /**
     *  register service activate
     */
    fun register() {
        val intent = Intent(mContext, WorkService::class.java)
        mContext.startService(intent)
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * unBind , Service de-Activate
     */
    fun unRegister() {
        if (mServiceBound) {
            mContext.unbindService(mServiceConnection)
            mServiceBound = false
            mInstance = null
        }
    }

    fun seekTo(position: Long) {
        mBoundService.localMediaPlayer.seekTo(position)
    }

    fun play() {
        mBoundService.start()
    }

    fun pause() {
        mBoundService.stop()
    }

    fun loadMediaItems() {
        mBoundService.addMediaItems()
    }

    fun next() {

    }

    fun stopService() {
        if (mServiceBound) {
            mContext.unbindService(mServiceConnection)
            mServiceBound = false
        }
        val intent = Intent(mContext,
                WorkService::class.java)
        mContext.stopService(intent)
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mServiceBound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val myBinder = service as WorkService.WorkBinder
            mBoundService = myBinder.service
            mBoundService.setCallback(mediaPlayerListener)
            mServiceBound = true
        }
    }
}