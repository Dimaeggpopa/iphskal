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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.GarageItem
import com.example.data.model.PlatformType
import com.example.data.model.StorageZone
import com.example.ui.components.ItemThumbnailBox
import com.example.ui.components.PhotoHintChip
import com.example.ui.components.PlatformBadgeIcon
import com.example.ui.components.formatRubles
import com.example.ui.theme.GarageAmberYellow
import com.example.ui.theme.GarageElectricBlueBright
import com.example.ui.theme.GarageEmeraldGreen
import com.example.ui.theme.GarageOrange

@Composable
fun WarehouseScreen(
    items: List<GarageItem>,
    customZones: List<StorageZone>,
    onScanQrClick: () -> Unit,
    onAddItemClick: () -> Unit,
    onItemClick: (GarageItem) -> Unit,
    onPhotoHintClick: (GarageItem) -> Unit,
    onToggleInventoryCheck: (GarageItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedZoneFilter by remember { mutableStateOf("Все места") }
    var isInventoryAuditMode by remember { mutableStateOf(false) }
    var sortByPriceDesc by remember { mutableStateOf(false) }

    val zoneTabs = remember(customZones, items) {
        val defaultZones = listOf("Все места", "Гараж", "Склад 1", "Ангар", "Стеллаж А")
        val extra = customZones.map { it.name } + items.map { it.zone }
        (defaultZones + extra).filter { it.isNotBlank() }.distinct()
    }

    val totalCapacity = remember(customZones) {
        if (customZones.isEmpty()) 192L else customZones.sumOf { it.capacity }
    }
    val freeSlots = (totalCapacity - items.size).coerceAtLeast(0L)

    val filteredItems = remember(items, searchQuery, selectedZoneFilter, sortByPriceDesc) {
        val base = items.filter { item ->
            val matchesZone = selectedZoneFilter == "Все места" ||
                item.zone.equals(selectedZoneFilter, ignoreCase = true) ||
                item.shelfLocation.contains(selectedZoneFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.qrCode.contains(searchQuery, ignoreCase = true) ||
                item.barcode.contains(searchQuery, ignoreCase = true) ||
                item.serialCode.contains(searchQuery, ignoreCase = true) ||
                item.shelfLocation.contains(searchQuery, ignoreCase = true) ||
                item.zone.contains(searchQuery, ignoreCase = true)
            matchesZone && matchesSearch
        }
        if (sortByPriceDesc) base.sortedByDescending { it.price } else base
    }

    val verifiedCount = remember(items) { items.count { it.isVerifiedInInventory } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header: МОЙ СКЛАД | Всего: X товара • Свободно Y мест
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "МОЙ СКЛАД",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Всего: ${items.size} товара • Свободно $freeSlots мест",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                            text = "Поиск по названию, QR, штрихкоду, месту...",
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                        .background(
                            if (sortByPriceDesc) GarageOrange.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (sortByPriceDesc) GarageOrange else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { sortByPriceDesc = !sortByPriceDesc },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Сортировка по цене",
                        tint = if (sortByPriceDesc) GarageOrange else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Zone Filter Chips matching Screenshot 4
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                zoneTabs.forEach { tab ->
                    val isSelected = selectedZoneFilter == tab
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
                            .clickable { selectedZoneFilter = tab }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) GarageOrange else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3 Action Cards: Сканировать QR / штрихкод | Добавить товар | Инвентаризация
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Scan QR card with glowing blue border matching Screenshot 4
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GarageElectricBlueBright.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(86.dp)
                        .border(1.5.dp, GarageElectricBlueBright, RoundedCornerShape(16.dp))
                        .clickable(onClick = onScanQrClick)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Сканировать",
                            tint = GarageElectricBlueBright,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Сканировать\nQR / штрихкод",
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Add Item card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(86.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .clickable(onClick = onAddItemClick)
                        .testTag("warehouse_add_item_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить товар",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Добавить товар",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Inventory Check card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isInventoryAuditMode) {
                            GarageEmeraldGreen.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(86.dp)
                        .border(
                            width = if (isInventoryAuditMode) 1.5.dp else 1.dp,
                            color = if (isInventoryAuditMode) GarageEmeraldGreen else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { isInventoryAuditMode = !isInventoryAuditMode }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = "Инвентаризация",
                            tint = GarageEmeraldGreen,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Инвентаризация",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Interactive Inventory Audit Mode Banner
        item {
            AnimatedVisibility(visible = isInventoryAuditMode) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GarageEmeraldGreen.copy(alpha = 0.14f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GarageEmeraldGreen, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Режим ревизии склада активен",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GarageEmeraldGreen
                            )
                            Text(
                                text = "Проверено: $verifiedCount / ${items.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (items.isEmpty()) 0f else verifiedCount.toFloat() / items.size
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = GarageEmeraldGreen
                        )
                    }
                }
            }
        }

        // Warehouse Item List or Empty State
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
                            text = "Склад пока пуст",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Добавьте товары с их местоположением на полке, QR-кодом, штрихкодом и загрузите фото-подсказку для быстрого поиска.",
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
                            Text("Внести товар на склад", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredItems, key = { it.id }) { item ->
                WarehouseDetailedItemCard(
                    item = item,
                    showInventoryCheck = isInventoryAuditMode,
                    onClick = { onItemClick(item) },
                    onPhotoHintClick = { onPhotoHintClick(item) },
                    onToggleInventoryCheck = { onToggleInventoryCheck(item) }
                )
            }
        }
    }
}

@Composable
private fun WarehouseDetailedItemCard(
    item: GarageItem,
    showInventoryCheck: Boolean,
    onClick: () -> Unit,
    onPhotoHintClick: () -> Unit,
    onToggleInventoryCheck: () -> Unit
) {
    val pubCount = item.publishedPlatforms.size
    val statusDotColor = when {
        pubCount >= 4 -> GarageEmeraldGreen
        pubCount > 0 -> GarageAmberYellow
        else -> Color(0xFF64748B)
    }
    val statusText = when {
        pubCount >= 4 -> "Опубликовано везде (4/4)"
        pubCount > 0 -> "Опубликовано ($pubCount/4)"
        else -> "Не опубликовано (0/4)"
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left column: Main Thumbnail + optional secondary shelf photo preview below it (matching Screenshot 4)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ItemThumbnailBox(
                    photoUri = item.photoUri,
                    title = item.title,
                    size = 90.dp
                )
                if (item.hintPhotoUri.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF3E4759), RoundedCornerShape(8.dp))
                            .clickable(onClick = onPhotoHintClick)
                    ) {
                        AsyncImage(
                            model = item.hintPhotoUri,
                            contentDescription = "Миниатюра полки",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Right column: Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (showInventoryCheck) {
                        IconButton(
                            onClick = onToggleInventoryCheck,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (item.isVerifiedInInventory) {
                                    Icons.Default.CheckCircle
                                } else {
                                    Icons.Default.RadioButtonUnchecked
                                },
                                contentDescription = "Проверено",
                                tint = if (item.isVerifiedInInventory) GarageEmeraldGreen else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Price + quantity row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatRubles(item.price),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GarageEmeraldGreen
                    )
                    Text(
                        text = " • ${item.quantity} ${item.unitLabel}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Location pin row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.zone}, ${item.shelfLocation}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // QR + Barcode + Serial code
                Text(
                    text = buildString {
                        append("QR: ${item.qrCode}")
                        if (item.barcode.isNotBlank()) {
                            append("   Штрихкод: ${item.barcode.take(10)}...")
                        }
                        if (item.serialCode.isNotBlank()) {
                            append("   ID: ${item.serialCode}")
                        }
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Photo Hint Chip
                PhotoHintChip(
                    hasHintPhoto = item.hintPhotoUri.isNotBlank(),
                    hintNote = item.hintNote,
                    labelText = "Фото-подсказка (место на полке)",
                    onClick = onPhotoHintClick
                )

                // Bottom row: Status + Platform badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val platformsToShow = if (item.publishedPlatforms.isNotEmpty()) {
                            item.publishedPlatforms.map { PlatformType.fromKey(it) }
                        } else {
                            emptyList()
                        }
                        platformsToShow.forEach { pt ->
                            PlatformBadgeIcon(platformType = pt, size = 20.dp)
                        }
                    }
                }
            }
        }
    }
}
