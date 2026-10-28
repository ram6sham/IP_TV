package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
  isAdminMode: Boolean,
  remoteConfigUrl: String,
  remoteSyncStatus: String?,
  onOpenAdminLogin: () -> Unit,
  onLockAdmin: () -> Unit,
  onOpenRemoteConfig: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .testTag("settings_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = CircleShape,
          color = LiveRed,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text("PP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
          }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("PP TV App Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
          Text("Version 1.0 • Live TV Experience", fontSize = 12.sp, color = TextSecondary)
        }
      }
    }

    // Playback & Display Features Card
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Display & Playback (डिस्प्ले सेटिङ्स)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
          Spacer(modifier = Modifier.height(10.dp))

          // Screen Always On status
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Brightness7, contentDescription = null, tint = BroadcastCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text("Screen Keep-On & Full Brightness", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
              Text("स्ट्रिम हेर्दा स्क्रिन कहिल्यै निभ्दैन र ब्राइटनेस घट्दैन (Active)", fontSize = 11.sp, color = TextSecondary)
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF10B981).copy(alpha = 0.2f)
            ) {
              Text("ACTIVE", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Landscape Fullscreen status
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = BroadcastCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text("Landscape & Cinema Mode (तेर्सो बनाएर हेर्ने)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
              Text("मोबाइल तेर्सो बनाउँदा वा फुलस्क्रिन बटन थिच्दा फुलस्क्रिन चल्छ", fontSize = 11.sp, color = TextSecondary)
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF10B981).copy(alpha = 0.2f)
            ) {
              Text("ENABLED", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
            }
          }
        }
      }
    }

    // Broadcaster & Admin Control Card
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (isAdminMode) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isAdminMode) Color(0xFF10B981) else LiveRed,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Broadcaster Access Control", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isAdminMode) Color(0xFF10B981).copy(alpha = 0.2f) else LiveRed.copy(alpha = 0.2f)
            ) {
              Text(
                text = if (isAdminMode) "ADMIN UNLOCKED" else "VIEWER MODE",
                color = if (isAdminMode) Color(0xFF10B981) else LiveRed,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Text(
            text = if (isAdminMode) {
              "तपाईँ एडमिन मोडमा हुनुहुन्छ। Broadcast Studio र Add Stream लिंक सक्रिय छन्। अरुलाई दिनु अघि लक गर्नुहोस्।"
            } else {
              "साधारण दर्शकहरूले स्ट्रिम मात्र हेर्न पाउँछन्। लिंक थप्न वा ब्रोडकास्ट गर्न एडमिन पिन (१२३४) हाल्नुहोस्।"
            },
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
          )

          if (isAdminMode) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = LiveRed,
                modifier = Modifier
                  .weight(1f)
                  .clickable { onLockAdmin() }
                  .testTag("lock_admin_mode_button")
              ) {
                Row(
                  modifier = Modifier.padding(vertical = 10.dp),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Lock Admin Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = BroadcastCyan,
                modifier = Modifier
                  .weight(1f)
                  .clickable { onOpenRemoteConfig() }
                  .testTag("open_remote_config_button")
              ) {
                Row(
                  modifier = Modifier.padding(vertical = 10.dp),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Auto Link Setup", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }
            }
          } else {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = BroadcastCyan,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAdminLogin() }
                .testTag("login_admin_button")
            ) {
              Row(
                modifier = Modifier.padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enter Admin PIN to Unlock (पिन हाल्नुहोस्)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      }
    }

    // Auto Link Update Info
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudSync, contentDescription = null, tint = BroadcastCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Auto Link Update Status", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (remoteConfigUrl.isNotBlank()) {
              "Remote URL: $remoteConfigUrl\n${remoteSyncStatus ?: "Active sync enabled (checks every 60s)"}"
            } else {
              "Remote URL configure गरिएको छैन। Admin मोडमा गएर Auto Link Setup बाट Remote Master JSON लिङ्क सेट गर्न सक्नुहुन्छ।"
            },
            fontSize = 12.sp,
            color = TextSecondary
          )
        }
      }
    }
  }
}
