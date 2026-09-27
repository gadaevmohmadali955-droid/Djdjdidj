package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.ui.components.BloxButton
import com.example.ui.components.BloxCard
import com.example.ui.components.BloxSecondaryButton
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RouletteScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.BloxCard
import com.example.ui.theme.BloxCardElevated
import com.example.ui.theme.BloxCyan
import com.example.ui.theme.BloxDarkBg
import com.example.ui.theme.BloxGreen
import com.example.ui.theme.BloxGreenBright
import com.example.ui.theme.BloxRed
import com.example.ui.theme.BloxSurface
import com.example.ui.theme.BloxTextMuted
import com.example.ui.theme.BloxTextPrimary
import com.example.ui.theme.BloxTextSecondary
import com.example.ui.theme.MyApplicationTheme

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("Главная", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    SEARCH("Поиск", Icons.Filled.Search, Icons.Outlined.Search, "tab_search"),
    FRIENDS("Друзья", Icons.Filled.Group, Icons.Outlined.Group, "tab_friends"),
    PROFILE("Профиль", Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: DataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = DataRepository(applicationContext)

        setContent {
            MyApplicationTheme {
                BloxApp(repository = repository)
            }
        }
    }
}

@Composable
fun BloxApp(repository: DataRepository) {
    val currentUser by repository.currentUser.collectAsState()
    val friends by repository.friends.collectAsState()
    val incomingCall by repository.incomingCall.collectAsState()

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var selectedFriendForChat by remember { mutableStateOf<String?>(null) }
    var rouletteInitialVideo by remember { mutableStateOf(true) }
    var isShowingRoulette by remember { mutableStateOf(false) }
    var isShowingChat by remember { mutableStateOf(false) }
    var isShowingAdminScreen by remember { mutableStateOf(false) }

    // If user is not yet logged in or registered -> Onboarding Flow
    if (currentUser == null) {
        OnboardingScreen(
            repository = repository,
            onOnboardingComplete = {
                currentTab = MainTab.HOME
            }
        )
        return
    }

    // Check if user is banned
    val isBanned = currentUser?.isBanned == true
    if (isBanned) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BloxDarkBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            BloxCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BloxCard,
                borderColor = BloxRed
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🚫", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Аккаунт заблокирован",
                        color = BloxRed,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Администратор заблокировал ваш профиль за нарушение правил платформы и безопасности.",
                        color = BloxTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    BloxButton(
                        text = "Выйти из аккаунта",
                        onClick = { repository.logout() },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxRed
                    )
                }
            }
        }
        return
    }

    // Admin Screen takes precedence if opened
    if (isShowingAdminScreen) {
        BackHandler {
            isShowingAdminScreen = false
        }
        AdminScreen(
            repository = repository,
            onNavigateBack = {
                isShowingAdminScreen = false
            }
        )
        return
    }

    // Roulette Screen opens when clicking "Видео Чат" or "Текстовая Рулетка"
    if (isShowingRoulette) {
        BackHandler {
            isShowingRoulette = false
        }
        RouletteScreen(
            repository = repository,
            initialIsVideo = rouletteInitialVideo,
            onExitRoulette = {
                isShowingRoulette = false
            }
        )
        return
    }

    // Direct Chats Screen opens when selecting a friend or starting chat from Search
    if (isShowingChat) {
        BackHandler {
            isShowingChat = false
        }
        ChatsScreen(
            repository = repository,
            initialFriendId = selectedFriendForChat,
            onNavigateBackToLobby = {
                isShowingChat = false
            }
        )
        return
    }

    // Back press returns to Home tab if on other tabs
    BackHandler(enabled = (currentTab != MainTab.HOME)) {
        currentTab = MainTab.HOME
    }

    // Calculate unread badge count for friends tab
    val totalUnread = friends.sumOf { it.unreadCount }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BloxDarkBg,
        bottomBar = {
            NavigationBar(
                containerColor = BloxSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("blox_bottom_bar")
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = (currentTab == tab)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentTab = tab
                        },
                        icon = {
                            if (tab == MainTab.FRIENDS && totalUnread > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = BloxRed,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = "$totalUnread", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BloxCyan,
                            selectedTextColor = BloxCyan,
                            unselectedIconColor = BloxTextMuted,
                            unselectedTextColor = BloxTextMuted,
                            indicatorColor = BloxCardElevated
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.HOME -> {
                    HomeScreen(
                        repository = repository,
                        onNavigateToRoulette = { isVideo ->
                            rouletteInitialVideo = isVideo
                            isShowingRoulette = true
                        },
                        onNavigateToFriends = {
                            currentTab = MainTab.FRIENDS
                        }
                    )
                }

                MainTab.SEARCH -> {
                    SearchScreen(
                        repository = repository,
                        onOpenChatWithUser = { user ->
                            selectedFriendForChat = user.id
                            isShowingChat = true
                        }
                    )
                }

                MainTab.FRIENDS -> {
                    FriendsScreen(
                        repository = repository,
                        onOpenFriendChat = { friendId ->
                            selectedFriendForChat = friendId
                            isShowingChat = true
                        }
                    )
                }

                MainTab.PROFILE -> {
                    ProfileScreen(
                        repository = repository,
                        onNavigateToAdmin = {
                            isShowingAdminScreen = true
                        },
                        onLogout = {
                            currentTab = MainTab.HOME
                        }
                    )
                }
            }

            // Real-time Incoming Call Alert Banner
            incomingCall?.let { call ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BloxCardElevated)
                        .border(2.dp, BloxGreenBright, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (call.isVideo) "📹 Входящий видеозвонок" else "📞 Входящий звонок",
                                color = BloxGreenBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = call.callerName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BloxButton(
                                text = "Принять",
                                onClick = {
                                    selectedFriendForChat = call.callerId
                                    isShowingChat = true
                                },
                                backgroundColor = BloxGreen,
                                modifier = Modifier.height(38.dp)
                            )
                            BloxSecondaryButton(
                                text = "Сброс",
                                onClick = { repository.dismissIncomingCall() },
                                modifier = Modifier.height(38.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
