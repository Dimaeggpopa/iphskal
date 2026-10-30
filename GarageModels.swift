import Foundation
import SwiftUI
import FirebaseFirestore

enum PlatformType: String, CaseIterable, Identifiable, Codable {
    case avito = "avito"
    case youla = "youla"
    case vk = "vk"
    case yandex = "yandex"

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .avito: return "Авито"
        case .youla: return "Юла"
        case .vk: return "ВК Маркет"
        case .yandex: return "Яндекс.Объявления"
        }
    }

    var shortName: String {
        switch self {
        case .avito: return "Авито"
        case .youla: return "Юла"
        case .vk: return "ВК"
        case .yandex: return "Яндекс"
        }
    }

    var apiPortalHint: String {
        switch self {
        case .avito: return "Avito Автозагрузка / API Pro"
        case .youla: return "Юла Магазин / Фид выгрузки"
        case .vk: return "VK Товары Сообщества API"
        case .yandex: return "Яндекс Товары и Объявления"
        }
    }

    var brandColor: Color {
        switch self {
        case .avito: return Color(red: 0.0, green: 0.67, blue: 1.0)
        case .youla: return Color(red: 0.56, green: 0.27, blue: 0.99)
        case .vk: return Color(red: 0.0, green: 0.47, blue: 1.0)
        case .yandex: return Color(red: 1.0, green: 0.8, blue: 0.0)
        }
    }

    static func fromKey(_ key: String) -> PlatformType {
        PlatformType(rawValue: key) ?? .avito
    }
}

enum AppThemeMode: String, CaseIterable, Identifiable {
    case dark = "dark"
    case light = "light"
    case system = "system"

    var id: String { rawValue }

    var label: String {
        switch self {
        case .dark: return "Тёмная (Гараж)"
        case .light: return "Светлая"
        case .system: return "Авто iOS"
        }
    }
}

enum AppAccentPalette: String, CaseIterable, Identifiable {
    case orangeBlue = "orange_blue"
    case cyberBlue = "cyber_blue"
    case emeraldPro = "emerald_pro"

    var id: String { rawValue }

    var label: String {
        switch self {
        case .orangeBlue: return "Оранжевый Драйв + Синий"
        case .cyberBlue: return "Кибер-Синий (iOS Blue)"
        case .emeraldPro: return "Изумрудный Склад"
        }
    }

    var primaryColor: Color {
        switch self {
        case .orangeBlue: return Color(red: 1.0, green: 0.43, blue: 0.0)
        case .cyberBlue: return Color(red: 0.04, green: 0.52, blue: 1.0)
        case .emeraldPro: return Color(red: 0.19, green: 0.82, blue: 0.35)
        }
    }
}

struct GarageItem: Identifiable, Codable, Equatable {
    var id: String = ""
    var userId: String = ""
    var title: String = ""
    var description: String = ""
    var price: Int64 = 0
    var quantity: Int64 = 1
    var unitLabel: String = "шт."
    var zone: String = "Гараж"
    var shelfLocation: String = ""
    var qrCode: String = ""
    var barcode: String = ""
    var serialCode: String = ""
    var photoUri: String = ""
    var hintPhotoUri: String = ""
    var hintNote: String = ""
    var publishedPlatforms: [String] = []
    var publicationStatus: String = "draft"
    var isSelectedForPublish: Bool = true
    var viewsCount: Int64 = 0
    var inquiriesCount: Int64 = 0
    var isVerifiedInInventory: Bool = false
    var createdAt: Date? = nil
    var updatedAt: Date? = nil
}

struct PlatformConnection: Identifiable, Codable, Equatable {
    var id: String = ""
    var userId: String = ""
    var platformKey: String = "avito"
    var platformName: String = "Авито"
    var isConnected: Bool = false
    var isSelectedForUpload: Bool = true
    var accountIdentifier: String = ""
    var apiClientId: String = ""
    var autoSync: Bool = true
    var autoPromote: Bool = false
    var defaultContactPhone: String = ""
    var defaultCityAddress: String = ""
    var syncedCount: Int64 = 0
    var lastSyncStatus: String = "Готово к подключению"
    var createdAt: Date? = nil
    var updatedAt: Date? = nil
}

struct ChatMessage: Identifiable, Equatable {
    let id: String
    let isOutgoing: Bool
    let time: String
    let text: String
}

struct BuyerDialog: Identifiable, Codable, Equatable {
    var id: String = ""
    var userId: String = ""
    var buyerName: String = ""
    var platformKey: String = "avito"
    var itemId: String = ""
    var itemTitle: String = ""
    var itemPrice: Int64 = 0
    var itemShelfLocation: String = ""
    var itemQrCode: String = ""
    var itemPhotoUri: String = ""
    var lastMessage: String = ""
    var timeLabel: String = "Сейчас"
    var unreadCount: Int64 = 0
    var messagesLog: [String] = []
    var createdAt: Date? = nil
    var updatedAt: Date? = nil

    func parsedMessages() -> [ChatMessage] {
        messagesLog.enumerated().map { index, raw in
            let parts = raw.split(separator: "|", maxSplits: 2, omittingEmptySubsequences: false).map(String.init)
            if parts.count == 3 {
                return ChatMessage(
                    id: "\(id)-\(index)",
                    isOutgoing: parts[0] == "ME",
                    time: parts[1],
                    text: parts[2]
                )
            } else {
                return ChatMessage(
                    id: "\(id)-\(index)",
                    isOutgoing: false,
                    time: timeLabel,
                    text: raw
                )
            }
        }
    }
}

struct StorageZone: Identifiable, Codable, Equatable {
    var id: String = ""
    var userId: String = ""
    var name: String = ""
    var capacity: Int64 = 50
    var description: String = ""
    var createdAt: Date? = nil
    var updatedAt: Date? = nil
}

struct PublishProgressState: Equatable {
    var isPublishing: Bool = false
    var currentStepText: String = ""
    var progressFraction: Double = 0.0
    var completedBannerMessage: String? = nil
}

func formatRubles(_ amount: Int64) -> String {
    let formatter = NumberFormatter()
    formatter.numberStyle = .decimal
    formatter.groupingSeparator = " "
    let formatted = formatter.string(from: NSNumber(value: amount)) ?? "\(amount)"
    return "\(formatted) ₽"
}
