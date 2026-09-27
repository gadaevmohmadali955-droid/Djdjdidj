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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.data.User
import com.example.ui.components.BadgeType
import com.example.ui.components.BloxAvatar
import com.example.ui.components.BloxBadge
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
import com.example.ui.theme.BloxInputBg
import com.example.ui.theme.BloxPurple
import com.example.ui.theme.BloxRed
import com.example.ui.theme.BloxTextMuted
import com.example.ui.theme.BloxTextPrimary
import com.example.ui.theme.BloxTextSecondary

@Composable
fun HomeScreen(
    repository: DataRepository,
    onNavigateToRoulette: (isVideo: Boolean) -> Unit,
    onNavigateToFriends: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val friendRequests by repository.friendRequests.collectAsState()
    val communityUsers by repository.communityUsers.collectAsState()
    val onlineCount by repository.onlineCount.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSearchResult by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BloxDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Platform Online Counters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BloxCard(
                modifier = Modifier.weight(1f),
                backgroundColor = BloxCard
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(BloxGreenBright)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "$onlineCount",
                            color = BloxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Сейчас онлайн",
                            color = BloxTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            BloxCard(
                modifier = Modifier.weight(1f),
                backgroundColor = BloxCard
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = BloxCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${repository.totalRegisteredCount}",
                            color = BloxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Зарегистрировано",
                            color = BloxTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current User Profile Hero Card with Rank & Verified Age
        currentUser?.let { user ->
            BloxCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BloxCardElevated,
                borderColor = BloxCyan.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BloxAvatar(
                        name = user.name,
                        avatarKey = user.avatarUrl,
                        size = 56.dp,
                        isOnline = true
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.name,
                                color = BloxTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BloxBadge(text = "${user.estimatedAge} лет", badgeType = BadgeType.VERIFIED)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = user.rank,
                            color = BloxGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (user.isIncognito) "Режим: Инкогнито 🕶️" else "Профиль: Публичный",
                            color = if (user.isIncognito) BloxPurple else BloxTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // INCOMING FRIEND REQUESTS (Roblox rounded cards with Accept / Decline)
        if (friendRequests.isNotEmpty()) {
            Text(
                text = "Заявки в друзья (${friendRequests.size})",
                color = BloxTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            friendRequests.forEach { req ->
                BloxCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    backgroundColor = BloxCard,
                    borderColor = BloxBorder
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BloxAvatar(
                            name = req.name,
                            avatarKey = req.avatarUrl,
                            size = 46.dp,
                            isOnline = true
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = req.name,
                                color = BloxTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = req.timeAgo,
                                color = BloxTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Accept Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BloxGreen)
                                .clickable {
                                    repository.acceptFriendRequest(req.id)
                                    Toast.makeText(context, "${req.name} добавлен в друзья!", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("accept_friend_${req.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Принять",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Decline Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BloxCardElevated)
                                .border(1.dp, BloxBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    repository.declineFriendRequest(req.id)
                                }
                                .testTag("decline_friend_${req.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Отклонить",
                                tint = BloxTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // QUICK LAUNCH ROULETTE MODES
        Text(
            text = "Режимы Чат-Рулетки",
            color = BloxTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Video Roulette Card
            BloxCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToRoulette(true) }
                    .testTag("launch_video_roulette"),
                backgroundColor = BloxCard,
                borderColor = BloxCyan.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BloxCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = BloxCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Видео Рулетка",
                        color = BloxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "С камерой и микрофоном",
                        color = BloxTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Text Roulette Card
            BloxCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToRoulette(false) }
                    .testTag("launch_text_roulette"),
                backgroundColor = BloxCard,
                borderColor = BloxGreen.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BloxGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💬", fontSize = 26.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Текст Рулетка",
                        color = BloxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Анонимный чат мгновенно",
                        color = BloxTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SEARCH USER BY NAME & ADD TO FRIENDS
        Text(
            text = "Найти человека по имени",
            color = BloxTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                selectedSearchResult = if (query.isNotBlank()) {
                    communityUsers.find { it.name.contains(query, ignoreCase = true) }
                } else null
            },
            placeholder = { Text("Введите имя человека...", color = BloxTextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = BloxCyan
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("friend_search_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BloxInputBg,
                unfocusedContainerColor = BloxInputBg,
                focusedBorderColor = BloxCyan,
                unfocusedBorderColor = BloxBorder,
                focusedTextColor = BloxTextPrimary,
                unfocusedTextColor = BloxTextPrimary
            )
        )

        selectedSearchResult?.let { foundUser ->
            Spacer(modifier = Modifier.height(12.dp))
            BloxCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BloxCardElevated,
                borderColor = BloxCyan
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BloxAvatar(
                        name = foundUser.name,
                        avatarKey = foundUser.avatarUrl,
                        size = 46.dp,
                        isOnline = foundUser.isOnline
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = foundUser.name,
                            color = BloxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Возраст: ${foundUser.estimatedAge} лет • ${foundUser.rank}",
                            color = BloxTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    BloxButton(
                        text = "+ В друзья",
                        onClick = {
                            val added = repository.sendFriendRequestTo(foundUser)
                            if (added) {
                                Toast.makeText(context, "${foundUser.name} добавлен в друзья!", Toast.LENGTH_SHORT).show()
                                searchQuery = ""
                                selectedSearchResult = null
                                onNavigateToFriends()
                            } else {
                                Toast.makeText(context, "Этот пользователь уже у вас в друзьях", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.height(42.dp),
                        backgroundColor = BloxCyan,
                        testTag = "add_friend_btn"
                    )
                }
            }
        }
    }
}
