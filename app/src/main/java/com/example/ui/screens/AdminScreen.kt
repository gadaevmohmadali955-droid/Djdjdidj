package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallRecord
import com.example.data.DataRepository
import com.example.data.RouletteSessionLog
import com.example.data.User
import com.example.ui.components.BloxAvatar
import com.example.ui.components.BloxButton
import com.example.ui.components.BloxCard
import com.example.ui.theme.BloxBorder
import com.example.ui.theme.BloxCard
import com.example.ui.theme.BloxCardElevated
import com.example.ui.theme.BloxCyan
import com.example.ui.theme.BloxDarkBg
import com.example.ui.theme.BloxGold
import com.example.ui.theme.BloxGreen
import com.example.ui.theme.BloxGreenBright
import com.example.ui.theme.BloxPurple
import com.example.ui.theme.BloxRed
import com.example.ui.theme.BloxSurface
import com.example.ui.theme.BloxTextMuted
import com.example.ui.theme.BloxTextPrimary
import com.example.ui.theme.BloxTextSecondary

enum class AdminTab(val title: String) {
    ROULETTE("Чат-Рулетки"),
    CALLS("Звонки и Видео"),
    PHOTOS("Фото Проверки"),
    USERS("Пользователи")
}

@Composable
fun AdminScreen(
    repository: DataRepository,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AdminTab.ROULETTE) }
    val communityUsers by repository.communityUsers.collectAsState()
    val callRecords by repository.callRecords.collectAsState()
    val rouletteLogs by repository.rouletteLogs.collectAsState()

    var selectedDayFilter by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BloxDarkBg)
    ) {
        // ADMIN HEADER BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BloxSurface)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = BloxTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Панель Администратора",
                    color = BloxTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Администратор: gadaevmohmad955@gmail.com",
                    color = BloxPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // STEALTH MODE BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BloxPurple.copy(alpha = 0.25f))
                .border(1.dp, BloxPurple.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = BloxPurple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Скрытый режим: Вы не видны пользователям во время наблюдения",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // TABS
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = BloxSurface,
            contentColor = BloxCyan,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = BloxCyan
                )
            }
        ) {
            AdminTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // DAY FILTER BAR
        if (selectedTab == AdminTab.ROULETTE || selectedTab == AdminTab.CALLS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair(-1, "Все"),
                    Pair(0, "🔴 Прямой эфир"),
                    Pair(3, "🕒 3 дня назад"),
                    Pair(10, "📅 10 дней назад")
                ).forEach { (day, label) ->
                    val isSelected = (selectedDayFilter == day)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BloxCyan else BloxCard)
                            .border(1.dp, if (isSelected) BloxCyan else BloxBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedDayFilter = day }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else BloxTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // TAB CONTENTS
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            when (selectedTab) {
                AdminTab.ROULETTE -> AdminRouletteView(rouletteLogs, selectedDayFilter)
                AdminTab.CALLS -> AdminCallsView(callRecords, selectedDayFilter)
                AdminTab.PHOTOS -> AdminPhotosView(communityUsers)
                AdminTab.USERS -> AdminUsersView(communityUsers, repository)
            }
        }
    }
}

