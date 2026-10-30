package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Requirement 6:
 * Mobile Rotation Ad:
 * Triggered when rotating phone to landscape / portrait if user does not have 12H Ad-Free pass.
 */
@Composable
fun RotationAdDialog(
  adUnitId: String,
  publisherId: String = "ca-pub-9009399398398580",
  authorizedDomain: String = "https://ram6sham.github.io",
  onDismiss: () -> Unit
) {
  var secondsLeft by remember { mutableIntStateOf(6) }
  var canSkip by remember { mutableStateOf(false) }
  val progress = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 6000, easing = LinearEasing)
    )
  }

  LaunchedEffect(Unit) {
    while (secondsLeft > 0) {
      delay(1000)
      secondsLeft--
    }
    canSkip = true
  }

  Dialog(
    onDismissRequest = {
      if (canSkip) onDismiss()
    },
    properties = DialogProperties(
      dismissOnBackPress = canSkip,
      dismissOnClickOutside = canSkip
    )
  ) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = SurfaceDark,
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .padding(16.dp)
        .testTag("rotation_ad_dialog")
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF3B82F6)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "ROTATION INTERSTITIAL AD",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }

          if (canSkip) {
            IconButton(
              onClick = onDismiss,
              modifier = Modifier.size(28.dp).testTag("close_rotation_ad_button")
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
          } else {
            Surface(shape = CircleShape, color = SurfaceVariantDark) {
              Text(
                text = "${secondsLeft}s",
                color = BroadcastCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceHighlight),
          contentAlignment = Alignment.Center
        ) {
          AdSenseWebView(
            clientId = publisherId.ifBlank { "ca-pub-9009399398398580" },
            slotId = adUnitId.ifBlank { "9034181554" },
            authorizedDomain = authorizedDomain,
            modifier = Modifier.fillMaxWidth().height(180.dp)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LinearProgressIndicator(
          progress = { progress.value },
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = BroadcastCyan,
          trackColor = SurfaceVariantDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (canSkip) {
          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = BroadcastCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("skip_rotation_ad_button")
          ) {
            Text("Continue Video", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        } else {
          Text(text = "Skip in $secondsLeft seconds...", color = TextSecondary, fontSize = 11.sp)
        }
      }
    }
  }
}
