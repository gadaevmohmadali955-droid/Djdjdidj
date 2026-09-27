package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.data.Friend
import com.example.ui.components.BloxAvatar
import com.example.ui.components.BloxCard
import com.example.ui.components.FullScreenPhotoViewer
import com.example.ui.theme.BloxBorder
import com.example.ui.theme.BloxCard
import com.example.ui.theme.BloxCardElevated
import com.example.ui.theme.BloxCyan
import com.example.ui.theme.BloxDarkBg
import com.example.ui.theme.BloxGreenBright
import com.example.ui.theme.BloxRed
import com.example.ui.theme.BloxTextMuted
import com.example.ui.theme.BloxTextPrimary
import com.example.ui.theme.BloxTextSecondary

@Composable
fun FriendsScreen(
    repository: DataRepository,
    onOpenFriendChat: (friendId: String) -> Unit
) {
    val friends by repository.friends.collectAsState()
    var selectedFriendForPhoto by remember { mutableStateOf<Friend?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BloxDarkBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Мои друзья (${friends.size})",
                color = BloxTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            val totalUnread = friends.sumOf { it.unreadCount }
            if (totalUnread > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BloxRed)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$totalUnread новых",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (friends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = BloxTextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "У вас пока нет друзей",
                        color = BloxTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ищите людей по имени на Главной странице или знакомьтесь в чат-рулетке!",
                        color = BloxTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(friends) { friend ->
                    BloxCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                repository.clearUnreadForFriend(friend.id)
                                onOpenFriendChat(friend.id)
                            }
                            .testTag("friend_item_${friend.id}"),
                        backgroundColor = BloxCard,
                        borderColor = if (friend.unreadCount > 0) BloxRed.copy(alpha = 0.6f) else BloxBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.clickable {
                                    selectedFriendForPhoto = friend
                                }
                            ) {
                                BloxAvatar(
                                    name = friend.name,
                                    avatarKey = friend.avatarUrl,
                                    size = 48.dp,
                                    isOnline = friend.isOnline
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = friend.name,
                                        color = BloxTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (friend.isOnline) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(BloxGreenBright)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = friend.lastMessage,
                                    color = if (friend.unreadCount > 0) BloxTextPrimary else BloxTextMuted,
                                    fontWeight = if (friend.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = friend.statusText,
                                    color = if (friend.isOnline) BloxGreenBright else BloxTextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            // Notification Red Pill Badge
                            if (friend.unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(BloxRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${friend.unreadCount}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            // Quick Chat Action
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BloxCardElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Чат",
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

    selectedFriendForPhoto?.let { friend ->
        FullScreenPhotoViewer(
            title = "Профиль друга: ${friend.name}",
            subtitle = friend.statusText,
            avatarName = friend.name,
            photoPath = null,
            onDismiss = { selectedFriendForPhoto = null }
        )
    }
}
