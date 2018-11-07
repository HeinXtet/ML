package com.heinhtet.deevd.servicesample

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.session.MediaSession
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
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

    override fun onStateChanged(state: Int  , playWhenReady :Boolean) {
        mediaPlayerListener?.onStateChanged(state,playWhenReady)
        if (state == Player.STATE_READY && playWhenReady) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    localMediaPlayer.getCurrentPosition(), 1f)
        } else if (state == Player.STATE_READY) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    localMediaPlayer.getCurrentPosition(), 1f)
        }
        mMediaSession.setPlaybackState(mStateBuilder.build())
        showNotification(state = mStateBuilder.build())

        Log.i(LOG_TAG, " playWhenReady $playWhenReady  state $state  " )
    }

    companion object {
         lateinit var mMediaSession : MediaSessionCompat
    }

    lateinit var builder : NotificationCompat.Builder
    private lateinit var mStateBuilder: PlaybackStateCompat.Builder
    private val LOG_TAG = "WorkService"
    private var workBinder: IBinder = WorkBinder()
    lateinit var localMediaPlayer: LocalMediaPlayer
    private var mediaPlayerListener: MediaPlayer.MediaPlayerListener? = null
    private lateinit var mNotificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.v(LOG_TAG, "in onCreate")
        mContext = this
        localMediaPlayer = LocalMediaPlayer(this, this, mContext)
        initMediaSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mMediaSession, intent)
        return START_STICKY

    }

    private fun initMediaSession(){
        mMediaSession = MediaSessionCompat(this,"MusicService")

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null)

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PAUSE or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)

        mMediaSession.setPlaybackState(mStateBuilder.build())


        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(MySessionCallback())

        // Start the Media Session since the activity is active.
        mMediaSession.isActive = true
    }

    private inner class MySessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            localMediaPlayer.startPlayback(true)
        }

        override fun onPause() {
            localMediaPlayer.pausePlayback()
        }

        override fun onSkipToPrevious() {
            localMediaPlayer.seekTo(0)
        }
    }


    class MediaReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("MainActivity", "onReceive Media")
            MediaButtonReceiver.handleIntent(mMediaSession, intent)
        }
    }


    fun start() {
        localMediaPlayer.startPlayback(true)
    }

    fun next(){
        localMediaPlayer.next()
    }

    fun stop() {
        localMediaPlayer.pausePlayback()
    }

    fun addMediaItems() {
        localMediaPlayer.loadMediaItems(AppConstants.list, 0)
    }

    override fun onBind(intent: Intent?): IBinder {
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
        mMediaSession.isActive = false
        mNotificationManager.cancelAll()
        Log.v(LOG_TAG, "in onDestroy")
        //unregisterReceiver(MediaReceiver)
    }

    fun setCallback(mediaPlayerListener: MediaPlayer.MediaPlayerListener) {
        this.mediaPlayerListener = mediaPlayerListener
    }

    inner class WorkBinder : Binder() {
        val service: WorkService
            get() = this@WorkService
    }

    private fun showNotification(state: PlaybackStateCompat) {
        builder = NotificationCompat.Builder(this)

        val icon: Int
        val play_pause: String
        if (state.state == PlaybackStateCompat.STATE_PLAYING) {
            icon = R.drawable.exo_controls_pause
            play_pause = "pause"
        } else {
            icon = R.drawable.exo_controls_play
            play_pause = "play"
        }
        val playPauseAction = NotificationCompat.Action(
                icon, play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE))
        val nextAction =  android.support.v4.app.NotificationCompat.Action(R.drawable.exo_controls_next, "next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT))

        val restartAction = android.support.v4.app.NotificationCompat.Action(R.drawable.exo_controls_previous, getString(R.string.restart),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))

        val contentPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0)
        builder.setContentTitle(getString(R.string.guess))
                .setContentText(getString(R.string.notification_text))
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(restartAction)
                .addAction(playPauseAction)
                .addAction(nextAction)
                .setOngoing(false)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.sessionToken)
                        .setShowActionsInCompactView(0, 1))


        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(0, builder.build())
    }


}