package com.example.data

data class User(
    val id: String,
    val name: String,
    val email: String,
    val estimatedAge: Int,
    val avatarUrl: String,
    val frontPhotoUri: String? = null,
    val anglePhotoUri: String? = null,
    val secretKey: String,
    val rank: String = "💎 Bloxian Master",
    val isVerified: Boolean = true,
    val isCameraDisabledInRoulette: Boolean = false,
    val isIncognito: Boolean = false,
    val isBanned: Boolean = false,
    val isOnline: Boolean = true,
    val registrationDate: String = "26.09.2026"
)

data class Friend(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isOnline: Boolean,
    val statusText: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0
)

data class FriendRequest(
    val id: String,
    val userId: String,
    val name: String,
    val avatarUrl: String,
    val timeAgo: String
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean,
    val mediaUrl: String? = null
) {
    val timeText: String
        get() {
            val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
}

data class CallRecord(
    val id: String,
    val callerName: String,
    val callerAvatar: String,
    val receiverName: String,
    val receiverAvatar: String,
    val isVideo: Boolean,
    val timestampDaysAgo: Int, // 0 = live/today, 3 = 3 days ago, 10 = 10 days ago
    val timestampFormatted: String,
    val durationFormatted: String,
    val durationSeconds: Int,
    val transcript: List<String>,
    val isLiveNow: Boolean = false
)

data class RouletteSessionLog(
    val id: String,
    val partner1Name: String,
    val partner1Avatar: String,
    val partner2Name: String,
    val partner2Avatar: String,
    val isVideo: Boolean,
    val timestampDaysAgo: Int, // 0 = live now, 3 = 3 days ago, 10 = 10 days ago
    val timestampFormatted: String,
    val durationFormatted: String,
    val isLiveNow: Boolean,
    val chatTranscript: List<String>
)

data class CaptchaItem(
    val id: Int,
    val emoji: String,
    val nameRu: String,
    val isTarget: Boolean
)
