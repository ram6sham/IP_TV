package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotificationAlert
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InAppNotificationBanner(
  alert: NotificationAlert?,
  onWatchNow: (String) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = alert != null,
    enter = slideInVertically(initialOffsetY = { -it }),
    exit = slideOutVertically(targetOffsetY = { -it }),
    modifier = modifier
  ) {
    if (alert != null) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        shadowElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp)
          .testTag("in_app_notification_banner")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Left Live indicator dot
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(LiveRed)
          )

          Spacer(modifier = Modifier.width(10.dp))

          Column(
            modifier = Modifier
              .weight(1f)
              .clickable { onWatchNow(alert.streamId) }
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "🔴 LIVE BROADCAST ALERT",
                color = LiveRed,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
              )
            }
            Text(
              text = alert.streamTitle,
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              maxLines = 1
            )
            Text(
              text = alert.message,
              color = TextSecondary,
              fontSize = 11.sp,
              maxLines = 1
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Watch Now Button
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = BroadcastCyan,
            modifier = Modifier
              .clickable { onWatchNow(alert.streamId) }
              .testTag("alert_watch_now_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Watch",
                tint = Color.Black,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = "Watch",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp).padding(start = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Dismiss",
              tint = TextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
