package com.example.ui.player

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.model.LiveStream
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
  stream: LiveStream,
  modifier: Modifier = Modifier,
  isFullscreen: Boolean = false,
  onFullscreenToggle: () -> Unit = {}
) {
  val context = LocalContext.current
  var isPlaying by remember { mutableStateOf(true) }
  var isBuffering by remember { mutableStateOf(true) }
  var isMuted by remember { mutableStateOf(false) }
  var showControls by remember { mutableStateOf(true) }
  var playbackError by remember { mutableStateOf<String?>(null) }

  // Guarantee Screen NEVER dims or turns off while watching Live TV
  DisposableEffect(Unit) {
    val activity = context as? Activity
    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    onDispose {
      // Retain flags as needed
    }
  }

  // ExoPlayer instance
  val exoPlayer = remember(stream.id, stream.streamUrl) {
    ExoPlayer.Builder(context).build().apply {
      val mediaItem = MediaItem.fromUri(Uri.parse(stream.streamUrl))
      setMediaItem(mediaItem)
      prepare()
      playWhenReady = true
    }
  }

  DisposableEffect(exoPlayer) {
    val listener = object : Player.Listener {
      override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
          Player.STATE_BUFFERING -> {
            isBuffering = true
            playbackError = null
          }
          Player.STATE_READY -> {
            isBuffering = false
            playbackError = null
          }
          Player.STATE_ENDED -> {
            isBuffering = false
          }
          Player.STATE_IDLE -> {
            isBuffering = false
          }
        }
      }

      override fun onIsPlayingChanged(playing: Boolean) {
        isPlaying = playing
      }

      override fun onPlayerError(error: PlaybackException) {
        isBuffering = false
        playbackError = error.localizedMessage ?: "Unable to connect to live stream source"
      }
    }

    exoPlayer.addListener(listener)

    onDispose {
      exoPlayer.removeListener(listener)
      exoPlayer.release()
    }
  }

  // Auto-hide controls after 4 seconds
  LaunchedEffect(showControls, isPlaying) {
    if (showControls && isPlaying) {
      delay(4000)
      showControls = false
    }
  }

  val baseModifier = if (isFullscreen) {
    modifier.fillMaxSize()
  } else {
    modifier
      .fillMaxWidth()
      .aspectRatio(16f / 9f)
  }

  Box(
    modifier = baseModifier
      .background(Color.Black)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        showControls = !showControls
      }
      .testTag("video_player_box")
  ) {
    // Player Surface View with keepScreenOn enabled
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = exoPlayer
          useController = false
          keepScreenOn = true // Prevents screen timeout and dimming
          layoutParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
          )
        }
      },
      update = { playerView ->
        playerView.player = exoPlayer
        playerView.keepScreenOn = true
      },
      modifier = Modifier.fillMaxSize()
    )

    // Buffering Spinner
    if (isBuffering && playbackError == null) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(
          color = BroadcastCyan,
          strokeWidth = 3.dp,
          modifier = Modifier.size(44.dp)
        )
      }
    }

    // Playback Error Fallback Card
    if (playbackError != null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xEE0D131F)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(16.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Stream Offline",
            tint = LiveRed,
            modifier = Modifier.size(40.dp)
          )
          Spacer(modifier = Modifier.size(8.dp))
          Text(
            text = "Stream Offline or Connecting",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
          Text(
            text = stream.streamUrl,
            color = Color.LightGray,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp)
          )
          Spacer(modifier = Modifier.size(12.dp))
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = BroadcastCyan,
            modifier = Modifier
              .clickable {
                playbackError = null
                exoPlayer.prepare()
                exoPlayer.play()
              }
              .testTag("retry_stream_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Retry Stream", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }
      }
    }

    // Controls Overlay
    AnimatedVisibility(
      visible = showControls,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                Color.Black.copy(alpha = 0.75f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.85f)
              )
            )
          )
      ) {
        // Top Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // LIVE Badge and Viewers
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = LiveRed,
              modifier = Modifier.padding(end = 8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "LIVE",
                  color = Color.White,
                  fontWeight = FontWeight.Black,
                  fontSize = 11.sp
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color.Black.copy(alpha = 0.6f)
            ) {
              Text(
                text = "${stream.viewersCount / 1000}.${(stream.viewersCount % 1000) / 100}k watching",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            if (isFullscreen) {
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = stream.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1
              )
            }
          }

          // Resolution / Landscape Exit Controls
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color.Black.copy(alpha = 0.6f),
              modifier = Modifier.padding(end = 8.dp)
            ) {
              Text(
                text = stream.resolution,
                color = BroadcastCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            if (isFullscreen) {
              IconButton(
                onClick = onFullscreenToggle,
                modifier = Modifier
                  .size(36.dp)
                  .background(Color.Black.copy(alpha = 0.6f), CircleShape)
              ) {
                Icon(
                  imageVector = Icons.Default.FullscreenExit,
                  contentDescription = "Exit Fullscreen",
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }

        // Center Play / Pause Button
        IconButton(
          onClick = {
            if (isPlaying) {
              exoPlayer.pause()
            } else {
              exoPlayer.play()
            }
          },
          modifier = Modifier
            .align(Alignment.Center)
            .size(58.dp)
            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            .testTag("play_pause_button")
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(34.dp)
          )
        }

        // Bottom Controls Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Volume & Live Sync
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = {
                isMuted = !isMuted
                exoPlayer.volume = if (isMuted) 0f else 1f
              },
              modifier = Modifier.size(36.dp).testTag("mute_toggle_button")
            ) {
              Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White.copy(alpha = 0.15f),
              modifier = Modifier.clickable {
                exoPlayer.seekToDefaultPosition()
              }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(LiveRed)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Go to Live",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          // Fullscreen / Cinema Mode Toggle Button
          IconButton(
            onClick = onFullscreenToggle,
            modifier = Modifier.size(36.dp).testTag("fullscreen_button")
          ) {
            Icon(
              imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
              contentDescription = if (isFullscreen) "Exit Fullscreen" else "Enter Landscape Fullscreen",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }
    }
  }
}
