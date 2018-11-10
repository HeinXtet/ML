package com.heinhtet.deevd.servicesample.base

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.heinhtet.deevd.servicesample.MediaItem
import java.io.File


/**
 * Created by Hein Htet on 9/20/18.
 */
class LocalMediaPlayer(progressUpdateListener: ProgressUpdateListener, val mediaPlayerListener: MediaPlayerListener
                       , context: Context) :
        MediaPlayer(progressUpdateListener, mediaPlayerListener), Player.EventListener {


    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

    }

    override fun onSeekProcessed() {
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPositionDiscontinuity(reason: Int) {
        Log.i(TAG, " onPositionDiscontinuity : $reason")
        mediaPlayerListener.trackChange(mediaList[reason - 1])
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        this.playWhenReady = playWhenReady
        mediaPlayerListener.onStateChanged(playbackState, playWhenReady)
    }


    private var progressHandler: android.os.Handler? = Handler()
    private var playWhenReady = false

    private fun updateProgress() {
        if (mMediaPlayer != null) {
            val playbackState = if (mMediaPlayer == null) Player.STATE_IDLE else mMediaPlayer?.playbackState

            if (mMediaPlayer!!.playWhenReady && playbackState == Player.STATE_READY) {
                val position: Long = if (mMediaPlayer == null) {
                    0L
                } else {
                    mMediaPlayer!!.currentPosition
                }
                mediaPlayerListener.progressChange(position, mMediaPlayer!!)
            }
            progressHandler?.postDelayed(updateProgressAction, 1000)
        }
    }

    private val updateProgressAction = Runnable { updateProgress() }
    private var playbackPosition = 0L
    private var currentPosition = 0
    private var mContext: Context = context
    private var mMediaPlayer: ExoPlayer? = null
    private var mIsStreaming: Boolean = false
    private var mMediaPlayerState: Int = 0
    private var trackSelector: DefaultTrackSelector
    private val TAG = "LocalMediaPlayer : "
    private var mediaList = ArrayList<MediaItem>()
    private var PLAYLIST_ID = 0L
    private var lastPlayIndex = 0

    init {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        // val mediaDataSourceFactory = DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "mediaPlayerSample"), bandwidthMeter as TransferListener<in DataSource>)
        trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        mMediaPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector)
        mMediaPlayer?.addListener(this)
        mMediaPlayerState = Player.STATE_IDLE
    }


    private fun buildMediaItems(list: ArrayList<MediaItem>, index: Int, playlistId: Long) {
        lastPlayIndex = index
        this.PLAYLIST_ID = playlistId
        val media: MediaSource?
        val size = list.size
        val mediaSources = arrayOfNulls<MediaSource>(size)
        list.forEachIndexed { index, mission ->
            mediaList.add(mission)
            mediaSources[index] = buildMediaSource(mission.path)
            Log.i(TAG, " media list ${mission.path} title ${mission.title}")
        }
        media = if (mediaSources.size == 1)
            mediaSources[0]
        else
            ConcatenatingMediaSource(*mediaSources)
        mMediaPlayer!!.prepare(media)
        mMediaPlayer!!.seekTo(currentPosition, playbackPosition)
        updateProgress()
        mediaPlayerListener.trackChange(mediaList[lastPlayIndex])
    }


    private fun buildMediaSource(path: String): MediaSource {
        val bandwidthMeter = DefaultBandwidthMeter()
        val extractorsFactory = DefaultExtractorsFactory()
        val mediaSourceFactory = ExtractorMediaSource.Factory(DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "MusicService")))
//
//        val mediaDataSourceFactory = DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "Muscy"),
//                bandwidthMeter as TransferListener<in DataSource>)
//        return ExtractorMediaSource(Uri.fromFile(File(path)),
//                mediaDataSourceFactory, extractorsFactory, null, null)

        return mediaSourceFactory.createMediaSource(Uri.fromFile(File(path)))

    }


    override fun next() {
        if (mMediaPlayer != null) {

            if (mMediaPlayer!!.hasNext()) {
                mMediaPlayer!!.next()
            }

        }

    }


    override fun previous() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer!!.hasPrevious()) {
                mMediaPlayer!!.previous()
            }
        }
    }

    private val isNextAvailable: Boolean
        get() = currentPosition != -1 && currentPosition + 1 < mediaList.count()


    private val isPreviousAvailable: Boolean
        get() = currentPosition > 0


    fun isPlaying() :Boolean{
       return playWhenReady
    }

    override fun startPlayback(playImmediately: Boolean) {
        mMediaPlayer!!.playWhenReady = playImmediately
        updateProgress()
    }

    override fun loadMediaItems(items: ArrayList<MediaItem>, position: Int, playlistId: Long) {
        if (this.mediaList.isEmpty()) {
            currentPosition = position
            buildMediaItems(items, position, playlistId)
        }
    }

    override fun resumePlayback() {
    }

    override fun pausePlayback() {
        mMediaPlayer?.playWhenReady = false
        progressHandler?.removeCallbacks(updateProgressAction)
    }

    override fun stopPlayback() {
    }

    override fun seekTo(position: Long) {
        mMediaPlayer?.seekTo(position)
    }

    override fun isStreaming(): Boolean {
        return false
    }

    override fun getState(): Int {
        return mMediaPlayerState
    }

    override fun getCurrentPosition(): Long {
        return mMediaPlayer!!.currentPosition
    }


    override fun getDuration(): Long {
        return mMediaPlayer!!.duration
    }

    override fun getBufferedPosition(): Long {
        return mMediaPlayer?.bufferedPosition!!
    }

    override fun setPlaybackSpeed(speed: Float) {

    }




}