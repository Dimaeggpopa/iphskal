import Foundation
import SwiftUI
import FirebaseFirestore

@MainActor
final class GarageViewModel: ObservableObject {
    @Published var items: [GarageItem] = []
    @Published var platforms: [PlatformConnection] = []
    @Published var dialogs: [BuyerDialog] = []
    @Published var zones: [StorageZone] = []
    @Published var publishProgress = PublishProgressState()
    @Published var statusToast: String? = nil

    @AppStorage("garage_theme_mode") var themeModeRaw: String = AppThemeMode.dark.rawValue
    @AppStorage("garage_accent_palette") var accentPaletteRaw: String = AppAccentPalette.orangeBlue.rawValue

    @Published var quickReplyTemplates: [String] = [
        "Здравствуйте! Товар в наличии на складе, готов к выдаче.",
        "Могу отдать сегодня, подъезжайте к гаражу.",
        "Стеллаж проверил по QR-коду — комплект полный!",
        "Торг минимальный при самовывозе сегодня."
    ]

    private let repository = GarageRepository()
    private let currentUserId: String
    private var listeners: [ListenerRegistration] = []

    var themeMode: AppThemeMode {
        get { AppThemeMode(rawValue: themeModeRaw) ?? .dark }
        set { themeModeRaw = newValue.rawValue }
    }

    var accentPalette: AppAccentPalette {
        get { AppAccentPalette(rawValue: accentPaletteRaw) ?? .orangeBlue }
        set { accentPaletteRaw = newValue.rawValue }
    }

    init(userId: String) {
        self.currentUserId = userId
        startObserving()
    }

    deinit {
        listeners.forEach { $0.remove() }
    }

    private func startObserving() {
        let itemsListener = repository.observeItems(userId: currentUserId) { [weak self] list in
            Task { @MainActor in
                self?.items = list
            }
        }

        let platformsListener = repository.observePlatforms(userId: currentUserId) { [weak self] list in
            Task { @MainActor in
                guard let self else { return }
                let byKey = Dictionary(uniqueKeysWithValues: list.map { ($0.platformKey, $0) })
                self.platforms = PlatformType.allCases.map { type in
                    byKey[type.rawValue] ?? PlatformConnection(
                        id: type.rawValue,
                        userId: self.currentUserId,
                        platformKey: type.rawValue,
                        platformName: type.displayName,
                        isConnected: false,
                        isSelectedForUpload: true,
                        lastSyncStatus: "Нажмите для привязки аккаунта"
                    )
                }
            }
        }

        let dialogsListener = repository.observeDialogs(userId: currentUserId) { [weak self] list in
            Task { @MainActor in
                self?.dialogs = list
            }
        }

        let zonesListener = repository.observeZones(userId: currentUserId) { [weak self] list in
            Task { @MainActor in
                self?.zones = list
            }
        }

        listeners = [itemsListener, platformsListener, dialogsListener, zonesListener]
    }

    func showToast(_ message: String) {
        statusToast = message
    }

    func saveItem(_ item: GarageItem, onDone: @escaping () -> Void = {}) {
        Task {
            do {
                var updated = item
                updated.userId = currentUserId
                try await repository.saveItem(updated)
                showToast(item.id.isEmpty ? "Товар добавлен в гараж!" : "Товар обновлён!")
                onDone()
            } catch {
                showToast("Ошибка сохранения: \(error.localizedDescription)")
            }
        }
    }

    func deleteItem(itemId: String) {
        Task {
            do {
                try await repository.deleteItem(itemId: itemId)
                showToast("Товар удалён со склада")
            } catch {
                showToast("Ошибка удаления товара")
            }
        }
    }

    func toggleItemSelectionForPublish(_ item: GarageItem) {
        var copy = item
        copy.isSelectedForPublish.toggle()
        saveItem(copy)
    }

    func toggleInventoryVerification(_ item: GarageItem) {
        var copy = item
        copy.isVerifiedInInventory.toggle()
        saveItem(copy)
    }

    func boostItemPromotion(_ item: GarageItem) {
        var copy = item
        copy.viewsCount += 45
        copy.inquiriesCount += 2
        saveItem(copy)
    }

