package com.example.reelsapplication.reels

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import com.example.reelsapplication.R
import com.example.reelsapplication.ReelPlayer
import com.example.reelsapplication.customize
import com.example.reelsapplication.dp
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.metadata.mp4.SlowMotionData.Segment
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import java.util.concurrent.TimeUnit

class ReelActivity1 : AppCompatActivity() {

    private lateinit var player: SimpleExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var segmentedVideoPlayer: ReelPlayer
    private lateinit var progressContainer: LinearLayoutCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reel1)

        progressContainer = findViewById(R.id.progress_container)
        playerView = findViewById(R.id.player_view)

        player = SimpleExoPlayer.Builder(this).build()
        segmentedVideoPlayer = ReelPlayer(player, playerView, progressContainer, autoPlay = true)
        updateProgress()

        val reels = mutableListOf(
            ReelsData("https://user-images.githubusercontent.com/90382113/170887700-e405c71e-fe31-458d-8572-aea2e801eecc.mp4",8000L),
            ReelsData("https://user-images.githubusercontent.com/90382113/170890384-43214cc8-79c6-4815-bcb7-e22f6f7fe1bc.mp4",6000L),
            ReelsData("https://user-images.githubusercontent.com/90382113/170889265-7ed9a56c-dd5f-4d78-b453-18b011644da0.mp4",11000L),
            ReelsData("https://user-images.githubusercontent.com/90382113/170885742-d82e3b59-e45a-4fcf-a851-fed58ff5a690.mp4",10000L)
        )

        segmentedVideoPlayer.setData(reels)

        segmentedVideoPlayer.closeObserver.observe(this){
            if (it)
                finish()
        }

        findViewById<ImageView>(R.id.closeBtn).setOnClickListener {
            segmentedVideoPlayer.release()
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        segmentedVideoPlayer.release()
    }


    private fun updateProgress() {
        // progress bars
        progressContainer.children.map { it as ProgressBar }
            .forEach {
                it.customize(
                    Color.WHITE,
                    Color.GRAY,
                    4.dp,
                    2.dp
                )
            }

        // progress container
        progressContainer.customize(
            10.dp,
            4.dp,
            12.dp,
            4.dp,
            0.dp
        )

    }

}