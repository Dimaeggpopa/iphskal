package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.GarageItem
import com.example.data.model.PlatformConnection
import com.example.data.model.UiState
import com.example.data.repository.GarageRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.components.GarageTopBar
import com.example.ui.components.formatRubles
import com.example.ui.modals.AddOrEditItemDialog
import com.example.ui.modals.CreateBuyerDialogModal
import com.example.ui.modals.PhotoHintModal
import com.example.ui.modals.PlatformConnectionModal
import com.example.ui.modals.QrScannerAndSearchModal
import com.example.ui.screens.DialogsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.PublishScreen
import com.example.ui.screens.WarehouseScreen
import com.example.ui.theme.GarageEmeraldGreen
import com.example.ui.theme.GarageOrange
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GarageViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

enum class GarageTab(val label: String, val icon: ImageVector, val tag: String) {
    HOME("Главная", Icons.Default.Home, "nav_tab_home"),
    WAREHOUSE("Склад", Icons.Default.Inventory2, "nav_tab_warehouse"),
    PUBLISH("Публикация", Icons.Default.Add, "nav_tab_publish"),
    DIALOGS("Диалоги", Icons.Default.ChatBubbleOutline, "nav_tab_dialogs"),
    MORE("Ещё", Icons.Default.MoreHoriz, "nav_tab_more")
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        MyApplicationTheme {
            AuthScreen(onAuthSuccess = {})
        }
    } else {
        val viewModel: GarageViewModel = viewModel(
            key = user.uid,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY]) {
                        "APPLICATION_KEY missing from CreationExtras"
                    }
                    val databaseId = app.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(databaseId)
                    GarageViewModel(
                        repository = GarageRepository(db),
                        currentUserId = user.uid,
                        appContext = app.applicationContext
                    )
                }
            }
        )

        val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
        val accentPalette by viewModel.accentPalette.collectAsStateWithLifecycle()

        MyApplicationTheme(
            themeMode = themeMode,
            accentPalette = accentPalette
        ) {
            AuthenticatedGarageApp(
                user = user,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun AuthenticatedGarageApp(
    user: FirebaseUser,
    viewModel: GarageViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val itemsUiState by viewModel.itemsState.collectAsStateWithLifecycle()
    val platformsUiState by viewModel.platformsState.collectAsStateWithLifecycle()
    val dialogsUiState by viewModel.dialogsState.collectAsStateWithLifecycle()
    val zonesUiState by viewModel.zonesState.collectAsStateWithLifecycle()
    val publishProgress by viewModel.publishProgress.collectAsStateWithLifecycle()
    val statusToast by viewModel.statusToast.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val accentPalette by viewModel.accentPalette.collectAsStateWithLifecycle()
    val quickReplyTemplates by viewModel.quickReplyTemplates.collectAsStateWithLifecycle()

    val items = (itemsUiState as? UiState.Success)?.data.orEmpty()
    val platforms = (platformsUiState as? UiState.Success)?.data.orEmpty()
    val dialogs = (dialogsUiState as? UiState.Success)?.data.orEmpty()
    val zones = (zonesUiState as? UiState.Success)?.data.orEmpty()

    var currentTab by remember { mutableStateOf(GarageTab.HOME) }
    var itemModalState by remember { mutableStateOf<Pair<Boolean, GarageItem?>>(false to null) }
    var photoHintModalItem by remember { mutableStateOf<GarageItem?>(null) }
    var platformModalTarget by remember { mutableStateOf<PlatformConnection?>(null) }
    var showQrScannerModal by remember { mutableStateOf(false) }
    var showCreateDialogModal by remember { mutableStateOf(false) }
    var showPromotionModal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusToast) {
        statusToast?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    BackHandler(enabled = currentTab != GarageTab.HOME) {
        currentTab = GarageTab.HOME
    }

    val unreadTotal = remember(dialogs) { dialogs.sumOf { it.unreadCount }.toInt() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                GarageTopBar(
                    unreadCount = unreadTotal,
                    userPhotoUrl = user.photoUrl?.toString(),
                    onProfileClick = { currentTab = GarageTab.MORE },
                    onNotificationsClick = { currentTab = GarageTab.DIALOGS }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    thickness = 1.dp
                )

                // Publish completion banner
                AnimatedVisibility(visible = publishProgress.completedBannerMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GarageEmeraldGreen.copy(alpha = 0.18f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GarageEmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = publishProgress.completedBannerMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissPublishBanner() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            GarageBottomNavigationBar(
                currentTab = currentTab,
                unreadDialogsCount = unreadTotal,
                onSelectTab = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 10.dp)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "garage_tab_transition"
            ) { tab ->
                when (tab) {
                    GarageTab.HOME -> {
                        HomeScreen(
                            items = items,
                            platforms = platforms,
                            dialogs = dialogs,
                            publishProgress = publishProgress,
                            onPlatformClick = { platformModalTarget = it },
                            onOneClickPublishAll = { viewModel.publishAllInOneClick(onlySelected = false) },
                            onAddItemClick = { itemModalState = true to null },
                            onScanQrClick = { showQrScannerModal = true },
                            onPromotionClick = { showPromotionModal = true },
                            onItemClick = { itemModalState = true to it },
                            onPhotoHintClick = { photoHintModalItem = it },
                            onOpenDialogsClick = { currentTab = GarageTab.DIALOGS }
                        )
                    }

                    GarageTab.WAREHOUSE -> {
                        WarehouseScreen(
                            items = items,
                            customZones = zones,
                            onScanQrClick = { showQrScannerModal = true },
                            onAddItemClick = { itemModalState = true to null },
                            onItemClick = { itemModalState = true to it },
                            onPhotoHintClick = { photoHintModalItem = it },
                            onToggleInventoryCheck = { viewModel.toggleInventoryVerification(it) }
                        )
                    }

                    GarageTab.PUBLISH -> {
                        PublishScreen(
                            items = items,
                            platforms = platforms,
                            publishProgress = publishProgress,
                            onTogglePlatformUpload = { viewModel.togglePlatformForUpload(it) },
                            onConfigurePlatform = { platformModalTarget = it },
                            onToggleItemSelection = { viewModel.toggleItemSelectionForPublish(it) },
                            onPublishSelected = { viewModel.publishAllInOneClick(onlySelected = true) },
                            onAddItemClick = { itemModalState = true to null },
                            onPhotoHintClick = { photoHintModalItem = it },
                            onEditItemClick = { itemModalState = true to it }
                        )
                    }

                    GarageTab.DIALOGS -> {
                        DialogsScreen(
                            dialogs = dialogs,
                            items = items,
                            quickReplyTemplates = quickReplyTemplates,
                            onNewDialogClick = { showCreateDialogModal = true },
                            onSendMessage = { dlg, text, fromMe ->
                                viewModel.sendMessage(dlg, text, fromMe)
                            },
                            onMarkRead = { viewModel.markDialogAsRead(it) },
                            onDeleteDialog = { viewModel.deleteDialog(it) },
                            onOpenItemPhotoHint = { photoHintModalItem = it }
                        )
                    }

                    GarageTab.MORE -> {
                        MoreScreen(
                            items = items,
                            platforms = platforms,
                            zones = zones,
                            themeMode = themeMode,
                            accentPalette = accentPalette,
                            quickReplyTemplates = quickReplyTemplates,
                            userEmail = user.email,
                            onSelectThemeMode = { viewModel.setThemeMode(it) },
                            onSelectAccentPalette = { viewModel.setAccentPalette(it) },
                            onSaveZone = { name, cap, desc ->
                                viewModel.saveStorageZone(name, cap, desc)
                            },
                            onDeleteZone = { viewModel.deleteStorageZone(it) },
                            onAddQuickReply = { viewModel.addQuickReplyTemplate(it) },
                            onRemoveQuickReply = { viewModel.removeQuickReplyTemplate(it) },
                            onOpenPhotoHint = { photoHintModalItem = it },
                            onSignOut = {
                                signOutUser(
                                    credentialManager = credentialManager,
                                    onSignOutComplete = {},
                                    scope = scope
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // --- MODALS ---

    if (itemModalState.first) {
        AddOrEditItemDialog(
            initialItem = itemModalState.second,
            availableZones = zones.map { it.name },
            onDismiss = { itemModalState = false to null },
            onSave = { updated ->
                viewModel.saveItem(updated) {
                    itemModalState = false to null
                }
            },
            onDelete = { id ->
                viewModel.deleteItem(id)
            }
        )
    }

    if (photoHintModalItem != null) {
        val currentItem = photoHintModalItem!!
        PhotoHintModal(
            item = currentItem,
            onDismiss = { photoHintModalItem = null },
            onUpdateHintPhoto = { newUri, newNote ->
                viewModel.saveItem(
                    currentItem.copy(
                        hintPhotoUri = newUri,
                        hintNote = newNote
                    )
                )
            }
        )
    }

    if (platformModalTarget != null) {
        PlatformConnectionModal(
            connection = platformModalTarget!!,
            onDismiss = { platformModalTarget = null },
            onSave = { updated ->
                viewModel.savePlatform(updated)
            }
        )
    }

    if (showQrScannerModal) {
        QrScannerAndSearchModal(
            items = items,
            onDismiss = { showQrScannerModal = false },
            onSelectItem = { selected ->
                showQrScannerModal = false
                photoHintModalItem = selected
            }
        )
    }

    if (showCreateDialogModal) {
        CreateBuyerDialogModal(
            items = items,
            onDismiss = { showCreateDialogModal = false },
            onCreate = { newDialog ->
                viewModel.createDialog(newDialog)
            }
        )
    }

    if (showPromotionModal) {
        PromotionBoostModal(
            items = items,
            onDismiss = { showPromotionModal = false },
            onBoostItem = { item ->
                viewModel.boostItemPromotion(item)
            }
        )
    }
}

@Composable
private fun GarageBottomNavigationBar(
    currentTab: GarageTab,
    unreadDialogsCount: Int,
    onSelectTab: (GarageTab) -> Unit
) {
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .windowInsetsPadding(WindowInsets.navigationBars),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
            thickness = 0.7.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GarageTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val tintColor = if (isSelected) GarageOrange else MaterialTheme.colorScheme.onSurfaceVariant

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptics.performHapticFeedback(
                                androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove
                            )
                            onSelectTab(tab)
                        }
                        .padding(vertical = 4.dp)
                        .testTag(tab.tag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = tintColor,
                            modifier = Modifier.size(24.dp)
                        )
                        if (tab == GarageTab.DIALOGS && unreadDialogsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFF3B30))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = tintColor
                    )
                }
            }
        }
        // Signature iPhone iOS Home Indicator Pill (matching all 4 reference screenshots)
        Box(
            modifier = Modifier
                .padding(top = 2.dp, bottom = 6.dp)
                .width(134.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(2.5.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f))
        )
    }
}

@Composable
private fun PromotionBoostModal(
    items: List<GarageItem>,
    onDismiss: () -> Unit,
    onBoostItem: (GarageItem) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GarageOrange, RoundedCornerShape(20.dp))
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
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Продвижение",
                            tint = GarageOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ускорение продаж и поднятие",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Text(
                    text = "Обновляет дату размещения и поднимает объявление в поиске на привязанных площадках.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (items.isEmpty()) {
                    Text(
                        text = "Сначала добавьте товары в гараж, чтобы запустить их продвижение.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    items.take(5).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                Text(
                                    "${formatRubles(item.price)} • 👁 ${item.viewsCount} просм.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { onBoostItem(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = GarageOrange),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Поднять ⚡", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
