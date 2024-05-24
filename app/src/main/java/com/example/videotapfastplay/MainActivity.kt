package com.example.videotapfastplay

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var selectVideoButton: FloatingActionButton
    private var currentUri: Uri? = null
    private var currentPosition: Long = 0

    private val pickVideo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentUri = it
            playVideo(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.player_view)
        selectVideoButton = findViewById(R.id.select_video_button)

        // ExoPlayerの初期化
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // パーミッションの確認
        checkPermissions()

        // 動画選択ボタンのクリックリスナー
        selectVideoButton.setOnClickListener {
            pickVideo.launch("video/*")
        }

        // タップイベントの設定
        playerView.setOnClickListener {
            if (player.playbackParameters.speed == 1.0f) {
                player.setPlaybackParameters(player.playbackParameters.withSpeed(2.0f))
            } else {
                player.setPlaybackParameters(player.playbackParameters.withSpeed(1.0f))
            }
        }

        // ロングタップイベントの設定
        playerView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longPressRunnable)
                    player.setPlaybackParameters(player.playbackParameters.withSpeed(1.0f))
                }
            }
            true
        }
    }

    private fun playVideo(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onStart() {
        super.onStart()
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        currentUri?.let {
            playVideo(it)
            player.seekTo(currentPosition)
        }
    }

    override fun onStop() {
        super.onStop()
        currentPosition = player.currentPosition
        player.release()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横向きの処理
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 縦向きの処理
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val LONG_PRESS_TIMEOUT = 500L // ロングタップとみなす時間 (ミリ秒)
    private val longPressRunnable = Runnable {
        player.setPlaybackParameters(player.playbackParameters.withSpeed(2.0f))
    }
}
