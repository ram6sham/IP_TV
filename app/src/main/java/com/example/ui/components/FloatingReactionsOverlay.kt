package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FloatingReaction

@Composable
fun FloatingReactionItem(
  reaction: FloatingReaction,
  onFinished: (String) -> Unit
) {
  val animY = remember { Animatable(0f) }
  val animAlpha = remember { Animatable(1f) }

  LaunchedEffect(reaction.id) {
    animY.animateTo(
      targetValue = -350f,
      animationSpec = tween(durationMillis = 2200, easing = EaseOut)
    )
  }

  LaunchedEffect(reaction.id) {
    // Fade out near top
    animAlpha.animateTo(
      targetValue = 0f,
      animationSpec = tween(durationMillis = 2200, delayMillis = 600)
    )
    onFinished(reaction.id)
  }

  val density = LocalDensity.current
  val xOffsetPx = remember(reaction.startXFraction) {
    (reaction.startXFraction * 600f - 300f).toInt()
  }

  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = Alignment.BottomCenter
  ) {
    Text(
      text = reaction.emoji,
      fontSize = 28.sp,
      modifier = Modifier
        .offset {
          IntOffset(
            x = xOffsetPx,
            y = animY.value.toInt()
          )
        }
        .alpha(animAlpha.value)
    )
  }
}
