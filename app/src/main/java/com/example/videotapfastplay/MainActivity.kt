package com.example.videotapfastplay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
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

    private val pickVideo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
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

    override fun onStop() {
        super.onStop()
        player.release()
    }
}
