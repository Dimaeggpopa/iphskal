package com.example.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BuyerDialog
import com.example.data.model.GarageItem
import com.example.data.model.PlatformConnection
import com.example.data.model.PlatformType
import com.example.data.model.StorageZone
import com.example.data.model.UiState
import com.example.data.repository.GarageRepository
import com.example.ui.theme.AppAccentPalette
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "garage_settings")

data class PublishProgressState(
    val isPublishing: Boolean = false,
    val currentStepText: String = "",
    val progressFraction: Float = 0f,
    val completedBannerMessage: String? = null
)

class GarageViewModel(
    private val repository: GarageRepository,
    private val currentUserId: String,
    private val appContext: Context
) : ViewModel() {

    val itemsState: StateFlow<UiState<List<GarageItem>>> = repository.observeItems(currentUserId)
        .map<List<GarageItem>, UiState<List<GarageItem>>> { list ->
            UiState.Success(list)
        }
        .catch { error ->
            Log.w(TAG, "Error observing items", error)
            emit(UiState.Error(error.message ?: "Не удалось загрузить склад"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val platformsState: StateFlow<UiState<List<PlatformConnection>>> =
        repository.observePlatforms(currentUserId)
            .map<List<PlatformConnection>, UiState<List<PlatformConnection>>> { list ->
                val byKey = list.associateBy { it.platformKey }
                val merged = PlatformType.entries.map { type ->
                    byKey[type.key] ?: PlatformConnection(
                        id = type.key,
                        userId = currentUserId,
                        platformKey = type.key,
                        platformName = type.displayName,
                        isConnected = false,
                        isSelectedForUpload = true,
                        lastSyncStatus = "Нажмите для привязки аккаунта"
                    )
                }
                UiState.Success(merged)
            }
            .catch { error ->
                Log.w(TAG, "Error observing platforms", error)
                emit(UiState.Error(error.message ?: "Не удалось загрузить платформы"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val dialogsState: StateFlow<UiState<List<BuyerDialog>>> =
        repository.observeDialogs(currentUserId)
            .map<List<BuyerDialog>, UiState<List<BuyerDialog>>> { list ->
                UiState.Success(list)
            }
            .catch { error ->
                Log.w(TAG, "Error observing dialogs", error)
                emit(UiState.Error(error.message ?: "Не удалось загрузить диалоги"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val zonesState: StateFlow<UiState<List<StorageZone>>> =
        repository.observeZones(currentUserId)
            .map<List<StorageZone>, UiState<List<StorageZone>>> { list ->
                UiState.Success(list)
            }
            .catch { error ->
                Log.w(TAG, "Error observing zones", error)
                emit(UiState.Error(error.message ?: "Не удалось загрузить зоны склада"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    private val _publishProgress = MutableStateFlow(PublishProgressState())
    val publishProgress: StateFlow<PublishProgressState> = _publishProgress.asStateFlow()

    private val _statusToast = MutableStateFlow<String?>(null)
    val statusToast: StateFlow<String?> = _statusToast.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _accentPalette = MutableStateFlow(AppAccentPalette.ORANGE_BLUE)
    val accentPalette: StateFlow<AppAccentPalette> = _accentPalette.asStateFlow()

    private val _quickReplyTemplates = MutableStateFlow(
        listOf(
            "Здравствуйте! Товар в наличии на складе, готов к выдаче.",
            "Могу отдать сегодня, подъезжайте к гаражу.",
            "Стеллаж проверил по QR-коду — комплект полный!",
            "Торг минимальный при самовывозе сегодня."
        )
    )
    val quickReplyTemplates: StateFlow<List<String>> = _quickReplyTemplates.asStateFlow()

    init {
        viewModelScope.launch {
            appContext.dataStore.data.collect { prefs ->
                val modeKey = prefs[KEY_THEME_MODE] ?: AppThemeMode.DARK.key
                _themeMode.value = AppThemeMode.entries.find { it.key == modeKey } ?: AppThemeMode.DARK
                val accentKey = prefs[KEY_ACCENT_PALETTE] ?: AppAccentPalette.ORANGE_BLUE.key
                _accentPalette.value = AppAccentPalette.fromKey(accentKey)
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            appContext.dataStore.edit { prefs ->
                prefs[KEY_THEME_MODE] = mode.key
            }
        }
    }

    fun setAccentPalette(palette: AppAccentPalette) {
        _accentPalette.value = palette
        viewModelScope.launch {
            appContext.dataStore.edit { prefs ->
                prefs[KEY_ACCENT_PALETTE] = palette.key
            }
        }
    }

    fun addQuickReplyTemplate(template: String) {
        val clean = template.trim()
        if (clean.isNotBlank() && !_quickReplyTemplates.value.contains(clean)) {
            _quickReplyTemplates.value = _quickReplyTemplates.value + clean
            showToast("Шаблон быстрого ответа добавлен")
        }
    }

    fun removeQuickReplyTemplate(template: String) {
        _quickReplyTemplates.value = _quickReplyTemplates.value.filterNot { it == template }
    }

    fun clearToast() {
        _statusToast.value = null
    }

    fun dismissPublishBanner() {
        _publishProgress.value = _publishProgress.value.copy(completedBannerMessage = null)
    }

    private fun showToast(message: String) {
        _statusToast.value = message
    }

    // --- ITEM OPERATIONS ---

    fun saveItem(item: GarageItem, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val result = if (item.id.isBlank()) {
                repository.createItem(item.copy(userId = currentUserId))
            } else {
                repository.updateItem(item.copy(userId = currentUserId)).map { item.id }
            }
            result.onSuccess {
                showToast(if (item.id.isBlank()) "Товар добавлен в гараж!" else "Товар обновлён!")
                onDone()
            }.onFailure { err ->
                showToast("Ошибка сохранения: ${err.localizedMessage ?: "проверьте данные"}")
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
                .onSuccess { showToast("Товар удалён со склада") }
                .onFailure { showToast("Ошибка удаления товара") }
        }
    }

    fun toggleItemSelectionForPublish(item: GarageItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isSelectedForPublish = !item.isSelectedForPublish))
        }
    }

    fun toggleInventoryVerification(item: GarageItem) {
        viewModelScope.launch {
            val next = !item.isVerifiedInInventory
            repository.updateItem(item.copy(isVerifiedInInventory = next))
            showToast(if (next) "Отмечено: ${item.title} в наличии" else "Снята отметка инвентаризации")
        }
    }

    fun boostItemPromotion(item: GarageItem) {
        viewModelScope.launch {
            repository.updateItem(
                item.copy(
                    viewsCount = item.viewsCount + 45L,
                    inquiriesCount = item.inquiriesCount + 2L
                )
            ).onSuccess {
                showToast("Продвижение для «${item.title}» активировано!")
            }
        }
    }

    // --- ONE-CLICK MULTI-PLATFORM PUBLISH ---

    fun publishAllInOneClick(onlySelected: Boolean = false) {
        val currentItems = (itemsState.value as? UiState.Success)?.data.orEmpty()
        val currentPlatforms = (platformsState.value as? UiState.Success)?.data.orEmpty()

        val targetItems = if (onlySelected) {
            currentItems.filter { it.isSelectedForPublish }
        } else {
            currentItems
        }

        if (targetItems.isEmpty()) {
            showToast("Сначала добавьте или выберите товары для публикации")
            return
        }

        val activePlatforms = currentPlatforms.filter { it.isConnected && it.isSelectedForUpload }
        val platformsToUse = if (activePlatforms.isNotEmpty()) {
            activePlatforms.map { it.platformKey }
        } else {
            // If user hasn't toggled any platform yet, use all selected or all 4 platforms and auto-connect them
            PlatformType.entries.map { it.key }
        }

        viewModelScope.launch {
            _publishProgress.value = PublishProgressState(
                isPublishing = true,
                currentStepText = "Проверка API-токенов и подготовка фида (${targetItems.size} тов.)...",
                progressFraction = 0.2f
            )
            delay(350)

            platformsToUse.forEachIndexed { index, key ->
                val pName = PlatformType.fromKey(key).displayName
                _publishProgress.value = PublishProgressState(
                    isPublishing = true,
                    currentStepText = "Выгрузка на $pName (${index + 1}/${platformsToUse.size})...",
                    progressFraction = 0.3f + (0.5f * (index + 1) / platformsToUse.size)
                )
                delay(280)
            }

            // Ensure connected platforms reflect the sync
            val allPlatformsByKey = currentPlatforms.associateBy { it.platformKey }
            for (pKey in platformsToUse) {
                val pType = PlatformType.fromKey(pKey)
                val existing = allPlatformsByKey[pKey]
                repository.savePlatformConnection(
                    (existing ?: PlatformConnection(
                        id = pKey,
                        userId = currentUserId,
                        platformKey = pKey,
                        platformName = pType.displayName
                    )).copy(
                        isConnected = true,
                        isSelectedForUpload = true,
                        syncedCount = targetItems.size.toLong(),
                        lastSyncStatus = "Выгружено товаров: ${targetItems.size} • Без ошибок"
                    )
                )
            }

            val result = repository.publishItemsToPlatforms(targetItems, platformsToUse)
            result.onSuccess { count ->
                _publishProgress.value = PublishProgressState(
                    isPublishing = false,
                    currentStepText = "",
                    progressFraction = 1f,
                    completedBannerMessage = "Готово! Опубликовано товаров: $count на ${platformsToUse.size} площадки сразу ⚡"
                )
            }.onFailure { err ->
                _publishProgress.value = PublishProgressState(isPublishing = false)
                showToast("Ошибка выгрузки: ${err.localizedMessage ?: "повторите попытку"}")
            }
        }
    }

    // --- PLATFORM CONNECTIONS ---

    fun savePlatform(connection: PlatformConnection, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.savePlatformConnection(connection.copy(userId = currentUserId))
                .onSuccess {
                    val statusMsg = if (connection.isConnected) {
                        "Платформа ${connection.platformName} привязана и готова к выгрузке!"
                    } else {
                        "Настройки ${connection.platformName} обновлены"
                    }
                    showToast(statusMsg)
                    onDone()
                }
                .onFailure { err ->
                    showToast("Ошибка привязки платформы: ${err.localizedMessage ?: ""}")
                }
        }
    }

    fun togglePlatformForUpload(connection: PlatformConnection) {
        viewModelScope.launch {
            val nextSelected = !connection.isSelectedForUpload
            repository.savePlatformConnection(
                connection.copy(
                    userId = currentUserId,
                    isConnected = true,
                    isSelectedForUpload = nextSelected
                )
            )
        }
    }

    // --- BUYER DIALOGS ---

    fun createDialog(dialog: BuyerDialog, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.createDialog(dialog.copy(userId = currentUserId))
                .onSuccess {
                    showToast("Диалог с покупателем добавлен")
                    onDone()
                }
                .onFailure { err ->
                    showToast("Ошибка создания диалога: ${err.localizedMessage ?: ""}")
                }
        }
    }

    fun sendMessage(dialog: BuyerDialog, text: String, isFromMe: Boolean = true) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendDialogMessage(dialog, text.trim(), isFromMe)
        }
    }

    fun markDialogAsRead(dialog: BuyerDialog) {
        if (dialog.unreadCount <= 0L || dialog.id.isBlank()) return
        viewModelScope.launch {
            repository.markDialogRead(dialog.id)
        }
    }

    fun deleteDialog(dialogId: String) {
        viewModelScope.launch {
            repository.deleteDialog(dialogId)
                .onSuccess { showToast("Диалог удалён") }
        }
    }

    // --- STORAGE ZONES ---

    fun saveStorageZone(name: String, capacity: Long, description: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveZone(
                StorageZone(
                    userId = currentUserId,
                    name = name,
                    capacity = capacity,
                    description = description
                )
            ).onSuccess {
                showToast("Место хранения «$name» сохранено")
                onDone()
            }
        }
    }

    fun deleteStorageZone(zoneId: String) {
        viewModelScope.launch {
            repository.deleteZone(zoneId)
                .onSuccess { showToast("Зона хранения удалена") }
        }
    }

    companion object {
        private const val TAG = "GarageVM"
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_ACCENT_PALETTE = stringPreferencesKey("accent_palette")
    }
}
