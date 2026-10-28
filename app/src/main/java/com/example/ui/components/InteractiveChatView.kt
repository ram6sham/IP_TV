package com.example.ui.components

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.UserBadge
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InteractiveChatView(
  messages: List<ChatMessage>,
  onSendMessage: (String) -> Unit,
  onTriggerReaction: (String) -> Unit,
  onLikeMessage: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  // Auto scroll to bottom when new messages arrive
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val pinnedMessage = remember(messages) {
    messages.firstOrNull { it.isPinned }
  }
  val standardMessages = remember(messages) {
    messages.filter { !it.isPinned }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(SurfaceDark)
  ) {
    // Pinned Message Header (if available)
    if (pinnedMessage != null) {
      Surface(
        color = SurfaceVariantDark,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Row(
          modifier = Modifier.padding(10.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.PushPin,
            contentDescription = "Pinned",
            tint = LiveRed,
            modifier = Modifier
              .size(16.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = pinnedMessage.userName,
                color = LiveRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              BadgeTag(badge = pinnedMessage.badge)
            }
            Text(
              text = pinnedMessage.message,
              color = TextPrimary,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
        }
      }
    }

    // Chat Messages Stream
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .testTag("chat_messages_list"),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(standardMessages, key = { it.id }) { msg ->
        ChatMessageRow(
          message = msg,
          onLike = { onLikeMessage(msg.id) }
        )
      }
    }

    // Quick Emoji Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(SurfaceVariantDark)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val emojis = listOf("❤️", "🔥", "👏", "🇳🇵", "🎉", "🚀")
      emojis.forEach { emoji ->
        Surface(
          shape = CircleShape,
          color = SurfaceHighlight,
          modifier = Modifier
            .size(38.dp)
            .clickable { onTriggerReaction(emoji) }
            .testTag("reaction_button_$emoji")
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(text = emoji, fontSize = 18.sp)
          }
        }
      }
    }

    // Input Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(SurfaceDark)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        placeholder = { Text("Say something in live chat...", color = TextMuted, fontSize = 13.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
          onSend = {
            if (inputText.isNotBlank()) {
              onSendMessage(inputText)
              inputText = ""
            }
          }
        ),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = SurfaceVariantDark,
          unfocusedContainerColor = SurfaceVariantDark,
          focusedBorderColor = BroadcastCyan,
          unfocusedBorderColor = Color.Transparent,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("chat_input_field")
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = {
          if (inputText.isNotBlank()) {
            onSendMessage(inputText)
            inputText = ""
          }
        },
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(BroadcastCyan)
          .testTag("send_chat_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send",
          tint = Color.Black,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun ChatMessageRow(
  message: ChatMessage,
  onLike: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    verticalAlignment = Alignment.Top
  ) {
    // User Avatar Initials
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(Color(message.avatarColorHex)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = message.userName.take(1).uppercase(),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = message.userName,
          fontWeight = FontWeight.SemiBold,
          fontSize = 12.sp,
          color = if (message.isMyMessage) BroadcastCyan else TextSecondary
        )
        Spacer(modifier = Modifier.width(6.dp))
        BadgeTag(badge = message.badge)
      }
      Text(
        text = message.message,
        fontSize = 13.sp,
        color = TextPrimary,
        modifier = Modifier.padding(top = 2.dp)
      )
    }

    // Like message
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clickable { onLike() }
        .padding(start = 4.dp, top = 4.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Like Message",
        tint = if (message.likesCount > 0) LiveRed else TextMuted,
        modifier = Modifier.size(14.dp)
      )
      if (message.likesCount > 0) {
        Spacer(modifier = Modifier.width(3.dp))
        Text(
          text = message.likesCount.toString(),
          color = TextMuted,
          fontSize = 11.sp
        )
      }
    }
  }
}

@Composable
fun BadgeTag(badge: UserBadge) {
  if (badge == UserBadge.VIEWER) return
  Surface(
    shape = RoundedCornerShape(4.dp),
    color = Color(badge.colorHex).copy(alpha = 0.2f),
    modifier = Modifier.padding(horizontal = 2.dp)
  ) {
    Text(
      text = badge.label,
      color = Color(badge.colorHex),
      fontWeight = FontWeight.Bold,
      fontSize = 9.sp,
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
    )
  }
}
