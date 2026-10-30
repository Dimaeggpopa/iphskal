import Foundation
import FirebaseCore
import FirebaseAuth
import FirebaseFirestore

final class GarageRepository {
    static let databaseId = "ai-studio-android-118a4e2a-5c69-4018-9cc3-1ff1d7dac084"

    private let db: Firestore

    init() {
        if let app = FirebaseApp.app() {
            self.db = Firestore.firestore(app: app, database: Self.databaseId)
        } else {
            self.db = Firestore.firestore()
        }
    }

    private func requireUserId() throws -> String {
        guard let uid = Auth.auth().currentUser?.uid else {
            throw NSError(
                domain: "GarageRepository",
                code: 401,
                userInfo: [NSLocalizedDescriptionKey: "Пользователь должен войти через Google перед доступом к данным"]
            )
        }
        return uid
    }

    // MARK: - Items

    func observeItems(userId: String, onUpdate: @escaping ([GarageItem]) -> Void) -> ListenerRegistration {
        return db.collection("users").document(userId).collection("items")
            .whereField("userId", isEqualTo: userId)
            .addSnapshotListener { snapshot, error in
                guard let documents = snapshot?.documents, error == nil else {
                    return
                }
                let items = documents.map { doc -> GarageItem in
                    let data = doc.data()
                    return GarageItem(
                        id: doc.documentID,
                        userId: data["userId"] as? String ?? userId,
                        title: data["title"] as? String ?? "",
                        description: data["description"] as? String ?? "",
                        price: (data["price"] as? NSNumber)?.int64Value ?? 0,
                        quantity: (data["quantity"] as? NSNumber)?.int64Value ?? 1,
                        unitLabel: data["unitLabel"] as? String ?? "шт.",
                        zone: data["zone"] as? String ?? "Гараж",
                        shelfLocation: data["shelfLocation"] as? String ?? "",
                        qrCode: data["qrCode"] as? String ?? "",
                        barcode: data["barcode"] as? String ?? "",
                        serialCode: data["serialCode"] as? String ?? "",
                        photoUri: data["photoUri"] as? String ?? "",
                        hintPhotoUri: data["hintPhotoUri"] as? String ?? "",
                        hintNote: data["hintNote"] as? String ?? "",
                        publishedPlatforms: data["publishedPlatforms"] as? [String] ?? [],
                        publicationStatus: data["publicationStatus"] as? String ?? "draft",
                        isSelectedForPublish: data["isSelectedForPublish"] as? Bool ?? true,
                        viewsCount: (data["viewsCount"] as? NSNumber)?.int64Value ?? 0,
                        inquiriesCount: (data["inquiriesCount"] as? NSNumber)?.int64Value ?? 0,
                        isVerifiedInInventory: data["isVerifiedInInventory"] as? Bool ?? false,
                        createdAt: (data["createdAt"] as? Timestamp)?.dateValue(),
                        updatedAt: (data["updatedAt"] as? Timestamp)?.dateValue()
                    )
                }
                onUpdate(items)
            }
    }

