package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PlatformType
import com.example.ui.theme.AvitoBlue
import com.example.ui.theme.AvitoGreen
import com.example.ui.theme.AvitoPurple
import com.example.ui.theme.AvitoRed
import com.example.ui.theme.GarageElectricBlueBright
import com.example.ui.theme.GarageEmeraldGreen
import com.example.ui.theme.GarageOrange
import com.example.ui.theme.VkBlue
import com.example.ui.theme.YandexCream
import com.example.ui.theme.YandexRed
import com.example.ui.theme.YoulaCyan
import com.example.ui.theme.YoulaPurple
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

fun formatRubles(price: Long): String {
    val nf = NumberFormat.getIntegerInstance(Locale("ru", "RU"))
    return "${nf.format(price)} ₽"
}

/**
 * Custom vector-drawn Garage + Orange Toolbox logo matching the screenshot header.
 */
@Composable
fun GarageBrandLogo(size: Dp = 36.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Garage silver frame
        val framePath = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.92f, h * 0.28f)
            lineTo(w * 0.92f, h * 0.92f)
            lineTo(w * 0.78f, h * 0.92f)
            lineTo(w * 0.78f, h * 0.42f)
            lineTo(w * 0.22f, h * 0.42f)
            lineTo(w * 0.22f, h * 0.92f)
            lineTo(w * 0.08f, h * 0.92f)
            lineTo(w * 0.08f, h * 0.28f)
            close()
        }
        drawPath(framePath, color = Color(0xFFBCC6D4))

        // Roof shutter lines
        drawRoundRect(
            color = Color(0xFF64748B),
            topLeft = Offset(w * 0.22f, h * 0.28f),
            size = Size(w * 0.56f, h * 0.05f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = Color(0xFF64748B),
            topLeft = Offset(w * 0.22f, h * 0.35f),
            size = Size(w * 0.56f, h * 0.04f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Orange toolbox inside garage
        drawRoundRect(
            color = GarageOrange,
            topLeft = Offset(w * 0.27f, h * 0.54f),
            size = Size(w * 0.46f, h * 0.38f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Toolbox lid stripe
        drawRect(
            color = Color(0xFF181C26),
            topLeft = Offset(w * 0.27f, h * 0.68f),
            size = Size(w * 0.46f, h * 0.05f)
        )
        // Toolbox latch
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.45f, h * 0.65f),
            size = Size(w * 0.10f, h * 0.11f),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}

/**
 * Top iOS Cupertino-styled header matching the 4 reference screenshots:
 * Garage logo + "Мой гараж" + User avatar + Notification bell with iOS red badge.
 */
@Composable
fun GarageTopBar(
    unreadCount: Int,
    userPhotoUrl: String?,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GarageBrandLogo(size = 36.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Мой гараж",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Avatar circle with iOS blue ring
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, Color(0xFF0A84FF), CircleShape)
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onProfileClick()
                    }
                    .testTag("top_bar_profile_button"),
                contentAlignment = Alignment.Center
            ) {
                if (!userPhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = userPhotoUrl,
                        contentDescription = "Профиль пользователя",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Профиль пользователя",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Notification Bell iOS squircle box with red badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(13.dp))
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNotificationsClick()
                    }
                    .testTag("top_bar_notifications_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Уведомления",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30)), // iOS System Red
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.coerceAtMost(99).toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * iOS Modal Sheet Top Drag Handle + Cupertino Header ("Отмена" / Title / "Готово")
 */
@Composable
fun CupertinoSheetHeader(
    title: String,
    cancelText: String = "Отмена",
    doneText: String? = null,
    doneEnabled: Boolean = true,
    onCancel: () -> Unit,
    onDone: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // iOS Grabber Pill
        Box(
            modifier = Modifier
                .padding(top = 6.dp, bottom = 10.dp)
                .width(38.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(Color(0xFF64748B).copy(alpha = 0.55f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cancelText,
                color = Color(0xFF0A84FF), // iOS System Blue
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onCancel)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (doneText != null && onDone != null) {
                Text(
                    text = doneText,
                    color = if (doneEnabled) GarageOrange else Color(0xFF64748B),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = doneEnabled, onClick = onDone)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

/**
 * Renders the authentic platform icon (Авито, Юла, ВК Маркет, Яндекс.Объявления)
 * with optional green connected dot or checkmark badge.
 */
@Composable
fun PlatformBadgeIcon(
    platformType: PlatformType,
    size: Dp = 54.dp,
    showStatusDot: Boolean = false,
    showCheckmark: Boolean = false,
    isConnected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .let { mod ->
                if (onClick != null) mod.clickable(onClick = onClick) else mod
            },
        contentAlignment = Alignment.Center
    ) {
        when (platformType) {
            PlatformType.AVITO -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            color = if (isConnected) Color(0xFF30D158).copy(alpha = 0.6f) else Color(0xFF334155),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(size * 0.56f)) {
                        val w = this.size.width
                        val h = this.size.height
                        drawCircle(
                            color = AvitoBlue,
                            radius = w * 0.24f,
                            center = Offset(w * 0.30f, h * 0.30f)
                        )
                        drawCircle(
                            color = AvitoRed,
                            radius = w * 0.20f,
                            center = Offset(w * 0.72f, h * 0.32f)
                        )
                        drawCircle(
                            color = AvitoPurple,
                            radius = w * 0.16f,
                            center = Offset(w * 0.28f, h * 0.72f)
                        )
                        drawCircle(
                            color = AvitoGreen,
                            radius = w * 0.25f,
                            center = Offset(w * 0.68f, h * 0.70f)
                        )
                    }
                }
            }

            PlatformType.YOULA -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF1F2430))
                        .border(
                            width = 1.5.dp,
                            color = if (isConnected) YoulaCyan.copy(alpha = 0.6f) else Color(0xFF3B4254),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(size * 0.58f)) {
                        val w = this.size.width
                        val h = this.size.height
                        val starPath = Path().apply {
                            moveTo(w * 0.5f, 0f)
                            quadraticTo(w * 0.54f, h * 0.44f, w, h * 0.5f)
                            quadraticTo(w * 0.54f, h * 0.56f, w * 0.5f, h)
                            quadraticTo(w * 0.46f, h * 0.56f, 0f, h * 0.5f)
                            quadraticTo(w * 0.46f, h * 0.44f, w * 0.5f, 0f)
                            close()
                        }
                        drawPath(
                            path = starPath,
                            brush = Brush.linearGradient(
                                colors = listOf(YoulaCyan, YoulaPurple)
                            )
                        )
                    }
                }
            }

            PlatformType.VK -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(VkBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VK",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (size.value * 0.34f).sp
                    )
                }
            }

            PlatformType.YANDEX -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(YandexCream),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(size * 0.64f)
                            .clip(CircleShape)
                            .background(YandexRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Я",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = (size.value * 0.34f).sp
                        )
                    }
                }
            }
        }

        if (showStatusDot || showCheckmark) {
            val badgeSize = (size.value * 0.32f).coerceIn(12f, 20f).dp
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(badgeSize)
                    .clip(CircleShape)
                    .background(if (isConnected) Color(0xFF30D158) else Color(0xFF64748B))
                    .border(1.5.dp, Color(0xFF14161D), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (showCheckmark && isConnected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Подключено",
                        tint = Color.White,
                        modifier = Modifier.size(badgeSize * 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Signature Orange-to-Blue 1-Click Publish Gradient Button with iOS squircle corners & haptic feedback.
 */
@Composable
fun OneClickPublishGradientButton(
    title: String,
    subtitle: String,
    isPublishing: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "publish_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPublishing) 0.98f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = !isPublishing,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .height(64.dp)
            .testTag("one_click_publish_button")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6B00),
                            Color(0xFFFF851B),
                            Color(0xFF0A84FF),
                            Color(0xFF0060DF)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.38f),
                            Color.White.copy(alpha = 0.16f)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "В 1 клик",
                        tint = Color(0xFFFFD60A),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Deterministic visual QR code matrix drawn on Canvas from any code string (e.g. #GAR-084-III).
 */
@Composable
fun MiniQrCodeCanvas(
    code: String,
    size: Dp = 28.dp,
    tint: Color = Color.White
) {
    Canvas(modifier = Modifier.size(size)) {
        val grid = 7
        val cellW = this.size.width / grid
        val cellH = this.size.height / grid
        val hash = abs(code.hashCode().coerceAtLeast(1))

        for (r in 0 until grid) {
            for (c in 0 until grid) {
                val isCorner = (r < 2 && c < 2) || (r < 2 && c >= grid - 2) || (r >= grid - 2 && c < 2)
                val bit = ((hash shr ((r * grid + c) % 28)) and 1) == 1
                if (isCorner || bit) {
                    drawRect(
                        color = tint,
                        topLeft = Offset(c * cellW, r * cellH),
                        size = Size(cellW * 0.85f, cellH * 0.85f)
                    )
                }
            }
        }
    }
}

/**
 * Deterministic Barcode stripes drawn on Canvas from any barcode string.
 */
@Composable
fun MiniBarcodeCanvas(
    barcode: String,
    width: Dp = 56.dp,
    height: Dp = 20.dp,
    tint: Color = Color(0xFFCBD5E1)
) {
    Canvas(modifier = Modifier.width(width).height(height)) {
        val bars = 22
        val step = this.size.width / bars
        val seed = abs(barcode.ifEmpty { "4607185" }.hashCode())
        for (i in 0 until bars) {
            val thick = if (((seed shr (i % 16)) and 1) == 1) step * 0.72f else step * 0.35f
            drawRect(
                color = tint,
                topLeft = Offset(i * step, 0f),
                size = Size(thick, this.size.height)
            )
        }
    }
}

/**
 * Renders product image from URI or a clean styled garage item visual placeholder.
 */
@Composable
fun ItemThumbnailBox(
    photoUri: String,
    title: String,
    size: Dp = 84.dp,
    cornerRadius: Dp = 14.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF282E3D), Color(0xFF191D26))
                )
            )
            .border(1.dp, Color(0xFF384152), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri.isNotBlank()) {
            AsyncImage(
                model = Uri.parse(photoUri),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = title,
                    tint = GarageOrange,
                    modifier = Modifier.size(size * 0.38f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title.take(10),
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Pill button for "📷 Фото-подсказка" matching the user's screenshots.
 */
@Composable
fun PhotoHintChip(
    hasHintPhoto: Boolean,
    hintNote: String,
    labelText: String = "Фото-подсказка",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF2A303D))
            .border(
                width = 1.dp,
                color = if (hasHintPhoto || hintNote.isNotBlank()) GarageOrange.copy(alpha = 0.6f) else Color(0xFF3E4759),
                shape = RoundedCornerShape(9.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = labelText,
            tint = if (hasHintPhoto || hintNote.isNotBlank()) GarageOrange else Color(0xFF94A3B8),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = labelText,
            fontSize = 11.sp,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Medium
        )
    }
}
