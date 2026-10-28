package com.example.data

import com.example.model.ChatMessage
import com.example.model.FloatingReaction
import com.example.model.UserBadge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class ChatRepository(private val scope: CoroutineScope) {

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  private val _reactions = MutableSharedFlow<FloatingReaction>(extraBufferCapacity = 20)
  val reactions: SharedFlow<FloatingReaction> = _reactions.asSharedFlow()

  private var activeStreamId: String = ""

  private val sampleNames = listOf(
    "Aayush Shrestha", "Rohan Thapa", "Pooja Gurung", "Sunil Magar",
    "Bikash Sharma", "Alisha Rai", "Karan KC", "Prashant Giri",
    "Samikshya Gautam", "Manish Tamang", "Suman Karki", "Anjali Adhikari"
  )

  private val sampleComments = listOf(
    "जय नेपाल 🇳🇵 Live stream ekdam ramro cha!",
    "Audio quality is crystal clear! 🔥",
    "Watching live from Kathmandu! Namaste sabailai 🙏",
    "Stream link works super fast without buffering!",
    "Great coverage PP TV team! Keep it up 👏",
    "Love from Pokhara 🏔️",
    "Who else is watching this live right now? 🙋‍♂️",
    "Video resolution 1080p looks amazing! 🚀",
    "Best streaming app so far! Super smooth ❤️",
    "Notifications work instantly too, awesome feature!"
  )

  private val avatarColors = listOf(
    0xFFEF4444, 0xFF3B82F6, 0xFF10B981, 0xFFF59E0B,
    0xFF8B5CF6, 0xFFEC4899, 0xFF06B6D4, 0xFF14B8A6
  )

  init {
    startLiveChatSimulation()
  }

  fun switchStream(streamId: String, streamTitle: String, broadcasterName: String) {
    activeStreamId = streamId
    // Initial welcome & pinned message
    val pinned = ChatMessage(
      id = "pinned_" + UUID.randomUUID().toString(),
      streamId = streamId,
      userName = broadcasterName,
      avatarColorHex = 0xFFFF2B56,
      badge = UserBadge.BROADCASTER,
      message = "📢 Welcome to \"$streamTitle\"! Enjoy the live stream & share your thoughts in chat. ❤️",
      timestamp = System.currentTimeMillis() - 60000,
      isPinned = true
    )

    val initialList = mutableListOf(pinned)
    // Add 4-5 prior chatter messages
    for (i in 0 until 5) {
      val name = sampleNames.random()
      val comment = sampleComments.random()
      val color = avatarColors.random()
      val badge = if (i == 1) UserBadge.VIP else if (i == 3) UserBadge.MODERATOR else UserBadge.VIEWER
      initialList.add(
        ChatMessage(
          id = "msg_${System.currentTimeMillis()}_$i",
          streamId = streamId,
          userName = name,
          avatarColorHex = color,
          badge = badge,
          message = comment,
          timestamp = System.currentTimeMillis() - (5 - i) * 15000,
          likesCount = Random.nextInt(0, 8)
        )
      )
    }
    _messages.value = initialList
  }

  fun sendMessage(text: String, userName: String = "You") {
    if (text.isBlank()) return
    val msg = ChatMessage(
      id = "my_msg_" + UUID.randomUUID().toString(),
      streamId = activeStreamId,
      userName = userName,
      avatarColorHex = 0xFF38BDF8,
      badge = UserBadge.VIEWER,
      message = text.trim(),
      timestamp = System.currentTimeMillis(),
      isMyMessage = true
    )
    _messages.value = _messages.value + msg

    // Trigger reaction if emoji
    if (text.contains("❤️") || text.contains("🔥") || text.contains("👏") || text.contains("🇳🇵")) {
      val emoji = if (text.contains("❤️")) "❤️" else if (text.contains("🔥")) "🔥" else "👏"
      triggerReaction(emoji)
    }
  }

  fun triggerReaction(emoji: String) {
    val reaction = FloatingReaction(
      id = UUID.randomUUID().toString(),
      emoji = emoji,
      startXFraction = Random.nextFloat() * 0.6f + 0.2f
    )
    _reactions.tryEmit(reaction)
  }

  fun likeMessage(messageId: String) {
    _messages.value = _messages.value.map {
      if (it.id == messageId) it.copy(likesCount = it.likesCount + 1) else it
    }
  }

  private fun startLiveChatSimulation() {
    scope.launch(Dispatchers.Default) {
      while (isActive) {
        delay(Random.nextLong(4000, 9000))
        if (activeStreamId.isNotBlank()) {
          val newMsg = ChatMessage(
            id = "sim_msg_" + UUID.randomUUID().toString(),
            streamId = activeStreamId,
            userName = sampleNames.random(),
            avatarColorHex = avatarColors.random(),
            badge = if (Random.nextInt(10) > 7) UserBadge.VIP else UserBadge.VIEWER,
            message = sampleComments.random(),
            timestamp = System.currentTimeMillis()
          )
          _messages.value = (_messages.value + newMsg).takeLast(60)

          // Random ambient heart reaction
          if (Random.nextBoolean()) {
            val emojis = listOf("❤️", "🔥", "👏", "🇳🇵", "🎉")
            triggerReaction(emojis.random())
          }
        }
      }
    }
  }
}