    func saveItem(_ item: GarageItem) async throws {
        let uid = try requireUserId()
        let colRef = db.collection("users").document(uid).collection("items")
        let isNew = item.id.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
        let docRef = isNew ? colRef.document() : colRef.document(item.id)

        var payload: [String: Any] = [
            "title": String(item.title.trimmingCharacters(in: .whitespacesAndNewlines).prefix(150)),
            "description": String(item.description.trimmingCharacters(in: .whitespacesAndNewlines).prefix(2000)),
            "price": max(0, item.price),
            "quantity": max(0, item.quantity),
            "unitLabel": item.unitLabel.isEmpty ? "шт." : String(item.unitLabel.prefix(40)),
            "zone": item.zone.isEmpty ? "Гараж" : String(item.zone.prefix(60)),
            "shelfLocation": item.shelfLocation.isEmpty ? "Стеллаж А1" : String(item.shelfLocation.prefix(120)),
            "qrCode": item.qrCode.isEmpty ? Self.generateQrCode() : String(item.qrCode.prefix(64)),
            "barcode": String(item.barcode.prefix(64)),
            "serialCode": String(item.serialCode.prefix(80)),
            "photoUri": String(item.photoUri.prefix(1000)),
            "hintPhotoUri": String(item.hintPhotoUri.prefix(1000)),
            "hintNote": String(item.hintNote.prefix(500)),
            "publishedPlatforms": Array(item.publishedPlatforms.prefix(10)),
            "publicationStatus": item.publicationStatus.isEmpty ? "draft" : item.publicationStatus,
            "isSelectedForPublish": item.isSelectedForPublish,
            "viewsCount": max(0, item.viewsCount),
            "inquiriesCount": max(0, item.inquiriesCount),
            "isVerifiedInInventory": item.isVerifiedInInventory,
            "updatedAt": FieldValue.serverTimestamp()
        ]

        if isNew {
            payload["userId"] = uid
            payload["createdAt"] = FieldValue.serverTimestamp()
            try await docRef.setData(payload)
        } else {
            try await docRef.updateData(payload)
        }
    }

    func deleteItem(itemId: String) async throws {
        let uid = try requireUserId()
        try await db.collection("users").document(uid).collection("items").document(itemId).delete()
    }

    func publishItemsToPlatforms(items: [GarageItem], targetPlatformKeys: [String]) async throws -> Int {
        let uid = try requireUserId()
        let cleanPlatforms = Array(NSOrderedSet(array: targetPlatformKeys)).compactMap { $0 as? String }
        guard !cleanPlatforms.isEmpty, !items.isEmpty else { return 0 }

        var count = 0
        for item in items where !item.id.isEmpty {
            let merged = Array(Set(item.publishedPlatforms + cleanPlatforms)).prefix(10)
            let status = merged.count >= 4 ? "published" : "partial"
            let docRef = db.collection("users").document(uid).collection("items").document(item.id)
            try await docRef.updateData([
                "publishedPlatforms": Array(merged),
                "publicationStatus": status,
                "viewsCount": item.viewsCount + Int64(cleanPlatforms.count * 12),
                "updatedAt": FieldValue.serverTimestamp()
            ])
            count += 1
        }
        return count
    }

    // MARK: - Platforms

    func observePlatforms(userId: String, onUpdate: @escaping ([PlatformConnection]) -> Void) -> ListenerRegistration {
        return db.collection("users").document(userId).collection("platforms")
            .whereField("userId", isEqualTo: userId)
            .addSnapshotListener { snapshot, error in
                guard let documents = snapshot?.documents, error == nil else { return }
                let list = documents.map { doc -> PlatformConnection in
                    let data = doc.data()
                    return PlatformConnection(
                        id: doc.documentID,
                        userId: data["userId"] as? String ?? userId,
                        platformKey: data["platformKey"] as? String ?? "avito",
                        platformName: data["platformName"] as? String ?? "Авито",
                        isConnected: data["isConnected"] as? Bool ?? false,
                        isSelectedForUpload: data["isSelectedForUpload"] as? Bool ?? true,
                        accountIdentifier: data["accountIdentifier"] as? String ?? "",
                        apiClientId: data["apiClientId"] as? String ?? "",
                        autoSync: data["autoSync"] as? Bool ?? true,
                        autoPromote: data["autoPromote"] as? Bool ?? false,
                        defaultContactPhone: data["defaultContactPhone"] as? String ?? "",
                        defaultCityAddress: data["defaultCityAddress"] as? String ?? "",
                        syncedCount: (data["syncedCount"] as? NSNumber)?.int64Value ?? 0,
                        lastSyncStatus: data["lastSyncStatus"] as? String ?? "Готово к подключению",
                        createdAt: (data["createdAt"] as? Timestamp)?.dateValue(),
                        updatedAt: (data["updatedAt"] as? Timestamp)?.dateValue()
                    )
                }
                onUpdate(list)
            }
    }

