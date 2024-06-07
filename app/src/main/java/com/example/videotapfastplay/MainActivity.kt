package com.example.videotapfastplay

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
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
    private var controlsVisible: Boolean = true
    private lateinit var gestureDetector: GestureDetector
    private val handler = Handler(Looper.getMainLooper())

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

        // ジェスチャーディテクタの設定
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                toggleControlsVisibility()
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                val screenWidth = playerView.width
                val touchX = e.x
                if (touchX > screenWidth / 2) {
                    // 右半分のダブルタップ
                    fastForwardVideo(5000)  // Fast forward 5 seconds
                } else {
                    // 左半分のダブルタップ
                    rewindVideo(5000)  // Rewind 5 seconds
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val screenWidth = playerView.width
                val touchX = e.x
                if (touchX > screenWidth / 2) {
                    // 右半分の長押し
                    setPlaybackSpeed(3.0f)
                } else {
                    // 左半分の長押し
                    setPlaybackSpeed(0.5f)  // Slow down to half speed
                }
            }
        })

        // タップイベントの設定
        playerView.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                handler.removeCallbacksAndMessages(null)
                player.setPlaybackParameters(player.playbackParameters.withSpeed(1.0f))
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

    private fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackParameters(player.playbackParameters.withSpeed(speed))
    }

    private fun rewindVideo(milliseconds: Long) {
        val newPosition = (player.currentPosition - milliseconds).coerceAtLeast(0)
        player.seekTo(newPosition)
    }

    private fun fastForwardVideo(milliseconds: Long) {
        val newPosition = (player.currentPosition + milliseconds).coerceAtMost(player.duration)
        player.seekTo(newPosition)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    private fun toggleControlsVisibility() {
        if (controlsVisible) {
            playerView.hideController()
        } else {
            playerView.showController()
        }
        controlsVisible = !controlsVisible
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
}
