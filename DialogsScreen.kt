package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BuyerDialog
import com.example.data.model.GarageItem
import com.example.data.model.PlatformType
import com.example.ui.components.ItemThumbnailBox
import com.example.ui.components.PlatformBadgeIcon
import com.example.ui.components.formatRubles
import com.example.ui.theme.GarageElectricBlueBright
import com.example.ui.theme.GarageEmeraldGreen
import com.example.ui.theme.GarageOrange

@Composable
fun DialogsScreen(
    dialogs: List<BuyerDialog>,
    items: List<GarageItem>,
    quickReplyTemplates: List<String>,
    onNewDialogClick: () -> Unit,
    onSendMessage: (BuyerDialog, String, Boolean) -> Unit,
    onMarkRead: (BuyerDialog) -> Unit,
    onDeleteDialog: (String) -> Unit,
    onOpenItemPhotoHint: (GarageItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlatformFilter by remember { mutableStateOf("all") }
    var openedDialogId by remember { mutableStateOf<String?>(null) }

    val activeDialog = remember(dialogs, openedDialogId) {
        dialogs.find { it.id == openedDialogId }
    }

    val unreadTotal = remember(dialogs) { dialogs.sumOf { it.unreadCount }.toInt() }

    val filteredDialogs = remember(dialogs, searchQuery, selectedPlatformFilter) {
        dialogs.filter { d ->
            val matchesPlatform = selectedPlatformFilter == "all" || d.platformKey == selectedPlatformFilter
            val matchesSearch = searchQuery.isBlank() ||
                d.buyerName.contains(searchQuery, ignoreCase = true) ||
                d.itemTitle.contains(searchQuery, ignoreCase = true) ||
                d.lastMessage.contains(searchQuery, ignoreCase = true)
            matchesPlatform && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header: ДИАЛОГИ | X непрочитанных
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ДИАЛОГИ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "$unreadTotal непрочитанных",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(GarageOrange.copy(alpha = 0.18f))
                            .border(1.dp, GarageOrange, RoundedCornerShape(10.dp))
                            .clickable(onClick = onNewDialogClick)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("add_dialog_button")
                    ) {
                        Text(
                            text = "+ Диалог",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
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
                            text = "Поиск по покупателям, товарам...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
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
                        contentDescription = "Фильтр диалогов",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Platform Filter Pills matching Screenshot 1
        item {
            val avitoCount = dialogs.count { it.platformKey == "avito" }
            val youlaCount = dialogs.count { it.platformKey == "youla" }
            val vkCount = dialogs.count { it.platformKey == "vk" }
            val yandexCount = dialogs.count { it.platformKey == "yandex" }

            val filterOptions = listOf(
                "all" to "Все (${dialogs.size})",
                "avito" to "Авито ($avitoCount)",
                "youla" to "Юла ($youlaCount)",
                "vk" to "ВК Маркет ($vkCount)",
                "yandex" to "Яндекс ($yandexCount)"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (key, label) ->
                    val isSelected = selectedPlatformFilter == key
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
                            .clickable { selectedPlatformFilter = key }
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

        // Dialog List or Empty State
        if (filteredDialogs.isEmpty()) {
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
                            text = "Диалогов пока нет",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Здесь собираются сообщения от покупателей с Авито, Юлы, ВК Маркета и Яндекс.Объявлений в едином удобном окне с подсказкой стеллажа и QR-кода товара.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onNewDialogClick,
                            colors = ButtonDefaults.buttonColors(containerColor = GarageOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Добавить диалог с покупателем", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredDialogs, key = { it.id }) { dialog ->
                BuyerDialogRowCard(
                    dialog = dialog,
                    onClick = {
                        onMarkRead(dialog)
                        openedDialogId = dialog.id
                    }
                )
            }
        }
    }

    // Compact Phone-Friendly Chat Modal when a dialog is tapped
    if (activeDialog != null) {
        val linkedItem = items.find { it.id == activeDialog.itemId }
        CompactPhoneChatModal(
            dialog = activeDialog,
            linkedItem = linkedItem,
            quickReplyTemplates = quickReplyTemplates,
            onDismiss = { openedDialogId = null },
            onSendMessage = { text, isFromMe ->
                onSendMessage(activeDialog, text, isFromMe)
            },
            onDeleteDialog = {
                onDeleteDialog(activeDialog.id)
                openedDialogId = null
            },
            onOpenPhotoHint = {
                if (linkedItem != null) {
                    onOpenItemPhotoHint(linkedItem)
                }
            }
        )
    }
}

/**
 * Compact, ergonomic Dialog Card matching Screenshot 1.
 */
@Composable
private fun BuyerDialogRowCard(
    dialog: BuyerDialog,
    onClick: () -> Unit
) {
    val platformType = PlatformType.fromKey(dialog.platformKey)

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Buyer Avatar with Unread Dot (top-right) and Platform Badge (bottom-right)
            Box(modifier = Modifier.size(58.dp)) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
                            )
                        )
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dialog.buyerName.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Unread blue dot at top right
                if (dialog.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                            .size(13.dp)
                            .clip(CircleShape)
                            .background(GarageElectricBlueBright)
                            .border(2.dp, Color(0xFF232732), CircleShape)
                    )
                }

                // Platform badge at bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                ) {
                    PlatformBadgeIcon(
                        platformType = platformType,
                        size = 22.dp
                    )
                }
            }

            // Center text: Name, Message, Platform • Item Title
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dialog.buyerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dialog.timeLabel,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = dialog.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${platformType.shortName} • ${dialog.itemTitle}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right product thumbnail
            ItemThumbnailBox(
                photoUri = dialog.itemPhotoUri,
                title = dialog.itemTitle,
                size = 54.dp,
                cornerRadius = 12.dp
            )
        }
    }
}