    func savePlatformConnection(_ connection: PlatformConnection) async throws {
        let uid = try requireUserId()
        let docRef = db.collection("users").document(uid).collection("platforms").document(connection.platformKey)
        let snap = try? await docRef.getDocument()

        var payload: [String: Any] = [
            "platformName": String(connection.platformName.prefix(60)),
            "isConnected": connection.isConnected,
            "isSelectedForUpload": connection.isSelectedForUpload,
            "accountIdentifier": String(connection.accountIdentifier.prefix(120)),
            "apiClientId": String(connection.apiClientId.prefix(200)),
            "autoSync": connection.autoSync,
            "autoPromote": connection.autoPromote,
            "defaultContactPhone": String(connection.defaultContactPhone.prefix(40)),
            "defaultCityAddress": String(connection.defaultCityAddress.prefix(200)),
            "syncedCount": max(0, connection.syncedCount),
            "lastSyncStatus": String(connection.lastSyncStatus.prefix(200)),
            "updatedAt": FieldValue.serverTimestamp()
        ]

        if snap?.exists == true {
            try await docRef.updateData(payload)
        } else {
            payload["userId"] = uid
            payload["platformKey"] = connection.platformKey
            payload["createdAt"] = FieldValue.serverTimestamp()
            try await docRef.setData(payload)
        }
    }

    // MARK: - Dialogs

    func observeDialogs(userId: String, onUpdate: @escaping ([BuyerDialog]) -> Void) -> ListenerRegistration {
        return db.collection("users").document(userId).collection("dialogs")
            .whereField("userId", isEqualTo: userId)
            .addSnapshotListener { snapshot, error in
                guard let documents = snapshot?.documents, error == nil else { return }
                let list = documents.map { doc -> BuyerDialog in
                    let data = doc.data()
                    return BuyerDialog(
                        id: doc.documentID,
                        userId: data["userId"] as? String ?? userId,
                        buyerName: data["buyerName"] as? String ?? "",
                        platformKey: data["platformKey"] as? String ?? "avito",
                        itemId: data["itemId"] as? String ?? "",
                        itemTitle: data["itemTitle"] as? String ?? "",
                        itemPrice: (data["itemPrice"] as? NSNumber)?.int64Value ?? 0,
                        itemShelfLocation: data["itemShelfLocation"] as? String ?? "",
                        itemQrCode: data["itemQrCode"] as? String ?? "",
                        itemPhotoUri: data["itemPhotoUri"] as? String ?? "",
                        lastMessage: data["lastMessage"] as? String ?? "",
                        timeLabel: data["timeLabel"] as? String ?? "Сейчас",
                        unreadCount: (data["unreadCount"] as? NSNumber)?.int64Value ?? 0,
                        messagesLog: data["messagesLog"] as? [String] ?? [],
                        createdAt: (data["createdAt"] as? Timestamp)?.dateValue(),
                        updatedAt: (data["updatedAt"] as? Timestamp)?.dateValue()
                    )
                }
                onUpdate(list)
            }
    }

    func createDialog(_ dialog: BuyerDialog) async throws {
        let uid = try requireUserId()
        let docRef = db.collection("users").document(uid).collection("dialogs").document()
        let timeNow = Self.currentTimeFormatted()
        let initialLog = dialog.messagesLog.isEmpty
            ? ["BUYER|\(timeNow)|\(dialog.lastMessage.prefix(300))"]
            : Array(dialog.messagesLog.prefix(50))

        try await docRef.setData([
            "userId": uid,
            "buyerName": dialog.buyerName.isEmpty ? "Покупатель" : String(dialog.buyerName.prefix(80)),
            "platformKey": dialog.platformKey,
            "itemId": String(dialog.itemId.prefix(128)),
            "itemTitle": dialog.itemTitle.isEmpty ? "Товар со склада" : String(dialog.itemTitle.prefix(150)),
            "itemPrice": max(0, dialog.itemPrice),
            "itemShelfLocation": String(dialog.itemShelfLocation.prefix(120)),
            "itemQrCode": String(dialog.itemQrCode.prefix(64)),
            "itemPhotoUri": String(dialog.itemPhotoUri.prefix(1000)),
            "lastMessage": dialog.lastMessage.isEmpty ? "Здравствуйте!" : String(dialog.lastMessage.prefix(500)),
            "timeLabel": timeNow,
            "unreadCount": max(0, dialog.unreadCount),
            "messagesLog": initialLog,
            "createdAt": FieldValue.serverTimestamp(),
            "updatedAt": FieldValue.serverTimestamp()
        ])
    }

