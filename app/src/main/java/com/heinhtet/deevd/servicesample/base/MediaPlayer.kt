package com.heinhtet.deevd.servicesample.base

import com.google.android.exoplayer2.ExoPlayer
import com.heinhtet.deevd.miscy.model.Song
import com.heinhtet.deevd.servicesample.MediaItem

/**
 * Created by Hein Htet on 9/20/18.
 */
abstract class MediaPlayer() {
    private var mProgressUpdateListener: ProgressUpdateListener? = null
    private var mMediaplayerListener: MediaPlayerListener? = null

    constructor(progressUpdateListener: ProgressUpdateListener,
                mediaPlayerListener: MediaPlayerListener) : this() {
        this.mProgressUpdateListener = progressUpdateListener
        this.mMediaplayerListener = mediaPlayerListener
    }

    fun tearDown() {
        mMediaplayerListener = null
        mProgressUpdateListener = null
    }


    abstract fun loadMediaItems(items: ArrayList<MediaItem>, position: Int = 0, playlistId: Long = 0L)

    abstract fun startPlayback(playImmediately: Boolean)

    abstract fun resumePlayback()

    abstract fun pausePlayback()

    abstract fun stopPlayback()

    abstract fun seekTo(position: Long)

    abstract fun isStreaming(): Boolean

    abstract fun getState(): Int

    abstract fun getCurrentPosition(): Long

    abstract fun getDuration(): Long

    abstract fun getBufferedPosition(): Long

    abstract fun setPlaybackSpeed(speed: Float)

    abstract fun next()

    abstract  fun  previous()

    abstract fun getCurrentItem() : MediaItem


    interface ProgressUpdateListener {
        fun updateProgress(progress: Long)
    }

    interface MediaPlayerListener {
        fun onStateChanged(state: Int,playWhenReady: Boolean,item: MediaItem)
        fun trackChange(item: MediaItem)
        fun progressChange(progress: Long, player: ExoPlayer)
    }

}