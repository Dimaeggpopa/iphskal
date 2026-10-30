package com.example.ui.modals

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.BuyerDialog
import com.example.data.model.GarageItem
import com.example.data.model.PlatformConnection
import com.example.data.model.PlatformType
import com.example.data.repository.GarageRepository
import com.example.ui.components.CupertinoSheetHeader
import com.example.ui.components.MiniBarcodeCanvas
import com.example.ui.components.MiniQrCodeCanvas
import com.example.ui.components.PlatformBadgeIcon
import com.example.ui.components.formatRubles
import com.example.ui.theme.GarageElectricBlueBright
import com.example.ui.theme.GarageEmeraldGreen
import com.example.ui.theme.GarageOrange

/**
 * Compact phone-friendly modal to Add or Edit a Garage Item with QR, Barcode, Shelf Location,
 * and Emergency Photo Hint using Android's zero-permission PickVisualMedia.
 */
@Composable
fun AddOrEditItemDialog(
    initialItem: GarageItem? = null,
    availableZones: List<String>,
    onDismiss: () -> Unit,
    onSave: (GarageItem) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var priceText by remember {
        mutableStateOf(if ((initialItem?.price ?: 0L) > 0L) initialItem!!.price.toString() else "")
    }
    var quantityText by remember {
        mutableStateOf((initialItem?.quantity ?: 1L).toString())
    }
    var unitLabel by remember { mutableStateOf(initialItem?.unitLabel ?: "шт.") }
    val zoneOptions = remember(availableZones) {
        (listOf("Гараж", "Склад 1", "Ангар", "Стеллаж А") + availableZones).distinct()
    }
    var selectedZone by remember { mutableStateOf(initialItem?.zone ?: zoneOptions.first()) }
    var shelfLocation by remember {
        mutableStateOf(initialItem?.shelfLocation ?: "Стеллаж А3, полка 2")
    }
    var qrCode by remember {
        mutableStateOf(initialItem?.qrCode?.ifBlank { null } ?: GarageRepository.generateQrCode())
    }
    var barcode by remember {
        mutableStateOf(initialItem?.barcode?.ifBlank { null } ?: GarageRepository.generateBarcode())
    }
    var serialCode by remember { mutableStateOf(initialItem?.serialCode ?: "") }
    var description by remember { mutableStateOf(initialItem?.description ?: "") }
    var photoUri by remember { mutableStateOf(initialItem?.photoUri ?: "") }
    var hintPhotoUri by remember { mutableStateOf(initialItem?.hintPhotoUri ?: "") }
    var hintNote by remember { mutableStateOf(initialItem?.hintNote ?: "") }

    val mainPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            photoUri = uri.toString()
        }
    }

    val hintPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            hintPhotoUri = uri.toString()
        }
    }

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
                .fillMaxWidth(0.94f)
                .heightIn(max = 620.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // iOS Cupertino Sheet Header with Grabber Pill + Отмена / Готово
                CupertinoSheetHeader(
                    title = if (initialItem == null) "Новый товар" else "Товар на складе",
                    cancelText = "Отмена",
                    doneText = "Готово",
                    doneEnabled = title.isNotBlank(),
                    onCancel = onDismiss,
                    onDone = {
                        val parsedPrice = priceText.toLongOrNull() ?: 0L
                        val parsedQty = (quantityText.toLongOrNull() ?: 1L).coerceAtLeast(1L)
                        val updated = (initialItem ?: GarageItem()).copy(
                            title = title.trim(),
                            description = description.trim(),
                            price = parsedPrice,
                            quantity = parsedQty,
                            unitLabel = unitLabel.trim().ifBlank { "шт." },
                            zone = selectedZone,
                            shelfLocation = shelfLocation.trim().ifBlank { "Стеллаж А1" },
                            qrCode = qrCode.trim().ifBlank { GarageRepository.generateQrCode() },
                            barcode = barcode.trim().ifBlank { GarageRepository.generateBarcode() },
                            serialCode = serialCode.trim(),
                            photoUri = photoUri,
                            hintPhotoUri = hintPhotoUri,
                            hintNote = hintNote.trim()
                        )
                        onSave(updated)
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.outline
                )

                // Scrollable body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Название товара *") },
                        placeholder = { Text("Напр.: Набор инструментов 150 предм.") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_title_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Цена (₽) *") },
                            placeholder = { Text("8900") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("item_price_input")
                        )
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Кол-во") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(0.8f)
                        )
                        OutlinedTextField(
                            value = unitLabel,
                            onValueChange = { unitLabel = it },
                            label = { Text("Ед.") },
                            placeholder = { Text("шт.") },
                            singleLine = true,
                            modifier = Modifier.weight(0.9f)
                        )
                    }

                    // Zone selector chips
                    Text(
                        text = "Зона хранения:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        zoneOptions.forEach { z ->
                            val selected = selectedZone == z
                            FilterChip(
                                selected = selected,
                                onClick = { selectedZone = z },
                                label = { Text(z, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GarageOrange.copy(alpha = 0.2f),
                                    selectedLabelColor = GarageOrange
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = shelfLocation,
                        onValueChange = { shelfLocation = it },
                        label = { Text("Точное место (стеллаж, полка, сектор)") },
                        placeholder = { Text("Стеллаж А3, полка 2") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // QR & Barcode row with auto-generate button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = qrCode,
                            onValueChange = { qrCode = it },
                            label = { Text("QR-код") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Штрихкод") },
                            singleLine = true,
                            modifier = Modifier.weight(1.1f)
                        )
                        IconButton(
                            onClick = {
                                qrCode = GarageRepository.generateQrCode()
                                barcode = GarageRepository.generateBarcode()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GarageOrange.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Сгенерировать коды",
                                tint = GarageOrange
                            )
                        }
                    }

                    OutlinedTextField(
                        value = serialCode,
                        onValueChange = { serialCode = it },
                        label = { Text("Цифры / артикул / серийный номер") },
                        placeholder = { Text("Напр.: SN-08492 / Партия 4") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Photo & Emergency Hint Photo Pickers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Main Photo button
                        OutlinedButton(
                            onClick = {
                                mainPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Фото товара",
                                modifier = Modifier.size(18.dp),
                                tint = if (photoUri.isNotBlank()) GarageEmeraldGreen else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (photoUri.isNotBlank()) "Фото загружено" else "Фото товара",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Emergency Hint Photo button
                        OutlinedButton(
                            onClick = {
                                hintPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Фото-подсказка",
                                modifier = Modifier.size(18.dp),
                                tint = if (hintPhotoUri.isNotBlank()) GarageEmeraldGreen else GarageOrange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hintPhotoUri.isNotBlank()) "Подсказка есть" else "Фото-подсказка",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedTextField(
                        value = hintNote,
                        onValueChange = { hintNote = it },
                        label = { Text("Подсказка для ЧС (как быстро найти в темноте/спешке)") },
                        placeholder = { Text("Напр.: У ворот справа, 2-я полка, оранжевый кейс") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Описание для объявления (Авито / Юла / ВК / Яндекс)") },
                        placeholder = { Text("Состояние, комплектация, условия самовывоза...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (initialItem != null && onDelete != null) {
                        TextButton(
                            onClick = {
                                onDelete(initialItem.id)
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Удалить", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("Отмена")
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button
                            val parsedPrice = priceText.toLongOrNull() ?: 0L
                            val parsedQty = (quantityText.toLongOrNull() ?: 1L).coerceAtLeast(1L)
                            val updated = (initialItem ?: GarageItem()).copy(
                                title = title.trim(),
                                description = description.trim(),
                                price = parsedPrice,
                                quantity = parsedQty,
                                unitLabel = unitLabel.trim().ifBlank { "шт." },
                                zone = selectedZone,
                                shelfLocation = shelfLocation.trim().ifBlank { "Стеллаж А1" },
                                qrCode = qrCode.trim().ifBlank { GarageRepository.generateQrCode() },
                                barcode = barcode.trim().ifBlank { GarageRepository.generateBarcode() },
                                serialCode = serialCode.trim(),
                                photoUri = photoUri,
                                hintPhotoUri = hintPhotoUri,
                                hintNote = hintNote.trim()
                            )
                            onSave(updated)
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GarageOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_item_button")
                    ) {
                        Text(
                            text = if (initialItem == null) "Сохранить на склад" else "Обновить",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Emergency Location Photo Hint Modal ("Фото-подсказка") — shows exact physical spot,
 * allows uploading/updating the shelf photo, and includes high-contrast flashlight screen mode.
 */
@Composable
fun PhotoHintModal(
    item: GarageItem,
    onDismiss: () -> Unit,
    onUpdateHintPhoto: (String, String) -> Unit
) {
    val context = LocalContext.current
    var hintUri by remember { mutableStateOf(item.hintPhotoUri) }
    var hintNote by remember { mutableStateOf(item.hintNote) }
    var isBeaconBright by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            hintUri = uri.toString()
            onUpdateHintPhoto(hintUri, hintNote)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isBeaconBright) Color.White else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, GarageOrange, RoundedCornerShape(20.dp))
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
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Фото-подсказка",
                            tint = GarageOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Фото-подсказка места",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isBeaconBright) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { isBeaconBright = !isBeaconBright }) {
                        Icon(
                            imageVector = Icons.Default.FlashlightOn,
                            contentDescription = "Подсветка экрана",
                            tint = if (isBeaconBright) GarageOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Exact location badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GarageOrange.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            color = if (isBeaconBright) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Место",
                                tint = GarageOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${item.zone} • ${item.shelfLocation}",
                                fontWeight = FontWeight.Bold,
                                color = GarageOrange,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "QR: ${item.qrCode}   Штрихкод: ${item.barcode.ifBlank { "—" }}",
                            fontSize = 12.sp,
                            color = if (isBeaconBright) Color.DarkGray else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Photo view box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1A1E27))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                        .clickable {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val displayUri = hintUri.ifBlank { item.photoUri }
                    if (displayUri.isNotBlank()) {
                        AsyncImage(
                            model = displayUri,
                            contentDescription = "Фото полки",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Загрузить фото полки",
                                tint = GarageOrange,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Нажмите, чтобы загрузить реальное фото полки / ящика для быстрого поиска в ЧС",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = hintNote,
                    onValueChange = { hintNote = it },
                    label = { Text("Ориентир на месте (для экстренного поиска)") },
                    placeholder = { Text("Напр.: Справа от входа, 2-й ярус, коробка с синей меткой") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Закрыть")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onUpdateHintPhoto(hintUri, hintNote)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GarageOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Сохранить подсказку", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Modal for linking and configuring marketplace platforms (Авито, Юла, ВК Маркет, Яндекс.Объявления).
 */
@Composable
fun PlatformConnectionModal(
    connection: PlatformConnection,
    onDismiss: () -> Unit,
    onSave: (PlatformConnection) -> Unit
) {
    val platformType = PlatformType.fromKey(connection.platformKey)
    var accountId by remember {
        mutableStateOf(connection.accountIdentifier.ifBlank { "+7 (999) 000-00-00" })
    }
    var apiClientId by remember {
        mutableStateOf(connection.apiClientId.ifBlank { "${platformType.key.uppercase()}-PRO-TOKEN-9482" })
    }
    var contactPhone by remember {
        mutableStateOf(connection.defaultContactPhone.ifBlank { "+7 (999) 000-00-00" })
    }
    var pickupAddress by remember {
        mutableStateOf(connection.defaultCityAddress.ifBlank { "Гаражный кооператив / Склад самовывоза" })
    }
    var autoSync by remember { mutableStateOf(connection.autoSync) }
    var autoPromote by remember { mutableStateOf(connection.autoPromote) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlatformBadgeIcon(
                            platformType = platformType,
                            size = 44.dp,
                            showCheckmark = true,
                            isConnected = connection.isConnected
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = platformType.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = platformType.apiPortalHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                // Connection status pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (connection.isConnected) GarageEmeraldGreen.copy(alpha = 0.15f)
                            else GarageOrange.copy(alpha = 0.15f)
                        )
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (connection.isConnected) Icons.Default.Verified else Icons.Default.Link,
                            contentDescription = null,
                            tint = if (connection.isConnected) GarageEmeraldGreen else GarageOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (connection.isConnected) {
                                "Аккаунт привязан • Готов к публикации в 1 клик"
                            } else {
                                "Укажите профиль или токен для мгновенной привязки"
                            },
                            fontSize = 12.sp,
                            color = if (connection.isConnected) GarageEmeraldGreen else GarageOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                OutlinedTextField(
                    value = accountId,
                    onValueChange = { accountId = it },
                    label = { Text("Логин / Телефон / ID профиля") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apiClientId,
                    onValueChange = { apiClientId = it },
                    label = { Text("API Ключ / Фид автозагрузки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text("Адрес склада / гаража в объявлениях") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Автосинхронизация цен и остатков", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Снимать с публикации при продаже", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoSync,
                        onCheckedChange = { autoSync = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF30D158)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Умное авто-поднятие просмотров", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Обновлять объявления в часы пик", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoPromote,
                        onCheckedChange = { autoPromote = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF30D158)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (connection.isConnected) {
                        OutlinedButton(
                            onClick = {
                                onSave(
                                    connection.copy(
                                        isConnected = false,
                                        isSelectedForUpload = false,
                                        lastSyncStatus = "Отключено пользователем"
                                    )
                                )
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Отключить", fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            onSave(
                                connection.copy(
                                    isConnected = true,
                                    isSelectedForUpload = true,
                                    accountIdentifier = accountId.trim(),
                                    apiClientId = apiClientId.trim(),
                                    defaultContactPhone = contactPhone.trim(),
                                    defaultCityAddress = pickupAddress.trim(),
                                    autoSync = autoSync,
                                    autoPromote = autoPromote,
                                    lastSyncStatus = "Привязано ($accountId) • Работает без сбоев"
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GarageEmeraldGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("connect_platform_confirm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (connection.isConnected) "Сохранить" else "Привязать в 1 клик",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Scanner & Printable Sticker Modal for finding items by QR / Barcode / Serial code in seconds.
 */
@Composable
fun QrScannerAndSearchModal(
    items: List<GarageItem>,
    initialItemForLabel: GarageItem? = null,
    onDismiss: () -> Unit,
    onSelectItem: (GarageItem) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf(initialItemForLabel?.qrCode ?: "") }
    val matchedItems = remember(query, items) {
        if (query.isBlank()) items
        else items.filter {
            it.qrCode.contains(query, ignoreCase = true) ||
                it.barcode.contains(query, ignoreCase = true) ||
                it.serialCode.contains(query, ignoreCase = true) ||
                it.title.contains(query, ignoreCase = true) ||
                it.shelfLocation.contains(query, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .border(1.dp, GarageElectricBlueBright, RoundedCornerShape(20.dp))
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
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Сканер",
                            tint = GarageElectricBlueBright
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Скан QR / Штрихкода и Этикетка",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                // Simulated Laser Scanner Viewfinder Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF10141D))
                        .border(1.5.dp, GarageElectricBlueBright.copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = GarageElectricBlueBright,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = "Введите или отсканируйте код (#GAR-..., EAN-13, артикул)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Поиск по QR, штрихкоду или цифрам") },
                    placeholder = { Text("#GAR-084-III или 4607...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (items.isEmpty()) {
                    Text(
                        text = "На складе пока нет товаров. Добавьте товар, чтобы получить его QR-код и штрихкод.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        matchedItems.forEach { item ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectItem(item) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "📍 ${item.zone}, ${item.shelfLocation}",
                                            fontSize = 12.sp,
                                            color = GarageOrange,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "QR: ${item.qrCode} • Штрих: ${item.barcode.ifBlank { "—" }}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        MiniQrCodeCanvas(code = item.qrCode, size = 32.dp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        MiniBarcodeCanvas(barcode = item.barcode, width = 48.dp, height = 12.dp)
                                    }
                                    IconButton(
                                        onClick = {
                                            val shareText = buildString {
                                                appendLine("📦 Товар: ${item.title}")
                                                appendLine("💰 Цена: ${formatRubles(item.price)}")
                                                appendLine("📍 Место: ${item.zone}, ${item.shelfLocation}")
                                                appendLine("🔖 QR: ${item.qrCode} | Штрихкод: ${item.barcode}")
                                                if (item.hintNote.isNotBlank()) {
                                                    appendLine("💡 Подсказка: ${item.hintNote}")
                                                }
                                            }
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(sendIntent, "Поделиться этикеткой")
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Поделиться этикеткой",
                                            tint = GarageElectricBlueBright,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact dialog to create a new Buyer Inquiry / Chat Thread linked to any item and platform.
 */
@Composable
fun CreateBuyerDialogModal(
    items: List<GarageItem>,
    onDismiss: () -> Unit,
    onCreate: (BuyerDialog) -> Unit
) {
    var buyerName by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf(PlatformType.AVITO) }
    var selectedItem by remember { mutableStateOf(items.firstOrNull()) }
    var customItemTitle by remember { mutableStateOf("") }
    var initialMessage by remember {
        mutableStateOf("Здравствуйте! Товар в наличии? На каком стеллаже забирать?")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Новый диалог с покупателем",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Площадка объявления:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PlatformType.entries.forEach { pt ->
                        val isSel = selectedPlatform == pt
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSel) GarageOrange.copy(alpha = 0.18f) else Color.Transparent
                                )
                                .clickable { selectedPlatform = pt }
                                .padding(6.dp)
                        ) {
                            PlatformBadgeIcon(platformType = pt, size = 36.dp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(pt.shortName, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Имя покупателя *") },
                    placeholder = { Text("Напр.: Алексей") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (items.isNotEmpty()) {
                    Text(
                        text = "Привязать товар со склада:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items.forEach { item ->
                            FilterChip(
                                selected = selectedItem?.id == item.id,
                                onClick = { selectedItem = item },
                                label = {
                                    Text(
                                        text = item.title.take(22),
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customItemTitle,
                        onValueChange = { customItemTitle = it },
                        label = { Text("Название интересующего товара") },
                        placeholder = { Text("Напр.: Шины Michelin 205/55 R16") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = initialMessage,
                    onValueChange = { initialMessage = it },
                    label = { Text("Сообщение от покупателя") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (buyerName.isBlank()) return@Button
                            val linked = selectedItem
                            onCreate(
                                BuyerDialog(
                                    buyerName = buyerName.trim(),
                                    platformKey = selectedPlatform.key,
                                    itemId = linked?.id ?: "",
                                    itemTitle = linked?.title ?: customItemTitle.ifBlank { "Товар из гаража" },
                                    itemPrice = linked?.price ?: 0L,
                                    itemShelfLocation = if (linked != null) "${linked.zone}, ${linked.shelfLocation}" else "",
                                    itemQrCode = linked?.qrCode ?: "",
                                    itemPhotoUri = linked?.photoUri ?: "",
                                    lastMessage = initialMessage.trim().ifBlank { "Здравствуйте!" },
                                    timeLabel = GarageRepository.currentTimeFormatted(),
                                    unreadCount = 1L
                                )
                            )
                            onDismiss()
                        },
                        enabled = buyerName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GarageOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Создать диалог", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