    func sendDialogMessage(dialog: BuyerDialog, text: String, isFromMe: Bool = true) async throws {
        let uid = try requireUserId()
        guard !dialog.id.isEmpty else { return }
        let docRef = db.collection("users").document(uid).collection("dialogs").document(dialog.id)
        let timeNow = Self.currentTimeFormatted()
        let prefix = isFromMe ? "ME" : "BUYER"
        let cleanText = text.replacingOccurrences(of: "|", with: " ").trimmingCharacters(in: .whitespacesAndNewlines)
        let entry = "\(prefix)|\(timeNow)|\(cleanText.prefix(350))"
        let updatedLog = Array((dialog.messagesLog + [entry]).suffix(50))
        let unread: Int64 = isFromMe ? 0 : (dialog.unreadCount + 1)

        try await docRef.updateData([
            "lastMessage": String(cleanText.prefix(350)),
            "timeLabel": timeNow,
            "unreadCount": unread,
            "messagesLog": updatedLog,
            "updatedAt": FieldValue.serverTimestamp()
        ])
    }

    func markDialogRead(dialogId: String) async throws {
        let uid = try requireUserId()
        let docRef = db.collection("users").document(uid).collection("dialogs").document(dialogId)
        try await docRef.updateData([
            "unreadCount": 0,
            "updatedAt": FieldValue.serverTimestamp()
        ])
    }

    func deleteDialog(dialogId: String) async throws {
        let uid = try requireUserId()
        try await db.collection("users").document(uid).collection("dialogs").document(dialogId).delete()
    }

    // MARK: - Storage Zones

    func observeZones(userId: String, onUpdate: @escaping ([StorageZone]) -> Void) -> ListenerRegistration {
        return db.collection("users").document(userId).collection("zones")
            .whereField("userId", isEqualTo: userId)
            .addSnapshotListener { snapshot, error in
                guard let documents = snapshot?.documents, error == nil else { return }
                let list = documents.map { doc -> StorageZone in
                    let data = doc.data()
                    return StorageZone(
                        id: doc.documentID,
                        userId: data["userId"] as? String ?? userId,
                        name: data["name"] as? String ?? "",
                        capacity: (data["capacity"] as? NSNumber)?.int64Value ?? 50,
                        description: data["description"] as? String ?? "",
                        createdAt: (data["createdAt"] as? Timestamp)?.dateValue(),
                        updatedAt: (data["updatedAt"] as? Timestamp)?.dateValue()
                    )
                }
                onUpdate(list)
            }
    }

    func saveZone(_ zone: StorageZone) async throws {
        let uid = try requireUserId()
        let docRef = db.collection("users").document(uid).collection("zones").document()
        try await docRef.setData([
            "userId": uid,
            "name": String(zone.name.prefix(60)),
            "capacity": min(max(1, zone.capacity), 100000),
            "description": String(zone.description.prefix(200)),
            "createdAt": FieldValue.serverTimestamp(),
            "updatedAt": FieldValue.serverTimestamp()
        ])
    }

    func deleteZone(zoneId: String) async throws {
        let uid = try requireUserId()
        try await db.collection("users").document(uid).collection("zones").document(zoneId).delete()
    }

    // MARK: - Generators

    static func generateQrCode() -> String {
        let num = Int.random(in: 100...999)
        let roman = ["I", "II", "III", "IV", "V"].randomElement() ?? "I"
        return "#GAR-\(num)-\(roman)"
    }

    static func generateBarcode() -> String {
        let digits = (1...9).map { _ in String(Int.random(in: 0...9)) }.joined()
        return "4607\(digits)"
    }

    static func currentTimeFormatted() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: Date())
    }
}
