package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BuyerDialog
import com.example.data.model.GarageItem
import com.example.data.model.PlatformConnection
import com.example.data.model.PlatformType
import com.example.ui.components.MiniBarcodeCanvas
import com.example.ui.components.MiniQrCodeCanvas
import com.example.ui.components.OneClickPublishGradientButton
import com.example.ui.components.PhotoHintChip
import com.example.ui.components.PlatformBadgeIcon
import com.example.ui.components.formatRubles
import com.example.ui.theme.GarageAmberYellow
import com.example.ui.theme.GarageElectricBlueBright
import com.example.ui.theme.GarageEmeraldGreen
import com.example.ui.theme.GarageOrange
import com.example.ui.viewmodel.PublishProgressState

@Composable
fun HomeScreen(
    items: List<GarageItem>,
    platforms: List<PlatformConnection>,
    dialogs: List<BuyerDialog>,
    publishProgress: PublishProgressState,
    onPlatformClick: (PlatformConnection) -> Unit,
    onOneClickPublishAll: () -> Unit,
    onAddItemClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onPromotionClick: () -> Unit,
    onItemClick: (GarageItem) -> Unit,
    onPhotoHintClick: (GarageItem) -> Unit,
    onOpenDialogsClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var onlyUnpublishedFilter by remember { mutableStateOf(false) }

    val filteredItems = remember(items, searchQuery, onlyUnpublishedFilter) {
        items.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.qrCode.contains(searchQuery, ignoreCase = true) ||
                item.barcode.contains(searchQuery, ignoreCase = true) ||
                item.serialCode.contains(searchQuery, ignoreCase = true) ||
                item.shelfLocation.contains(searchQuery, ignoreCase = true)
            val matchesFilter = !onlyUnpublishedFilter || item.publishedPlatforms.size < 4
            matchesQuery && matchesFilter
        }
    }

    val totalViews = remember(items) { items.sumOf { it.viewsCount } }
    val totalInquiries = remember(items, dialogs) {
        items.sumOf { it.inquiriesCount } + dialogs.size
    }
    val connectedPlatformsCount = remember(platforms) {
        platforms.count { it.isConnected }.let { if (it == 0) 4 else it }
    }
    val unreadMessagesCount = remember(dialogs) {
        dialogs.sumOf { it.unreadCount }.toInt()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Connected Platforms Card
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Подключенные платформы",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Привязать / API",
                        style = MaterialTheme.typography.labelSmall,
                        color = GarageOrange
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    PlatformType.entries.forEach { pType ->
                        val conn = platforms.find { it.platformKey == pType.key }
                            ?: PlatformConnection(
                                id = pType.key,
                                platformKey = pType.key,
                                platformName = pType.displayName
                            )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPlatformClick(conn) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("home_platform_${pType.key}")
                        ) {
                            PlatformBadgeIcon(
                                platformType = pType,
                                size = 56.dp,
                                showStatusDot = true,
                                isConnected = conn.isConnected
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = pType.displayName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Views & responses summary row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "$totalViews просмотров сегодня  •  ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalInquiries откликов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 1-Click Publish All Button + Live Progress
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OneClickPublishGradientButton(
                    title = "ОПУБЛИКОВАТЬ ВСЁ В 1 КЛИК",
                    subtitle = "разместить все товары на $connectedPlatformsCount платформы сразу",
                    isPublishing = publishProgress.isPublishing,
                    onClick = onOneClickPublishAll
                )

                AnimatedVisibility(visible = publishProgress.isPublishing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = publishProgress.currentStepText,
                            fontSize = 12.sp,
                            color = GarageOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { publishProgress.progressFraction },
                            modifier = Modifier.fillMaxWidth(),
                            color = GarageOrange
                        )
                    }
                }
            }
        }

        // Quick Access ("БЫСТРЫЙ ДОСТУП")
        item {
            Column {
                Text(
                    text = "БЫСТРЫЙ ДОСТУП",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickAccessCard(
                        icon = Icons.Default.Add,
                        label = "Добавить товар",
                        iconTint = Color(0xFFCBD5E1),
                        onClick = onAddItemClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_item_card")
                    )
                    QuickAccessCard(
                        icon = Icons.Default.QrCodeScanner,
                        label = "Скан QR / кода",
                        iconTint = GarageElectricBlueBright,
                        onClick = onScanQrClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_scan_qr_card")
                    )
                    QuickAccessCard(
                        icon = Icons.Default.RocketLaunch,
                        label = "Продвижение",
                        iconTint = GarageOrange,
                        onClick = onPromotionClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_promote_card")
                    )
                }
            }
        }

        // My Garage & Warehouse Section Header + Search Bar
        item {
            Column {
                Text(
                    text = "МОЙ ГАРАЖ И СКЛАД",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Найти по названию, QR или штрихкоду...",
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Поиск"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = GarageOrange
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (onlyUnpublishedFilter) GarageOrange.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (onlyUnpublishedFilter) GarageOrange else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onlyUnpublishedFilter = !onlyUnpublishedFilter },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Фильтр",
                            tint = if (onlyUnpublishedFilter) GarageOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2-Column Grid of Garage Items or Clean Empty State
        if (filteredItems.isEmpty()) {
            item {
                EmptyGarageWelcomeCard(
                    onAddItemClick = onAddItemClick,
                    onConnectPlatformsClick = {
                        platforms.firstOrNull()?.let(onPlatformClick)
                    }
                )
            }
        } else {
            val chunked = filteredItems.chunked(2)
            items(chunked.size) { rowIndex ->
                val pair = chunked[rowIndex]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { garageItem ->
                        HomeGarageGridCard(
                            item = garageItem,
                            onClick = { onItemClick(garageItem) },
                            onPhotoHintClick = { onPhotoHintClick(garageItem) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Unified Messages Bottom Pill matching Screenshot 3
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenDialogsClick)
                    .testTag("home_unified_messages_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(GarageOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = GarageOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (unreadMessagesCount > 0) {
                                "Единые сообщения ($unreadMessagesCount новых)"
                            } else {
                                "Единые сообщения (${dialogs.size} диалогов)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                        PlatformType.entries.take(3).forEach { pt ->
                            PlatformBadgeIcon(platformType = pt, size = 24.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .height(84.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HomeGarageGridCard(
    item: GarageItem,
    onClick: () -> Unit,
    onPhotoHintClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pubCount = item.publishedPlatforms.size
    val statusDotColor = when {
        pubCount >= 4 -> GarageEmeraldGreen
        pubCount > 0 -> GarageAmberYellow
        else -> Color(0xFF64748B)
    }
    val statusLabel = when {
        pubCount >= 4 -> "Опубликовано везде (4/4)"
        pubCount > 0 -> "Опубликовано ($pubCount/4)"
        else -> "Черновик (0/4)"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatRubles(item.price),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${item.zone}, ${item.shelfLocation}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QR: ${item.qrCode}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (item.barcode.isNotBlank()) {
                    MiniBarcodeCanvas(barcode = item.barcode, width = 38.dp, height = 16.dp)
                } else {
                    MiniQrCodeCanvas(code = item.qrCode, size = 20.dp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusDotColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = statusLabel,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom preview box with Photo Hint button overlay matching Screenshot 3
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1A1E27), Color(0xFF283040))
                        )
                    )
                    .clickable(onClick = onPhotoHintClick),
                contentAlignment = Alignment.BottomStart
            ) {
                val previewUri = item.hintPhotoUri.ifBlank { item.photoUri }
                if (previewUri.isNotBlank()) {
                    AsyncImage(
                        model = previewUri,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(modifier = Modifier.padding(6.dp)) {
                    PhotoHintChip(
                        hasHintPhoto = item.hintPhotoUri.isNotBlank(),
                        hintNote = item.hintNote,
                        labelText = "Фото-подсказка",
                        onClick = onPhotoHintClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGarageWelcomeCard(
    onAddItemClick: () -> Unit,
    onConnectPlatformsClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Ваш гараж и склад готовы к работе",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Добавьте свой первый товар с ценой, QR-кодом, штрихкодом, номером полки и фото-подсказкой, чтобы находить его за секунду и публиковать в 1 клик.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddItemClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GarageOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("empty_state_add_item_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Добавить товар", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onConnectPlatformsClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Платформы", fontSize = 13.sp)
                }
            }
        }
    }
}
