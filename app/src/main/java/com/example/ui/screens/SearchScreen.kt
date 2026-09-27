package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.components.FullScreenPhotoViewer
import com.example.ui.theme.BloxBorder
import com.example.ui.theme.BloxCard
import com.example.ui.theme.BloxCardElevated
import com.example.ui.theme.BloxCyan
import com.example.ui.theme.BloxDarkBg
import com.example.ui.theme.BloxInputBg
import com.example.ui.theme.BloxTextMuted
import com.example.ui.theme.BloxTextPrimary
import com.example.ui.theme.BloxTextSecondary

@Composable
fun SearchScreen(
    repository: DataRepository,
    onOpenChatWithUser: (user: User) -> Unit
) {
    val context = LocalContext.current
    val communityUsers by repository.communityUsers.collectAsState()
    val friends by repository.friends.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForPhotoPreview by remember { mutableStateOf<User?>(null) }

    val filteredUsers = remember(searchQuery, communityUsers) {
        if (searchQuery.isBlank()) {
            communityUsers
        } else {
            communityUsers.filter { user ->
                user.name.contains(searchQuery, ignoreCase = true) ||
                user.id.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BloxDarkBg)
            .padding(16.dp)
    ) {
        Text(
            text = "Поиск людей",
            color = BloxTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Найдите реальных пользователей по настоящему имени или юзернейму",
            color = BloxTextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Введите имя или юзернейм...", color = BloxTextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Поиск",
                    tint = BloxCyan
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_people_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BloxInputBg,
                unfocusedContainerColor = BloxInputBg,
                focusedBorderColor = BloxCyan,
                unfocusedBorderColor = BloxBorder,
                focusedTextColor = BloxTextPrimary,
                unfocusedTextColor = BloxTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔍", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "Ожидание пользователей в сети" else "Пользователь не найден",
                        color = BloxTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "Все зарегистрированные в сети люди появятся здесь" else "Проверьте правильность написания имени",
                        color = BloxTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers) { user ->
                    val isAlreadyFriend = friends.any { it.id == user.id }

                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCard
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.clickable {
                                    selectedUserForPhotoPreview = user
                                }
                            ) {
                                BloxAvatar(
                                    name = user.name,
                                    avatarKey = user.avatarUrl,
                                    size = 48.dp,
                                    isOnline = user.isOnline
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.name,
                                        color = BloxTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    BloxBadge(text = "${user.estimatedAge} лет", badgeType = BadgeType.VERIFIED)
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = "ID: ${user.id.take(12)} • ${user.rank}",
                                    color = BloxTextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!isAlreadyFriend) {
                                    BloxButton(
                                        text = "+ Друг",
                                        onClick = {
                                            val sent = repository.sendFriendRequestTo(user)
                                            if (sent) {
                                                Toast.makeText(context, "Запрос отправлен ${user.name}!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Запрос уже был отправлен", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.height(38.dp),
                                        backgroundColor = BloxCyan
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(BloxCardElevated, RoundedCornerShape(8.dp))
                                        .clickable { onOpenChatWithUser(user) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "Написать",
                                        tint = BloxCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedUserForPhotoPreview?.let { user ->
        FullScreenPhotoViewer(
            title = "Профиль: ${user.name}",
            subtitle = "${user.estimatedAge} лет • ID: ${user.id.take(10)}",
            avatarName = user.name,
            photoPath = user.frontPhotoUri,
            onDismiss = { selectedUserForPhotoPreview = null }
        )
    }
}
