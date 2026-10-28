package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RemoteConfigDialog(
  currentUrl: String,
  syncStatus: String?,
  onDismiss: () -> Unit,
  onSaveUrl: (String) -> Unit,
  onSyncNow: () -> Unit
) {
  var urlInput by remember { mutableStateOf(currentUrl) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = SurfaceDark,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 16.dp)
        .testTag("remote_config_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CloudSync,
              contentDescription = "Cloud Sync",
              tint = BroadcastCyan,
              modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Auto Link Update System",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = TextPrimary
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Nepali Explanation
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = SurfaceVariantDark,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "💡 Link Auto-Update कसरी काम गर्छ?",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = BroadcastCyan
            )
            Text(
              text = "कुनै पनि निःशुल्क सर्भर (जस्तै GitHub Gist, Raw JSON, वा Firebase) मा एउटा JSON फाइल बनाउनुहोस्। एपले प्रत्येक ६० सेकेन्डमा स्वतः नयाँ लिङ्क चेक गर्छ। तपाईँले अनलाइन लिङ्क फेर्ने बित्तिकै संसारभरका सबै मोबाइलमा स्वतः नयाँ स्ट्रिम चल्नेछ!",
              fontSize = 11.sp,
              color = TextSecondary,
              lineHeight = 16.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Remote Master JSON URL",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
          value = urlInput,
          onValueChange = { urlInput = it },
          placeholder = { Text("https://raw.githubusercontent.com/.../streams.json", color = TextMuted, fontSize = 11.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = BroadcastCyan)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("remote_config_url_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceVariantDark,
            unfocusedContainerColor = SurfaceVariantDark,
            focusedBorderColor = BroadcastCyan,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(12.dp)
        )

        // Sample JSON format preview
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "JSON Structure Example (अनलाइन राख्ने ढाँचा):",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextSecondary
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFF090D16),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
        ) {
          Text(
            text = """[
  {
    "id": "stream_pptv_official",
    "title": "PP TV 24/7 Live Stream HD",
    "streamUrl": "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
    "category": "NEWS",
    "broadcasterName": "PP TV Studios"
  }
]""",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = BroadcastCyan,
            modifier = Modifier.padding(10.dp)
          )
        }

        if (syncStatus != null) {
          Text(
            text = "Status: $syncStatus",
            color = BroadcastCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceHighlight,
            modifier = Modifier.clickable { onSyncNow() }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(imageVector = Icons.Default.Refresh, contentDescription = "Sync", tint = BroadcastCyan, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Sync Now", color = BroadcastCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDismiss) {
              Text("Cancel", color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Button(
              onClick = {
                onSaveUrl(urlInput)
                onDismiss()
              },
              colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("save_remote_url_button")
            ) {
              Text("Save URL", color = Color.White, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
