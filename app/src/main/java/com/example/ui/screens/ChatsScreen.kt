package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ChatMessage
import com.example.data.DataRepository
import com.example.data.Friend
import com.example.data.PhotoStorage
import com.example.ui.components.BloxAvatar
import com.example.ui.components.BloxButton
import com.example.ui.components.BloxCard
import com.example.ui.components.BloxSecondaryButton
import com.example.ui.components.CameraPreviewBox
import com.example.ui.components.FullScreenPhotoViewer
import com.example.ui.theme.BloxBorder
import com.example.ui.theme.BloxCard
import com.example.ui.theme.BloxCardElevated
import com.example.ui.theme.BloxCyan
import com.example.ui.theme.BloxDarkBg
import com.example.ui.theme.BloxGold
import com.example.ui.theme.BloxGreen
import com.example.ui.theme.BloxGreenBright
import com.example.ui.theme.BloxInputBg
import com.example.ui.theme.BloxRed
import com.example.ui.theme.BloxSurface
import com.example.ui.theme.BloxTextMuted
import com.example.ui.theme.BloxTextPrimary
import com.example.ui.theme.BloxTextSecondary
import kotlinx.coroutines.delay

enum class CallState {
    IDLE,
    OUTGOING_RINGING,
    ACTIVE_VOICE,
    ACTIVE_VIDEO
}

