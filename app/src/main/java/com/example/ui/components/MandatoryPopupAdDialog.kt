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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
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
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Requirement 6:
 * Mandatory Startup Popup Ad:
 * User must watch for 15 seconds. After 15 seconds, unlocks 12 Hours Ad-Free!
 */
@Composable
fun MandatoryPopupAdDialog(
  publisherId: String,
  adUnitId: String,
  authorizedDomain: String = "https://ram6sham.github.io",
  onDismiss: () -> Unit,
  onRewardEarned: () -> Unit
) {
  var secondsLeft by remember { mutableIntStateOf(15) }
  var isCompleted by remember { mutableStateOf(false) }
  val progress = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 15000, easing = LinearEasing)
    )
  }

  LaunchedEffect(Unit) {
    while (secondsLeft > 0) {
      delay(1000)
      secondsLeft--
    }
    isCompleted = true
  }

  Dialog(
    onDismissRequest = {
      if (isCompleted) onDismiss()
    },
    properties = DialogProperties(
      dismissOnBackPress = isCompleted,
      dismissOnClickOutside = isCompleted
    )
  ) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = SurfaceDark,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .padding(16.dp)
        .testTag("mandatory_popup_ad_dialog")
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top Ad Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF59E0B)
          ) {
            Text(
              text = "Sponsored Ad",
              color = Color.Black,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }

          if (isCompleted) {
            IconButton(
              onClick = {
                onRewardEarned()
                onDismiss()
              },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
            }
          } else {
            Surface(
              shape = CircleShape,
              color = SurfaceVariantDark
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = LiveRed, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "${secondsLeft}s",
                  color = LiveRed,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ad Banner Media Creative with Google AdSense
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceHighlight),
          contentAlignment = Alignment.Center
        ) {
          AdSenseWebView(
            clientId = publisherId.ifBlank { "ca-pub-9009399398398580" },
            slotId = adUnitId.ifBlank { "9034181554" },
            authorizedDomain = authorizedDomain,
            modifier = Modifier.fillMaxWidth().height(200.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar
        LinearProgressIndicator(
          progress = { progress.value },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = if (isCompleted) Color(0xFF10B981) else LiveRed,
          trackColor = SurfaceVariantDark
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (isCompleted) {
          Button(
            onClick = {
              onRewardEarned()
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("claim_mandatory_ad_button")
          ) {
            Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
          }
        } else {
          Text(
            text = "${secondsLeft}s",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}