/**
 * Compact, phone-friendly chat window ("окошки не особо большие, а удобные для телефона")
 * with instant shelf location & QR lookup bar right at the top of the conversation.
 */
@Composable
private fun CompactPhoneChatModal(
    dialog: BuyerDialog,
    linkedItem: GarageItem?,
    quickReplyTemplates: List<String>,
    onDismiss: () -> Unit,
    onSendMessage: (String, Boolean) -> Unit,
    onDeleteDialog: () -> Unit,
    onOpenPhotoHint: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    var messageInput by remember { mutableStateOf("") }
    val platformType = PlatformType.fromKey(dialog.platformKey)
    val messages = remember(dialog.messagesLog) { dialog.parsedMessages() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 520.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // iOS Sheet Grabber Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                        .width(38.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.28f))
                )

                // Compact Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlatformBadgeIcon(platformType = platformType, size = 30.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${dialog.buyerName} (${platformType.shortName})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = dialog.itemTitle,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onDeleteDialog, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Удалить диалог",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Instant Warehouse Shelf & QR Strip right inside the chat!
                val locationStr = linkedItem?.let { "${it.zone}, ${it.shelfLocation}" }
                    ?: dialog.itemShelfLocation.ifBlank { "Стеллаж А1" }
                val qrStr = linkedItem?.qrCode ?: dialog.itemQrCode.ifBlank { "#GAR-084" }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GarageOrange.copy(alpha = 0.14f))
                        .clickable { onOpenPhotoHint() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = GarageOrange,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$locationStr • $qrStr",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GarageOrange,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (linkedItem != null) {
                            Text(
                                text = "Фото полки →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0A84FF)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Compact iOS iMessage-style Message List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 240.dp)
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isOutgoing) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 240.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (msg.isOutgoing) 16.dp else 4.dp,
                                            bottomEnd = if (msg.isOutgoing) 4.dp else 16.dp
                                        )
                                    )
                                    .background(
                                        if (msg.isOutgoing) Color(0xFF0A84FF)
                                        else Color(0xFF2C2C2E)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = msg.time,
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.72f),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Reply Chips (1-tap phone replies)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickReplyTemplates.forEach { tpl ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                .clickable {
                                    onSendMessage(tpl, true)
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = tpl,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Compact Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Написать ответ...", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                onSendMessage(messageInput, true)
                                messageInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GarageOrange)
                            .testTag("send_chat_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Отправить",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
