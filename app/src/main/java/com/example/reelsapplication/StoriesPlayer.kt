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
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource


class StoriesPlayer(
    private val player: SimpleExoPlayer,
    private val playerView: PlayerView,
    private val progressBarContainer: LinearLayoutCompat,
    var autoPlay: Boolean = true
) {

    private val _close = MutableLiveData(false)
    val closeObserver : LiveData<Boolean> = _close


    private val dataSourceFactory: DataSource.Factory
    private val internalStories = mutableListOf<Stories>()
    private var storiesIndex = DEFAULT_INDEX

    private val delayedPauseRunnable = Runnable { pause() }

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
                    view.postDelayed(delayedPauseRunnable, PAUSE_DELAY) // pause after pauseDelay
                }

                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    view.removeCallbacks(delayedPauseRunnable)

                    val duration = motionEvent.eventTime - motionEvent.downTime

                    val isInRewindArea =
                        motionEvent.x.toInt() in 0..center // touch is close to the left edge of the screen

                    if (duration <= PAUSE_DELAY) {
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
        if (storiesIndex >= internalStories.size) {
            playbackEnd()
            return
        }

        val segment = internalStories[storiesIndex]

        if (position == DEFAULT_INDEX.toLong())
            segment.setMaxDuration(player.duration)

        segment.setProgress(position)

        handler.postDelayed(updateProgressAction, PROGRESS_DELAY);
    }

    internal fun pause() {
        player.playWhenReady = false
    }

    internal fun play() {
        player.playWhenReady = true
    }

    private fun forward() {
        //play next segment or close if last played
        if (storiesIndex < internalStories.size) {
            internalStories[storiesIndex].completed()
        }
        if (++storiesIndex < internalStories.size) {
            showStory(storiesIndex)
            onUpdateProgress(0)
        }
        else{
            release()
        }
    }

    private fun rewind() {
        //play previous segment or repeat first segment
        internalStories[storiesIndex].notStarted()
        if (storiesIndex > DEFAULT_INDEX) {
            showStory(--storiesIndex)
            internalStories[storiesIndex].notStarted()
            onUpdateProgress(DEFAULT_INDEX.toLong())
        }
        else if (storiesIndex == DEFAULT_INDEX){
            showStory(storiesIndex)
            internalStories[storiesIndex].notStarted()
            onUpdateProgress(DEFAULT_INDEX.toLong())
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

    fun setData(stories: List<String>) {
        if (stories.isNotEmpty()) {
            progressBarContainer.removeAllViews()
            stories.mapIndexedTo(internalStories) { index, url ->

                // add progress bar to container and set max to duration (millis)
                val progressBar = LayoutInflater.from(progressBarContainer.context)
                    .inflate(R.layout.progress_bar_item, progressBarContainer, false) as ProgressBar
                //progressBar.max = story.duration.toInt()
                progressBarContainer.addView(progressBar)

                Stories(url = url, progressBar = progressBar, index = index)
            }
            showStory(0)
            onUpdateProgress(0)
        }
        else
            release()
    }

    private fun showStory(index: Int) {
        player.setMediaSource(createProgressiveMediaSource(internalStories[index].url))
        player.prepare()
    }

    companion object {
        const val DEFAULT_INDEX = 0
        const val PAUSE_DELAY = 150L
        const val PROGRESS_DELAY = 50L
    }
}

data class Stories(
    var url: String,
    var duration: Long = 0,
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

    fun setMaxDuration(duration:Long) {
        progressBar.max = duration.toInt()
        this.duration = duration
    }
}
