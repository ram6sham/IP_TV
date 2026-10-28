package com.example.model

enum class UserBadge(val label: String, val colorHex: Long) {
  VIEWER("Viewer", 0xFF94A3B8),
  VIP("VIP", 0xFFFFB800),
  MODERATOR("Mod", 0xFF10B981),
  BROADCASTER("Host", 0xFFFF2B56)
}

data class ChatMessage(
  val id: String,
  val streamId: String,
  val userName: String,
  val avatarColorHex: Long,
  val badge: UserBadge = UserBadge.VIEWER,
  val message: String,
  val timestamp: Long = System.currentTimeMillis(),
  val likesCount: Int = 0,
  val isPinned: Boolean = false,
  val isMyMessage: Boolean = false
)

data class FloatingReaction(
  val id: String,
  val emoji: String,
  val startXFraction: Float, // horizontal placement 0.2f - 0.8f
  val timestamp: Long = System.currentTimeMillis()
)
