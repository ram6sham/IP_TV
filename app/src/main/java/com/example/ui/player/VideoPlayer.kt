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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.LiveStream
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import kotlinx.coroutines.delay

enum class ScreenResizeMode(val label: String, val modeValue: Int) {
  FIT("Fit", AspectRatioFrameLayout.RESIZE_MODE_FIT),
  FILL("Fill", AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
  stream: LiveStream,
  activeUrl: String,
  selectedServer: String,
  onSelectServer: (String, String) -> Unit,
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

  // Requirement 4: Screen Fit, Fill, Auto and Screen Lock
  var currentResizeMode by remember { mutableStateOf(ScreenResizeMode.FIT) }
  var isScreenLocked by remember { mutableStateOf(false) }
  var showUnlockPrompt by remember { mutableStateOf(false) }

  val targetStreamUrl = if (activeUrl.isNotBlank()) activeUrl else stream.streamUrl
  val availableServers = remember(stream) { stream.getAvailableServers() }

  // Keep screen always on during live streaming
  DisposableEffect(Unit) {
    val activity = context as? Activity
    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    onDispose {}
  }

  // Single persistent ExoPlayer instance per player composable to prevent MediaCodec exhaustion
  val exoPlayer = remember {
    try {
      ExoPlayer.Builder(context).build()
    } catch (_: Throwable) {
      null
    }
  }

  // Update MediaItem on the active player without destroying ExoPlayer
  LaunchedEffect(exoPlayer, targetStreamUrl) {
    val player = exoPlayer ?: return@LaunchedEffect
    if (targetStreamUrl.isNotBlank()) {
      try {
        val mediaItem = MediaItem.fromUri(Uri.parse(targetStreamUrl))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        playbackError = null
      } catch (t: Throwable) {
        playbackError = "Check Your Internet or Check Next Server"
      }
    } else {
      player.stop()
      player.clearMediaItems()
    }
  }

  DisposableEffect(exoPlayer) {
    val playerInstance = exoPlayer
    if (playerInstance != null) {
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
          val msg = error.localizedMessage ?: ""
          if (msg.contains("MediaCodec", ignoreCase = true) || error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED) {
            try {
              playerInstance.prepare()
            } catch (_: Throwable) {}
          }
          playbackError = "Check Your Internet or Check Next Server"
        }
      }

      playerInstance.addListener(listener)

      onDispose {
        playerInstance.removeListener(listener)
        playerInstance.stop()
        playerInstance.clearMediaItems()
        playerInstance.release()
      }
    } else {
      onDispose {}
    }
  }

  // Auto-hide controls after 4 seconds if not locked
  LaunchedEffect(showControls, isPlaying, isScreenLocked) {
    if (showControls && isPlaying && !isScreenLocked) {
      delay(4000)
      showControls = false
    }
  }

  // Auto-hide unlock prompt after 3 seconds
  LaunchedEffect(showUnlockPrompt) {
    if (showUnlockPrompt) {
      delay(3000)
      showUnlockPrompt = false
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
        if (isScreenLocked) {
          showUnlockPrompt = !showUnlockPrompt
        } else {
          showControls = !showControls
        }
      }
      .testTag("video_player_box")
  ) {
    // Media3 PlayerView with dynamic resizeMode
    AndroidView(
      factory = { ctx ->
        try {
          PlayerView(ctx).apply {
            player = exoPlayer
            useController = false
            keepScreenOn = true
            resizeMode = currentResizeMode.modeValue
            layoutParams = android.view.ViewGroup.LayoutParams(
              android.view.ViewGroup.LayoutParams.MATCH_PARENT,
              android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
          }
        } catch (_: Throwable) {
          android.view.View(ctx)
        }
      },
      update = { playerView ->
        if (playerView is PlayerView) {
          try {
            playerView.player = exoPlayer
            playerView.keepScreenOn = true
            playerView.resizeMode = currentResizeMode.modeValue
          } catch (_: Throwable) {}
        }
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

    // Playback Error Fallback
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
            modifier = Modifier.size(44.dp)
          )
          Spacer(modifier = Modifier.size(10.dp))
          Text(
            text = "Check Your Internet or Check Next Server",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Text(
            text = "The current server is unavailable or connection was interrupted.",
            color = Color.LightGray,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
          )

          // Alternative server recommendation
          if (availableServers.size > 1) {
            Text(
              text = "Switch to Another Server:",
              color = BroadcastCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.padding(bottom = 6.dp)
            ) {
              availableServers.forEach { (name, url) ->
                val isCurrent = name == selectedServer
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = if (isCurrent) LiveRed else BroadcastCyan.copy(alpha = 0.2f),
                  border = if (isCurrent) null else androidx.compose.foundation.BorderStroke(1.dp, BroadcastCyan.copy(alpha = 0.5f)),
                  modifier = Modifier.clickable {
                    playbackError = null
                    onSelectServer(name, url)
                  }
                ) {
                  Text(
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.size(12.dp))
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = BroadcastCyan,
            modifier = Modifier
              .clickable {
                playbackError = null
                exoPlayer?.prepare()
                exoPlayer?.play()
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
              Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }
      }
    }

    // Screen Lock Overlay (Requirement 4)
    if (isScreenLocked) {
      AnimatedVisibility(
        visible = showUnlockPrompt,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = Color.Black.copy(alpha = 0.85f),
          border = androidx.compose.foundation.BorderStroke(1.dp, BroadcastCyan),
          modifier = Modifier
            .clickable {
              isScreenLocked = false
              showControls = true
              showUnlockPrompt = false
            }
            .testTag("unlock_screen_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LockOpen,
              contentDescription = "Unlock",
              tint = BroadcastCyan,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Unlock Controls",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    // Normal Controls Overlay (When not locked)
    AnimatedVisibility(
      visible = showControls && !isScreenLocked,
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
                Color.Black.copy(alpha = 0.8f),
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // LIVE Badge and Match Title
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
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

            Text(
              text = stream.title,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              maxLines = 1
            )
          }

          // Top Action Buttons: Lock & Fullscreen
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Lock Button (Requirement 4)
            IconButton(
              onClick = {
                isScreenLocked = true
                showControls = false
                showUnlockPrompt = true
              },
              modifier = Modifier
                .size(36.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                .testTag("lock_screen_button")
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock Controls",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }

            if (isFullscreen) {
              Spacer(modifier = Modifier.width(6.dp))
              IconButton(
                onClick = onFullscreenToggle,
                modifier = Modifier
                  .size(36.dp)
                  .background(Color.Black.copy(alpha = 0.5f), CircleShape)
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
              exoPlayer?.pause()
            } else {
              exoPlayer?.play()
            }
          },
          modifier = Modifier
            .align(Alignment.Center)
            .size(56.dp)
            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
            .testTag("play_pause_button")
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(34.dp)
          )
        }

        // Bottom Controls Bar (Clean unified layout: Servers on left, Fit/Fill/Mute/Fullscreen on right)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left: Multi-Server Selection Chips
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            availableServers.forEach { (serverName, url) ->
              val isCurrent = serverName == selectedServer
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isCurrent) LiveRed else Color.Black.copy(alpha = 0.6f),
                border = if (isCurrent) null else androidx.compose.foundation.BorderStroke(0.8.dp, Color.Gray),
                modifier = Modifier
                  .clickable { onSelectServer(serverName, url) }
                  .testTag("server_button_${serverName.replace(" ", "_")}")
              ) {
                Text(
                  text = serverName,
                  color = Color.White,
                  fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          // Right: Mute, Fit/Fill, and Fullscreen
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            // Mute / Unmute
            IconButton(
              onClick = {
                isMuted = !isMuted
                exoPlayer?.volume = if (isMuted) 0f else 1f
              },
              modifier = Modifier.size(32.dp).testTag("mute_toggle_button")
            ) {
              Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }

            // Screen Aspect Ratio Modes: Fit & Fill (Auto removed)
            ScreenResizeMode.entries.forEach { mode ->
              val isSelected = currentResizeMode == mode
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) BroadcastCyan else Color.Black.copy(alpha = 0.5f),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(0.6.dp, Color.Gray),
                modifier = Modifier
                  .clickable { currentResizeMode = mode }
                  .testTag("resize_mode_${mode.name.lowercase()}")
              ) {
                Text(
                  text = mode.label,
                  color = if (isSelected) Color.Black else Color.White,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 10.sp,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }

            // Fullscreen Toggle
            IconButton(
              onClick = onFullscreenToggle,
              modifier = Modifier.size(32.dp).testTag("fullscreen_button")
            ) {
              Icon(
                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }
  }
}