@Composable
fun AdminRouletteView(logs: List<RouletteSessionLog>, dayFilter: Int) {
    val filtered = logs.filter { if (dayFilter == -1) true else it.timestampDaysAgo == dayFilter }
    if (filtered.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🛰️", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Нет активных сессий рулетки", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Все реальные пользователи, подключившиеся к рулетке, появляются здесь в скрытом режиме наблюдения.",
                    color = BloxTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { log ->
                BloxCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BloxCard,
                    borderColor = if (log.isLiveNow) BloxGreenBright.copy(alpha = 0.6f) else BloxBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (log.isVideo) Icons.Default.Videocam else Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = if (log.isVideo) BloxCyan else BloxGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (log.isVideo) "Видео-Рулетка" else "Текст-Рулетка",
                                    color = BloxTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = log.timestampFormatted,
                                color = if (log.isLiveNow) BloxGreenBright else BloxTextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                BloxAvatar(name = log.partner1Name, avatarKey = log.partner1Avatar, size = 36.dp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(text = log.partner1Name, color = BloxTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(text = "⚡↔️⚡", fontSize = 14.sp)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                BloxAvatar(name = log.partner2Name, avatarKey = log.partner2Avatar, size = 36.dp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(text = log.partner2Name, color = BloxTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BloxCardElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "📜 Логи сессии и сообщения:", color = BloxCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                log.chatTranscript.forEach { line ->
                                    Text(text = "• $line", color = BloxTextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCallsView(records: List<CallRecord>, dayFilter: Int) {
    val filtered = records.filter { if (dayFilter == -1) true else it.timestampDaysAgo == dayFilter }
    if (filtered.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📞", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Журнал звонков пуст", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Аудио- и видеозвонки между реальными пользователями платформы фиксируются здесь.",
                    color = BloxTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { call ->
                BloxCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BloxCard,
                    borderColor = if (call.isLiveNow) BloxGreenBright else BloxBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                                    contentDescription = null,
                                    tint = if (call.isVideo) BloxCyan else BloxGreenBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (call.isVideo) "Видеозвонок" else "Аудиозвонок",
                                    color = BloxTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = call.timestampFormatted,
                                color = if (call.isLiveNow) BloxGreenBright else BloxTextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${call.callerName}  📞 ➡️  ${call.receiverName}  (${call.durationFormatted})",
                            color = BloxTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BloxCardElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "🎙️ Расшифровка звонка:", color = BloxGreenBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                call.transcript.forEach { line ->
                                    Text(text = line, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPhotosView(users: List<User>) {
    if (users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "📸", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Ожидание регистраций в сети", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Все реальные пользователи, прошедшие проверку лица, появятся здесь со своими фото двух ракурсов.",
                    color = BloxTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(users) { user ->
                BloxCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BloxCard
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BloxAvatar(name = user.name, avatarKey = user.avatarUrl, size = 42.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = user.name, color = BloxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "Возраст: ${user.estimatedAge} лет • Регистрация: ${user.registrationDate}", color = BloxCyan, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Снимки камеры при регистрации (для безопасности):", color = BloxTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BloxCardElevated)
                                    .border(1.dp, BloxGreenBright, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = BloxCyan, modifier = Modifier.size(28.dp))
                                    Text(text = "Ракурс 1 (Прямой)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(84.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BloxCardElevated)
                                    .border(1.dp, BloxCyan, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = BloxGreenBright, modifier = Modifier.size(28.dp))
                                    Text(text = "Ракурс 2 (Поворот)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUsersView(users: List<User>, repository: DataRepository) {
    val context = LocalContext.current
    if (users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "👥", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Ожидание пользователей в сети", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Все реальные пользователи будут отображаться здесь с возможностью мгновенной блокировки и разблокировки.",
                    color = BloxTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(users) { user ->
                BloxCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BloxCard,
                    borderColor = if (user.isBanned) BloxRed else BloxBorder
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BloxAvatar(name = user.name, avatarKey = user.avatarUrl, size = 44.dp)
                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = user.name, color = BloxTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Возраст: ${user.estimatedAge} • ${user.email}", color = BloxTextMuted, fontSize = 11.sp)
                            if (user.isBanned) {
                                Text(text = "🚫 ЗАБЛОКИРОВАН", color = BloxRed, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                            }
                        }

                        BloxButton(
                            text = if (user.isBanned) "Разблокировать" else "Заблокировать",
                            onClick = {
                                repository.toggleBanUser(user.id)
                                val action = if (user.isBanned) "разблокирован" else "заблокирован"
                                Toast.makeText(context, "${user.name} $action", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.height(40.dp),
                            backgroundColor = if (user.isBanned) BloxGreen else BloxRed
                        )
                    }
                }
            }
        }
    }
}
