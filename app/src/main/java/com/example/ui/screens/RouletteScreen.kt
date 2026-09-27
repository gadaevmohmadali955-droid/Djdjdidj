package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.DataRepository
import com.example.data.NetworkService
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
import com.example.ui.theme.BloxGreen
import com.example.ui.theme.BloxGreenBright
import com.example.ui.theme.BloxInputBg
import com.example.ui.theme.BloxPurple
import com.example.ui.theme.BloxRed
import com.example.ui.theme.BloxSurface
import com.example.ui.theme.BloxTextMuted
import com.example.ui.theme.BloxTextPrimary
import com.example.ui.theme.BloxTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun RouletteScreen(
    repository: DataRepository,
    initialIsVideo: Boolean = true,
    onExitRoulette: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState()
    val isSearching by repository.isSearchingRoulette.collectAsState()
    val activeMatch by repository.activeRouletteMatch.collectAsState()

    var isVideoMode by remember { mutableStateOf(initialIsVideo) }
    var sessionSeconds by remember { mutableIntStateOf(0) }
    var partnerPhotoToPreview by remember { mutableStateOf<String?>(null) }

    // System Back Press handling
    BackHandler {
        onExitRoulette()
    }

    // Microphone & Camera runtime permission launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val mic = perms[Manifest.permission.RECORD_AUDIO] ?: false
        val cam = perms[Manifest.permission.CAMERA] ?: false
        if (!mic) {
            Toast.makeText(context, "Внимание: микрофон необходим для голосового общения", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isVideoMode) {
        if (isVideoMode) {
            val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (!hasMic || !hasCam) {
                permissionsLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
            }
        } else {
            val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (!hasMic) {
                permissionsLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            }
        }
    }

    // Real Text roulette messages
    val rouletteChatMessages = remember { mutableStateListOf<Pair<Boolean, String>>() }
    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Safety: Auto-exit and leave roulette queue on screen dispose / tab change
    DisposableEffect(Unit) {
        repository.enterRouletteQueue(isVideoMode)
        onDispose {
            repository.leaveRoulette()
        }
    }

    // Connect to room chat channel when match is found
    LaunchedEffect(activeMatch?.roomId) {
        val room = activeMatch?.roomId
        if (room != null) {
            rouletteChatMessages.clear()
            NetworkService.startListening(scope, "bloxchat_room_${room}") { obj ->
                val senderId = obj.optString("senderId")
                val text = obj.optString("text")
                val isMe = (senderId == currentUser?.id)
                if (!isMe && text.isNotBlank()) {
                    rouletteChatMessages.add(Pair(false, text))
                }
            }
        }
    }

    // Timer effect
    LaunchedEffect(activeMatch) {
        sessionSeconds = 0
        if (activeMatch != null) {
            while (true) {
                delay(1000)
                sessionSeconds += 1
            }
        }
    }

    fun skipToNextPartner() {
        rouletteChatMessages.clear()
        sessionSeconds = 0
        repository.enterRouletteQueue(isVideoMode)
    }

    val minutes = sessionSeconds / 60
    val seconds = sessionSeconds % 60
    val timerFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BloxDarkBg)
    ) {
        // TOP BAR: Exit Back Button + Mode Selector + Live Timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BloxSurface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Exit button to return to Home
                IconButton(
                    onClick = onExitRoulette,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Выйти в главное меню",
                        tint = BloxCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Mode Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BloxCard)
                        .border(1.dp, BloxBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isVideoMode) BloxCyan else Color.Transparent)
                            .clickable {
                                if (!isVideoMode) {
                                    isVideoMode = true
                                    repository.enterRouletteQueue(true)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "📹 Видео",
                            color = if (isVideoMode) Color.White else BloxTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isVideoMode) BloxCyan else Color.Transparent)
                            .clickable {
                                if (isVideoMode) {
                                    isVideoMode = false
                                    repository.enterRouletteQueue(false)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "💬 Текст",
                            color = if (!isVideoMode) Color.White else BloxTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Session Duration Timer Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BloxCardElevated)
                    .border(1.dp, BloxCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (activeMatch != null) BloxGreenBright else BloxRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timerFormatted,
                    color = BloxTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // MAIN CONTENT AREA
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val match = activeMatch
            if (match == null) {
                // REAL MATCHMAKING QUEUE (NO FAKE BOTS!)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = BloxCyan,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Поиск реального собеседника в сети...",
                        color = BloxTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Система ожидает подключения второго реального человека в очередь рулетки (без фейковых ботов).\n\n💡 Чтобы протестировать соединение прямо сейчас, откройте ссылку на приложение во второй вкладке браузера или на втором устройстве!",
                        color = BloxTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    BloxSecondaryButton(
                        text = "🔄 Перезапустить поиск",
                        onClick = { repository.enterRouletteQueue(isVideoMode) },
                        modifier = Modifier.width(220.dp)
                    )
                }
            } else if (isVideoMode) {
                // REAL VIDEO ROULETTE VIEW WITH MATCHED REAL PERSON
                Box(modifier = Modifier.fillMaxSize()) {
                    // REMOTE INTERLOCUTOR FEED (Top area)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF14161A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            BloxAvatar(
                                name = if (match.isPartnerIncognito) "Аноним" else match.partnerName,
                                avatarKey = match.partnerAvatar,
                                size = 110.dp,
                                isOnline = true
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(BloxGreenBright)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Прямой видеопоток • В эфире",
                                    color = BloxGreenBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // INTERLOCUTOR INFO OVERLAY (Above their camera)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(1.dp, BloxBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        BloxAvatar(
                            name = if (match.isPartnerIncognito) "Аноним" else match.partnerName,
                            avatarKey = match.partnerAvatar,
                            size = 28.dp,
                            isOnline = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (match.isPartnerIncognito) "Анонимный пользователь" else match.partnerName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (!match.isPartnerIncognito) {
                                Text(
                                    text = "Возраст: ${match.partnerAge} лет",
                                    color = BloxCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // YOUR FRONT CAMERA PIP (Bottom Right Corner)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp)
                            .size(width = 110.dp, height = 150.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BloxCardElevated)
                            .border(2.dp, BloxCyan, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val camDisabled = currentUser?.isCameraDisabledInRoulette == true

                        if (camDisabled) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideocamOff,
                                    contentDescription = null,
                                    tint = BloxRed,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Камера выкл",
                                    color = BloxTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            CameraPreviewBox(
                                modifier = Modifier.fillMaxSize(),
                                isScanning = false
                            )
                        }

                        // YOUR INFO BADGE (Above your PiP camera, respecting incognito toggle)
                        currentUser?.let { me ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (me.isIncognito) "Инкогнито 🕶️" else me.name,
                                    color = if (me.isIncognito) BloxPurple else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // REAL TEXT ROULETTE VIEW
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Matched Partner Header
                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCardElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BloxAvatar(
                                name = if (match.isPartnerIncognito) "Аноним" else match.partnerName,
                                avatarKey = match.partnerAvatar,
                                size = 36.dp,
                                isOnline = true
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (match.isPartnerIncognito) "Анонимный собеседник" else match.partnerName,
                                    color = BloxTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Реальный пользователь онлайн",
                                    color = BloxGreenBright,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real-time messages
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(rouletteChatMessages) { (isMe, text) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isMe) BloxCyan else BloxCardElevated)
                                        .border(
                                            1.dp,
                                            if (isMe) BloxCyan else BloxBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        text = text,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageInput,
                            onValueChange = { messageInput = it },
                            placeholder = { Text("Напишите собеседнику...", color = BloxTextMuted) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BloxInputBg,
                                unfocusedContainerColor = BloxInputBg,
                                focusedBorderColor = BloxCyan,
                                unfocusedBorderColor = BloxBorder,
                                focusedTextColor = BloxTextPrimary,
                                unfocusedTextColor = BloxTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (messageInput.isNotBlank()) {
                                    val textToSend = messageInput
                                    rouletteChatMessages.add(Pair(true, textToSend))
                                    messageInput = ""

                                    scope.launch {
                                        val payload = JSONObject().apply {
                                            put("senderId", currentUser?.id ?: "me")
                                            put("text", textToSend)
                                        }
                                        NetworkService.publish("bloxchat_room_${match.roomId}", payload)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BloxCyan)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Отправить",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM ACTION CONTROLS ("Пропустить" / "Выйти")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BloxSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exit button
            BloxSecondaryButton(
                text = "Выйти ✕",
                onClick = {
                    repository.leaveRoulette()
                    onExitRoulette()
                },
                modifier = Modifier.weight(1f),
                testTag = "roulette_exit_btn"
            )

            // Skip to next partner button
            BloxButton(
                text = "Пропустить ⏭️",
                onClick = { skipToNextPartner() },
                modifier = Modifier.weight(1.3f),
                backgroundColor = BloxCyan,
                testTag = "roulette_skip_btn"
            )
        }

        partnerPhotoToPreview?.let { name ->
            FullScreenPhotoViewer(
                title = "Фото собеседника: $name",
                subtitle = "Участник чат-рулетки",
                avatarName = name,
                onDismiss = { partnerPhotoToPreview = null }
            )
        }
    }
}
