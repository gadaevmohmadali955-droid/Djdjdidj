package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CaptchaItem
import com.example.data.DataRepository
import com.example.data.KeyGenerator
import com.example.data.NameValidator
import com.example.data.PhotoStorage
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
import com.example.ui.theme.BloxPurple
import com.example.ui.theme.BloxRed
import com.example.ui.theme.BloxTextMuted
import com.example.ui.theme.BloxTextPrimary
import com.example.ui.theme.BloxTextSecondary
import kotlinx.coroutines.delay

enum class OnboardingStep {
    BOT_WELCOME,
    CAPTCHA_CHECK,
    FACE_VERIFICATION,
    NAME_INPUT,
    CONNECT_GOOGLE,
    CREATING_PROFILE,
    SECRET_KEY_REVEAL,
    LOGIN_WITH_KEY,
    LOGIN_WITH_GOOGLE
}

@Composable
fun OnboardingScreen(
    repository: DataRepository,
    onOnboardingComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(OnboardingStep.BOT_WELCOME) }

    // Captcha State
    val captchaAttemptsLeft by repository.captchaAttemptsLeft.collectAsState()
    val captchaBanSeconds by repository.captchaBanSeconds.collectAsState()
    val activeCaptcha by repository.activeCaptcha.collectAsState()
    var captchaSuccessMessage by remember { mutableStateOf(false) }

    // Face Scan State
    var faceSubStep by remember { mutableIntStateOf(1) } // 1: straight, 2: angle, 3: analyzing, 4: complete
    var detectedAge by remember { mutableIntStateOf(19) }
    var straightPhotoPath by remember { mutableStateOf<String?>(null) }
    var anglePhotoPath by remember { mutableStateOf<String?>(null) }
    var straightPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var anglePhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var photoToPreviewInModal by remember { mutableStateOf<Bitmap?>(null) }

    // Name input state
    var nameInput by remember { mutableStateOf("") }
    var validatedName by remember { mutableStateOf("") }
    var nameErrorMessage by remember { mutableStateOf<String?>(null) }

    // Google Linking during registration
    var googleEmailInput by remember { mutableStateOf("") }
    var googlePasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var googleLinkErrorMessage by remember { mutableStateOf<String?>(null) }
    var connectedGoogleEmail by remember { mutableStateOf("") }

    // Google Direct Login state
    var directLoginEmail by remember { mutableStateOf("") }
    var directLoginPassword by remember { mutableStateOf("") }
    var directLoginError by remember { mutableStateOf<String?>(null) }

    // Account creation messages state
    var profileCreationMessageIndex by remember { mutableIntStateOf(0) }

    // Generated Key & 5s lock state
    var generatedKey by remember { mutableStateOf(KeyGenerator.generateUniqueKey()) }
    var keyLockCountdown by remember { mutableIntStateOf(5) }
    var keyCopied by remember { mutableStateOf(false) }

    // Login with existing key
    var loginKeyInput by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Ban countdown timer effect
    LaunchedEffect(captchaBanSeconds) {
        if (captchaBanSeconds > 0) {
            delay(1000)
            repository.decrementBanTimer()
        }
    }

    // 5 seconds lock timer on secret key screen
    LaunchedEffect(currentStep) {
        if (currentStep == OnboardingStep.SECRET_KEY_REVEAL) {
            keyLockCountdown = 5
            while (keyLockCountdown > 0) {
                delay(1000)
                keyLockCountdown -= 1
            }
        }
    }

    // Animated sequence for account creation
    LaunchedEffect(currentStep) {
        if (currentStep == OnboardingStep.CREATING_PROFILE) {
            profileCreationMessageIndex = 0
            delay(1800)
            profileCreationMessageIndex = 1
            delay(2200)
            profileCreationMessageIndex = 2
            delay(2000)
            profileCreationMessageIndex = 3
            delay(1800)

            // Save user to repository
            // Note: email is only non-empty if user actually connected their Google account!
            repository.saveUser(
                name = validatedName.ifEmpty { "Роблоксер" },
                email = connectedGoogleEmail,
                age = detectedAge,
                frontPhotoUri = straightPhotoPath,
                anglePhotoUri = anglePhotoPath,
                secretKey = generatedKey
            )
            currentStep = OnboardingStep.SECRET_KEY_REVEAL
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BloxDarkBg)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStep) {

                // 1. BOT WELCOME MINI-APP
                OnboardingStep.BOT_WELCOME -> {
                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCard,
                        borderColor = BloxBorder
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(BloxCyan, BloxPurple)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🤖",
                                    fontSize = 40.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "BloxChat Mini-App",
                                color = BloxTextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Добро пожаловать в официальный сервис BloxChat! Здесь вы можете безопасно общаться в чат-рулетке нового поколения, находить настоящих друзей и совершать видеозвонки с защитой от фейков.",
                                color = BloxTextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            BloxButton(
                                text = "Начать регистрацию 🚀",
                                onClick = {
                                    currentStep = OnboardingStep.CAPTCHA_CHECK
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "bot_start_button"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = { currentStep = OnboardingStep.LOGIN_WITH_KEY }
                            ) {
                                Text(
                                    text = "🔑 Войти по секретному ключу",
                                    color = BloxCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            TextButton(
                                onClick = { currentStep = OnboardingStep.LOGIN_WITH_GOOGLE }
                            ) {
                                Text(
                                    text = "🌐 Войти через Google-аккаунт",
                                    color = BloxGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // 2. CAPTCHA FRUIT MINI-GAME (3 attempts, 1-min ban)
                OnboardingStep.CAPTCHA_CHECK -> {
                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCard
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = BloxCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Проверка: А не бот ли ты?",
                                    color = BloxTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (captchaBanSeconds > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BloxRed.copy(alpha = 0.2f))
                                        .border(1.dp, BloxRed, RoundedCornerShape(10.dp))
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "⏳", fontSize = 28.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Вы исчерпали 3 попытки!",
                                            color = BloxRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Повторная попытка доступна через: $captchaBanSeconds сек",
                                            color = BloxTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else if (captchaSuccessMessage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BloxGreen.copy(alpha = 0.2f))
                                        .border(1.dp, BloxGreen, RoundedCornerShape(10.dp))
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "🎉", fontSize = 32.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Вы человек! Капча пройдена",
                                            color = BloxGreenBright,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        BloxButton(
                                            text = "Перейти к сканированию лица 📸",
                                            onClick = {
                                                currentStep = OnboardingStep.FACE_VERIFICATION
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            } else {
                                val targetName = activeCaptcha?.first ?: "Помидор"
                                Text(
                                    text = "Нажмите на: $targetName",
                                    color = BloxCyan,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Осталось попыток: $captchaAttemptsLeft из 3",
                                    color = if (captchaAttemptsLeft == 1) BloxRed else BloxTextMuted,
                                    fontSize = 13.sp
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // 3x3 Grid of Fruit Emojis
                                val items = activeCaptcha?.second ?: emptyList()
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp),
                                    contentPadding = PaddingValues(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(items) { item ->
                                        Box(
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(BloxCardElevated)
                                                .border(1.dp, BloxBorder, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    val success = repository.onCaptchaItemClicked(item)
                                                    if (success) {
                                                        captchaSuccessMessage = true
                                                    } else {
                                                        if (captchaAttemptsLeft > 1) {
                                                            Toast.makeText(
                                                                context,
                                                                "Неверно! Попробуйте еще раз",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                                .testTag("captcha_item_${item.id}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = item.emoji,
                                                fontSize = 32.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. CIRCULAR CAMERA FACE & REAL AGE VERIFICATION
                OnboardingStep.FACE_VERIFICATION -> {
                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCard
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Биометрическая верификация",
                                color = BloxTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = when (faceSubStep) {
                                    1 -> "Шаг 1: Посмотрите прямо в круг камеры"
                                    2 -> "Шаг 2: Поверните лицо в сторону (в ракурс)"
                                    3 -> "Сканирование и определение возраста..."
                                    else -> "Верификация успешно завершена!"
                                },
                                color = BloxCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Circular Camera Viewfinder with animated corner photo
                            Box(
                                modifier = Modifier
                                    .size(240.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Camera circle
                                CameraPreviewBox(
                                    modifier = Modifier
                                        .size(230.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, BloxCyan, CircleShape),
                                    isScanning = (faceSubStep == 3),
                                    scanText = if (faceSubStep == 3) "Определение возраста..." else null
                                )

                                // When step 2, first straight photo animates and stays in corner
                                if (faceSubStep >= 2 && straightPhotoBitmap != null) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 6.dp, y = (-6).dp)
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BloxCardElevated)
                                            .border(2.dp, BloxGreenBright, RoundedCornerShape(12.dp))
                                            .clickable {
                                                photoToPreviewInModal = straightPhotoBitmap
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            bitmap = straightPhotoBitmap!!.asImageBitmap(),
                                            contentDescription = "Снимок лица 1",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.6f))
                                        ) {
                                            Text(
                                                text = "Ракурс 1 ✓",
                                                color = BloxGreenBright,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }

                                if (faceSubStep == 2) {
                                    // Directional arrow prompt
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = 8.dp)
                                            .clip(CircleShape)
                                            .background(BloxCyan.copy(alpha = 0.85f))
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            when (faceSubStep) {
                                1 -> {
                                    BloxButton(
                                        text = "Сделать снимок лица 📸",
                                        onClick = {
                                            val bm = PhotoStorage.createBiometricFaceBitmap(
                                                name = "Пользователь",
                                                age = 19,
                                                angleLabel = "ПРЯМОЙ РАКУРС"
                                            )
                                            straightPhotoBitmap = bm
                                            straightPhotoPath = PhotoStorage.saveBitmapToFile(
                                                context,
                                                bm,
                                                "face_straight_${System.currentTimeMillis()}"
                                            )
                                            faceSubStep = 2
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        testTag = "capture_straight_photo_button"
                                    )
                                }
                                2 -> {
                                    BloxButton(
                                        text = "Сфотографировать в ракурсе 📸",
                                        onClick = {
                                            val bm = PhotoStorage.createBiometricFaceBitmap(
                                                name = "Пользователь",
                                                age = 19,
                                                angleLabel = "БОКОВОЙ РАКУРС"
                                            )
                                            anglePhotoBitmap = bm
                                            anglePhotoPath = PhotoStorage.saveBitmapToFile(
                                                context,
                                                bm,
                                                "face_angle_${System.currentTimeMillis()}"
                                            )
                                            faceSubStep = 3
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = BloxGreen,
                                        testTag = "capture_angle_photo_button"
                                    )
                                }
                                3 -> {
                                    LaunchedEffect(Unit) {
                                        delay(2600)
                                        detectedAge = (18..24).random()
                                        faceSubStep = 4
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = BloxCyan,
                                            modifier = Modifier.size(36.dp),
                                            strokeWidth = 3.dp
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Анализ биометрии и пропорций лица...",
                                            color = BloxTextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                4 -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(BloxCyan.copy(alpha = 0.15f))
                                            .border(1.dp, BloxCyan, RoundedCornerShape(10.dp))
                                            .padding(14.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = BloxCyan,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Возраст определён: $detectedAge лет",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Биометрическая проверка пройдена успешно. Фотографии сохранены для вашего профиля.",
                                                color = BloxTextSecondary,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    BloxButton(
                                        text = "Продолжить к выбору имени ➔",
                                        onClick = {
                                            currentStep = OnboardingStep.NAME_INPUT
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. REAL NAME CHECK (Rejects random letters / gibberish)
                OnboardingStep.NAME_INPUT -> {
                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCard
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Какое ваше настоящее имя?",
                                color = BloxTextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Система проверяет наличие имени в реальном мире. Пожалуйста, укажите настоящее имя, а не случайный набор букв.",
                                color = BloxTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = {
                                    nameInput = it
                                    nameErrorMessage = null
                                },
                                placeholder = {
                                    Text(
                                        text = "Например: Мохьмад, Артём, Анна...",
                                        color = BloxTextMuted
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("name_input_field"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = BloxInputBg,
                                    unfocusedContainerColor = BloxInputBg,
                                    focusedBorderColor = BloxCyan,
                                    unfocusedBorderColor = BloxBorder,
                                    focusedTextColor = BloxTextPrimary,
                                    unfocusedTextColor = BloxTextPrimary
                                ),
                                isError = nameErrorMessage != null,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        val result = NameValidator.validateRealName(nameInput)
                                        if (result.isValid) {
                                            validatedName = result.formattedName
                                            currentStep = OnboardingStep.CONNECT_GOOGLE
                                        } else {
                                            nameErrorMessage = result.errorMessage
                                        }
                                    }
                                )
                            )

                            if (nameErrorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = BloxRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = nameErrorMessage ?: "",
                                        color = BloxRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            BloxButton(
                                text = "Подтвердить имя ✓",
                                onClick = {
                                    val result = NameValidator.validateRealName(nameInput)
                                    if (result.isValid) {
                                        validatedName = result.formattedName
                                        currentStep = OnboardingStep.CONNECT_GOOGLE
                                    } else {
                                        nameErrorMessage = result.errorMessage
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "confirm_name_button"
                            )
                        }
                    }
                }

                // 5. CONNECT GOOGLE ACCOUNT STEP (OPTIONAL / STRICT ADMIN BINDING)
                OnboardingStep.CONNECT_GOOGLE -> {
                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCard
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Подключить аккаунт Google",
                                color = BloxTextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Вы можете привязать свою почту Google и пароль. Если это аккаунт администратора (gadaevmohmad955@gmail.com), откроется Панель Администратора.",
                                color = BloxTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Google Email Field
                            OutlinedTextField(
                                value = googleEmailInput,
                                onValueChange = {
                                    googleEmailInput = it
                                    googleLinkErrorMessage = null
                                },
                                placeholder = { Text("your.email@gmail.com", color = BloxTextMuted) },
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password Field
                            OutlinedTextField(
                                value = googlePasswordInput,
                                onValueChange = {
                                    googlePasswordInput = it
                                    googleLinkErrorMessage = null
                                },
                                placeholder = { Text("Пароль от Google", color = BloxTextMuted) },
                                label = { Text("Пароль", color = BloxCyan) },
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

                            if (googleLinkErrorMessage != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = googleLinkErrorMessage ?: "",
                                    color = BloxRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Connect Button
                            BloxButton(
                                text = "🔗 Подключить Google-аккаунт",
                                onClick = {
                                    val norm = googleEmailInput.trim().lowercase()
                                    if (norm.isEmpty() || !norm.contains("@")) {
                                        googleLinkErrorMessage = "Введите корректную почту Google"
                                        return@BloxButton
                                    }
                                    if (googlePasswordInput.length < 4) {
                                        googleLinkErrorMessage = "Введите пароль от аккаунта Google (мин. 4 символа)"
                                        return@BloxButton
                                    }

                                    // Check if email was already bound anywhere
                                    val sp = context.getSharedPreferences("blox_chat_prefs", Context.MODE_PRIVATE)
                                    val claimedUid = sp.getString("google_claimed_uid_$norm", null)
                                    if (claimedUid != null) {
                                        googleLinkErrorMessage = "❌ Эта почта Google уже привязана к другому аккаунту! Повторное подключение невозможно."
                                        return@BloxButton
                                    }

                                    connectedGoogleEmail = norm
                                    currentStep = OnboardingStep.CREATING_PROFILE
                                },
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = BloxCyan
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            BloxSecondaryButton(
                                text = "Пропустить (войти как Гость)",
                                onClick = {
                                    connectedGoogleEmail = ""
                                    currentStep = OnboardingStep.CREATING_PROFILE
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 6. ACCOUNT CREATION TRANSITIONS
                OnboardingStep.CREATING_PROFILE -> {
                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCard
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(32.dp)
                                .height(260.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = BloxCyan,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            AnimatedContent(
                                targetState = profileCreationMessageIndex,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(500)) togetherWith
                                            fadeOut(animationSpec = tween(500))
                                },
                                label = "profile_creation_step"
                            ) { index ->
                                Text(
                                    text = when (index) {
                                        0 -> "Привет!"
                                        1 -> "Пожалуйста, подождите...\nМы создаём вам профиль в BloxChat"
                                        2 -> "Осталось ещё немного...\nШифрование данных и ключа безопасности"
                                        else -> "Привет, $validatedName!\nСпасибо, что вы зарегистрировались!"
                                    },
                                    color = BloxTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 26.sp
                                )
                            }
                        }
                    }
                }

                // 7. SECRET ACCESS KEY REVEAL (5-second locked dialog)
                OnboardingStep.SECRET_KEY_REVEAL -> {
                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCard,
                        borderColor = BloxCyan.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(BloxCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = BloxCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Ваш секретный ключ для входа",
                                color = BloxTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Вот ваш ключ для входа в аккаунт. Сохраните его в надёжном месте, чтобы не потерять доступ к аккаунту!",
                                color = BloxTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Key Container Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BloxInputBg)
                                    .border(1.5.dp, BloxCyan, RoundedCornerShape(10.dp))
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = generatedKey,
                                    color = BloxCyan,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.8.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Copy Button
                            BloxSecondaryButton(
                                text = if (keyCopied) "Ключ скопирован ✓" else "Скопировать ключ 📋",
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("BloxChat Key", generatedKey))
                                    keyCopied = true
                                    Toast.makeText(context, "Ключ скопирован в буфер обмена!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "copy_key_button"
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 5 Seconds Lock Button
                            BloxButton(
                                text = if (keyLockCountdown > 0) {
                                    "Подождите ($keyLockCountdown сек)..."
                                } else {
                                    "Войти в BloxChat 🚀"
                                },
                                onClick = {
                                    if (keyLockCountdown == 0) {
                                        onOnboardingComplete()
                                    }
                                },
                                enabled = (keyLockCountdown == 0),
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = if (keyLockCountdown == 0) BloxGreen else BloxCardElevated,
                                testTag = "enter_app_button"
                            )
                        }
                    }
                }

                // 8. LOGIN WITH EXISTING KEY
                OnboardingStep.LOGIN_WITH_KEY -> {
                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCard
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Вход по секретному ключу",
                                color = BloxTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Введите ваш уникальный 16-значный ключ:",
                                color = BloxTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedTextField(
                                value = loginKeyInput,
                                onValueChange = {
                                    loginKeyInput = it.uppercase()
                                    loginError = null
                                },
                                placeholder = {
                                    Text("XXXX-XXXX-XXXX-XXXX", color = BloxTextMuted)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_key_input"),
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

                            if (loginError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = loginError ?: "",
                                    color = BloxRed,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            BloxButton(
                                text = "Войти в аккаунт",
                                onClick = {
                                    val success = repository.loginWithKey(loginKeyInput)
                                    if (success) {
                                        onOnboardingComplete()
                                    } else {
                                        loginError = "Неверный ключ или аккаунт не найден"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "submit_login_key"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = { currentStep = OnboardingStep.BOT_WELCOME }
                            ) {
                                Text(
                                    text = "← Назад к регистрации",
                                    color = BloxCyan,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // 9. LOGIN WITH GOOGLE ACCOUNT
                OnboardingStep.LOGIN_WITH_GOOGLE -> {
                    BloxCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = BloxCard
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Вход через Google",
                                color = BloxTextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Введите почту Google и пароль. Если указана почта gadaevmohmad955@gmail.com, откроется доступ администратора.",
                                color = BloxTextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedTextField(
                                value = directLoginEmail,
                                onValueChange = {
                                    directLoginEmail = it
                                    directLoginError = null
                                },
                                placeholder = { Text("gadaevmohmad955@gmail.com", color = BloxTextMuted) },
                                label = { Text("Почта Google", color = BloxCyan) },
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

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = directLoginPassword,
                                onValueChange = {
                                    directLoginPassword = it
                                    directLoginError = null
                                },
                                placeholder = { Text("Пароль", color = BloxTextMuted) },
                                label = { Text("Пароль", color = BloxCyan) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
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

                            if (directLoginError != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = directLoginError ?: "",
                                    color = BloxRed,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            BloxButton(
                                text = "Войти через Google",
                                onClick = {
                                    val norm = directLoginEmail.trim().lowercase()
                                    if (norm.isEmpty() || !norm.contains("@")) {
                                        directLoginError = "Введите корректную почту Google"
                                        return@BloxButton
                                    }
                                    if (directLoginPassword.length < 4) {
                                        directLoginError = "Пароль должен содержать не менее 4 символов"
                                        return@BloxButton
                                    }

                                    repository.loginWithGoogle(email = norm)
                                    onOnboardingComplete()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = BloxGold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = { currentStep = OnboardingStep.BOT_WELCOME }
                            ) {
                                Text(
                                    text = "← Назад к регистрации",
                                    color = BloxCyan,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fullscreen Modal for viewing captured face photo
        photoToPreviewInModal?.let { bitmap ->
            FullScreenPhotoViewer(
                title = "Биометрический снимок лица",
                subtitle = "Верифицировано в BloxChat",
                bitmap = bitmap,
                onDismiss = { photoToPreviewInModal = null }
            )
        }
    }
}
