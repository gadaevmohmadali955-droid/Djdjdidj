package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DataRepository
import com.example.data.PhotoStorage
import com.example.ui.components.BadgeType
import com.example.ui.components.BloxAvatar
import com.example.ui.components.BloxBadge
import com.example.ui.components.BloxButton
import com.example.ui.components.BloxCard
import com.example.ui.components.BloxSecondaryButton
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
import com.example.ui.theme.BloxPurple
import com.example.ui.theme.BloxRed
import com.example.ui.theme.BloxTextMuted
import com.example.ui.theme.BloxTextPrimary
import com.example.ui.theme.BloxTextSecondary

@Composable
fun ProfileScreen(
    repository: DataRepository,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()

    var isKeyVisible by remember { mutableStateOf(false) }

    // Google Linking Form
    var googleEmailInput by remember { mutableStateOf("") }
    var googlePasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var linkingErrorMessage by remember { mutableStateOf<String?>(null) }
    var linkingSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Photo Preview Modal State
    var previewPhotoTitle by remember { mutableStateOf("Фотография") }
    var previewPhotoSubtitle by remember { mutableStateOf<String?>(null) }
    var previewPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewPhotoPath by remember { mutableStateOf<String?>(null) }
    var isShowingPhotoViewer by remember { mutableStateOf(false) }

    val user = currentUser ?: return
    val isAdmin = user.email.trim().equals("gadaevmohmad955@gmail.com", ignoreCase = true)
    val hasGoogleLinked = user.email.isNotBlank() && user.email.contains("@")

    // Load local face photos
    val frontBitmap = remember(user.frontPhotoUri) {
        PhotoStorage.loadBitmapFromFile(user.frontPhotoUri) ?: PhotoStorage.createBiometricFaceBitmap(
            user.name,
            user.estimatedAge,
            "ПРЯМОЙ РАКУРС"
        )
    }
    val angleBitmap = remember(user.anglePhotoUri) {
        PhotoStorage.loadBitmapFromFile(user.anglePhotoUri) ?: PhotoStorage.createBiometricFaceBitmap(
            user.name,
            user.estimatedAge,
            "БОКОВОЙ РАКУРС"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BloxDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // PROFILE HEADER CARD
        BloxCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = BloxCard
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with Verified badge (Clickable to view full photo)
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable {
                        previewPhotoTitle = "Аватар профиля: ${user.name}"
                        previewPhotoSubtitle = "${user.rank} • ID: ${user.id.take(10)}"
                        previewPhotoBitmap = null
                        previewPhotoPath = user.frontPhotoUri
                        isShowingPhotoViewer = true
                    }
                ) {
                    BloxAvatar(
                        name = user.name,
                        avatarKey = user.avatarUrl,
                        photoPath = user.frontPhotoUri,
                        size = 84.dp,
                        isOnline = true
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(BloxCyan)
                            .border(2.dp, BloxCard, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Верифицирован",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.name,
                    color = BloxTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (hasGoogleLinked) user.email else "Google-аккаунт не привязан",
                    color = if (hasGoogleLinked) BloxCyan else BloxTextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BloxBadge(text = "${user.estimatedAge} лет", badgeType = BadgeType.VERIFIED)
                    BloxBadge(text = user.rank, badgeType = if (isAdmin) BadgeType.ADMIN else BadgeType.VIP)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Нажмите на аватар или снимок для полноразмерного просмотра 🔍",
                    color = BloxTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VERIFIED BIOMETRIC PHOTOS SECTION (CLICKABLE IN FULLSCREEN)
        BloxCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = BloxCard
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = BloxCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Верифицированные фотографии лица",
                        color = BloxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Снимки биометрии, сделанные во время подтверждения возраста. Нажмите на снимок для детального просмотра.",
                    color = BloxTextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Straight Face Photo Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BloxCardElevated)
                            .border(1.5.dp, BloxCyan, RoundedCornerShape(12.dp))
                            .clickable {
                                previewPhotoTitle = "Прямой ракурс лица"
                                previewPhotoSubtitle = "Биометрическая верификация • ${user.estimatedAge} лет"
                                previewPhotoBitmap = frontBitmap
                                previewPhotoPath = user.frontPhotoUri
                                isShowingPhotoViewer = true
                            }
                    ) {
                        Image(
                            bitmap = frontBitmap.asImageBitmap(),
                            contentDescription = "Прямой ракурс",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Прямой ракурс 🔍",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Angle Face Photo Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BloxCardElevated)
                            .border(1.5.dp, BloxGreenBright, RoundedCornerShape(12.dp))
                            .clickable {
                                previewPhotoTitle = "Боковой ракурс лица"
                                previewPhotoSubtitle = "Биометрическая верификация • ${user.estimatedAge} лет"
                                previewPhotoBitmap = angleBitmap
                                previewPhotoPath = user.anglePhotoUri
                                isShowingPhotoViewer = true
                            }
                    ) {
                        Image(
                            bitmap = angleBitmap.asImageBitmap(),
                            contentDescription = "Боковой ракурс",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Боковой ракурс 🔍",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GOOGLE ACCOUNT LINKING CARD
        BloxCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = BloxCard
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mail,
                        contentDescription = null,
                        tint = BloxGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Привязка Google-аккаунта",
                        color = BloxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (hasGoogleLinked) {
                    // Already connected
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BloxGreen.copy(alpha = 0.15f))
                            .border(1.dp, BloxGreenBright, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BloxGreenBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Google-аккаунт успешно привязан:",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = user.email,
                                    color = BloxGreenBright,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Повторная привязка к другому пользователю заблокирована.",
                                    color = BloxTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                } else {
                    // Form to connect Google
                    Text(
                        text = "Введите почту и пароль для привязки. Если это аккаунт gadaevmohmad955@gmail.com, откроется панель администратора.",
                        color = BloxTextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = googleEmailInput,
                        onValueChange = {
                            googleEmailInput = it
                            linkingErrorMessage = null
                            linkingSuccessMessage = null
                        },
                        placeholder = { Text("gadaevmohmad955@gmail.com", color = BloxTextMuted) },
                        label = { Text("Почта Google (@gmail.com)", color = BloxCyan) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = googlePasswordInput,
                        onValueChange = {
                            googlePasswordInput = it
                            linkingErrorMessage = null
                            linkingSuccessMessage = null
                        },
                        placeholder = { Text("Пароль", color = BloxTextMuted) },
                        label = { Text("Пароль от Google", color = BloxCyan) },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Показать пароль",
                                    tint = BloxCyan
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
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

                    if (linkingErrorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = linkingErrorMessage ?: "",
                            color = BloxRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (linkingSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = linkingSuccessMessage ?: "",
                            color = BloxGreenBright,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    BloxButton(
                        text = "Подключить этот Google-аккаунт",
                        onClick = {
                            val (success, message) = repository.linkGoogleAccount(
                                email = googleEmailInput,
                                password = googlePasswordInput
                            )
                            if (success) {
                                linkingSuccessMessage = message
                                linkingErrorMessage = null
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            } else {
                                linkingErrorMessage = message
                                linkingSuccessMessage = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCyan
                    )
                }
            }
        }

        // ADMIN PANEL ACCESS CARD (STRICTLY AND ONLY FOR GADAEVMOHMAD955@GMAIL.COM)
        if (isAdmin) {
            Spacer(modifier = Modifier.height(16.dp))

            BloxCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BloxPurple.copy(alpha = 0.15f),
                borderColor = BloxPurple
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = BloxPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Панель Администратора",
                                color = BloxTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "👑 Доступ разрешён: gadaevmohmad955@gmail.com",
                                color = BloxPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    BloxButton(
                        text = "Открыть Панель Администратора 🛡️",
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxPurple,
                        testTag = "open_admin_panel_btn"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PRIVACY SETTINGS TOGGLES
        BloxCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = BloxCard
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Настройки конфиденциальности",
                    color = BloxTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle 1: Disable camera in roulette
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Выключить камеру в чат-рулетке",
                            color = BloxTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Общаться только голосом без видеопотока",
                            color = BloxTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = user.isCameraDisabledInRoulette,
                        onCheckedChange = { repository.toggleCameraInRoulette(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BloxCyan,
                            uncheckedThumbColor = BloxTextMuted,
                            uncheckedTrackColor = BloxCardElevated
                        ),
                        modifier = Modifier.testTag("switch_cam_disabled")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle 2: Incognito mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Режим Инкогнито",
                            color = BloxTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Скрыть профиль, имя и аватарку в чат-рулетке",
                            color = BloxTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = user.isIncognito,
                        onCheckedChange = { repository.toggleIncognito(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BloxCyan,
                            uncheckedThumbColor = BloxTextMuted,
                            uncheckedTrackColor = BloxCardElevated
                        ),
                        modifier = Modifier.testTag("switch_incognito")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECRET KEY CARD (Masked with Eye toggle & Copy button)
        BloxCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = BloxCard
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = BloxCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Секретный ключ восстановления",
                        color = BloxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Используйте этот уникальный ключ для входа в аккаунт на любых устройствах.",
                    color = BloxTextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Key container with Eye button & Copy button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BloxInputBg)
                        .border(1.dp, BloxBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isKeyVisible) user.secretKey else "••••-••••-••••-••••",
                            color = if (isKeyVisible) BloxCyan else BloxTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 1.2.sp
                        )

                        Row {
                            // Toggle visibility eye button
                            IconButton(
                                onClick = { isKeyVisible = !isKeyVisible },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Показать ключ",
                                    tint = BloxCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Copy button
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("BloxChat Key", user.secretKey))
                                    Toast.makeText(context, "Ключ скопирован в буфер обмена!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Скопировать ключ",
                                    tint = BloxTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LOGOUT & SWITCH ACCOUNT
        BloxSecondaryButton(
            text = "Выйти из аккаунта",
            onClick = {
                repository.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            testTag = "logout_button"
        )
    }

    // Fullscreen Photo Viewer Dialog
    if (isShowingPhotoViewer) {
        FullScreenPhotoViewer(
            title = previewPhotoTitle,
            subtitle = previewPhotoSubtitle,
            bitmap = previewPhotoBitmap,
            photoPath = previewPhotoPath,
            avatarName = user.name,
            onDismiss = { isShowingPhotoViewer = false }
        )
    }
}
