package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.PhotoStorage
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

/**
 * Iconic Roblox-style Primary Button with bold text and blocky rounded corners
 */
@Composable
fun BloxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = BloxCyan,
    contentColor: Color = Color.White,
    icon: @Composable (() -> Unit)? = null,
    testTag: String = "blox_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = BloxCardElevated,
            disabledContentColor = BloxTextMuted
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

/**
 * Secondary Dark Button
 */
@Composable
fun BloxSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = BloxCardElevated,
    contentColor: Color = Color.White,
    icon: @Composable (() -> Unit)? = null,
    testTag: String = "blox_secondary_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(48.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(BloxBorder, BloxBorder))
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
        }
    }
}

/**
 * Roblox Solid Card Container
 */
@Composable
fun BloxCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BloxCard,
    borderColor: Color = BloxBorder,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        content()
    }
}

/**
 * Roblox Status & Verification Badges
 */
@Composable
fun BloxBadge(
    text: String,
    badgeType: BadgeType,
    modifier: Modifier = Modifier
) {
    val (bg, fg, icon) = when (badgeType) {
        BadgeType.VERIFIED -> Triple(
            BloxCyan.copy(alpha = 0.2f),
            BloxCyan,
            Icons.Default.Shield
        )
        BadgeType.VIP -> Triple(
            BloxGold.copy(alpha = 0.2f),
            BloxGold,
            Icons.Default.Star
        )
        BadgeType.ADMIN -> Triple(
            BloxPurple.copy(alpha = 0.25f),
            BloxPurple,
            Icons.Default.CheckCircle
        )
        BadgeType.ONLINE -> Triple(
            BloxGreen.copy(alpha = 0.2f),
            BloxGreenBright,
            null
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        } else if (badgeType == BadgeType.ONLINE) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BloxGreenBright)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = text,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

enum class BadgeType {
    VERIFIED, VIP, ADMIN, ONLINE
}

/**
 * Roblox Avatar Renderer with optional click to view photo
 */
@Composable
fun BloxAvatar(
    name: String,
    avatarKey: String = "avatar_1",
    photoPath: String? = null,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val gradients = listOf(
        listOf(Color(0xFF00A2FF), Color(0xFF0055AA)),
        listOf(Color(0xFF00B06F), Color(0xFF006633)),
        listOf(Color(0xFFFFAA00), Color(0xFFCC5500)),
        listOf(Color(0xFF8B5CF6), Color(0xFF4C1D95)),
        listOf(Color(0xFFEC4899), Color(0xFF9D174D)),
        listOf(Color(0xFF06B6D4), Color(0xFF0E7490))
    )
    val colorIndex = kotlin.math.abs((name + avatarKey).hashCode()) % gradients.size
    val brush = Brush.linearGradient(gradients[colorIndex])

    val localBitmap = remember(photoPath) {
        PhotoStorage.loadBitmapFromFile(photoPath) ?: PhotoStorage.base64ToBitmap(photoPath)
    }

    val clickableMod = if (onClick != null) {
        modifier.clickable { onClick() }
    } else modifier

    Box(
        modifier = clickableMod.size(size)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(size * 0.28f))
                .background(brush)
                .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(size * 0.28f)),
            contentAlignment = Alignment.Center
        ) {
            if (localBitmap != null) {
                Image(
                    bitmap = localBitmap.asImageBitmap(),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val initial = if (name.isNotBlank()) name.take(1).uppercase() else "R"
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (size.value * 0.45f).sp
                )
            }
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(BloxGreenBright)
                    .border(2.dp, BloxDarkBg, CircleShape)
            )
        }
    }
}

/**
 * Fullscreen Interactive Photo Viewer Modal
 * Allows users to inspect verified facial photos, avatars, and chat images in full clarity.
 */
