package com.example.reelsapplication

import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.doOnLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.reelsapplication.reels.ReelsData
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource


class ReelPlayer(
    private val player: SimpleExoPlayer,
    private val playerView: PlayerView,
    private val progressBarContainer: LinearLayoutCompat,
    var autoPlay: Boolean = true
) {

    private val _close = MutableLiveData(false)
    val closeObserver : LiveData<Boolean> = _close


    private val dataSourceFactory: DataSource.Factory
    private val internalReels = mutableListOf<Reels>()
    private var reelIndex = 0

    private val delayedPauseRunnable = Runnable { pause() }

    var pauseDelay = 150L

    init {
        val context = playerView.context
        dataSourceFactory = DefaultHttpDataSource.Factory()

        player.apply {
            playerView.player = this
            prepare()
            playWhenReady = autoPlay

            onReady {
                // do something if needed
            }

            onError {
                Toast.makeText(context, "Could not load the video", Toast.LENGTH_LONG).show()
            }

            onPlaybackEnd {
                playbackEnd()
            }

        }

        // handle touches
        playerView.doOnLayout {
            val center = it.width / 2

            it.setOnTouchListener { view, motionEvent ->
                view.performClick()

                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    view.removeCallbacks(delayedPauseRunnable)
                    view.postDelayed(delayedPauseRunnable, pauseDelay) // pause after pauseDelay
                }

                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    view.removeCallbacks(delayedPauseRunnable)

                    val duration = motionEvent.eventTime - motionEvent.downTime

                    val isInRewindArea =
                        motionEvent.x.toInt() in 0..center // touch is close to the left edge of the screen

                    if (duration <= pauseDelay) {
                        if (isInRewindArea) rewind() else forward()
                    } else {
                        play()
                    }

                    return@setOnTouchListener true
                }

                true
            }
        }
    }

    // called once
    private fun playbackEnd() {
        forward()
    }

    // cleanup
    fun release() {
        pause()
        playerView.player?.release()
        playerView.player = null
        playerView.setOnTouchListener(null)
        player.release()
        _close.value = true
    }

    val handler: Handler = Handler()
    private val updateProgressAction = Runnable { onUpdateProgress(player.currentPosition) }

    private fun createProgressiveMediaSource(url: String): MediaSource {
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(url))
    }

    private fun onUpdateProgress(position: Long) {
        if (reelIndex >= internalReels.size) {
            playbackEnd()
            return
        }

        val segment = internalReels[reelIndex]

        segment.setProgress(position)

        handler.postDelayed(updateProgressAction, 50L);
    }

    internal fun pause() {
        player.playWhenReady = false
    }

    internal fun play() {
        player.playWhenReady = true
    }

    private fun forward() {
        //play next segment or close if last played
        if (reelIndex < internalReels.size) {
            internalReels[reelIndex].completed()
        }
        if (++reelIndex < internalReels.size) {
            showReel(reelIndex)
            onUpdateProgress(0)
        }
        else{
            release()
        }
    }

    private fun rewind() {
        //play previous segment or repeat first segment
        internalReels[reelIndex].notStarted()
        if (reelIndex > 0) {
            showReel(--reelIndex)
            internalReels[reelIndex].notStarted()
            onUpdateProgress(0)
        }
        else if (reelIndex == 0){
            showReel(reelIndex)
            internalReels[reelIndex].notStarted()
            onUpdateProgress(0)
        }
    }

    private fun SimpleExoPlayer.onReady(listener: SimpleExoPlayer.() -> Unit) {

        this.addAnalyticsListener(object : AnalyticsListener {
            override fun onPlayerStateChanged(
                eventTime: AnalyticsListener.EventTime,
                playWhenReady: Boolean,
                playbackState: Int
            ) {
                super.onPlayerStateChanged(eventTime, playWhenReady, playbackState)
                if (playbackState == Player.STATE_READY) {
                    listener()
                }
            }
        })
    }

    private fun SimpleExoPlayer.onError(listener: SimpleExoPlayer.() -> Unit) {

        this.addAnalyticsListener(object : AnalyticsListener {
            override fun onPlayerError(
                eventTime: AnalyticsListener.EventTime,
                error: PlaybackException
            ) {
                super.onPlayerError(eventTime, error)
                listener()
            }
        })
    }

    private fun SimpleExoPlayer.onPlaybackEnd(listener: SimpleExoPlayer.() -> Unit) {

        this.addAnalyticsListener(object : AnalyticsListener {
            override fun onPlayerStateChanged(
                eventTime: AnalyticsListener.EventTime,
                playWhenReady: Boolean,
                playbackState: Int
            ) {
                super.onPlayerStateChanged(eventTime, playWhenReady, playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    listener()
                }
            }
        })
    }

    fun setData(reels: List<ReelsData>) {
        if (reels.isNotEmpty()) {
            progressBarContainer.removeAllViews()
            reels.mapIndexedTo(internalReels) { index, reel ->

                // add progress bar to container and set max to duration (millis)
                val progressBar = LayoutInflater.from(progressBarContainer.context)
                    .inflate(R.layout.progress_bar_item, progressBarContainer, false) as ProgressBar
                progressBar.max = reel.duration.toInt()
                progressBarContainer.addView(progressBar)

                Reels(reel.url, reel.duration, progressBar, index)
            }
            showReel(0)
            onUpdateProgress(0)
        }
        else
            release()
    }

    private fun showReel(index: Int) {
        player.setMediaSource(createProgressiveMediaSource(internalReels[index].url))
        player.prepare()
    }

}

data class Reels(
    var url: String,
    var duration: Long,
    var progressBar: ProgressBar,
    var index: Int
) {

    fun completed() {
        progressBar.progress = duration.toInt()
    }

    fun notStarted() {
        progressBar.progress = 0
    }

    fun setProgress(progress: Long) {
        progressBar.progress = progress.toInt()
    }
}