package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Quickreply
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GarageItem
import com.example.data.model.PlatformConnection
import com.example.data.model.StorageZone
import com.example.ui.components.formatRubles
import com.example.ui.theme.AppAccentPalette
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.GarageElectricBlueBright
import com.example.ui.theme.GarageEmeraldGreen
import com.example.ui.theme.GarageOrange

@Composable
fun MoreScreen(
    items: List<GarageItem>,
    platforms: List<PlatformConnection>,
    zones: List<StorageZone>,
    themeMode: AppThemeMode,
    accentPalette: AppAccentPalette,
    quickReplyTemplates: List<String>,
    userEmail: String?,
    onSelectThemeMode: (AppThemeMode) -> Unit,
    onSelectAccentPalette: (AppAccentPalette) -> Unit,
    onSaveZone: (String, Long, String) -> Unit,
    onDeleteZone: (String) -> Unit,
    onAddQuickReply: (String) -> Unit,
    onRemoveQuickReply: (String) -> Unit,
    onOpenPhotoHint: (GarageItem) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var newZoneName by remember { mutableStateOf("") }
    var newZoneCapacity by remember { mutableStateOf("50") }
    var newQuickReply by remember { mutableStateOf("") }
    var showEmergencyRadar by remember { mutableStateOf(false) }

    val totalValue = remember(items) { items.sumOf { it.price * it.quantity } }
    val totalUnits = remember(items) { items.sumOf { it.quantity } }
    val publishedCount = remember(items) { items.count { it.publishedPlatforms.isNotEmpty() } }
    var showIosProjectGuide by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Free iOS / iPhone Edition Badge + Native SwiftUI / .ipa Project Inspector
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF30D158).copy(alpha = 0.14f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF30D158), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Мой гараж • Нативный проект для iPhone (iOS / SwiftUI / .ipa)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Все файлы для iOS созданы в папке /ios (SwiftUI, Info.plist, GoogleService-Info.plist, XcodeGen project.yml, build_ipa.sh и автосборка .ipa через GitHub Actions)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showIosProjectGuide = !showIosProjectGuide },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_ios_files_guide_button")
                        ) {
                            Text(
                                text = if (showIosProjectGuide) "Скрыть файлы iOS" else "Файлы iOS (.ipa)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val iosPackage = buildString {
                                    appendLine("🍏 Мой гараж — Нативный проект для iPhone (iOS / SwiftUI / .ipa)")
                                    appendLine("Папка проекта: /ios/MyGarageIOS")
                                    appendLine("Скрипт сборки IPA: /ios/build_ipa.sh")
                                    appendLine("Автосборка IPA без Mac: /.github/workflows/build_ios_ipa.yml")
                                    appendLine("Аккаунт облака: ${userEmail ?: "Google / Apple Cloud Sync"}")
                                    appendLine("Товаров в гараже: ${items.size} шт. (${formatRubles(totalValue)})")
                                    appendLine("")
                                    appendLine("Реестр товаров:")
                                    items.forEachIndexed { idx, item ->
                                        appendLine("${idx + 1}. ${item.title} — ${formatRubles(item.price)} | 📍 ${item.zone}, ${item.shelfLocation} | QR: ${item.qrCode}")
                                    }
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Мой гараж — Проект и данные для iPhone (iOS)")
                                    putExtra(Intent.EXTRA_TEXT, iosPackage)
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "Отправить данные на iPhone (AirDrop / Telegram / iCloud)")
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_ios_package_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = Color(0xFF0A84FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "На iPhone",
                                color = Color(0xFF0A84FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (showIosProjectGuide) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "📂 Созданные нативные файлы iOS в проекте:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF30D158)
                            )
                            Text(
                                text = "• /ios/project.yml и /ios/Package.swift — конфигурация Xcode 16 и SwiftPM\n" +
                                    "• /ios/MyGarageIOS/Info.plist и GoogleService-Info.plist — ключи Firebase и камера iPhone\n" +
                                    "• /ios/MyGarageIOS/MyGarageApp.swift — точка входа SwiftUI + Google Sign-In\n" +
                                    "• /ios/MyGarageIOS/Models/GarageModels.swift — модели склада, площадок, чатов и зон\n" +
                                    "• /ios/MyGarageIOS/Services/GarageRepository.swift — синхронизация с вашей БД Firestore\n" +
                                    "• /ios/MyGarageIOS/ViewModels/GarageViewModel.swift — публикация в 1 клик и QR-генератор\n" +
                                    "• /ios/MyGarageIOS/Views/*.swift — экраны Главная, Склад, Выгрузка, Диалоги, Ещё и шторки\n" +
                                    "• /ios/build_ipa.sh и /.github/workflows/build_ios_ipa.yml — сборка MyGarageIOS.ipa",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                            Text(
                                text = "📲 Как получить .ipa для iPhone (даже без Mac): нажмите Export to GitHub или Export ZIP в меню AI Studio — GitHub Actions автоматически соберёт готовый MyGarageIOS.ipa для установки через AltStore / Sideloadly / Xcode.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 1. Theme Management ("управление темами тоже добавь в ещё") with iOS Segmented Control
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Темы",
                            tint = GarageOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Управление темами (iOS Оформление)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Режим темы (Cupertino Segmented Control):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // iOS Segmented Control container
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppThemeMode.entries.forEach { mode ->
                            val selected = themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(
                                        if (selected) GarageOrange
                                        else Color.Transparent
                                    )
                                    .clickable { onSelectThemeMode(mode) }
                                    .padding(vertical = 9.dp, horizontal = 6.dp)
                                    .testTag("theme_mode_${mode.key}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Text(
                        text = "Цветовой акцент интерфейса:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppAccentPalette.entries.forEach { palette ->
                            val selected = accentPalette == palette
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) palette.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectAccentPalette(palette) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(palette.primary)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = palette.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Выбрано",
                                        tint = palette.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Emergency Location Radar ("чтобы точно не запутаться где находится товар в чрезвычайных ситуациях")
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GarageOrange.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = null,
                                tint = GarageOrange
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Экстренный радар полок и фото-подсказок",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Быстрый поиск где лежит каждый товар без путаницы",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { showEmergencyRadar = !showEmergencyRadar },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (showEmergencyRadar) "Скрыть" else "Открыть", fontSize = 12.sp)
                        }
                    }

                    if (showEmergencyRadar) {
                        if (items.isEmpty()) {
                            Text(
                                text = "Сначала добавьте товары с местами хранения и фото-подсказками.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable { onOpenPhotoHint(item) }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = "📍 ${item.zone} → ${item.shelfLocation} (QR: ${item.qrCode})",
                                            fontSize = 12.sp,
                                            color = GarageOrange,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (item.hintNote.isNotBlank()) {
                                            Text(
                                                text = "Ориентир: ${item.hintNote}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Открыть фото полки",
                                        tint = GarageOrange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Warehouse Financial & Analytics Summary
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = GarageElectricBlueBright
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Финансовая сводка гаража и экспорт",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatMiniCard(
                            label = "Сумма товаров",
                            value = formatRubles(totalValue),
                            modifier = Modifier.weight(1.2f)
                        )
                        StatMiniCard(
                            label = "Единиц на складе",
                            value = "$totalUnits шт.",
                            modifier = Modifier.weight(1f)
                        )
                        StatMiniCard(
                            label = "В рекламе",
                            value = "$publishedCount / ${items.size}",
                            modifier = Modifier.weight(0.9f)
                        )
                    }

                    Button(
                        onClick = {
                            val report = buildString {
                                appendLine("=== РЕЕСТР СКЛАДА «МОЙ ГАРАЖ» ===")
                                appendLine("Позиций: ${items.size} | Общая стоимость: ${formatRubles(totalValue)}")
                                appendLine("---------------------------------")
                                items.forEachIndexed { idx, it ->
                                    appendLine(
                                        "${idx + 1}. ${it.title} — ${formatRubles(it.price)} (${it.quantity} ${it.unitLabel})"
                                    )
                                    appendLine("   Место: ${it.zone}, ${it.shelfLocation}")
                                    appendLine("   QR: ${it.qrCode} | Штрихкод: ${it.barcode.ifBlank { "—" }} | ID: ${it.serialCode.ifBlank { "—" }}")
                                    if (it.hintNote.isNotBlank()) {
                                        appendLine("   Подсказка: ${it.hintNote}")
                                    }
                                }
                            }
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(
                                Intent.createChooser(sendIntent, "Экспорт реестра склада")
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GarageElectricBlueBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Экспортировать полный реестр склада (CSV / Текст)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 4. Storage Zones & Capacity Constructor
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = GarageEmeraldGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Конструктор мест и стеллажей",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newZoneName,
                            onValueChange = { newZoneName = it },
                            label = { Text("Новое место (напр. Бокс 2)") },
                            singleLine = true,
                            modifier = Modifier.weight(1.4f)
                        )
                        OutlinedTextField(
                            value = newZoneCapacity,
                            onValueChange = { newZoneCapacity = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Мест") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                        IconButton(
                            onClick = {
                                if (newZoneName.isNotBlank()) {
                                    onSaveZone(
                                        newZoneName.trim(),
                                        newZoneCapacity.toLongOrNull() ?: 50L,
                                        "Пользовательская зона хранения"
                                    )
                                    newZoneName = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GarageEmeraldGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить зону", tint = Color.White)
                        }
                    }

                    zones.forEach { z ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${z.name} • Вместимость: ${z.capacity} мест", fontSize = 13.sp)
                            IconButton(
                                onClick = { onDeleteZone(z.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Quick Reply Templates Manager
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Quickreply,
                            contentDescription = null,
                            tint = GarageOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Шаблоны быстрых ответов в чатах",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newQuickReply,
                            onValueChange = { newQuickReply = it },
                            label = { Text("Новая быстрая фраза для покупателей") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (newQuickReply.isNotBlank()) {
                                    onAddQuickReply(newQuickReply)
                                    newQuickReply = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GarageOrange)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить", tint = Color.White)
                        }
                    }

                    quickReplyTemplates.forEach { tpl ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tpl,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onRemoveQuickReply(tpl) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить шаблон",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Account & Sign Out
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Облачный профиль синхронизирован",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = userEmail ?: "Google Аккаунт",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Выйти",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Выйти", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
