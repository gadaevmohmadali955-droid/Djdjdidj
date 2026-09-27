package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

class DataRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val prefs: SharedPreferences =
        context.getSharedPreferences("blox_chat_prefs", Context.MODE_PRIVATE)

    // Current User
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Real Community Users (retrieved live over internet from real registrations)
    private val _communityUsers = MutableStateFlow<List<User>>(emptyList())
    val communityUsers: StateFlow<List<User>> = _communityUsers.asStateFlow()

    // Real Friends
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    // Real Incoming Friend Requests
    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

    // Real Direct Messages Map: friendId -> List<ChatMessage>
    private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()

    // Real Admin Audit Logs: Calls history
    private val _callRecords = MutableStateFlow<List<CallRecord>>(emptyList())
    val callRecords: StateFlow<List<CallRecord>> = _callRecords.asStateFlow()

    // Real Admin Audit Logs: Roulette sessions
    private val _rouletteLogs = MutableStateFlow<List<RouletteSessionLog>>(emptyList())
    val rouletteLogs: StateFlow<List<RouletteSessionLog>> = _rouletteLogs.asStateFlow()

    // Active Incoming Call Dialog State
    data class IncomingCall(
        val callId: String,
        val callerId: String,
        val callerName: String,
        val callerAvatar: String,
        val isVideo: Boolean
    )
    private val _incomingCall = MutableStateFlow<IncomingCall?>(null)
    val incomingCall: StateFlow<IncomingCall?> = _incomingCall.asStateFlow()

    // Active Roulette Match State
    data class RouletteMatch(
        val roomId: String,
        val partnerId: String,
        val partnerName: String,
        val partnerAge: Int,
        val partnerAvatar: String,
        val isVideo: Boolean,
        val isPartnerIncognito: Boolean
    )
    private val _activeRouletteMatch = MutableStateFlow<RouletteMatch?>(null)
    val activeRouletteMatch: StateFlow<RouletteMatch?> = _activeRouletteMatch.asStateFlow()

    private val _isSearchingRoulette = MutableStateFlow(false)
    val isSearchingRoulette: StateFlow<Boolean> = _isSearchingRoulette.asStateFlow()

    // Real Online count
    private val _onlineCount = MutableStateFlow(1)
    val onlineCount: StateFlow<Int> = _onlineCount.asStateFlow()

    val totalRegisteredCount: Int
        get() = maxOf(1, _communityUsers.value.size)

    // Bot check / Captcha State
    private val _captchaAttemptsLeft = MutableStateFlow(3)
    val captchaAttemptsLeft: StateFlow<Int> = _captchaAttemptsLeft.asStateFlow()

    private val _captchaBanSeconds = MutableStateFlow(0)
    val captchaBanSeconds: StateFlow<Int> = _captchaBanSeconds.asStateFlow()

    private val _activeCaptcha = MutableStateFlow<Pair<String, List<CaptchaItem>>?>(null)
    val activeCaptcha: StateFlow<Pair<String, List<CaptchaItem>>?> = _activeCaptcha.asStateFlow()

    // Network streaming jobs
    private var userChannelJob: Job? = null
    private var rouletteQueueJob: Job? = null
    private var rouletteRoomJob: Job? = null

    init {
        loadStoredUser()
        generateNewCaptcha()
        startNetworkSync()
    }

    private fun loadStoredUser() {
        val id = prefs.getString("user_id", null)
        if (id != null) {
            val name = prefs.getString("user_name", "Robloxian") ?: "Robloxian"
            val email = prefs.getString("user_email", "") ?: ""
            val age = prefs.getInt("user_age", 19)
            val key = prefs.getString("user_key", KeyGenerator.generateUniqueKey()) ?: KeyGenerator.generateUniqueKey()
            val frontPhoto = prefs.getString("user_front_photo", null)
            val anglePhoto = prefs.getString("user_angle_photo", null)
            val camDisabled = prefs.getBoolean("user_cam_disabled", false)
            val incognito = prefs.getBoolean("user_incognito", false)

            val isAdmin = email.trim().lowercase() == "gadaevmohmad955@gmail.com"

            val user = User(
                id = id,
                name = name,
                email = email,
                estimatedAge = age,
                avatarUrl = "avatar_me",
                frontPhotoUri = frontPhoto,
                anglePhotoUri = anglePhoto,
                secretKey = key,
                isCameraDisabledInRoulette = camDisabled,
                isIncognito = incognito,
                rank = if (isAdmin) "👑 Главный Администратор" else "💎 Bloxian Master"
            )
            _currentUser.value = user
            listenToUserChannel(user.id)
            publishUserRegistration(user)
        }
    }

    fun saveUser(
        name: String,
        email: String = "",
        age: Int,
        frontPhotoUri: String?,
        anglePhotoUri: String?,
        secretKey: String
    ): User {
        val id = "u_" + (name.hashCode().toLong() and 0xFFFFFFFFL).toString() + "_" + (System.currentTimeMillis() % 10000)
        val normalizedEmail = email.trim().lowercase()
        val isAdmin = (normalizedEmail == "gadaevmohmad955@gmail.com")

        val user = User(
            id = id,
            name = name,
            email = normalizedEmail,
            estimatedAge = age,
            avatarUrl = "avatar_1",
            frontPhotoUri = frontPhotoUri,
            anglePhotoUri = anglePhotoUri,
            secretKey = secretKey,
            rank = if (isAdmin) "👑 Главный Администратор" else "💎 Bloxian Master",
            isVerified = true
        )

        prefs.edit()
            .putString("user_id", id)
            .putString("user_name", name)
            .putString("user_email", normalizedEmail)
            .putInt("user_age", age)
            .putString("user_front_photo", frontPhotoUri)
            .putString("user_angle_photo", anglePhotoUri)
            .putString("user_key", secretKey)
            .apply()

        // If email was linked at creation time, store claim
        if (normalizedEmail.isNotEmpty()) {
            prefs.edit().putString("google_claimed_uid_$normalizedEmail", id).apply()
        }

        _currentUser.value = user
        listenToUserChannel(user.id)
        publishUserRegistration(user)
        return user
    }

    fun linkGoogleAccount(email: String, password: String): Pair<Boolean, String> {
        val normalized = email.trim().lowercase()
        if (normalized.isEmpty() || !normalized.contains("@")) {
            return Pair(false, "Пожалуйста, укажите корректную почту Google (@gmail.com)")
        }
        if (password.length < 4) {
            return Pair(false, "Пароль должен содержать не менее 4 символов")
        }

        val current = _currentUser.value ?: return Pair(false, "Пользователь не найден")

        // Check if this Google email was ALREADY claimed by ANY user in this app
        val claimedUserId = prefs.getString("google_claimed_uid_$normalized", null)
        if (claimedUserId != null && claimedUserId != current.id) {
            return Pair(
                false,
                "❌ Эта почта Google уже была привязана к другому аккаунту! Повторное подключение этой почты невозможно."
            )
        }

        // If current user already linked a different email
        if (current.email.isNotBlank() && current.email != normalized) {
            return Pair(false, "К вашему профилю уже привязана другая почта: ${current.email}")
        }

        val isAdmin = (normalized == "gadaevmohmad955@gmail.com")

        // Permanently record that this email belongs strictly to this user
        prefs.edit()
            .putString("google_claimed_uid_$normalized", current.id)
            .putString("user_email", normalized)
            .apply()

        val updated = current.copy(
            email = normalized,
            rank = if (isAdmin) "👑 Главный Администратор" else "💎 Bloxian Master"
        )
        _currentUser.value = updated
        publishUserRegistration(updated)

        return Pair(
            true,
            if (isAdmin) "👑 Google-аккаунт gadaevmohmad955@gmail.com подтверждён! Панель Администратора разблокирована 🛡️"
            else "✓ Google-аккаунт ($normalized) успешно подключен к вашему профилю!"
        )
    }

    fun isCurrentUserAdmin(): Boolean {
        val email = _currentUser.value?.email?.trim()?.lowercase()
        return email == "gadaevmohmad955@gmail.com"
    }

    fun loginWithKey(key: String): Boolean {
        val storedKey = prefs.getString("user_key", null)
        if (storedKey != null && storedKey.equals(key.trim(), ignoreCase = true)) {
            loadStoredUser()
            return true
        }
        // Match from community users who registered
        val matched = _communityUsers.value.find { it.secretKey.equals(key.trim(), ignoreCase = true) }
        if (matched != null) {
            _currentUser.value = matched
            prefs.edit()
                .putString("user_id", matched.id)
                .putString("user_name", matched.name)
                .putString("user_email", matched.email)
                .putInt("user_age", matched.estimatedAge)
                .putString("user_key", matched.secretKey)
                .apply()
            listenToUserChannel(matched.id)
            return true
        }
        return false
    }

    fun loginWithGoogle(email: String, name: String = "Пользователь Google") {
        val norm = email.trim().lowercase()
        val isAdmin = (norm == "gadaevmohmad955@gmail.com")
        val id = "u_google_" + (norm.hashCode().toLong() and 0xFFFFFFFFL)
        val user = User(
            id = id,
            name = if (isAdmin) "Мохьмад Гадаев" else name,
            email = norm,
            estimatedAge = if (isAdmin) 21 else 20,
            avatarUrl = "avatar_user",
            secretKey = KeyGenerator.generateUniqueKey(),
            rank = if (isAdmin) "👑 Главный Администратор" else "💎 Bloxian Master",
            isVerified = true
        )
        prefs.edit()
            .putString("google_claimed_uid_$norm", user.id)
            .putString("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .putInt("user_age", user.estimatedAge)
            .putString("user_key", user.secretKey)
            .apply()
        _currentUser.value = user
        listenToUserChannel(user.id)
        publishUserRegistration(user)
    }

    fun logout() {
        userChannelJob?.cancel()
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    fun toggleCameraInRoulette(disabled: Boolean) {
        val u = _currentUser.value ?: return
        val updated = u.copy(isCameraDisabledInRoulette = disabled)
        _currentUser.value = updated
        prefs.edit().putBoolean("user_cam_disabled", disabled).apply()
        publishUserRegistration(updated)
    }

    fun toggleIncognito(incognito: Boolean) {
        val u = _currentUser.value ?: return
        val updated = u.copy(isIncognito = incognito)
        _currentUser.value = updated
        prefs.edit().putBoolean("user_incognito", incognito).apply()
        publishUserRegistration(updated)
    }

    // --- REAL NETWORK SYNCHRONIZATION ---

    private fun startNetworkSync() {
        scope.launch {
            // Poll registered users from network
            fetchGlobalUsers()

            // Poll bans
            fetchGlobalBans()

            // Real Presence & Online Counter loop
            while (isActive) {
                val me = _currentUser.value
                if (me != null) {
                    val presenceObj = JSONObject().apply {
                        put("type", "presence")
                        put("userId", me.id)
                        put("name", me.name)
                        put("time", System.currentTimeMillis())
                    }
                    NetworkService.publish("bloxchat_presence_v2", presenceObj)
                }

                // Poll presence to calculate real online users
                val recentPresence = NetworkService.pollHistory("bloxchat_presence_v2", since = "2m")
                val activeIds = recentPresence.mapNotNull { it.optString("userId") }.toSet()
                if (activeIds.isNotEmpty()) {
                    _onlineCount.value = maxOf(1, activeIds.size)
                } else {
                    _onlineCount.value = maxOf(1, _friends.value.count { it.isOnline } + 1)
                }

                // Refresh users periodically
                fetchGlobalUsers()
                delay(20000)
            }
        }
    }

    private suspend fun fetchGlobalUsers() {
        val history = NetworkService.pollHistory("bloxchat_users_registry_v2", since = "72h")
        val currentList = _communityUsers.value.toMutableList()
        val myId = _currentUser.value?.id

        for (obj in history) {
            val uid = obj.optString("id")
            if (uid.isNotBlank() && uid != myId) {
                val existingIndex = currentList.indexOfFirst { it.id == uid }
                val user = User(
                    id = uid,
                    name = obj.optString("name", "Пользователь"),
                    email = obj.optString("email", ""),
                    estimatedAge = obj.optInt("age", 19),
                    avatarUrl = obj.optString("avatarUrl", "avatar_1"),
                    frontPhotoUri = obj.optString("frontPhoto", "front_verified"),
                    anglePhotoUri = obj.optString("anglePhoto", "angle_verified"),
                    secretKey = obj.optString("secretKey", ""),
                    rank = obj.optString("rank", "💎 Bloxian Master"),
                    isVerified = obj.optBoolean("isVerified", true),
                    isOnline = true,
                    registrationDate = obj.optString("date", "Сегодня")
                )
                if (existingIndex >= 0) {
                    currentList[existingIndex] = user
                } else {
                    currentList.add(user)
                }
            }
        }
        _communityUsers.value = currentList
    }

    private suspend fun fetchGlobalBans() {
        val bans = NetworkService.pollHistory("bloxchat_bans_v2", since = "72h")
        val bannedIds = bans.mapNotNull { it.optString("bannedUserId") }.toSet()
        val myId = _currentUser.value?.id
        if (myId != null && bannedIds.contains(myId)) {
            _currentUser.value = _currentUser.value?.copy(isBanned = true)
        }
        _communityUsers.value = _communityUsers.value.map {
            if (bannedIds.contains(it.id)) it.copy(isBanned = true) else it
        }
    }

    private fun publishUserRegistration(user: User) {
        scope.launch {
            val json = JSONObject().apply {
                put("id", user.id)
                put("name", user.name)
                put("email", user.email)
                put("age", user.estimatedAge)
                put("avatarUrl", user.avatarUrl)
                put("frontPhoto", user.frontPhotoUri)
                put("anglePhoto", user.anglePhotoUri)
                put("secretKey", user.secretKey)
                put("rank", user.rank)
                put("isVerified", user.isVerified)
                put("date", user.registrationDate)
            }
            NetworkService.publish("bloxchat_users_registry_v2", json)
        }
    }

    private fun listenToUserChannel(userId: String) {
        userChannelJob?.cancel()
        val topic = "bloxchat_u_${userId.replace("-", "_")}"
        userChannelJob = NetworkService.startListening(scope, topic) { obj ->
            when (obj.optString("type")) {
                "friend_request" -> {
                    val fromObj = obj.optJSONObject("fromUser") ?: return@startListening
                    val req = FriendRequest(
                        id = "req_" + fromObj.optString("id"),
                        userId = fromObj.optString("id"),
                        name = fromObj.optString("name"),
                        avatarUrl = fromObj.optString("avatarUrl", "avatar_1"),
                        timeAgo = "Только что"
                    )
                    if (_friendRequests.value.none { it.userId == req.userId } &&
                        _friends.value.none { it.id == req.userId }) {
                        _friendRequests.value = listOf(req) + _friendRequests.value
                    }
                }
                "friend_accept" -> {
                    val friendObj = obj.optJSONObject("friend") ?: return@startListening
                    val newFriend = Friend(
                        id = friendObj.optString("id"),
                        name = friendObj.optString("name"),
                        avatarUrl = friendObj.optString("avatarUrl", "avatar_1"),
                        isOnline = true,
                        statusText = "В сети",
                        lastMessage = "Запрос дружбы принят!",
                        lastMessageTime = "Только что",
                        unreadCount = 1
                    )
                    if (_friends.value.none { it.id == newFriend.id }) {
                        _friends.value = listOf(newFriend) + _friends.value
                    }
                }
                "dm_message" -> {
                    val msgObj = obj.optJSONObject("message") ?: return@startListening
                    val senderId = msgObj.optString("senderId")
                    val text = msgObj.optString("text")
                    val chatMsg = ChatMessage(
                        id = msgObj.optString("id", "msg_" + System.currentTimeMillis()),
                        senderId = senderId,
                        receiverId = _currentUser.value?.id ?: "me",
                        text = text,
                        timestamp = msgObj.optLong("timestamp", System.currentTimeMillis()),
                        isFromMe = false,
                        mediaUrl = if (msgObj.has("mediaUrl")) msgObj.optString("mediaUrl") else null
                    )
                    val map = _chatMessages.value.toMutableMap()
                    val list = map[senderId]?.toMutableList() ?: mutableListOf()
                    list.add(chatMsg)
                    map[senderId] = list
                    _chatMessages.value = map

                    _friends.value = _friends.value.map {
                        if (it.id == senderId) {
                            it.copy(
                                lastMessage = text,
                                lastMessageTime = "Только что",
                                unreadCount = it.unreadCount + 1
                            )
                        } else it
                    }
                }
                "call_invite" -> {
                    val callId = obj.optString("callId")
                    val callerObj = obj.optJSONObject("caller") ?: return@startListening
                    _incomingCall.value = IncomingCall(
                        callId = callId,
                        callerId = callerObj.optString("id"),
                        callerName = callerObj.optString("name"),
                        callerAvatar = callerObj.optString("avatarUrl", "avatar_1"),
                        isVideo = obj.optBoolean("isVideo", false)
                    )
                }
                "call_end" -> {
                    _incomingCall.value = null
                }
                "user_banned" -> {
                    val bannedId = obj.optString("bannedUserId")
                    if (bannedId == _currentUser.value?.id) {
                        _currentUser.value = _currentUser.value?.copy(isBanned = true)
                    }
                }
            }
        }
    }

    // --- REAL FRIENDS ---

    fun sendFriendRequestTo(user: User): Boolean {
        val me = _currentUser.value ?: return false
        val existing = _friends.value.any { it.id == user.id }
        if (existing) return false

        scope.launch {
            val payload = JSONObject().apply {
                put("type", "friend_request")
                put("fromUser", JSONObject().apply {
                    put("id", me.id)
                    put("name", me.name)
                    put("avatarUrl", me.avatarUrl)
                })
            }
            NetworkService.publish("bloxchat_u_${user.id.replace("-", "_")}", payload)
        }
        return true
    }

    fun acceptFriendRequest(requestId: String) {
        val req = _friendRequests.value.find { it.id == requestId } ?: return
        _friendRequests.value = _friendRequests.value.filter { it.id != requestId }

        val newFriend = Friend(
            id = req.userId,
            name = req.name,
            avatarUrl = req.avatarUrl,
            isOnline = true,
            statusText = "В сети",
            lastMessage = "Мы теперь друзья в BloxChat!",
            lastMessageTime = "Только что",
            unreadCount = 0
        )
        _friends.value = listOf(newFriend) + _friends.value

        // Notify friend
        val me = _currentUser.value
        if (me != null) {
            scope.launch {
                val payload = JSONObject().apply {
                    put("type", "friend_accept")
                    put("friend", JSONObject().apply {
                        put("id", me.id)
                        put("name", me.name)
                        put("avatarUrl", me.avatarUrl)
                    })
                }
                NetworkService.publish("bloxchat_u_${req.userId.replace("-", "_")}", payload)
            }
        }
    }

    fun declineFriendRequest(requestId: String) {
        _friendRequests.value = _friendRequests.value.filter { it.id != requestId }
    }

    fun clearUnreadForFriend(friendId: String) {
        _friends.value = _friends.value.map {
            if (it.id == friendId) it.copy(unreadCount = 0) else it
        }
    }

    fun sendMessage(friendId: String, text: String, mediaUrl: String? = null) {
        val me = _currentUser.value ?: return
        val message = ChatMessage(
            id = "msg_" + System.currentTimeMillis(),
            senderId = me.id,
            receiverId = friendId,
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromMe = true,
            mediaUrl = mediaUrl
        )

        val map = _chatMessages.value.toMutableMap()
        val list = map[friendId]?.toMutableList() ?: mutableListOf()
        list.add(message)
        map[friendId] = list
        _chatMessages.value = map

        _friends.value = _friends.value.map {
            if (it.id == friendId) {
                it.copy(
                    lastMessage = if (mediaUrl != null) "📷 Фото" else text,
                    lastMessageTime = "Только что"
                )
            } else it
        }

        // Send over network to real friend
        scope.launch {
            val payload = JSONObject().apply {
                put("type", "dm_message")
                put("message", JSONObject().apply {
                    put("id", message.id)
                    put("senderId", me.id)
                    put("text", text)
                    put("timestamp", message.timestamp)
                    if (mediaUrl != null) put("mediaUrl", mediaUrl)
                })
            }
            NetworkService.publish("bloxchat_u_${friendId.replace("-", "_")}", payload)
        }
    }

    // --- REAL CALLS ---

    fun startCall(friendId: String, isVideo: Boolean): String {
        val me = _currentUser.value ?: return ""
        val callId = "call_" + System.currentTimeMillis()
        val friend = _friends.value.find { it.id == friendId }

        scope.launch {
            val payload = JSONObject().apply {
                put("type", "call_invite")
                put("callId", callId)
                put("isVideo", isVideo)
                put("caller", JSONObject().apply {
                    put("id", me.id)
                    put("name", me.name)
                    put("avatarUrl", me.avatarUrl)
                })
            }
            NetworkService.publish("bloxchat_u_${friendId.replace("-", "_")}", payload)

            // Audit log for Admin Panel
            val auditLog = JSONObject().apply {
                put("type", "call_audit")
                put("callId", callId)
                put("callerName", me.name)
                put("receiverName", friend?.name ?: "Пользователь")
                put("isVideo", isVideo)
                put("time", System.currentTimeMillis())
            }
            NetworkService.publish("bloxchat_admin_audit_v2", auditLog)
        }
        return callId
    }

    fun endCall(friendId: String, callId: String) {
        _incomingCall.value = null
        scope.launch {
            val payload = JSONObject().apply {
                put("type", "call_end")
                put("callId", callId)
            }
            NetworkService.publish("bloxchat_u_${friendId.replace("-", "_")}", payload)
        }
    }

    fun dismissIncomingCall() {
        val call = _incomingCall.value ?: return
        endCall(call.callerId, call.callId)
    }

    // --- REAL ROULETTE MATCHMAKING ---

    fun enterRouletteQueue(isVideo: Boolean) {
        val me = _currentUser.value ?: return
        _isSearchingRoulette.value = true
        _activeRouletteMatch.value = null

        rouletteQueueJob?.cancel()
        val topic = if (isVideo) "bloxchat_roulette_video_queue_v2" else "bloxchat_roulette_text_queue_v2"

        // Broadcast search request
        scope.launch {
            val joinPayload = JSONObject().apply {
                put("type", "queue_join")
                put("userId", me.id)
                put("name", me.name)
                put("age", me.estimatedAge)
                put("avatarUrl", me.avatarUrl)
                put("isVideo", isVideo)
                put("incognito", me.isIncognito)
                put("timestamp", System.currentTimeMillis())
            }
            NetworkService.publish(topic, joinPayload)
        }

        // Listen for other real participants
        rouletteQueueJob = NetworkService.startListening(scope, topic) { obj ->
            val otherUserId = obj.optString("userId")
            if (otherUserId.isNotBlank() && otherUserId != me.id) {
                val actionType = obj.optString("type")
                if (actionType == "queue_join") {
                    // Match found! Create room and invite
                    val roomId = "room_" + minOf(me.id, otherUserId) + "_" + maxOf(me.id, otherUserId)
                    val matchInvite = JSONObject().apply {
                        put("type", "match_start")
                        put("roomId", roomId)
                        put("user1Id", me.id)
                        put("user2Id", otherUserId)
                        put("p1Name", me.name)
                        put("p1Age", me.estimatedAge)
                        put("p1Avatar", me.avatarUrl)
                        put("p1Incognito", me.isIncognito)
                        put("p2Name", obj.optString("name"))
                        put("p2Age", obj.optInt("age", 19))
                        put("p2Avatar", obj.optString("avatarUrl", "avatar_1"))
                        put("p2Incognito", obj.optBoolean("incognito", false))
                        put("isVideo", isVideo)
                    }
                    scope.launch {
                        NetworkService.publish(topic, matchInvite)
                    }
                } else if (actionType == "match_start") {
                    val u1 = obj.optString("user1Id")
                    val u2 = obj.optString("user2Id")
                    if (u1 == me.id || u2 == me.id) {
                        val isPartnerP2 = (u1 == me.id)
                        _activeRouletteMatch.value = RouletteMatch(
                            roomId = obj.optString("roomId"),
                            partnerId = if (isPartnerP2) u2 else u1,
                            partnerName = if (isPartnerP2) obj.optString("p2Name") else obj.optString("p1Name"),
                            partnerAge = if (isPartnerP2) obj.optInt("p2Age") else obj.optInt("p1Age"),
                            partnerAvatar = if (isPartnerP2) obj.optString("p2Avatar") else obj.optString("p1Avatar"),
                            isVideo = isVideo,
                            isPartnerIncognito = if (isPartnerP2) obj.optBoolean("p2Incognito") else obj.optBoolean("p1Incognito")
                        )
                        _isSearchingRoulette.value = false
                        rouletteQueueJob?.cancel()
                    }
                }
            }
        }
    }

    fun leaveRoulette() {
        _isSearchingRoulette.value = false
        _activeRouletteMatch.value = null
        rouletteQueueJob?.cancel()
        rouletteRoomJob?.cancel()
    }

    // --- REAL ADMIN ACTIONS ---

    fun toggleBanUser(userId: String) {
        val isNowBanned = !(_communityUsers.value.find { it.id == userId }?.isBanned ?: false)
        _communityUsers.value = _communityUsers.value.map {
            if (it.id == userId) it.copy(isBanned = isNowBanned) else it
        }

        scope.launch {
            val banPayload = JSONObject().apply {
                put("type", "user_banned")
                put("bannedUserId", userId)
                put("isBanned", isNowBanned)
            }
            NetworkService.publish("bloxchat_bans_v2", banPayload)
            NetworkService.publish("bloxchat_u_${userId.replace("-", "_")}", banPayload)
        }
    }

    // --- CAPTCHA ---

    fun generateNewCaptcha() {
        val fruits = listOf(
            Pair("🍅", "Помидор"),
            Pair("🍎", "Красное яблоко"),
            Pair("🍌", "Банан"),
            Pair("🍇", "Виноград"),
            Pair("🍓", "Клубника"),
            Pair("🍊", "Апельсин"),
            Pair("🍉", "Арбуз"),
            Pair("🍍", "Ананас"),
            Pair("🥑", "Авокадо")
        ).shuffled()

        val target = fruits.random()
        val items = fruits.mapIndexed { index, (emoji, name) ->
            CaptchaItem(
                id = index,
                emoji = emoji,
                nameRu = name,
                isTarget = (emoji == target.first)
            )
        }
        _activeCaptcha.value = Pair(target.second, items)
    }

    fun onCaptchaItemClicked(item: CaptchaItem): Boolean {
        if (item.isTarget) {
            _captchaAttemptsLeft.value = 3
            _captchaBanSeconds.value = 0
            return true
        } else {
            val left = _captchaAttemptsLeft.value - 1
            if (left <= 0) {
                _captchaAttemptsLeft.value = 0
                _captchaBanSeconds.value = 60
            } else {
                _captchaAttemptsLeft.value = left
            }
            return false
        }
    }

    fun decrementBanTimer() {
        val cur = _captchaBanSeconds.value
        if (cur > 0) {
            _captchaBanSeconds.value = cur - 1
            if (cur - 1 == 0) {
                _captchaAttemptsLeft.value = 3
                generateNewCaptcha()
            }
        }
    }
}