@Composable
fun ChatsScreen(
    repository: DataRepository,
    initialFriendId: String? = null,
    onNavigateBackToLobby: () -> Unit = {}
) {
    val context = LocalContext.current
    val friends by repository.friends.collectAsState()
    val allMessages by repository.chatMessages.collectAsState()

    var activeFriendId by remember { mutableStateOf(initialFriendId ?: friends.firstOrNull()?.id) }
    val activeFriend = friends.find { it.id == activeFriendId }

    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Call states
    var callState by remember { mutableStateOf(CallState.IDLE) }
    var callSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }

    // Fullscreen Photo Viewer state
    var selectedPhotoTitle by remember { mutableStateOf("Фотография") }
    var selectedPhotoSubtitle by remember { mutableStateOf<String?>(null) }
    var selectedPhotoPath by remember { mutableStateOf<String?>(null) }
    var selectedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isViewingPhoto by remember { mutableStateOf(false) }

    // Back handling to return to previous screen
    BackHandler {
        if (callState != CallState.IDLE) {
            callState = CallState.IDLE
            callSeconds = 0
        } else {
            onNavigateBackToLobby()
        }
    }

    // Microphone & Camera Permission Launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val mic = perms[Manifest.permission.RECORD_AUDIO] ?: false
        val cam = perms[Manifest.permission.CAMERA] ?: false
        if (!mic) {
            Toast.makeText(context, "Для звонка требуется разрешение на микрофон", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && activeFriendId != null) {
            val bm = PhotoStorage.createBiometricFaceBitmap(
                name = "Фотоснимок",
                age = 19,
                angleLabel = "ФОТО ИЗ ГАЛЕРЕИ"
            )
            val path = PhotoStorage.saveBitmapToFile(context, bm, "chat_photo_${System.currentTimeMillis()}")
            repository.sendMessage(activeFriendId!!, "📷 Фотография", mediaUrl = path)
            Toast.makeText(context, "Фото отправлено!", Toast.LENGTH_SHORT).show()
        }
    }

    // Clear unread badge when entering chat
    LaunchedEffect(activeFriendId) {
        activeFriendId?.let { repository.clearUnreadForFriend(it) }
    }

    // Call timer
    LaunchedEffect(callState) {
        if (callState == CallState.ACTIVE_VOICE || callState == CallState.ACTIVE_VIDEO) {
            while (true) {
                delay(1000)
                callSeconds += 1
            }
        }
    }

    // Outgoing ringing auto-answer simulation for real communication response
    LaunchedEffect(callState) {
        if (callState == CallState.OUTGOING_RINGING) {
            delay(2800)
            callState = if (isCameraOff) CallState.ACTIVE_VOICE else CallState.ACTIVE_VIDEO
        }
    }

    // Auto scroll on new message
    val currentMessages = (activeFriendId?.let { allMessages[it] } ?: emptyList())
    LaunchedEffect(currentMessages.size) {
        if (currentMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentMessages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BloxDarkBg)
    ) {
        // ACTIVE CALL OVERLAY (FULL SCREEN)
        if (callState != CallState.IDLE) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Call Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            callState = CallState.IDLE
                            callSeconds = 0
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Свернуть",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (callState == CallState.OUTGOING_RINGING) "Вызов..." else "Идёт разговор",
                            color = BloxCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (callState != CallState.OUTGOING_RINGING) {
                            val min = callSeconds / 60
                            val sec = callSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", min, sec),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(modifier = Modifier.size(40.dp))
                }

                // Middle: Remote User Video Feed or Large Avatar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BloxCard)
                        .border(1.5.dp, BloxCyan, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (callState == CallState.ACTIVE_VIDEO && !isCameraOff) {
                        // Two-way video call view with local camera preview PiP
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Remote caller camera simulation canvas
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                BloxAvatar(
                                    name = activeFriend?.name ?: "Собеседник",
                                    avatarKey = activeFriend?.avatarUrl ?: "avatar_1",
                                    size = 110.dp,
                                    isOnline = true
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = activeFriend?.name ?: "Собеседник",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "HD Видеопоток активен 📹",
                                    color = BloxGreenBright,
                                    fontSize = 12.sp
                                )
                            }

                            // My Camera Picture-in-Picture
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .size(width = 110.dp, height = 150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black)
                                    .border(2.dp, BloxCyan, RoundedCornerShape(12.dp))
                            ) {
                                CameraPreviewBox(
                                    modifier = Modifier.fillMaxSize(),
                                    isScanning = false
                                )
                            }
                        }
                    } else {
                        // Audio Call View
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            BloxAvatar(
                                name = activeFriend?.name ?: "Собеседник",
                                avatarKey = activeFriend?.avatarUrl ?: "avatar_1",
                                size = 110.dp,
                                isOnline = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = activeFriend?.name ?: "Собеседник",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (callState == CallState.OUTGOING_RINGING) "Гудки..." else "Голосовая связь активна 🎙️",
                                color = if (callState == CallState.OUTGOING_RINGING) BloxGold else BloxGreenBright,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Call Controls Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Button
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) BloxRed else BloxCard)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Микрофон",
                            tint = Color.White
                        )
                    }

                    // Camera Toggle Button
                    IconButton(
                        onClick = {
                            isCameraOff = !isCameraOff
                            if (callState == CallState.ACTIVE_VOICE && !isCameraOff) {
                                callState = CallState.ACTIVE_VIDEO
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isCameraOff) BloxCard else BloxCyan)
                    ) {
                        Icon(
                            imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Камера",
                            tint = Color.White
                        )
                    }

                    // End Call Button
                    IconButton(
                        onClick = {
                            callState = CallState.IDLE
                            callSeconds = 0
                            Toast.makeText(context, "Звонок завершён", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BloxRed)
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Завершить звонок",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            return
        }

        // CHAT MAIN INTERFACE
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BloxSurface)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onNavigateBackToLobby,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = BloxCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                activeFriend?.let { friend ->
                    Box(
                        modifier = Modifier.clickable {
                            selectedPhotoTitle = "Аватар: ${friend.name}"
                            selectedPhotoSubtitle = friend.statusText
                            selectedPhotoBitmap = null
                            selectedPhotoPath = null
                            isViewingPhoto = true
                        }
                    ) {
                        BloxAvatar(
                            name = friend.name,
                            avatarKey = friend.avatarUrl,
                            size = 40.dp,
                            isOnline = friend.isOnline
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = friend.name,
                            color = BloxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = friend.statusText,
                            color = if (friend.isOnline) BloxGreenBright else BloxTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    // VOICE CALL BUTTON 📞
                    IconButton(
                        onClick = {
                            val hasMic = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!hasMic) {
                                permissionsLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                            }
                            isCameraOff = true
                            callState = CallState.OUTGOING_RINGING
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BloxCardElevated)
                            .testTag("voice_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Голосовой звонок",
                            tint = BloxGreenBright,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // VIDEO CALL BUTTON 📹
                    IconButton(
                        onClick = {
                            val hasMic = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            val hasCam = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!hasMic || !hasCam) {
                                permissionsLauncher.launch(
                                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                )
                            }
                            isCameraOff = false
                            callState = CallState.OUTGOING_RINGING
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BloxCyan.copy(alpha = 0.2f))
                            .border(1.dp, BloxCyan, RoundedCornerShape(8.dp))
                            .testTag("video_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Видеозвонок",
                            tint = BloxCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } ?: run {
                    Text(
                        text = "Диалог",
                        color = BloxTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(currentMessages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isFromMe) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            horizontalAlignment = if (msg.isFromMe) Alignment.End else Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (msg.isFromMe) 14.dp else 2.dp,
                                            bottomEnd = if (msg.isFromMe) 2.dp else 14.dp
                                        )
                                    )
                                    .background(if (msg.isFromMe) BloxCyan else BloxCardElevated)
                                    .border(
                                        1.dp,
                                        if (msg.isFromMe) BloxCyan else BloxBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    if (msg.mediaUrl != null) {
                                        val mediaBitmap = remember(msg.mediaUrl) {
                                            PhotoStorage.loadBitmapFromFile(msg.mediaUrl) ?: PhotoStorage.createBiometricFaceBitmap(
                                                "Фотосообщение",
                                                19,
                                                "СНИМОК BLOX"
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(width = 180.dp, height = 130.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF2C2F33))
                                                .clickable {
                                                    selectedPhotoTitle = "Фото от ${if (msg.isFromMe) "вас" else (activeFriend?.name ?: "собеседника")}"
                                                    selectedPhotoSubtitle = msg.timeText
                                                    selectedPhotoBitmap = mediaBitmap
                                                    selectedPhotoPath = msg.mediaUrl
                                                    isViewingPhoto = true
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                bitmap = mediaBitmap.asImageBitmap(),
                                                contentDescription = "Фотосообщение",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.6f))
                                                    .padding(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Photo,
                                                    contentDescription = "Увеличить",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    Text(
                                        text = msg.text,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )

                                    Text(
                                        text = msg.timeText,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BloxSurface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach Photo button
                IconButton(
                    onClick = {
                        activeFriendId?.let { id ->
                            val bm = PhotoStorage.createBiometricFaceBitmap(
                                name = "Снимок",
                                age = 19,
                                angleLabel = "ФОТО СООБЩЕНИЯ"
                            )
                            val path = PhotoStorage.saveBitmapToFile(context, bm, "sent_photo_${System.currentTimeMillis()}")
                            repository.sendMessage(id, "📷 Отправлено фото", mediaUrl = path)
                            Toast.makeText(context, "Фото прикреплено и отправлено!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Отправить фото",
                        tint = BloxCyan
                    )
                }

                // Attach from Gallery button
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch("image/*")
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Выбрать из галереи",
                        tint = BloxGreenBright
                    )
                }

                // Text Field
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = {
                        Text("Написать сообщение...", color = BloxTextMuted, fontSize = 14.sp)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BloxInputBg,
                        unfocusedContainerColor = BloxInputBg,
                        focusedBorderColor = BloxCyan,
                        unfocusedBorderColor = BloxBorder,
                        focusedTextColor = BloxTextPrimary,
                        unfocusedTextColor = BloxTextPrimary
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            activeFriendId?.let { id ->
                                repository.sendMessage(id, messageInput.trim())
                                messageInput = ""
                            }
                        }
                    },
                    enabled = messageInput.isNotBlank(),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (messageInput.isNotBlank()) BloxCyan else BloxCardElevated)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Отправить",
                        tint = if (messageInput.isNotBlank()) Color.White else BloxTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Fullscreen Photo Viewer Dialog for Chat
        if (isViewingPhoto) {
            FullScreenPhotoViewer(
                title = selectedPhotoTitle,
                subtitle = selectedPhotoSubtitle,
                bitmap = selectedPhotoBitmap,
                photoPath = selectedPhotoPath,
                avatarName = activeFriend?.name,
                onDismiss = { isViewingPhoto = false }
            )
        }
    }
}
