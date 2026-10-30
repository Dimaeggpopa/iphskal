package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.BuyerDialog
import com.example.data.model.GarageItem
import com.example.data.model.PlatformConnection
import com.example.data.model.StorageZone
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GarageRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Пользователь должен войти через Google перед доступом к данным")
    }

    // --- ITEMS ---

    fun observeItems(userId: String = requireUserId()): Flow<List<GarageItem>> = flow {
        val path = "users/$userId/items"
        emitAll(
            db.collection("users").document(userId).collection("items")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        GarageItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getItemById(userId: String, itemId: String): Result<GarageItem?> = runCatching {
        val path = "users/$userId/items/$itemId"
        try {
            db.collection("users").document(userId).collection("items").document(itemId)
                .get()
                .await()
                .toObject(GarageItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            throw e
        }
    }

    suspend fun createItem(item: GarageItem): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("items")
        val docRef = if (item.id.isNotBlank()) colRef.document(item.id) else colRef.document()
        val payload = mapOf(
            "userId" to uid,
            "title" to item.title.trim().take(150),
            "description" to item.description.trim().take(2000),
            "price" to item.price.coerceAtLeast(0L),
            "quantity" to item.quantity.coerceAtLeast(0L),
            "unitLabel" to item.unitLabel.trim().ifEmpty { "шт." }.take(40),
            "zone" to item.zone.trim().ifEmpty { "Гараж" }.take(60),
            "shelfLocation" to item.shelfLocation.trim().ifEmpty { "Стеллаж А1" }.take(120),
            "qrCode" to item.qrCode.trim().ifEmpty { generateQrCode() }.take(64),
            "barcode" to item.barcode.trim().take(64),
            "serialCode" to item.serialCode.trim().take(80),
            "photoUri" to item.photoUri.take(1000),
            "hintPhotoUri" to item.hintPhotoUri.take(1000),
            "hintNote" to item.hintNote.trim().take(500),
            "publishedPlatforms" to item.publishedPlatforms.take(10),
            "publicationStatus" to item.publicationStatus.ifEmpty { "draft" },
            "isSelectedForPublish" to item.isSelectedForPublish,
            "viewsCount" to item.viewsCount.coerceAtLeast(0L),
            "inquiriesCount" to item.inquiriesCount.coerceAtLeast(0L),
            "isVerifiedInInventory" to item.isVerifiedInInventory,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateItem(item: GarageItem): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(item.id.isNotBlank()) { "Item ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("items").document(item.id)
        val updates = mapOf(
            "title" to item.title.trim().take(150),
            "description" to item.description.trim().take(2000),
            "price" to item.price.coerceAtLeast(0L),
            "quantity" to item.quantity.coerceAtLeast(0L),
            "unitLabel" to item.unitLabel.trim().ifEmpty { "шт." }.take(40),
            "zone" to item.zone.trim().ifEmpty { "Гараж" }.take(60),
            "shelfLocation" to item.shelfLocation.trim().ifEmpty { "Стеллаж А1" }.take(120),
            "qrCode" to item.qrCode.trim().ifEmpty { generateQrCode() }.take(64),
            "barcode" to item.barcode.trim().take(64),
            "serialCode" to item.serialCode.trim().take(80),
            "photoUri" to item.photoUri.take(1000),
            "hintPhotoUri" to item.hintPhotoUri.take(1000),
            "hintNote" to item.hintNote.trim().take(500),
            "publishedPlatforms" to item.publishedPlatforms.take(10),
            "publicationStatus" to item.publicationStatus,
            "isSelectedForPublish" to item.isSelectedForPublish,
            "viewsCount" to item.viewsCount.coerceAtLeast(0L),
            "inquiriesCount" to item.inquiriesCount.coerceAtLeast(0L),
            "isVerifiedInInventory" to item.isVerifiedInInventory,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("items").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun publishItemsToPlatforms(
        items: List<GarageItem>,
        targetPlatformKeys: List<String>
    ): Result<Int> = runCatching {
        val uid = requireUserId()
        val cleanPlatforms = targetPlatformKeys.distinct().take(10)
        if (cleanPlatforms.isEmpty() || items.isEmpty()) return@runCatching 0

        var publishedCount = 0
        for (item in items) {
            if (item.id.isBlank()) continue
            val mergedPlatforms = (item.publishedPlatforms + cleanPlatforms).distinct().take(10)
            val status = if (mergedPlatforms.size >= 4) "published" else "partial"
            val docRef = db.collection("users").document(uid).collection("items").document(item.id)
            try {
                docRef.update(
                    mapOf(
                        "publishedPlatforms" to mergedPlatforms,
                        "publicationStatus" to status,
                        "viewsCount" to (item.viewsCount + (cleanPlatforms.size * 12L)),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                publishedCount++
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        }
        publishedCount
    }

    // --- PLATFORMS ---

    fun observePlatforms(userId: String = requireUserId()): Flow<List<PlatformConnection>> = flow {
        val path = "users/$userId/platforms"
        emitAll(
            db.collection("users").document(userId).collection("platforms")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PlatformConnection::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun savePlatformConnection(connection: PlatformConnection): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = connection.platformKey
        val docRef = db.collection("users").document(uid).collection("platforms").document(docId)
        val existingSnap = try {
            docRef.get().await()
        } catch (e: Exception) {
            null
        }
        try {
            if (existingSnap != null && existingSnap.exists()) {
                docRef.update(
                    mapOf(
                        "platformName" to connection.platformName.take(60),
                        "isConnected" to connection.isConnected,
                        "isSelectedForUpload" to connection.isSelectedForUpload,
                        "accountIdentifier" to connection.accountIdentifier.trim().take(120),
                        "apiClientId" to connection.apiClientId.trim().take(200),
                        "autoSync" to connection.autoSync,
                        "autoPromote" to connection.autoPromote,
                        "defaultContactPhone" to connection.defaultContactPhone.trim().take(40),
                        "defaultCityAddress" to connection.defaultCityAddress.trim().take(200),
                        "syncedCount" to connection.syncedCount.coerceAtLeast(0L),
                        "lastSyncStatus" to connection.lastSyncStatus.take(200),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "platformKey" to connection.platformKey,
                        "platformName" to connection.platformName.take(60),
                        "isConnected" to connection.isConnected,
                        "isSelectedForUpload" to connection.isSelectedForUpload,
                        "accountIdentifier" to connection.accountIdentifier.trim().take(120),
                        "apiClientId" to connection.apiClientId.trim().take(200),
                        "autoSync" to connection.autoSync,
                        "autoPromote" to connection.autoPromote,
                        "defaultContactPhone" to connection.defaultContactPhone.trim().take(40),
                        "defaultCityAddress" to connection.defaultCityAddress.trim().take(200),
                        "syncedCount" to connection.syncedCount.coerceAtLeast(0L),
                        "lastSyncStatus" to connection.lastSyncStatus.take(200),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- DIALOGS ---

    fun observeDialogs(userId: String = requireUserId()): Flow<List<BuyerDialog>> = flow {
        val path = "users/$userId/dialogs"
        emitAll(
            db.collection("users").document(userId).collection("dialogs")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        BuyerDialog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createDialog(dialog: BuyerDialog): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("dialogs")
        val docRef = if (dialog.id.isNotBlank()) colRef.document(dialog.id) else colRef.document()
        val initialLog = if (dialog.messagesLog.isNotEmpty()) {
            dialog.messagesLog.take(50)
        } else {
            listOf("BUYER|${dialog.timeLabel}|${dialog.lastMessage.take(300)}")
        }
        val payload = mapOf(
            "userId" to uid,
            "buyerName" to dialog.buyerName.trim().ifEmpty { "Покупатель" }.take(80),
            "platformKey" to dialog.platformKey,
            "itemId" to dialog.itemId.take(128),
            "itemTitle" to dialog.itemTitle.trim().ifEmpty { "Товар со склада" }.take(150),
            "itemPrice" to dialog.itemPrice.coerceAtLeast(0L),
            "itemShelfLocation" to dialog.itemShelfLocation.take(120),
            "itemQrCode" to dialog.itemQrCode.take(64),
            "itemPhotoUri" to dialog.itemPhotoUri.take(1000),
            "lastMessage" to dialog.lastMessage.trim().ifEmpty { "Здравствуйте!" }.take(500),
            "timeLabel" to dialog.timeLabel.ifEmpty { currentTimeFormatted() }.take(40),
            "unreadCount" to dialog.unreadCount.coerceAtLeast(0L),
            "messagesLog" to initialLog,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun sendDialogMessage(
        dialog: BuyerDialog,
        messageText: String,
        isFromMe: Boolean = true
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(dialog.id.isNotBlank()) { "Dialog ID required" }
        val docRef = db.collection("users").document(uid).collection("dialogs").document(dialog.id)
        val timeNow = currentTimeFormatted()
        val senderPrefix = if (isFromMe) "ME" else "BUYER"
        val cleanText = messageText.replace("|", " ").trim().take(350)
        val newEntry = "$senderPrefix|$timeNow|$cleanText"
        val updatedLog = (dialog.messagesLog + newEntry).takeLast(50)
        val newUnread = if (isFromMe) 0L else (dialog.unreadCount + 1L)
        try {
            docRef.update(
                mapOf(
                    "lastMessage" to cleanText,
                    "timeLabel" to timeNow,
                    "unreadCount" to newUnread,
                    "messagesLog" to updatedLog,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun markDialogRead(dialogId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("dialogs").document(dialogId)
        try {
            docRef.update(
                mapOf(
                    "unreadCount" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteDialog(dialogId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("dialogs").document(dialogId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- STORAGE ZONES ---

    fun observeZones(userId: String = requireUserId()): Flow<List<StorageZone>> = flow {
        val path = "users/$userId/zones"
        emitAll(
            db.collection("users").document(userId).collection("zones")
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        StorageZone::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveZone(zone: StorageZone): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("zones")
        val docRef = if (zone.id.isNotBlank()) colRef.document(zone.id) else colRef.document()
        val payload = mapOf(
            "userId" to uid,
            "name" to zone.name.trim().take(60),
            "capacity" to zone.capacity.coerceIn(1L, 100000L),
            "description" to zone.description.trim().take(200),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteZone(zoneId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("zones").document(zoneId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    companion object {
        fun generateQrCode(): String {
            val num = (100..999).random()
            val roman = listOf("I", "II", "III", "IV", "V").random()
            return "#GAR-$num-$roman"
        }

        fun generateBarcode(): String {
            val prefix = "4607"
            val randomDigits = (1..9).map { (0..9).random() }.joinToString("")
            return "$prefix$randomDigits"
        }

        fun currentTimeFormatted(): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }
    }
}