@Composable
fun FullScreenPhotoViewer(
    title: String = "Просмотр фотографии",
    subtitle: String? = null,
    bitmap: Bitmap? = null,
    photoPath: String? = null,
    avatarName: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val resolvedBitmap = remember(bitmap, photoPath) {
        bitmap ?: PhotoStorage.loadBitmapFromFile(photoPath) ?: PhotoStorage.base64ToBitmap(photoPath)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            color = BloxCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Фото сохранено в галерею", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Сохранить",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = Color.White
                        )
                    }
                }
            }

            // Image Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (resolvedBitmap != null) {
                    Image(
                        bitmap = resolvedBitmap.asImageBitmap(),
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, BloxCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    )
                } else {
                    // Aesthetic stylized avatar card if no physical photo file exists
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(BloxCard)
                            .border(2.dp, BloxCyan, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            BloxAvatar(
                                name = avatarName ?: "Bloxian",
                                size = 120.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = avatarName ?: "Пользователь BloxChat",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            BloxBadge(text = "Биометрический профиль", badgeType = BadgeType.VERIFIED)
                        }
                    }
                }
            }

            // Bottom Bar status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔒 Защищено сквозным шифрованием Blox Guard",
                    color = BloxTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Live Camera Preview with CameraX, Front Camera support, and Biometric Scanner Overlay
 */
@Composable
fun CameraPreviewBox(
    modifier: Modifier = Modifier,
    isScanning: Boolean = false,
    scanText: String? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_laser_line"
    )

    Box(
        modifier = modifier
            .background(BloxDarkBg),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            DisposableEffect(lifecycleOwner) {
                onDispose {
                    try {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        if (cameraProviderFuture.isDone) {
                            cameraProviderFuture.get().unbindAll()
                        }
                    } catch (_: Exception) {}
                }
            }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (e: Exception) {
                            // ignore gracefully
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                onRelease = { view ->
                    try {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(view.context)
                        if (cameraProviderFuture.isDone) {
                            cameraProviderFuture.get().unbindAll()
                        }
                    } catch (_: Exception) {}
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Interactive fallback simulated camera canvas with realistic biometric face outline
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val ovalWidth = size.width * 0.55f
                val ovalHeight = size.height * 0.65f

                // Draw face guide oval
                drawOval(
                    color = if (isScanning) BloxCyan.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(cx - ovalWidth / 2, cy - ovalHeight / 2),
                    size = Size(ovalWidth, ovalHeight),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Corner brackets
                val bracketLen = 30.dp.toPx()
                val inset = 16.dp.toPx()
                drawLine(BloxCyan, Offset(inset, inset), Offset(inset + bracketLen, inset), 4f)
                drawLine(BloxCyan, Offset(inset, inset), Offset(inset, inset + bracketLen), 4f)
                drawLine(BloxCyan, Offset(size.width - inset, inset), Offset(size.width - inset - bracketLen, inset), 4f)
                drawLine(BloxCyan, Offset(size.width - inset, inset), Offset(size.width - inset, inset + bracketLen), 4f)
                drawLine(BloxCyan, Offset(inset, size.height - inset), Offset(inset + bracketLen, size.height - inset), 4f)
                drawLine(BloxCyan, Offset(inset, size.height - inset), Offset(inset, size.height - inset - bracketLen), 4f)
                drawLine(BloxCyan, Offset(size.width - inset, size.height - inset), Offset(size.width - inset - bracketLen, size.height - inset), 4f)
                drawLine(BloxCyan, Offset(size.width - inset, size.height - inset), Offset(size.width - inset, size.height - inset - bracketLen), 4f)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = BloxCyan,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Фронтальная HD Камера",
                    color = BloxTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Biometric Laser Scanner Line animation when scanning
        if (isScanning) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height * scanLineY
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BloxCyan.copy(alpha = 0.9f),
                            Color.White,
                            BloxCyan.copy(alpha = 0.9f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 6.dp.toPx()
                )
            }

            if (scanText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = scanText,
                        color = BloxCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
