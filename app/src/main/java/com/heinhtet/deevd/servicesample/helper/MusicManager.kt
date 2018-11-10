package com.heinhtet.deevd.servicesample.helper

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
class MusicManager(private val context: Context, private var mediaPlayerListener: MediaPlayer.MediaPlayerListener) {
    private var mBoundService = WorkService()
    private var mServiceBound = false
    /**
     *  register service activate
     */
    fun register() {
        val intent = Intent(context, WorkService::class.java)
        context.startService(intent)
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    fun seekTo(position: Long) {
        mBoundService.localMediaPlayer.seekTo(position)
    }

    fun play(state:(isPlaying : Boolean)->Unit) {
        if (!mBoundService.isPlaying()) {
            mBoundService.start()
            state(true)
        } else {
            mBoundService.stop()
            state(false)
        }
    }



    fun isPlaying() = mBoundService.isPlaying()

    fun pause() {
        mBoundService.stop()
    }

    fun loadMediaItems() {
        mBoundService.addMediaItems()
    }

    fun next() {
        mBoundService.next()
    }

    fun previous() {
        mBoundService.previous()
    }

    /**
     * unBind , Service de-Activate
     */
    fun unRegister() {
        if (mServiceBound) {
            context.unbindService(mServiceConnection)
            mServiceBound = false
        }
    }

    fun stopService() {
        if (mServiceBound) {
            context.unbindService(mServiceConnection)
            mServiceBound = false
        }
        val intent = Intent(context,
                WorkService::class.java)
        context.stopService(intent)
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
