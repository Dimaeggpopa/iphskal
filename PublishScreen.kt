package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GarageItem
import com.example.data.model.PlatformConnection
import com.example.data.model.PlatformType
import com.example.ui.components.ItemThumbnailBox
import com.example.ui.components.MiniBarcodeCanvas
import com.example.ui.components.OneClickPublishGradientButton
import com.example.ui.components.PhotoHintChip
import com.example.ui.components.PlatformBadgeIcon
import com.example.ui.components.formatRubles
import com.example.ui.theme.GarageEmeraldGreen
import com.example.ui.theme.GarageOrange
import com.example.ui.viewmodel.PublishProgressState

@Composable
fun PublishScreen(
    items: List<GarageItem>,
    platforms: List<PlatformConnection>,
    publishProgress: PublishProgressState,
    onTogglePlatformUpload: (PlatformConnection) -> Unit,
    onConfigurePlatform: (PlatformConnection) -> Unit,
    onToggleItemSelection: (GarageItem) -> Unit,
    onPublishSelected: () -> Unit,
    onAddItemClick: () -> Unit,
    onPhotoHintClick: (GarageItem) -> Unit,
    onEditItemClick: (GarageItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeFilterTab by remember { mutableStateOf("Все товары") }

    val selectedCount = remember(items) { items.count { it.isSelectedForPublish } }
    val activePlatformsCount = remember(platforms) {
        platforms.count { it.isConnected && it.isSelectedForUpload }.let { if (it == 0) 4 else it }
    }

    val filteredItems = remember(items, searchQuery, activeFilterTab) {
        items.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.qrCode.contains(searchQuery, ignoreCase = true) ||
                item.barcode.contains(searchQuery, ignoreCase = true)
            val matchesTab = when (activeFilterTab) {
                "Выбранные" -> item.isSelectedForPublish
                "Черновики" -> item.publishedPlatforms.isEmpty()
                "Ошибки" -> item.publicationStatus == "error"
                else -> true
            }
            matchesSearch && matchesTab
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header: ПУБЛИКАЦИЯ | Активных площадок: 4
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ПУБЛИКАЦИЯ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Активных площадок: $activePlatformsCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Платформы для выгрузки",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
                                platformName = pType.displayName,
                                isConnected = false,
                                isSelectedForUpload = true
                            )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!conn.isConnected) {
                                        onConfigurePlatform(conn)
                                    } else {
                                        onTogglePlatformUpload(conn)
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            PlatformBadgeIcon(
                                platformType = pType,
                                size = 56.dp,
                                showCheckmark = true,
                                isConnected = conn.isConnected && conn.isSelectedForUpload
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = pType.displayName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Big 1-Click Publish Selected Button matching Screenshot 2
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OneClickPublishGradientButton(
                    title = "ОПУБЛИКОВАТЬ ВЫБРАННОЕ ($selectedCount)",
                    subtitle = "на $activePlatformsCount платформы сразу",
                    isPublishing = publishProgress.isPublishing,
                    onClick = onPublishSelected
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

        // Search Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Найти товар для выгрузки...",
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск")
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
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Параметры выгрузки",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Filter Chips: Все товары | Выбранные (N) | Черновики | Ошибки
        item {
            val tabs = listOf(
                "Все товары" to "Все товары",
                "Выбранные" to "Выбранные ($selectedCount)",
                "Черновики" to "Черновики",
                "Ошибки" to "Ошибки"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { (key, label) ->
                    val isSelected = activeFilterTab == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) GarageOrange.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GarageOrange else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { activeFilterTab = key }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) GarageOrange else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Selectable Item Cards matching Screenshot 2
        if (filteredItems.isEmpty()) {
            item {
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Нет товаров для публикации",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Добавьте товары в разделе «Склад» или по кнопке ниже, чтобы выгружать их на Авито, Юлу, ВК и Яндекс в 1 клик.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onAddItemClick,
                            colors = ButtonDefaults.buttonColors(containerColor = GarageOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Добавить товар для выгрузки", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                PublishSelectableItemCard(
                    item = item,
                    onToggleSelect = { onToggleItemSelection(item) },
                    onCardClick = { onEditItemClick(item) },
                    onPhotoHintClick = { onPhotoHintClick(item) }
                )
            }
        }
    }
}

@Composable
private fun PublishSelectableItemCard(
    item: GarageItem,
    onToggleSelect: () -> Unit,
    onCardClick: () -> Unit,
    onPhotoHintClick: () -> Unit
) {
    val isPublished = item.publishedPlatforms.isNotEmpty()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (item.isSelectedForPublish) GarageOrange.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onCardClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Circular checkbox on the left matching Screenshot 2
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.isSelectedForPublish) Color(0xFFD6C2A8)
                        else Color.Transparent
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (item.isSelectedForPublish) Color(0xFFD6C2A8) else Color(0xFF64748B),
                        shape = CircleShape
                    )
                    .clickable(onClick = onToggleSelect)
                    .testTag("publish_checkbox_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (item.isSelectedForPublish) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Выбрано",
                        tint = Color(0xFF181C26),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Item thumbnail
            ItemThumbnailBox(
                photoUri = item.photoUri,
                title = item.title,
                size = 86.dp
            )

            // Item info column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatRubles(item.price),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GarageEmeraldGreen
                    )
                    Text(
                        text = " • ${item.quantity} ${item.unitLabel}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
                        fontSize = 12.sp,
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
                        text = "QR: ${item.qrCode}  Штрихкод: ${item.barcode.take(7)}...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (item.barcode.isNotBlank()) {
                        MiniBarcodeCanvas(barcode = item.barcode, width = 42.dp, height = 14.dp)
                    }
                }

                PhotoHintChip(
                    hasHintPhoto = item.hintPhotoUri.isNotBlank(),
                    hintNote = item.hintNote,
                    labelText = "Фото-подсказка (4/4)",
                    onClick = onPhotoHintClick
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPublished) GarageEmeraldGreen else Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isPublished) "Опубликовано везде" else "Готов к выгрузке",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        PlatformType.entries.forEach { pt ->
                            PlatformBadgeIcon(
                                platformType = pt,
                                size = 18.dp,
                                showCheckmark = true,
                                isConnected = item.publishedPlatforms.contains(pt.key)
                            )
                        }
                    }
                }
            }
        }
    }
}
