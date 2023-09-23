package com.example.reelsapplication.reels

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import com.example.reelsapplication.R
import com.example.reelsapplication.ReelPlayer
import com.example.reelsapplication.customize
import com.example.reelsapplication.dp
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class ReelActivity : AppCompatActivity() {

    private lateinit var player: SimpleExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var segmentedVideoPlayer: ReelPlayer
    private lateinit var progressContainer: LinearLayoutCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reel)

        val reels = intent.getStringArrayListExtra("data") ?: emptyList()

        progressContainer = findViewById(R.id.progress_container)
        playerView = findViewById(R.id.player_view)

        player = SimpleExoPlayer.Builder(this).build()
        segmentedVideoPlayer = ReelPlayer(player, playerView, progressContainer, autoPlay = true)
        updateProgress()

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