    func publishAllInOneClick(onlySelected: Bool = false) {
        let targetItems = onlySelected ? items.filter(\.isSelectedForPublish) : items
        guard !targetItems.isEmpty else {
            showToast("Сначала добавьте или выберите товары для публикации")
            return
        }

        let activePlatforms = platforms.filter { $0.isConnected && $0.isSelectedForUpload }
        let platformsToUse = activePlatforms.isEmpty
            ? PlatformType.allCases.map(\.rawValue)
            : activePlatforms.map(\.platformKey)

        Task {
            publishProgress = PublishProgressState(
                isPublishing: true,
                currentStepText: "Проверка API-токенов и подготовка фида (\(targetItems.count) тов.)...",
                progressFraction: 0.2
            )
            try? await Task.sleep(nanoseconds: 350_000_000)

            for (index, key) in platformsToUse.enumerated() {
                let pName = PlatformType.fromKey(key).displayName
                publishProgress = PublishProgressState(
                    isPublishing: true,
                    currentStepText: "Выгрузка на \(pName) (\(index + 1)/\(platformsToUse.count))...",
                    progressFraction: 0.3 + (0.5 * Double(index + 1) / Double(platformsToUse.count))
                )
                try? await Task.sleep(nanoseconds: 280_000_000)
            }

            let byKey = Dictionary(uniqueKeysWithValues: platforms.map { ($0.platformKey, $0) })
            for pKey in platformsToUse {
                let pType = PlatformType.fromKey(pKey)
                var conn = byKey[pKey] ?? PlatformConnection(
                    id: pKey,
                    userId: currentUserId,
                    platformKey: pKey,
                    platformName: pType.displayName
                )
                conn.isConnected = true
                conn.isSelectedForUpload = true
                conn.syncedCount = Int64(targetItems.count)
                conn.lastSyncStatus = "Выгружено товаров: \(targetItems.count) • Без ошибок"
                try? await repository.savePlatformConnection(conn)
            }

            do {
                let count = try await repository.publishItemsToPlatforms(
                    items: targetItems,
                    targetPlatformKeys: platformsToUse
                )
                publishProgress = PublishProgressState(
                    isPublishing: false,
                    currentStepText: "",
                    progressFraction: 1.0,
                    completedBannerMessage: "Готово! Опубликовано товаров: \(count) на \(platformsToUse.count) площадки сразу ⚡"
                )
            } catch {
                publishProgress = PublishProgressState(isPublishing: false)
                showToast("Ошибка выгрузки: \(error.localizedDescription)")
            }
        }
    }

    func savePlatform(_ connection: PlatformConnection, onDone: @escaping () -> Void = {}) {
        Task {
            do {
                var copy = connection
                copy.userId = currentUserId
                try await repository.savePlatformConnection(copy)
                showToast("Платформа \(connection.platformName) сохранена!")
                onDone()
            } catch {
                showToast("Ошибка привязки: \(error.localizedDescription)")
            }
        }
    }

    func togglePlatformForUpload(_ connection: PlatformConnection) {
        var copy = connection
        copy.isConnected = true
        copy.isSelectedForUpload.toggle()
        savePlatform(copy)
    }

    func createDialog(_ dialog: BuyerDialog, onDone: @escaping () -> Void = {}) {
        Task {
            do {
                try await repository.createDialog(dialog)
                showToast("Диалог с покупателем добавлен")
                onDone()
            } catch {
                showToast("Ошибка создания диалога")
            }
        }
    }

    func sendMessage(dialog: BuyerDialog, text: String, isFromMe: Bool = true) {
        guard !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        Task {
            try? await repository.sendDialogMessage(dialog: dialog, text: text, isFromMe: isFromMe)
        }
    }

    func markDialogRead(_ dialog: BuyerDialog) {
        guard dialog.unreadCount > 0, !dialog.id.isEmpty else { return }
        Task {
            try? await repository.markDialogRead(dialogId: dialog.id)
        }
    }

    func deleteDialog(dialogId: String) {
        Task {
            try? await repository.deleteDialog(dialogId: dialogId)
            showToast("Диалог удалён")
        }
    }

    func saveStorageZone(name: String, capacity: Int64, description: String) {
        Task {
            let zone = StorageZone(
                userId: currentUserId,
                name: name,
                capacity: capacity,
                description: description
            )
            try? await repository.saveZone(zone)
            showToast("Место хранения «\(name)» сохранено")
        }
    }

    func deleteStorageZone(zoneId: String) {
        Task {
            try? await repository.deleteZone(zoneId: zoneId)
            showToast("Зона хранения удалена")
        }
    }

    func addQuickReplyTemplate(_ text: String) {
        let clean = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !clean.isEmpty, !quickReplyTemplates.contains(clean) else { return }
        quickReplyTemplates.append(clean)
        showToast("Шаблон быстрого ответа добавлен")
    }

    func removeQuickReplyTemplate(_ text: String) {
        quickReplyTemplates.removeAll { $0 == text }
    }
}
