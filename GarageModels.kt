package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class PlatformType(
    val key: String,
    val displayName: String,
    val shortName: String,
    val apiPortalHint: String
) {
    AVITO("avito", "Авито", "Авито", "Avito Автозагрузка / API Pro"),
    YOULA("youla", "Юла", "Юла", "Юла Магазин / Фид выгрузки"),
    VK("vk", "ВК Маркет", "ВК", "VK Товары Сообщества API"),
    YANDEX("yandex", "Яндекс.Объявления", "Яндекс", "Яндекс Товары и Объявления");

    companion object {
        fun fromKey(key: String): PlatformType =
            entries.find { it.key == key } ?: AVITO
    }
}

@IgnoreExtraProperties
data class GarageItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val price: Long = 0L,
    val quantity: Long = 1L,
    val unitLabel: String = "шт.",
    val zone: String = "Гараж",
    val shelfLocation: String = "",
    val qrCode: String = "",
    val barcode: String = "",
    val serialCode: String = "",
    val photoUri: String = "",
    val hintPhotoUri: String = "",
    val hintNote: String = "",
    val publishedPlatforms: List<String> = emptyList(),
    val publicationStatus: String = "draft",
    val isSelectedForPublish: Boolean = true,
    val viewsCount: Long = 0L,
    val inquiriesCount: Long = 0L,
    val isVerifiedInInventory: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PlatformConnection(
    @DocumentId val id: String = "",
    val userId: String = "",
    val platformKey: String = "avito",
    val platformName: String = "Авито",
    val isConnected: Boolean = false,
    val isSelectedForUpload: Boolean = true,
    val accountIdentifier: String = "",
    val apiClientId: String = "",
    val autoSync: Boolean = true,
    val autoPromote: Boolean = false,
    val defaultContactPhone: String = "",
    val defaultCityAddress: String = "",
    val syncedCount: Long = 0L,
    val lastSyncStatus: String = "Готово к подключению",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BuyerDialog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val buyerName: String = "",
    val platformKey: String = "avito",
    val itemId: String = "",
    val itemTitle: String = "",
    val itemPrice: Long = 0L,
    val itemShelfLocation: String = "",
    val itemQrCode: String = "",
    val itemPhotoUri: String = "",
    val lastMessage: String = "",
    val timeLabel: String = "Сейчас",
    val unreadCount: Long = 0L,
    val messagesLog: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun parsedMessages(): List<ChatMessage> {
        return messagesLog.mapIndexed { index, raw ->
            val parts = raw.split("|", limit = 3)
            if (parts.size == 3) {
                ChatMessage(
                    id = "$id-$index",
                    isOutgoing = parts[0] == "ME",
                    time = parts[1],
                    text = parts[2]
                )
            } else {
                ChatMessage(
                    id = "$id-$index",
                    isOutgoing = false,
                    time = timeLabel,
                    text = raw
                )
            }
        }
    }
}

data class ChatMessage(
    val id: String,
    val isOutgoing: Boolean,
    val time: String,
    val text: String
)

@IgnoreExtraProperties
data class StorageZone(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val capacity: Long = 50L,
    val description: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
